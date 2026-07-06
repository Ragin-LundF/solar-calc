# Module solarcalc-kotlin-extensions - solarcalc Kotlin Extensions

The solarcalc Kotlin extensions are mainly used for easier handling of classes with `toString()`, `hasCode()` and `equals()` methods.
In most cases this applies to Hibernate entities.

## Usage

```kotlin
@Entity
@Table(name = "MYBACKEND_EXAMPLE")
class MyBackendExample: Auditable() {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "mybackend_example_sequence_generator"
    )
    @SequenceGenerator(
        name = "mybackend_example_sequence_generator",
        sequenceName = "MYBACKEND_EXAMPLE_SEQ",
        allocationSize = 1
    )
    @Column(name = "id", updatable = false, nullable = false)
    var id: Long? = null

    @Column(name = "external_id", nullable = true, length = 255)
    var externalId: String? = null

    @Column(name = "user_id", nullable = false, length = 255)
    lateinit var userId: String

    @Column(name = "status", nullable = true, length = 50)
    lateinit var status: String

    // usage of the Kotlin Extension for the equals() method
    override fun equals(other: Any?): Boolean {
        return kotlinEquals(properties = properties, other = other)
    }

    // usage of the Kotlin Extension for the hashCode() method
    override fun hashCode(): Int {
        return kotlinHashCode(properties = properties)
    }

    // usage of the Kotlin Extension for the toString() method
    override fun toString(): String {
        return kotlinToString(properties = properties)
    }

    companion object {
        private const val serialVersionUID = -4293227875261236785L

        // defines the properties of the class, which should be used for the
        // Kotlin Extension methods equals(), hashCode() and toString()
        private val properties = arrayOf(
            MyBackendExample::id,
            MyBackendExample::externalId,
            MyBackendExample::userId,
            MyBackendExample::status,
            MyBackendExample::createdBy,
            MyBackendExample::createdDate,
            MyBackendExample::lastModifiedBy,
            MyBackendExample::lastModifiedDate,
            MyBackendExample::version,
        )
    }
}

```

## Recommendation - Use Data Classes
The use of Kotlin extensions is only necessary for `class` objects.
Ideally, however, a `data class` is used, as this is already immutable per se.

The only advantage of a `class` object is that it can be inherited.

### Usage

#### Gradle Properties
To be able to use `data class`'es for Hibernate, the following plugin must be added to `gradle.properties`:

```groovy
apply plugin: "org.jetbrains.kotlin.plugin.jpa"
```

#### Kotlin Implementation

```kotlin
@Entity
@Table(name = "MYBACKEND_EXAMPLE")
data class MyBackendExample(
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "mybackend_example_sequence_generator"
    )
    @SequenceGenerator(
        name = "mybackend_example_sequence_generator",
        sequenceName = "MYBACKEND_EXAMPLE_SEQ",
        allocationSize = 1
    )
    @Column(name = "id", updatable = false, nullable = false)
    var id: Long? = null,

    @Column(name = "extuid", updatable = false, nullable = false)
    var uid: UUID,

    @Column(name = "user_id", nullable = false, length = 255)
    var userId: String,

    @Column(name = "name", nullable = false, length = 30)
    var name: String
) : Serializable {
    companion object {
        @Serial
        private const val serialVersionUID = -4138529791397115824L
    }
}
```
