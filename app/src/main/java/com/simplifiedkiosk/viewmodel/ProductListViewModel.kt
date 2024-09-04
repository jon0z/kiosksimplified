package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.FavoritesRepository
import com.simplifiedkiosk.repository.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val TAG = "ProductListViewModel"

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productsRepository: ProductsRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<ProductStateResults>(ProductStateResults.Loading)
    val productsState: StateFlow<ProductStateResults> = _productsState

    init {
        fetchReactProducts()
        loadCartItems()
    }

    fun fetchReactProducts(){
        viewModelScope.launch {
            productsRepository.fetchReactProducts()
                .collectLatest {result ->
                    result.fold(
                        { products ->
                            _productsState.value = ProductStateResults.SuccessLoadingReactProducts(products)
                        }, { error ->
                            _productsState.value = ProductStateResults.FailedLoadingReactProducts(error)
                        }
                    )
                }
        }
    }

    fun searchForProducts(query: String) {
        viewModelScope.launch {
            productsRepository.searchProducts(query = query)
                .collectLatest { result ->
                    result.fold(
                        { products ->
                            _productsState.value = ProductStateResults.SuccessfulProductSearch(products)
                        },
                        {
                            _productsState.value = ProductStateResults.FailedProductSearch(it)
                        }
                    )
                }
        }
    }

    private fun loadCartItems(){
        viewModelScope.launch {
            cartRepository.loadCartItems().collectLatest { result ->
                result.fold({
                    _productsState.value = ProductStateResults.SuccessLoadingCartProducts(it)
                },{
                    _productsState.value = ProductStateResults.FailedLoadingCartProducts(it)
                })
            }
        }
    }

    fun getCartSize() = cartRepository.getCartTotalQuantity()

    fun addToFavorites(product: Product) {
        viewModelScope.launch {
            favoritesRepository.addOrUpdateFavorite(product)
                .collectLatest { result ->
                    result.fold({ id ->
                        if(id != -1L){
                            _productsState.value = ProductStateResults.AddedProductToFavoritesSuccess(true)
                        }
                    }, {
                        _productsState.value = ProductStateResults.AddedProductToFavoritesFailed(it)

                    })
                }
        }
    }

    fun removeFromFavorites(product: Product) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(product)
                .collectLatest { result ->
                    result.fold({ rowsAffected ->
                        if(rowsAffected > 0){
                            _productsState.value = ProductStateResults.RemovedProductFromFavoritesSuccess(true)
                        }
                    }, {
                        _productsState.value = ProductStateResults.RemovedProductFromFavoritesFailed(it)
                    })
                }
        }
    }
}

sealed class ProductStateResults {
    object Loading : ProductStateResults()
    data class SuccessLoadingCartProducts(val cartDetails: Map<String, String>) : ProductStateResults()
    data class FailedLoadingCartProducts(val error: Throwable) : ProductStateResults()

    data class SuccessLoadingReactProducts(val list: List<Product>): ProductStateResults()
    data class FailedLoadingReactProducts(val error: Throwable): ProductStateResults()

    data class SuccessfulProductSearch(val list: List<Product>): ProductStateResults()
    data class FailedProductSearch(val error: Throwable): ProductStateResults()

    data class AddedProductToFavoritesSuccess(val success: Boolean): ProductStateResults()
    data class AddedProductToFavoritesFailed(val error: Throwable): ProductStateResults()

    data class RemovedProductFromFavoritesSuccess(val success: Boolean): ProductStateResults()
    data class RemovedProductFromFavoritesFailed(val error: Throwable): ProductStateResults()
}