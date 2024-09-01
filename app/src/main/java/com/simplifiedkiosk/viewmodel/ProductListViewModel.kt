package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.repository.ReactProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productsRepository: ProductsRepository,
    private val cartRepository: CartRepository,
    private val reactProductsRepository: ReactProductsRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<ProductStateResults>(ProductStateResults.Loading)
    val productsState: StateFlow<ProductStateResults> = _productsState

    init {
        fetchProducts()
        fetchReactProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            productsRepository.fetchProducts().collectLatest { result ->
                result.fold(
                    { products ->
                        _productsState.value = ProductStateResults.FetchProductsSuccess(products)
                    },
                    { error ->
                        _productsState.value = ProductStateResults.FetchProductsError(error)
                    })
            }
        }
    }

    fun fetchReactProducts(){
        viewModelScope.launch {
            reactProductsRepository.fetchReactProducts()
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
            reactProductsRepository.searchProducts(query = query)
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

    fun loadCartItems(){
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
}

sealed class ProductStateResults {
    object Loading : ProductStateResults()
    data class FetchProductsSuccess(val products: List<Product>) : ProductStateResults()
    data class FetchProductsError(val error: Throwable) : ProductStateResults()

    data class SuccessLoadingCartProducts(val cartDetails: Map<String, String>) : ProductStateResults()
    data class FailedLoadingCartProducts(val error: Throwable) : ProductStateResults()

    data class SuccessLoadingReactProducts(val list: List<ReactProduct>): ProductStateResults()
    data class FailedLoadingReactProducts(val error: Throwable): ProductStateResults()

    data class SuccessfulProductSearch(val list: List<ReactProduct>): ProductStateResults()
    data class FailedProductSearch(val error: Throwable): ProductStateResults()
}