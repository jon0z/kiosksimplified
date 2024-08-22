package com.simplifiedkiosk.repository

import com.simplifiedkiosk.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ItemRepository {

    private val items = listOf(
        Item("1", "Item 1", "Description for Item 1", 9.99, "https://via.placeholder.com/150"),
        Item("2", "Item 2", "Description for Item 2", 19.99, "https://via.placeholder.com/150"),
        Item("3", "Item 3", "Description for Item 3", 29.99, "https://via.placeholder.com/150")
    )

    fun fetchItems(): Flow<List<Item>> {
        return flow {
            emit(items)
        }
    }

    fun getItemById(itemId: String): Item? {
        return items.find { it.id == itemId }
    }

    suspend fun saveData(data: Item) {
        // Implement saving logic, if needed
    }
}
