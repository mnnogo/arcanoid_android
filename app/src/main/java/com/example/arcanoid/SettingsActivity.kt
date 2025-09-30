package com.example.arcanoid

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private lateinit var mediaPlayer: MediaPlayer

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
        }
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

    Button(onClick = {
        val intent = Intent(activity, MainActivity::class.java)
        activity?.startActivity(intent)
    }) {
        Text(text = "Назад", color = Color.White)
    }

}