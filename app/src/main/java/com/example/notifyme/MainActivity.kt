package com.example.notifyme

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.notifyme.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var activityMainBinding: ActivityMainBinding? = null
    private val primaryChanelID = "primary_notification_channel"
    private var notifyManager: NotificationManager? = null
    private val notificationID = 0
    private val actionUpdateNotification =
        "com.example.notifyme.actionUpdateNotification"
    private val actionCancelNotification =
        "com.example.notifyme.actionCancelNotification"
    private val mReceiver = NotificationReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewBinding()
        setOnClickListener()
        createNotificationChannel()
    }

    // unregister your receiver
    override fun onDestroy() {
        unregisterReceiver(mReceiver)
        super.onDestroy()
    }

    private fun setUpViewBinding() {
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)
        setNotificationButtonState(true, false, false)

        // register receiver
        val intentFilter = IntentFilter()
        intentFilter.addAction(actionUpdateNotification)
        intentFilter.addAction(actionCancelNotification)
        registerReceiver(mReceiver, intentFilter)
    }

    private fun setOnClickListener() {
        activityMainBinding?.btnNotify?.setOnClickListener(View.OnClickListener { sendNotification() })
        activityMainBinding?.btnUpdate?.setOnClickListener(View.OnClickListener { updateNotification() })
        activityMainBinding?.btnCancel?.setOnClickListener(View.OnClickListener { cancelNotification() })
    }

    private fun sendNotification() {
        val updateIntent = Intent(actionUpdateNotification)
        val updatePendingIntent = PendingIntent.getBroadcast(
            this,
            notificationID,
            updateIntent,
            PendingIntent.FLAG_ONE_SHOT // Flag indicating that this PendingIntent can be used only once
        )

        // set if use dismiss intent
        val cancelIntent = Intent(actionCancelNotification)
        val cancelPendingIntent = PendingIntent.getBroadcast(
            this,
            notificationID,
            cancelIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notifyBuilder = getNotificationBuilder()
        notifyBuilder.addAction(
            R.drawable.ic_menu_upload,
            "Update Notification",
            updatePendingIntent
        )
        notifyBuilder.setDeleteIntent(cancelPendingIntent)
        notifyManager?.notify(notificationID, notifyBuilder.build())
        setNotificationButtonState(false, true, true)
    }

    private fun cancelNotification() {
        notifyManager?.cancel(notificationID)
        setNotificationButtonState(true, false, false)
    }

    private fun updateNotification() {
        val androidImage = BitmapFactory
            .decodeResource(resources, R.drawable.star_big_on)
        val notifyBuilder = getNotificationBuilder()
        notifyBuilder.setStyle(
            NotificationCompat.BigPictureStyle()
                .bigPicture(androidImage)
                .setBigContentTitle("Notification Updated!")
        )
        notifyManager?.notify(notificationID, notifyBuilder.build())
        setNotificationButtonState(false, false, true)
    }

    fun setNotificationButtonState(
        isNotifyEnabled: Boolean,
        isUpdateEnabled: Boolean,
        isCancelEnabled: Boolean
    ) {
        activityMainBinding?.btnNotify?.isEnabled = isNotifyEnabled
        activityMainBinding?.btnNotify?.isEnabled = isUpdateEnabled
        activityMainBinding?.btnNotify?.isEnabled = isCancelEnabled
    }

    private fun createNotificationChannel() {
        // instantiate the NotificationManager
        notifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // check for the device's API version
        // construct and configure a NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                primaryChanelID, "Mascot Notification", NotificationManager.IMPORTANCE_HIGH
            )
            with(notificationChannel) {
                description = "Notification from Mascot"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                notifyManager?.createNotificationChannel(this)
            }
        }
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(
            this, // context
            notificationID, // id
            notificationIntent, // intent
            PendingIntent.FLAG_UPDATE_CURRENT // flag update
        )
        val notifyBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, primaryChanelID)
        return NotificationCompat.Builder(this, primaryChanelID)
            .setContentTitle("You've been notified!")
            .setContentText("This is your notification text.")
            .setSmallIcon(R.drawable.btn_star)
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
    }


    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent!!.action) {
                actionUpdateNotification -> {
                    updateNotification()
                }
                actionCancelNotification -> cancelNotification()
            }
        }
    }
}