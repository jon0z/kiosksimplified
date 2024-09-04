package com.simplifiedkiosk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simplifiedkiosk.dao.CartDao
import com.simplifiedkiosk.dao.FavoritesDao
import com.simplifiedkiosk.model.CartItemEntity
import com.simplifiedkiosk.model.FavoriteEntity

@Database(entities = [CartItemEntity::class, FavoriteEntity::class], version = 1, exportSchema = false)
@TypeConverters(ImagesTypeConverted::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .fallbackToDestructiveMigration()  // Rebuild the database on schema change
                .build()
        }
    }
}
