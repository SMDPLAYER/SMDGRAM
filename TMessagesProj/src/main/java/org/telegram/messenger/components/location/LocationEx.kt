package org.telegram.messenger.components.location


import android.app.*
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * Created by Siddikov Mukhriddin on 6/3/22
 */

object LocationEx{

    var showDialogVerifyLocation:AlertDialog? =null
    fun Activity.showDialogVerifyLocation(message: String, title: String = "", btnText: String = "Ok", granted: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setPositiveButton(btnText
        ) { dialog, which ->
            try {
                if (!isFinishing) showDialogVerifyLocation?.dismiss()
            }catch (e:Exception){
                Log.e("TTTDialogVerifyLocation",e.toString())
            }
            showDialogVerifyLocation = null
            granted()
        }
        builder.setMessage(message)
        try {
            if (!isFinishing) showDialogVerifyLocation?.dismiss()
        }catch (e:Exception){
            Log.e("TTTDialogVerifyLocation",e.toString())
        }
        showDialogVerifyLocation = null
        showDialogVerifyLocation = builder.create()
        try {
            if (!isFinishing) showDialogVerifyLocation?.show()
        }catch (e:Exception){
            Log.e("TTTDialogVerifyLocation",e.toString())
        }

    }








    var gpsDialog: AlertDialog? = null
     fun showSettingsAlert(activity: Activity?) {
        if (activity != null) {
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
            alertDialog.setMessage("Turn on GPS")
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton("Ok"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(intent)
            }

            try {
                if(!activity.isFinishing) gpsDialog?.dismiss()
            }catch (e:Exception){
                Log.e("TTTshowSettingsAlert",e.toString())
            }
            gpsDialog = null
            gpsDialog = alertDialog.create()
            try {
                if(!activity.isFinishing) gpsDialog?.show()
            }catch (e:Exception){
                Log.e("TTTshowSettingsAlert",e.toString())
            }
        }

    }



}