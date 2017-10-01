package just4fun.holomorph

import just4fun.holomorph.types.SchemaType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.starProjectedType

/** A tool kit based upon the [schema] of a class [T] that allows to de/serialize objects of [T]. A subclass also allows to materialize serializable properties of the class and holds them as the [properties] list.
 *
 * It also provides access to the utility methods: [copy], [equal], [toString], [hashCode] based on serializable properties.
 * > [See guide for examples](https://just-4-fun.github.io/holomorph)
 *
 * @param [T] type of the serializable class.
 * @constructor creates [Reflexion] for the class [kClass] with optional `genericArgType` which is a list of type resolvers ([Type]) for corresponding type parameters if the class is generic.
 */

open class Reflexion<T: Any>(kClass: KClass<T>, vararg genericArgType: Type<*>): Producer<T> {
	/** The schema of the underlying class [T] */
	val schema: SchemaType<T> = Types.getOrAddType(kClass, *genericArgType) {
		if (genericArgType.isEmpty()) SchemaType(kClass) else SchemaType(kClass, genericArgType)
	}
	/** The list of materialized properties declared with [property]. */
	val properties = mutableListOf<Property<*>>()
	/** The way to materialize serializable property of the class [T]. Should be called as delegate (via `by`) */
	protected val property: PropDelegate<*> get() = PropDelegate<Any>(null)
	
	/** Type-aware variant of [property] */
	protected inline fun <reified T: Any> property(): PropDelegate<T> = PropDelegate(T::class)
	
	/** Allows to override default [Property] class to mode suitable subclass using [base] property as the class delegate. */
	protected open fun <T: Any> newProperty(base: Property<T>): Property<T> = base
	
	/** Updates values of the [inst] from the [input]. */
	fun <D: Any> instanceUpdateFrom(inst: T, input: D, factory: EntryProviderFactory<D>) = schema.instanceUpdateFrom(inst, input, factory)
	
	/** Creates instance from the [input] that form is supported by the provider [factory] */
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>) = schema.instanceFrom(input, factory)
	
	/** Creates an output in the form supported by the [factory] from the [input] */
	override fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>) = schema.instanceTo(input, factory)
	
	/** Creates an output in form supported by the [factory] from the [input]. [props] limits properties to serialize.
	 * @param[nameless] defines the mode. null: every schema uses its [SchemaType.nameless] value; true: makes schemas to be serialized as nameless sequence; false: makes schemas to be serialized as named sequence   */
	fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>, props: List<Property<*>>?, nameless: Boolean? = null) = schema.instanceTo(input, factory, props, nameless)
	
	/** Creates a list of instances from the [input] that form is supported by the provider [factory] */
	fun <D: Any> instancesFrom(input: D, factory: EntryProviderFactory<D>) = schema.instancesFrom(input, factory)
	
	/** Creates an output in the form supported by the [factory] from the list of instances [input] */
	fun <D: Any> instancesTo(input: List<T>, factory: EntryConsumerFactory<D>) = schema.instancesTo(input, factory)
	
	/** Returns new instance by copying values of serializable properties. If [deep] is true all values that are references will be copied via [copy] method. Otherwise, a new copy is created from the original values by references. */
	fun copy(v: T?, deep: Boolean = false): T? = schema.copy(v, deep)
	
	/** Checks equalty by comparing values of serializable properties */
	fun equal(v1: T?, v2: T?): Boolean = schema.equal(v1, v2)
	
	/** Returns a json-like representation of the [v] using serializable properties. For value that is a collection the [sequenceSizeLimit] limits its size. */
	fun toString(v: T?, sequenceSizeLimit: Int = -1): String = schema.toString(v, sequenceSizeLimit)
	
	/** Generates a hash code from serializable properties */
	fun hashCode(v: T): Int = schema.hashCode(v)
	
	
	/* Property Delegate */
	/**  */
	protected inner class PropDelegate<T: Any> @PublishedApi internal constructor(val propTypeKlas: KClass<T>?) {
		lateinit private var prop: Property<T>
		operator fun getValue(thisRef: Any?, kProp: KProperty<*>): Property<T> = prop
		operator fun provideDelegate(thisRef: Any?, kProp: KProperty<*>): PropDelegate<T> {
			val sProp = schema.propertiesMap[kProp.name] ?: throw Exception("${this@Reflexion::class} is trying to materialize property ${kProp.name} that isn't in ${schema.typeKlas} schema")// TODO allow define new property ?
			//
			if (propTypeKlas != null) {
				val type = Types.resolve(propTypeKlas.starProjectedType, propTypeKlas)
				if (type != sProp.type) throw Exception("${this@Reflexion::class} is trying to introduce property ${kProp.name} with type other than the one of the original property in ${schema.typeKlas}")
			}
			//
			@Suppress("UNCHECKED_CAST")
			prop = newProperty(sProp) as Property<T>
			properties.add(prop)
			return this
		}
	}
}
