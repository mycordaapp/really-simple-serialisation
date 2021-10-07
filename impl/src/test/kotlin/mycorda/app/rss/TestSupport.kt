package mycorda.app.rss

import mycorda.app.helpers.random
import java.lang.RuntimeException
import java.util.*

enum class Colour {
    Red, Green, Blue;

    companion object {
        fun random(): Colour = Colour.values()[Random().nextInt(2)]
    }
}

data class DemoModel(
    val string: String = String.random(80),
    val int: Int = Random().nextInt(),
    val long: Long = Random().nextLong(),
    val double: Double = Random().nextDouble(),
    val float: Float = Random().nextFloat(),
    val boolean: Boolean = Random().nextBoolean(),
    val colour: Colour = Colour.random(),
    val nested: DemoModel? = null
)

class DemoException(message : String) : RuntimeException(message)