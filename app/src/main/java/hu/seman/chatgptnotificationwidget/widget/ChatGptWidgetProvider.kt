package hu.seman.chatgptnotificationwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import hu.seman.chatgptnotificationwidget.MainActivity
import hu.seman.chatgptnotificationwidget.R
import hu.seman.chatgptnotificationwidget.data.AppDatabase
import hu.seman.chatgptnotificationwidget.data.NotificationEntity
import hu.seman.chatgptnotificationwidget.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

open class ChatGptWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = refreshAll(context)
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: android.os.Bundle) = update(context.applicationContext, manager, id)

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val providerClasses = listOf(ChatGptWidgetProvider::class.java, CompactChatGptWidgetProvider::class.java, DetailedChatGptWidgetProvider::class.java)

        /** Uses the caller context, so refresh survives process recreation and receiver-only starts. */
        fun refreshAll(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            providerClasses.forEach { providerClass ->
                manager.getAppWidgetIds(ComponentName(appContext, providerClass)).forEach { update(appContext, manager, it) }
            }
        }

        private fun update(context: Context, manager: AppWidgetManager, id: Int) {
            scope.launch {
                val settings = SettingsRepository(context).settings.first()
                val options = manager.getAppWidgetOptions(id)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 180)
                val rowCount = WidgetLayoutPolicy.visibleRows(minWidth, minHeight)
                val dao = AppDatabase.get(context).notifications()
                val items = if (settings.unreadOnly) dao.latestUnread(rowCount) else dao.latest(rowCount)
                val unread = dao.unreadCount()
                val remote = RemoteViews(context.packageName, R.layout.widget_chatgpt)
                remote.setTextViewText(R.id.widget_unread, if (unread == 0) "" else "$unread új")
                remote.setViewVisibility(R.id.widget_actions, if (WidgetLayoutPolicy.showActions(minHeight)) View.VISIBLE else View.GONE)
                val views = intArrayOf(R.id.widget_item_1, R.id.widget_item_2, R.id.widget_item_3)
                views.forEachIndexed { index, viewId ->
                    val item = items.getOrNull(index)
                    when {
                        index >= rowCount -> remote.setViewVisibility(viewId, View.GONE)
                        item == null && index == 0 -> { remote.setViewVisibility(viewId, View.VISIBLE); remote.setTextViewText(viewId, "Még nincs ChatGPT értesítés.") }
                        item == null -> remote.setViewVisibility(viewId, View.GONE)
                        else -> {
                            remote.setViewVisibility(viewId, View.VISIBLE)
                            remote.setTextViewText(viewId, format(item, settings.preview))
                            remote.setOnClickPendingIntent(viewId, actionIntent(context, WidgetActionReceiver.ACTION_OPEN, item.notificationKey, item.notificationKey.hashCode()))
                        }
                    }
                }
                remote.setOnClickPendingIntent(R.id.widget_title, openAppIntent(context))
                remote.setOnClickPendingIntent(R.id.widget_mark_read, actionIntent(context, WidgetActionReceiver.ACTION_READ_ALL, null, id * 10 + 1))
                remote.setOnClickPendingIntent(R.id.widget_refresh, actionIntent(context, WidgetActionReceiver.ACTION_REFRESH, null, id * 10 + 2))
                manager.updateAppWidget(id, remote)
            }
        }

        private fun format(item: NotificationEntity, showPreview: Boolean): String = buildString {
            append(if (item.isRead) "○ " else "● ")
            append(item.title.ifBlank { "ChatGPT" })
            if (showPreview && item.body.isNotBlank()) append("\n").append(item.body)
        }
        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(context, 20, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        private fun actionIntent(context: Context, action: String, key: String?, request: Int): PendingIntent {
            val intent = Intent(context, WidgetActionReceiver::class.java).setAction(action).setData(Uri.parse("chatgptwidget://$action/${key ?: request}")).putExtra(WidgetActionReceiver.EXTRA_KEY, key)
            return PendingIntent.getBroadcast(context, request, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    }
}