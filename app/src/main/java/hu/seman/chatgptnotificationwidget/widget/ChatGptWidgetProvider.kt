package hu.seman.chatgptnotificationwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import hu.seman.chatgptnotificationwidget.MainActivity
import hu.seman.chatgptnotificationwidget.R
import hu.seman.chatgptnotificationwidget.data.AppDatabase
import hu.seman.chatgptnotificationwidget.data.NotificationEntity
import hu.seman.chatgptnotificationwidget.data.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class ChatGptWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        appContext = context.applicationContext
        ids.forEach { update(context, manager, it) }
    }
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) = update(context, manager, id)
    companion object {
        private var appContext: Context? = null
        fun refreshAll() {
            val context = appContext ?: return
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, ChatGptWidgetProvider::class.java))
            ids.forEach { update(context, manager, it) }
        }
        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                val settings = SettingsRepository(context).settings.first()
                val items = AppDatabase.get(context).notifications().latest(3).filter { !settings.unreadOnly || !it.isRead }
                val unread = AppDatabase.get(context).notifications().unreadCount()
                val remote = RemoteViews(context.packageName, R.layout.widget_chatgpt)
                remote.setTextViewText(R.id.widget_unread, if (unread == 0) "" else "$unread új")
                val views = intArrayOf(R.id.widget_item_1, R.id.widget_item_2, R.id.widget_item_3)
                views.forEachIndexed { index, viewId ->
                    val item = items.getOrNull(index)
                    if (item == null) { remote.setViewVisibility(viewId, if (index == 0) View.VISIBLE else View.GONE); if (index == 0) remote.setTextViewText(viewId, "Még nincs ChatGPT értesítés.") }
                    else { remote.setViewVisibility(viewId, View.VISIBLE); remote.setTextViewText(viewId, format(item, settings.preview)); remote.setOnClickPendingIntent(viewId, actionIntent(context, WidgetActionReceiver.ACTION_OPEN, item.notificationKey, index)) }
                }
                remote.setOnClickPendingIntent(R.id.widget_title, openAppIntent(context))
                remote.setOnClickPendingIntent(R.id.widget_mark_read, actionIntent(context, WidgetActionReceiver.ACTION_READ_ALL, null, 40))
                remote.setOnClickPendingIntent(R.id.widget_refresh, actionIntent(context, WidgetActionReceiver.ACTION_REFRESH, null, 41))
                manager.updateAppWidget(id, remote)
            }
        }
        private fun format(item: NotificationEntity, showPreview: Boolean): String = buildString { append(if (item.isRead) "○ " else "● "); append(item.title.ifBlank { "ChatGPT" }); if (showPreview && item.body.isNotBlank()) append("\n").append(item.body) }
        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(context, 20, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        private fun actionIntent(context: Context, action: String, key: String?, request: Int): PendingIntent = PendingIntent.getBroadcast(context, request, Intent(context, WidgetActionReceiver::class.java).setAction(action).putExtra(WidgetActionReceiver.EXTRA_KEY, key), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
