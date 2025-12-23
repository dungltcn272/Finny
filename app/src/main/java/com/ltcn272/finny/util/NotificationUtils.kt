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
import android.media.RingtoneManager
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.toColorInt
import com.ltcn272.finny.MainActivity
import com.ltcn272.finny.R
import androidx.core.graphics.createBitmap

object NotificationUtils {
    private const val DEFAULT_CHANNEL_ID = "finny_default_channel_v2"
    private const val DEFAULT_CHANNEL_NAME = "Finny Notifications"

    private fun getManager(ctx: Context): NotificationManager {
        return ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun ensureDefaultChannel(ctx: Context) {
        val mgr = getManager(ctx)

        if (mgr.getNotificationChannel(DEFAULT_CHANNEL_ID) == null) {
            val chan = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // <<< QUAN TRỌNG: Để có Heads-up notification
            ).apply {
                description = "General app notifications"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
            }
            mgr.createNotificationChannel(chan)
        }
    }

    fun showNotification(ctx: Context, title: String, body: String, data: Map<String, String> = emptyMap()) {
        ensureDefaultChannel(ctx)

        val notificationType = data["type"] ?: "unknown"
        val displayInfo = getNotificationDisplayInfo(notificationType)
        val smallIconResId = imageVectorToDrawableRes(displayInfo.icon)

        val largeIconBitmap = createCircularBitmapFromVector(
            context = ctx,
            vectorResId = smallIconResId,
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

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(ctx, DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Small icon PHẢI là icon đơn sắc, trắng
            .setLargeIcon(largeIconBitmap) // <<< SỬ DỤNG LARGE ICON
            .setColor(displayInfo.color)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH) // <<< QUAN TRỌNG: Ưu tiên cao cho Heads-up
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        getManager(ctx).notify(notificationId, builder.build())
    }

    private data class NotificationDisplayInfo(val icon: ImageVector, val color: Int)

    private fun getNotificationDisplayInfo(type: String): NotificationDisplayInfo {
        return when (type.uppercase()) {
            "OUTCOME", "INCOME", "TRANSACTION_CREATED", "TRANSACTION_UPDATED" -> NotificationDisplayInfo(
                Icons.Default.Sync, "#4A8BFF".toColorInt()
            )
            "BUDGET_THRESHOLD_REACHED" -> NotificationDisplayInfo(
                Icons.Default.Warning, "#FD7F6B".toColorInt()
            )
            else -> NotificationDisplayInfo(
                Icons.Default.Notifications, Color.BLUE
            )
        }
    }

    @DrawableRes
    private fun imageVectorToDrawableRes(imageVector: ImageVector): Int {
        return when (imageVector.name) {
            Icons.Default.Sync.name -> R.drawable.ic_transaction
            Icons.Default.Warning.name -> R.drawable.ic_warning
            else -> R.drawable.ic_notification
        }
    }

    private fun createCircularBitmapFromVector(
        context: Context,
        @DrawableRes vectorResId: Int,
        backgroundColor: Int,
        iconColor: Int,
        sizeDp: Int = 64 // <<< TĂNG KÍCH THƯỚC ICON LÊN
    ): Bitmap {
        val sizePx = (sizeDp * context.resources.displayMetrics.density).toInt()
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        // Vẽ nền tròn
        val paint = Paint().apply {
            this.color = backgroundColor
            isAntiAlias = true
        }
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        // Vẽ icon ở giữa
        val drawable = ContextCompat.getDrawable(context, vectorResId)!!
        DrawableCompat.setTint(drawable, iconColor)
        val iconSize = (sizePx * 0.6).toInt() // Giữ tỷ lệ icon bên trong
        val halfIconSize = iconSize / 2
        val left = (sizePx / 2) - halfIconSize
        val top = (sizePx / 2) - halfIconSize
        drawable.setBounds(left, top, left + iconSize, top + iconSize)
        drawable.draw(canvas)

        return bitmap
    }
}
