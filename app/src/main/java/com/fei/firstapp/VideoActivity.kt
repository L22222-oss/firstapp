package com.fei.firstapp

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.core.content.FileProvider
import com.fei.firstapp.ui.theme.FirstappTheme
import java.io.File

class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirstappTheme {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("视频页面")
                    Button(onClick = { openBundledVideo() }) { Text("播放视频") }
                }
            }
        }
    }

    private fun openBundledVideo() {
        val dir = File(cacheDir, "videos").apply { mkdirs() }
        val file = File(dir, "demo_video.mp4")
        resources.openRawResource(R.raw.demo_video).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        try {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/mp4")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            })
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "手机没有可用的视频播放器", Toast.LENGTH_LONG).show()
        }
    }
}

