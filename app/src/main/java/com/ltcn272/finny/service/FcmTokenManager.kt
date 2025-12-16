package com.ltcn272.finny.service

import com.google.firebase.messaging.FirebaseMessaging
import com.ltcn272.finny.domain.repository.FcmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility to send the current FCM token to your backend.
 * Call `sendCurrentTokenIfAvailableAsync()` after a successful login to associate
 * the device's token with the logged-in user on the server.
 */
@Singleton
class FcmTokenManager @Inject constructor(
    private val fcmRepository: FcmRepository
) {

    /**
     * Non-suspending helper that fetches the current FCM token and sends it to the backend.
     * It uses an IO coroutine for the network call and swallows errors so callers don't need
     * to handle failures.
     */
    fun sendCurrentTokenIfAvailableAsync() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (!token.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            fcmRepository.updateOrCreateFcmToken(token)
                        } catch (t: Throwable) {
                            // ignore/log
                        }
                    }
                }
            }
        }
    }

}

