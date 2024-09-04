package com.simplifiedkiosk.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simplifiedkiosk.utils.SharedPreferencesManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val creditCardState = remember { mutableStateOf(CreditCardState()) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CreditCardForm(
                        card = null,
                        onCardSave = { card ->
                            SharedPreferencesManager.saveCreditCard(requireContext(), card)
                            creditCardState.value = CreditCardState()
                        },
                        onCardCancel = {
                            creditCardState.value = CreditCardState()
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}