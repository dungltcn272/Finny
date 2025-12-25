package com.ltcn272.finny.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import com.ltcn272.finny.MainActivity
import com.ltcn272.finny.R
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri

object NotificationUtils {
    private const val DEFAULT_CHANNEL_ID = "finny_default_channel_v7"
    private const val DEFAULT_CHANNEL_NAME = "Finny Notifications"

    private fun getManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun ensureDefaultChannel(ctx: Context) {
        val mgr = getManager(ctx)
        if (mgr.getNotificationChannel(DEFAULT_CHANNEL_ID) == null) {
            val soundUri = "android.resource://${ctx.packageName}/${R.raw.message_sound}".toUri()
            val chan = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General app notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                setSound(soundUri, null)
            }
            mgr.createNotificationChannel(chan)
        }
    }

    fun showNotification(ctx: Context, title: String, body: String, data: Map<String, String> = emptyMap()) {
        ensureDefaultChannel(ctx)

        val notificationType = data["type"] ?: "unknown"
        val displayInfo = getNotificationDisplayInfo(notificationType)

        val largeIconBitmap = createCircularBitmapFromVector(
            context = ctx,
            vectorResId = displayInfo.iconRes,
            backgroundColor = displayInfo.color,
            iconColor = Color.WHITE
        )

        val launchIntent = Intent(ctx, MainActivity::class.java).apply {
            val extras = Bundle().apply { data.forEach { (k, v) -> putString(k, v) } }
            putExtras(extras)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = "android.resource://${ctx.packageName}/${R.raw.message_sound}".toUri()

        val builder = NotificationCompat.Builder(ctx, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIconBitmap)
            .setColor(displayInfo.color)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSound(soundUri)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        getManager(ctx).notify(notificationId, builder.build())
    }

    private data class NotificationDisplayInfo(
        @DrawableRes val iconRes: Int,
        val color: Int
    )

    private fun getNotificationDisplayInfo(type: String): NotificationDisplayInfo {
        return when (type.uppercase()) {
            "INCOME" -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_income,
                color = "#2E7D32".toColorInt()
            )
            "OUTCOME" -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_outcome,
                color = "#C62828".toColorInt()
            )
            "TRANSACTION_CREATED" -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_add_transaction,
                color = "#4A8BFF".toColorInt()
            )
            "TRANSACTION_UPDATED" -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_edit_transaction,
                color = "#4A8BFF".toColorInt()
            )
            "BUDGET_THRESHOLD_REACHED" -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_warning,
                color = "#FD7F6B".toColorInt()
            )
            else -> NotificationDisplayInfo(
                iconRes = R.drawable.ic_notification,
                color = "#4A8BFF".toColorInt()
            )
        }
    }

    private fun createCircularBitmapFromVector(
        context: Context,
        @DrawableRes vectorResId: Int,
        backgroundColor: Int,
        iconColor: Int,
        sizeDp: Int = 48
    ): Bitmap {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            this.color = backgroundColor
            isAntiAlias = true
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        val drawable = ContextCompat.getDrawable(context, vectorResId)!!
        DrawableCompat.setTint(drawable, iconColor)

        val iconSize = (sizePx * 0.6).toInt()
        val halfIconSize = iconSize / 2
        val left = (sizePx / 2) - halfIconSize
        val top = (sizePx / 2) - halfIconSize
        drawable.setBounds(left, top, left + iconSize, top + iconSize)
        drawable.draw(canvas)

        return bitmap
    }
}
