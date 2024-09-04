package com.simplifiedkiosk.viewmodel

import android.app.Application
import com.google.common.truth.Truth
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.repository.CartRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CartViewModelTest {

    private lateinit var applicationMock: Application
    @Before
    fun setUp() {
        applicationMock = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGetCartProducts() {
        
        val cartRepository = mockk<CartRepository>()
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val cartProducts = listOf(Product())
        every { cartRepository.getCartItems() } returns cartProducts

        
        val result = cartViewModel.getCartProducts()

        
        Truth.assertThat(result).isEqualTo(cartProducts)
    }

    @Test
    fun testGetCartSize() {
        
        val cartRepository = mockk<CartRepository>()
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val cartSize = 10
        every { cartRepository.getCartTotalQuantity() } returns cartSize

        
        val result = cartViewModel.getCartSize()

        
        Truth.assertThat(result).isEqualTo(cartSize)
    }

    @Test
    fun testGetCartTotalPrice() {
        
        val cartRepository = mockk<CartRepository>()
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val cartTotalPrice = 100.0
        every { cartRepository.getCartTotalPrice() } returns cartTotalPrice

        
        val result = cartViewModel.getCartTotalPrice()

        
        Truth.assertThat(result).isEqualTo(cartTotalPrice)
    }

    @Test
    fun testRemoveProductFromCart() = runTest {
        
        val cartRepository = mockk<CartRepository>()
        coEvery { cartRepository.removeProductFromCart(any()) } returns flowOf(Result.success(emptyMap()))
        coEvery { cartRepository.loadCartItems() } returns flowOf(Result.success(emptyMap()))
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val product = Product()


        
        cartViewModel.removeProductFromCart(product)

        
        verify { cartRepository.removeProductFromCart(product) }
        val cartState = cartViewModel.cartState.value
        Truth.assertThat(cartState).isInstanceOf(CartState.SuccessRemovingProductFromCart::class.java)
    }

    @Test
    fun testRemoveProductFromCartFailure() {
        
        val result = Result.failure<Map<String, String>>(Throwable("Error"))
        val cartRepository = mockk<CartRepository>()
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val product = Product()
        every { cartRepository.removeProductFromCart(product) } returns flowOf(result)

        
        cartViewModel.removeProductFromCart(product)

        
        val cartState = cartViewModel.cartState.value
        Truth.assertThat(cartState).isInstanceOf(CartState.FailedRemovingProductFromCart::class.java)
    }

    @Test
    fun testAddProductToCart() = runTest {
        val cartRepository = mockk<CartRepository>()
        coEvery { cartRepository.loadCartItems() } returns flowOf(Result.success(emptyMap()))
        coEvery { cartRepository.addProductToCart(any()) } returns flowOf(Result.success(emptyMap()))
        
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val product = Product()
        
        cartViewModel.addProductToCart(product)
        
        verify { cartRepository.addProductToCart(product) }
        val cartState = cartViewModel.cartState.value
        Truth.assertThat(cartState).isInstanceOf(CartState.SuccessAddingProductToCart::class.java)
    }

    @Test
    fun testAddProductToCartFailure() {
        val result = Result.failure<Map<String, String>>(Throwable("Error"))

        
        val cartRepository = mockk<CartRepository>()
        val cartViewModel = CartViewModel(applicationMock, cartRepository)
        val product = Product()
        every { cartRepository.addProductToCart(product) } returns flowOf(result)

        
        cartViewModel.addProductToCart(product)

        
        val cartState = cartViewModel.cartState.value
        Truth.assertThat(cartState).isInstanceOf(CartState.FailedAddingProductToCart::class.java)
    }
}