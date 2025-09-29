package com.example.arcanoid

import android.graphics.Paint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arcanoid.ui.theme.ArcanoidTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

// текст с очками
const val SCORE_TOP_PADDING = 100f
const val SCORE_LEFT_PADDING = 50f

// ракетка
const val PADDLE_INIT_X = 400f
const val PADDLE_INIT_WIDTH = 200f
const val PADDLE_HEIGHT = 30f
const val PADDLE_BOTTOM_PADDING = 420f
val PADDLE_COLOR = Color.Cyan
const val WIDER_PADDLE_BONUS = 50f

// мяч
const val BALL_RADIUS = 20f
val BALL_COLOR = Color.Magenta
val BALL_INIT_POSITION = Offset(500f, 1500f)
val BALL_INIT_VELOCITY = Offset(12f, -12f)
const val NEW_BALL_Y_OFFSET = 270f

// блоки
const val BLOCK_WIDTH = 100f
const val BLOCK_HEIGHT = 40f
const val BLOCK_ROWS = 10
const val BLOCK_COLUMNS = 7
const val BLOCK_HORIZONTAL_SPACING = 30f
const val BLOCK_VERTICAL_SPACING = 30f
const val BLOCK_MARGIN_TOP = 160f
const val BLOCK_MARGIN_LEFT = 100f
val NORMAL_BLOCK_COLOR = Color(0xFF00BCD4)
val BONUS_EXTRA_BALL_BLOCK_COLOR = Color.Magenta
val BONUS_WIDER_PADDLE_COLOR = Color.Green

// вероятности появления блоков
const val CHANCE_EXTRA_BALL_BLOCK = 0.15f
const val CHANCE_WIDER_PADDLE_BLOCK = 0.1f

// остальное
const val START_GAME_DELAY_MILLISECONDS = 500L

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ArcanoidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ArkanoidGameScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun ArkanoidGameScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    /*
    * инициализация начального состояния (state) поля
    */
    var score by remember { mutableIntStateOf(0) }
    val bestScore by ScoreManager.getBestScore(context).collectAsState(initial = 0)
    var isGameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var isStarting by remember { mutableStateOf(true) }

    var paddleWidth by remember { mutableFloatStateOf(PADDLE_INIT_WIDTH) }
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }
    var paddleX by remember { mutableFloatStateOf(PADDLE_INIT_X) }
    val balls = remember { mutableStateListOf(Ball( BALL_INIT_POSITION, BALL_INIT_VELOCITY))}
    val blocks = remember { mutableStateListOf<Block>().apply { initializeBlocks(this) } }

    fun restartGame(startingScore: Int = 0) {
        score = startingScore
        isGameOver = false
        isPaused = false
        isStarting = true
        paddleWidth = PADDLE_INIT_WIDTH
        paddleX = PADDLE_INIT_X
        balls.clear()
        balls.add(Ball(BALL_INIT_POSITION, BALL_INIT_VELOCITY))
        blocks.clear()
        initializeBlocks(blocks)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.menu_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Canvas(
            modifier = modifier
                .fillMaxSize()
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
                color = PADDLE_COLOR,
                topLeft = Offset(paddleX, canvasHeight - PADDLE_BOTTOM_PADDING),
                size = Size(paddleWidth, PADDLE_HEIGHT)
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
                        BlockType.BONUS_WIDER_PADDLE -> BONUS_WIDER_PADDLE_COLOR
                    }
                    drawRect(
                        color = blockColor,
                        topLeft = Offset(block.x, block.y),
                        size = Size(block.width, block.height)
                    )
                }
            }

            // отрисовка очков
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    "Счет: $score",
                    SCORE_LEFT_PADDING,
                    SCORE_TOP_PADDING,
                    Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 60f
                        isAntiAlias = true
                    }
                )
            }
        }

        PauseButton(
            onClick = { isPaused = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )


        if (isPaused) {
            PausePanel(
                score = score,
                bestScore = bestScore,
                onContinue = { isPaused = false },
                onRestart = { restartGame() }
            )
        }

        if (isGameOver) {
            GameOverPanel(
                score = score,
                bestScore = bestScore,
                onRestart = { restartGame() }
            )
        }
    }

    // цикл обновления игры
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)

            if (isPaused)
                continue

            if (isStarting) {
                // пауза 0.5 секунды перед стартом
                delay(START_GAME_DELAY_MILLISECONDS)
                isStarting = false
            }

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
                    ball.position.x <= paddleX + paddleWidth
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
                            score++

                            // добавление нового мяча при столкновении с бонусным
                            if (block.type == BlockType.BONUS_EXTRA_BALL) {
                                newBalls.add(
                                    Ball(
                                        position = ball.position.copy(y = ball.position.y + NEW_BALL_Y_OFFSET),
                                        velocity = Offset(-ball.velocity.x, ball.velocity.y)
                                    )
                                )
                            } else if (block.type == BlockType.BONUS_WIDER_PADDLE) {
                                paddleWidth += WIDER_PADDLE_BONUS
                            }
                        }
                    }
                }
            }
            balls.addAll(newBalls)

            // проверка окончания игры
            if (balls.all { it.position.y - BALL_RADIUS > canvasHeight }) {
                isGameOver = true
            }

            // проверка, разрушены ли все блоки
            if (blocks.all { it.isDestroyed }) {
                restartGame(score) // перезапуск игры но с сохранением очков
            }
        }
    }

    // обновление лучшего результата
    LaunchedEffect(isGameOver) {
        if (isGameOver && score > bestScore) {
            ScoreManager.saveBestScore(context, score)
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

@Composable
fun GameOverPanel(score: Int, bestScore: Int, onRestart: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)) // полупрозрачный фон
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = Color(0xFF2A2F45),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(32.dp),
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Игра окончена",
                    color = Color.Cyan,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Ваш счёт: $score",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Лучший счёт: $bestScore",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Button(
                    onClick = onRestart,
                    modifier = Modifier.padding(top = 8.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFFF)
                    )
                ) {
                    Text(
                        "Начать сначала",
                        color = Color.Black,
                        fontSize = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PausePanel(
    score: Int,
    bestScore: Int,
    onContinue: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(
                    color = Color(0xFF2A2F45),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Пауза",
                    color = Color.Cyan,
                    fontSize = 35.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Ваш счёт: $score",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Лучший счёт: $bestScore",
                    color = Color.White,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(260.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFAA)
                    )
                ) {
                    Text(
                        "Продолжить",
                        color = Color.Black,
                        fontSize = 21.sp
                    )
                }
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(260.dp),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFFF)
                    )
                ) {
                    Text(
                        "Начать сначала",
                        color = Color.Black,
                        fontSize = 21.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PauseButton(onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures {
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            val barWidth = 6.dp.toPx()
            val barHeight = size.height
            val spacing = 6.dp.toPx()

            // левая палка
            drawRect(
                color = Color.White,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, barHeight)
            )
            // правая палка
            drawRect(
                color = Color.White,
                topLeft = Offset(barWidth + spacing, 0f),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

fun initializeBlocks(list: MutableList<Block>) {
    for (row in 0 until BLOCK_ROWS) {
        for (col in 0 until BLOCK_COLUMNS) {
            val type = when {
                Random.nextFloat() < CHANCE_EXTRA_BALL_BLOCK -> BlockType.BONUS_EXTRA_BALL
                Random.nextFloat() < CHANCE_WIDER_PADDLE_BLOCK -> BlockType.BONUS_WIDER_PADDLE
                else -> BlockType.NORMAL
            }
            list.add(
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