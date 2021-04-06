package com.wasabicode.notificationstoxso.app

import android.app.Notification
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import com.wasabicode.notificationstoxso.app.config.Configuration
import com.wasabicode.notificationstoxso.server.types.MyNotification
import com.wasabicode.notificationstoxso.server.types.MyNotification.Icon
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.math.max


class MyNotificationListenerService(dispatchers: Dispatchers = Dispatchers) : NotificationListenerService() {

    lateinit var config: Configuration
    lateinit var httpClient: HttpClient
    private val coroutineScope: CoroutineScope = CoroutineScope(dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        config = (applicationContext as App).configuration
        httpClient = (applicationContext as App).httpClient
    }

    override fun onDestroy() {
        coroutineScope.cancel()
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val extras = statusBarNotification.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = extras.getString(Notification.EXTRA_TEXT) ?: ""

        if (title.isBlank() && content.isBlank()) {
            Log.i(LOG_TAG, "Notification seen, blank title + content so not forwarding")
            return
        }
        if (config.exclusions.any { title.contains(it) || content.contains(it) }) {
            Log.i(LOG_TAG, "Notification seen, matches exclusion list so not forwarding (title: $title content: $content)")
            return
        }

        val iconBitmap: Bitmap? =
            if (config.preferredIcon == Icon.Custom::class) statusBarNotification.notification.createIconBitmap(this) else null
        Log.i(LOG_TAG, "Notification seen, title: $title, content: $content")
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.w(LOG_TAG, "Exception!", throwable)
        }
        Log.i(LOG_TAG, "Sending to ${config.host}:${config.port}")
        if (config.enabled) {
            coroutineScope.launch(coroutineExceptionHandler) {
                httpClient.put<Unit>(
                    host = config.host,
                    port = config.port,
                    body = MyNotification(
                        titleRtf = title,
                        contentRtf = content,
                        durationSecs = config.durationSecs,
                        icon = iconBitmap?.toCustomIcon() ?: config.preferredIcon.objectInstance ?: Icon.Default
                    )
                ) {
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }

    private fun Notification.createIconBitmap(context: Context): Bitmap? {
        // TODO consider using extras.getInt(Notification.EXTRA_PICTURE) if present, for surfacing richer content

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (getLargeIcon() ?: smallIcon)?.loadDrawable(context)?.toBitmap()
        } else {
            @Suppress("DEPRECATION") // for old versions, so we need to use those
            val iconId = extras.getInt(Notification.EXTRA_LARGE_ICON).takeIf { it != 0 } ?: extras.getInt(Notification.EXTRA_SMALL_ICON)
            val packageContext = context.createPackageContext(packageName, CONTEXT_IGNORE_SECURITY)
            ContextCompat.getDrawable(packageContext, iconId)?.toBitmap()
        }
    }

    private fun Drawable.toBitmap(): Bitmap? {
        (this as? BitmapDrawable)?.bitmap?.let { return it }

        return if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            val largestSide = max(intrinsicWidth, intrinsicHeight)
            val scale = (MAX_ICON_SIZE / largestSide).coerceAtMost(1.0f)
            Log.v(LOG_TAG, "scaling $scale (was $intrinsicWidth x $intrinsicHeight)")
            val bitmap = Bitmap.createBitmap((intrinsicWidth * scale).toInt(), (intrinsicHeight * scale).toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            setBounds(0, 0, (canvas.width * scale).toInt(), (canvas.height * scale).toInt())
            draw(canvas)
            bitmap
        } else null
    }

    private fun Bitmap.toCustomIcon(): Icon.Custom = Icon.Custom(this.toBase64())

    private fun Bitmap.toBase64(): String {
        val buffer = ByteBuffer.allocate(byteCount)
        copyPixelsToBuffer(buffer)
        return Base64.encodeToString(buffer.array(), 0)
    }
}

private const val LOG_TAG = "NotificationListener"
private const val MAX_ICON_SIZE = 96f
