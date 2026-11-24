package com.ivantorbey.pomodoro.watch.tile

import android.content.Context
import android.content.Intent
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.ivantorbey.pomodoro.watch.R
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import com.ivantorbey.pomodoro.watch.data.readSessionState
import com.ivantorbey.pomodoro.watch.presentation.MainActivity
import kotlinx.coroutines.runBlocking

private const val RESOURCES_VERSION = "1"

class MainTileService : TileService() {

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest
    ): ListenableFuture<TileBuilders.Tile> {
        val state = runBlocking { applicationContext.readSessionState() }
        return Futures.immediateFuture(buildTile(requestParams, applicationContext, state))
    }

    override fun onTileResourcesRequest(
        requestParams: ResourcesRequest
    ): ListenableFuture<ResourceBuilders.Resources> =
        Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
}

private fun buildTile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    state: SessionState,
): TileBuilders.Tile {
    val now = System.currentTimeMillis()
    val remainingMs = maxOf(0L, state.endTimeMs - now)
    val remainingMin = remainingMs / 60_000L

    val (mainLine, subLine, buttonLabel) = when (state.phase) {
        Phase.IDLE -> Triple(
            "🍅 ${state.workDurationMin} min",
            "Pomodoro",
            "Démarrer"
        )
        Phase.WORK -> Triple(
            "%02d min".format(remainingMin),
            "Travail",
            "Pause maintenant"
        )
        Phase.BREAK -> Triple(
            "%02d min".format(remainingMin),
            "Pause",
            null
        )
        Phase.BREAK_DONE -> Triple(
            "Pause ✓",
            "Prêt à reprendre",
            null
        )
    }

    val openIntent = ActionBuilders.LaunchAction.Builder()
        .setAndroidActivity(
            ActionBuilders.AndroidActivity.Builder()
                .setPackageName(context.packageName)
                .setClassName(MainActivity::class.java.name)
                .build()
        )
        .build()

    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        mainSlot = {
                            text(
                                mainLine.layoutString,
                                typography = Typography.DISPLAY_SMALL
                            )
                        },
                        labelSlot = {
                            text(subLine.layoutString, typography = Typography.LABEL_SMALL)
                        }
                    )
                }
            )
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(
    onTileResourceRequest = {
        ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build()
    }
) { req ->
    buildTile(req, context, SessionState())
}
