package com.ivantorbey.pomodoro.watch.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.ivantorbey.pomodoro.watch.R
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import com.ivantorbey.pomodoro.watch.presentation.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "pomodoro_session"
    const val NOTIF_ID_ONGOING = 1
    const val NOTIF_ID_FULLSCREEN = 2

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setShowBadge(false)
            enableVibration(false)
        }
        nm.createNotificationChannel(channel)
    }

    /** Notification permanente (Ongoing Activity) visible depuis le cadran. */
    fun showOngoingNotification(context: Context, state: SessionState) {
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = openMainActivityPendingIntent(context)

        val title = when (state.phase) {
            Phase.WORK -> context.getString(R.string.notif_title_work)
            Phase.BREAK -> context.getString(R.string.notif_title_break)
            Phase.BREAK_DONE -> context.getString(R.string.notif_title_break_done)
            Phase.IDLE -> return
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tomato)
            .setContentTitle(title)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setContentIntent(openIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val status = Status.Builder().addTemplate(title).build()

        val ongoingActivity = OngoingActivity.Builder(context, NOTIF_ID_ONGOING, builder)
            .setStaticIcon(R.drawable.ic_tomato)
            .setStatus(status)
            .build()
        ongoingActivity.apply(context)

        nm.notify(NOTIF_ID_ONGOING, builder.build())
    }

    /** Notification plein écran pour réveil à la transition vers BREAK. */
    fun showBreakStartNotification(context: Context, state: SessionState) {
        ensureChannel(context)
        showOngoingNotification(context, state)
        showFullScreenNotification(context, context.getString(R.string.notif_title_break))
    }

    /** Notification plein écran pour réveil à la fin de la pause. */
    fun showBreakDoneNotification(context: Context, state: SessionState) {
        ensureChannel(context)
        showOngoingNotification(context, state)
        showFullScreenNotification(context, context.getString(R.string.notif_title_break_done))
    }

    fun cancelAll(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancelAll()
    }

    private fun showFullScreenNotification(context: Context, title: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = openMainActivityPendingIntent(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tomato)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(openIntent, true)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID_FULLSCREEN, notif)
    }

    private fun openMainActivityPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
