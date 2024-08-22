package com.simplifiedkiosk.model

class Cart {

    private val items = mutableListOf<CartItem>()

    // Add an item to the cart
    fun addItem(item: Item) {
        val cartItem = items.find { it.item.id == item.id }
        cartItem?.let {
            it.quantity += 1
        } ?: items.add(CartItem(item))
    }

    // Remove an item from the cart
    fun removeItem(itemId: String) {
        val cartItem = items.find { it.item.id == itemId }
        cartItem?.let {
            if (it.quantity > 1) {
                it.quantity -= 1
            } else {
                items.remove(it)
            }
        }
    }

    // Get the total price of all items in the cart
    fun getTotalPrice(): Double {
        return items.sumOf { it.item.price * it.quantity }
    }

    // Get the list of items in the cart
    fun getItems(): List<CartItem> {
        return items
    }

    // Clear all items from the cart
    fun clear() {
        items.clear()
    }
}
