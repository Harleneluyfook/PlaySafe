package com.example.playsafe

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID


// Feature state and ViewModel
data class FeatureState(
    val playCount: Int = 0,
    val completed: Boolean = false
)

class FeatureViewModel : ViewModel() {
    // mutableStateListOf so Compose observes changes
    var features = mutableStateListOf(
        FeatureState(), FeatureState(), FeatureState(), FeatureState()
    )

    // call this when a feature finishes normally
    fun markCompleted(index: Int) {
        if (index in features.indices) {
            val f = features[index]
            features[index] = f.copy(playCount = f.playCount + 1, completed = true)
        }
    }

}

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
    val featureViewModel: FeatureViewModel = viewModel() // single instance for all screens

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") { DashboardScreen(navController) }

        composable("safe_steps") {
            SafeStepsScreen(
                navController = navController,
                featureIndex = 0,
                featureViewModel = featureViewModel
            )
        }

        composable("toothy_time") {
            ToothyTimeScreen(
                navController = navController,
                featureIndex = 1,
                featureViewModel = featureViewModel
            )
        }

        composable("bubble_buddy") {
            BubbleBuddyScreen(
                navController = navController,
                featureIndex = 2,
                featureViewModel = featureViewModel
            )
        }

        composable("rescue_ring") {
            RescueDialScreen(
                navController = navController,
                featureIndex = 3,
                featureViewModel = featureViewModel
            )
        }

        composable("menu") {
            ParentDashboardScreen(
                featureViewModel = featureViewModel,
                navController = navController
            )
        }

        composable("avatar") { AvatarScreen(navController) }
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

        // Make everything scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // ✅ Added scroll
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

/* ---------------- BUBBLE BUDDY FEATURE ---------------- */
@Composable
fun ToothyTimeScreen(
    navController: NavHostController,
    featureIndex: Int,
    featureViewModel: FeatureViewModel
) {
    val context = LocalContext.current

    // 🎬 States
    var showStartOverlay by remember { mutableStateOf(true) }
    var videoStarted by remember { mutableStateOf(false) }
    var brushOffset by remember { mutableStateOf(Offset.Zero) }
    var brushingProgress by remember { mutableFloatStateOf(0f) }
    var currentTexts by remember { mutableStateOf(listOf("")) }
    var isDragging by remember { mutableStateOf(false) }

    // NEW: Separate state to track completion
    var gameCompleted by remember { mutableStateOf(false) }

    // 🎵 Intro audio
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.toothy_start).apply { isLooping = false }
    }

    LaunchedEffect(showStartOverlay) {
        if (showStartOverlay) {
            mediaPlayer.seekTo(0)
            mediaPlayer.start()
        } else if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose { try { mediaPlayer.release() } catch (_: Exception) {} }
    }

    // 🦷 Timeline
    val brushingTimeline = listOf(
        BrushingStep(0, 5, listOf("Brush your front teeth in a forward and backward motion - show me your smile!"), false),
        BrushingStep(5, 12, listOf("Good Job!"), true),
        BrushingStep(12, 19, listOf("Next, brush your upper front teeth in a circular motion!"), false),
        BrushingStep(19, 23, listOf("Keep Brushing!"), true),
        BrushingStep(23, 28, listOf("Now do your lower front teeth! Circle Circle!"), false),
        BrushingStep(28, 32, listOf("Great Work!"), true),
        BrushingStep(32, 37, listOf("Brush the left side of your teeth in an up and down motion!"), false),
        BrushingStep(37, 41, listOf("Keep Brushing!"), true),
        BrushingStep(41, 46, listOf("Now brush the right side of your teeth! Up and Down!"), false),
        BrushingStep(46, 52, listOf("Great Work!"), true),
        BrushingStep(52, 57, listOf("Brush the chewing surface of your lower teeth! Let's start with the right side!"), false),
        BrushingStep(57, 62, listOf("Keep Brushing!"), true),
        BrushingStep(62, 68, listOf("Do the other side! Brush your left!"), false),
        BrushingStep(68, 73, listOf("Good Job!"), true),
        BrushingStep(73, 80, listOf("Brush the chewing surface of your upper teeth next! Start with the left side!"), false),
        BrushingStep(80, 86, listOf("You can do it!"), true),
        BrushingStep(86, 93, listOf("Do the other side! Brush your right!"), false),
        BrushingStep(93, 99, listOf("Nice Work!"), true),
        BrushingStep(99, 106, listOf("Don't forget to brush your tongue! Brush it up and down!"), false),
        BrushingStep(106, 112, listOf("Keep Brushing!"), true),
        BrushingStep(113, 120, listOf("Lastly, rinse out your mouth with clean water!"), false),
        BrushingStep(120, 122, listOf("Wow! Super clean teeth! Great brushing!"), false)
    )

    val brushingOnlyDuration = brushingTimeline
        .filter { it.showBrush }
        .sumOf { (it.endSec - it.startSec).toDouble() }
        .toFloat()

    // 🎥 Video setup
    val videoView = remember { VideoView(context) }

    AndroidView(
        factory = {
            videoView.apply {
                setVideoURI("android.resource://${context.packageName}/${R.raw.toothy_video}".toUri())
                setOnPreparedListener { mp ->
                    mp.isLooping = false
                    if (videoStarted) start()
                }
                // NEW: Add completion listener
                setOnCompletionListener {
                    gameCompleted = true
                }
            }
        },
        update = { view ->
            if (videoStarted && !view.isPlaying && !showStartOverlay) {
                view.start()
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // NEW: Handle game completion separately
    LaunchedEffect(gameCompleted) {
        if (gameCompleted) {
            // Stop video immediately
            try { videoView.pause() } catch (_: Exception) {}

            // Mark as completed in ViewModel
            featureViewModel.markCompleted(featureIndex)

            // Show toast
            Toast.makeText(context, "🎉 Great job! You finished brushing!", Toast.LENGTH_LONG).show()

            // Wait a bit for toast to be visible
            delay(500L)

            // Reset all states to return to start screen
            showStartOverlay = true
            videoStarted = false
            isDragging = false
            brushingProgress = 0f
            brushOffset = Offset.Zero
            currentTexts = listOf("")
            gameCompleted = false

            // Stop video playback completely
            try { videoView.stopPlayback() } catch (_: Exception) {}
        }
    }

    // 🔁 Timeline tracking (SIMPLIFIED - only track progress, not completion)
    LaunchedEffect(videoStarted) {
        if (!videoStarted) return@LaunchedEffect

        var brushedSeconds = 0f
        var previousStep: BrushingStep? = null

        while (videoStarted && !showStartOverlay && !gameCompleted) {
            val posSec = try { videoView.currentPosition / 1000f } catch (_: Exception) { 0f }
            val currentStep = brushingTimeline.find { posSec >= it.startSec && posSec < it.endSec }

            if (currentStep != null) {
                currentTexts = currentStep.texts

                if (previousStep?.showBrush == true && !currentStep.showBrush) {
                    brushOffset = Offset.Zero
                    isDragging = false
                }

                if (currentStep.showBrush) {
                    if (!isDragging && videoView.isPlaying) videoView.pause()
                    else if (isDragging && !videoView.isPlaying) videoView.start()

                    if (isDragging) {
                        brushedSeconds = (brushedSeconds + 0.3f).coerceAtMost(brushingOnlyDuration)
                        brushingProgress = brushedSeconds / brushingOnlyDuration
                    }
                }
                previousStep = currentStep
            }

            delay(300L)
        }
    }

    // 🧭 Progress + top bar
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        try { videoView.stopPlayback() } catch (_: Exception) {}
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
            )

            Spacer(modifier = Modifier.width(12.dp))

            LinearProgressIndicator(
            progress = { brushingProgress },
            modifier = Modifier
                                .weight(1f)
                                .height(10.dp),
            color = ProgressIndicatorDefaults.linearColor,
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_mascot),
                contentDescription = "Mascot",
                modifier = Modifier.size(140.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = currentTexts.firstOrNull() ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 🪥 Draggable toothbrush
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val posSec = (try { videoView.currentPosition } catch (_: Exception) { 0 }) / 1000f
        val currentStep = brushingTimeline.find { posSec >= it.startSec && posSec < it.endSec }

        if (currentStep?.showBrush == true) {
            Image(
                painter = painterResource(id = R.drawable.ic_toothbrush),
                contentDescription = "Toothbrush",
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer(
                        translationX = brushOffset.x,
                        translationY = brushOffset.y
                    )
                    .pointerInput(currentStep.startSec) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                brushOffset += dragAmount
                            },
                            onDragEnd = {
                                isDragging = false
                                brushOffset = Offset.Zero
                                if (videoView.isPlaying) videoView.pause()
                            }
                        )
                    }
            )
        }
    }

    // 🟢 Start overlay
    if (showStartOverlay) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAnim"
            )

            Image(
                painter = painterResource(id = R.drawable.ic_start),
                contentDescription = "Start",
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .clickable {
                        showStartOverlay = false
                        videoStarted = true
                        brushOffset = Offset.Zero
                        brushingProgress = 0f
                        if (mediaPlayer.isPlaying) mediaPlayer.pause()
                        gameCompleted = false
                    }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { videoView.stopPlayback() } catch (_: Exception) {}
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }
}

/* ---------------- MODEL ---------------- */
data class BrushingStep(
    val startSec: Int,
    val endSec: Int,
    val texts: List<String>,
    val showBrush: Boolean
)

/* ---------------- BUBBLE BUDDY FEATURE ---------------- */
@Composable
fun BubbleBuddyScreen(navController: NavHostController,
                      featureIndex: Int,
                      featureViewModel: FeatureViewModel) {
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }

    // NEW: Track completed steps separately for progress
    var completedSteps by remember { mutableIntStateOf(0) }

    // MediaPlayers
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val stepAudios = listOf(
        R.raw.step1_audio,
        R.raw.step2_audio,
        R.raw.step3_audio,
        R.raw.step4_audio,
        R.raw.step5_audio,
        R.raw.step6_audio,
        R.raw.step7_audio,
        R.raw.step8_audio
    )
    val wrongAudio = R.raw.oops_audio

    val steps = listOf(
        "Step 1: First, let's wet our hands under water!",
        "Step 2: Palm to palm! Rub your hands together!",
        "Step 3: Now the backs of your hands! Keep scrubbing!",
        "Step 4: Between your fingers, get those germs!",
        "Step 5: Grip your fingers on each hand!",
        "Step 6: Make sure to clean your thumbs!",
        "Step 7: Clean your fingertips, almost done!",
        "Step 8: Make sure to dry your hands with a clean towel. All clean!"
    )

    val stepImages = listOf(
        R.drawable.ic_step1,
        R.drawable.ic_step2,
        R.drawable.ic_step3,
        R.drawable.ic_step4,
        R.drawable.ic_step5,
        R.drawable.ic_step6,
        R.drawable.ic_step7,
        R.drawable.ic_step8
    )

    // Animation states
    var correctAnswerTrigger by remember { mutableStateOf(false) }
    var wrongAnswerTrigger by remember { mutableStateOf(false) }

    // NEW: Track which image is being pressed for better feedback
    var pressedImage by remember { mutableStateOf<Int?>(null) }

    val mascotScale by animateFloatAsState(
        targetValue = if (correctAnswerTrigger) 1.2f else 1f,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        finishedListener = { correctAnswerTrigger = false }
    )

    val mascotShake = remember { Animatable(0f) }
    LaunchedEffect(wrongAnswerTrigger) {
        if (wrongAnswerTrigger) {
            mascotShake.snapTo(0f)
            mascotShake.animateTo(30f, tween(100, easing = LinearEasing))
            mascotShake.animateTo(-30f, tween(100, easing = LinearEasing))
            mascotShake.animateTo(0f, tween(100, easing = LinearEasing))
            wrongAnswerTrigger = false
        }
    }

    // Start audio loop
    LaunchedEffect(gameStarted) {
        if (!gameStarted) {
            while (true) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, R.raw.start_audio)
                mediaPlayer?.start()

                val duration = mediaPlayer?.duration ?: 0
                delay(duration.toLong())
                delay(5000L)
            }
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Play step audio
    LaunchedEffect(currentStep, gameStarted) {
        if (gameStarted) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, stepAudios[currentStep])
            mediaPlayer?.start()
        }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_bubble_buddy),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (!gameStarted) {
            // Start Screen
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(60.dp)
                        .clickable { navController.popBackStack() },
                    contentScale = ContentScale.Fit
                )

                // Start button pulsing animation
                val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
                val startScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "startScale"
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_start),
                    contentDescription = "Start",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(300.dp)
                        .graphicsLayer(
                            scaleX = startScale,
                            scaleY = startScale
                        )
                        .clickable {
                            gameStarted = true
                            currentStep = 0
                            completedSteps = 0 // NEW: Reset progress when starting
                        }
                )
            }
        } else {
            // Game Screen
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Exit
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Exit",
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { navController.popBackStack() },
                        contentScale = ContentScale.Fit
                    )
                }

                // Progress - FIXED: Use completedSteps instead of currentStep
                LinearProgressIndicator(
                    progress = { completedSteps / steps.size.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Mascot + Bubble
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_mascot),
                        contentDescription = "Mascot",
                        modifier = Modifier
                            .size(200.dp)
                            .graphicsLayer(
                                scaleX = mascotScale,
                                scaleY = mascotScale,
                                rotationZ = mascotShake.value
                            )
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = steps[currentStep],
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 👉 Choices: correct + 1 wrong (stable per step)
                val correctImage = stepImages[currentStep]

                // pick a wrong image that's not the correct one, rotating each step
                val wrongImage = stepImages[(currentStep + 1) % stepImages.size]

                // shuffle once per step, no reshuffle on wrong answers
                val options by remember(currentStep) {
                    mutableStateOf(listOf(correctImage, wrongImage).shuffled())
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    options.forEach { img ->
                        key("$currentStep-$img") {
                            // NEW: Enhanced clickable hand images with visual feedback
                            Box(
                                modifier = Modifier
                                    .size(180.dp) // Slightly larger for better touch target
                                    .shadow(
                                        elevation = if (pressedImage == img) 4.dp else 8.dp,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(
                                        color = if (pressedImage == img)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = if (pressedImage == img) 3.dp else 2.dp,
                                        color = if (pressedImage == img)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null // Remove default ripple
                                    ) {
                                        pressedImage = img
                                        // Reset pressed state after a short delay
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(150)
                                            pressedImage = null
                                        }

                                        if (img == correctImage) {
                                            correctAnswerTrigger = true
                                            // NEW: Update progress only when correct button is selected
                                            completedSteps = currentStep + 1

                                            if (currentStep < steps.lastIndex) {
                                                currentStep++
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "🎉 Great job! Hands are clean!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                featureViewModel.markCompleted(featureIndex)
                                                gameStarted = false
                                                // NEW: Reset progress when game ends
                                                completedSteps = 0
                                            }
                                        } else {
                                            wrongAnswerTrigger = true
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer.create(context, wrongAudio)
                                            mediaPlayer?.start()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = img),
                                    contentDescription = "Hand washing step option",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .graphicsLayer {
                                            scaleX = if (pressedImage == img) 0.9f else 1f
                                            scaleY = if (pressedImage == img) 0.9f else 1f
                                        },
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- SAFE STEPS FEATURE ---------------- */
@Composable
fun SafeStepsScreen(
    navController: NavHostController,
    featureIndex: Int,
    featureViewModel: FeatureViewModel
) {
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }
    var carsCleared by remember { mutableStateOf(false) }
    var playCarsVideo by remember { mutableStateOf(false) }

    // NEW: Track completed steps separately for progress
    var completedSteps by remember { mutableIntStateOf(0) }

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isAudioPlaying by remember { mutableStateOf(false) } // 🔒 prevent skipping

    // COLOR-CODED step dialogues with highlighted keywords
    val stepDialogues = listOf(
        "First things first – we <color>STOP</color>! Never run into the street. Tap the red STOP button to show me you know how to stop.",
        "Great stopping! Now we <color>LOOK</color> both ways. Left, then right, then left again. Tap the LOOK button to check for cars.",
        "Good looking! Now close your eyes and <color>LISTEN</color>. Do you hear any cars? Tap LISTEN to practice using your ears.",
        "Perfect! No cars are coming. Now we can <color>CROSS</color> safely. Tap the green CROSS button to walk across.",
        "You did it! You followed all the safety steps: <color>STOP</color>, <color>LOOK</color>, <color>LISTEN</color>, <color>CROSS</color>! You're a street safety expert!"
    )

    // Colors for each step keyword
    val stepColors = listOf(
        Color(0xFFF44336), // Red for STOP
        Color(0xFFFF9800), // Orange for LOOK
        Color(0xFF2196F3), // Blue for LISTEN
        Color(0xFF4CAF50), // Green for CROSS
        Color(0xFF9C27B0)  // Purple for success message
    )

    val stepAudios = listOf(
        R.raw.stop,
        R.raw.look,
        R.raw.listen,
        R.raw.cross,
        R.raw.success
    )
    val startAudio = R.raw.safesteps_start
    val whoopsAudio = R.raw.whoops
    val waitCarsAudio = R.raw.during_look

    var correctAnswerTrigger by remember { mutableStateOf(false) }
    var wrongAnswerTrigger by remember { mutableStateOf(false) }

    val mascotScale by animateFloatAsState(
        targetValue = if (correctAnswerTrigger) 1.2f else 1f,
        animationSpec = tween(400, easing = LinearOutSlowInEasing),
        finishedListener = { correctAnswerTrigger = false }
    )

    val mascotShake = remember { Animatable(0f) }
    LaunchedEffect(wrongAnswerTrigger) {
        if (wrongAnswerTrigger) {
            mascotShake.snapTo(0f)
            mascotShake.animateTo(20f, tween(100))
            mascotShake.animateTo(-20f, tween(100))
            mascotShake.animateTo(0f, tween(100))
            wrongAnswerTrigger = false
        }
    }

    // Start screen audio loop
    LaunchedEffect(gameStarted) {
        if (!gameStarted) {
            while (true) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, startAudio)
                mediaPlayer?.start()
                delay((mediaPlayer?.duration ?: 0).toLong() + 3000)
            }
        } else {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Auto play audio when step changes
    LaunchedEffect(currentStep, gameStarted) {
        if (gameStarted && currentStep in stepAudios.indices) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, stepAudios[currentStep])
            isAudioPlaying = true
            mediaPlayer?.setOnCompletionListener {
                isAudioPlaying = false
            }
            mediaPlayer?.start()
        }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background rendering
        when {
            playCarsVideo && !carsCleared -> {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI("android.resource://${ctx.packageName}/${R.raw.cars_pass}".toUri())
                            setOnCompletionListener {
                                carsCleared = true
                                playCarsVideo = false
                                currentStep++
                                // NEW: Update progress after cars video completes
                                completedSteps = currentStep
                            }
                            start()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            !carsCleared && currentStep >= 1 -> {
                Image(
                    painter = painterResource(id = R.drawable.bg_street_cars),
                    contentDescription = "Street with cars",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Image(
                    painter = painterResource(id = R.drawable.bg_street),
                    contentDescription = "Clear street",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (!gameStarted) {
            // Start Screen
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(60.dp)
                        .clickable { navController.popBackStack() }
                )

                val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
                val startScale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        tween(1000, easing = LinearEasing),
                        RepeatMode.Reverse
                    ), label = "startScale"
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_start),
                    contentDescription = "Start",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(250.dp)
                        .graphicsLayer(
                            scaleX = startScale,
                            scaleY = startScale
                        )
                        .clickable {
                            gameStarted = true
                            currentStep = 0
                            completedSteps = 0 // NEW: Reset progress when starting
                            carsCleared = false
                            playCarsVideo = false
                            correctAnswerTrigger = true
                        }
                )
            }
        } else {
            // Game Screen - FIXED: Not scrollable, progress bar beside back button
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // FIXED: Progress bar beside back button in same row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Exit",
                        modifier = Modifier
                            .size(60.dp)
                            .clickable {
                                mediaPlayer?.stop()
                                mediaPlayer?.release()
                                navController.popBackStack()
                            }
                    )

                    LinearProgressIndicator(
                        // FIXED: Use completedSteps instead of currentStep for progress
                        progress = { completedSteps / 4f }, // 4 steps to complete (0-3)
                        modifier = Modifier
                            .weight(1f)
                            .height(12.dp)
                            .padding(horizontal = 16.dp)
                    )
                }

                Spacer(Modifier.height(18.dp))

                Image(
                    painter = painterResource(id = R.drawable.safestep_mascot),
                    contentDescription = "Mascot",
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer(
                            scaleX = mascotScale,
                            scaleY = mascotScale,
                            rotationZ = mascotShake.value
                        )
                )

                Box(
                    Modifier
                        .padding(16.dp)
                        .background(
                            Color.White.copy(alpha = 0.8f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    // Build the text with colored keywords
                    val dialogueText = stepDialogues[currentStep]
                    val parts = dialogueText.split("<color>", "</color>")

                    Text(
                        buildAnnotatedString {
                            parts.forEachIndexed { index, part ->
                                if (index % 2 == 0) {
                                    // Regular text
                                    append(part)
                                } else {
                                    // Colored keyword
                                    withStyle(
                                        style = SpanStyle(
                                            color = stepColors.getOrElse(currentStep) { Color.Black },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    ) {
                                        append(part)
                                    }
                                }
                            }
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Step Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val btns = listOf(
                        R.drawable.btn_stop to 0,
                        R.drawable.btn_look to 1,
                        R.drawable.btn_listen to 2,
                        R.drawable.btn_cross to 3
                    )

                    btns.chunked(2).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { (img, idx) ->
                                var pressed by remember { mutableStateOf(false) }

                                val scale by animateFloatAsState(
                                    targetValue = if (pressed) 1.2f else 1f,
                                    animationSpec = tween(300),
                                    finishedListener = { pressed = false }
                                )

                                Image(
                                    painter = painterResource(id = img),
                                    contentDescription = "Step Button",
                                    modifier = Modifier
                                        .size(150.dp)
                                        .aspectRatio(1f)
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                        )
                                        .clickable(enabled = !isAudioPlaying) { // 🔒 prevent skipping
                                            pressed = true
                                            if (idx == currentStep) {
                                                correctAnswerTrigger = true
                                                // NEW: Update progress only when correct button is selected
                                                completedSteps = currentStep + 1

                                                if (idx == 1) {
                                                    playCarsVideo = true
                                                    mediaPlayer?.release()
                                                    mediaPlayer = MediaPlayer.create(context, waitCarsAudio)
                                                    isAudioPlaying = true
                                                    mediaPlayer?.setOnCompletionListener {
                                                        isAudioPlaying = false
                                                    }
                                                    mediaPlayer?.start()
                                                } else if (currentStep in 0..2) {
                                                    currentStep++
                                                } else if (currentStep == 3) {
                                                    mediaPlayer?.release()
                                                    mediaPlayer = MediaPlayer.create(context, stepAudios[4])
                                                    isAudioPlaying = true
                                                    mediaPlayer?.setOnCompletionListener {
                                                        isAudioPlaying = false
                                                        Toast.makeText(
                                                            context,
                                                            "🎉 Well done crossing safely!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        featureViewModel.markCompleted(featureIndex)
                                                        gameStarted = false
                                                        currentStep = 0
                                                        completedSteps = 0 // NEW: Reset progress
                                                        carsCleared = false
                                                        playCarsVideo = false
                                                    }
                                                    mediaPlayer?.start()
                                                }
                                            } else {
                                                wrongAnswerTrigger = true
                                                mediaPlayer?.release()
                                                mediaPlayer = MediaPlayer.create(context, whoopsAudio)
                                                isAudioPlaying = true
                                                mediaPlayer?.setOnCompletionListener {
                                                    isAudioPlaying = false
                                                }
                                                mediaPlayer?.start()
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- RESCUE DIAL FEATURE ---------------- */
@Composable
fun RescueDialScreen(
    navController: NavHostController,
    featureIndex: Int,
    featureViewModel: FeatureViewModel
) {
    val context = LocalContext.current

    // TextToSpeech setup
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try { ttsInstance?.language = Locale.US } catch (e: Exception) { e.printStackTrace() }
            }
        }
        ttsInstance
    }

    var step by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf("") }
    var showVideo by remember { mutableStateOf(false) }
    var mascotShakeTrigger by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }

    val toneGen = remember { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }
    val mascotShake = remember { Animatable(0f) }
    val mascotScale = rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Reverse)
    )

    val stepDialogues = mapOf(
        0 to "Tap the numbers to dial 911!",
        1 to "Tap the numbers to dial 911!",
        2 to "Calling now..."
    )

    // Background loop audio until game starts
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    LaunchedEffect(gameStarted) {
        if (!gameStarted) {
            while (!gameStarted) {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer.create(context, R.raw.rescue_start)
                mediaPlayer?.start()
                val duration = mediaPlayer?.duration ?: 0
                delay(duration.toLong())
                delay(500L)
            }
        } else {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
    DisposableEffect(Unit) { onDispose { mediaPlayer?.release() } }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // START SCREEN (UPDATED to match other features)
        if (!gameStarted) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Back button
                Image(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(60.dp)
                        .clickable {
                            navController.popBackStack()
                        }
                )

                // Start button with same pulse animation as other features
                val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
                val startScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "startScale"
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_start),
                    contentDescription = "Start",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(300.dp) // Same size as other features
                        .graphicsLayer(
                            scaleX = startScale,
                            scaleY = startScale
                        )
                        .clickable {
                            gameStarted = true
                            step = 1
                            input = ""
                            tts.speak(
                                "Tap the numbers to dial 911!",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                UUID.randomUUID().toString()
                            )
                        }
                )
            }
        } else {
            // Back button for game screen
            Image(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                modifier = Modifier
                    .padding(16.dp)
                    .size(60.dp)
                    .clickable {
                        step = 0
                        input = ""
                        gameStarted = false
                        navController.popBackStack()
                    }
            )

            // DIAL PAD SCREEN
            if (step == 1) {
                LaunchedEffect(mascotShakeTrigger) {
                    if (mascotShakeTrigger) {
                        mascotShake.snapTo(0f)
                        mascotShake.animateTo(15f, tween(100))
                        mascotShake.animateTo(-15f, tween(100))
                        mascotShake.animateTo(0f, tween(100))
                        mascotShakeTrigger = false
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mascot with breathing animation
                    Image(
                        painter = painterResource(id = R.drawable.buddy),
                        contentDescription = "Mascot",
                        modifier = Modifier
                            .size(180.dp)
                            .graphicsLayer(
                                scaleX = mascotScale.value,
                                scaleY = mascotScale.value,
                                rotationZ = mascotShake.value
                            )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dialogue box
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stepDialogues[step] ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(60.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = input.ifEmpty { "Tap numbers..." },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // HARD-CODED NUMPAD CONTAINER
                    Box(
                        modifier = Modifier
                            .width(280.dp) // Fixed width
                            .height(360.dp) // Fixed height
                            .background(Color(0xCC1A1A1A), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val rows = listOf(
                            listOf('1', '2', '3'),
                            listOf('4', '5', '6'),
                            listOf('7', '8', '9'),
                            listOf('*', '0', '#')
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rows.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    row.forEach { digit ->
                                        var pressed by remember { mutableStateOf(false) }
                                        val scale by animateFloatAsState(
                                            targetValue = if (pressed) 0.9f else 1f,
                                            animationSpec = tween(150)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3A3A3A))
                                                .graphicsLayer { scaleX = scale; scaleY = scale }
                                                .clickable {
                                                    pressed = true
                                                    if (input.length < 3) {
                                                        input += digit
                                                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                                                        tts.speak(
                                                            digit.toString(),
                                                            TextToSpeech.QUEUE_FLUSH,
                                                            null,
                                                            UUID.randomUUID().toString()
                                                        )
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = digit.toString(),
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // ICON-BASED CALL BUTTON (no PNG needed)
                    var callButtonPressed by remember { mutableStateOf(false) }
                    val callButtonScale by animateFloatAsState(
                        targetValue = if (callButtonPressed) 0.95f else 1f,
                        animationSpec = tween(150)
                    )

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                if (input == "911") Color(0xFF4CAF50) else Color(0xFFF44336),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape)
                            .graphicsLayer { scaleX = callButtonScale; scaleY = callButtonScale }
                            .clickable {
                                callButtonPressed = true
                                if (input == "911") {
                                    step = 2
                                    showVideo = true
                                } else if (input.length == 3) {
                                    mascotShakeTrigger = true
                                    input = ""
                                    tts.speak("Try again!", TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call 911",
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Call button instruction
                    Text(
                        text = if (input == "911") "Tap to call emergency!" else "Dial 911 first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Video after correct call
            if (step == 2 && showVideo) {
                AndroidView(
                    factory = {
                        VideoView(context).apply {
                            setVideoURI("android.resource://${context.packageName}/${R.raw.call_sequence}".toUri())
                            setOnCompletionListener {
                                step = 0
                                input = ""
                                gameStarted = false
                                showVideo = false
                                Toast.makeText(context, "Well done! You called help!", Toast.LENGTH_SHORT).show()
                                featureViewModel.markCompleted(featureIndex)
                            }
                            start()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


/* ---------------- AVATAR FEATURE ---------------- */
@Composable
fun AvatarScreen(navController: NavHostController? = null) {
    val avatars = listOf(R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3)
    var selectedAvatar by remember { mutableIntStateOf(avatars[0]) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F7FF))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Select a character", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        Image(painter = painterResource(selectedAvatar), contentDescription = "Selected Avatar",
            modifier = Modifier.size(160.dp).clip(RoundedCornerShape(100.dp))
        )
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            avatars.forEach { avatar ->
                val isSelected = avatar == selectedAvatar
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) BorderStroke(3.dp, Color.Red) else BorderStroke(2.dp, Color.Gray),
                    modifier = Modifier
                        .size(90.dp)
                        .clickable { selectedAvatar = avatar }
                ) {
                    Image(painter = painterResource(avatar), contentDescription = "Avatar Option", modifier = Modifier.padding(8.dp))
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                // Go back to the previous screen (main dashboard)
                navController?.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0E0)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.width(200.dp).height(56.dp)
        ) {
            Text("Select", fontSize = 20.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}

/* ---------------- PARENT DASHBOARD FEATURE ---------------- */
@Composable
fun ParentDashboardScreen(
    featureViewModel: FeatureViewModel,
    navController: NavHostController,
    parentPin: String = "1234"
) {
    val features = featureViewModel.features
    val total = (features.size).coerceAtLeast(1)
    val completedCount = features.count { it.completed }

    var isAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // 🔒 Show PIN first
    if (!isAuthenticated) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Parent Access") },
            text = {
                Column {
                    Text("Enter the parent PIN to access the dashboard.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it.filter { ch -> ch.isDigit() }.take(6)
                            pinError = false
                        },
                        placeholder = { Text("PIN") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    if (pinError) {
                        Text(
                            "Incorrect PIN",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (enteredPin == parentPin) {
                        isAuthenticated = true
                        enteredPin = ""
                    } else pinError = true
                }) {
                    Text("Confirm")
                }
            }
        )
    } else {
        // ✅ Main dashboard content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFF6FF))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Parent Dashboard",
                fontSize = 28.sp,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { completedCount / total.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor
            )

           // Find most and least played features
            Spacer(Modifier.height(16.dp))

            val mostPlayedEntry = features.withIndex().maxByOrNull { it.value.playCount }
            val leastPlayedEntry = features.withIndex().minByOrNull { it.value.playCount }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Activity Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )

                    if (features.isNotEmpty()) {
                        // Most played
                        if (mostPlayedEntry != null) {
                            val idx = mostPlayedEntry.index
                            val f = mostPlayedEntry.value
                            Text("🎯 Most Played: Feature ${idx + 1} (${f.playCount} plays)")
                        }

                        // Least played
                        if (leastPlayedEntry != null) {
                            val idx = leastPlayedEntry.index
                            val f = leastPlayedEntry.value
                            Text("🕹️ Least Played: Feature ${idx + 1} (${f.playCount} plays)")
                        }
                    } else {
                        Text("No data available yet.", fontStyle = FontStyle.Italic, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("$completedCount of $total features completed", color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            // 🔹 Feature list
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                features.forEachIndexed { index, feature ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Feature ${index + 1}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Played ${feature.playCount} times",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }

                            // ✅ Small status badge
                            val statusColor =
                                if (feature.completed) Color(0xFF4CAF50) else Color(0xFFFFA726)
                            Box(
                                modifier = Modifier
                                    .background(statusColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (feature.completed) "Completed" else "In Progress",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Main")
            }
        }
    }
}







