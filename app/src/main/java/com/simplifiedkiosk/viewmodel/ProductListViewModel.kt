package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.FakeProduct
import com.simplifiedkiosk.repository.ProductsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productsRepository: ProductsRepository
) : ViewModel() {

    private val _productsState = MutableStateFlow<ProductStateResults>(ProductStateResults.Loading)
    val productsState: StateFlow<ProductStateResults> = _productsState

    init {
        fetchProducts()
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
}

sealed class ProductStateResults {
    object Loading : ProductStateResults()
    data class FetchProductsSuccess(val products: List<FakeProduct>) : ProductStateResults()
    data class FetchProductsError(val error: Throwable) : ProductStateResults()
}