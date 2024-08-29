package com.simplifiedkiosk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.model.FakeCart
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.model.Item
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.utils.getCurrentDate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val repository: ProductsRepository,
    private val cartRepository: CartRepository
) : ViewModel() {
    private var _itemDetailsState = MutableStateFlow<ItemDetailsState>(ItemDetailsState.Loading)
    val itemDetailsState: StateFlow<ItemDetailsState> = _itemDetailsState

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            repository.fetchProductById(productId = productId).collectLatest { result ->
                result.fold({
                    _itemDetailsState.value = ItemDetailsState.SuccessLoadingProductDetails(it)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedLoadingProductDetails(it)
                })
            }
        }
    }

    fun addToCart(product: Product) {
        Log.e("ItemDetailsViewModel", "following button click", )
        viewModelScope.launch {
            cartRepository.addProductToCart(product = product).collectLatest { result ->
                result.fold({ cartMap ->
                    _itemDetailsState.value = ItemDetailsState.SuccessAddingProductToCart(cartMap)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedAddingProductToCart(it)
                })
            }
        }
    }

    fun removeFromCart(product: Product) {
        viewModelScope.launch {
            cartRepository.removeProductFromCart(product).collectLatest { result ->
                result.fold({ cartMap ->
                    _itemDetailsState.value = ItemDetailsState.SuccessRemovingProductFromCart(cartMap)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedRemovingProductFromCart(it)
                })
            }
        }
    }

    fun getCartSize(): Int = cartRepository.getCartTotalQuantity()

    fun loadCartItems() {
        viewModelScope.launch {
            cartRepository.loadCartItems().collectLatest { result ->
                result.fold({
                    _itemDetailsState.value = ItemDetailsState.SuccessLoadingCartItems(it)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedLoadingCartItems
                })
            }
        }
    }
}

sealed class ItemDetailsState {
    object Loading : ItemDetailsState()
    data class SuccessLoadingProductDetails(val product: Product): ItemDetailsState()
    data class FailedLoadingProductDetails(val error: Throwable): ItemDetailsState()
    data class SuccessAddingProductToCart(val cartDetails: Map<String, String>): ItemDetailsState()
    data class FailedAddingProductToCart(val error: Throwable): ItemDetailsState()
    data class SuccessRemovingProductFromCart(val cartDetails: Map<String, String>): ItemDetailsState()
    data class FailedRemovingProductFromCart(val error: Throwable): ItemDetailsState()
    data class SuccessLoadingCartItems(val cartDetails: Map<String, String>): ItemDetailsState()
    object FailedLoadingCartItems: ItemDetailsState()
}
