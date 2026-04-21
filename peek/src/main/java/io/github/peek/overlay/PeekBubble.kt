package io.github.peek.overlay

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.peek.Peek
import io.github.peek.ui.PeekActivity
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

private val BubbleGreen   = Color(0xFF1B6840)
private val PauseRed      = Color(0xFFB71C1C)

/**
 * Draggable floating bubble with two buttons:
 *  🔍 [N]   — open inspector (tap)
 *  ▶ / ⏸   — start / pause logging (toggle)
 *
 * Place inside a root Box alongside your NavHost:
 * ```
 * Box(Modifier.fillMaxSize()) {
 *     YourNavHost()
 *     PeekBubble()
 * }
 * ```
 */
@Composable
fun PeekBubble() {
    val context  = LocalContext.current
    val isLogging by Peek.isLogging

    val logCount by remember {
        Peek.repository().observeAll().map { it.size }
    }.collectAsState(initial = 0)

    var offsetX by remember { mutableFloatStateOf(-16f) }
    var offsetY by remember { mutableFloatStateOf(300f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) },
        contentAlignment = Alignment.TopEnd,
    ) {
        Row(
            modifier = Modifier
                .padding(end = 16.dp)
                .clip(CircleShape)
                .background(Color.Green.copy(alpha = 0.65f))
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Inspect button — log sonini ko'rsatadi ────────────────────
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        context.startActivity(
                            Intent(context, PeekActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text  = "$logCount",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),

                )
            }

            // ── Start / Pause toggle ──────────────────────────────────────
            IconButton(
                onClick  = { Peek.toggle() },
                modifier = Modifier.size(36.dp),
                colors   = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isLogging) BubbleGreen else PauseRed,
                ),
            ) {
                Icon(
                    imageVector        = if (isLogging) Icons.Default.Pause
                                        else Icons.Default.PlayArrow,
                    contentDescription = if (isLogging) "Pause logging"
                                        else "Start logging",
                )
            }
        }
    }
}
