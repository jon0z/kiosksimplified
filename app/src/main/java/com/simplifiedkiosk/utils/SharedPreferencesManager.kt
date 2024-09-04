package com.simplifiedkiosk.utils

import android.content.Context
import com.google.gson.Gson
import com.simplifiedkiosk.model.ShippingAddress

object SharedPreferencesManager {
    private const val PREFS_NAME = "shipping_address_prefs"
    private const val SHIPPING_ADDRESS_KEY = "shipping_address"

    fun saveShippingAddress(context: Context, shippingAddress: ShippingAddress) {
        val gson = Gson()
        val json = gson.toJson(shippingAddress)
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(SHIPPING_ADDRESS_KEY, json).apply()
    }

    fun getShippingAddress(context: Context): ShippingAddress? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = preferences.getString(SHIPPING_ADDRESS_KEY, null)
        return if (json != null) {
            val gson = Gson()
            gson.fromJson(json, ShippingAddress::class.java)
        } else {
            null
        }
    }
}