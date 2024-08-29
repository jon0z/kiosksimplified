package com.simplifiedkiosk.ui.cart

import android.Manifest
import android.location.Address
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.text.NumberFormat
import javax.inject.Inject


private const val TAG = "CartFragment"
@AndroidEntryPoint
class CartFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var viewBinding: FragmentCartBinding
    private var mCurrentAddress: Address? = null

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

        viewBinding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val cartAdapter = CartAdapter(){
            // ask user to update quantity
            Log.e(TAG, "onViewCreated: got click in from cart adapter with productId ${it.productId}\n dbId ${it.dbId}")
        }
        viewBinding.cartRecyclerView.adapter = cartAdapter


        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                cartViewModel.cartState
                    .filterNotNull()
                    .collectLatest { state ->
                    when (state) {
                        is CartState.FailedLoadingCartItems -> {
                            showAlertDialog(requireContext(), title = "Error", message = state.error.message.toString())
                        }
                        CartState.Loading -> {}
                        is CartState.SuccessLoadingCartItems -> {
                            cartAdapter.submitList(cartViewModel.getCartProducts())
                            val subtotal = cartViewModel.getCartTotalPrice()
                            updateCartCalculations(cartViewModel.getCartTotalPrice(), 0.08)
                            cartViewModel.location
                                .filterNotNull()
                                .map {
                                    val geocoder = Geocoder(requireContext())
                                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                }.filterNotNull()
                                .collectLatest { updateCartCalculations(subtotal, 0.08, it.first()) }
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

    private fun updateCartCalculations(subtotal: Double, tax: Double, address: Address? = null) {
        mCurrentAddress = address

        val totalPriceFmt = NumberFormat.getCurrencyInstance().format(subtotal)
        viewBinding.cartCalculationsContainer.subtotalTextview.text = "$totalPriceFmt"

        val totalTax = subtotal * tax // 8% tax
        val totalTaxFmt = NumberFormat.getCurrencyInstance().format(totalTax)
        viewBinding.cartCalculationsContainer.taxesTextview.text = "$totalTaxFmt"

        address?.let {
            val city = it.locality
            val state = it.adminArea
            val zipcode = it.postalCode
            viewBinding.locationTextView.text = "Location: $city, $state $zipcode"

            val deliveryFee = subtotal * 0.25
            val deliveryFeeFmt = NumberFormat.getCurrencyInstance().format(deliveryFee)
            viewBinding.cartCalculationsContainer.deliveryFeeTextview.text = "$deliveryFeeFmt"

            val netTotal = subtotal.plus(tax).plus(deliveryFee)
            val netTotalFmt = NumberFormat.getCurrencyInstance().format(netTotal)
            viewBinding.cartCalculationsContainer.totalTextview.text = "$netTotalFmt"
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        cartViewModel.fetchLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // User denied permissions, handle this case
    }
}
