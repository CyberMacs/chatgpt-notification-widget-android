package hu.seman.chatgptnotificationwidget.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import hu.seman.chatgptnotificationwidget.data.AppDatabase
import hu.seman.chatgptnotificationwidget.data.NotificationEntity
import hu.seman.chatgptnotificationwidget.data.NotificationRepository
import hu.seman.chatgptnotificationwidget.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ChatGptNotificationListener : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        val currentChatGptNotifications = activeNotifications.filter { it.packageName == NotificationEntity.CHATGPT_PACKAGE }
        if (currentChatGptNotifications.isEmpty()) return
        scope.launch {
            val repository = NotificationRepository(this@ChatGptNotificationListener, AppDatabase.get(this@ChatGptNotificationListener), SettingsRepository(this@ChatGptNotificationListener))
            for (notification in currentChatGptNotifications) {
                notification.notification.contentIntent?.let { rememberIntent(notification.key, it) }
                repository.save(notification)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != NotificationEntity.CHATGPT_PACKAGE) return
        sbn.notification.contentIntent?.let { rememberIntent(sbn.key, it) }
        scope.launch { NotificationRepository(this@ChatGptNotificationListener, AppDatabase.get(this@ChatGptNotificationListener), SettingsRepository(this@ChatGptNotificationListener)).save(sbn) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) = Unit

    companion object {
        private const val INTENT_RETENTION_MS = 12 * 60 * 60 * 1000L
        private data class CapturedIntent(val intent: PendingIntent, val capturedAt: Long)
        private val activeIntents = ConcurrentHashMap<String, CapturedIntent>()

        fun requestRefresh(context: Context): Boolean = try {
            requestRebind(ComponentName(context.applicationContext, ChatGptNotificationListener::class.java))
            true
        } catch (_: Exception) { false }

        private fun rememberIntent(key: String, intent: PendingIntent) {
            val now = System.currentTimeMillis()
            activeIntents.entries.removeIf { now - it.value.capturedAt > INTENT_RETENTION_MS }
            activeIntents[key] = CapturedIntent(intent, now)
        }
        fun takeIntent(key: String): PendingIntent? {
            val captured = activeIntents[key] ?: return null
            return if (System.currentTimeMillis() - captured.capturedAt <= INTENT_RETENTION_MS) captured.intent else { activeIntents.remove(key); null }
        }
    }
}