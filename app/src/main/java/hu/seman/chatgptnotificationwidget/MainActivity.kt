package hu.seman.chatgptnotificationwidget

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import hu.seman.chatgptnotificationwidget.data.*
import hu.seman.chatgptnotificationwidget.widget.ChatGptWidgetProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MaterialTheme { WidgetApp(this) } } }
}

private class AppViewModel(private val repository: NotificationRepository, private val settingsRepository: SettingsRepository) : ViewModel() {
    val notifications = repository.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val settings = settingsRepository.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WidgetSettings())
    fun clear() = viewModelScope.launch { repository.clear() }
    fun readAll() = viewModelScope.launch { repository.markAllRead() }
    fun limit(value: Int) = viewModelScope.launch { settingsRepository.setLimit(value) }
    fun unreadOnly(value: Boolean) = viewModelScope.launch { settingsRepository.setUnreadOnly(value) }
    fun preview(value: Boolean) = viewModelScope.launch { settingsRepository.setPreview(value) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun WidgetApp(activity: MainActivity) {
    val vm: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = object : ViewModelProvider.Factory { override fun <T : ViewModel> create(modelClass: Class<T>): T { val db = AppDatabase.get(activity); @Suppress("UNCHECKED_CAST") return AppViewModel(NotificationRepository(db, SettingsRepository(activity)), SettingsRepository(activity)) as T } })
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf("home") }
    val access = notificationAccessGranted(activity)
    Scaffold(topBar = { TopAppBar(title = { Text(if (screen == "home") "ChatGPT értesítések" else "Beállítások") }, navigationIcon = { if (screen == "settings") { TextButton(onClick = { screen = "home" }) { Text("‹ Vissza") } } }) }) { padding ->
        if (screen == "settings") SettingsScreen(settings, vm, Modifier.padding(padding)) else HomeScreen(notifications, access, { activity.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) }, { screen = "settings" }, vm, Modifier.padding(padding))
    }
}

@Composable private fun HomeScreen(items: List<NotificationEntity>, access: Boolean, enable: () -> Unit, settings: () -> Unit, vm: AppViewModel, modifier: Modifier) = LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item { Card { Column(Modifier.padding(16.dp)) { Text(if (access) "Értesítés-hozzáférés engedélyezve" else "Értesítés-hozzáférés szükséges", style = MaterialTheme.typography.titleMedium); Text(if (access) "Csak a ChatGPT értesítéseit mentjük helyben." else "A widget csak az engedélyezés után tud ChatGPT értesítéseket gyűjteni.", style = MaterialTheme.typography.bodyMedium); if (!access) Button(onClick = enable, modifier = Modifier.padding(top = 8.dp)) { Text("Értesítés-hozzáférés engedélyezése") } } } }
    item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${items.count { !it.isRead }} olvasatlan", style = MaterialTheme.typography.titleMedium); Row { TextButton(onClick = vm::readAll) { Text("Mind olvasott") }; TextButton(onClick = vm::clear) { Text("Törlés") } } } }
    item { OutlinedButton(onClick = settings, modifier = Modifier.fillMaxWidth()) { Text("Beállítások és widget tartalma") } }
    if (items.isEmpty()) item { Text("Még nincs ChatGPT értesítés. A rendszer más alkalmazásait nem tároljuk.", style = MaterialTheme.typography.bodyLarge) }
    items(items, key = { it.notificationKey }) { item -> ListItem(headlineContent = { Text(item.title.ifBlank { "ChatGPT" }) }, supportingContent = { Text(item.body.ifBlank { "Nincs megjeleníthető szöveg" }) }, overlineContent = { Text(if (item.isRead) "Olvasott" else "Olvasatlan • " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.postedAt))) }) }
}

@Composable private fun SettingsScreen(state: WidgetSettings, vm: AppViewModel, modifier: Modifier) = Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text("Előzmények limitje", style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(50, 100, 250, 500).forEach { limit -> FilterChip(selected = state.historyLimit == limit, onClick = { vm.limit(limit) }, label = { Text(limit.toString()) }) } }
    HorizontalDivider(); Text("Widget tartalma", style = MaterialTheme.typography.titleMedium)
    Row(Modifier.clickable { vm.unreadOnly(false) }) { RadioButton(selected = !state.unreadOnly, onClick = { vm.unreadOnly(false) }); Text("Minden értesítés", Modifier.padding(top = 12.dp)) }
    Row(Modifier.clickable { vm.unreadOnly(true) }) { RadioButton(selected = state.unreadOnly, onClick = { vm.unreadOnly(true) }); Text("Csak olvasatlan", Modifier.padding(top = 12.dp)) }
    HorizontalDivider(); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Értesítési előnézet"); Text("A widgeten megjelenjen az üzenettörzs", style = MaterialTheme.typography.bodySmall) }; Switch(checked = state.preview, onCheckedChange = vm::preview) }
    Text("Megjelenés: a widget a rendszer világos/sötét témájához igazodik.", style = MaterialTheme.typography.bodySmall)
}

private fun notificationAccessGranted(activity: MainActivity): Boolean = Settings.Secure.getString(activity.contentResolver, "enabled_notification_listeners")?.contains(ComponentName(activity, hu.seman.chatgptnotificationwidget.service.ChatGptNotificationListener::class.java).flattenToString()) == true
