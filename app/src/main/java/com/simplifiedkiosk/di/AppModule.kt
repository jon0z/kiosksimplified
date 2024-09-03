package com.simplifiedkiosk.di

import android.content.Context
import androidx.room.Room
import com.simplifiedkiosk.dao.CartDao
import com.simplifiedkiosk.dao.FavoritesDao
import com.simplifiedkiosk.database.AppDatabase
import com.simplifiedkiosk.model.Cart
import com.simplifiedkiosk.network.ReactProductsApiClient
import com.simplifiedkiosk.network.ReacProductsApiService
import com.simplifiedkiosk.network.FakeProductApiService
import com.simplifiedkiosk.network.FakeProductsApiClient
import com.simplifiedkiosk.repository.CartRepository
import com.simplifiedkiosk.repository.FavoritesRepository
import com.simplifiedkiosk.repository.ProductsRepository
import com.simplifiedkiosk.repository.ReactProductsRepository
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
    @Singleton
    fun provideFavoritesDao(appDatabase: AppDatabase): FavoritesDao {
        return appDatabase.favoritesDao()
    }
    @Provides
    fun provideCommerceJsApiService(): ReacProductsApiService {
        return ReactProductsApiClient.apiService
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
    fun provideReactProductsRepository(productsApiService: ReacProductsApiService): ReactProductsRepository {
        return ReactProductsRepository(productsApiService)
    }

    @Provides
    fun provideFavoritesRepository(favoritesDao: FavoritesDao): FavoritesRepository{
        return FavoritesRepository(favoritesDao)
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