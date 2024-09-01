package com.simplifiedkiosk.ui.itemdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemDetailsBinding
import com.simplifiedkiosk.model.ReactProduct
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.ItemDetailsState
import com.simplifiedkiosk.viewmodel.ItemDetailsViewModel
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


private const val TAG = "ItemDetailsFragment"
@AndroidEntryPoint
class ItemDetailsFragment : Fragment() {

    private lateinit var viewBinding: FragmentItemDetailsBinding
    private val itemDetailsViewModel: ItemDetailsViewModel by viewModels()

    private lateinit var currentProduct: ReactProduct

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Product Details"

        itemDetailsViewModel.loadCartItems()
        // Get the item ID from the arguments
        val productId = arguments?.getInt("productId") ?: return
        itemDetailsViewModel.loadProductDetails(productId)

        // Load item details
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                itemDetailsViewModel.itemDetailsState
                    .collectLatest { state ->
                    when (state) {
                        ItemDetailsState.Loading -> { Log.e(TAG, "Loading...") }
                        is ItemDetailsState.FailedAddingProductToCart -> {
                            showAlertDialog(requireContext(), "Error", state.error.message.toString())
                        }
                        is ItemDetailsState.SuccessAddingProductToCart -> {
                            viewBinding.addToCartButton.isEnabled = true
                            val totalCartQuantity = state.cartDetails["totalCartQuantity"]
                            viewBinding.cartItemCountTextView.text = "Items in Cart: $totalCartQuantity"
                            val totalCartPrice = state.cartDetails["totalCartPrice"]
                            viewBinding.cartTotalPriceTextView.text = "Cart Total (pre-tax): $$totalCartPrice"
                            viewBinding.viewCartButton.text = "View Cart($totalCartQuantity)"
                        }
                        ItemDetailsState.FailedLoadingCartItems -> {}
                        is ItemDetailsState.SuccessLoadingCartItems -> {
                            val totalCartQuantity = state.cartDetails["totalCartQuantity"]
                            viewBinding.cartItemCountTextView.text = "Items in Cart: $totalCartQuantity"
                            val totalCartPrice = state.cartDetails["totalCartPrice"]
                            viewBinding.cartTotalPriceTextView.text = "Cart Total (Pre-tax): $$totalCartPrice"
                            viewBinding.viewCartButton.text = if(totalCartQuantity == "0") "View Cart" else "View Cart($totalCartQuantity)"
                        }

                        is ItemDetailsState.FailedLoadingReactProductDetails -> {
                            showAlertDialog(requireContext(), "Error", state.error.message.toString())
                        }
                        is ItemDetailsState.SuccessLoadingReactProductDetails -> {
                            currentProduct = state.product
                            viewBinding.itemName.text = state.product.title
                            viewBinding.itemDescription.text = state.product.description
                            viewBinding.itemPrice.text = "$${state.product.price}"

                            val images = state.product.images
                            if(!images.isNullOrEmpty()){
                                images.forEach {
                                    val image = ImageView(requireActivity())
                                    Picasso.get().load(it).into(image)
                                    viewBinding.imageListContainer.addView(image)
                                }
                            }
                        }
                    }
                }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            if(itemDetailsViewModel.getCartSize() != 0) {
                findNavController().navigate(R.id.action_itemDetailsFragment_to_cartFragment)
            } else {
                showAlertDialog(requireContext(), "Cart is empty", "Please add items to continue")
            }
        }

        viewBinding.addToCartButton.setOnClickListener {
            itemDetailsViewModel.addToCart(currentProduct)
            viewBinding.addToCartButton.isEnabled = false
        }
    }
}
