package com.example.gymtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gymtracker.MainActivity
import com.example.gymtracker.R

class WorkoutNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "gym_workout_active_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "ACTION_START_WORKOUT_NOTIFICATION"
        const val ACTION_UPDATE = "ACTION_UPDATE_WORKOUT_NOTIFICATION"
        const val ACTION_STOP = "ACTION_STOP_WORKOUT_NOTIFICATION"

        const val EXTRA_TITLE = "extra_workout_title"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_REST_SECONDS = "extra_rest_seconds"
        const val EXTRA_IS_RESTING = "extra_is_resting"
        const val EXTRA_IS_COUNTDOWN = "extra_is_countdown"
        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"

        fun start(context: Context, title: String, startTime: Long) {
            try {
                val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_TITLE, title)
                    putExtra(EXTRA_START_TIME, startTime)
                }
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("WorkoutNotificationService", "Failed to start service", e)
            }
        }

        fun updateRest(
            context: Context,
            isResting: Boolean,
            restSeconds: Long,
            title: String? = null,
            isCountdown: Boolean = false,
            remainingSeconds: Long = 0L
        ) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_IS_RESTING, isResting)
                putExtra(EXTRA_REST_SECONDS, restSeconds)
                putExtra(EXTRA_IS_COUNTDOWN, isCountdown)
                putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
                if (title != null) putExtra(EXTRA_TITLE, title)
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            val intent = Intent(context, WorkoutNotificationService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }
    }

    private var currentTitle: String = "Тренировка"
    private var currentStartTime: Long = 0L
    private var isResting: Boolean = false
    private var restSeconds: Long = 0L
    private var isCountdown: Boolean = false
    private var remainingSeconds: Long = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                try {
                    currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Тренировка"
                    currentStartTime = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                    isResting = false
                    restSeconds = 0L
                    val notification = buildNotification()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        } else {
                            0
                        }
                        startForeground(NOTIFICATION_ID, notification, serviceType)
                    } else {
                        startForeground(NOTIFICATION_ID, notification)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WorkoutNotificationService", "Could not startForeground with type specialUse", e)
                    try {
                        val notification = buildNotification()
                        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        nm.notify(NOTIFICATION_ID, notification)
                    } catch (_: Exception) {}
                }
            }
            ACTION_UPDATE -> {
                try {
                    if (intent.hasExtra(EXTRA_TITLE)) {
                        currentTitle = intent.getStringExtra(EXTRA_TITLE) ?: currentTitle
                    }
                    isResting = intent.getBooleanExtra(EXTRA_IS_RESTING, false)
                    restSeconds = intent.getLongExtra(EXTRA_REST_SECONDS, 0L)
                    isCountdown = intent.getBooleanExtra(EXTRA_IS_COUNTDOWN, false)
                    remainingSeconds = intent.getLongExtra(EXTRA_REMAINING_SECONDS, 0L)
                    val notification = buildNotification()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                } catch (e: Exception) {
                    android.util.Log.e("WorkoutNotificationService", "Failed to update notification", e)
                }
            }
            ACTION_STOP -> {
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } catch (e: Exception) {
                    android.util.Log.e("WorkoutNotificationService", "Failed to stop foreground", e)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Активная тренировка",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Секундомер и таймер отдыха активной тренировки"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        fun formatSeconds(sec: Long): String {
            val m = sec / 60
            val s = sec % 60
            return "%02d:%02d".format(m, s)
        }

        val subText = if (isResting) {
            if (isCountdown) {
                if (remainingSeconds > 0) "⏳ Отдых: ${formatSeconds(remainingSeconds)}" else "🔔 Отдых завершён!"
            } else {
                "⏱ Отдых: ${formatSeconds(restSeconds)}"
            }
        } else {
            "Идет тренировка"
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🏋️‍♂️ $currentTitle")
            .setContentText(subText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)

        // Если не в режиме отдыха, включаем нативный непрерывный хронометр Android
        if (!isResting && currentStartTime > 0L) {
            builder.setUsesChronometer(true)
            builder.setWhen(currentStartTime)
            builder.setShowWhen(true)
        } else {
            builder.setUsesChronometer(false)
        }

        return builder.build()
    }
}
