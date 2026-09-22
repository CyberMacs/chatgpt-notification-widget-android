package hu.seman.chatgptnotificationwidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import hu.seman.chatgptnotificationwidget.data.*
import hu.seman.chatgptnotificationwidget.service.ChatGptNotificationListener
import kotlinx.coroutines.*

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val repository = NotificationRepository(context, AppDatabase.get(context), SettingsRepository(context))
            when (intent.action) {
                ACTION_READ_ALL -> repository.markAllRead()
                ACTION_OPEN -> {
                    val key = intent.getStringExtra(EXTRA_KEY).orEmpty()
                    repository.markRead(key)
                    try { ChatGptNotificationListener.takeIntent(key)?.send() ?: openChatGpt(context) } catch (_: Exception) { openChatGpt(context) }
                }
                ACTION_REFRESH -> {
                    ChatGptNotificationListener.requestRefresh(context)
                    ChatGptWidgetProvider.refreshAll(context)
                }
            }
            result.finish()
        }
    }
    private fun openChatGpt(context: Context) { context.packageManager.getLaunchIntentForPackage(NotificationEntity.CHATGPT_PACKAGE)?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }?.let(context::startActivity) }
    companion object { const val ACTION_READ_ALL = "widget.read_all"; const val ACTION_REFRESH = "widget.refresh"; const val ACTION_OPEN = "widget.open"; const val EXTRA_KEY = "key" }
}