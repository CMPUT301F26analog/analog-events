package com.example.birchbark.ui.owner

// See #TBD7 in LLM.md

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.birchbark.util.MapUtils
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.File

/**
 * US 02.01.02 (map location). A map of a campsite with its camp pin, using
 * OpenStreetMap through osmdroid (approved by our TA, free and no API key).
 *
 * If [onPick] is given, the owner can tap anywhere on the map to drop the pin
 * there, which is how the register and update forms set the location. Without
 * it the map just shows the pin, like on the details screen.
 *
 * osmdroid's map is an old style Android View, not Compose, so it's wrapped
 * in an AndroidView. It's clipped to its own box, since otherwise it can draw
 * over the text above it when the screen scrolls.
 *
 * OpenStreetMap's tile servers block apps that don't name themselves, and they
 * block package names starting with com.example. osmdroid's built in
 * OpenStreetMap style sends a name made from our package name, so the map uses
 * [OSM_TILES] instead, which sends our own app name ([USER_AGENT]). Their
 * rules also say the map has to credit "OpenStreetMap contributors", which is
 * the small text in the corner.
 *
 * Map pieces (tiles) are saved in the app's cache folder so they don't have to
 * download every time, and clearing the app's cache removes them.
 *
 * Outstanding issues. The map needs internet to load. With no internet the
 * pin still shows but the map behind it is blank.
 *
 * @param latitude the pin's latitude, or null if there's no pin yet.
 * @param longitude the pin's longitude, or null if there's no pin yet.
 * @param modifier size and tags from the caller.
 * @param onPick called with the latitude and longitude the owner tapped, or null to only show the pin.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun CampsiteMap(
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
    onPick: ((Double, Double) -> Unit)? = null
) {
    val context = LocalContext.current
    //so a tap always uses the newest onPick, even though the map is only made once
    val currentOnPick by rememberUpdatedState(onPick)

    val mapView = remember {
        //osmdroid asks every app to load its settings and give its name before downloading map tiles
        // See #TBD8 in LLM.md
        val settings = Configuration.getInstance()
        settings.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        settings.userAgentValue = USER_AGENT
        //keep the saved map tiles in the app's cache folder
        settings.osmdroidTileCache = File(context.cacheDir, "map-tiles")

        MapView(context).apply {
            setTileSource(OSM_TILES)
            setMultiTouchControls(true)
            //"© OpenStreetMap contributors" in the corner, required by OpenStreetMap
            overlays.add(CopyrightOverlay(context))

            //start on the pin if there is one, otherwise on Edmonton
            if (MapUtils.isValidLocation(latitude, longitude)) {
                controller.setZoom(MapUtils.PIN_ZOOM)
                controller.setCenter(GeoPoint(latitude!!, longitude!!))
            } else {
                controller.setZoom(MapUtils.DEFAULT_ZOOM)
                controller.setCenter(GeoPoint(MapUtils.DEFAULT_LATITUDE, MapUtils.DEFAULT_LONGITUDE))
            }

            //the forms scroll up and down, so stop them from stealing the finger when the owner drags the map
            setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                }
                false
            }

            if (onPick != null) {
                overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(point: GeoPoint): Boolean {
                        currentOnPick?.invoke(point.latitude, point.longitude)
                        return true
                    }

                    override fun longPressHelper(point: GeoPoint): Boolean {
                        return false
                    }
                }))
            }
        }
    }

    //start and stop the map with the screen so it doesn't keep downloading in the background
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        //keep the map inside its own box so it doesn't draw over the text above it
        // See #TBD9 in LLM.md
        modifier = modifier.clipToBounds(),
        update = { map ->
            //redraw the pin every time the location changes
            map.overlays.removeAll { it is Marker }
            if (MapUtils.isValidLocation(latitude, longitude)) {
                val pin = Marker(map)
                pin.position = GeoPoint(latitude!!, longitude!!)
                pin.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                pin.title = "Campsite"
                map.overlays.add(pin)
            }
            map.invalidate()
        }
    )
}

/**
 * The name the map gives OpenStreetMap when it downloads map tiles, so they
 * know which app is asking. Can't start with com.example or it gets blocked.
 */
// See #TBD8 in LLM.md
private const val USER_AGENT = "BirchBark/1.0 (CMPUT 301 student project, University of Alberta)"

/**
 * The normal OpenStreetMap map, same as osmdroid's built in one, but it sends
 * our own [USER_AGENT] instead of one made from the package name. Follows
 * OpenStreetMap's rules of at most 2 downloads at a time and no bulk downloading.
 */
private val OSM_TILES = XYTileSource(
    "BirchBarkOSM",
    0,
    19,
    256,
    ".png",
    arrayOf("https://tile.openstreetmap.org/"),
    "© OpenStreetMap contributors",
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_NO_BULK or
                TileSourcePolicy.FLAG_NO_PREVENTIVE or
                TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
    )
)