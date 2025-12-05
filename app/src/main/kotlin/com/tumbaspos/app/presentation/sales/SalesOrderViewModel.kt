package com.tumbaspos.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.CustomerEntity
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.domain.repository.CustomerRepository
import com.tumbaspos.app.domain.usecase.sales.GetSalesOrdersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.tumbaspos.app.data.local.dao.SalesOrderWithItems

data class SalesOrderUiState(
    val orders: List<SalesOrderWithItems> = emptyList(),
    val customers: List<CustomerEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddCustomerDialogOpen: Boolean = false,
    val selectedCustomer: CustomerEntity? = null // For editing
)

class SalesOrderViewModel(
    private val getSalesOrdersUseCase: GetSalesOrdersUseCase,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesOrderUiState())
    val uiState: StateFlow<SalesOrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
        loadCustomers()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getSalesOrdersUseCase().collect { orders ->
                    _uiState.update { it.copy(orders = orders, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _uiState.update { it.copy(customers = customers) }
            }
        }
    }

    fun onAddCustomerClick() {
        _uiState.update { it.copy(isAddCustomerDialogOpen = true, selectedCustomer = null) }
    }

    fun onEditCustomerClick(customer: CustomerEntity) {
        _uiState.update { it.copy(isAddCustomerDialogOpen = true, selectedCustomer = customer) }
    }

    fun onDismissCustomerDialog() {
        _uiState.update { it.copy(isAddCustomerDialogOpen = false, selectedCustomer = null) }
    }

    fun onSaveCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                customerRepository.insertCustomer(customer)
            } else {
                customerRepository.updateCustomer(customer)
            }
            onDismissCustomerDialog()
        }
    }
}
