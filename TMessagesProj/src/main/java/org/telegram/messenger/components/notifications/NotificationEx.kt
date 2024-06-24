package org.telegram.messenger.components.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.telegram.messenger.components.local.Prefs
import java.text.SimpleDateFormat
import java.util.*

fun schedulePrayerAlarms(context: Context, prayerTimes: Map<String, String>, dateKey: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

    for ((prayer, time) in prayerTimes) {
        try {
            if (Prefs.prayNotificationsBlockList.contains(prayer)) return

            val prayerTime = "$dateKey $time"
            val date = dateFormat.parse(prayerTime)
            date?.let {
                val calendar = Calendar.getInstance().apply {
                    this@apply.time = date
                }

                // Create a unique intent for each prayer
                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    putExtra("prayer_name", prayer)
                    putExtra("prayer_time", dateKey)
                }
                val pendingIntent = PendingIntent.getBroadcast(context, prayer.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                // Schedule or update the alarm
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


//fun schedulePrayerAlarms(context: Context, prayerTimes: Map<String, String>, dateKey: String) {
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
//    val sharedPreferences = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
//    val editor = sharedPreferences.edit()
//
//    for ((prayer, time) in prayerTimes) {
//        try {
//            val prayerTime = "$dateKey $time"
//            val date = dateFormat.parse(prayerTime)
//            date?.let {
//                val calendar = Calendar.getInstance().apply {
//                    this@apply.time = date
//                }
//
//                // Cancel the old alarm
//                val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
//                    putExtra("prayer_name", prayer)
//                    putExtra("prayer_time", prayerTime)
//                }
//                val pendingIntent = PendingIntent.getBroadcast(context, prayer.hashCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                alarmManager.cancel(pendingIntent)
//
//                // Schedule the new alarm
//                if (calendar.timeInMillis > System.currentTimeMillis()) {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
//                    }else{
//                        //TODO
//                    }
//                    editor.putBoolean("$dateKey-$prayer", true).apply()
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }
//}

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
//            set(Calendar.YEAR, dateKey.substringAfterLast("-").toInt())
//            set(Calendar.MONTH, dateKey.substring(3, 5).toInt() - 1)
//            set(Calendar.DAY_OF_MONTH, dateKey.substringBefore("-").toInt())
//
//            set(Calendar.YEAR, dateKey.substring(0, 4).toInt())
//            set(Calendar.MONTH, dateKey.substring(5, 7).toInt() - 1)
//            set(Calendar.DAY_OF_MONTH, dateKey.substring(8).toInt())
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
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            prayerCalendar.timeInMillis,
//            pendingIntent
//        )
//
//        // Update shared preferences
//        editor.putBoolean(prayerKey, true)
//    }
//    editor.apply()
//}
