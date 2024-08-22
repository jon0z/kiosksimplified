package com.simplifiedkiosk.ui.cart

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentCartBinding
import com.simplifiedkiosk.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class CartFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var viewBinding: FragmentCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCartBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasLocationPermission()) {
            cartViewModel.fetchLocation()
        } else {
            requestLocationPermission()
        }

        lifecycleScope.launch {
            cartViewModel.location.collect { location ->
                location?.let {
                    viewBinding.locationTextView.text = "Location: ${it.latitude}, ${it.longitude}"
                    // You can also use this location to calculate shipping charges
                }
            }
        }

        viewBinding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val cartAdapter = CartAdapter()
        viewBinding.cartRecyclerView.adapter = cartAdapter

        // Observe the cart items and total price from the ViewModel
        lifecycleScope.launch {
            cartViewModel.cartItems.collect { cartItems ->
                // Update RecyclerView adapter with cartItems
                cartAdapter.submitList(cartItems)
            }
        }

        lifecycleScope.launch {
            cartViewModel.totalPrice.collectLatest { totalPrice ->
                viewBinding.totalPriceTextView.text = "Total: $$totalPrice"
            }
        }

        viewBinding.checkoutButton.setOnClickListener {
            // handle checkout process
            findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This app needs access to your location to calculate shipping charges.",
            123,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        cartViewModel.fetchLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // User denied permissions, handle this case
    }
}
