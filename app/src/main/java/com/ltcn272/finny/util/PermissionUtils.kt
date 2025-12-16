package com.ltcn272.finny.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object PermissionUtils {
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
