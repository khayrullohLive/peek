package io.github.peek.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.peek.Peek
import io.github.peek.overlay.PeekBubble
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MainActivity : ComponentActivity() {

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(Peek.interceptor())
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    SampleContent(
                        onFireRequest = { url -> fireRequest(url) }
                    )
                    PeekBubble()   // ← bubble sits on top
                }
            }
        }
    }

    private fun fireRequest(url: String) {
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val req = Request.Builder().url(url).build()
                    httpClient.newCall(req).execute().use { }
                } catch (_: Exception) { /* logged by Peek */ }
            }
        }
    }
}

@Composable
private fun SampleContent(onFireRequest: (String) -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = "Peek Sample",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text  = "Tap a button to fire a request, then open the bubble on the right.",
                style = MaterialTheme.typography.bodyMedium,
            )

            Button(onClick = { onFireRequest("https://httpbin.org/get") }) {
                Text("GET httpbin.org/get")
            }
            Button(onClick = { onFireRequest("https://httpbin.org/status/404") }) {
                Text("GET /status/404")
            }
            Button(onClick = { onFireRequest("https://httpbin.org/status/500") }) {
                Text("GET /status/500")
            }
            Button(onClick = { onFireRequest("https://httpbin.org/delay/2") }) {
                Text("GET /delay/2")
            }
            Button(onClick = { onFireRequest("https://nonexistent.invalid/x") }) {
                Text("GET invalid host (error)")
            }
        }
    }
}
