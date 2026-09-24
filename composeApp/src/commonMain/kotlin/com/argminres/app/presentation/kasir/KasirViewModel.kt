package com.argminres.app.presentation.kasir

import com.argminres.app.MenuItem
import com.argminres.app.PosCartItem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// ─── Payment Method ─────────────────────────────────────────────────────────

sealed class PaymentMethod {
    object Tunai : PaymentMethod()
    object Qris : PaymentMethod()
}

// ─── UI State ────────────────────────────────────────────────────────────────

data class KasirUiState(
    val cart: List<PosCartItem> = emptyList(),
    val subtotal: Long = 0L,
    val pajak: Long = 0L,
    val totalTagihan: Long = 0L,
    val paymentMethod: PaymentMethod = PaymentMethod.Tunai,
    val cashPaidInput: String = "",
    val dibayarkan: Long = 0L,
    val kembalian: Long = 0L,
    val qrisTotalAmount: Long = 0L,   // total + 1000 fee
    val isPaymentDialogOpen: Boolean = false,
    val isTransactionComplete: Boolean = false,
    val isSuccessDialogOpen: Boolean = false,
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val lastOrderId: String = "",
    val lastPaidAmount: Long = 0L,
    val lastOrderTotal: Long = 0L,
    val lastChangeAmount: Long = 0L,
    val lastPaymentMethod: String = "Tunai"
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

class KasirViewModel {

    var state by mutableStateOf(KasirUiState())
        private set

    companion object {
        const val TAX_RATE = 0.10           // PB1 10%
        const val QRIS_FEE = 1000L          // Biaya layanan QRIS
    }

    // ── Cart ─────────────────────────────────────────────────────────────────

    fun addToCart(item: MenuItem) {
        val idx = state.cart.indexOfFirst { it.item.id == item.id }
        val newCart = state.cart.toMutableList()
        if (idx >= 0) {
            val cur = newCart[idx]
            if (cur.quantity < item.stock) {
                newCart[idx] = cur.copy(quantity = cur.quantity + 1)
            }
        } else {
            if (item.stock > 0) newCart.add(PosCartItem(item = item, quantity = 1))
        }
        updateCartState(newCart)
    }

    fun removeFromCart(item: MenuItem) {
        val idx = state.cart.indexOfFirst { it.item.id == item.id }
        val newCart = state.cart.toMutableList()
        if (idx >= 0) {
            val cur = newCart[idx]
            if (cur.quantity > 1) newCart[idx] = cur.copy(quantity = cur.quantity - 1)
            else newCart.removeAt(idx)
        }
        updateCartState(newCart)
    }

    fun clearCart() = updateCartState(emptyList())

    fun setQuantity(item: MenuItem, qty: Int) {
        val newCart = state.cart.toMutableList()
        val idx = newCart.indexOfFirst { it.item.id == item.id }
        when {
            qty <= 0  -> if (idx >= 0) newCart.removeAt(idx)
            idx >= 0  -> newCart[idx] = newCart[idx].copy(quantity = qty.coerceAtMost(item.stock))
            else      -> newCart.add(PosCartItem(item = item, quantity = qty.coerceAtMost(item.stock)))
        }
        updateCartState(newCart)
    }

    private fun updateCartState(newCart: List<PosCartItem>) {
        val subtotal = newCart.sumOf { it.item.price * it.quantity }
        val pajak    = (subtotal * TAX_RATE).toLong()
        val total    = subtotal + pajak
        state = state.copy(
            cart            = newCart,
            subtotal        = subtotal,
            pajak           = pajak,
            totalTagihan    = total,
            qrisTotalAmount = total + QRIS_FEE,
            kembalian       = (state.dibayarkan - total).coerceAtLeast(0L)
        )
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    fun setSelectedCategory(category: String) {
        state = state.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun filterMenu(items: List<MenuItem>): List<MenuItem> {
        return items.filter {
            (state.selectedCategory == "Semua" || it.category == state.selectedCategory) &&
            (state.searchQuery.isBlank() || it.name.contains(state.searchQuery, ignoreCase = true))
        }
    }

    // ── Payment Dialog ───────────────────────────────────────────────────────

    fun openPaymentDialog() {
        state = state.copy(
            isPaymentDialogOpen = true,
            cashPaidInput = "",
            dibayarkan = 0L,
            kembalian = 0L,
            paymentMethod = PaymentMethod.Tunai
        )
    }

    fun closePaymentDialog() {
        state = state.copy(isPaymentDialogOpen = false)
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        state = state.copy(paymentMethod = method)
    }

    fun setDibayarkan(amount: Long) {
        val inputStr = if (amount == 0L) "" else amount.toString()
        val kembalian = (amount - state.totalTagihan).coerceAtLeast(0L)
        state = state.copy(
            cashPaidInput = inputStr,
            dibayarkan = amount,
            kembalian = kembalian
        )
    }

    fun setCashPaidInput(input: String) {
        val digits = input.filter { it.isDigit() }.take(10)
        val amount = digits.toLongOrNull() ?: 0L
        val kembalian = (amount - state.totalTagihan).coerceAtLeast(0L)
        state = state.copy(
            cashPaidInput = digits,
            dibayarkan = amount,
            kembalian = kembalian
        )
    }

    fun clearCashInput() {
        setCashPaidInput("")
    }

    fun addDenomination(amount: Long) = setDibayarkan(state.dibayarkan + amount)

    fun setUangPas() = setDibayarkan(state.totalTagihan)

    // ── Complete Transaction ─────────────────────────────────────────────────

    fun completePayment(orderId: String = "#ORD-TEST"): Boolean {
        if (state.cart.isEmpty()) return false
        if (state.paymentMethod is PaymentMethod.Tunai && state.dibayarkan < state.totalTagihan) return false

        val isTunai = state.paymentMethod is PaymentMethod.Tunai
        state = state.copy(
            isPaymentDialogOpen   = false,
            isTransactionComplete = true,
            isSuccessDialogOpen   = true,
            lastOrderId           = orderId,
            lastPaidAmount        = if (isTunai) state.dibayarkan else state.qrisTotalAmount,
            lastOrderTotal        = if (isTunai) state.totalTagihan else state.qrisTotalAmount,
            lastChangeAmount      = if (isTunai) state.kembalian else 0L,
            lastPaymentMethod     = if (isTunai) "Tunai" else "QRIS Dinamis"
        )
        return true
    }

    fun closeSuccessDialog() {
        state = state.copy(isSuccessDialogOpen = false)
    }

    fun resetAfterTransaction() {
        state = KasirUiState(
            selectedCategory = state.selectedCategory,
            searchQuery = state.searchQuery
        )
    }

    // ── Computed ─────────────────────────────────────────────────────────────

    val isCashSufficient: Boolean get() = state.dibayarkan >= state.totalTagihan
    val cartItemCount:    Int     get() = state.cart.sumOf { it.quantity }
    val canPay:           Boolean get() = state.cart.isNotEmpty() && state.totalTagihan > 0
}
