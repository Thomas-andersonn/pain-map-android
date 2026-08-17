package com.example.painmap.ui.components.model3d

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Hardware-Accelerated 3D GLB Mesh Viewer rendering the authentic Z-Anatomy 3D Medical Model
 * with real-time PBR lighting, 360-degree touch orbit, pinch-zoom, and precision mesh paint highlighting.
 */
@Composable
fun Anatomical3DViewer(
    activePainPoints: List<PainPoint>,
    selectedRegion: AnatomicalRegion?,
    toolMode: PaintToolMode,
    brushIntensity: Int,
    onPaintRegion: (AnatomicalRegion, Int) -> Unit,
    onEraseRegion: (AnatomicalRegion) -> Unit,
    onSelectRegion: (AnatomicalRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentPreset by remember { mutableStateOf("front") }

    // Sync tool mode with 3D engine
    LaunchedEffect(toolMode) {
        val modeStr = when (toolMode) {
            PaintToolMode.ROTATE -> "ROTATE"
            PaintToolMode.PAINT -> "PAINT"
            PaintToolMode.ERASE -> "ERASE"
        }
        webViewRef?.evaluateJavascript("if (window.setToolMode) window.setToolMode('$modeStr');", null)
    }

    // Sync brush intensity with 3D engine
    LaunchedEffect(brushIntensity) {
        webViewRef?.evaluateJavascript("if (window.setBrushIntensity) window.setBrushIntensity($brushIntensity);", null)
    }

    // Sync active pain points with 3D engine
    LaunchedEffect(activePainPoints) {
        val jsonString = Json.encodeToString(
            activePainPoints.map {
                PainPointJsonPayload(
                    region = it.region.name,
                    intensity = it.intensity
                )
            }
        )
        webViewRef?.evaluateJavascript("if (window.syncPainPoints) window.syncPainPoints('$jsonString');", null)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Top Bar: 3D Model Status & Camera Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = TealLight,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Z-Anatomy 3D Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "3D Musculoskeletal Mesh (PBR)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    QuickViewButton("Front", isSelected = currentPreset == "front") {
                        currentPreset = "front"
                        webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('front');", null)
                    }
                    QuickViewButton("Back", isSelected = currentPreset == "back") {
                        currentPreset = "back"
                        webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('back');", null)
                    }
                    IconButton(
                        onClick = {
                            currentPreset = "front"
                            webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('reset');", null)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset View",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hardware-Accelerated 3D WebGL Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        create3DWebView(
                            context = ctx,
                            onPaint = { regionName, intensity ->
                                val region = runCatching { AnatomicalRegion.valueOf(regionName) }.getOrNull()
                                if (region != null) {
                                    onPaintRegion(region, intensity)
                                }
                            },
                            onErase = { regionName ->
                                val region = runCatching { AnatomicalRegion.valueOf(regionName) }.getOrNull()
                                if (region != null) {
                                    onEraseRegion(region)
                                }
                            },
                            onSelect = { regionName ->
                                val region = runCatching { AnatomicalRegion.valueOf(regionName) }.getOrNull()
                                if (region != null) {
                                    onSelectRegion(region)
                                }
                            }
                        ).also { webViewRef = it }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun QuickViewButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 11.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
            selectedLabelColor = TealPrimary
        ),
        modifier = Modifier.height(28.dp)
    )
}

@kotlinx.serialization.Serializable
private data class PainPointJsonPayload(
    val region: String,
    val intensity: Int
)

@SuppressLint("SetJavaScriptEnabled")
private fun create3DWebView(
    context: Context,
    onPaint: (String, Int) -> Unit,
    onErase: (String) -> Unit,
    onSelect: (String) -> Unit
): WebView {
    return WebView(context).apply {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                if (url.endsWith(".glb") || url.contains("models/")) {
                    val filename = if (url.contains("z_anatomy_fullbody")) {
                        "models/z_anatomy_fullbody.glb"
                    } else {
                        "models/human_body.glb"
                    }
                    return try {
                        val inputStream = context.assets.open(filename)
                        WebResourceResponse("model/gltf-binary", "UTF-8", inputStream).apply {
                            responseHeaders = mapOf(
                                "Access-Control-Allow-Origin" to "*",
                                "Access-Control-Allow-Methods" to "GET, OPTIONS",
                                "Access-Control-Allow-Headers" to "*"
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        addJavascriptInterface(
            object {
                @JavascriptInterface
                fun onMeshPainted(region: String, intensity: Int) {
                    post { onPaint(region, intensity) }
                }

                @JavascriptInterface
                fun onMeshErased(region: String) {
                    post { onErase(region) }
                }

                @JavascriptInterface
                fun onMeshSelected(region: String) {
                    post { onSelect(region) }
                }
            },
            "AndroidBridge"
        )
        loadUrl("file:///android_asset/viewer3d/index.html")
    }
}
