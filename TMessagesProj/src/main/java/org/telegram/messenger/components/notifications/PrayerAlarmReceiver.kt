package org.telegram.messenger.components.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val prayerName = intent?.getStringExtra("prayer_name") ?: return
        val prayerTime = intent.getStringExtra("prayer_time") ?: return

        if (context != null) {
            NotificationUtil.showNotification(
                context,
                "Prayer Time: $prayerName",
                "It's time for $prayerName prayer at $prayerTime"
            )
        }
    }
}
