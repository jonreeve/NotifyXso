package com.wasabicode.notifyxso.app

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
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.server.types.MyNotification
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.nio.ByteBuffer
import kotlin.math.max


class MyNotificationListenerService(dispatchers: Dispatchers = Dispatchers) : NotificationListenerService() {

    private lateinit var httpClient: HttpClient
    private lateinit var configRepo: ConfigurationRepo
    private val coroutineScope: CoroutineScope = CoroutineScope(dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        httpClient = (applicationContext as App).httpClient
        configRepo = (application as App).configurationRepo
    }

    override fun onDestroy() {
        coroutineScope.cancel()
    }

    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val extras = statusBarNotification.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = extras.getString(Notification.EXTRA_TEXT) ?: ""

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.w(LOG_TAG, "Exception!", throwable)
        }

        val context = this
        coroutineScope.launch(coroutineExceptionHandler) {
            val config = configRepo.configuration.first()

            if (title.isBlank() && content.isBlank()) {
                Log.v(LOG_TAG, "Notification seen, blank title + content so not forwarding")
            }
            else if (config.exclusions.any { title.contains(it) || content.contains(it) }) {
                Log.v(LOG_TAG, "Notification seen, matches exclusion list so not forwarding (title: $title content: $content)")
            } else {
                Log.i(LOG_TAG, "Notification seen, title: $title, content: $content")

                if (config.enabled) {
                    Log.v(LOG_TAG, "Sending to ${config.host}:${config.port}")
                    val iconBitmapBase64 = when (config.preferredIcon) {
                        PreferredIcon.Custom -> statusBarNotification.notification.createIconBitmap(context)?.toBase64()
                        else -> null
                    }

                    httpClient.put {
                        url(host = config.host, port = config.port)
                        setBody(
                            MyNotification(
                                titleRtf = title,
                                contentRtf = content,
                                durationSecs = config.durationSecs,
                                icon = config.preferredIcon.toNotificationIcon(iconBitmapBase64)
                            )
                        )
                        contentType(ContentType.Application.Json)
                    }
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

    private fun Bitmap.toBase64(): String {
        val buffer = ByteBuffer.allocate(byteCount)
        copyPixelsToBuffer(buffer)
        return Base64.encodeToString(buffer.array(), 0)
    }
}

private const val LOG_TAG = "NotificationListener"
private const val MAX_ICON_SIZE = 96f
