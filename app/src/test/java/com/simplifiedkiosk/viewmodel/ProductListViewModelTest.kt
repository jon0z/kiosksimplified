package com.simplifiedkiosk.viewmodel

import com.google.common.truth.Truth.assertThat
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.FavoritesRepository
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.repository.ReactProductsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ProductListViewModelTest {

    private lateinit var viewModel: ProductListViewModel
    private val productsRepository: ProductsRepository = mockk()
    private val reactProductsRepository: ReactProductsRepository = mockk()
    private val cartRepository: CartRepository = mockk()
    private val favoritesRepository: FavoritesRepository = mockk()

    @Before
    fun setup() {
        viewModel = ProductListViewModel(
            productsRepository,
            cartRepository,
            reactProductsRepository,
            favoritesRepository
        )
    }

    @Test
    fun `fetchReactProducts success`() = runTest {
        // Arrange
        val reactProducts = listOf(ReactProduct())
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(reactProducts.first()))
        coEvery { reactProductsRepository.fetchReactProducts() } returns flowOf(Result.success(reactProducts))

        // Act
        viewModel.fetchReactProducts()

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.SuccessLoadingReactProducts::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.SuccessLoadingReactProducts).list).isEqualTo(reactProducts)
    }

    @Test
    fun `fetchReactProducts failure`() = runBlockingTest {
        // Arrange
        val error = Exception("Error")
        val products = listOf(ReactProduct())
        every { reactProductsRepository.fetchReactProducts() } returns flowOf(Result.failure(error))

        // Act
        viewModel.fetchReactProducts()

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.FailedLoadingReactProducts::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.FailedLoadingReactProducts).error).isEqualTo(error)
    }

    @Test
    fun `searchForProducts success`() = runTest {
        // Arrange
        val query = "query"
        val products = listOf(ReactProduct())
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(products.first()))
        coEvery { reactProductsRepository.searchProducts(query) } returns flowOf(Result.success(products))

        // Act
        viewModel.searchForProducts(query)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.SuccessfulProductSearch::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.SuccessfulProductSearch).list).isEqualTo(products)
    }

    @Test
    fun `searchForProducts failure`() = runBlockingTest {
        // Arrange
        val query = "query"
        val error = Exception("Error")
        every { reactProductsRepository.searchProducts(query) } returns flowOf(Result.failure(error))

        // Act
        viewModel.searchForProducts(query)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.FailedProductSearch::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.FailedProductSearch).error).isEqualTo(error)
    }

    @Test
    fun `getCartSize returns correct value`() = runTest {
        // Arrange
        val cartSize = 10
        coEvery { cartRepository.getCartTotalQuantity() } returns cartSize
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(
            ReactProduct()
        ))

        // Act
        val result = viewModel.getCartSize()

        // Assert
        assertThat(result).isEqualTo(cartSize)
    }

    @Test
    fun `addToFavorites success`() = runTest {
        // Arrange
        val product = ReactProduct()
        val id = 1L
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(product))
        coEvery { favoritesRepository.addOrUpdateFavorite(product) } returns flowOf(Result.success(id))

        // Act
        viewModel.addToFavorites(product)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.AddedProductToFavoritesSuccess::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.AddedProductToFavoritesSuccess).success).isEqualTo(true)
    }

    @Test
    fun `addToFavorites failure`() = runTest {
        // Arrange
        val product = ReactProduct()
        val error = Exception("Error")
        every { favoritesRepository.addOrUpdateFavorite(product) } returns flowOf(Result.failure(error))

        // Act
        viewModel.addToFavorites(product)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.AddedProductToFavoritesFailed::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.AddedProductToFavoritesFailed).error).isEqualTo(error)
    }

    @Test
    fun `removeFromFavorites success`() = runTest {
        // Arrange
        val product = ReactProduct()
        val rowsAffected = 1
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(product))
        coEvery { favoritesRepository.removeFavorite(product) } returns flowOf(Result.success(rowsAffected))

        // Act
        viewModel.removeFromFavorites(product)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.RemovedProductFromFavoritesSuccess::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.RemovedProductFromFavoritesSuccess).success).isEqualTo(true)
    }

    @Test
    fun `removeFromFavorites failure`() = runTest {
        // Arrange
        val product = ReactProduct()
        val error = Exception("Error")
        coEvery { reactProductsRepository.fetchReactProduct(any()) } returns flowOf(Result.success(product))
        coEvery { favoritesRepository.removeFavorite(product) } returns flowOf(Result.failure(error))

        // Act
        viewModel.removeFromFavorites(product)

        // Assert
        assertThat(viewModel.productsState.value).isInstanceOf(ProductStateResults.RemovedProductFromFavoritesFailed::class.java)
        assertThat((viewModel.productsState.value as ProductStateResults.RemovedProductFromFavoritesFailed).error).isEqualTo(error)
    }
}