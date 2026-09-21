package hu.seman.chatgptnotificationwidget.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("widget_settings")
data class WidgetSettings(val historyLimit: Int = 500, val unreadOnly: Boolean = false, val preview: Boolean = true)
class SettingsRepository(private val context: Context) {
    private object Keys { val limit = intPreferencesKey("history_limit"); val unreadOnly = booleanPreferencesKey("unread_only"); val preview = booleanPreferencesKey("preview") }
    val settings: Flow<WidgetSettings> = context.dataStore.data.map { WidgetSettings(it[Keys.limit] ?: 500, it[Keys.unreadOnly] ?: false, it[Keys.preview] ?: true) }
    suspend fun setLimit(value: Int) { context.dataStore.edit { it[Keys.limit] = value } }
    suspend fun setUnreadOnly(value: Boolean) { context.dataStore.edit { it[Keys.unreadOnly] = value } }
    suspend fun setPreview(value: Boolean) { context.dataStore.edit { it[Keys.preview] = value } }
}
