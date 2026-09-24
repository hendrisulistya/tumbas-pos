package com.argomin.app.presentation.kasir

import com.argomin.app.domain.model.MenuItem
import kotlin.test.*

class KasirViewModelTest {

    private val vm = KasirViewModel()

    // ── Sample data ──────────────────────────────────────────────────────────

    private val rendang = MenuItem(
        id = "1001", name = "Rendang Daging", category = "Makanan",
        price = 35000L, cost = 22000L, stock = 35
    )
    private val esTeh = MenuItem(
        id = "1016", name = "Es Teh Manis", category = "Minuman",
        price = 5000L, cost = 1200L, stock = 80
    )
    private val nasiPutih = MenuItem(
        id = "1011", name = "Nasi Putih", category = "Makanan",
        price = 5000L, cost = 2000L, stock = 100
    )

    // ── Cart Tests ───────────────────────────────────────────────────────────

    @Test
    fun `addToCart - menambah item baru ke keranjang`() {
        vm.addToCart(rendang)
        assertEquals(1, vm.state.cart.size)
        assertEquals(1, vm.state.cart[0].quantity)
        assertEquals("1001", vm.state.cart[0].item.id)
    }

    @Test
    fun `addToCart - increment quantity jika sudah ada`() {
        vm.addToCart(rendang)
        vm.addToCart(rendang)
        assertEquals(1, vm.state.cart.size)
        assertEquals(2, vm.state.cart[0].quantity)
    }

    @Test
    fun `addToCart - tidak bisa melebihi stok`() {
        val limitedItem = rendang.copy(stock = 2)
        vm.addToCart(limitedItem)
        vm.addToCart(limitedItem)
        vm.addToCart(limitedItem) // ini harus diabaikan
        assertEquals(2, vm.state.cart[0].quantity)
    }

    @Test
    fun `addToCart - item dengan stok 0 tidak masuk keranjang`() {
        val outOfStock = rendang.copy(stock = 0)
        vm.addToCart(outOfStock)
        assertTrue(vm.state.cart.isEmpty())
    }

    @Test
    fun `removeFromCart - kurangi quantity`() {
        vm.addToCart(rendang)
        vm.addToCart(rendang)
        vm.removeFromCart(rendang)
        assertEquals(1, vm.state.cart[0].quantity)
    }

    @Test
    fun `removeFromCart - hapus item jika quantity jadi 0`() {
        vm.addToCart(rendang)
        vm.removeFromCart(rendang)
        assertTrue(vm.state.cart.isEmpty())
    }

    @Test
    fun `clearCart - mengosongkan semua item`() {
        vm.addToCart(rendang)
        vm.addToCart(esTeh)
        vm.clearCart()
        assertTrue(vm.state.cart.isEmpty())
    }

    @Test
    fun `cartItemCount - hitung total item`() {
        vm.addToCart(rendang)
        vm.addToCart(rendang) // 2x rendang
        vm.addToCart(esTeh)   // 1x esTeh
        assertEquals(3, vm.cartItemCount)
    }

    // ── Kalkulasi Total ──────────────────────────────────────────────────────

    @Test
    fun `kalkulasi subtotal 1 item`() {
        vm.addToCart(rendang)
        assertEquals(35000L, vm.state.subtotal)
    }

    @Test
    fun `kalkulasi pajak PB1 10 persen`() {
        vm.addToCart(rendang)  // 35.000
        assertEquals(3500L, vm.state.pajak)
    }

    @Test
    fun `kalkulasi total tagihan subtotal + pajak`() {
        vm.addToCart(rendang)  // 35.000 + 3.500 pajak = 38.500
        assertEquals(38500L, vm.state.totalTagihan)
    }

    @Test
    fun `kalkulasi multi-item benar`() {
        vm.addToCart(rendang)     // 35.000
        vm.addToCart(esTeh)       // 5.000
        vm.addToCart(nasiPutih)   // 5.000
        // subtotal: 45.000, pajak 10%: 4.500, total: 49.500
        assertEquals(45000L, vm.state.subtotal)
        assertEquals(4500L, vm.state.pajak)
        assertEquals(49500L, vm.state.totalTagihan)
    }

    @Test
    fun `kalkulasi qty lebih dari 1`() {
        vm.setQuantity(rendang, 3)  // 3 x 35.000 = 105.000
        assertEquals(105000L, vm.state.subtotal)
    }

    // ── QRIS Fee ─────────────────────────────────────────────────────────────

    @Test
    fun `qrisTotalAmount = totalTagihan + fee 1000`() {
        vm.addToCart(rendang)  // totalTagihan = 38.500
        assertEquals(39500L, vm.state.qrisTotalAmount)
    }

    @Test
    fun `qrisFee adalah 1000 rupiah`() {
        assertEquals(1000L, KasirViewModel.QRIS_FEE)
    }

    // ── Payment Tunai ────────────────────────────────────────────────────────

    @Test
    fun `setDibayarkan - kembalian dihitung benar`() {
        vm.addToCart(rendang)  // total 38.500
        vm.openPaymentDialog()
        vm.setDibayarkan(50000L)
        assertEquals(11500L, vm.state.kembalian)
    }

    @Test
    fun `setDibayarkan - kembalian tidak negatif jika kurang bayar`() {
        vm.addToCart(rendang)  // total 38.500
        vm.openPaymentDialog()
        vm.setDibayarkan(20000L)
        assertEquals(0L, vm.state.kembalian)
    }

    @Test
    fun `addDenomination - akumulatif`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.addDenomination(20000L)
        vm.addDenomination(20000L)
        assertEquals(40000L, vm.state.dibayarkan)
        assertEquals(1500L, vm.state.kembalian)
    }

    @Test
    fun `setUangPas - dibayarkan = total, kembalian = 0`() {
        vm.addToCart(rendang)  // total 38.500
        vm.openPaymentDialog()
        vm.setUangPas()
        assertEquals(38500L, vm.state.dibayarkan)
        assertEquals(0L, vm.state.kembalian)
    }

    @Test
    fun `isCashSufficient - true jika cukup`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setDibayarkan(38500L)
        assertTrue(vm.isCashSufficient)
    }

    @Test
    fun `isCashSufficient - false jika kurang`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setDibayarkan(30000L)
        assertFalse(vm.isCashSufficient)
    }

    // ── completePayment ──────────────────────────────────────────────────────

    @Test
    fun `completePayment Tunai - gagal jika keranjang kosong`() {
        vm.openPaymentDialog()
        val result = vm.completePayment()
        assertFalse(result)
    }

    @Test
    fun `completePayment Tunai - gagal jika kurang bayar`() {
        vm.addToCart(rendang)  // total 38.500
        vm.openPaymentDialog()
        vm.setDibayarkan(10000L)
        val result = vm.completePayment()
        assertFalse(result)
    }

    @Test
    fun `completePayment Tunai - berhasil jika cukup bayar`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setDibayarkan(50000L)
        val result = vm.completePayment("#ORD-TEST-001")
        assertTrue(result)
        assertTrue(vm.state.isTransactionComplete)
        assertEquals("#ORD-TEST-001", vm.state.lastOrderId)
    }

    @Test
    fun `completePayment QRIS - berhasil tanpa cek dibayarkan`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.selectPaymentMethod(PaymentMethod.Qris)
        // QRIS tidak butuh cek dibayarkan (pembayaran via app)
        val result = vm.completePayment()
        assertTrue(result)
    }

    @Test
    fun `resetAfterTransaction - kembali ke state kosong`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setDibayarkan(50000L)
        vm.completePayment()
        vm.resetAfterTransaction()
        assertEquals(KasirUiState(), vm.state)
        assertTrue(vm.state.cart.isEmpty())
        assertEquals(0L, vm.state.totalTagihan)
    }

    // ── canPay ───────────────────────────────────────────────────────────────

    @Test
    fun `canPay - false jika keranjang kosong`() {
        assertFalse(vm.canPay)
    }

    @Test
    fun `canPay - true jika ada item di keranjang`() {
        vm.addToCart(rendang)
        assertTrue(vm.canPay)
    }

    // ── Filter & Search Menu ──────────────────────────────────────────────────

    @Test
    fun `filterMenu - filter berdasarkan kategori`() {
        val allItems = listOf(rendang, esTeh, nasiPutih)
        vm.setSelectedCategory("Minuman")
        val filtered = vm.filterMenu(allItems)
        assertEquals(1, filtered.size)
        assertEquals("Es Teh Manis", filtered[0].name)
    }

    @Test
    fun `filterMenu - filter berdasarkan kata kunci pencarian`() {
        val allItems = listOf(rendang, esTeh, nasiPutih)
        vm.setSearchQuery("putih")
        val filtered = vm.filterMenu(allItems)
        assertEquals(1, filtered.size)
        assertEquals("Nasi Putih", filtered[0].name)
    }

    @Test
    fun `filterMenu - kategori Semua menampilkan seluruh menu`() {
        val allItems = listOf(rendang, esTeh, nasiPutih)
        vm.setSelectedCategory("Semua")
        vm.setSearchQuery("")
        val filtered = vm.filterMenu(allItems)
        assertEquals(3, filtered.size)
    }

    // ── Cash Input & Dialog State ─────────────────────────────────────────────

    @Test
    fun `setCashPaidInput - update dibayarkan dan kembalian secara tepat`() {
        vm.addToCart(rendang) // Total 38.500
        vm.openPaymentDialog()
        vm.setCashPaidInput("50000")
        assertEquals("50000", vm.state.cashPaidInput)
        assertEquals(50000L, vm.state.dibayarkan)
        assertEquals(11500L, vm.state.kembalian)
    }

    @Test
    fun `clearCashInput - mengosongkan input dan reset nominal`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setCashPaidInput("50000")
        vm.clearCashInput()
        assertEquals("", vm.state.cashPaidInput)
        assertEquals(0L, vm.state.dibayarkan)
        assertEquals(0L, vm.state.kembalian)
    }

    @Test
    fun `closeSuccessDialog - menutup dialog sukses`() {
        vm.addToCart(rendang)
        vm.openPaymentDialog()
        vm.setDibayarkan(50000L)
        vm.completePayment()
        assertTrue(vm.state.isSuccessDialogOpen)
        vm.closeSuccessDialog()
        assertFalse(vm.state.isSuccessDialogOpen)
    }

    @Test
    fun `test QrCode encoder generates valid matrix`() {
        val payload = "00020101021226610014COM.GO-JEK.WWW01189360091433961813320210G3961813320303UMI51440014ID.CO.QRIS.WWW0215ID10264699901090303UMI5204581253033605405395005802ID5925RM ARGO MINANG 3 KEPEK, P6011KULON PROGO61055565262070703A02630483B1"
        val qr = com.argomin.app.util.QrCode.encodeText(payload, com.argomin.app.util.QrCode.Ecc.MEDIUM)
        assertEquals(61, qr.size) // Version 11 QR Code (11 * 4 + 17 = 61)
        assertTrue(qr.getModule(0, 0)) // Top-left finder pattern corner
        assertTrue(qr.getModule(qr.size - 1, 0)) // Top-right finder pattern corner
        assertTrue(qr.getModule(0, qr.size - 1)) // Bottom-left finder pattern corner
        assertFalse(qr.getModule(7, 7)) // Separator light module
    }
}
