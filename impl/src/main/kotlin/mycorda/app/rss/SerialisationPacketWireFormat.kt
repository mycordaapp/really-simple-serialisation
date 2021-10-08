package mycorda.app.rss

data class SerialisationPacketWireFormat(
    val scalar: String? = null,
    val data: String? = null,
    val list: String? = null,
    val exception: String? = null,
    val clazzName: String
) {
    private fun all(): List<Any?> = listOf(scalar, data, list, exception)
    fun any(): Any = all().single { it != null }!!
}