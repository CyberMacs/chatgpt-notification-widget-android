package hu.seman.chatgptnotificationwidget.data

import android.app.Notification
import android.service.notification.StatusBarNotification
import hu.seman.chatgptnotificationwidget.widget.ChatGptWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotificationRepository(private val database: AppDatabase, private val settings: SettingsRepository) {
    fun observeAll(): Flow<List<NotificationEntity>> = database.notifications().observeAll()
    fun observeUnread(): Flow<List<NotificationEntity>> = database.notifications().observeUnread()
    suspend fun save(sbn: StatusBarNotification) {
        if (sbn.packageName != NotificationEntity.CHATGPT_PACKAGE) return
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        database.notifications().upsert(NotificationEntity(sbn.key, sbn.id, title, body, subText, sbn.postTime))
        val limit = settings.settings.first().historyLimit
        database.notifications().trim(limit)
        ChatGptWidgetProvider.refreshAll()
    }
    suspend fun markAllRead() { database.notifications().markAllRead(); ChatGptWidgetProvider.refreshAll() }
    suspend fun markRead(key: String) { database.notifications().markRead(key); ChatGptWidgetProvider.refreshAll() }
    suspend fun clear() { database.notifications().clear(); ChatGptWidgetProvider.refreshAll() }
}
