package com.simplifiedkiosk.ui.itemlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemListBinding
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.ProductListViewModel
import com.simplifiedkiosk.viewmodel.ProductStateResults
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.view.MenuProvider
import com.simplifiedkiosk.model.ReactProduct


private const val TAG = "ItemListFragment"
@AndroidEntryPoint
class ItemListFragment : Fragment(), MenuProvider {

    private val productsListViewModel: ProductListViewModel by viewModels()
    private lateinit var viewBinding: FragmentItemListBinding

    private var mSelectedProduct by mutableStateOf<ReactProduct?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.e(TAG, "onCreateView: called", )
        viewBinding = FragmentItemListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Products"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                productsListViewModel.productsState.collectLatest { state ->
                    when (state) {
                        is ProductStateResults.FetchProductsSuccess -> {

                        }
                        is ProductStateResults.FetchProductsError -> {
                            Log.e(TAG, "failed to fetch products")
                        }
                        ProductStateResults.Loading -> {
                            Log.e(TAG, "loading products")
                        }

                        is ProductStateResults.FailedLoadingCartProducts -> {
                            Log.e(TAG, "failed to load cart products ${state.error}")
                        }
                        is ProductStateResults.SuccessLoadingCartProducts -> {
                            val quantity = state.cartDetails["totalCartQuantity"]
                            viewBinding.viewCartButton.text = "View Cart ($quantity)"
                        }

                        is ProductStateResults.FailedLoadingReactProducts -> {
                            Log.e(TAG, "failed to load react products ${state.error}")
                        }
                        is ProductStateResults.SuccessLoadingReactProducts -> {
                            val products = state.list
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = products,
                                    onFavoriteClick = { productSelected ->
                                        Log.e(TAG, "onViewCreated: *** favorite icon clicked... isFavorite = ${productSelected.isFavorite}", )
                                        productSelected.isFavorite?.let {
                                            if (it){
                                                Log.e(TAG,  "*** removing from favorites " )
                                                productSelected.isFavorite = false
                                                productsListViewModel.removeFromFavorites(productSelected)
                                            } else {
                                                Log.e(TAG,  "*** adding from favorites " )
                                                productSelected.isFavorite = true
                                                productsListViewModel.addToFavorites(productSelected)
                                            }
                                        }
                                    },
                                    onItemClick = {
                                        itemClicked(it)
                                    }
                                )
                            }
                        }

                        is ProductStateResults.FailedProductSearch -> {
                            // search failed or returned no results. Display error and clear call to action
                            // to search again or fetch products from server
                            Log.e(TAG, "onViewCreated: failed product search")
                            showNoSearchResultsFoundAlert(true)
                            productsListViewModel.fetchReactProducts()
                        }
                        is ProductStateResults.SuccessfulProductSearch -> {
                            Log.e(TAG, "onViewCreated: successful product search" )
                            if(state.list.isEmpty()){
                                showNoSearchResultsFoundAlert(true)
                            }
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = state.list,
                                    onFavoriteClick = { product ->
                                        product.isFavorite?.let {
                                            if (it){
                                                product.isFavorite = false
                                                productsListViewModel.removeFromFavorites(product)
                                            } else {
                                                product.isFavorite = true
                                                productsListViewModel.addToFavorites(product)
                                            }
                                        }
                                    } ,
                                    onItemClick = {
                                        itemClicked(it)
                                    })
                            }
                        }

                        is ProductStateResults.AddedProductToFavoritesFailed -> {
                            Log.e(TAG, "failed to add product to favorites. Reason = ${state.error}")
                            Toast.makeText(requireActivity(), "Failed to add product to favorites", Toast.LENGTH_SHORT).show()
                        }
                        is ProductStateResults.AddedProductToFavoritesSuccess -> {
                            // high light the favorite icon on button
                            Log.e(TAG, "onViewCreated: successfully added product to favorites" )
                        }

                        is ProductStateResults.RemovedProductFromFavoritesFailed -> {
                            Log.e(TAG, "failed to remove product from favorites. Reason = ${state.error}")
                            Toast.makeText(requireActivity(), "Failed to remove product from favorites", Toast.LENGTH_SHORT).show()
                        }
                        is ProductStateResults.RemovedProductFromFavoritesSuccess -> {
                            //remove highlight
                            Log.e(TAG, "onViewCreated: successfully removed product from favorites" )
                        }
                    }
                }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            if (productsListViewModel.getCartSize() != 0){
                findNavController().navigate(R.id.action_itemListFragment_to_cartFragment)
            } else {
                showAlertDialog(requireContext(), "Cart is empty", "Please add items to continue")
            }
        }

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return query?.let {
                    viewBinding.searchView.visibility = View.GONE
                    hideSoftKeyboard(true)
                    productsListViewModel.searchForProducts(query = it)
                    true
                } ?: false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()){
                    viewBinding.searchView.visibility = View.GONE
                    productsListViewModel.fetchReactProducts()
                }
                return true
            }
        })

        viewBinding.reloadButton.setOnClickListener {
            productsListViewModel.fetchReactProducts()
            showNoSearchResultsFoundAlert(false)
        }
    }

    private fun itemClicked(product: ReactProduct) {
        mSelectedProduct = product
        findNavController().navigate(R.id.action_itemListFragment_to_itemDetailsFragment, bundleOf("productId" to product.productId))
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_item_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_favorites -> {
                findNavController().navigate(R.id.action_itemListFragment_to_favoritesFragment)
                true
            }
            R.id.action_search -> {
                if(viewBinding.searchView.visibility != View.VISIBLE){
                    viewBinding.searchView.visibility = View.VISIBLE
                } else {
                    viewBinding.searchView.visibility = View.GONE
                }
                true
            }
            else -> false
        }
    }

    override fun onResume() {
        super.onResume()
        productsListViewModel.fetchProducts()
        productsListViewModel.fetchReactProducts()
    }

    private fun showNoSearchResultsFoundAlert(show: Boolean = true){
        if(show){
            viewBinding.composeView.visibility = View.GONE
            viewBinding.viewCartButton.visibility = View.GONE
            viewBinding.searchView.visibility = View.GONE
            viewBinding.noSearchResultsView.visibility = View.VISIBLE

        } else {
            viewBinding.noSearchResultsView.visibility = View.GONE
            viewBinding.composeView.visibility = View.VISIBLE
            viewBinding.viewCartButton.visibility = View.VISIBLE
            viewBinding.searchView.visibility = View.GONE
        }
    }

    private fun hideSoftKeyboard(hide: Boolean){
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(hide){
            // hide soft keyboard
            imm.hideSoftInputFromWindow(viewBinding.searchView.windowToken, 0)
        } else{
            // show soft keyboard
            imm.showSoftInput(viewBinding.searchView, InputMethodManager.SHOW_IMPLICIT)
        }

    }
}
