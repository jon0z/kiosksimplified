package com.simplifiedkiosk.ui.itemdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemDetailsBinding
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.utils.formatDoubleToCurrencyString
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

    private lateinit var currentProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemDetailsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolBarTitle =
            (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
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
                            ItemDetailsState.Loading -> {}
                            is ItemDetailsState.FailedAddingProductToCart -> {
                                viewBinding.addToCartButton.isEnabled = true
                                state.error.message?.let {
                                    showAlertDialog(
                                        context = requireContext(),
                                        title = "Error",
                                        message = it
                                    )
                                }
                            }

                            is ItemDetailsState.SuccessAddingProductToCart -> {
                                viewBinding.addToCartButton.isEnabled = true
                                val totalCartQuantity = state.cartDetails["totalCartQuantity"]
                                viewBinding.cartItemCountTextView.text =
                                    "Items in Cart: $totalCartQuantity"
                                val totalCartPrice = state.cartDetails["totalCartPrice"]
                                viewBinding.cartTotalPriceTextView.text =
                                    "Cart Total (pre-tax): $$totalCartPrice"
                                viewBinding.viewCartButton.text = "View Cart($totalCartQuantity)"
                            }

                            is ItemDetailsState.FailedLoadingCartItems -> {}
                            is ItemDetailsState.SuccessLoadingCartItems -> {
                                val totalCartQuantity = state.cartDetails["totalCartQuantity"]
                                viewBinding.cartItemCountTextView.text =
                                    "Items in Cart: $totalCartQuantity"
                                val totalCartPrice = state.cartDetails["totalCartPrice"]
                                viewBinding.cartTotalPriceTextView.text =
                                    "Cart Total (Pre-tax): $$totalCartPrice"
                                viewBinding.viewCartButton.text =
                                    if (totalCartQuantity == "0") "View Cart" else "View Cart($totalCartQuantity)"
                            }

                            is ItemDetailsState.FailedLoadingReactProductDetails -> {
                                state.error.message?.let {
                                    showAlertDialog(
                                        context = requireContext(),
                                        title = "Error",
                                        message = it
                                    )
                                }
                            }

                            is ItemDetailsState.SuccessLoadingReactProductDetails -> {
                                currentProduct = state.product
                                viewBinding.itemName.text = state.product.title
                                viewBinding.itemDescription.text = state.product.description
                                state.product.price?.let {
                                    viewBinding.itemPrice.text = formatDoubleToCurrencyString(it)
                                }
                                val images = state.product.images
                                if (!images.isNullOrEmpty()) {
                                    images.forEach {
                                        val image = ImageView(requireActivity())
                                        Picasso.get().load(it).into(image)
                                        viewBinding.imageListContainer.addView(image)
                                    }
                                }
                            }

                            is ItemDetailsState.FailedAddingProductToFavorites -> {
                                showAlertDialog(
                                    context = requireActivity(),
                                    title = "Error",
                                    message = "Failed to add product to favorites. Reason: ${state.error.message}"
                                )
                            }

                            is ItemDetailsState.SuccessAddingProductToFavorites -> {
                                viewBinding.favoritesIcon.setImageResource(R.drawable.favorite_24_black)
                                Toast
                                    .makeText(
                                        requireActivity(),
                                        "Product added to favorites",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }

                            is ItemDetailsState.FailedRemovingProductFromFavorites -> {
                                showAlertDialog(
                                    context = requireActivity(),
                                    title = "Error",
                                    message = "Failed to remove product from favorites. Reason: ${state.error.message}"
                                )
                            }

                            is ItemDetailsState.SuccessRemovingProductFromFavorites -> {
                                viewBinding.favoritesIcon.setImageResource(R.drawable.baseline_favorite_border_24)
                                Toast
                                    .makeText(
                                        requireActivity(),
                                        "Product removed from favorites",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                    }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            if (itemDetailsViewModel.getCartSize() != 0) {
                findNavController().navigate(R.id.action_itemDetailsFragment_to_cartFragment)
            } else {
                showAlertDialog(
                    context = requireContext(),
                    title = "Cart is empty",
                    message = "Please add items to continue"
                )
            }
        }

        viewBinding.favoritesIcon.setOnClickListener {
            currentProduct.isFavorite?.let {
                if (it) {
                    currentProduct.isFavorite = false
                    itemDetailsViewModel.removeFromFavorites(currentProduct)
                } else {
                    currentProduct.isFavorite = true
                    itemDetailsViewModel.addToFavorites(currentProduct)
                }
            }
        }

        viewBinding.addToCartButton.setOnClickListener {
            viewBinding.addToCartButton.isEnabled = false
            itemDetailsViewModel.addToCart(currentProduct)
        }
    }
}
