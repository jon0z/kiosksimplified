package com.simplifiedkiosk.repository

import com.google.common.truth.Truth.assertThat
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.model.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CartRepositoryTest {

    private lateinit var cart: Cart
    private lateinit var cartRepository: CartRepository

    @Before
    fun setup() {
        cart = mockk()
        cartRepository = CartRepository(cart)
    }

    @Test
    fun testLoadCartItems_Successful() = runTest {
        coEvery { cart.loadItemsFromDb() } returns Result.success(mapOf("item1" to "value1", "item2" to "value2"))
        val result = cartRepository.loadCartItems().first()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.isFailure).isFalse()
        assertThat(result.getOrNull()).isNotNull()
        assertThat(result.getOrNull()).containsExactlyEntriesIn(mapOf("item1" to "value1", "item2" to "value2"))
    }

    @Test
    fun testLoadCartItems_Failure() = runTest {
        coEvery { cart.loadItemsFromDb() } returns Result.failure(Throwable("Mocked error"))

        val result = cartRepository.loadCartItems().first()

        assertThat(result.isSuccess).isFalse()
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("Mocked error")
    }

    @Test
    fun testLoadCartItems_EmptyCart() = runTest {
        coEvery { cart.loadItemsFromDb() } returns Result.success(mapOf())

        val result = cartRepository.loadCartItems().first()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.isFailure).isFalse()
        assertThat(result.getOrThrow()).isEmpty()
    }

    @Test
    fun `add product to cart successfully`() = runTest {
        val product = Product(1, "Test Product", 10.99)
        coEvery { cart.addProduct(product) } returns Result.success(mapOf("product" to "Test Product"))

        val result = cartRepository.addProductToCart(product).first()

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isNotNull()
        assertThat(result.getOrNull()).containsEntry("product", "Test Product")
        coVerify { cart.addProduct(product) }
    }

    @Test
    fun `add product to cart fails`() = runTest {
        val product = Product(1,"Test Product", 10.99)
        coEvery { cart.addProduct(product) } returns Result.failure(Exception("Failed to add product"))

        val result = cartRepository.addProductToCart(product).first()

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).hasMessageThat().contains("Failed to add product")
        coVerify { cart.addProduct(product) }
    }
}