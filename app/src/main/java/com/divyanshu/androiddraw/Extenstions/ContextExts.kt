package com.divyanshu.androiddraw.Extenstions

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.divyanshu.androiddraw.R

var alertDialog: AlertDialog? = null
var appUpdateDialog: AlertDialog? = null

fun initDialog(context: Context, title: String, message: String) {

    if (alertDialog != null) {
        alertDialog!!.dismiss()
    }

    alertDialog = AlertDialog.Builder(context).create()

    if (title.isNotEmpty()) {
        alertDialog?.setTitle(title)
    }

    alertDialog?.setMessage(message)
}

fun Context.showDialog(message: String, title: String = "") {

    initDialog(this, title, message)

    alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(android.R.string.ok)) { dialog, _ ->
        dialog.dismiss()
    }

    alertDialog?.show()
}

fun Context.showDialogWithBackNavigation(mActivity: AppCompatActivity, message: String, title: String = "") {

    initDialog(this, title, message)

    alertDialog?.setCancelable(false)

    alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, resources.getString(android.R.string.ok)) { dialog, _ ->
        dialog.dismiss()
        mActivity.onBackPressed()
    }

    alertDialog?.show()
}

fun Context.showDialogWithAction(
        message: String, title: String = "", positiveButtonLabel: String = resources.getString(android.R.string.ok)
        , showNegativeButton: Boolean = false, setCancelable: Boolean = false, function: () -> Unit
) {

    initDialog(this, title, message)

    alertDialog?.setCancelable(setCancelable)

    alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonLabel) { dialog, _ ->
        function()
        dialog.dismiss()
    }

    if (showNegativeButton) {
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, resources.getString(android.R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
    }

    alertDialog?.show()
}

fun Context.showDialogWithActions(
        message: String, title: String = "", positiveButtonLabel: String = resources.getString(android.R.string.ok)
        , negativeButtonLabel: String = resources.getString(android.R.string.cancel), setCancelable: Boolean = false
        , positiveFunction: () -> Unit, negativeFunction: () -> Unit
) {

    initDialog(this, title, message)

    alertDialog?.setCancelable(setCancelable)

    alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonLabel) { dialog, _ ->
        positiveFunction()
        dialog.dismiss()
    }

    alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonLabel) { dialog, _ ->
        dialog.dismiss()
        negativeFunction()
    }

    alertDialog?.show()
}

fun showAlert(context: Context?, message: String) {
    val builder = context?.let { AlertDialog.Builder(it) }
    builder?.setMessage(message)
            ?.setTitle(R.string.app_name)
            ?.setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            ?.show()
}