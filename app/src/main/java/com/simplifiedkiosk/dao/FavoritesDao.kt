package com.simplifiedkiosk.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplifiedkiosk.model.FavoriteEntity
import com.simplifiedkiosk.model.ReactProduct


@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites_table")
    suspend fun getFavorites(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdateFavorite(product: FavoriteEntity): Long

    @Update
    suspend fun updateFavorite(product: FavoriteEntity): Int

    @Insert
    suspend fun addFavorite(product: FavoriteEntity): Long

    @Delete
    suspend fun removeFavorite(product: FavoriteEntity): Int

    @Query("DELETE FROM favorites_table")
    suspend fun deleteFavorites(): Int




}