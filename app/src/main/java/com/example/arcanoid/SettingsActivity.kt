package com.example.arcanoid

import android.content.Intent
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private lateinit var mediaPlayer: MediaPlayer
private lateinit var soundPool: SoundPool
private var brickBreakSfx = 0

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Settings()
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.main_menu_theme).apply {
            isLooping = true
            seekTo(5_000)
            start()
            setVolume(GameSettings.musicVolume, GameSettings.musicVolume)
        }
        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .build()
        brickBreakSfx = soundPool.load(this, R.raw.brick_break_sfx, 1)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
    }
}

@Preview
@Composable
fun Settings() {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var musicVolume by remember { mutableFloatStateOf(GameSettings.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(GameSettings.sfxVolume) }
    var ballSpeed by remember { mutableFloatStateOf(GameSettings.ballSpeed) }
    var chanceExtraBallBlock by remember { mutableFloatStateOf(GameSettings.chanceExtraBallBlock) }
    var chanceWiderPaddleBlock by remember { mutableFloatStateOf(GameSettings.chanceWiderPaddleBlock) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.menu_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(35.dp)
        ) {
            SettingLabeledSlider(
                text = "Громкость музыки: ${(musicVolume * 100).toInt()}%",
                value = musicVolume,
                minValue = 0f,
                maxValue = 1f,
                onValueChange = {
                    musicVolume = it
                    GameSettings.musicVolume = it
                    mediaPlayer.setVolume(it, it) // мгновенно меняем громкость
                    scope.launch { PreferencesManager.saveMusicVolume(context, it) }
                }
            )

            SettingLabeledSlider(
                text = "Громкость эффектов: ${(sfxVolume * 100).toInt()}%",
                value = sfxVolume,
                minValue = 0f,
                maxValue = 1f,
                onValueChange = {
                    sfxVolume = it
                },
                onValueChangeFinished = {
                    GameSettings.sfxVolume = sfxVolume

                    // короткий звук после отпускания для наглядности
                    soundPool.play(brickBreakSfx, sfxVolume, sfxVolume, 1, 0, 1f)

                    scope.launch { PreferencesManager.saveSfxVolume(context, sfxVolume) }
                }
            )

            SettingLabeledSlider(
                text = "Скорость шарика: ${ballSpeed.toInt()}",
                value = ballSpeed,
                minValue = 1f,
                maxValue = 24f,
                onValueChange = {
                    ballSpeed = it
                },
                onValueChangeFinished = {
                    GameSettings.ballSpeed = ballSpeed
                    scope.launch { PreferencesManager.saveBallSpeed(context, ballSpeed) }
                }
            )

            SettingLabeledSlider(
                text = "Шанс розового блока: ${(chanceExtraBallBlock * 100).toInt()}%",
                value = chanceExtraBallBlock,
                minValue = 0f,
                maxValue = 1f,
                onValueChange = {
                    chanceExtraBallBlock = it
                },
                onValueChangeFinished = {
                    GameSettings.chanceExtraBallBlock = chanceExtraBallBlock
                    scope.launch { PreferencesManager.saveChanceExtraBallBlock(context, chanceExtraBallBlock) }
                }
            )

            SettingLabeledSlider(
                text = "Шанс зеленого блока: ${(chanceWiderPaddleBlock * 100).toInt()}%",
                value = chanceWiderPaddleBlock,
                minValue = 0f,
                maxValue = 1f,
                onValueChange = {
                    chanceWiderPaddleBlock = it
                },
                onValueChangeFinished = {
                    GameSettings.chanceWiderPaddleBlock = chanceWiderPaddleBlock
                    scope.launch { PreferencesManager.saveChanceWiderPaddleBlock(context, chanceWiderPaddleBlock) }
                }
            )

            Button(
                onClick = {
                    val intent = Intent(activity, MainActivity::class.java)
                    activity?.startActivity(intent)
                },
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(260.dp)
                    .border(width = 2.dp, color = Color.Magenta),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                )
            ) {
                Text(
                    "Назад",
                    color = Color.White,
                    fontSize = 21.sp
                )
            }
        }
    }
}

@Composable
fun SettingLabeledSlider(
    text: String,
    value: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit = {}
) {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ){
        Text(
            text,
            color = Color.White,
            fontSize = 21.sp
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = minValue..maxValue,
            modifier = Modifier
                .fillMaxWidth(0.75f)
        )
    }
}