package com.simplifiedkiosk.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.model.Item
import com.simplifiedkiosk.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val application: Application,
    private val cartRepository: CartRepository
) : AndroidViewModel(application) {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
//            val items = cartRepository.getAllCartItems()
//            _cartItems.value = items
//            _totalPrice.value = items.sumOf { it.item.price * it.quantity }
        }
    }

    fun addItemToCart(cartItem: CartItem) {
        viewModelScope.launch {
            cartRepository.addItemToCart(cartItem)
            loadCartItems() // Reload items after adding to update the UI
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
            _cartItems.value = emptyList()
            _totalPrice.value = 0.0
        }
    }


    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location: Location? ->
            _location.value = location
        }
    }
}
