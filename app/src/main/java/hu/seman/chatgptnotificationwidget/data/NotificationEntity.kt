package hu.seman.chatgptnotificationwidget.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chatgpt_notifications", indices = [Index(value = ["notificationKey"], unique = true)])
data class NotificationEntity(
    @PrimaryKey val notificationKey: String,
    val notificationId: Int,
    val title: String,
    val body: String,
    val subText: String?,
    val postedAt: Long,
    val packageName: String = CHATGPT_PACKAGE,
    val isRead: Boolean = false
) {
    companion object { const val CHATGPT_PACKAGE = "com.openai.chatgpt" }
}
