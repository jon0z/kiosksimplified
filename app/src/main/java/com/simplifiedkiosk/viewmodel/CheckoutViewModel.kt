package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

//    init {
//        loadCartItems()
//    }

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
}
