# Really Simple Serialization (rss)

[![Circle CI](https://circleci.com/gh/mycordaapp/really-simple-serialisation.svg?style=shield)](https://circleci.com/gh/mycordaapp/really-simple-serialisation)
[![Licence Status](https://img.shields.io/github/license/mycordaapp/really-simple-serialisation)](https://github.com/mycordaapp/really-simple-serialisation/blob/master/licence.txt)

Alternatively, '**yafs**' - "yet another !@#$%^&* serialiser".

## Why RSS?

There is absolutely nothing wrong with modern Java/Kotlin serialisers technically (in fact rss embeds
[Jackson](https://github.com/FasterXML/jackson)), but at scale they have some annoying problems. These include:

* ambiguities over the actual wire format due the use of reflections magic and differing opinions as to the best mapping
  rules. At best these result in minor differences creeping in overtime between versions. At worst there are such
  significant gaps in the formats expected by different clients and libraries that significant development time is
  expended on translation layers.
* no commonly agreed rules for packaging different result types - for example how is a single scalar best represented (
  its not valid json on its own) - or an exception. Most applications layer some convention around the core serialisers
  to solve these problem.
* loss of type data. Java class serialisers assume that the Java/Kotlin class is available to reconstruct the data and
  need the schema information derived from the class for this. This has some weaknesses:
    - supporting generics due to erasures
    - supporting non-java clients is harder
    - it makes changes to wire formats problematic  (see above)

RSS makes a number of simplifying assumptions that minimise these problems.

### 1 - Restricted set of types

With RSS serialisation, only the following types are supported:

* a restricted set of pre agreed scalars, currently
    - Int
    - Long
    - Double
    - String
    - Float
    - Boolean
    - UUID
    - BigDecimal
* kotlin data classes
* type safe list - *raw generic (e.g. List<String>) are banned to avoid problems with erasures*
* exceptions
* a handful of classes that represent "nothing" e.g. Unit, NotRequired

Anything else should result in an exception when running the serialisation(_currently this validation is only partly
supported_)

Initially there is a small subset of scalar types, but the intent is to include all the commonly used java domain types.

### 2 - A single holder

A `SerialisationPacket` can hold any type of data

```kotlin
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
    // impl 
}
```

### 3 - Including type data in the wire format

There is a specified wire format `SerialisationPacketWireFormat` and this allows for meta-data to be passed back to the
client. Currently only kotlin clients are supported and this meta-data is simple, however the longer term intention is
to include richer type data (for example Swagger and GraphQL schema) to more easily support other types of clients.

### 3 - Round tripping

For all types there are 'round trip' tests that to confirm that information isn't being lost or translated incorrectly.

### A simple example

```kotlin
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
```

see
the [Test cases](https://github.com/mycordaapp/really-simple-serialisation/blob/master/impl/src/test/kotlin/mycorda/app/rss/JsonSerialiserTest.kt)
for more examples