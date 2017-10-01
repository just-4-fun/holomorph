package just4fun.holomorph

import just4fun.holomorph.types.SchemaType
import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField



/* Prop */

enum class PropertyVisibility { PUBLIC, PROTECTED, INTERNAL, PRIVATE }


/** Represents the serializable property of the [schema] of some class which corresponds to the counterpart property of that class with the same name and type [T].
 *
 * Note that the schema property represented by this class and the class property (as it's defined by Kotlin docs) are different notions.
 *
 * Serializable properties are selected by the [schema] ([SchemaType] ) from the class it reflects the way described [in this guide](https://just-4-fun.github.io/holomorph/#tuning-the-schema)
 *
 */
interface Property<T: Any> {
	companion object {
		/** The most restrictive visibility level for properties to be selected by the schema as serializable.  [See this guide for details](https://just-4-fun.github.io/holomorph/#tuning-the-schema) */
		var visibilityBound = PropertyVisibility.PUBLIC
	}
	
	/** The zero-based ordinal number of the property in the [schema] */
	val ordinal: Int
	/** The property name matching the corresponding property of the class */
	val name: String
	/** The type resolver of [T] */
	val type: Type<T>
	/** The schema which this property belongs to */
	val schema: SchemaType<*>
	/** Indicates whether the original class property is nullable */
	val nullable: Boolean
	
	/** Returns the value of object's [obj] original property. */
	operator fun get(obj: Any): T?
	
	/** Assigns the value of object's [obj] original property to [v].  */
	operator fun set(obj: Any, v: Any?): Unit
	
	/** Assigns the value of object's [obj] original property to [v] ensuring type compatibility. */
	fun setChecked(obj: Any, v: T?): Unit
}




internal class PropImpl<T: Any>(ordinal: Int, val id: String, override val type: Type<T>, override val schema: SchemaType<*>, override val nullable: Boolean): Property<T> {
	override var ordinal: Int = ordinal; set(value) = run { field = value }
	override val name: String get() = id
	private lateinit var getter: KCallable<T>
	private var setter: KCallable<T>? = null
	private var field: Field? = null
	internal var rType: KType? = null
	private var useAccessors = schema.useAccessors
	
	@Suppress("UNCHECKED_CAST")
	override operator fun get(obj: Any): T? = if (useAccessors) getter.call(obj) else field?.get(obj) as T?
	
	override operator fun set(obj: Any, v: Any?) {
		if (v == Unit) return// skips updating from an array of values
		val ev = type.asInstance(v, nullable)
		if (useAccessors) setter?.call(obj, ev) ?: field?.set(obj, ev) else field?.set(obj, ev)
	}
	
	override fun setChecked(obj: Any, v: T?) {
		val ev = v ?: if (nullable) null else type.newInstance()
		if (useAccessors) setter?.call(obj, ev) ?: field?.set(obj, ev) else field?.set(obj, ev)
	}
	
	override fun toString() = name// "$typeName: $type"
	
	@Suppress("UNCHECKED_CAST")
	internal fun initAccessors(kProp: KProperty1<*, *>) {
		getter = kProp.getter.apply { isAccessible = true } as KCallable<T>
		if (kProp is KMutableProperty<*>) setter = kProp.setter.apply { isAccessible = true } as KCallable<T>
		field = kProp.javaField?.apply { isAccessible = true } ?: run { useAccessors = true; null }
	}
}


/* Constructor Param */

internal class Param(val index: Int, var name: String, val type: Type<*>, val optional: Boolean, val nullable: Boolean) {
	val default: Any? get() = if (nullable) null else type.newInstance()
	override fun toString() = "$name: $type"
}