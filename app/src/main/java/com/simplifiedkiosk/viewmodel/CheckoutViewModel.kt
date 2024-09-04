package com.simplifiedkiosk.viewmodel

import android.location.Address
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _checkoutState = MutableStateFlow<CheckoutStateResults?>(null)
    val checkoutState: StateFlow<CheckoutStateResults?> = _checkoutState

    private val checkoutScreenState: CheckoutState = CheckoutState(
        cartSubTotal = 0.0,
        totalCartQuantity = 0,
        cartProducts = emptyList()
    )

    fun processPayment(paymentMethod: String) {
        // Simulate payment processing logic
        // Here we would integrate with a real payment gateway
        viewModelScope.launch {
            if (paymentMethod.isNotBlank()) {
                _checkoutState.value =
                    CheckoutStateResults.SuccessfulPaymentProcessed(paymentMethod.isNotBlank())
            } else {
                _checkoutState.value =
                    CheckoutStateResults.FailedPaymentProcessing(Throwable("Payment Failed. Please try again"))
            }
        }
    }

    fun loadCheckOutStateFromCart(state: CheckoutState) {
        viewModelScope.launch {
            _checkoutState.value = CheckoutStateResults.ReceivedProductsFromCartSummary(
                checkoutScreenState.copy(
                    cartSubTotal = state.cartSubTotal,
                    totalCartQuantity = state.totalCartQuantity,
                    cartProducts = state.cartProducts,
                    address = state.address
                )
            )
        }
    }

    fun emptyCart() {
        viewModelScope.launch {
            cartRepository.clearCart().collectLatest { result ->
                result.fold({ didClear ->
                    _checkoutState.value = CheckoutStateResults.ClearedCartSuccess(didClear)
                }, {
                    _checkoutState.value = CheckoutStateResults.FailedClearedCart(it)
                })
            }
        }
    }

    fun getCartSize() = cartRepository.getCartTotalQuantity()
}

sealed class CheckoutStateResults {
    object Loading : CheckoutStateResults()
    data class ReceivedProductsFromCartSummary(val checkoutState: CheckoutState) :
        CheckoutStateResults()

    data class ClearedCartSuccess(val didClear: Boolean) : CheckoutStateResults()
    data class FailedClearedCart(val error: Throwable) : CheckoutStateResults()
    data class FailedPaymentProcessing(val error: Throwable) : CheckoutStateResults()
    data class SuccessfulPaymentProcessed(val didProcess: Boolean) : CheckoutStateResults()

}

@Parcelize
data class CheckoutState(
    var cartSubTotal: Double = 0.0,
    var totalCartQuantity: Int = 0,
    var cartProducts: List<Product> = emptyList(),
    val shippingRate: Double = 0.15, // 15% shipping
    val taxRate: Double = 0.08, // 8% tax default
    var address: Address? = null,
    var paymentMethod: String? = "googlePay",
) : Parcelable
