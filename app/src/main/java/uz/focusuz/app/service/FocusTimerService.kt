package uz.focusuz.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.Build
import android.content.pm.ServiceInfo
import uz.focusuz.app.MainActivity

class FocusTimerService : Service() {

    companion object {
        const val CHANNEL_ID = "focus_timer"
        const val NOTIFICATION_ID = 301
    }

    override fun onCreate() {
        super.onCreate()

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fokus taymeri",
            NotificationManager.IMPORTANCE_LOW
        )

        channel.description =
            "Davom etayotgan fokus seansi"

        channel.setSound(null, null)
        channel.enableVibration(false)

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == "STOP") {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val deadline = intent?.getLongExtra(
            "deadline", 0L
        ) ?: 0L

        if (deadline <= System.currentTimeMillis()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(
            this, CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.ic_lock_idle_alarm
            )
            .setContentTitle("FocusUZ")
            .setContentText("Taymer davom etmoqda")
            .setContentIntent(openApp)
            .setWhen(deadline)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notification
            )
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
