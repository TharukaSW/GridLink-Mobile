package com.example.gridlink.app.ui.screens.prosumer

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.remote.MicrogridNodeDto
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.data.repository.StationRepository
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.CyanElectric
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate800
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

@Composable
fun StationMapScreen(
    onNavigateBack: () -> Unit,
    onSelectStationForBooking: (stationId: String) -> Unit
) {
    val context = LocalContext.current
    val stationRepo = remember { StationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Reference center coordinates (Colombo, Sri Lanka - matching Web frontend config)
    val defaultCenterLat = 6.9271
    val defaultCenterLng = 79.8612

    var radiusKm by remember { mutableDoubleStateOf(50.0) }
    var stations by remember { mutableStateOf<List<MicrogridNodeDto>>(emptyList()) }
    var selectedStation by remember { mutableStateOf<MicrogridNodeDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isListView by remember { mutableStateOf(false) }

    fun fetchNearby() {
        scope.launch {
            isLoading = true
            when (val res = stationRepo.getNearbyStations(defaultCenterLat, defaultCenterLng, radiusKm, activeOnly = false)) {
                is Resource.Success -> {
                    stations = res.data ?: emptyList()
                    if (selectedStation == null && stations.isNotEmpty()) {
                        selectedStation = stations.firstOrNull()
                    }
                }
                is Resource.Error -> {
                    // Fallback to all stations if nearby filter has error
                    val allRes = stationRepo.getAllStations()
                    if (allRes is Resource.Success) {
                        stations = allRes.data ?: emptyList()
                        if (selectedStation == null && stations.isNotEmpty()) {
                            selectedStation = stations.firstOrNull()
                        }
                    } else {
                        snackbarHostState.showSnackbar(res.message ?: "Failed to load solar stations.")
                    }
                }
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(radiusKm) {
        fetchNearby()
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Solar Microgrid Map",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showLogout = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Controls: Radius Filter & View Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(15.0 to "15km", 30.0 to "30km", 50.0 to "50km", 100.0 to "100km").forEach { (r, label) ->
                        FilterChip(
                            selected = radiusKm == r,
                            onClick = { radiusKm = r },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Row {
                    IconButton(onClick = { isListView = !isListView }) {
                        Icon(
                            imageVector = if (isListView) Icons.Default.Map else Icons.Default.List,
                            contentDescription = "Toggle View",
                            tint = GreenPrimary
                        )
                    }
                    IconButton(onClick = { fetchNearby() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GreenPrimary)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (isListView) {
                // List View Mode
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stations) { station ->
                        StationCardItem(
                            station = station,
                            isSelected = selectedStation?.id == station.id,
                            onSelect = { selectedStation = station },
                            onBook = { onSelectStationForBooking(station.id) }
                        )
                    }
                }
            } else {
                // Interactive Map Canvas Mode
                Box(modifier = Modifier.weight(1f)) {
                    StationsInteractiveMap(
                        stations = stations,
                        selectedStation = selectedStation,
                        centerLat = defaultCenterLat,
                        centerLng = defaultCenterLng,
                        radiusKm = radiusKm,
                        onStationSelected = { selectedStation = it }
                    )

                    // Reference coordinates badge
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = GreenLight, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Center: 6.927°N, 79.861°E | ${stations.size} hubs", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                // Selected Station Details Card
                if (selectedStation != null) {
                    val s = selectedStation!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${s.stationCode} • ${s.address}", color = Color.Gray, fontSize = 12.sp)
                                }
                                StatusBadge(status = if (s.isActive) "Active" else "Inactive")
                            }

                            Spacer(Modifier.height(10.dp))
                            Divider()
                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${s.capacityKWh} kW/h", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("${s.availableBatterySlots} / ${s.totalBatterySlots} Slots", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = CyanElectric, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(s.operatingHours, fontSize = 12.sp)
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = { onSelectStationForBooking(s.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Book Energy Slot Here", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StationsInteractiveMap(
    stations: List<MicrogridNodeDto>,
    selectedStation: MicrogridNodeDto?,
    centerLat: Double,
    centerLng: Double,
    radiusKm: Double,
    onStationSelected: (MicrogridNodeDto) -> Unit
) {
    val stationsMap = remember(stations) { stations.associateBy { it.id } }

    val stationsJson = remember(stations, selectedStation) {
        val array = JSONArray()
        stations.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.stationName)
            obj.put("code", s.stationCode)
            obj.put("lat", s.latitude)
            obj.put("lng", s.longitude)
            obj.put("slots", "${s.availableBatterySlots}/${s.totalBatterySlots}")
            obj.put("capacity", "${s.capacityKWh} kWh")
            obj.put("isSelected", selectedStation?.id == s.id)
            array.put(obj)
        }
        array.toString()
    }

    val htmlContent = remember(centerLat, centerLng, radiusKm, stationsJson) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                * { box-sizing: border-box; }
                html, body {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background: #0f172a;
                    overflow: hidden;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                }
                #map {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: #0f172a;
                }
                .station-pin {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 36px;
                    height: 36px;
                    border-radius: 50%;
                    border: 2.5px solid #ffffff;
                    box-shadow: 0 4px 14px rgba(0,0,0,0.55);
                    font-size: 17px;
                    cursor: pointer;
                    transition: transform 0.2s ease;
                }
                .station-pin.active {
                    background: #10b981;
                    box-shadow: 0 0 16px rgba(16, 185, 129, 0.9);
                }
                .station-pin.selected {
                    background: #f59e0b;
                    box-shadow: 0 0 22px rgba(245, 158, 11, 1.0);
                    transform: scale(1.2);
                }
                .center-pin {
                    width: 22px;
                    height: 22px;
                    border-radius: 50%;
                    background: #06b6d4;
                    border: 3.5px solid #ffffff;
                    box-shadow: 0 0 18px #06b6d4;
                }
                .leaflet-popup-content-wrapper {
                    background: #1e293b;
                    color: #f8fafc;
                    border-radius: 12px;
                    box-shadow: 0 8px 24px rgba(0,0,0,0.6);
                    border: 1px solid rgba(255,255,255,0.18);
                }
                .leaflet-popup-content {
                    margin: 10px 14px;
                    font-size: 13px;
                    line-height: 1.4;
                }
                .leaflet-popup-tip {
                    background: #1e293b;
                }
                .popup-title {
                    font-weight: 700;
                    color: #10b981;
                    font-size: 14px;
                    margin-bottom: 4px;
                }
                .popup-stat {
                    color: #cbd5e1;
                    font-size: 12px;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var mapInstance = null;
                var markersGroup = null;
                var circleLayer = null;

                function adjustDimensions() {
                    var el = document.getElementById('map');
                    if (el) {
                        el.style.height = (window.innerHeight || document.documentElement.clientHeight || 500) + 'px';
                        el.style.width = (window.innerWidth || document.documentElement.clientWidth || 360) + 'px';
                    }
                }

                function initMap() {
                    if (typeof L === 'undefined') {
                        setTimeout(initMap, 60);
                        return;
                    }

                    adjustDimensions();

                    if (!mapInstance) {
                        mapInstance = L.map('map', {
                            zoomControl: false,
                            attributionControl: false
                        }).setView([$centerLat, $centerLng], 12);

                        L.control.zoom({ position: 'bottomright' }).addTo(mapInstance);

                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19
                        }).addTo(mapInstance);

                        markersGroup = L.featureGroup().addTo(mapInstance);
                    }

                    renderLayers($centerLat, $centerLng, ${radiusKm * 1000}, $stationsJson);

                    setTimeout(function() {
                        adjustDimensions();
                        if (mapInstance) mapInstance.invalidateSize();
                    }, 250);
                }

                function renderLayers(cLat, cLng, radiusM, stations) {
                    if (!mapInstance) return;

                    if (circleLayer) {
                        mapInstance.removeLayer(circleLayer);
                    }
                    if (markersGroup) {
                        markersGroup.clearLayers();
                    }

                    // Proximity radius circle
                    circleLayer = L.circle([cLat, cLng], {
                        radius: radiusM,
                        color: '#06b6d4',
                        fillColor: '#06b6d4',
                        fillOpacity: 0.1,
                        weight: 2,
                        dashArray: '5, 8'
                    }).addTo(mapInstance);

                    // User Center Pin
                    var centerIcon = L.divIcon({
                        className: 'center-pin-container',
                        html: '<div class="center-pin"></div>',
                        iconSize: [22, 22],
                        iconAnchor: [11, 11]
                    });
                    L.marker([cLat, cLng], { icon: centerIcon })
                        .addTo(markersGroup)
                        .bindPopup('<div class="popup-title">Current Hub Location</div><div class="popup-stat">Colombo Central • ' + cLat + '°N, ' + cLng + '°E</div>');

                    // Station Markers
                    stations.forEach(function(s) {
                        var pinClass = s.isSelected ? 'station-pin selected' : 'station-pin active';
                        var icon = L.divIcon({
                            className: 'station-icon-wrapper',
                            html: '<div class="' + pinClass + '">⚡</div>',
                            iconSize: [36, 36],
                            iconAnchor: [18, 18]
                        });

                        var marker = L.marker([s.lat, s.lng], { icon: icon }).addTo(markersGroup);
                        marker.bindPopup(
                            '<div class="popup-title">' + s.name + ' (' + s.code + ')</div>' +
                            '<div class="popup-stat">🔋 Available Slots: <b>' + s.slots + '</b></div>' +
                            '<div class="popup-stat">⚡ Capacity: <b>' + s.capacity + '</b></div>'
                        );

                        marker.on('click', function() {
                            if (window.AndroidBridge && window.AndroidBridge.onStationClicked) {
                                window.AndroidBridge.onStationClicked(s.id);
                            }
                        });

                        if (s.isSelected) {
                            marker.openPopup();
                        }
                    });
                }

                window.updateMapData = function(cLat, cLng, radiusM, stations) {
                    renderLayers(cLat, cLng, radiusM, stations);
                };

                window.addEventListener('resize', function() {
                    adjustDimensions();
                    if (mapInstance) mapInstance.invalidateSize();
                });

                if (document.readyState === 'complete') {
                    initMap();
                } else {
                    window.addEventListener('load', initMap);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    class WebAppInterface {
        @JavascriptInterface
        fun onStationClicked(stationId: String) {
            Handler(Looper.getMainLooper()).post {
                stationsMap[stationId]?.let {
                    onStationSelected(it)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    settings.userAgentString = "Mozilla/5.0 (Linux; Android 15; Pixel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 GridLinkApp/1.0"
                    addJavascriptInterface(WebAppInterface(), "AndroidBridge")
                    loadDataWithBaseURL("https://tile.openstreetmap.org", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                val currentKey = "$centerLat,$centerLng,$radiusKm,$stationsJson"
                if (webView.tag != currentKey) {
                    webView.tag = currentKey
                    webView.evaluateJavascript(
                        "if (window.updateMapData) { window.updateMapData($centerLat, $centerLng, ${radiusKm * 1000}, $stationsJson); } else { location.reload(); }",
                        null
                    )
                }
            }
        )
    }
}

@Composable
fun StationCardItem(
    station: MicrogridNodeDto,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(station.stationName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${station.stationCode} • ${station.address}", color = Color.Gray, fontSize = 12.sp)
                }
                StatusBadge(status = if (station.isActive) "Active" else "Inactive")
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Capacity: ${station.capacityKWh} kW/h", fontSize = 12.sp, color = GreenPrimary)
                Text("Slots: ${station.availableBatterySlots}/${station.totalBatterySlots}", fontSize = 12.sp, color = SolarAmber)
                Text(station.operatingHours, fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("Select & Book Energy Slot")
            }
        }
    }
}
