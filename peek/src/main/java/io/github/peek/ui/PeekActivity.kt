package io.github.peek.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import io.github.peek.Peek
import io.github.peek.core.PeekLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PeekActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PeekApp()
            }
        }
    }
}

@Composable
private fun PeekApp() {
    var selectedId by remember { mutableStateOf<Long?>(null) }
    if (selectedId == null) {
        PeekListScreen(onLogClick = { selectedId = it })
    } else {
        PeekDetailScreen(logId = selectedId!!, onBack = { selectedId = null })
    }
}

// ─── List ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeekListScreen(onLogClick: (Long) -> Unit) {
    val logs by Peek.repository().observeAll().collectAsState(initial = emptyList())
    val scope   = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title   = { Text("Peek") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val intent = Peek.buildShareIntent()
                                context.startActivity(
                                    Intent.createChooser(intent, "Share Peek logs")
                                )
                            }
                        },
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share as file")
                    }
                    IconButton(onClick = { scope.launch { Peek.clear() } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(logs, key = { it.id }) { log ->
                LogRow(log = log, onClick = { onLogClick(log.id) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LogRow(log: PeekLog, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(log.statusColor()),
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = log.method ?: "---",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp,
                    color      = log.methodColor(),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text       = log.statusCode?.toString() ?: "...",
                    fontWeight = FontWeight.Medium,
                    fontSize   = 12.sp,
                    color      = log.statusColor(),
                )
                Spacer(Modifier.width(8.dp))
                log.durationMs?.let {
                    Text(
                        text     = "${it}ms",
                        fontSize = 12.sp,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text     = log.path ?: log.endpoint,
                fontSize = 14.sp,
                maxLines = 1,
            )
            log.host?.let {
                Text(
                    text     = it,
                    fontSize = 12.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text     = formatTime(log.date),
            fontSize = 11.sp,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ─── Detail ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeekDetailScreen(logId: Long, onBack: () -> Unit) {
    var log by remember { mutableStateOf<PeekLog?>(null) }
    LaunchedEffect(logId) { log = Peek.repository().findById(logId) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title          = {
                    Text(
                        "${log?.method.orEmpty()} ${log?.path ?: log?.endpoint ?: ""}",
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        log?.let { l ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Section("Endpoint", l.endpoint)
                Section("Date",     formatDateTime(l.date))
                l.method?.let     { Section("Method", it) }
                l.statusCode?.let { Section("Status", it.toString()) }
                l.durationMs?.let { Section("Duration", "$it ms") }
                HorizontalDivider()
                l.requestHeaders?.let { Section("Request Headers", it) }
                l.requestBody?.let    { Section("Request Body", it) }
                HorizontalDivider()
                l.responseHeaders?.let { Section("Response Headers", it) }
                l.responseBody?.let    { Section("Response Body", it) }
                l.error?.let           { Section("Error", it) }
            }
        }
    }
}

@Composable
private fun Section(title: String, body: String) {
    Column {
        Text(
            text       = title,
            fontWeight = FontWeight.Bold,
            fontSize   = 13.sp,
            color      = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = body,
            fontSize   = 13.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun PeekLog.statusColor(): Color = when {
    isError   -> Color(0xFFE53935)
    isPending -> Color(0xFFFFA726)
    isSuccess -> Color(0xFF43A047)
    else      -> Color.Gray
}

private fun PeekLog.methodColor(): Color = when (method) {
    "GET"    -> Color(0xFF1976D2)
    "POST"   -> Color(0xFF43A047)
    "PUT"    -> Color(0xFFFFA726)
    "DELETE" -> Color(0xFFE53935)
    else     -> Color.Gray
}

private val timeFmt = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
private val dateFmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

private fun formatTime(ms: Long)     = timeFmt.format(Date(ms))
private fun formatDateTime(ms: Long) = dateFmt.format(Date(ms))
