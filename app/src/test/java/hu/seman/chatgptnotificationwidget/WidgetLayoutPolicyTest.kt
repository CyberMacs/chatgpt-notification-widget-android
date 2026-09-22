package hu.seman.chatgptnotificationwidget

import hu.seman.chatgptnotificationwidget.widget.WidgetLayoutPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutPolicyTest {
    @Test fun compactWidgetShowsOneRowAndHidesActions() {
        assertEquals(1, WidgetLayoutPolicy.visibleRows(150, 140))
        assertFalse(WidgetLayoutPolicy.showActions(140))
    }
    @Test fun mediumWidgetShowsTwoRows() = assertEquals(2, WidgetLayoutPolicy.visibleRows(250, 180))
    @Test fun largeWidgetShowsThreeRowsAndActions() {
        assertEquals(3, WidgetLayoutPolicy.visibleRows(250, 260))
        assertTrue(WidgetLayoutPolicy.showActions(260))
    }
}