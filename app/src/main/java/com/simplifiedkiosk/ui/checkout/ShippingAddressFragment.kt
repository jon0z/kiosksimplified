package com.simplifiedkiosk.ui.checkout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.R
import com.simplifiedkiosk.model.ShippingAddress
import com.simplifiedkiosk.utils.SharedPreferencesManager
import com.simplifiedkiosk.utils.showAlertDialog
import dagger.hilt.android.AndroidEntryPoint


/**
 * A simple [Fragment] subclass.
 * Use the [ShippingAddressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

private const val TAG = "ShippingAddressFragment"
@AndroidEntryPoint
class ShippingAddressFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val savedShippingAddress = SharedPreferencesManager.getShippingAddress(requireActivity())
        return ComposeView(requireContext()).apply {
            setContent {
                val context = LocalContext.current
                ShippingAddressScreen(
                    onShippingAddressChanged = {name, addressLine1, addressLine2, city, state, zipcode ->
                                               val shippingAddress = ShippingAddress(
                                                   name = name,
                                                   addressLine1 = addressLine1,
                                                   addressLine2 = addressLine2,
                                                   city = city,
                                                   state = state,
                                                   zipCode = zipcode
                                               )
                        SharedPreferencesManager.saveShippingAddress(context, shippingAddress)
                    },
                    onShippingAddressSaved = {
                        findNavController().navigate(R.id.action_shippingAddressFragment_to_itemListFragment)
                    },
                    onShippingAddressCanceled = {
                        findNavController().popBackStack()
                    },
                    shippingAddress = savedShippingAddress
                )
            }
        }
    }
}