package mycorda.app.rss

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mycorda.app.tasks.serialisation.ReflectionsSupport
import java.lang.Exception
import mycorda.app.types.*
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

class JsonSerialiser {
    private val mapper: ObjectMapper = ObjectMapper()

    init {
        val module = KotlinModule()
        mapper.registerModule(module)
    }

    fun deserialiseData(serialised: String): SerialisationPacket {
        val raw = mapper.readValue(serialised, SerialisationPacketWireFormat::class.java)
        val clazz = ReflectionsSupport.forClass(raw.clazzName)

        return when {
            raw.scalar != null -> {
                val scalar = ReflectionsSupport.deserialiseScalar(raw.scalar, clazz)
                SerialisationPacket.create(scalar)
            }
            raw.data != null -> {
                val data = mapper.readValue(raw.data, clazz.java)
                SerialisationPacket.create(data)
            }
            raw.list != null -> {
                val list = mapper.readValue(raw.list, clazz.java)
                SerialisationPacket.create(list)
            }
            raw.exception != null -> {
                val exception = mapper.readValue(raw.exception, clazz.java)
                SerialisationPacket.create(exception)
            }
            else -> {
                // only option left is one of the "nothing" types
                val nothing = ReflectionsSupport.deserialiseNothing(raw.clazzName)
                SerialisationPacket.create(nothing)
            }
        }
    }

    fun serialiseData(data: Any): String {
        val packet = SerialisationPacket.create(data)
        val wire = packetToWireFormat(packet)
        return mapper.writeValueAsString(wire)
    }

    private fun packetToWireFormat(packet: SerialisationPacket): SerialisationPacketWireFormat {
        return when {
            packet.scalar != null -> {
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), scalar = packet.scalar.toString())
            }
            packet.data != null -> {
                val json = mapper.writeValueAsString(packet.data)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), data = json)
            }
            packet.list != null -> {
                val json = mapper.writeValueAsString(packet.list)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), list = json)
            }
            packet.exception != null -> {
                val json = mapper.writeValueAsString(packet.exception)
                SerialisationPacketWireFormat(clazzName = packet.clazzName(), exception = json)
            }
            packet.nothingClazz != null -> {
                SerialisationPacketWireFormat(clazzName = packet.clazzName())
            }
            else -> {
                throw java.lang.RuntimeException("Cannot map SerialisationPacket: $packet")
            }
        }
    }
}

