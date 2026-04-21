package io.github.peek.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.github.peek.core.PeekLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object PeekShareManager {

    private val fileNameFmt = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val isoFmt      = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun buildShareIntent(context: Context, logs: List<PeekLog>): Intent {
        val file = writeLogsToFile(context, logs)

        // Use the host app's own FileProvider authority if available,
        // otherwise fall back to a simple file URI (works on API < 24 or
        // when no FileProvider is needed for internal cache sharing).
        val uri: Uri = try {
            // Try common authority patterns used by host apps
            val authorities = listOf(
                "${context.packageName}.provider",
                "${context.packageName}.fileprovider",
                "${context.packageName}.peek.fileprovider",
            )
            var resolved: Uri? = null
            for (authority in authorities) {
                try {
                    resolved = FileProvider.getUriForFile(context, authority, file)
                    break
                } catch (_: Exception) { }
            }
            resolved ?: Uri.fromFile(file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Peek network logs (${logs.size} entries)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writeLogsToFile(context: Context, logs: List<PeekLog>): File {
        val exportDir = File(context.cacheDir, "peek-exports").apply { mkdirs() }
        val file      = File(exportDir, "peek-logs-${fileNameFmt.format(Date())}.json")

        val array = JSONArray()
        for (log in logs) { array.put(log.toJson()) }
        file.writeText(array.toString(2))
        return file
    }

    private fun PeekLog.toJson(): JSONObject = JSONObject().apply {
        put("endpoint",           endpoint)
        put("date",               isoFmt.format(Date(date)))
        putOpt("method",          method)
        putOpt("host",            host)
        putOpt("path",            path)
        putOpt("statusCode",      statusCode)
        putOpt("durationMs",      durationMs)
        putOpt("requestHeaders",  requestHeaders)
        putOpt("requestBody",     requestBody)
        putOpt("responseHeaders", responseHeaders)
        putOpt("responseBody",    responseBody)
        putOpt("error",           error)
    }
}