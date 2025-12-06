package com.ltcn272.finny.presentation.features.transation.transaction_detail.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun StaticMap(
    lat: Double,
    lng: Double,
    modifier: Modifier = Modifier
) {
    val url = remember(lng, lat) { buildStaticMapWithPointUrl(lat = lat, lon = lng, width = 1280, height = 720, retina = true) }

    AsyncImage(
        model = url,
        contentDescription = "Location",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}

fun buildStaticMapWithPointUrl(
    lat: Double,
    lon: Double,
    zoom: Double = 16.0,
    width: Int = 1280,
    height: Int = 720,
    retina: Boolean = false,
    showLogo: Boolean = false,
    showAttribution: Boolean = false
): String {

    val accessToken =
        "pk.eyJ1IjoiZHVuZ2x0Y24yNzIiLCJhIjoiY21pbm5waGJzMHY1bzNmcHVydzFyM3hoeCJ9.UBkm7Yug8vLDUuhTrscS-w"

    val geoJson = """{"type":"Point","coordinates":[$lon,$lat]}"""

    val encodedGeoJson = URLEncoder.encode(
        geoJson,
        StandardCharsets.UTF_8.toString()
    )

    val size = if (retina) "${width}x${height}@2x" else "${width}x${height}"

    val logoParam = if (!showLogo) "&logo=false" else ""
    val attributionParam = if (!showAttribution) "&attribution=false" else ""

    return "https://api.mapbox.com/styles/v1/mapbox/streets-v12/static/" +
            "geojson($encodedGeoJson)/" +
            "$lon,$lat,$zoom/" +
            size +
            "?access_token=$accessToken$logoParam$attributionParam"
}
