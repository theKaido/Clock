package org.fossify.clock.services

import android.app.IntentService
import android.content.Intent
import org.fossify.clock.extensions.config
import org.fossify.clock.extensions.dbHelper
import org.fossify.clock.extensions.hideNotification
import org.fossify.clock.extensions.setupAlarmClock
import org.fossify.clock.helpers.ALARM_ID
import org.fossify.commons.helpers.MINUTE_SECONDS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import org.fossify.clock.R
import org.fossify.clock.activities.MainActivity
import org.fossify.clock.helpers.ALARM_NOTIFICATION_CHANNEL_ID
import org.fossify.clock.helpers.ALARM_NOTIF_ID
import org.fossify.clock.models.Alarm


class SnoozeService : IntentService("Snooze") {
    override fun onHandleIntent(intent: Intent?) {
        val id = intent!!.getIntExtra(ALARM_ID, -1)
        val alarm = dbHelper.getAlarmWithId(id) ?: return
        hideNotification(id)
        setupAlarmClock(alarm, config.snoozeTime * MINUTE_SECONDS)
        showSnoozeNotification(alarm)
    }
    private fun showSnoozeNotification(alarm: Alarm) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(ALARM_NOTIFICATION_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    ALARM_NOTIFICATION_CHANNEL_ID,
                    "Alarm Snooze",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Channel for Alarm Snooze Notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }
        }


        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val builder = NotificationCompat.Builder(this, ALARM_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_vector)
            .setContentTitle(getString(R.string.alarm_snoozed))
            .setContentText(getString(R.string.alarm_will_ring_again, config.snoozeTime))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        notificationManager.notify(ALARM_NOTIF_ID, builder.build())
    }
}
