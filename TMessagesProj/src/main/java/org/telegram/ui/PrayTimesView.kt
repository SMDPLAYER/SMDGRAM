package org.telegram.ui

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.AndroidUtilities.dp
import org.telegram.messenger.R
import org.telegram.messenger.components.local.Prefs
import org.telegram.messenger.components.location.LocationEx
import org.telegram.messenger.components.location.LocationEx.showDialogVerifyLocation
import org.telegram.messenger.components.notifications.NotificationUtil
import org.telegram.messenger.components.notifications.schedulePrayerAlarms
import org.telegram.messenger.components.permissions.checkPermission
import org.telegram.messenger.components.permissions.checkPermissionLocation
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Calendar
import java.util.Date
import java.util.Locale


class PrayTimesView @JvmOverloads constructor(
    context: Context,

    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var currentAccount: Int = 0
    private var k = true
    private var height0 = 55f
    private val holderPrayTimes:LinearLayout
    private val bigLayout:LinearLayout
    private val miniLayout:LinearLayout

    var latitude = 30.045411
    var longitude = 31.236735
    init {
        // Inflate XML layout
        LayoutInflater.from(context).inflate(R.layout.view_pray_times, this, true)
        holderPrayTimes = findViewById(R.id.holderPrayTimes)
        miniLayout = findViewById(R.id.miniLayout)
        bigLayout = findViewById(R.id.bigLayout)
        NotificationUtil.createNotificationChannel(context)
        holderPrayTimes.setOnClickListener {

            // Create notification channel

            valueChangeListener?.invoke(Prefs.getLocation()==null)
            val lp1 = holderPrayTimes.layoutParams

            height0 = if (miniLayout.isVisible) 105f  else 55f
            miniLayout.isVisible = !miniLayout.isVisible
            bigLayout.isVisible = !bigLayout.isVisible

            lp1.height = dp(height0)

            holderPrayTimes.layoutParams = lp1

            k = !k
            requestLayout()
        }
    }
    var valueChangeListener: ((Boolean) -> Unit)? = null
    fun checkAlarmPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                }
                context.startActivity(intent)
            }
        }
    }
    fun updateLocation(){
        try {
            update()
        }catch (e:Exception){
            Log.e("TTT",e.toString())
        }
    }

    fun hide(hide:Boolean){
        holderPrayTimes.isGone = hide
    }

    fun setBackgroundColor1(color: Int) {
        holderPrayTimes.setBackgroundColor(color)
    }
    fun update(){
        val loc = Prefs.getLocation()
        if (loc!=null){
            latitude = loc.latitude
            longitude = loc.longitude
        } else {
            getDeviceLocation(context)
        }
        // Launch a coroutine to perform network operation on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            val methodId = Prefs.prayCalculationMethod
            val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val tomorrow = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.time)
            val yesterday = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time)


            val tune = "0,${Prefs.addTimeFajr},${Prefs.addTimeMorning},${Prefs.addTimeDhuhur},${Prefs.addTimeAsr},0,${Prefs.addTimeMaghrib},${Prefs.addTimeIsha},0"
            val todayResponse = getJsonResponse("https://api.aladhan.com/v1/timings/$today?latitude=$latitude&longitude=$longitude&method=$methodId&tune=$tune")
            val tomorrowResponse = getJsonResponse("https://api.aladhan.com/v1/timings/$tomorrow?latitude=$latitude&longitude=$longitude&method=$methodId&tune=$tune")
            val yesterdayResponse = getJsonResponse("https://api.aladhan.com/v1/timings/$yesterday?latitude=$latitude&longitude=$longitude&method=$methodId&tune=$tune")
            todayResponse?.let {todayResult->
                tomorrowResponse?.let {tomorrowResult->
                    yesterdayResponse?.let {yesterdayResult->
                        // Handle the JSON response on the main thread
                        withContext(Dispatchers.Main) {
                            handleJsonResponse(todayResponse,tomorrowResponse,yesterdayResponse,today,tomorrow,yesterday)
                        }
                    }

                }
            }

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
                            blockTry{
                                AndroidUtilities.runOnUIThread {
                                    blockTry{
                                        update()
//                                        Toast.makeText(context,"Prefs" + latitude + " " + longitude,Toast.LENGTH_SHORT).show()

                                    }
                                }
                            }

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

    private suspend fun getJsonResponse(urlString: String): String? {
        var urlConnection: HttpURLConnection? = null
        var reader: BufferedReader? = null
        var jsonResponse: String? = null

        try {
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            val inputStream = urlConnection.inputStream
            reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            jsonResponse = stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
            reader?.close()
        }
        return jsonResponse
    }

    fun updateTimes(
        fajr:String,
        sunrise:String,
        dhuhr:String,
        asr:String,
        maghrib:String,
        isha:String,
        fajrTomorrow:String,
        ishaYesterday:String,
    ){
        findViewById<TextView>(R.id.tvTimeFajr).text = fajr
        findViewById<TextView>(R.id.tvTimeMorning).text = sunrise
        findViewById<TextView>(R.id.tvTimeDhuhur).text = dhuhr
        findViewById<TextView>(R.id.tvTimeAsr).text = asr
        findViewById<TextView>(R.id.tvTimeMagrib).text = maghrib
        findViewById<TextView>(R.id.tvTimeIsha).text = isha

        val currentTime = LocalTime.now()
        val fajrTime = LocalTime.parse(fajr)
        val sunriseTime = LocalTime.parse(sunrise)
        val dhuhrTime = LocalTime.parse(dhuhr)
        val asrTime = LocalTime.parse(asr)
        val maghribTime = LocalTime.parse(maghrib)
        val ishaTime = LocalTime.parse(isha)
        when {
            currentTime.isBefore(fajrTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Isha"
                findViewById<TextView>(R.id.tvTime1).text = ishaYesterday
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_isha)
                val colorInt = Color.parseColor("#EB6FFF")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_isha)

                findViewById<TextView>(R.id.tvTitle2).text="Fajr"
                findViewById<TextView>(R.id.tvTime2).text = fajr
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_fajr)
                val colorInt2 = Color.parseColor("#5BA8EF").toInt()
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_fajr)
            }
            currentTime.isBefore(sunriseTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Fajr"
                findViewById<TextView>(R.id.tvTime1).text = fajr
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_fajr)
                val colorInt = Color.parseColor("#5BA8EF")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_fajr)


                findViewById<TextView>(R.id.tvTitle2).text="Morning"
                findViewById<TextView>(R.id.tvTime2).text = sunrise
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_sunrise)
                val colorInt2 = Color.parseColor("#99DDA8")
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_sunrise)

            }
            currentTime.isBefore(dhuhrTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Morning"
                findViewById<TextView>(R.id.tvTime1).text = sunrise
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_sunrise)
                val colorInt = Color.parseColor("#99DDA8")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_sunrise)

                findViewById<TextView>(R.id.tvTitle2).text="Dhuhur"
                findViewById<TextView>(R.id.tvTime2).text = dhuhr
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_zuhr)
                val colorInt2 = Color.parseColor("#AAE55F")
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_zuhr)
            }
            currentTime.isBefore(asrTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Dhuhur"
                findViewById<TextView>(R.id.tvTime1).text = dhuhr
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_zuhr)
                val colorInt = Color.parseColor("#AAE55F")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_zuhr)


                findViewById<TextView>(R.id.tvTitle2).text="Asr"
                findViewById<TextView>(R.id.tvTime2).text = asr
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_asr)
                val colorInt2 = Color.parseColor("#EBE555")
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_asr)

            }
            currentTime.isBefore(maghribTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Asr"
                findViewById<TextView>(R.id.tvTime1).text = asr
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_asr)
                val colorInt = Color.parseColor("#EBE555")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_asr)


                findViewById<TextView>(R.id.tvTitle2).text="Maghrib"
                findViewById<TextView>(R.id.tvTime2).text = maghrib
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_magrib)
                val colorInt2 = Color.parseColor("#F69D6B")
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_magrib)

            }
            currentTime.isBefore(ishaTime) -> {
                findViewById<TextView>(R.id.tvTitle1).text="Maghrib"
                findViewById<TextView>(R.id.tvTime1).text = maghrib
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_magrib)
                val colorInt = Color.parseColor("#F69D6B")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_magrib)


                findViewById<TextView>(R.id.tvTitle2).text="Isha"
                findViewById<TextView>(R.id.tvTime2).text = isha
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_isha)
                val colorInt2 = Color.parseColor("#EB6FFF")
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_isha)

            }
            else-> {
                findViewById<TextView>(R.id.tvTitle1).text="Isha"
                findViewById<TextView>(R.id.tvTime1).text = isha
                findViewById<ImageView>(R.id.imgSun1).setImageResource(R.drawable.ic_pray_time_isha)
                val colorInt1 = Color.parseColor("#EB6FFF")
                findViewById<TextView>(R.id.tvTitle1).setTextColor(colorInt1)
                findViewById<TextView>(R.id.tvTime1).setTextColor(colorInt1)
                findViewById<TextView>(R.id.tvTime1).setBackgroundResource(R.drawable.ic_pray_time_bg_isha)

                findViewById<TextView>(R.id.tvTitle2).text="Fajr"
                findViewById<TextView>(R.id.tvTime2).text = fajrTomorrow
                findViewById<ImageView>(R.id.imgSun2).setImageResource(R.drawable.ic_pray_time_fajr)
                val colorInt2 = Color.parseColor("#5BA8EF").toInt()
                findViewById<TextView>(R.id.tvTitle2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setTextColor(colorInt2)
                findViewById<TextView>(R.id.tvTime2).setBackgroundResource(R.drawable.ic_pray_time_bg_fajr)

            }
        }

        requestLayout()
    }
    private fun handleJsonResponse(todayResult: String, tomorrowResult: String,yesterdayResult:String,today:String,tomorrow:String,yesterday:String) {
        try {
            Log.e("TTT_TODAY",todayResult)
            Log.e("TTT_TOMORROW",tomorrowResult)
            Log.e("TTT_YESTERDAY",yesterdayResult)
            // Parse the JSON response
            val todayResponse = JSONObject(todayResult).getJSONObject("data").getJSONObject("timings")
            val tomorrowResponse = JSONObject(tomorrowResult).getJSONObject("data").getJSONObject("timings")
            val yesterdayResponse = JSONObject(yesterdayResult).getJSONObject("data").getJSONObject("timings")



            val fajrTomorrow = tomorrowResponse.getString("Fajr")
            val fajr = todayResponse.getString("Fajr")
            val sunrise = todayResponse.getString("Sunrise")
            val dhuhr = todayResponse.getString("Dhuhr")
            val asr = todayResponse.getString("Asr")
            val maghrib = todayResponse.getString("Maghrib")
            val isha = todayResponse.getString("Isha")
            val ishaYesterday = yesterdayResponse.getString("Isha")
            // Handle the JSON object as needed

            val prayerTimesToday = mapOf(
                "Fajr" to todayResponse.getString("Fajr"),
                "Sunrise" to todayResponse.getString("Sunrise"),
                "Dhuhr" to todayResponse.getString("Dhuhr"),
                "Asr" to todayResponse.getString("Asr"),
                "Maghrib" to todayResponse.getString("Maghrib"),
                "Isha" to todayResponse.getString("Isha"),
            )
            val prayerTimesTomorrow = mapOf(
                "Fajr" to tomorrowResponse.getString("Fajr"),
                "Sunrise" to tomorrowResponse.getString("Sunrise"),
                "Dhuhr" to tomorrowResponse.getString("Dhuhr"),
                "Asr" to tomorrowResponse.getString("Asr"),
                "Maghrib" to tomorrowResponse.getString("Maghrib"),
                "Isha" to tomorrowResponse.getString("Isha"),
            )
            updateTimes(fajr,sunrise,dhuhr,asr,maghrib,isha,fajrTomorrow,ishaYesterday)
            // Schedule alarms for the prayer times
            schedulePrayerAlarms(context, prayerTimesToday, today)
            schedulePrayerAlarms(context, prayerTimesTomorrow, tomorrow)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun getHeight1(): Int {
        return dp(height0)
    }


}




//fun schedulePrayerAlarms(context: Context, timings: Map<String, String>, dateKey: String) {
//    val sharedPreferences: SharedPreferences = context.getSharedPreferences("prayer_times", Context.MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//    timings.forEach { (prayerName, prayerTime) ->
//        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
//        val prayerCalendar = Calendar.getInstance().apply {
//            time = sdf.parse(prayerTime) ?: return@forEach
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//            // Adjust the time to the specified date
//
//            set(Calendar.YEAR, dateKey.substringAfterLast("-").toInt())
//            set(Calendar.MONTH, dateKey.substring(3, 5).toInt() - 1)
//            set(Calendar.DAY_OF_MONTH, dateKey.substringBefore("-").toInt())
//        }
//
//        val currentTime = Calendar.getInstance()
//        if (prayerCalendar.before(currentTime)) return@forEach
//
//        val prayerKey = "$dateKey-$prayerName"
//        val prayerIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
//            putExtra("prayer_name", prayerName)
//            putExtra("prayer_time", prayerTime)
//        }
//
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            prayerKey.hashCode(),
//            prayerIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Cancel existing alarm if it exists
//        alarmManager.cancel(pendingIntent)
//
//        // Schedule new alarm
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(
//                AlarmManager.RTC_WAKEUP,
//                prayerCalendar.timeInMillis,
//                pendingIntent
//            )
//        }
//
//        // Update shared preferences
//        editor.putBoolean(prayerKey, true)
//    }
//    editor.apply()
//}

//class PrayerAlarmReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val prayerName = intent?.getStringExtra("prayer_name") ?: return
//        val prayerTime = intent.getStringExtra("prayer_time") ?: return
//
//        if (context != null) {
//            NotificationUtil.showNotification(
//                context,
//                "Prayer Time: $prayerName",
//                "It's time for $prayerName prayer at $prayerTime"
//            )
//        }
//    }
//}

//object NotificationUtil {
//    private const val CHANNEL_ID = "prayer_times_channel"
//    private const val CHANNEL_NAME = "Prayer Times"
//    private const val CHANNEL_DESC = "Notifications for prayer times"
//
//    fun createNotificationChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                CHANNEL_ID,
//                CHANNEL_NAME,
//                NotificationManager.IMPORTANCE_HIGH
//            ).apply {
//                description = CHANNEL_DESC
//            }
//            val notificationManager: NotificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//        }
//    }
//
//    fun showNotification(context: Context, title: String, message: String) {
//        val soundUri = Uri.parse("android.resource://${context.packageName}/raw/prayer_time_sound")
//
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setSmallIcon(R.drawable.msg_mini_autodelete_timer) // Replace with your own icon
//            .setContentTitle(title)
//            .setContentText(message)
//            .setSound(soundUri)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//
//        with(NotificationManagerCompat.from(context)) {
//            notify(System.currentTimeMillis().toInt(), builder.build())
//        }
//    }
//}
