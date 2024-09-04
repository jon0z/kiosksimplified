package com.simplifiedkiosk.ui.cart

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentCartBinding
import com.simplifiedkiosk.model.Product
import com.simplifiedkiosk.utils.formatDoubleToCurrencyString
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.CartState
import com.simplifiedkiosk.viewmodel.CartViewModel
import com.simplifiedkiosk.viewmodel.CheckoutState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions


private const val TAG = "CartFragment"
@AndroidEntryPoint
class CartFragment : Fragment(), EasyPermissions.PermissionCallbacks, MenuProvider {

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var viewBinding: FragmentCartBinding
    private lateinit var mCartAdapter: CartAdapter
    private var mCurrentAddress: Address? = null
    private var mSubTotal: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCartBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Cart"

        if (hasLocationPermission()) {
            cartViewModel.fetchLocation()
        } else {
            requestLocationPermission()
        }

        viewBinding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        mCartAdapter = CartAdapter(::removeProductFromCart, ::addProductToCart)
        viewBinding.cartRecyclerView.adapter = mCartAdapter


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
                            mCartAdapter.submitList(cartViewModel.getCartProducts())
                            mSubTotal = cartViewModel.getCartTotalPrice()
                            updateCartCalculations(cartViewModel.getCartTotalPrice(), 0.08)
                            cartViewModel.location
                                .filterNotNull()
                                .map {
                                    val geocoder = Geocoder(requireContext())
                                    geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                }.filterNotNull()
                                .collectLatest {
                                    enableButtons(true)
                                    updateCartCalculations(mSubTotal, 0.08, it.first())
                                }
                        }

                        is CartState.FailedAddingProductToCart -> {
                            showAlertDialog(requireContext(), title = "Error", message = "Failed to add product to cart with error: ${state.error.message}")
                        }
                        is CartState.FailedRemovingProductFromCart -> {
                            showAlertDialog(requireContext(), title = "Error", message = "Failed to remove product from cart with error: ${state.error.message}")
                        }
                        is CartState.SuccessAddingProductToCart -> {
                            updateCartCalculations(cartViewModel.getCartTotalPrice(), 0.08, mCurrentAddress)
                        }
                        is CartState.SuccessRemovingProductFromCart -> {
                            updateCartCalculations(cartViewModel.getCartTotalPrice(), 0.08, mCurrentAddress)
                        }

                        is CartState.FailedClearingCart -> {
                            showAlertDialog(
                                context = requireContext(),
                                title = "Error",
                                message = "Failed to clear cart with error: ${state.error.message}"
                            )
                        }
                        is CartState.SuccessClearingCart -> {
                            // clear cart adapter and update calculations
                            showAlertDialog(
                                context = requireContext(),
                                title = "Success",
                                message = "Cart cleared successfully",
                                positiveButtonText = "Ok",
                                onPositiveClick = {
                                    mCartAdapter.submitList(emptyList())
                                    updateCartCalculations(0.0, 0.08, mCurrentAddress)
                                    findNavController().popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }

        viewBinding.checkoutButton.setOnClickListener {
            if(cartViewModel.getCartSize() != 0){
                val checkoutState = CheckoutState(
                    cartSubTotal = mSubTotal,
                    totalCartQuantity = cartViewModel.getCartSize(),
                    cartProducts = cartViewModel.getCartProducts(),
                    address = mCurrentAddress
                )
                findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment, Bundle().apply{
                    putParcelable("checkoutState", checkoutState)
                })
            } else {
                showAlertDialog(requireContext(), title = "Error", message = "Cart is empty")
            }
        }

        viewBinding.cartDiscountContainer.applyDiscountButton.setOnClickListener {
            if(viewBinding.cartDiscountContainer.discountCodeInput.text.isNullOrBlank()){
                showAlertDialog(requireContext(), title = "Error", message = "Please enter a valid discount code")
            } else {
                // Apply 10% discount

            }
        }
    }

    private fun enableButtons(enable: Boolean){
        if(enable){
            viewBinding.checkoutButton.isEnabled = true
            viewBinding.cartDiscountContainer.applyDiscountButton.isEnabled = true
        } else {
            viewBinding.checkoutButton.isEnabled = false
            viewBinding.cartDiscountContainer.applyDiscountButton.isEnabled = false
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

    private fun removeProductFromCart(product: Product) {
        product.productId?.let {
            mCartAdapter.removeItem(it.toString())
        }
        cartViewModel.removeProductFromCart(product)
    }

    private fun addProductToCart(product: Product) {
        mCartAdapter.updateCartItem(product)
        cartViewModel.addProductToCart(product)
    }

    private fun updateCartCalculations(subtotal: Double, tax: Double, address: Address? = null) {
        mCurrentAddress = address

        val totalPriceFmt = formatDoubleToCurrencyString(subtotal)
        viewBinding.cartCalculationsContainer.subtotalTextview.text = "$totalPriceFmt"

        val totalTax = subtotal * tax // 8% tax
        val totalTaxFmt = formatDoubleToCurrencyString(totalTax)
        viewBinding.cartCalculationsContainer.taxesTextview.text = "$totalTaxFmt"

        address?.let {
            val city = it.locality
            val state = it.adminArea
            viewBinding.cartCalculationsContainer.deliveryFeeLabel.textSize = 12f
            viewBinding.cartCalculationsContainer.deliveryFeeLabel.text = "Ship to $city, $state:"

            val deliveryFee = subtotal * 0.15 // 15% of subtotal
            val deliveryFeeFmt = formatDoubleToCurrencyString(deliveryFee)
            viewBinding.cartCalculationsContainer.deliveryFeeTextview.text = "$deliveryFeeFmt"

            val netTotal = subtotal.plus(totalTax).plus(deliveryFee)
            val netTotalFmt = formatDoubleToCurrencyString(netTotal)
            viewBinding.cartCalculationsContainer.totalTextview.text = "$netTotalFmt"
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        cartViewModel.fetchLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        // User denied permissions, handle this case
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.cart_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_clear_cart -> {
                showAlertDialog(
                    context = requireContext(),
                    title = "Clear Cart",
                    message = "Are you sure you want to clear your cart?",
                    positiveButtonText = "Yes",
                    onPositiveClick = {
                        cartViewModel.clearCart()
                    }
                )
                true
            }
            else -> false
        }
    }

}
