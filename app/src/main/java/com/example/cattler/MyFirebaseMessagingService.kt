package com.example.cattler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_Service"

    /**
     * This is called when your app is first installed or when the token is refreshed.
     * You MUST send this token to your Raspberry Pi server!
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // ✅ Send token to your Raspberry Pi server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if the message contains data (the info from your Pi)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Cattle Alert"
            val body = remoteMessage.data["body"] ?: "A cattle is out of range!"

            Log.d(TAG, "Message Title: $title")
            Log.d(TAG, "Message Body: $body")

            // Show the notification
            sendNotification(title, body)
        }
    }

    /**
     * ✅ Sends the FCM token to your Raspberry Pi backend.
     */
    private fun sendTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Build your request body (you must have FcmTokenRequest data class)
                val requestBody = FcmTokenRequest(token = token)

                // Call your Retrofit endpoint
                RetrofitInstance.api.registerDeviceToken(requestBody)

                Log.d(TAG, "Token sent to server successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending token to server: ${e.message}", e)
            }
        }
    }

    /**
     * Shows a notification when a message arrives from Firebase.
     */
    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "cattle_alert_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel (for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cattle Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for cattle moving out of range"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Replace with proper icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show the notification
        notificationManager.notify(0, notificationBuilder.build())
    }
}
