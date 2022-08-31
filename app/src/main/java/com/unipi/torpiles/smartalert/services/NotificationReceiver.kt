package com.unipi.torpiles.smartalert.services

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unipi.torpiles.smartalert.utils.Constants


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {

        val notificationID = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1)
        if (notificationID > 0) {
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationID)
        }

        // todo
        val message = intent.getStringExtra("read")
    }
}
