package com.simplifiedkiosk.ui.cart

import android.Manifest
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.simplifiedkiosk.databinding.FragmentCartBinding
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.CartState
import com.simplifiedkiosk.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


private const val TAG = "CartFragment"
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
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Cart"

        if (hasLocationPermission()) {
            cartViewModel.fetchLocation()
        } else {
            requestLocationPermission()
        }

        lifecycleScope.launch {
            cartViewModel.location.collect { location ->
                location?.let {
                    val geocoder = Geocoder(requireContext())
                    val geoAddress = geocoder.getFromLocation(it.latitude, it.longitude, 1)?.first()
                    geoAddress?.let { address ->
                        val city = address.locality
                        val state = address.adminArea
                        val zipcode = address.postalCode
                        viewBinding.locationTextView.text = "Location: $city, $state $zipcode"
                    }
                }
            }
        }

        viewBinding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val cartAdapter = CartAdapter(){
            // ask user to update quantity
            Log.e(TAG, "onViewCreated: got click in from cart adapter with productId ${it.productId}\n dbId ${it.dbId}")
        }
        viewBinding.cartRecyclerView.adapter = cartAdapter


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                cartViewModel.cartState.collectLatest { state ->
                    when (state) {
                        is CartState.FailedLoadingCartItems -> {
                            showAlertDialog(requireContext(), title = "Error", message = state.error.message.toString())
                        }
                        CartState.Loading -> {}
                        is CartState.SuccessLoadingCartItems -> {
                            val totalPrice = cartViewModel.getCartTotalPrice()
                            cartAdapter.submitList(cartViewModel.getCartProducts())
                            viewBinding.totalPriceTextView.text = "Total (Pre-tax): $$totalPrice"
                        }
                    }
                }
            }
        }

        viewBinding.checkoutButton.setOnClickListener {
            if(cartViewModel.getCartSize() != 0){
                findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
            } else {
                showAlertDialog(requireContext(), title = "Error", message = "Cart is empty")
            }
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
