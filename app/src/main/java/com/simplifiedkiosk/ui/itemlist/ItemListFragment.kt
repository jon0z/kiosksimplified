package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
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
import com.simplifiedkiosk.model.Product
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
                                    onFavoriteClick = {},
                                    onItemClick = {
                                        itemClicked(it)
                                    }
                                )
                            }
                            products.forEach {
                                Log.e(TAG, "${it.title} - ${it.price} - ${it.images} - ${it.thumbnail}")
                            }
                        }

                        is ProductStateResults.FailedProductSearch -> {
                            // search failed or returned no results. Display error and clear call to action
                            // to search again or fetch products from server
                            if(viewBinding.composeView.visibility == View.VISIBLE) {
                                viewBinding.composeView.visibility = View.GONE
                                viewBinding.viewCartButton.visibility = View.GONE
                                viewBinding.searchView.visibility = View.GONE
                                viewBinding.noSearchResultsView.visibility = View.VISIBLE
                            }
                        }
                        is ProductStateResults.SuccessfulProductSearch -> {
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {
                                ProductList(
                                    products = state.list,
                                    onFavoriteClick = {} ,
                                    onItemClick = {})
                            }
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
                query?.let {
                    productsListViewModel.searchForProducts(query = it)
                }
                return true
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
}
