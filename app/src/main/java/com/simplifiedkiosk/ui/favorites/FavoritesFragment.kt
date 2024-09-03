package com.simplifiedkiosk.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentFavoritesBinding
import com.simplifiedkiosk.ui.itemlist.ProductList
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.FavoritesStateResults
import com.simplifiedkiosk.viewmodel.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment: Fragment(), MenuProvider {

    private lateinit var viewBinding: FragmentFavoritesBinding
    private val favoritesViewModel: FavoritesViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                favoritesViewModel.favoritesState.collectLatest {state ->
                    when(state){
                        is FavoritesStateResults.FetchFavoritesError -> {
                            showAlertDialog(
                                context = requireActivity(),
                                title = "Error",
                                message = "${state.error.message}",
                                positiveButtonText = "Back to products",
                                onPositiveClick = {
                                    findNavController().navigate(R.id.action_favoritesFragment_to_itemListFragment)
                                })
                        }
                        is FavoritesStateResults.FetchFavoritesSuccess -> {
                            val products = state.favorites
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = products,
                                    onFavoriteClick = { selecteProduct ->
                                                      selecteProduct.isFavorite?.let {
                                                          if(it){
                                                              selecteProduct.isFavorite = false
                                                              favoritesViewModel.removeFromFavorites(selecteProduct)
                                                          } else {
                                                              selecteProduct.isFavorite = true
                                                              favoritesViewModel.addProductToFavorites(selecteProduct)
                                                          }
                                                      }
                                    },
                                    onItemClick = { selectedProduct ->
                                        selectedProduct.productId?.let {
                                            findNavController().navigate(R.id.action_favoritesFragment_to_itemDetailsFragment, bundleOf("productId" to it))
                                        }
                                    } )
                            }
                        }
                        FavoritesStateResults.Loading -> {}
                        is FavoritesStateResults.AddProductToFavoritesFailed -> {
                            showAlertDialog(
                                context = requireActivity(),
                                title = "Error",
                                message = "Failed to add product to favorites. \n${state.error.message}")
                        }
                        is FavoritesStateResults.AddProductToFavoritesSuccess -> {}
                        is FavoritesStateResults.RemoveProductFromFavoritesFailed -> {
                            showAlertDialog(
                                context = requireActivity(),
                                title = "Error",
                                message = "Failed to remove product from favorites. \n${state.error.message}")
                        }
                        is FavoritesStateResults.RemoveProductFromFavoritesSuccess -> {
                        }
                        is FavoritesStateResults.FailedDeleteAllFavorites -> {
                            showAlertDialog(
                                context = requireActivity(),
                                title = "Error",
                                message = "Failed to delete all favorites. With reason:${state.error.message}",
                                positiveButtonText = "Ok",
                            )
                        }
                        is FavoritesStateResults.SuccessDeleteAllFavorites -> {
                            showAlertDialog(
                                context = requireActivity(),
                                title = "Success",
                                message = "All favorites were deleted successfully",
                                positiveButtonText = "Ok",
                                onPositiveClick = {
                                    findNavController().navigate(R.id.action_favoritesFragment_to_itemListFragment)
                                }
                            )
                        }
                    }
                }
            }
        }

    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.favorites_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.action_delete -> {
                favoritesViewModel.deleteAllFavorites()
                true
            }
            else -> {
                false
            }
        }
    }


}