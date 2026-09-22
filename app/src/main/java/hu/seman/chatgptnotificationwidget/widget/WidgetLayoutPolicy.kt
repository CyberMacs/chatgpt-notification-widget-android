package hu.seman.chatgptnotificationwidget.widget

/** Pure dp-based rules so every launcher receives a compact, readable widget layout. */
object WidgetLayoutPolicy {
    fun visibleRows(minWidthDp: Int, minHeightDp: Int): Int = when {
        minWidthDp < 180 || minHeightDp < 160 -> 1
        minHeightDp < 240 -> 2
        else -> 3
    }
    fun showActions(minHeightDp: Int): Boolean = minHeightDp >= 160
}