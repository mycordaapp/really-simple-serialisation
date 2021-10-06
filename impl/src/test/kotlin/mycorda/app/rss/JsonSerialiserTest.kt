package mycorda.app.rss

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mycorda.app.helpers.random
import mycorda.app.types.NotRequired
import mycorda.app.types.StringList
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.lang.Exception
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class JsonSerialiserTest {
    private val serialiser = JsonSerialiser()
    private val random = Random()

    @Test
    fun `should round-trip data`() {
        val examples = listOf(
            Colour.random(),
            random.nextInt(),
            random.nextLong(),
            random.nextDouble(),
            random.nextFloat(),
            random.nextBoolean(),
            BigDecimal.valueOf(random.nextDouble()),
            String.random(10),
            UUID.randomUUID(),
            DemoModel(),
            StringList(listOf("Mary", "had", "a", "little", "lamb")),
            RuntimeException("This went wrong")
        )

        examples.forEach {
            try {
                val roundTripped = roundTrip(it)
                if (it is Exception) {
                    // internal stack trace doesn't serialize exactly, so object equality
                    // fails
                    assertThat(
                        it.message,
                        equalTo((roundTripped as Exception).message)
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                } else {
                    assertThat(
                        it,
                        equalTo(roundTripped)
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                }
            } catch (ex: Exception) {
                fail("Exception ${ex.message} for round-trip $it of class ${it::class.qualifiedName}")
            }
        }
    }

    @Test
    fun `should map to SerialisationPacket`() {
        val examples = listOf(
            // scalars
            random.nextInt(),
            random.nextLong(),
            random.nextDouble(),
            random.nextFloat(),
            random.nextBoolean(),
            BigDecimal.valueOf(random.nextDouble()),
            String.random(10),
            UUID.randomUUID(),

            // data class
            DemoModel(),

            // list
            StringList(listOf("Mary", "had", "a", "little", "lamb")),

            // exceptions
            RuntimeException("This went wrong"),
            DemoException(),

            // Nothing
            Unit,
            NotRequired()
        )

        examples.forEach {
            try {
                SerialisationPacket.create(it)
            } catch (ex: Exception) {
                fail("Exception ${ex.message} for mapDataToSerialisationPacket $it of class ${it::class.qualifiedName}")
            }
        }
    }

    @Test
    fun `should not map to SerialisationPacket for unsupported types`() {
        val examples = listOf(
            Pair(ArrayList<String>(), "Raw List classes are not allowed. Must use a subclass"),
            Pair(Date(), "Don't know how to serialise class: java.util.Date"),
            Pair(
                mapOf<String, Any>("name" to "Paul"),
                "Don't know how to serialise class: java.util.Collections.SingletonMap"
            )
        )

        examples.forEach {
            try {
                SerialisationPacket.create(it.first)
                fail("should have thrown an exception")
            } catch (ex: Exception) {
                assertThat(ex.message, equalTo(it.second))
            }
        }
    }


    private fun roundTrip(data: Any): Any {
        val serialised = serialiser.serialiseData(data)
        return serialiser.deserialiseData(serialised).any()
    }
}