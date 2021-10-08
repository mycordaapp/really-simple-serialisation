package mycorda.app.rss

import mycorda.app.tasks.serialisation.ReflectionsSupport
import mycorda.app.types.NotRequired
import java.lang.Exception
import kotlin.reflect.KClass

/**
 * Represents what can be passed. Only one of the 5 options
 * can be filled in
 */
data class SerialisationPacket(
    /**
     * Represents the various types of "nothing", such as Unit
     */
    val nothingClazz: KClass<out Any>? = null,

    /**
     * Any of the supported scalar types
     */
    val scalar: Any? = null,

    /**
     * A data class. MUST be a kotlin data class
     */
    val data: Any? = null,

    /**
     * A list, but CANNOT be a raw list, i.e. List<String> to
     * avoid problems with erasures in generic
     */
    val list: Any? = null,

    /**
     * An exception. Any concrete class that extends from Exception is allowed
     */
    val exception: Exception? = null
) {
    private val clazzName: String

    init {
        // can have at most one option
        assert(all().filterNotNull().size == 1)
        clazzName = if (nothingClazz != null) {
            nothingClazz.qualifiedName!!
        } else {
            any()::class.qualifiedName!!
        }
    }

    private fun all(): List<Any?> = listOf(nothing(), scalar, data, list, exception)
    private fun values(): List<Any?> = listOf(scalar, data, list)

    private fun nothing(): Any? =
        if (isNothing()) ReflectionsSupport.deserialiseNothing(nothingClazz!!.qualifiedName!!) else null

    fun isNothing() = nothingClazz != null
    fun nothingClazz() = nothingClazz!!
    fun isValue() = values().filterNotNull().size == 1
    fun value(): Any = values().single { it != null }!!
    fun any(): Any = all().single { it != null }!!
    fun isException() = exception != null
    fun exception() = exception!!

    fun clazz(): KClass<out Any> {
        return nothingClazz ?: any()::class
    }

    fun clazzName(): String = clazzName


    companion object {
        fun create(data: Any): SerialisationPacket {
            val clazz = data::class

            if (data is Unit) return SerialisationPacket(nothingClazz = Unit::class)
            if (data is Nothing) return SerialisationPacket(nothingClazz = Nothing::class)
            if (data is NotRequired) return SerialisationPacket(nothingClazz = NotRequired::class)

            if (ReflectionsSupport.isRawList(clazz)) throw RuntimeException("Raw List classes are not allowed. Must use a subclass")
            if (ReflectionsSupport.isScalar(clazz)) return SerialisationPacket(scalar = data)
            if (ReflectionsSupport.isDataClass(clazz)) return SerialisationPacket(data = data)
            if (ReflectionsSupport.isListSubclass(clazz)) return SerialisationPacket(list = data)
            if (ReflectionsSupport.isException(clazz)) return SerialisationPacket(exception = data as Exception)
            if (ReflectionsSupport.isEnum(clazz)) return SerialisationPacket(scalar = data)

            throw RuntimeException("Don't know how to serialise class: ${data::class.qualifiedName}")
        }
    }
}