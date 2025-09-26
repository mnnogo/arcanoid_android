package com.example.arcanoid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.arcanoid.ui.theme.ArcanoidTheme
import kotlinx.coroutines.delay

data class Block(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    var isDestroyed: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcanoidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArkanoidGameScreen (
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ArkanoidGameScreen(modifier: Modifier = Modifier) {
    // параметры Canvas
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // ракетка
    var paddleX by remember { mutableFloatStateOf(400f) }
    val paddleWidth = 200f
    val paddleHeight = 30f
    val paddleColor = Color.Cyan
    val paddleBottomPadding = 300f

    // мяч
    var ballPosition by remember { mutableStateOf(Offset(500f, 1500f)) }
    var ballVelocity by remember { mutableStateOf(Offset(10f, -10f)) }
    val ballRadius = 20f
    val ballColor = Color.Magenta

    // блоки
    val blockWidth = 100f
    val blockHeight = 40f
    val blockRows = 10
    val blockColumns = 8
    val blockHorizontalSpacing = 10f
    val blockVerticalSpacing = 10f
    val blockMarginTop = 50f
    val blockMarginLeft = 100f

    val blocks = remember {
        mutableStateListOf<Block>().apply {
            for (row in 0 until blockRows) {
                for (col in 0 until blockColumns) {
                    add(
                        Block(
                            x = col * (blockWidth + blockHorizontalSpacing) + blockMarginLeft,
                            y = row * (blockHeight + blockVerticalSpacing) + blockMarginTop,
                            width = blockWidth,
                            height = blockHeight
                        )
                    )
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // смещение ракетки по X
                    paddleX += dragAmount.x

                    // проверка выхода за границы
                    if (paddleX < 0f) paddleX = 0f
                    if (paddleX > canvasWidth - paddleWidth) {
                        paddleX = canvasWidth - paddleWidth
                    }
                }
            }
    ) {
        canvasWidth = size.width
        canvasHeight = size.height

        // отрисовка ракетки
        drawRect(
            color = paddleColor,
            topLeft = Offset(paddleX, canvasHeight - paddleBottomPadding),
            size = Size(paddleWidth, paddleHeight)
        )

        // отрисовка мяча
        drawCircle(
            color = ballColor,
            radius = ballRadius,
            center = ballPosition
        )

        // отрисовка блоков
        blocks.forEach { block ->
            if (!block.isDestroyed) {
                drawRect(
                    color = Color.Cyan,
                    topLeft = Offset(block.x, block.y),
                    size = Size(block.width, block.height)
                )
            }
        }
    }

    // цикл обновления игры
    LaunchedEffect(Unit) {
        while (true) {
            ballPosition += ballVelocity

            // проверка столкновений со стенами (кроме нижней)
            if (ballPosition.x < ballRadius || ballPosition.x > canvasWidth - ballRadius) {
                ballVelocity = ballVelocity.copy(x = -ballVelocity.x)
            }
            if (ballPosition.y < ballRadius) {
                ballVelocity = ballVelocity.copy(y = -ballVelocity.y)
            }

            // столкновение с ракеткой
            val paddleTop = canvasHeight - paddleBottomPadding
            val paddleBottom = paddleTop + paddleHeight
            if (ballPosition.y + ballRadius >= paddleTop &&
                ballPosition.y - ballRadius <= paddleBottom &&
                ballPosition.x >= paddleX &&
                ballPosition.x <= paddleX + paddleWidth
            ) {
                ballVelocity = ballVelocity.copy(y = -ballVelocity.y)
            }

            // столкновение с блоками
            blocks.forEach { block ->
                if (!block.isDestroyed) {
                    val hit = ballPosition.x + ballRadius >= block.x &&
                            ballPosition.x - ballRadius <= block.x + block.width &&
                            ballPosition.y + ballRadius >= block.y &&
                            ballPosition.y - ballRadius <= block.y + block.height
                    if (hit) {
                        block.isDestroyed = true
                        ballVelocity = ballVelocity.copy(y = -ballVelocity.y)
                    }
                }
            }

            delay(16L)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArkanoidGamePreview() {
    ArcanoidTheme {
        ArkanoidGameScreen()
    }
}