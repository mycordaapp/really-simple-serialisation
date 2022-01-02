package mycorda.app.rss

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mycorda.app.types.*
import org.junit.jupiter.api.Test
import java.util.*

/**
 * We can use to the ToMapOfAny and FromMapOfAny
 * convention to create kotlin classes that are serialisable
 * by RSS.
 *
 * In the example below there are multiple ways of expressing a Colours. This is similar
 * to the ideas in CSS template
 */

sealed class Colours : ToMapOfAny {
    companion object : FromMapOfAny<Colours> {
        override fun fromMap(data: MapOfAny): Colours {
            return if (data.containsKey("name")) {
                Named(Colour.valueOf(data["name"] as String))
            } else {
                RGB(data.unpackInt("r"), data.unpackInt("g"), data.unpackInt("b"))
            }
        }
    }
}

// A colour in RGB terms
data class RGB(val r: Int, val g: Int, val b: Int) : Colours() {
    override fun toMap(): MapOfAny {
        return mapOf("r" to r, "g" to g, "b" to b).toMapOfAny()
    }
}

// A colour defined by a name
data class Named(val name: Colour) : Colours() {
    override fun toMap(): MapOfAny {
        return mapOf("name" to name).toMapOfAny()
    }
}

class SealedClassSerialiserTest {
    private val serialiser = JsonSerialiser()
    private val random = Random()

    @Test
    fun `should serialise RGB colours`() {
        val rgb = RGB(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        val asMap = rgb.toMap()
        val roundTripped = roundTrip(asMap)

        assertThat(rgb, equalTo(Colours.fromMap(roundTripped)))
    }

    @Test
    fun `should serialise named colours`() {
        val rgb = Named(Colour.Green)
        val asMap = rgb.toMap()
        val roundTripped = roundTrip(asMap)

        assertThat(rgb, equalTo(Colours.fromMap(roundTripped)))
    }


    private fun roundTrip(data: Any): MapOfAny {
        val serialised = serialiser.serialiseData(data)
        return serialiser.deserialiseData(serialised).map!!
    }
}