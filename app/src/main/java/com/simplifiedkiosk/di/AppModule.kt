package com.simplifiedkiosk.di

import android.content.Context
import androidx.room.Room
import com.simplifiedkiosk.dao.CartDao
import com.simplifiedkiosk.database.AppDatabase
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.network.CommerceJsApiClient
import com.simplifiedkiosk.network.CommerceJsApiService
import com.simplifiedkiosk.network.FakeProductApiService
import com.simplifiedkiosk.network.FakeProductsApiClient
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.ui.itemlist.ItemAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
            .fallbackToDestructiveMigration()  // Rebuild the database on schema change
            .build()
    }

    @Provides
    @Singleton
    fun provideCartItemDao(appDatabase: AppDatabase): CartDao {
        return appDatabase.cartDao()
    }

    @Provides
    fun provideCommerceJsApiService(): CommerceJsApiService {
        return CommerceJsApiClient.apiService
    }

    @Provides
    fun provideFakeProductsApiService(): FakeProductApiService {
        return FakeProductsApiClient.apiService
    }

    @Provides
    @Singleton
    fun provideCartRepository(cart: Cart): CartRepository {
        return CartRepository(cart)
    }

    @Provides
    fun provideProductsRepository(productsApi: FakeProductApiService): ProductsRepository {
        return ProductsRepository(productsApi)
    }

    @Provides
    fun provideItemAdapter(): ItemAdapter {
        return ItemAdapter {}
    }

    @Provides
    @Singleton
    fun provideCart(
        cartDao: CartDao
    ): Cart {
        return Cart(cartDao)
    }
}