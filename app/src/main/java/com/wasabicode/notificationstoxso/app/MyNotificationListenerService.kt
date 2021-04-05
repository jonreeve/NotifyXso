package com.wasabicode.notificationstoxso.app

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.wasabicode.notificationstoxso.server.types.MyNotification
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.*

class MyNotificationListenerService(dispatchers: Dispatchers = Dispatchers) : NotificationListenerService() {

    lateinit var config: Configuration
    lateinit var httpClient: HttpClient
    private val coroutineScope: CoroutineScope = CoroutineScope(dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        config = (applicationContext as App).configuration
        httpClient = (applicationContext as App).httpClient
    }

    override fun onDestroy() {
        coroutineScope.cancel()
    }

    @Suppress("DEPRECATION")
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        val extras = statusBarNotification.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val content = extras.getString(Notification.EXTRA_TEXT) ?: ""
        Log.i("JONDEBUG", "Notification seen, title: $title, content: $content")
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.w("JONDEBUG", "Exception!", throwable)
        }
        if (config.enabled) {
            coroutineScope.launch(coroutineExceptionHandler) {
                Log.i("JONDEBUG", "Sending to ${config.host}:${config.port}")
                httpClient.put<Unit>(
                    host = config.host,
                    port = config.port,
                    body = MyNotification(
                        titleRtf = title,
                        contentRtf = content,
                        durationSecs = config.durationSecs
                    )
                ) {
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }
}