package com.simplifiedkiosk.ui.checkout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.simplifiedkiosk.R
import com.simplifiedkiosk.databinding.FragmentCheckoutBinding
import com.simplifiedkiosk.utils.showAlertDialog
import com.simplifiedkiosk.viewmodel.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private val checkoutViewModel: CheckoutViewModel by viewModels()
    private lateinit var viewBinding: FragmentCheckoutBinding

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

        viewBinding.checkoutRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        val checkoutAdapter = CheckoutAdapter()
        viewBinding.checkoutRecyclerView.adapter = checkoutAdapter

        // Observe the total amount and cart items
        lifecycleScope.launch {
            checkoutViewModel.totalAmount.collectLatest { totalAmount ->
                viewBinding.totalAmountTextView.text = "Total: $$totalAmount"
            }
        }

        lifecycleScope.launch {
            checkoutViewModel.cartItems.collectLatest { cartItems ->
                // Update RecyclerView adapter with cartItems (adapter logic to be implemented)
                checkoutAdapter.submitList(cartItems)
            }
        }

        viewBinding.proceedToPaymentButton.setOnClickListener {
            if(!viewBinding.shippingAddressEditText.text.isNullOrBlank()){
                handlePaymentProcess()
            } else {
                showAlertDialog(
                    context = requireActivity(),
                    title = "Missing Shipping Address",
                    message = "Please add shipping address before checkout",
                )
            }
        }
    }

    private fun hideSoftKeyboard(){
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun handlePaymentProcess(){
        val shippingAddress = viewBinding.shippingAddressEditText.text.toString()
        val paymentSuccessful = checkoutViewModel.processPayment(shippingAddress)
        if (paymentSuccessful) {
            showAlertDialog(
                context = requireActivity(),
                title =  "Payment Successful",
                message = "Your payment was processed successfully!",
                onPositiveClick = {}
                )
            // clear cart
            // clear totals
            viewBinding.totalAmountTextView.text = ""
            // clear address field
            viewBinding.shippingAddressEditText.text.clear()
            hideSoftKeyboard()
            // Navigate to a confirmation screen or back to the item list
            findNavController().navigate(R.id.action_checkoutFragment_to_itemListFragment)

        } else {
            showAlertDialog(
                context = requireActivity(),
                title = "Payment Failed",
                message = "There was an error processing your payment. Please try again.",
                positiveButtonText = "Retry",
                negativeButtonText = "Cancel",
                onPositiveClick = {},
                onNegativeClick = {}
            )
        }
    }
}
