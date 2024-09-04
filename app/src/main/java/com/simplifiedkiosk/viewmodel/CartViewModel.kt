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
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val application: Application,
    private val cartRepository: CartRepository
) : AndroidViewModel(application) {

    private val _cartState = MutableStateFlow<CartState?>(null)
    val cartState: StateFlow<CartState?> = _cartState

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.loadCartItems().collectLatest { result ->
                result.fold({
                    _cartState.value = CartState.SuccessLoadingCartItems(it)
                }, {
                    _cartState.value = CartState.FailedLoadingCartItems(it)
                })
            }
        }
    }

    fun getCartProducts(): List<Product> {
        return cartRepository.getCartItems()
    }

    fun getCartSize(): Int = cartRepository.getCartTotalQuantity()

    fun getCartTotalPrice(): Double {
        return cartRepository.getCartTotalPrice()
    }

    fun removeProductFromCart(cartProduct: Product) {
        viewModelScope.launch {
            cartRepository.removeProductFromCart(cartProduct).collectLatest { result ->
                result.fold({ cartMap ->
                    _cartState.value = CartState.SuccessRemovingProductFromCart(cartMap)
                }, {
                    _cartState.value = CartState.FailedRemovingProductFromCart(it)
                })
            }
        }
    }

    fun addProductToCart(cartProduct: Product) {
        viewModelScope.launch {
            cartRepository.addProductToCart(cartProduct).collectLatest { result ->
                result.fold({ cartMap ->
                    _cartState.value = CartState.SuccessAddingProductToCart(cartMap)
                }, {
                    _cartState.value = CartState.FailedAddingProductToCart(it)
                })
            }
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

sealed class CartState {
    object Loading : CartState()
    data class SuccessLoadingCartItems(val cartDetails: Map<String, String>) : CartState()
    data class FailedLoadingCartItems(val error: Throwable) : CartState()

    data class SuccessRemovingProductFromCart(val cartDetails: Map<String, String>) : CartState()
    data class FailedRemovingProductFromCart(val error: Throwable) : CartState()

    data class SuccessAddingProductToCart(val cartDetails: Map<String, String>) : CartState()
    data class FailedAddingProductToCart(val error: Throwable) : CartState()
}