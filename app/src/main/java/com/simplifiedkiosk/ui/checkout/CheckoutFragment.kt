package com.simplifiedkiosk.ui.checkout

import android.os.Bundle
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
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentCheckoutBinding
import com.simplifiedkiosk.utils.formatAddressToStringAddressDetails
import com.simplifiedkiosk.utils.formatDoubleToCurrencyString
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.CheckoutState
import com.simplifiedkiosk.viewmodel.CheckoutStateResults
import com.simplifiedkiosk.viewmodel.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


private const val TAG = "CheckoutFragment"
@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private lateinit var viewBinding: FragmentCheckoutBinding
    private var mSelectedPaymentMethod: String = "gpay"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolBarTitle = (activity as AppCompatActivity).supportActionBar?.customView?.findViewById<TextView>(R.id.toolbar_title)
        toolBarTitle?.text = "Checkout"
        arguments?.let { bundle ->
            bundle.getParcelable<CheckoutState>("checkoutState")?.let { checkoutState ->
                checkoutViewModel.loadCheckOutStateFromCart(checkoutState)
            }
        }

        viewBinding.proceedToPaymentButton.setOnClickListener {
            if(checkoutViewModel.getCartSize() > 0) {
                handlePaymentProcess()
            } else {
                showAlertDialog(
                    context = requireActivity(),
                    title = "Missing Shipping Address",
                    message = "Please add shipping address before checkout",
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                checkoutViewModel.checkoutState
                    .filterNotNull()
                    .collectLatest { state ->
                        when(state){
                            CheckoutStateResults.Loading -> {}
                            is CheckoutStateResults.ReceivedProductsFromCartSummary -> {
                                loadCartProducts(state.checkoutState)
                            }
                            is CheckoutStateResults.ClearedCartSuccess -> {
                                findNavController().navigate(R.id.action_checkoutFragment_to_itemListFragment)
                            }
                            is CheckoutStateResults.FailedClearedCart -> {
                                showAlertDialog(
                                    context = requireActivity(),
                                    title = "Error",
                                    message = state.error.message ?: "Failed to clear cart",
                                )
                            }
                            is CheckoutStateResults.FailedPaymentProcessing -> {
                                showAlertDialog(
                                    context = requireActivity(),
                                    title = "Payment Failed",
                                    message = "There was an error processing your payment. Please try again.",
                                    positiveButtonText = "Retry",
                                    negativeButtonText = "Cancel",
                                    onPositiveClick = {
                                        handlePaymentProcess()
                                    },
                                    onNegativeClick = {
                                        findNavController().navigate(R.id.action_checkoutFragment_to_itemListFragment)
                                    }
                                )
                            }
                            is CheckoutStateResults.SuccessfulPaymentProcessed -> {
                                showAlertDialog(
                                    context = requireActivity(),
                                    title =  "Payment Successful",
                                    message = "Your payment was processed successfully!",
                                    positiveButtonText = "Back to products",
                                    onPositiveClick = {
                                        checkoutViewModel.emptyCart()
                                        findNavController().navigate(R.id.action_checkoutFragment_to_itemListFragment)
                                    }
                                )
                            }
                        }
                    }
            }
        }
        
        viewBinding.paymentsContainer.paymentMethodGroup.setOnCheckedChangeListener { radioGroup, checkedId ->
            when(checkedId){
                R.id.paymentCreditCard -> {
                    mSelectedPaymentMethod = "creditCard"

                }
                R.id.paymentGpay -> {
                    mSelectedPaymentMethod = "gpay"
                }
            }
        }

        viewBinding.paymentsContainer.addNewCard.setOnClickListener {
            findNavController().navigate(R.id.action_checkoutFragment_to_paymentsFragment)
        }

        viewBinding.addressContainer.addNewAddress.setOnClickListener {
            findNavController().navigate(R.id.action_checkoutFragment_to_addressFragment)
        }
    }

    private fun loadCartProducts(state: CheckoutState){
        viewBinding.cartCalculationsContainer.subtotalTextview.text = formatDoubleToCurrencyString(state.cartSubTotal)
        val taxes = state.cartSubTotal * state.taxRate
        viewBinding.cartCalculationsContainer.taxesTextview.text = formatDoubleToCurrencyString(taxes)
        val shippingCharges = state.cartSubTotal * state.shippingRate
        viewBinding.cartCalculationsContainer.deliveryFeeTextview.text = formatDoubleToCurrencyString(shippingCharges)
        val total = state.cartSubTotal.plus(taxes).plus(shippingCharges)
        viewBinding.cartCalculationsContainer.totalTextview.text = formatDoubleToCurrencyString(total)

        state.address?.let {
            viewBinding.addressContainer.deliveryAddressDetails.text = formatAddressToStringAddressDetails(it)
        }
    }

    private fun handlePaymentProcess(){
        if(mSelectedPaymentMethod.isNotBlank()){
            checkoutViewModel.processPayment(mSelectedPaymentMethod)
        } else {
            showAlertDialog(
                context = requireActivity(),
                title = "Missing Shipping Address",
                message = "Please add shipping address before checkout",
            )
        }
    }
}
