package com.ivantorbey.pomodoro.watch.complication

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.MonochromaticImageComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.SmallImage
import androidx.wear.watchface.complications.data.SmallImageComplicationData
import androidx.wear.watchface.complications.data.SmallImageType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.ivantorbey.pomodoro.watch.R
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import com.ivantorbey.pomodoro.watch.data.readSessionState
import com.ivantorbey.pomodoro.watch.presentation.MainActivity

class MainComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        buildComplicationData(type, SessionState(), preview = true)

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        val state = applicationContext.readSessionState()
        return buildComplicationData(request.complicationType, state, preview = false)
    }

    private fun buildComplicationData(
        type: ComplicationType,
        state: SessionState,
        preview: Boolean,
    ): ComplicationData? {
        val tap = if (preview) null else openPendingIntent()
        val now = System.currentTimeMillis()
        val remainingMs = maxOf(0L, state.endTimeMs - now)
        val remainingMin = (remainingMs / 60_000L).toInt()
        val totalMin = state.effectiveWorkDurationMin()

        val shortText = when (state.phase) {
            Phase.IDLE -> "${state.workDurationMin}"
            Phase.WORK -> "${remainingMin}m"
            Phase.BREAK -> "Pause"
            Phase.BREAK_DONE -> "Pause ✓"
        }
        val longText = when (state.phase) {
            Phase.IDLE -> "🍅 ${state.workDurationMin} min"
            Phase.WORK -> "Travail ${remainingMin}m"
            Phase.BREAK -> "Pause ${remainingMin}m"
            Phase.BREAK_DONE -> "Pause terminée"
        }
        val rangedValue = when (state.phase) {
            Phase.IDLE -> 0f
            Phase.WORK -> (totalMin - remainingMin).toFloat().coerceIn(0f, totalMin.toFloat())
            Phase.BREAK, Phase.BREAK_DONE -> totalMin.toFloat()
        }
        val rangedMax = totalMin.toFloat().coerceAtLeast(1f)

        val plainShort = PlainComplicationText.Builder(shortText).build()
        val plainLong = PlainComplicationText.Builder(longText).build()
        val plainDesc = PlainComplicationText.Builder("Pomodoro").build()
        val nativeIcon = Icon.createWithResource(applicationContext, R.drawable.ic_tomato)
        val monoImage = MonochromaticImage.Builder(nativeIcon).build()
        val smallImage = SmallImage.Builder(nativeIcon, SmallImageType.ICON).build()

        return when (type) {
            ComplicationType.SHORT_TEXT ->
                ShortTextComplicationData.Builder(plainShort, plainDesc)
                    .setMonochromaticImage(monoImage)
                    .apply { tap?.let { setTapAction(it) } }
                    .build()

            ComplicationType.LONG_TEXT ->
                LongTextComplicationData.Builder(plainLong, plainDesc)
                    .setMonochromaticImage(monoImage)
                    .apply { tap?.let { setTapAction(it) } }
                    .build()

            ComplicationType.RANGED_VALUE ->
                RangedValueComplicationData.Builder(
                    value = rangedValue,
                    min = 0f,
                    max = rangedMax,
                    contentDescription = plainDesc
                )
                    .setText(plainShort)
                    .setMonochromaticImage(monoImage)
                    .apply { tap?.let { setTapAction(it) } }
                    .build()

            ComplicationType.MONOCHROMATIC_IMAGE ->
                MonochromaticImageComplicationData.Builder(monoImage, plainDesc)
                    .apply { tap?.let { setTapAction(it) } }
                    .build()

            ComplicationType.SMALL_IMAGE ->
                SmallImageComplicationData.Builder(smallImage, plainDesc)
                    .apply { tap?.let { setTapAction(it) } }
                    .build()

            else -> null
        }
    }

    private fun openPendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
