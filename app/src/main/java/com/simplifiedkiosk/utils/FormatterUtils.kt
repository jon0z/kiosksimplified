package com.simplifiedkiosk.utils

import android.location.Address
import java.text.NumberFormat
import java.util.Locale

fun formatStringToCurrency(value: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
}

fun formatAddressToStringAddressDetails(address: Address): String {
    return address.getAddressLine(0)
}