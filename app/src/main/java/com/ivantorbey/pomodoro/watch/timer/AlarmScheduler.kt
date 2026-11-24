package com.ivantorbey.pomodoro.watch.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {

    const val ACTION_PHASE_TRANSITION = "com.ivantorbey.pomodoro.watch.ACTION_PHASE_TRANSITION"
    private const val REQUEST_CODE = 1001

    fun schedulePhaseTransition(context: Context, triggerAtMs: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context)
        // USE_EXACT_ALARM déclaré dans le manifest ; fallback setAndAllowWhileIdle si refusé.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }

    fun cancelAlarm(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent {
        // Intent EXPLICITE : component direct → plus fiable que l'intent implicite + setPackage.
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_PHASE_TRANSITION
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
