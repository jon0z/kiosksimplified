package com.simplifiedkiosk.ui.cart

import com.google.common.truth.Truth.assertThat
import com.simplifiedkiosk.model.Product
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CartAdapterTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `test adding to existing item with quantity greater than 1`() {
        val productId = 1
        val existingItem = Product(productId, "Existing Item", quantity = 2)
        val newProduct = Product(productId, "Existing Item", quantity = 1)
        val adapter = CartAdapter({}) {}
        adapter.submitList(mutableListOf(existingItem))

        adapter.updateCartItem(newProduct)

        assertEquals(3, existingItem.quantity)
        assertEquals(1, adapter.currentList.size)
    }

    @Test
        fun `test update existing item with quantity equal to 1`() {
        val productId = 1
        val existingItem = Product(productId, "Existing Item", quantity = 1)
        val newProduct = Product(productId, "Existing Item", quantity = 1)
        val adapter = CartAdapter({}) {}
        adapter.submitList(listOf(existingItem))

        adapter.updateCartItem(newProduct)

        assertEquals(1, adapter.currentList.size)
        assertEquals(2, existingItem.quantity)
    }

    @Test
    fun `test adding new item to list`() {
        val productId = 1
        val newProduct = Product(productId, "New Item", quantity = 1)
        val adapter = CartAdapter({}) {}

        adapter.updateCartItem(newProduct)

        assertEquals(1, adapter.currentList.size)
        assertEquals(newProduct, adapter.currentList.first())
    }

    @Test
    fun `test update item with null quantity`() {
        val productId = 1
        val existingItem = Product(productId, "Existing Item", quantity = null)
        val newProduct = Product(productId, "Existing Item", quantity = 1)
        val currentList = mutableListOf(existingItem)
        val adapter = CartAdapter({}) {}
        adapter.submitList(currentList)

        adapter.updateCartItem(newProduct)

        assertEquals(1, adapter.currentList.size)
        assertEquals(existingItem, adapter.currentList[0])
    }

    @Test
    fun `test remove existing item with valid ProductId and quantity 1 is successful`() = runTest {
        // Arrange
        val cartAdapter = CartAdapter({}) {}
        cartAdapter.submitList(listOf(
            Product(
                productId = 1,
                title = "Product 1",
                quantity = 1)
        ))

        // Act
        cartAdapter.removeItem("1")

        // Assert
        val updatedList = cartAdapter.currentList
        assertThat(updatedList).isEmpty()
        verify { cartAdapter.submitList(updatedList) }
    }
}