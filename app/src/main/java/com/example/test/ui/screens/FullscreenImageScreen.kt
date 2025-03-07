package com.example.test.ui.screens

import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FullscreenImageScreen(
    navController: NavHostController,
    imageData: List<Pair<String, String>>, // Pair berisi (imageUrl, title)
    startIndex: Int
) {
    val pagerState = rememberPagerState(initialPage = startIndex) {
        imageData.size
    }
    var swipeEnabled by remember { mutableStateOf(true) }
    var showUI by remember { mutableStateOf(true) } // Untuk mengontrol visibilitas AppBar & indikator

    BackHandler {
        navController.popBackStack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { showUI = !showUI } // Klik layar untuk toggle UI
    ) {
        if (imageData.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fill,
                userScrollEnabled = swipeEnabled // Aktif/nonaktifkan swipe
            ) { pageIndex ->
                DoubleTapZoom(
                    imageData[pageIndex].first,
                    onSwipeEnabled = { enabled -> swipeEnabled = enabled },
                    onToggleUI = { showUI = !showUI } // **Tambahkan toggle UI**
                )
            }

            // **App Bar yang bisa ditampilkan/sembunyikan**
            AnimatedVisibility(
                visible = showUI,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = { Text(imageData[pagerState.currentPage].second, color = Color.White,
                        maxLines = 1, // Batasi hanya 1 baris
                        overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(Color.Black.copy(alpha = 0.7f))
                )
            }

            AnimatedVisibility(
                visible = showUI && imageData.size > 1,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${imageData.size}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = "No images available",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}



@Composable
fun DoubleTapZoom(
    imageUrl: String,
    onSwipeEnabled: (Boolean) -> Unit,
    onToggleUI: () -> Unit // Untuk menampilkan/hide UI
) {
    var zoomed by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var zoomFloat by remember { mutableStateOf(1f) }

    val density = LocalDensity.current
    val imageSizePx = with(density) { 500.dp.toPx() }

    val minZoom = 1f
    val maxZoom = 4f // Batas Maksimal Zoom

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onToggleUI() }, // **Sembunyikan/tampilkan UI saat layar ditekan**
                    onDoubleTap = { tapOffset ->
                        if (zoomed) {
                            zoomFloat = minZoom
                            offset = Offset.Zero
                            zoomed = false
                            onSwipeEnabled(true)
                        } else {
                            val newZoom = 2f
                            val center = Offset(imageSizePx / 2, imageSizePx / 2)
                            val delta = (tapOffset - center) * (newZoom - 1)

                            offset = Offset(
                                delta.x.coerceIn(
                                    -(imageSizePx * (newZoom - 1) / 2),
                                    (imageSizePx * (newZoom - 1) / 2)
                                ),
                                delta.y.coerceIn(
                                    -(imageSizePx * (newZoom - 1) / 2),
                                    (imageSizePx * (newZoom - 1) / 2)
                                )
                            )

                            zoomFloat = newZoom
                            zoomed = true
                            onSwipeEnabled(false)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectCustomTransformGestures(
                    consume = false,
                    onGesture = { centroid, pan, gestureZoom, _, _, changes ->
                        val oldScale = zoomFloat
                        var newScale = zoomFloat * gestureZoom

                        if (newScale < minZoom) {
                            newScale = minZoom
                            offset = Offset.Zero
                            zoomed = false
                            onSwipeEnabled(true)
                        }
                        if (newScale > maxZoom) {
                            newScale = maxZoom
                        }

                        if (newScale > minZoom) {
                            val scaleFactor = ((newScale - 1f) / 3f + 1f)
                            val correctedPan = pan * scaleFactor

                            val newOffset = (offset + centroid / oldScale) -
                                    (centroid / newScale + correctedPan)

                            val maxOffsetX = ((imageSizePx * newScale) - imageSizePx) / 2
                            val maxOffsetY = ((imageSizePx * newScale) - imageSizePx) / 2

                            offset = Offset(
                                newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                            )
                        } else {
                            offset = Offset.Zero
                        }

                        zoomFloat = newScale

                        val isZoomed = zoomFloat > 1f
                        if (isZoomed != zoomed) {
                            zoomed = isZoomed
                            onSwipeEnabled(!zoomed)
                        }

                        if (changes.size > 1) {
                            changes.forEach { it.consume() }
                        }
                    }
                )
            }
            .graphicsLayer {
                scaleX = zoomFloat
                scaleY = zoomFloat
                translationX = if (zoomFloat > 1f) -offset.x else 0f
                translationY = if (zoomFloat > 1f) -offset.y else 0f
                rotationZ = 0f
                transformOrigin = TransformOrigin.Center
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier.size(500.dp)
        )
    }
}






fun Offset.rotateBy(degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble()) // Konversi derajat ke radian
    val cosTheta = cos(radians)
    val sinTheta = sin(radians)
    return Offset(
        (x * cosTheta - y * sinTheta).toFloat(),
        (x * sinTheta + y * cosTheta).toFloat()
    )
}



suspend fun PointerInputScope.detectCustomTransformGestures(
    panZoomLock: Boolean = false,
    consume: Boolean = true,
    pass: PointerEventPass = PointerEventPass.Main,
    onGestureStart: (PointerInputChange) -> Unit = {},
    onGesture: (
        centroid: Offset,
        pan: Offset,
        zoom: Float,
        rotation: Float,
        mainPointer: PointerInputChange,
        changes: List<PointerInputChange>
    ) -> Unit,
    onGestureEnd: (PointerInputChange) -> Unit = {}
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        // Wait for at least one pointer to press down, and set first contact position
        val down: PointerInputChange = awaitFirstDown(
            requireUnconsumed = false,
            pass = pass
        )
        onGestureStart(down)

        var pointer = down
        // Main pointer is the one that is down initially
        var pointerId = down.id

        do {
            val event = awaitPointerEvent(pass = pass)

            // If any position change is consumed from another PointerInputChange
            // or pointer count requirement is not fulfilled
            val canceled =
                event.changes.any { it.isConsumed }

            if (!canceled) {

                // Get pointer that is down, if first pointer is up
                // get another and use it if other pointers are also down
                // event.changes.first() doesn't return same order
                val pointerInputChange =
                    event.changes.firstOrNull { it.id == pointerId }
                        ?: event.changes.first()

                // Next time will check same pointer with this id
                pointerId = pointerInputChange.id
                pointer = pointerInputChange

                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion =
                        abs(rotation * kotlin.math.PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f ||
                        zoomChange != 1f ||
                        panChange != Offset.Zero
                    ) {
                        onGesture(
                            centroid,
                            panChange,
                            zoomChange,
                            effectiveRotation,
                            pointer,
                            event.changes
                        )
                    }

                    if (consume) {
                        event.changes.forEach {
                            if (it.positionChanged()) {
                                it.consume()
                            }
                        }
                    }
                }
            }
        } while (!canceled && event.changes.any { it.pressed })
        onGestureEnd(pointer)
    }
}





suspend fun animateToDefault(
    scale: Animatable<Float, AnimationVector1D>,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>
) {
    scale.animateTo(1f, animationSpec = tween(300))
    offsetX.animateTo(0f, animationSpec = tween(300))
    offsetY.animateTo(0f, animationSpec = tween(300))
}

suspend fun animateZoomIn(
    scale: Animatable<Float, AnimationVector1D>,
    offsetX: Animatable<Float, AnimationVector1D>,
    offsetY: Animatable<Float, AnimationVector1D>,
    tapOffset: Offset,
    imageWidth: Float,
    imageHeight: Float,
    screenWidth: Float,
    screenHeight: Float
) {
    val targetScale = 2f
    val currentScale = scale.value

    if (imageWidth == 0f || imageHeight == 0f) {
        scale.animateTo(targetScale, animationSpec = tween(300))
        return
    }

    val imageLeft = (screenWidth - imageWidth) / 2
    val imageTop = (screenHeight - imageHeight) / 2
    val tapXRelativeToImageCenter = tapOffset.x - imageLeft - imageWidth / 2
    val tapYRelativeToImageCenter = tapOffset.y - imageTop - imageHeight / 2

    val newOffsetX = -tapXRelativeToImageCenter * (targetScale - 1)
    val newOffsetY = -tapYRelativeToImageCenter * (targetScale - 1)

    scale.animateTo(targetScale, animationSpec = tween(300))
    offsetX.animateTo(newOffsetX, animationSpec = tween(300))
    offsetY.animateTo(newOffsetY, animationSpec = tween(300))
}




