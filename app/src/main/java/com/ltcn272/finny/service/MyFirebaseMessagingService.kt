package com.ltcn272.finny.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ltcn272.finny.domain.repository.FcmRepository
import com.ltcn272.finny.util.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmRepository: FcmRepository

    private val TAG = "MyFirebaseMsgService"
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (!token.isNullOrEmpty()) {
                    ioScope.launch {
                        try {
                            fcmRepository.updateOrCreateFcmToken(token)
                        } catch (_: Throwable) {}
                    }
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ioScope.launch {
            try {
                fcmRepository.updateOrCreateFcmToken(token)
            } catch (_: Throwable) {}
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Log.d(TAG, "Message received: from=${remoteMessage.from}")

            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Notification"
            val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""

            remoteMessage.notification?.let { n ->
                Log.d(TAG, "Notification: title=${n.title} body=${n.body}")
            }
            if (remoteMessage.data.isNotEmpty()) Log.d(TAG, "Data payload: ${remoteMessage.data}")

            try {
                NotificationUtils.showNotification(applicationContext, title, body, remoteMessage.data)
                Log.d(TAG, "Displayed notification: title=$title body=$body")
            } catch (t: Throwable) {
                Log.w(TAG, "Failed to show notification", t)
            }

        } catch (t: Throwable) {
            Log.w(TAG, "Error processing incoming FCM message", t)
        }
    }
}
