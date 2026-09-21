package hu.seman.chatgptnotificationwidget.service

import android.app.PendingIntent
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
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != NotificationEntity.CHATGPT_PACKAGE) return
        sbn.notification.contentIntent?.let { activeIntents[sbn.key] = it }
        scope.launch { NotificationRepository(AppDatabase.get(this@ChatGptNotificationListener), SettingsRepository(this@ChatGptNotificationListener)).save(sbn) }
    }
    override fun onNotificationRemoved(sbn: StatusBarNotification) { activeIntents.remove(sbn.key) }
    companion object {
        private val activeIntents = ConcurrentHashMap<String, PendingIntent>()
        fun takeIntent(key: String): PendingIntent? = activeIntents[key]
    }
}
