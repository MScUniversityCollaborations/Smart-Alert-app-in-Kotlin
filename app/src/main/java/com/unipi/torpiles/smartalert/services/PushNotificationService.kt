package com.unipi.torpiles.smartalert.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unipi.torpiles.smartalert.utils.Constants


class PushNotificationService : FirebaseMessagingService() {

   override fun onMessageReceived(remoteMessage: RemoteMessage) {
       // super.onMessageReceived(remoteMessage)

       Log.d(Constants.TAG, "Received Notification From: ${remoteMessage.from}")

       val title: String? = remoteMessage.notification?.title
       val body: String? = remoteMessage.notification?.body

       val CHANNEL_ID = "NOTIFICATION_SUBMISSION"

       val channel = NotificationChannel(
           CHANNEL_ID,
           "Heads Up Notification",
           NotificationManager.IMPORTANCE_HIGH
       )

       getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
       val notification = Notification.Builder(this, CHANNEL_ID)
           .setContentTitle(title)
           .setContentText(body)
           .setSmallIcon(R.drawable.btn_plus)
           .setAutoCancel(true)

       NotificationManagerCompat.from(this).notify(1, notification.build());
    }

    override fun onNewToken(token: String) {}
}
