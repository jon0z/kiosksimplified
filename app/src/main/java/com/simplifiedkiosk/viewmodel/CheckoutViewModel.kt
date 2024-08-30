package com.simplifiedkiosk.viewmodel

import android.location.Address
import android.os.Parcel
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize
import java.io.Serializable
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

    fun addCartProduct(cartProduct: Product) {
        viewModelScope.launch {
            cartRepository.addProductToCart(cartProduct).collectLatest { result ->
                result.fold({ cartDetails ->
                    _checkoutState.value = CheckoutStateResults.LoadedCartItems(
                        checkoutScreenState.copy(
                            cartSubTotal = cartDetails["totalCartPrice"]?.toDouble() ?: 0.00,
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
                            cartSubTotal = cartDetails["totalCartPrice"]?.toDouble() ?: 0.00,
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

    fun getCartSize() = cartRepository.getCartTotalQuantity()
}

sealed class CheckoutStateResults {
    object Loading: CheckoutStateResults()
    data class LoadedCartItems(val checkoutState: CheckoutState): CheckoutStateResults()
    data class FailedLoadingCartItems(val error: Throwable): CheckoutStateResults()

    data class ReceivedProductsFromCartSummary(val checkoutState: CheckoutState): CheckoutStateResults()

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
): Parcelable
