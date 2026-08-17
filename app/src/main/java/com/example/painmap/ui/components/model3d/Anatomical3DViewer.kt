package com.example.painmap.ui.components.model3d

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.example.painmap.domain.model.AnatomicalRegion
import com.example.painmap.domain.model.PainPoint
import com.example.painmap.ui.theme.TealLight
import com.example.painmap.ui.theme.TealPrimary
import org.json.JSONArray
import org.json.JSONObject

/**
 * Precision 3D Anatomical Pain Mapper with:
 * - Persistent Restored Painted Spots & Body Map Synchronization
 * - Pinch-To-Point Focal Centering & Double-Tap to Focus
 * - Dynamic Zoom-Invariant Precision Brush
 * - Continuous Drag Stroke Painting for Radiating Pain Bands
 */
@Composable
fun Anatomical3DViewer(
    activePainPoints: List<PainPoint>,
    selectedRegion: AnatomicalRegion?,
    toolMode: PaintToolMode,
    brushIntensity: Int,
    onPaintRegion: (AnatomicalRegion, Int, Float?, Float?, Float?, Float?, Float?) -> Unit,
    onEraseRegion: (AnatomicalRegion) -> Unit,
    onSelectRegion: (AnatomicalRegion) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentPreset by remember { mutableStateOf("front") }
    var currentInteractionMode by remember { mutableStateOf("ORBIT") } // "ORBIT" or "STROKE"
    var isModelReady by remember { mutableStateOf(false) }

    // Sync interaction mode with 3D engine
    LaunchedEffect(currentInteractionMode) {
        webViewRef?.evaluateJavascript("if (window.setInteractionMode) window.setInteractionMode('$currentInteractionMode');", null)
    }

    // Sync brush intensity with 3D engine
    LaunchedEffect(brushIntensity) {
        webViewRef?.evaluateJavascript("if (window.setBrushIntensity) window.setBrushIntensity($brushIntensity);", null)
    }

    // Sync & Restore active pain points onto the 3D canvas
    LaunchedEffect(activePainPoints, isModelReady, webViewRef) {
        if (isModelReady && webViewRef != null) {
            if (activePainPoints.isEmpty()) {
                webViewRef?.evaluateJavascript("if (window.clearAllPaint) window.clearAllPaint();", null)
            } else {
                val jsonArray = JSONArray().apply {
                    activePainPoints.forEach { pt ->
                        put(JSONObject().apply {
                            put("id", pt.id)
                            put("region", pt.region.name)
                            put("intensity", pt.intensity)
                            put("x", pt.x)
                            put("y", pt.y)
                            put("z", pt.z)
                            pt.uvX?.let { put("uvX", it) }
                            pt.uvY?.let { put("uvY", it) }
                        })
                    }
                }
                val escapedJson = jsonArray.toString()
                webViewRef?.evaluateJavascript("if (window.restorePaintedPoints) window.restorePaintedPoints($escapedJson);", null)
            }
        }
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
            // Top Bar: Model Header & Reset
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
                            text = "3D Precision Pain Mapper",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Double-tap to focus • Drag to rotate or paint",
                            style = MaterialTheme.typography.bodySmall,
                            color = TealPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
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

            Spacer(modifier = Modifier.height(6.dp))

            // Interaction Mode Toggle (Rotate & Tap vs Continuous Stroke)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentInteractionMode == "ORBIT",
                    onClick = { currentInteractionMode = "ORBIT" },
                    label = {
                        Text(
                            text = "🖐️ Rotate & Tap Spot",
                            fontSize = 12.sp,
                            fontWeight = if (currentInteractionMode == "ORBIT") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = TealPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = currentInteractionMode == "STROKE",
                    onClick = { currentInteractionMode = "STROKE" },
                    label = {
                        Text(
                            text = "🖌️ Continuous Stroke",
                            fontSize = 12.sp,
                            fontWeight = if (currentInteractionMode == "STROKE") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                        selectedLabelColor = TealPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick-Focus Region Chips (Horizontal Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickFocusChip("Full Body", isSelected = currentPreset == "front") {
                    currentPreset = "front"
                    webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('front');", null)
                }
                QuickFocusChip("Back View", isSelected = currentPreset == "back") {
                    currentPreset = "back"
                    webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('back');", null)
                }
                QuickFocusChip("Head & Neck", isSelected = currentPreset == "head_neck") {
                    currentPreset = "head_neck"
                    webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('head_neck');", null)
                }
                QuickFocusChip("Torso & Spine", isSelected = currentPreset == "torso_spine") {
                    currentPreset = "torso_spine"
                    webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('torso_spine');", null)
                }
                QuickFocusChip("Legs & Feet", isSelected = currentPreset == "legs_feet") {
                    currentPreset = "legs_feet"
                    webViewRef?.evaluateJavascript("if (window.setCameraPreset) window.setCameraPreset('legs_feet');", null)
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
                            onPaint = { regionName, intensity, uvX, uvY, x, y, z ->
                                val region = runCatching { AnatomicalRegion.valueOf(regionName) }.getOrNull()
                                if (region != null) {
                                    onPaintRegion(region, intensity, uvX, uvY, x, y, z)
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
                            },
                            onReady = {
                                isModelReady = true
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
private fun QuickFocusChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
            selectedLabelColor = TealPrimary
        ),
        modifier = Modifier.height(28.dp)
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun create3DWebView(
    context: Context,
    onPaint: (String, Int, Float?, Float?, Float?, Float?, Float?) -> Unit,
    onErase: (String) -> Unit,
    onSelect: (String) -> Unit,
    onReady: () -> Unit
): WebView {
    val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()

    return WebView(context).apply {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                android.util.Log.d("3DViewer", "${consoleMessage?.message()} [line: ${consoleMessage?.lineNumber()} in ${consoleMessage?.sourceId()}]")
                return true
            }
        }
        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request?.url ?: return null
                return assetLoader.shouldInterceptRequest(uri)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.evaluateJavascript("if (bodyMesh) window.AndroidBridge.onModelReady();", null)
            }
        }

        addJavascriptInterface(object {
            @JavascriptInterface
            fun onMeshPainted(
                regionName: String,
                intensity: Int,
                uvX: Double,
                uvY: Double,
                x: Double,
                y: Double,
                z: Double
            ) {
                post {
                    onPaint(
                        regionName,
                        intensity,
                        uvX.toFloat(),
                        uvY.toFloat(),
                        x.toFloat(),
                        y.toFloat(),
                        z.toFloat()
                    )
                }
            }

            @JavascriptInterface
            fun onMeshErased(regionName: String) {
                post { onErase(regionName) }
            }

            @JavascriptInterface
            fun onMeshSelected(regionName: String) {
                post { onSelect(regionName) }
            }

            @JavascriptInterface
            fun onModelReady() {
                post { onReady() }
            }
        }, "AndroidBridge")

        loadUrl("https://appassets.androidplatform.net/assets/viewer3d/index.html")
    }
}
