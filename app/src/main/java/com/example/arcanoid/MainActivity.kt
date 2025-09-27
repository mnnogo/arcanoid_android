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
import kotlin.random.Random

// ракетка
const val PADDLE_INIT_X = 400f
const val PADDLE_WIDTH = 200f
const val PADDLE_HEIGHT = 30f
const val PADDLE_BOTTOM_PADDING = 300f
val PADDLE_COLOR = Color.Cyan

// мяч
const val BALL_RADIUS = 20f
val BALL_COLOR = Color.Magenta
val BALL_INIT_POSITION = Offset(500f, 1500f)
val BALL_INIT_VELOCITY = Offset(10f, -10f)

// блоки
const val BLOCK_WIDTH = 100f
const val BLOCK_HEIGHT = 40f
const val BLOCK_ROWS = 10
const val BLOCK_COLUMNS = 7
const val BLOCK_HORIZONTAL_SPACING = 30f
const val BLOCK_VERTICAL_SPACING = 30f
const val BLOCK_MARGIN_TOP = 50f
const val BLOCK_MARGIN_LEFT = 100f
val NORMAL_BLOCK_COLOR = Color.Cyan
val BONUS_EXTRA_BALL_BLOCK_COLOR = Color.Magenta

// вероятности появления блоков
const val CHANCE_EXTRA_BALL_BLOCK = 0.2f


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
    /*
    * инициализация начального состояния (state) поля
    */
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var paddleX by remember { mutableFloatStateOf(PADDLE_INIT_X) }
    var balls = remember {
        mutableStateListOf(
            Ball(position = BALL_INIT_POSITION, velocity = BALL_INIT_VELOCITY)
        )
    }
    val blocks = remember {
        mutableStateListOf<Block>().apply {
            for (row in 0 until BLOCK_ROWS) {
                for (col in 0 until BLOCK_COLUMNS) {
                    // случайное определение типа блока
                    val type = if (Random.nextFloat() < CHANCE_EXTRA_BALL_BLOCK) {
                        BlockType.BONUS_EXTRA_BALL
                    } else {
                        BlockType.NORMAL
                    }
                    add(
                        Block(
                            x = col * (BLOCK_WIDTH + BLOCK_HORIZONTAL_SPACING) + BLOCK_MARGIN_LEFT,
                            y = row * (BLOCK_HEIGHT + BLOCK_VERTICAL_SPACING) + BLOCK_MARGIN_TOP,
                            width = BLOCK_WIDTH,
                            height = BLOCK_HEIGHT,
                            type = type
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
                    if (paddleX > canvasWidth - PADDLE_WIDTH) {
                        paddleX = canvasWidth - PADDLE_WIDTH
                    }
                }
            }
    ) {
        canvasWidth = size.width
        canvasHeight = size.height

        // отрисовка ракетки
        drawRect(
            color = PADDLE_COLOR,
            topLeft = Offset(paddleX, canvasHeight - PADDLE_BOTTOM_PADDING),
            size = Size(PADDLE_WIDTH, PADDLE_HEIGHT)
        )

        // отрисовка мячей
        balls.forEach { ball ->
            drawCircle(
                color = BALL_COLOR,
                radius = BALL_RADIUS,
                center = ball.position
            )
        }

        // отрисовка блоков
        blocks.forEach { block ->
            if (!block.isDestroyed) {
                val blockColor = when (block.type) {
                    BlockType.NORMAL -> NORMAL_BLOCK_COLOR
                    BlockType.BONUS_EXTRA_BALL -> BONUS_EXTRA_BALL_BLOCK_COLOR
                }
                drawRect(
                    color = blockColor,
                    topLeft = Offset(block.x, block.y),
                    size = Size(block.width, block.height)
                )
            }
        }
    }

    // цикл обновления игры
    LaunchedEffect(Unit) {
        while (true) {
            // чтобы избежать ConcurrentModificationException новые мячи добавляются только в конце
            // итерации
            val newBalls = mutableListOf<Ball>()
            balls.forEach { ball ->
                ball.position += ball.velocity

                // проверка столкновений со стенами (кроме нижней)
                if (ball.position.x < BALL_RADIUS || ball.position.x > canvasWidth - BALL_RADIUS) {
                    ball.velocity = ball.velocity.copy(x = -ball.velocity.x)
                }
                if (ball.position.y < BALL_RADIUS) {
                    ball.velocity = ball.velocity.copy(y = -ball.velocity.y)
                }

                // столкновение с ракеткой
                val paddleTop = canvasHeight - PADDLE_BOTTOM_PADDING
                val paddleBottom = paddleTop + PADDLE_HEIGHT
                if (ball.position.y + BALL_RADIUS >= paddleTop &&
                    ball.position.y - BALL_RADIUS <= paddleBottom &&
                    ball.position.x >= paddleX &&
                    ball.position.x <= paddleX + PADDLE_WIDTH
                ) {
                    ball.velocity = ball.velocity.copy(y = -ball.velocity.y)
                }

                // столкновение с блоками
                blocks.forEach { block ->
                    if (!block.isDestroyed) {
                        val hit = ball.position.x + BALL_RADIUS >= block.x &&
                                ball.position.x - BALL_RADIUS <= block.x + block.width &&
                                ball.position.y + BALL_RADIUS >= block.y &&
                                ball.position.y - BALL_RADIUS <= block.y + block.height
                        if (hit) {
                            block.isDestroyed = true
                            ball.velocity = ball.velocity.copy(y = -ball.velocity.y)

                            // добавление нового мяча при столкновении с бонусным
                            if (block.type == BlockType.BONUS_EXTRA_BALL) {
                                newBalls.add(
                                    Ball(
                                        position = ball.position.copy(y = ball.position.y + 150f),
                                        velocity = Offset(-ball.velocity.x, ball.velocity.y)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            balls.addAll(newBalls)

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