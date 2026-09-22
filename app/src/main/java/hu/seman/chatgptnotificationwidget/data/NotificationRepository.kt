package hu.seman.chatgptnotificationwidget.data

import android.app.Notification
import android.content.Context
import android.service.notification.StatusBarNotification
import hu.seman.chatgptnotificationwidget.widget.ChatGptWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class NotificationRepository(context: Context, private val database: AppDatabase, private val settings: SettingsRepository) {
    private val appContext = context.applicationContext
    fun observeAll(): Flow<List<NotificationEntity>> = database.notifications().observeAll()
    fun observeUnread(): Flow<List<NotificationEntity>> = database.notifications().observeUnread()
    suspend fun save(sbn: StatusBarNotification) {
        if (sbn.packageName != NotificationEntity.CHATGPT_PACKAGE) return
        val dao = database.notifications()
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val wasRead = dao.readState(sbn.key) ?: false
        dao.upsert(NotificationEntity(sbn.key, sbn.id, title, body, subText, sbn.postTime, isRead = wasRead))
        dao.trim(settings.settings.first().historyLimit)
        refreshWidgets()
    }
    suspend fun markAllRead() { database.notifications().markAllRead(); refreshWidgets() }
    suspend fun markRead(key: String) { database.notifications().markRead(key); refreshWidgets() }
    suspend fun clear() { database.notifications().clear(); refreshWidgets() }
    private fun refreshWidgets() = ChatGptWidgetProvider.refreshAll(appContext)
}