package com.fei.firstapp

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fei.firstapp.ui.theme.FirstappTheme

class AudioActivity : ComponentActivity() {
    private var audioPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstappTheme {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("音乐页面")
                    Button(onClick = { playAudio() }) { Text("播放音乐") }
                    Button(onClick = { stopAudio() }) { Text("停止音乐") }
                }
            }
        }
    }

    private fun playAudio() {
        stopAudio()
        try {
            audioPlayer = MediaPlayer.create(this, R.raw.sample_audio)?.apply {
                setOnCompletionListener { player ->
                    player.release()
                    if (audioPlayer === player) audioPlayer = null
                }
                setOnErrorListener { player, _, _ ->
                    player.release()
                    audioPlayer = null
                    Toast.makeText(this@AudioActivity, "音乐播放失败", Toast.LENGTH_SHORT).show()
                    true
                }
                start()
            }
            if (audioPlayer == null) Toast.makeText(this, "音频无法加载", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            stopAudio()
            Toast.makeText(this, "音乐播放失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopAudio() {
        audioPlayer?.let { player ->
            if (player.isPlaying) player.stop()
            player.release()
        }
        audioPlayer = null
    }

    override fun onDestroy() {
        stopAudio()
        super.onDestroy()
    }
}

