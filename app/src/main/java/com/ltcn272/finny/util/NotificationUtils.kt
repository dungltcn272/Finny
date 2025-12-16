package com.ltcn272.finny.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.ltcn272.finny.R

object NotificationUtils {
    private const val DEFAULT_CHANNEL_ID = "finny_default_channel"
    private const val DEFAULT_CHANNEL_NAME = "Finny Notifications"

    private fun getManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun ensureDefaultChannel(ctx: Context) {
        val mgr = getManager(ctx)
        val existing = mgr.getNotificationChannel(DEFAULT_CHANNEL_ID)
        if (existing == null) {
            val chan = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            chan.description = "General app notifications"
            chan.enableLights(true)
            chan.lightColor = Color.BLUE
            chan.enableVibration(true)
            mgr.createNotificationChannel(chan)
        }
    }

    fun showNotification(ctx: Context, title: String, body: String, data: Map<String, String> = emptyMap()) {
        ensureDefaultChannel(ctx)

        // Create pending intent to open launcher activity when notification tapped
        val packageManager = ctx.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(ctx.packageName)
        val pendingIntent: PendingIntent? = if (launchIntent != null) {
            // attach data as extras so tapping the notification can route if needed
            val extras = Bundle().apply {
                data.forEach { (k, v) -> putString(k, v) }
            }
            launchIntent.putExtras(extras)
            PendingIntent.getActivity(ctx, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or getImmutableFlag())
        } else null

        val builder = NotificationCompat.Builder(ctx, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (pendingIntent != null) builder.setContentIntent(pendingIntent)

        val notif = builder.build()
        val id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        getManager(ctx).notify(id, notif)
    }

    private fun getImmutableFlag(): Int {
        return PendingIntent.FLAG_IMMUTABLE
    }
}
