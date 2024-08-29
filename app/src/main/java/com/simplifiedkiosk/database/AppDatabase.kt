package com.simplifiedkiosk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.simplifiedkiosk.dao.CartDao
import com.simplifiedkiosk.model.CartItemEntity

@Database(entities = [CartItemEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao

    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .fallbackToDestructiveMigration()  // Rebuild the database on schema change
                .build()
        }
    }
}
