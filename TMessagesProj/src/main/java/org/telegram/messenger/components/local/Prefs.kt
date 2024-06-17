package org.telegram.messenger.components.local

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.util.Log
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * Created by Siddikov Mukhriddin on 9/6/21
 */

object Prefs {
    private const val NAME = "uz.smd.telegram.islamic.storage"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }


    private var mLocation: String
        get() = preferences.getString("mLocation", "") ?: ""
        set(value) = preferences.edit { it.putString("mLocation", value) }



    fun setLocation(loc: Location) {
        try {
            mLocation = Gson().toJson(
                LatLong(
                    time = Date(loc.time).getStringWithServerTimeStamp(),
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    speed = if (loc.hasSpeed()) loc.speed.toInt() else 0,
            )).toString()
            Log.e("TTT", mLocation)
        } catch (e: Exception) {
            Log.e("TTT", e.toString())
        }
    }

    fun getLocation(): LatLong? {
        return try {
            if (mLocation.isNotBlank())
                Gson().fromJson(mLocation, LatLong::class.java)
            else null
        } catch (e: Exception) {
            Log.e("TTT", e.toString())
            null
        }
    }

    /** Converting from Date to String**/


}
fun Date.getStringWithServerTimeStamp(): String {
    val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ssZ",
        Locale.getDefault()
    )
//    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormat.format(this)
}