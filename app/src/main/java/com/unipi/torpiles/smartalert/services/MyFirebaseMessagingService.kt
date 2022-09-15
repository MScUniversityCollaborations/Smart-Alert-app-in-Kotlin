package com.unipi.torpiles.smartalert.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unipi.torpiles.smartalert.R
import com.unipi.torpiles.smartalert.database.FirestoreHelper
import com.unipi.torpiles.smartalert.ui.activities.SubmissionDetailsActivity
import com.unipi.torpiles.smartalert.utils.Constants


class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(Constants.TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(Constants.TAG, "Message data payload: ${remoteMessage.data}")
            sendNotification(remoteMessage.data)
        }
    }

    private fun sendNotification(data: Map<String, String>) {
        val title = String.format(getString(R.string.notification_payload_fav_title),
            data[Constants.PAYLOAD_SUBMISSION_DESC])
        val body = String.format(getString(R.string.notification_payload_fav_body),
            data[Constants.PAYLOAD_SUBMISSION_DESC])

        val resultIntent = Intent(this@MyFirebaseMessagingService, SubmissionDetailsActivity::class.java).run {
                putExtra(Constants.EXTRA_SUBMISSION_ID, data[Constants.PAYLOAD_SUBMISSION_ID])
        }

        // Create the TaskStackBuilder
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            // Add the intent, which inflates the back stack
            addNextIntentWithParentStack(resultIntent)
            // Get the PendingIntent containing the entire back stack
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        // Action broadcasters
        val cancelIntent = Intent(this, NotificationReceiver::class.java).run {
            putExtra(Constants.EXTRA_NOTIFICATION_ID, Constants.NOTIFICATION_ID)
        }
        /*val cancelPendingIntent =
            PendingIntent.getBroadcast(this, Constants.NOTIFICATION_ID, cancelIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )*/

        val productImg = Glide.with(this@MyFirebaseMessagingService)
            .asBitmap()
            .load(data[Constants.PAYLOAD_SUBMISSION_IMG_URL])
            .fitCenter()
            .submit()

        val imgBitmap = productImg.get()

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification: Notification = NotificationCompat.Builder(this@MyFirebaseMessagingService, Constants.NOTIFICATION_CHANNEL_ID)
            .setOngoing(false)
            .setSmallIcon(getNotificationIcon())
            .setAutoCancel(true)
            .setContentIntent(resultPendingIntent)
            // .addAction(R.drawable.ic_heart, getString(R.string.notification_mark_as_read), cancelPendingIntent) // todo
            .setLargeIcon(imgBitmap)
            .setGroup(Constants.GROUP_KEY_FAVORITES)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(imgBitmap)
                .bigLargeIcon(null))
            .setContentTitle(title) // Title
            .setContentText(body) // Body
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // this parameter is used to configure lock screen visibility
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setWhen(System.currentTimeMillis())
            .setColor(getColor(R.color.colorPrimary))
            .setSound(defaultSoundUri).build()

        val notificationManager = this@MyFirebaseMessagingService.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        Glide.with(this).clear(productImg)

        val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_favorites),
            NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = getString(R.string.notification_channel_desc)
                lockscreenVisibility = 1
                setShowBadge(true)
        }

        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(Constants.NOTIFICATION_ID, notification)
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(Constants.TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(Constants.TAG, "Refreshed token: $token")

        if (FirestoreHelper().getCurrentUserID() != "") {
            addTokenToFirestore(token)
        }

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    private fun getNotificationIcon(): Int {
        return R.drawable.ic_stat_name
    }

    companion object {
        fun addTokenToFirestore(newRegistrationToken: String) {
            if (newRegistrationToken.isEmpty()) throw NullPointerException("FCM token is null.")

            FirestoreHelper().getFCMRegistrationTokenDB(newRegistrationToken) { tokenExists ->
                // Checking if current token already exists
                if (tokenExists)
                    return@getFCMRegistrationTokenDB

                FirestoreHelper().addFCMRegistrationToken(newRegistrationToken)
            }
        }
    }
}
