package org.telegram.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.azan.Azan
import com.azan.Method
import com.azan.astrologicalCalc.SimpleDate
import com.google.android.exoplayer2.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.R
import org.telegram.messenger.components.local.Prefs
import org.telegram.messenger.components.location.LocationEx
import org.telegram.messenger.components.location.LocationEx.showDialogVerifyLocation
import org.telegram.messenger.components.permissions.checkPermissionLocation
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class PrayTimesView @JvmOverloads constructor(
    context: Context,

    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var currentAccount: Int = 0
    private var k = true
    private var height0 = 50f
    private val holderPrayTimes:LinearLayout
    private val tvTitle:TextView
    private val btnUpdate:TextView

    var latitude = 30.045411
    var longitude = 31.236735
    var gmtOffset = 2.0
    var isDST = 0
    init {
        // Inflate XML layout
        LayoutInflater.from(context).inflate(R.layout.view_pray_times, this, true)
        holderPrayTimes = findViewById(R.id.holderPrayTimes)
        tvTitle = findViewById(R.id.tvTitle)
        btnUpdate = findViewById(R.id.btnUpdate)
        holderPrayTimes.setOnClickListener {
            valueChangeListener?.invoke(Prefs.getLocation()==null)
            val lp1 = holderPrayTimes.layoutParams

            height0 = if (k) 100f  else 200f

            lp1.height = dp(height0)

            holderPrayTimes.layoutParams = lp1

            k = !k
            requestLayout()
        }
        update()
    }
    var valueChangeListener: ((Boolean) -> Unit)? = null
    fun updateLocation(){
        try {
            update()
        }catch (e:Exception){
            Log.e("TTT",e.toString())
        }
    }


    fun update(){
        val loc = Prefs.getLocation()
        if (loc!=null){
            latitude = loc.latitude
            longitude = loc.longitude
            Toast.makeText(context,"Prefs" + latitude + " " + longitude,Toast.LENGTH_SHORT).show()
        } else {
            getDeviceLocation(context)
        }
        getTimeZoneInfo(context)
        val today = SimpleDate(GregorianCalendar())
        val location = com.azan.astrologicalCalc.Location(latitude, longitude,gmtOffset ,isDST)
        val azan = Azan(location, Method.EGYPT_SURVEY)
        val prayerTimes = azan.getPrayerTimes(today)
        val imsaak = azan.getImsaak(today)

        tvTitle.setText(
                    "imsaak ---> $imsaak"
                    +"\n"
                    +"Fajr ---> " + prayerTimes.fajr()
                    +" \n"
                    +"sunrise --->" + prayerTimes.shuruq()
                    +" \n"
                    +"Zuhr --->" + prayerTimes.thuhr()
                    +" \n"
                    +"Asr --->" + prayerTimes.assr()
                    +" \n"
                    +"Maghrib --->" + prayerTimes.maghrib()
                    +" \n"
                    +"ISHA  --->" + prayerTimes.ishaa()
        )
        requestLayout()
    }
    private suspend fun getUtcOffset(context: Context, latitude: Double, longitude: Double): Double? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCoroutine<List<android.location.Address>> { continuation ->
                        geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {

                            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                                continuation.resume(addresses)
                            }

                            override fun onError(errorMessage: String?) {
                                continuation.resume(emptyList())
                            }
                        })
                    }
                } else {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                if (addresses.isNullOrEmpty()) return@withContext null

                val address = addresses[0]
                val timeZone = TimeZone.getDefault() // Default timezone in case of failure to get accurate timezone

                // Attempt to get the accurate timezone
                if (address.hasLatitude() && address.hasLongitude()) {
                    val geocoderTimeZone = TimeZone.getTimeZone(Geocoder(context, Locale.getDefault()).getFromLocation(address.latitude, address.longitude, 1)
                        ?.get(0)?.locality)
                    if (!geocoderTimeZone.id.equals("GMT", true)) {
                        timeZone.id = geocoderTimeZone.id
                    }
                }

                val offsetInMillis = timeZone.getOffset(Date().time)

                gmtOffset = offsetInMillis / 3600000.0 // Convert milliseconds to hours
                isDST = if (timeZone.inDaylightTime(Date())) 1 else 0
                println("GMT Offset: $gmtOffset hours")
                println("DST in effect: $isDST")
                gmtOffset
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    private fun getTimeZoneInfo(context: Context) {
        runBlocking {
            getUtcOffset(context,latitude,longitude)
        }
    }


    fun restoreLocation(activity: Activity?){
        if (activity!=null) if (activity!=null){
            when{
                (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)->{
                    activity.showDialogVerifyLocation("The Islamic Telegram application collects location data to ensure the operation of application functions (for the monitoring center)","Attention!","Ок") {
                        checkPermissionLocation(activity, Manifest.permission.ACCESS_COARSE_LOCATION,{}) {
                            initLocation(activity)
                        }
                    }
                }
                else ->{
                   initLocation(activity)
                }
            }

        }
    }
    fun getDeviceLocation(context: Context?) {
        blockTry {
            if (context!=null)
                fusedLocationProviderClient(context)
                    .getLastLocation().addOnSuccessListener { loc->
                        if (loc != null){
                            Prefs.setLocation(loc)
//                            AndroidUtilities.runOnUIThread {
//                                blockTry{
//                                    latitude = loc.latitude
//                                    longitude = loc.longitude
//                                    getTimeZoneInfo(context)
//                                    update()
//                                    Toast.makeText(context,"Prefs" + latitude + " " + longitude,Toast.LENGTH_SHORT).show()
//
//                                }
//                            }
                        }
                    }
        }
    }
    fun blockTry(block:()->Unit){
        try {
            block.invoke()
        }catch (e: SecurityException) {
                android.util.Log.e("TTT: getDeviceLocation", e.message, e)
            } catch (e: Exception) {
                android.util.Log.e("TTT: getDeviceLocation", e.message, e)
            }

    }
    private fun initLocation(activity: Activity?) {
        // Calling Location Manager
        if (activity != null) {
            checkGPS(activity)
        }
    }

    fun checkGPS(activity: Activity?){
        if (!checkGPSEnabled(activity)) {
            LocationEx.showSettingsAlert(activity)
        } else {
            getDeviceLocation(activity?.applicationContext)
        }
    }
    private fun checkGPSEnabled(activity: Context?):Boolean{
        // Checking GPS is enabled
        val mLocationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private fun fusedLocationProviderClient(activity: Context): FusedLocationProviderClient {
        return if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(activity)
            fusedLocationProviderClient!!
        } else {
            fusedLocationProviderClient!!
        }
    }

    fun getHeight1(): Int {
        return dp(height0)
    }

}