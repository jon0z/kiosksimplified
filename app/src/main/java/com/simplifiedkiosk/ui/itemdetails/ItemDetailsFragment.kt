package com.simplifiedkiosk.ui.itemdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemDetailsBinding
import com.simplifiedkiosk.model.FakeCartProduct
import com.simplifiedkiosk.model.toFakeCartProduct
import com.simplifiedkiosk.viewmodel.ItemDetailsState
import com.simplifiedkiosk.viewmodel.ItemDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "ItemDetailsFragment"
@AndroidEntryPoint
class ItemDetailsFragment : Fragment() {

    private lateinit var viewBinding: FragmentItemDetailsBinding
    private val itemDetailsViewModel: ItemDetailsViewModel by viewModels()

    private lateinit var currentProduct: FakeCartProduct

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the item ID from the arguments
        val productId = arguments?.getInt("productId") ?: return
        itemDetailsViewModel.loadProductDetails(productId.toString())

        // Load item details
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                itemDetailsViewModel.itemDetailsState.collectLatest { state ->
                    when (state) {
                        is ItemDetailsState.SuccessLoadingProductDetails -> {
                            Log.e(TAG, "SuccessLoadingProductDetails: ${state.product}")
                            currentProduct = state.product.toFakeCartProduct()

                            viewBinding.itemName.text = state.product.title
                            viewBinding.itemDescription.text = state.product.description
                            viewBinding.itemPrice.text = "$${state.product.price}"
                            viewBinding.productImage.setImageResource(R.drawable.shopping_cart_image_placeholder)
                        }
                        is ItemDetailsState.FailedLoadingProductDetails -> {
                            Log.e(TAG, "Failed to load item details\n ${state.error.message}")
                        }

                        ItemDetailsState.Loading -> {
                            Log.e(TAG, "Loading")
                        }
                        is ItemDetailsState.FailedCreatingCart -> {
                            Log.e(TAG, "FailedCreatingCart\n ${state.error.message}")
                        }
                        is ItemDetailsState.SuccessCreatingCart -> {
                            Log.e(TAG, "SuccessCreatingCart ${state.cart}")
                            val cart = state.cart
                            var cartTotalProducts = 0
                            if(!cart.products.isNullOrEmpty()){
                                cart.products.forEach { fakeCartProduct ->
                                    fakeCartProduct.quantity?.let { cartTotalProducts += it }
                                }
                            }
//                            val cartQuantity = cart.products.sumOf { it.quantity }
                            Log.e(TAG, "cartQuantity: $cartTotalProducts", )
                            viewBinding.cartItemCountTextView.text = "Items in Cart: $cartTotalProducts"
                        }

                        is ItemDetailsState.FailedToGetUserCarts -> {
                            Log.e(TAG, "FailedToGetUserCarts\n ${state.error.message}")
                        }
                        is ItemDetailsState.SuccessGettingUserCarts -> {
                            val carts = state.carts
                            Log.e(TAG, "SuccessGettingUserCarts ${carts }}")
                            if(carts.isNotEmpty()){
                                carts.forEach {
                                    Log.e(TAG, "$it")
                                }
                            }
                        }
                    }
                }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            findNavController().navigate(R.id.action_itemDetailsFragment_to_cartFragment)
        }

        viewBinding.addToCartButton.setOnClickListener {
            Log.e(TAG, "clicked add to cart")
            itemDetailsViewModel.addToCart(listOf(currentProduct))
            Log.e(TAG, "addToCart() called", )
        }

        viewBinding.viewCartButton.setOnClickListener {
            itemDetailsViewModel.getUserCart()
        }
    }
}
