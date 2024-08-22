package com.simplifiedkiosk.ui.itemlist

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentItemListBinding
import com.simplifiedkiosk.viewmodel.CartViewModel
import com.simplifiedkiosk.viewmodel.ItemListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ItemListFragment : Fragment() {

    private val viewModel: ItemListViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var viewBinding: FragmentItemListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentItemListBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = ItemAdapter { item ->
            // Navigate to item details when an item is clicked
            Log.e("ItemListFrag", "item id = ${item.id}")
            val action = ItemListFragmentDirections.actionItemListFragmentToItemDetailsFragment(item.id)
            findNavController().navigate(action)
        }
        viewBinding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.items.collectLatest { items ->
                adapter.submitList(items)
            }
        }

        lifecycleScope.launch {
            cartViewModel.cartItems.collectLatest { cartItems ->
                var itemCount = 0
                cartItems.forEach {
                    itemCount += it.quantity
                }
                viewBinding.viewCartButton.text = if (itemCount > 0) {
                    "View Cart ($itemCount)"
                } else {
                    "View Cart"
                }
            }
        }

        viewBinding.viewCartButton.setOnClickListener {
            findNavController().navigate(R.id.action_itemListFragment_to_cartFragment)
        }
    }
}
