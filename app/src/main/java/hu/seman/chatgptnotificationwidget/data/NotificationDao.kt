package hu.seman.chatgptnotificationwidget.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM chatgpt_notifications ORDER BY postedAt DESC") fun observeAll(): Flow<List<NotificationEntity>>
    @Query("SELECT * FROM chatgpt_notifications WHERE isRead = 0 ORDER BY postedAt DESC") fun observeUnread(): Flow<List<NotificationEntity>>
    @Query("SELECT * FROM chatgpt_notifications ORDER BY postedAt DESC LIMIT :limit") suspend fun latest(limit: Int): List<NotificationEntity>
    @Query("SELECT COUNT(*) FROM chatgpt_notifications WHERE isRead = 0") suspend fun unreadCount(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: NotificationEntity)
    @Query("UPDATE chatgpt_notifications SET isRead = 1") suspend fun markAllRead()
    @Query("UPDATE chatgpt_notifications SET isRead = 1 WHERE notificationKey = :key") suspend fun markRead(key: String)
    @Query("DELETE FROM chatgpt_notifications") suspend fun clear()
    @Query("DELETE FROM chatgpt_notifications WHERE notificationKey IN (SELECT notificationKey FROM chatgpt_notifications ORDER BY postedAt DESC LIMIT -1 OFFSET :keep)") suspend fun trim(keep: Int)
}
