package com.simplifiedkiosk.utils

import android.content.Context
import com.google.gson.Gson
import com.simplifiedkiosk.model.ShippingAddress
import com.simplifiedkiosk.ui.checkout.CreditCard

object SharedPreferencesManager {
    private const val PREFS_NAME = "shipping_address_prefs"
    private const val SHIPPING_ADDRESS_KEY = "shipping_address"
    private const val CREDIT_CARD_KEY = "credit_card"

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

    fun saveCreditCard(context: Context, creditCard: CreditCard) {
        val gson = Gson()
        val json = gson.toJson(creditCard)
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(CREDIT_CARD_KEY, json).apply()
    }

    fun getCreditCard(context: Context): CreditCard? {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = preferences.getString(CREDIT_CARD_KEY, null)
        return if (json != null) {
            val gson = Gson()
            gson.fromJson(json, CreditCard::class.java)
        } else {
            null
        }
    }

    fun deleteCreditCard(context: Context) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().remove(CREDIT_CARD_KEY).apply()
    }
}