package com.simplifiedkiosk.model

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.simplifiedkiosk.dao.CartDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class CartTest {

    private lateinit var cart: Cart
    private lateinit var cartDao: CartDao
    private var productMock: ReactProduct = ReactProduct(
        productId = 123,
        title = "Product 1",
        price = 10.00,
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
    fun `test add product success when product is not in db`() = runTest{
        coEvery { cartDao.cartProductWithItemIdExists(any()) } returns false
//        coEvery { cartDao.cartItemExists(any()) } returns false
        coEvery { cartDao.insertCartItem(any()) } returns 200L
        coEvery { Log.e(any(), any()) } returns 1

        val result = cart.addProduct(product = productMock)

        assertThat(result.isSuccess).isTrue()
        val cartMap = result.getOrThrow()
        assertThat(cartMap["totalCartQuantity"]).isEqualTo("1")
        assertThat(cartMap["totalCartPrice"]).isEqualTo(productMock.price)

        coVerify { cartDao.insertCartItem(any()) }
    }

    @Test
    fun `test add product success when product is in db`() = runTest {
        mockkObject(productMock)
        every { productMock.quantity } returns 1
        coEvery { cartDao.getCartProductByItemId(any()) } returns listOf(productMock.toCartItem())
        coEvery { cartDao.cartProductWithItemIdExists(any()) } returns true
        coEvery { cartDao.updateCartItem(any()) } returns 1

        val result = cart.addProduct(productMock)

        assertThat(result.isSuccess).isTrue()
        val cartMap = result.getOrThrow()
        assertThat(cartMap["totalCartQuantity"]).isEqualTo("2")
        assertThat(cartMap["totalCartPrice"]).isEqualTo(productMock.price)

        coVerify { cartDao.insertCartItem(any()) }
    }

    @Test
    fun removeProduct() {
    }
}