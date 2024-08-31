package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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


private const val TAG = "ItemListFragment"
@AndroidEntryPoint
class ItemListFragment : Fragment() {

    private val productsListViewModel: ProductListViewModel by viewModels()
    private lateinit var viewBinding: FragmentItemListBinding

    private var selectedProduct by mutableStateOf<Product?>(null)

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
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Products"

        productsListViewModel.loadCartItems()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                productsListViewModel.productsState.collectLatest { state ->
                    when (state) {
                        is ProductStateResults.FetchProductsSuccess -> {
                            viewBinding.composeView.setViewTreeLifecycleOwner(viewLifecycleOwner)
                            viewBinding.composeView.setContent {

                                ItemList(
                                    state.products,
                                    selectedItem = selectedProduct,
                                    onSelectedItemChange = {
                                        selectedProduct = it
                                        findNavController().navigate(R.id.action_itemListFragment_to_itemDetailsFragment, bundleOf("productId" to it.productId))
                                    }
                                )
                            }
                        }
                        is ProductStateResults.FetchProductsError -> {
                            Log.e(TAG, "failed to fetch products")
                        }
                        ProductStateResults.Loading -> {
                            Log.e(TAG, "loading products")
                        }

                        is ProductStateResults.FailedLoadingCartProducts -> {
                            Log.e(TAG, "failed to load cart products")
                        }
                        is ProductStateResults.SuccessLoadingCartProducts -> {
                            val quantity = state.cartDetails["totalCartQuantity"]
                            viewBinding.viewCartButton.text = "View Cart ($quantity)"
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
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart: called", )
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "onResume: called", )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: called", )
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: called", )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: called", )
    }
}
