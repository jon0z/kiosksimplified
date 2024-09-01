package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.ReactProductsRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val reactProductsRepository: ReactProductsRepository
) : ViewModel() {

    private var _itemDetailsState = MutableStateFlow<ItemDetailsState>(ItemDetailsState.Loading)
    val itemDetailsState: StateFlow<ItemDetailsState> = _itemDetailsState

    fun loadProductDetails(productId: Int) {
        viewModelScope.launch {
            reactProductsRepository.fetchReactProduct(productId).collectLatest { result ->
                result.fold({
                    _itemDetailsState.value = ItemDetailsState.SuccessLoadingReactProductDetails(it)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedLoadingReactProductDetails(it)
                })
            }
        }
    }

    fun addToCart(product: ReactProduct) {
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
    data class SuccessAddingProductToCart(val cartDetails: Map<String, String>): ItemDetailsState()
    data class FailedAddingProductToCart(val error: Throwable): ItemDetailsState()
    data class SuccessLoadingCartItems(val cartDetails: Map<String, String>): ItemDetailsState()
    data class SuccessLoadingReactProductDetails(val product: ReactProduct): ItemDetailsState()
    data class FailedLoadingReactProductDetails(val error: Throwable): ItemDetailsState()
    object FailedLoadingCartItems: ItemDetailsState()
}
