package com.simplifiedkiosk.interactor

import kotlinx.coroutines.flow.Flow

interface BaseInteractor<T> {
    suspend fun processAction(): Flow<T>
}