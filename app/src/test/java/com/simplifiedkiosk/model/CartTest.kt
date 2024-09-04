package com.simplifiedkiosk.model

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.simplifiedkiosk.dao.CartDao
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class CartTest {

    private lateinit var cart: Cart
    private lateinit var cartDao: CartDao
    private var productMock: Product = Product(
        productId = 123,
        title = "Product 1",
        price = 10.0,
        description = "some description for product 1",
        quantity = 0,
    )

    @Before
    fun setUp() {
        cartDao = mockk()
        cart = Cart(cartDao)
        mockkStatic(Log::class)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `test AddProduct with new product returns success with product in db`() = runTest {
        val resultMap = mapOf("totalCartQuantity" to "1", "totalCartPrice" to "10.0")
        coEvery { cartDao.insertOrUpdateCartItem(any<CartItemEntity>()) } returns 1L

        val result = cart.addProduct(productMock)

        assertThat(result).isEqualTo(Result.success(resultMap))
        assertThat(cart.getProducts()).hasSize(1)
        assertThat(cart.getProducts()[0].quantity).isEqualTo(1)
    }

    @Test
    fun testAddExistingProduct() = runTest {
        coEvery { cartDao.insertOrUpdateCartItem(any<CartItemEntity>()) } returns 1L
        cart.addProduct(productMock)

        val result = cart.addProduct(productMock)

        assertThat(result.isSuccess).isTrue()
        assertThat(cart.getProducts()).hasSize(1)
        assertThat(cart.getProducts()[0].quantity).isEqualTo(2)
    }

    @Test
    fun testAddProductWithNullQuantity() = runTest {
        coEvery { cartDao.insertOrUpdateCartItem(any<CartItemEntity>()) } returns 1L

        val result = cart.addProduct(productMock)

        assertThat(result.isSuccess).isTrue()
        assertThat(cart.getProducts()).hasSize(1)
        assertThat(cart.getProducts()[0].quantity).isEqualTo(1)
    }
}