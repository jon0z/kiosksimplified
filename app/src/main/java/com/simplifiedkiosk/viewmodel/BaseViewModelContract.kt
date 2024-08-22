package com.simplifiedkiosk.viewmodel

import kotlinx.coroutines.flow.StateFlow

interface BaseViewModelContract<T> {
    val state: StateFlow<T>
    fun handleResult(result: Any)
}