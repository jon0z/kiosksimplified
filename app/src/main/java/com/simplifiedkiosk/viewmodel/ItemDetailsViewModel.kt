package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.FakeCart
import com.simplifiedkiosk.model.FakeCartProduct
import com.simplifiedkiosk.model.FakeProduct
import com.simplifiedkiosk.model.Item
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.utils.getCurrentDate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val repository: ProductsRepository
) : ViewModel() {

    private val _itemDetails = MutableStateFlow<Item?>(null)
    val itemDetails: StateFlow<Item?> = _itemDetails

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

    fun addToCart(products: List<FakeCartProduct>) {
        val userId = "1"
        val date: String = getCurrentDate()

        viewModelScope.launch {
            repository.addToCart(userId, date, products).collectLatest { result ->
                result.fold({
                    _itemDetailsState.value = ItemDetailsState.SuccessCreatingCart(it)
                }, {
                    _itemDetailsState.value = ItemDetailsState.FailedCreatingCart(it)
                })
            }
        }
    }

    fun getUserCart(userId: String? = "2") {
        viewModelScope.launch {
            userId?.let {
                repository.getCartByUserId(userId).collectLatest {result ->
                    result.fold({
                        _itemDetailsState.value = ItemDetailsState.SuccessGettingUserCarts(it)
                    },{
                        _itemDetailsState.value = ItemDetailsState.FailedToGetUserCarts(it)
                    })

                }
            }
        }
    }
}

sealed class ItemDetailsState {
    object Loading : ItemDetailsState()
    data class SuccessLoadingProductDetails(val product: FakeProduct): ItemDetailsState()
    data class FailedLoadingProductDetails(val error: Throwable): ItemDetailsState()

    data class SuccessCreatingCart(val cart: FakeCart): ItemDetailsState()
    data class FailedCreatingCart(val error: Throwable): ItemDetailsState()

    data class SuccessGettingUserCarts(val carts: List<FakeCart>): ItemDetailsState()
    data class FailedToGetUserCarts(val error: Throwable): ItemDetailsState()
}
