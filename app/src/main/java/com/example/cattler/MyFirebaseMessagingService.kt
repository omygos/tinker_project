package com.example.cattler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_Service"

    /**
     * This is called when your app is first installed or when the token is refreshed.
     * You MUST send this token to your Raspberry Pi server!
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // **IMPORTANT ACTION FOR YOU:**
        // You need to create a new API endpoint on your Pi
        // (e.g., /api/register-device) and send this 'token'
        // to it. Your Pi must store this token to know
        // who to send messages to.
        sendTokenToServer(token)
    }

    /**
     * This is called when a message is received from Firebase
     * while your app is in the foreground OR background.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if the message contains data (the info from your Pi)
        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"] ?: "Cattle Alert"
            val body = remoteMessage.data["body"] ?: "A cattle is out of range!"

            Log.d(TAG, "Message Title: $title")
            Log.d(TAG, "Message Body: $body")

            // Call the function to show the notification
            sendNotification(title, body)
        }
    }

    private fun sendTokenToServer(token: String) {
        // This is where you would make a Retrofit call
        // to your new server endpoint.
        // e.g., lifecycleScope.launch {
        //    RetrofitInstance.api.registerDevice(token)
        // }
    }

    /**
     * Builds and displays the notification on the user's screen.
     */
    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "cattle_alert_channel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // --- Create Notification Channel (Required for Android 8.0+) ---
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
        // --- End of Channel Code ---

        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // TODO: Change to a real alert icon
            .setContentTitle(title)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show the notification
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}