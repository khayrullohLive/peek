package io.github.peek.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import io.github.peek.core.PeekLog
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports captured logs to a JSON file and shares it via Android's
 * share sheet (email, Telegram, Drive, etc.) using FileProvider.
 */
internal object PeekShareManager {

    private val fileNameFmt = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val isoFmt      = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    /**
     * Writes logs to a JSON file in {cacheDir}/peek-exports/ and returns
     * an Intent that can be used with startActivity(Intent.createChooser(...)).
     */
    fun buildShareIntent(context: Context, logs: List<PeekLog>): Intent {
        val file = writeLogsToFile(context, logs)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.peek.fileprovider",
            file,
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Peek network logs (${logs.size} entries)")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun writeLogsToFile(context: Context, logs: List<PeekLog>): File {
        val exportDir = File(context.cacheDir, "peek-exports").apply { mkdirs() }
        val file = File(exportDir, "peek-logs-${fileNameFmt.format(Date())}.json")

        val array = JSONArray()
        for (log in logs) {
            array.put(log.toJson())
        }

        file.writeText(array.toString(2))
        return file
    }

    private fun PeekLog.toJson(): JSONObject = JSONObject().apply {
        put("endpoint",        endpoint)
        put("date",            isoFmt.format(Date(date)))
        putOpt("method",       method)
        putOpt("host",         host)
        putOpt("path",         path)
        putOpt("statusCode",   statusCode)
        putOpt("durationMs",   durationMs)
        putOpt("requestHeaders",  requestHeaders)
        putOpt("requestBody",     requestBody)
        putOpt("responseHeaders", responseHeaders)
        putOpt("responseBody",    responseBody)
        putOpt("error",        error)
    }
}
