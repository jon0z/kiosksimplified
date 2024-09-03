package com.simplifiedkiosk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
): ViewModel() {
    private val _favoritesState = MutableStateFlow<FavoritesStateResults>(FavoritesStateResults.Loading)
    val favoritesState = _favoritesState

    init {
        fetchFavorites()
    }

    fun fetchFavorites(){
        viewModelScope.launch {
            favoritesRepository.getFavorites()
                .collectLatest { result ->
                    result.fold(
                        { favorites ->
                            _favoritesState.value = FavoritesStateResults.FetchFavoritesSuccess(favorites)

                        },{
                            _favoritesState.value = FavoritesStateResults.FetchFavoritesError(it)
                        }
                    )
                }
        }
    }

    fun addProductToFavorites(product: ReactProduct){
        viewModelScope.launch {
            favoritesRepository.addOrUpdateFavorite(product).collectLatest { result ->
                result.fold({ newRowId ->
                    if (newRowId != -1L){
                        _favoritesState.value = FavoritesStateResults.AddProductToFavoritesSuccess(true)
                    }
                }, {
                    _favoritesState.value = FavoritesStateResults.AddProductToFavoritesFailed(it)
                })
            }
        }
    }

    fun removeFromFavorites(product: ReactProduct){
        viewModelScope.launch {
            favoritesRepository.removeFavorite(product).collectLatest { result ->
                result.fold({ rowsAffected ->
                    if (rowsAffected > 0){
                        _favoritesState.value = FavoritesStateResults.RemoveProductFromFavoritesSuccess(true)
                    }
                }, {
                    _favoritesState.value = FavoritesStateResults.RemoveProductFromFavoritesFailed(it)
                })
            }
        }
    }

    fun deleteAllFavorites(){
        viewModelScope.launch {
            favoritesRepository.deleteAllFavorites().collectLatest { result ->
                result.fold({ success ->
                    _favoritesState.value = FavoritesStateResults.SuccessDeleteAllFavorites(success)
                },{
                    _favoritesState.value = FavoritesStateResults.FailedDeleteAllFavorites(it)
                })
            }
        }
    }
}

sealed class FavoritesStateResults{
    object Loading: FavoritesStateResults()
    data class FetchFavoritesSuccess(val favorites: List<ReactProduct>): FavoritesStateResults()
    data class FetchFavoritesError(val error: Throwable): FavoritesStateResults()
    data class RemoveProductFromFavoritesSuccess(val success: Boolean): FavoritesStateResults()
    data class RemoveProductFromFavoritesFailed(val error: Throwable): FavoritesStateResults()
    data class AddProductToFavoritesSuccess(val success: Boolean): FavoritesStateResults()
    data class AddProductToFavoritesFailed(val error: Throwable): FavoritesStateResults()
    data class SuccessDeleteAllFavorites(val success: Boolean): FavoritesStateResults()
    data class FailedDeleteAllFavorites(val error: Throwable): FavoritesStateResults()
}