package com.simplifiedkiosk.ui.itemlist

import android.content.Context
import android.os.Bundle
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
import com.simplifiedkiosk.model.Product


private const val TAG = "ItemListFragment"

@AndroidEntryPoint
class ItemListFragment : Fragment(), MenuProvider {

    private val productsListViewModel: ProductListViewModel by viewModels()
    private lateinit var viewBinding: FragmentItemListBinding

    private var mSelectedProduct by mutableStateOf<Product?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        val toolBarTitle =
            (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Products"

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                productsListViewModel.productsState.collectLatest { state ->
                    when (state) {
                        ProductStateResults.Loading -> {}
                        is ProductStateResults.FailedLoadingCartProducts -> {}
                        is ProductStateResults.SuccessLoadingCartProducts -> {
                            val quantity = state.cartDetails["totalCartQuantity"]
                            viewBinding.viewCartButton.text = "View Cart ($quantity)"
                        }

                        is ProductStateResults.FailedLoadingReactProducts -> {}
                        is ProductStateResults.SuccessLoadingReactProducts -> {
                            val products = state.list
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = products,
                                    onFavoriteClick = { productSelected ->
                                        productSelected.isFavorite?.let {
                                            if (it) {
                                                productSelected.isFavorite = false
                                                productsListViewModel.removeFromFavorites(
                                                    productSelected
                                                )
                                            } else {
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
                            showNoSearchResultsFoundAlert(true)
                            productsListViewModel.fetchReactProducts()
                        }

                        is ProductStateResults.SuccessfulProductSearch -> {
                            if (state.list.isEmpty()) {
                                showNoSearchResultsFoundAlert(true)
                            }
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = state.list,
                                    onFavoriteClick = { product ->
                                        product.isFavorite?.let {
                                            if (it) {
                                                product.isFavorite = false
                                                productsListViewModel.removeFromFavorites(product)
                                            } else {
                                                product.isFavorite = true
                                                productsListViewModel.addToFavorites(product)
                                            }
                                        }
                                    },
                                    onItemClick = {
                                        itemClicked(it)
                                    })
                            }
                        }

                        is ProductStateResults.AddedProductToFavoritesFailed -> {
                            Toast.makeText(
                                requireActivity(),
                                "Failed to add product to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ProductStateResults.AddedProductToFavoritesSuccess -> {}

                        is ProductStateResults.RemovedProductFromFavoritesFailed -> {
                            Toast.makeText(
                                requireActivity(),
                                "Failed to remove product from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is ProductStateResults.RemovedProductFromFavoritesSuccess -> {}
                    }
                }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            if (productsListViewModel.getCartSize() != 0) {
                findNavController().navigate(R.id.action_itemListFragment_to_cartFragment)
            } else {
                showAlertDialog(
                    context = requireContext(),
                    title = "Cart is empty",
                    message = "Please add items to continue"
                )
            }
        }

        viewBinding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return query?.let {
                    viewBinding.searchView.visibility = View.GONE
                    hideSoftKeyboard(true)
                    productsListViewModel.searchForProducts(query = it)
                    true
                } ?: false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
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

    private fun itemClicked(product: Product) {
        mSelectedProduct = product
        findNavController().navigate(
            R.id.action_itemListFragment_to_itemDetailsFragment,
            bundleOf("productId" to product.productId)
        )
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
                if (viewBinding.searchView.visibility != View.VISIBLE) {
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
        productsListViewModel.fetchReactProducts()
    }

    private fun showNoSearchResultsFoundAlert(show: Boolean = true) {
        if (show) {
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

    private fun hideSoftKeyboard(hide: Boolean) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (hide) {
            imm.hideSoftInputFromWindow(viewBinding.searchView.windowToken, 0)
        } else {
            imm.showSoftInput(viewBinding.searchView, InputMethodManager.SHOW_IMPLICIT)
        }

    }
}
