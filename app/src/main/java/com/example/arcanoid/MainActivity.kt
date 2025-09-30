package com.example.arcanoid

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private lateinit var mediaPlayer: MediaPlayer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainMenu()
        }
    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launch {
            GameSettings.musicVolume = PreferencesManager.getMusicVolume(applicationContext).first()
            GameSettings.sfxVolume = PreferencesManager.getSfxVolume(applicationContext).first()
            GameSettings.ballSpeed = PreferencesManager.getBallSpeed(applicationContext).first()
            GameSettings.chanceExtraBallBlock =
                PreferencesManager.getChanceExtraBallBlock(applicationContext).first()
            GameSettings.chanceWiderPaddleBlock =
                PreferencesManager.getChanceWiderPaddleBlock(applicationContext).first()

            mediaPlayer = MediaPlayer.create(applicationContext, R.raw.main_menu_theme).apply {
                isLooping = true
                seekTo(5_000)
                start()
                setVolume(GameSettings.musicVolume, GameSettings.musicVolume)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer.stop()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainMenu() {
    val activity = LocalActivity.current

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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "ARCANOID",
                fontFamily = FontFamily(
                    Font(R.font.bungee_inline_regular)
                ),
                fontSize = 58.sp,
                modifier = Modifier
                    .padding(bottom = 165.dp),
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFA500),
                            Color(0xFFCA0B00)
                        )
                    )
                )
            )
            MenuButton(
                text = "Играть",
                onClick = {
                    val intent = Intent(activity, GameActivity::class.java)
                    activity?.startActivity(intent)
                },
                color = Color.Magenta
            )
            MenuButton(
                text = "Настройки",
                onClick = {
                    val intent = Intent(activity, SettingsActivity::class.java)
                    activity?.startActivity(intent)
                }
            )
            MenuButton(
                text = "Выйти",
                onClick = { activity?.finishAffinity() }
            )
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit, color: Color = Color.Cyan) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(60.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black
        ),
        border = BorderStroke(2.dp, color)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp
        )
    }
}