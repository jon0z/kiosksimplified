package com.simplifiedkiosk.model

class Cart {

    private val items = mutableListOf<FakeCartProduct>()

    // Add an item to the cart
    fun addItem(item: FakeCartProduct) {
        val cartItem = items.find { it.productId == item.productId }
        cartItem?.let {
            it.quantity = it.quantity?.plus(1)
        } ?: items.add(FakeCartProduct(item.productId, 1))
    }

    // Remove an item from the cart
    fun removeItem(itemId: String) {
        val cartItem = items.find { it.productId.toString() == itemId  }
        cartItem?.let { fakeCartProduct ->
            fakeCartProduct.quantity?.let {
                if(it > 1){
                    fakeCartProduct.quantity = it - 1
                } else {
                    items.remove(cartItem)
                }
            }
        }
    }

    // Get the total price of all items in the cart

    // Get the list of items in the cart
    fun getItems(): List<FakeCartProduct> {
        return items
    }

    // Clear all items from the cart
    fun clear() {
        items.clear()
    }
}
