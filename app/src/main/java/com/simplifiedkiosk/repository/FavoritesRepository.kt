package com.simplifiedkiosk.repository

import android.util.Log
import com.simplifiedkiosk.dao.FavoritesDao
import com.simplifiedkiosk.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.sql.SQLException
import javax.inject.Inject

class FavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao
){
    fun getFavorites(): Flow<Result<List<Product>>> = flow {
        val result = try {
            val favorites = favoritesDao.getFavorites().map { it.toReactProduct() }
            if (favorites.isNotEmpty()){
                Result.success(favorites)
            } else {
                Result.failure(Throwable("No favorites found"))
            }
        } catch ( e: SQLException){
            Result.failure(e)
        }
        emit(result)
    }
    fun addOrUpdateFavorite(product: Product): Flow<Result<Long>> = flow {
        val result = try {
            val newRowId = favoritesDao.addOrUpdateFavorite(product.toFavoriteEntity())
            Log.e("FavoritesRepository", "addFavorite: new row id: $newRowId" )
            if (newRowId != -1L){
                Result.success(newRowId)
            } else {
                Result.failure(Throwable("Failed to add to favorites"))
            }
        } catch (e: SQLException){
            Result.failure(e)
        }
        emit(result)
    }
    fun removeFavorite(product: Product): Flow<Result<Int>> = flow{
        val result = try {
            var rowsDeleted = 0
            rowsDeleted = favoritesDao.removeFavorite(product.toFavoriteEntity())
            Result.success(rowsDeleted)
        } catch (e: SQLException){
            // do nothing
            Result.failure(e)
        }
        emit(result)
    }

    fun deleteAllFavorites(): Flow<Result<Boolean>> = flow {
        val result = try {
            var rowsDeleted = 0
            rowsDeleted = favoritesDao.deleteFavorites()
            Result.success(rowsDeleted > 0)
        } catch (e: SQLException){
            Result.failure(e)
        }
        emit(result)
    }
}