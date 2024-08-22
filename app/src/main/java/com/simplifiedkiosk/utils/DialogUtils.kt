package com.simplifiedkiosk.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun showAlertDialog(
    context: Context,
    title: String,
    message: String,
    positiveButtonText: String = "OK",
    negativeButtonText: String? = null,
    onPositiveClick: (() -> Unit)? = null,
    onNegativeClick: (() -> Unit)? = null
) {
    val builder = AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick?.invoke()
            dialog.dismiss()
        }

    if (negativeButtonText != null) {
        builder.setNegativeButton(negativeButtonText) { dialog, _ ->
            onNegativeClick?.invoke()
            dialog.dismiss()
        }
    }

    builder.create().show()
}
