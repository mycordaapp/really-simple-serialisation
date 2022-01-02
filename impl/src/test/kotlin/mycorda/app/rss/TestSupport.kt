package mycorda.app.rss

import mycorda.app.helpers.random
import mycorda.app.types.*
import java.io.File
import java.lang.RuntimeException
import java.util.*

enum class Colour {
    Red, Green, Blue;

    companion object {
        fun random(): Colour = values()[Random().nextInt(2)]
    }
}

enum class Weapon(val weaponName: String, val damage: Int) {
    Sword("Sword", 12),
    Axe("Axe", 13),
    Bow("Bow", 14);

    companion object {
        fun random(): Weapon = values()[Random().nextInt(2)]
    }
}

enum class BadEnum(val enumName: String, val bad: BadModel = BadModel()) {
    One("One"),
    Two("Two"),
    Three("Three");

    companion object {
        fun random(): BadEnum = values()[Random().nextInt(2)]
    }
}


class DemoException(message: String) : RuntimeException(message) {
    override fun equals(other: Any?): Boolean {
        return if (other is DemoException) {
            other.message == this.message
        } else false
    }

    override fun hashCode(): Int {
        return (super.hashCode())
    }
}

// Should include all valid types
data class DemoModel(
    val string: String = String.random(80),
    val int: Int = Random().nextInt(),
    val long: Long = Random().nextLong(),
    val double: Double = Random().nextDouble(),
    val float: Float = Random().nextFloat(),
    val boolean: Boolean = Random().nextBoolean(),
    val colour: Colour = Colour.random(),
    //val notRequired : NotRequired = NotRequired.instance(), // need to fix equality on NotRequired for tests to pass
    val stringList: StringList = StringList(listOf(String.random(), String.random(), String.random())),
    val exception: DemoException = DemoException("oops"),
    val nested: DemoModel? = null
)

// not serializable
data class BadModel(val file: File = File("."))


class MapModel(private val name: String) : ToMapOfAny {

    override fun toMap(): MapOfAny {
        return mapOf("name" to name).toMapOfAny()
    }

    companion object : FromMapOfAny<MapModel> {
        override fun fromMap(data: MapOfAny): MapModel {
            return MapModel(data["name"] as String)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MapModel) {
            other.name == this.name
        } else false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

