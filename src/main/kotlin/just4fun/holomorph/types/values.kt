package just4fun.holomorph.types

import just4fun.holomorph.*
import kotlin.reflect.KClass



sealed class ValueBasedType<V: Any, T: Any>(override final val typeKlas: KClass<T>): Type<T> {
	abstract val valueType: Type<V>
	abstract fun fromValue(v: V): T?
	abstract fun toValue(v: T?): V?
	@Suppress("UNCHECKED_CAST")
	override fun asInstance(v: Any): T? = if (isInstance(v)) v as T else fromValue(valueType.asInstance(v, false)!!)
	
	override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else valueType.copy(toValue(v), true)?.let { fromValue(it) }
	override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && valueType.equal(toValue(v1), toValue(v2)))
	override fun hashCode(v: T): Int = toValue(v)?.let { valueType.hashCode(it) } ?: 0
	
	override fun fromEntry(value: ByteArray):T? = valueType.fromEntry(value)?.let { fromValue(it) } ?: evalError(value, this)
	override fun fromEntry(value: String):T? = valueType.fromEntry(value)?.let { fromValue(it) } ?: evalError(value, this)
	override fun fromEntry(value: Long) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Int) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Short) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Byte) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Char) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Double) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Float) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntry(value: Boolean) = fromValue(valueType.fromEntry(value)!!)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: T, name: String?, entryBuilder: EntryBuilder): Entry {
		return toValue(value)?.let { valueType.toEntry(it, name, entryBuilder) } ?: entryBuilder.NullEntry(name)
	}
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other || (other is ValueBasedType<*, *> && typeKlas == other.typeKlas && valueType == other.valueType)
	
}




abstract class LongBasedType<T: Any>(valueKlas: KClass<T>): ValueBasedType<Long, T>(valueKlas) {
	override val valueType: Type<Long> = LongType
	override fun fromEntry(value: Long) = fromValue(value)
}

abstract class StringBasedType<T: Any>(valueKlas: KClass<T>): ValueBasedType<String, T>(valueKlas) {
	override val valueType: Type<String> = StringType
	override fun fromEntry(value: String) = fromValue(value)
}

abstract class BytesBasedType<T: Any>(valueKlas: KClass<T>): ValueBasedType<ByteArray, T>(valueKlas) {
	override val valueType: Type<ByteArray> = BytesType
	override fun fromEntry(value: ByteArray) = fromValue(value)
}
