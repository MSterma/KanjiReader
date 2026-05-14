package com.example.kanjireader.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kanjireader.R
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val char: String,
    val x: Float,
    val y: Float,
    val alpha: Float,
    val size: Float,
    val glow: Float
)

@Composable
fun AnimatedSplashScreen(onAnimationEnd: () -> Unit) {
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }
    val centerX = screenWidth / 2
    val centerY = screenHeight / 2

    // Blok linii wyśrodkowany w pionie (wysokość bloku = 280dp, co daje 7 linii z odstępami)
    val blockHeight = with(density) { 280.dp.toPx() }
    val blockTop = (centerY - blockHeight / 2).coerceAtLeast(with(density) { 20.dp.toPx() })
    val safeBottom = screenHeight - with(density) { 150.dp.toPx() }
    val blockBottom = (blockTop + blockHeight).coerceAtMost(safeBottom)
    val lineSpacing = (blockBottom - blockTop) / 6f  // 7 linii = 6 odstępów

    // Zakres X: od 30% do 70% szerokości (znaki skupione w środku)
    val minX = screenWidth * 0.30f
    val maxX = screenWidth * 0.70f

    var phase by remember { mutableStateOf(0) }
    var scanY by remember { mutableStateOf(0f) }
    var scanAlpha by remember { mutableStateOf(0f) }
    var logoScale by remember { mutableStateOf(1f) }
    var logoAlpha by remember { mutableStateOf(0f) }
    var pulseScale by remember { mutableStateOf(1f) }

    var particles by remember { mutableStateOf(emptyList<Particle>()) }

    val fixedPositions = remember {
        val rand = Random(12345)
        val charsPool = listOf("日", "本", "語", "漢", "字", "学", "書", "読", "者", "生")
        val minDistance = with(density) { 50.dp.toPx() }
        val positions = mutableListOf<Particle>()

        for (line in 0 until 7) {
            val y = blockTop + line * lineSpacing
            val count = rand.nextInt(1, 6)
            val maxAttempts = 100
            val xPositions = mutableListOf<Float>()

            repeat(count) {
                var attempts = 0
                var placed = false
                while (!placed && attempts < maxAttempts) {
                    val candidateX = minX + rand.nextFloat() * (maxX - minX)
                    var conflict = false
                    for (existingX in xPositions) {
                        if (abs(candidateX - existingX) < minDistance) {
                            conflict = true
                            break
                        }
                    }
                    if (!conflict) {
                        xPositions.add(candidateX)
                        placed = true
                    }
                    attempts++
                }
                if (!placed) {
                    val step = (maxX - minX) / (count + 1)
                    val fallbackX = minX + step * (it + 1)
                    xPositions.add(fallbackX)
                }
            }
            xPositions.sort()
            for (x in xPositions) {
                val char = charsPool[rand.nextInt(charsPool.size)]
                positions.add(Particle(char, x, y, 1f, 34f, 0f))
            }
        }
        positions
    }

    LaunchedEffect(Unit) {
        particles = fixedPositions
    }

    LaunchedEffect(Unit) {
        val stepDelay = 16L
        delay(300)

        // Faza 1: skanowanie
        phase = 1
        scanAlpha = 0.9f
        val steps1 = 60
        for (step in 0..steps1) {
            val progress = step.toFloat() / steps1
            scanY = safeBottom * progress
            particles = particles.map { p ->
                val dist = abs(p.y - scanY)
                val glow = if (dist < 45f) (1f - dist / 45f) * 0.9f else 0f
                p.copy(glow = glow)
            }
            delay(stepDelay)
        }
        scanAlpha = 0f

        // Faza 2: zbieganie do środka
        phase = 2
        val steps2 = 60
        for (step in 0..steps2) {
            val progress = step.toFloat() / steps2
            val easeOut = 1f - (1f - progress) * (1f - progress)
            particles = particles.map { p ->
                val newX = p.x + (centerX - p.x) * easeOut
                val newY = p.y + (centerY - p.y) * easeOut
                val newAlpha = (1f - progress).coerceAtLeast(0f)
                p.copy(x = newX, y = newY, alpha = newAlpha)
            }
            delay(stepDelay)
        }

        // Faza 3: pojawienie się logo
        phase = 3
        val steps3 = 30
        for (step in 0..steps3) {
            val progress = step.toFloat() / steps3
            logoAlpha = progress
            logoScale = 1f
            delay(stepDelay)
        }

        // Faza 4: pulsacja logo
        phase = 4
        val steps4 = 50
        for (step in 0..steps4) {
            val t = step.toFloat() / steps4
            val scale = 1f + 0.12f * sin(t * Math.PI.toFloat() * 5f)
            pulseScale = scale
            delay(stepDelay)
        }
        pulseScale = 1f
        onAnimationEnd()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF12151E))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (phase <= 2) {
                drawIntoCanvas { canvas ->
                    particles.forEach { p ->
                        if (p.alpha > 0f) {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.argb((p.alpha * 255).toInt(), 255, 255, 255)
                                textSize = p.size
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                if (p.glow > 0f) {
                                    setShadowLayer(14f, 0f, 0f, android.graphics.Color.argb((p.glow * 200).toInt(), 100, 200, 255))
                                } else {
                                    setShadowLayer(0f, 0f, 0f, 0)
                                }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            canvas.nativeCanvas.drawText(p.char, p.x, p.y, paint)
                        }
                    }
                }
            }

            if (phase == 1 && scanAlpha > 0f) {
                drawLine(
                    color = Color.White.copy(alpha = scanAlpha * 0.85f),
                    start = Offset(0f, scanY),
                    end = Offset(screenWidth, scanY),
                    strokeWidth = 4.dp.toPx()
                )
                for (i in -2..2) {
                    drawLine(
                        color = Color.Cyan.copy(alpha = scanAlpha * 0.3f),
                        start = Offset(0f, scanY + i * 5f),
                        end = Offset(screenWidth, scanY + i * 5f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        if (phase >= 3 && logoAlpha > 0f) {
            val currentScale = if (phase == 3) logoScale else pulseScale
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((120.dp * currentScale).coerceAtLeast(0.dp))
                    .graphicsLayer(alpha = logoAlpha)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appicon),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "KanjiReader",
                fontSize = 28.sp,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "loading...",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}