package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.simplifiedkiosk.model.FakeProduct
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

    private var selectedProduct by mutableStateOf<FakeProduct?>(null)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    }
                }
            }
        }

//        lifecycleScope.launch {
//            cartViewModel.cartItems.collectLatest { cartItems ->
//                var itemCount = 0
//                cartItems.forEach {
//                    itemCount += it.quantity
//                }
//                viewBinding.viewCartButton.text = if (itemCount > 0) {
//                    "View Cart ($itemCount)"
//                } else {
//                    "View Cart"
//                }
//            }
//        }

        viewBinding.viewCartButton.setOnClickListener {
//            findNavController().navigate(R.id.action_itemListFragment_to_cartFragment)
        }
    }
}
