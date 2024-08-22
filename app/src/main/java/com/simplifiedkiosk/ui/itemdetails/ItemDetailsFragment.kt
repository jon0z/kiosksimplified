package com.simplifiedkiosk.ui.itemdetails

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemDetailsBinding
import com.simplifiedkiosk.model.CartItem
import com.simplifiedkiosk.viewmodel.CartViewModel
import com.simplifiedkiosk.viewmodel.ItemDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemDetailsFragment : Fragment() {

    private lateinit var viewBinding: FragmentItemDetailsBinding
    private val itemDetailsViewModel: ItemDetailsViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

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
        val itemId = arguments?.getString("itemId") ?: return
        Log.e("ItemDetailsFragment", "item id = $itemId")

        // Load item details
        itemDetailsViewModel.loadItemDetails(itemId)

        // Observe the item details from the ViewModel and update the UI
        lifecycleScope.launch {
            itemDetailsViewModel.itemDetails
                .filterNotNull()
                .collectLatest { item ->
                    viewBinding.itemName.text = item.name
                    viewBinding.itemDescription.text = item.description
                    viewBinding.itemPrice.text = "$${item.price}"
                    // Handle the Add to Cart button click
                    viewBinding.addToCartButton.setOnClickListener {
                        cartViewModel.addItemToCart(CartItem(item))
                    }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            findNavController().navigate(R.id.action_itemDetailsFragment_to_cartFragment)
        }

        // Observe the cart item count and update the TextView
        lifecycleScope.launch {
            cartViewModel.cartItems
                .collectLatest { cartItems ->
                    var itemCount = 0
                    cartItems.forEach {
                        itemCount += it.quantity
                    }
                    viewBinding.cartItemCountTextView.text = "Items in Cart: ${itemCount}"
            }
        }
    }
}
