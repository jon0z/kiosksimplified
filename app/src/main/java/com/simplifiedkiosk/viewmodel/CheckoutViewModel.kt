package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount

    private val _checkoutState = MutableStateFlow<CheckoutStateResults?>(null)
    val checkoutState: StateFlow<CheckoutStateResults?> = _checkoutState

    private val checkoutScreenState: CheckoutState = CheckoutState(
        totalCartPrice = 0.0,
        totalCartQuantity = 0,
        cartProducts = emptyList()
    )

    fun processPayment(shippingAddress: String): Boolean {
        // Simulate payment processing logic
        return if (shippingAddress.isNotBlank()) {
            // Here you would integrate with a real payment gateway
//            clearCart()
            true
        } else {
            false
        }
    }

    fun addCartProduct(cartProduct: Product) {
        viewModelScope.launch {
            cartRepository.addProductToCart(cartProduct).collectLatest { result ->
                result.fold({ cartDetails ->
                    _checkoutState.value = CheckoutStateResults.LoadedCartItems(
                        checkoutScreenState.copy(
                            totalCartPrice = cartDetails["totalCartPrice"]?.toDouble() ?: 0.00,
                            totalCartQuantity = cartDetails["totalCartQuantity"]?.toInt()  ?: 0,
                            cartProducts = cartRepository.getCartItems()
                        )
                    )
                }, {
                    _checkoutState.value = CheckoutStateResults.FailedLoadingCartItems(it)
                })
            }
        }
    }

    fun removeCartProduct(cartProduct: Product) {
        viewModelScope.launch {
            cartRepository.removeProductFromCart(cartProduct).collectLatest {result ->
                result.fold({ cartDetails ->
                    _checkoutState.value = CheckoutStateResults.LoadedCartItems(
                        checkoutScreenState.copy(
                            totalCartPrice = cartDetails["totalCartPrice"]?.toDouble() ?: 0.00,
                            totalCartQuantity = cartDetails["totalCartQuantity"]?.toInt()  ?: 0,
                            cartProducts = cartRepository.getCartItems()
                        )
                    )
                }, {
                    _checkoutState.value = CheckoutStateResults.FailedLoadingCartItems(it)
                })

            }
        }
    }
}

sealed class CheckoutStateResults {
    object Loading: CheckoutStateResults()
    data class LoadedCartItems(val checkoutState: CheckoutState): CheckoutStateResults()
    data class FailedLoadingCartItems(val error: Throwable): CheckoutStateResults()

}

data class CheckoutState(
    var totalCartPrice: Double = 0.0,
    var totalCartQuantity: Int = 0,
    var cartProducts: List<Product> = emptyList()
)
