package com.simplifiedkiosk.ui.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.simplifiedkiosk.model.ShippingAddress

@Composable
fun ShippingAddressScreen(
    onShippingAddressChanged: (String, String, String, String, String, String) -> Unit,
    onShippingAddressSaved: () -> Unit,
    onShippingAddressCanceled: () -> Unit,
    shippingAddress: ShippingAddress? = null
) {
    var name by remember { mutableStateOf(shippingAddress?.name ?: "") }
    var addressLine1 by remember { mutableStateOf(shippingAddress?.addressLine1 ?:"") }
    var addressLine2 by remember { mutableStateOf(shippingAddress?.addressLine2 ?:"") }
    var city by remember { mutableStateOf(shippingAddress?.city ?:"") }
    var state by remember { mutableStateOf(shippingAddress?.state ?:"") }
    var zipCode by remember { mutableStateOf(shippingAddress?.zipCode ?:"") }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        TextField(
            value = addressLine1,
            onValueChange = { addressLine1 = it },
            label = { Text("Address Line 1") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        TextField(
            value = addressLine2,
            onValueChange = { addressLine2 = it },
            label = { Text("Address Line 2 (Optional)") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("City") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        TextField(
            value = state,
            onValueChange = { state = it },
            label = { Text("State/Province") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        TextField(
            value = zipCode,
            onValueChange = { zipCode = it },
            label = { Text("Zip/Postal Code") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White, textColor = Color.LightGray)
        )

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {

            Button(
                onClick = {
                    onShippingAddressSaved()
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(color = 0XFF007BFF))
            ) {
                Text("Save",
                    color = Color.White)
            }

            OutlinedButton(
                onClick = {
                    onShippingAddressCanceled()
                    focusManager.clearFocus()
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.White, contentColor = Color(color = 0XFF007BFF))
            ) {
                Text("Cancel")
            }
        }
    }

    LaunchedEffect(Unit) {
        onShippingAddressChanged(name, addressLine1, addressLine2, city, state, zipCode)
    }
}

@Preview(showBackground = true)
@Composable
fun ShippingAddressScreenPreview() {
    ShippingAddressScreen(
        onShippingAddressChanged = { name, addressLine1, addressLine2, city, state, zipCode ->
            println("Shipping address changed: $name, $addressLine1, $addressLine2, $city, $state, $zipCode")
        },
        onShippingAddressSaved = {
            println("Shipping address saved")
        },
        onShippingAddressCanceled = {
            println("Shipping address canceled")
        },
    )
}