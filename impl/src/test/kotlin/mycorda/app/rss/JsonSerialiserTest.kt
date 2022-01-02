package mycorda.app.rss

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mycorda.app.helpers.random
import mycorda.app.types.NotRequired
import mycorda.app.types.StringList
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class JsonSerialiserTest {
    private val serialiser = JsonSerialiser()
    private val random = Random()

    @Test
    fun `should be a simple demo`() {
        val serialiser = JsonSerialiser()
        val aUUID = UUID.randomUUID()

        // wire formt
        val serialised = serialiser.serialiseData(aUUID)

        // a packet that can hold any data type
        val deserialisedPacket = serialiser.deserialiseData(serialised)

        // get the actual value from the packet
        val roundTrippped = deserialisedPacket.value()

        assertThat(roundTrippped, equalTo(aUUID))
    }

    @Test
    fun `should round-trip data`() {
        val examples = listOf(
            // scalars
            Colour.random(),
            Weapon.random(),
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
            DemoException("opps!"),

            // Nothing
            // these have ther own tests as the assertions are different
        )

        examples.forEach {
            try {
                val roundTripped = roundTrip(it)
                if (it is Exception) {
                    // internal stack trace doesn't serialize exactly, so object equality
                    // fails
                    assertThat(
                        it.message ?: "",
                        equalTo((roundTripped as Exception).message ?: "")
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                } else {
                    assertThat(
                        it,
                        equalTo(roundTripped)
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                fail("Exception ${ex.message} for round-trip $it of class ${it::class.qualifiedName}")
            }
        }
    }

    @Test
    fun `should round-trip Unit`() {
        val roundTripped = roundTrip(Unit)
        assert(roundTripped is Unit)
    }

    @Test
    fun `should round-trip NotRequired`() {
        val roundTripped = roundTrip(NotRequired.instance())
        assert(roundTripped is NotRequired)
    }

    @Test
    fun `should not serialize unsupported types`() {
        assertThrows<RuntimeException> {serialiser.serialiseData(BadModel())}
        assertThrows<RuntimeException> {serialiser.serialiseData(BadEnum.random())}
    }


    @Test
    fun `should map to SerialisationPacket`() {
        val examples = listOf(
            // scalars
            Colour.random(),
            Weapon.random(),
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
            DemoException("opps!"),

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