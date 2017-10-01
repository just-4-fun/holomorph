package just4fun.holomorph

import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName


/** Resolver for type [T] of a schema property. Performs conversions from the [Entry] value to the value of [T] and back. Provides the constructor and utility methods for the value of [T] basically for internal use.
 *
 * Built-in resolvers are defined in the package `types`.
 *
 * Custom resolver should be defined for property which type [T] is not supported out-of-the-box.
 * [See this guide for more details.](https://just-4-fun.github.io/holomorph/#property-types)
 * */
interface Type<T: Any>: EntryHelper<T> {
	/** The [KClass] of type [T] which is supposed to be resolved by this instance.  */
	val typeKlas: KClass<T>
	/** The short name for the underlying type */
	val typeName: String get() = typeKlas.simpleName ?: typeKlas.jvmName
	
	/** The constructor for instances of [T] */
	fun newInstance(): T
	
	/** The default value for the type. Which is `null` for all but 8 primitive types.  */
	val default: T? get() = null
	
	/** Checks if value [v] is instance of [T] */
	fun isInstance(v: Any): Boolean = v::class == typeKlas
	
	/** Casts the value [v] to [T] if possible or returns `null`. If the original class property isn't nullable returns a new instance. */
	fun asInstance(v: Any?, nullable: Boolean): T? {
		val ev = if (v == null || v == Unit) null else asInstance(v)
		return ev ?: if (nullable) null else newInstance()
	}
	
	/** Casts the value [v] to [T] if possible or returns `null`. If fails prints error message. */
	@Suppress("UNCHECKED_CAST")
	fun asInstance(v: Any): T? = when {
		isInstance(v) -> v as? T
		else -> evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKlas.javaObjectType}"))
	}
	
	/** Returns new instance by copying values of serializable properties. If [deep] is true all values that are references will be copied via [copy] method. Otherwise, a new copy is created from the original values by references. */
	fun copy(v: T?, deep: Boolean = false): T? = v
	
	/** Checks equalty by comparing values of serializable properties */
	fun equal(v1: T?, v2: T?): Boolean = v1 == v2
	
	/** Returns a json-like representation of the [v] using serializable properties. For value that is a collection the [sequenceSizeLimit] limits its size. */
	fun toString(v: T?, sequenceSizeLimit: Int = -1): String = v.toString()
	
	/** Generates a hash code from serializable properties */
	fun hashCode(v: T): Int = v.hashCode()
	
}

