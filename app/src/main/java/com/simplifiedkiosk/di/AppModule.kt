package com.simplifiedkiosk.di

import android.content.Context
import androidx.room.Room
import com.simplifiedkiosk.dao.CartItemDao
import com.simplifiedkiosk.database.AppDatabase
import com.simplifiedkiosk.model.Cart
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    fun provideCartItemDao(appDatabase: AppDatabase): CartItemDao {
        return appDatabase.cartItemDao()
    }

    @Provides
    @Singleton
    fun provideCartRepository(cartItemDao: CartItemDao,
                              cart: Cart): CartRepository {
        return CartRepository(cartItemDao, cart)
    }

    @Provides
    fun provideFakeProductsApiService(): FakeProductApiService {
        return FakeProductsApiClient.apiService
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
    fun provideCart(): Cart {
        return Cart()
    }
}