package hu.seman.chatgptnotificationwidget

import hu.seman.chatgptnotificationwidget.data.NotificationEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationRuleTest {
    @Test fun chatGptPackageIsAccepted() = assertTrue(isAccepted(NotificationEntity.CHATGPT_PACKAGE))
    @Test fun anotherPackageIsRejected() = assertFalse(isAccepted("com.example.other"))
    @Test fun equalKeyModelsAnUpdateInsteadOfDuplicate() { val first = NotificationEntity("key", 1, "A", "B", null, 1); assertEquals(first.notificationKey, first.copy(body = "C").notificationKey) }
    @Test fun readStateCanChange() = assertTrue(NotificationEntity("key", 1, "A", "B", null, 1).copy(isRead = true).isRead)
    private fun isAccepted(packageName: String) = packageName == NotificationEntity.CHATGPT_PACKAGE
}
