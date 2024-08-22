package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.Item
import com.simplifiedkiosk.repository.ItemRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ItemDetailsViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _itemDetails = MutableStateFlow<Item?>(null)
    val itemDetails: StateFlow<Item?> = _itemDetails

    fun loadItemDetails(itemId: String) {
        viewModelScope.launch {
            val item = repository.getItemById(itemId)
            _itemDetails.value = item
        }
    }
}
