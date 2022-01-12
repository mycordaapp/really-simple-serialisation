package mycorda.app.tasks.serialisation;

import mycorda.app.types.NotRequired
import mycorda.app.types.UniqueId
import java.math.BigDecimal
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf


/**
 * Helpers for Kotlin/Java reflections
 */
class ReflectionsSupport {

    companion object {
        // the allowed scalar values
        fun isScalar(clazz: KClass<*>): Boolean {
            return (clazz == Int::class)
                    || (clazz == Long::class)
                    || (clazz == Double::class)
                    || (clazz == String::class)
                    || (clazz == Float::class)
                    || (clazz == Boolean::class)
                    || (clazz == String::class)
                    || (clazz == UUID::class)
                    || (clazz == UniqueId::class)
                    || (clazz == BigDecimal::class)
        }

        fun isEnum(type: KClass<out Any>) = type.isSubclassOf(Enum::class)


        fun isUnit(clazz: KClass<*>): Boolean {
            return (clazz == Unit::class)
                    || (clazz == Nothing::class)
        }

        fun isNotRequired(clazz: KClass<*>): Boolean {
            return (clazz == NotRequired::class)
        }

        fun isDataClass(clazz: KClass<*>): Boolean {
            return (clazz.isData)
        }

        fun isException(clazz: KClass<*>): Boolean {
            return (clazz.isSubclassOf(Exception::class))
        }

        fun isRawMap(clazz: KClass<*>): Boolean {
            return (clazz.isSubclassOf(Map::class))
        }

        fun isListSubclass(clazz: KClass<*>): Boolean {
            return (clazz.isSubclassOf(List::class))
        }

        fun isSupportedType(clazz: KClass<*>): Boolean {
            return isScalar(clazz) ||
                    isEnum(clazz) ||
                    isNotRequired(clazz) ||
                    isDataClass(clazz) ||
                    isException(clazz) ||
                    isListSubclass(clazz)
        }

//        fun isMap(clazz: KClass<*>): Boolean {
//            return (clazz.isSubclassOf(Map::class))
//        }
//        fun isMapOfStrings(any: Any): Boolean {
//            return if (any is Map<*, *>) {
//                any.filterKeys { !(it is String) }.isEmpty()
//            } else {
//                false
//            }
//        }

        fun isRawList(clazz: KClass<out Any>): Boolean {
            return (clazz == ArrayList::class) || (clazz == LinkedList::class)
        }

        fun forClass(clazzName: String): KClass<Any> {
            @Suppress("UNCHECKED_CAST")
            return when (clazzName) {
                "kotlin.Int" -> 1::class
                "kotlin.Long" -> 1L::class
                "kotlin.Double" -> 123.0::class
                "kotlin.Float" -> 123.0f::class
                "kotlin.Boolean" -> true::class
                "kotlin.String" -> ""::class
                "kotlin.Unit" -> Unit::class
                else -> Class.forName(clazzName).kotlin
            } as KClass<Any>
        }

        fun deserialiseScalar(data: String, clazz: KClass<Any>): Any {
            return when (clazz.simpleName) {
                "Int" -> data.toInt()
                "Long" -> data.toLong()
                "Double" -> data.toDouble()
                "Float" -> data.toFloat()
                "Boolean" -> data.toBoolean()
                "BigDecimal" -> data.toBigDecimal()
                "String" -> data
                "UUID" -> UUID.fromString(data)
                "UniqueId" -> UniqueId.fromString(data)
                "Unit" -> Unit
                else -> {
                    if (isEnum(clazz)) {
                        val method = clazz.java.getMethod("valueOf", String::class.java)
                        val enum = method.invoke(null, data)
                        enum
                        //throw RuntimeException("how to build an enum")
                    } else {
                        throw RuntimeException("don't know what to do with $clazz")
                    }
                }
            }
        }

        fun deserialiseNothing(clazzName: String): Any {
            return when (clazzName) {
                "kotlin.Unit" -> Unit
                "mycorda.app.types.NotRequired" -> NotRequired.instance()
                else -> {
                    throw RuntimeException("don't know what to do with $clazzName")
                }
            }
        }
    }

}