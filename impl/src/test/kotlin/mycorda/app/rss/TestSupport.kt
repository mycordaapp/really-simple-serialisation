package mycorda.app.rss

import mycorda.app.helpers.random
import mycorda.app.types.StringList
import java.io.File
import java.lang.RuntimeException
import java.util.*

enum class Colour {
    Red, Green, Blue;

    companion object {
        fun random(): Colour = Colour.values()[Random().nextInt(2)]
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
data class BadModel(val file: File = File(".") )

