package com.example.playsafe

import android.media.MediaPlayer
import android.os.Bundle
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

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
        composable("bubble_buddy") { BubbleBuddyScreen(navController) } // ✅ Updated
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
fun ToothyTimeScreen(navController: NavHostController) {
    val context = LocalContext.current

    // State
    var showStartOverlay by remember { mutableStateOf(true) }
    var videoStarted by remember { mutableStateOf(false) }
    var brushOffset by remember { mutableStateOf(Offset.Zero) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentTexts by remember { mutableStateOf(listOf("Brush your front teeth in a forward and backward motion - show me your smile!")) }
    var brushJoltKey by remember { mutableIntStateOf(0) }
    var stoppedByUser by remember { mutableStateOf(false) }

    // 🎵 Start screen audio
    val mediaPlayer = remember(showStartOverlay) {
        MediaPlayer.create(context, R.raw.toothy_start).apply {
            isLooping = false
        }
    }

    // 🔊 Play audio immediately when overlay shows, then repeat every 5s
    LaunchedEffect(showStartOverlay) {
        if (showStartOverlay) {
            // play right away
            mediaPlayer.seekTo(0)
            mediaPlayer.start()

            // then loop every 5s
            while (showStartOverlay) {
                delay(5000L)
                if (!mediaPlayer.isPlaying) {
                    mediaPlayer.seekTo(0)
                    mediaPlayer.start()
                }
            }
        } else {
            if (mediaPlayer.isPlaying) mediaPlayer.pause()
        }
    }

    // Cleanup media player
    DisposableEffect(showStartOverlay) {
        onDispose {
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }

    // Timeline (demo vs brush intervals)
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
        BrushingStep(52, 57, listOf("Brush the chewing surface of your lower teeth! Lets start with the right side!"), false),
        BrushingStep(57, 62, listOf("Keep Brushing!"), true),
        BrushingStep(62, 68, listOf("Do the other side! Brush your left!"), false),
        BrushingStep(68, 73, listOf("Good Job!"), true),
        BrushingStep(73, 80, listOf("Brush the chewing surface of your upper teeth next! Start with the left side!"), false),
        BrushingStep(80, 86, listOf("You can do it!"), true),
        BrushingStep(86, 93, listOf("Do the other side! Brush your right!"), false),
        BrushingStep(93, 99, listOf("Nice Work!"), true),
        BrushingStep(99, 106, listOf("Don’t forget to brush your tongue! Brush it up and down!"), false),
        BrushingStep(106, 112, listOf("Keep Brushing!"), true),
        BrushingStep(113, 120, listOf("Lastly, rinse out your mouth with clean water!"), false),
        BrushingStep(120, 122, listOf("Wow! Super clean teeth! Great brushing!"), false)
    )

    // Video setup
    val videoView = remember { VideoView(context) }
    var shouldStartAfterPrepared by remember { mutableStateOf(false) }

    AndroidView(
        factory = {
            videoView.apply {
                setVideoURI("android.resource://${context.packageName}/${R.raw.toothy_video}".toUri())
                setOnPreparedListener { mp ->
                    mp.isLooping = false
                    if (shouldStartAfterPrepared) {
                        shouldStartAfterPrepared = false
                        start()
                        brushJoltKey++
                    }
                }
            }
        },
        update = { view ->
            if (videoStarted && !view.isPlaying) view.start()
        },
        modifier = Modifier.fillMaxSize()
    )

    // Overlay UI (progress + mascot + dialogue + back)
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button → Always dashboard
            Image(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Back",
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        stoppedByUser = true // 👈 mark as user exit
                        try { videoView.stopPlayback() } catch (_: Exception) {}
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
            )

            Spacer(modifier = Modifier.width(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(10.dp)
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

    // Draggable toothbrush overlay (only during brush steps)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val posSec = videoView.currentPosition / 1000
        val currentStep = brushingTimeline.find { posSec in it.startSec until it.endSec }

        var lastBrushedStep by remember { mutableStateOf<BrushingStep?>(null) }
        val popAnim = remember { Animatable(1f) }

        if (currentStep?.showBrush == true) {
            if (lastBrushedStep != currentStep) {
                LaunchedEffect(currentStep) {
                    popAnim.snapTo(1.2f)
                    popAnim.animateTo(1f, animationSpec = tween(380, easing = FastOutSlowInEasing))
                }
            }

            Image(
                painter = painterResource(id = R.drawable.ic_toothbrush),
                contentDescription = "Toothbrush",
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer(
                        translationX = brushOffset.x,
                        translationY = brushOffset.y,
                        scaleX = popAnim.value,
                        scaleY = popAnim.value
                    )
                    .pointerInput(currentStep.startSec) {
                        detectDragGestures(
                            onDrag = { change, dragAmount -> change.consume(); brushOffset += dragAmount },
                            onDragEnd = { if (brushOffset.getDistance() > 400f) brushOffset = Offset.Zero }
                        )
                    }
            )
        }
    }

    // Start overlay
    // Start overlay with Start button
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
                        stoppedByUser = false
                        if (mediaPlayer.isPlaying) mediaPlayer.pause() // stop audio on start
                    }
            )
        }
    }

    // Cleanup media player
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.release()
            } catch (_: Exception) {}
        }
    }

    // Sync video + progress + dialogues
    LaunchedEffect(videoStarted) {
        if (videoStarted) {
            while (videoView.isPlaying) {
                val posSec = videoView.currentPosition / 1000
                progress = posSec / 120f

                brushingTimeline.find { posSec in it.startSec until it.endSec }?.let { step ->
                    currentTexts = step.texts
                    if (step.showBrush) brushJoltKey++
                }

                delay(300L)
            }

            val durationSec = videoView.duration / 1000
            val posSec = videoView.currentPosition / 1000
            val completed = posSec >= durationSec - 1

            // Reset state
            videoStarted = false
            showStartOverlay = true
            progress = 0f
            brushOffset = Offset.Zero
            currentTexts = listOf("Brush your front teeth in a forward and backward motion - show me your smile!")

            // ✅ Only toast if NOT stopped by user
            if (!stoppedByUser && completed) {
                Toast.makeText(context, "🎉 Great job! You finished brushing!", Toast.LENGTH_LONG).show()
            }

            // reset flag for next run
            stoppedByUser = false
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            try { videoView.stopPlayback() } catch (_: Exception) {}
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
fun BubbleBuddyScreen(navController: NavHostController? = null) {
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }

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
                        .clickable { navController?.popBackStack() },
                    contentScale = ContentScale.Fit
                )

                Image(
                    painter = painterResource(id = R.drawable.ic_start),
                    contentDescription = "Start",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(300.dp)
                        .clickable {
                            gameStarted = true
                            currentStep = 0
                        }
                )
                // Start button pulsing animation
                val infiniteTransition = rememberInfiniteTransition(label = "startPulse")
                val startScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing), // speed of pulse
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
                            .clickable { navController?.popBackStack() },
                        contentScale = ContentScale.Fit
                    )
                }

                // Progress
                LinearProgressIndicator(
                    progress = { (currentStep + 1) / steps.size.toFloat() },
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

                // pick a wrong image that’s not the correct one, rotating each step
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
                            Image(
                                painter = painterResource(id = img),
                                contentDescription = "Option",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clickable {
                                        if (img == correctImage) {
                                            correctAnswerTrigger = true
                                            if (currentStep < steps.lastIndex) {
                                                currentStep++
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "🎉 Great job! Hands are clean!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                gameStarted = false
                                            }
                                        } else {
                                            wrongAnswerTrigger = true
                                            mediaPlayer?.release()
                                            mediaPlayer = MediaPlayer.create(context, wrongAudio)
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