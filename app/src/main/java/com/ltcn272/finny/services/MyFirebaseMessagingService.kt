package com.ltcn272.finny.services

import android.util.Log // <<< THÊM IMPORT
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ltcn272.finny.domain.repository.FcmRepository
import com.ltcn272.finny.util.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmRepository: FcmRepository

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val TAG = "MyFirebaseMsgService"

    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { sendTokenToServer(it) }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // --- BẮT ĐẦU LOGGING ---
        Log.d(TAG, "-------------------------------------------------")
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Log toàn bộ Data Payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message Data payload: ${remoteMessage.data}")
        }

        // 2. Log Notification Payload (nếu có)
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Title: ${it.title}")
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
        Log.d(TAG, "-------------------------------------------------")
        // --- KẾT THÚC LOGGING ---


        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Finny"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""

        NotificationUtils.showNotification(applicationContext, title, body, remoteMessage.data)
    }

    private fun sendTokenToServer(token: String) {
        if (token.isEmpty()) return

        ioScope.launch {

            fcmRepository.updateOrCreateFcmToken(token)
                .catch { e -> Log.e(TAG, "Failed to send FCM token to server", e) } // Thêm log lỗi
                .collect()
        }
    }
}

