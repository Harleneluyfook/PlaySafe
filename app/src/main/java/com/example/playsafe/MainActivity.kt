package com.example.playsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.widget.VideoView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlaySafeApp()
        }
    }
}

@Composable
fun PlaySafeApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(navController) }
        composable("safe_steps") { FeatureDashboardScreen(R.drawable.bg_safe_steps, navController) }
        composable("bubble_buddy") { FeatureDashboardScreen(R.drawable.bg_bubble_buddy, navController) }
        composable("toothy_time") { ToothyTimeScreen(navController) } // ✅ Updated
        composable("rescue_ring") { FeatureDashboardScreen(R.drawable.bg_rescue_ring, navController) }
        composable("menu") { FeatureDashboardScreen(R.drawable.bg_parent_dashboard, navController) }
        composable("avatar") { FeatureDashboardScreen(R.drawable.bg_avatar, navController) }
    }
}

@Composable
fun DashboardScreen(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.bg_dashboard),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = "Menu",
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { navController.navigate("menu") },
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_avatar),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { navController.navigate("avatar") },
                    contentScale = ContentScale.Fit
                )
            }

            // Logo
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "Play Safe Logo",
                modifier = Modifier
                    .height(200.dp)
                    .padding(vertical = 32.dp),
                contentScale = ContentScale.Fit
            )

            // Feature grid
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardIcon(
                        imageRes = R.drawable.ic_safe_steps,
                        title = "Safe Steps",
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("safe_steps") }

                    DashboardIcon(
                        imageRes = R.drawable.ic_bubble_buddy,
                        title = "Bubble Buddy",
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("bubble_buddy") }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DashboardIcon(
                        imageRes = R.drawable.ic_toothy_time,
                        title = "Toothy Time",
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("toothy_time") }

                    DashboardIcon(
                        imageRes = R.drawable.ic_rescue_ring,
                        title = "Rescue Ring",
                        modifier = Modifier.weight(1f)
                    ) { navController.navigate("rescue_ring") }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardIcon(
    imageRes: Int,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = title,
        modifier = modifier
            .height(180.dp)
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}

@Composable
fun FeatureDashboardScreen(bgImage: Int, navController: NavHostController? = null) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = bgImage),
            contentDescription = "Feature background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                modifier = Modifier
                    .size(90.dp)
                    .clickable { navController?.popBackStack() },
                contentScale = ContentScale.Fit
            )
        }
    }
}

/* ---------------- TOOTHY TIME FEATURE ---------------- */
@Composable
fun ToothyTimeScreen(navController: NavHostController? = null) {
    // 🎥 State
    var videoStarted by remember { mutableStateOf(false) }
    var jolting by remember { mutableStateOf(true) }

    // 🪥 Brush drag state
    var brushOffset by remember { mutableStateOf(Offset.Zero) }

    // 🔄 Jolt animation (bounce until touched)
    val infiniteTransition = rememberInfiniteTransition(label = "brushBounce")
    val joltScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )
    val scale = if (jolting) joltScale else 1f

    // 📊 Progress bar
    var progress by remember { mutableFloatStateOf(0f) }

    // Simulate 2 minutes brushing time
    LaunchedEffect(videoStarted) {
        if (videoStarted) {
            val totalDuration = 120_000L // 2 minutes
            val steps = 100
            val delayPerStep = totalDuration / steps
            repeat(steps) {
                progress = (it + 1) / steps.toFloat()
                delay(delayPerStep)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 🎬 Background video
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(
                        "android.resource://${context.packageName}/${R.raw.toothy_video}".toUri()
                    )
                    setOnPreparedListener { mp ->
                        mp.isLooping = false // don’t loop, stop after 2 mins
                    }
                }
            },
            update = { view ->
                if (videoStarted && !view.isPlaying) {
                    view.start()
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 🔙 Back button
        Image(
            painter = painterResource(id = R.drawable.ic_back_arrow),
            contentDescription = "Back",
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp)
                .clickable { navController?.popBackStack() },
            contentScale = ContentScale.Fit
        )

        // 📊 Progress bar (top center)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
        }

        // 🪥 Toothbrush (centered, draggable, jolting until touched)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_toothbrush),
                contentDescription = "Toothbrush",
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer(
                        translationX = brushOffset.x,
                        translationY = brushOffset.y,
                        scaleX = scale,
                        scaleY = scale
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                if (!videoStarted) {
                                    videoStarted = true
                                    jolting = false // stop jolting once touched
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                brushOffset += dragAmount
                            },
                            onDragEnd = {
                                // Snap back if dragged too far
                                val maxDistance = 400f
                                if (brushOffset.getDistance() > maxDistance) {
                                    brushOffset = Offset.Zero
                                }
                            }
                        )
                    }
            )
        }
    }
}

