package mycorda.app.rss


/**
 * Represents the four possible types of data to return
 */
sealed class DynamicValue

data class DynamicMap(val map: Map<String, Any?>) : DynamicValue()
data class DynamicList(val list: List<Any?>) : DynamicValue()
data class DynamicScalar(val scalar: Any) : DynamicValue()
object DynamicNull : DynamicValue()