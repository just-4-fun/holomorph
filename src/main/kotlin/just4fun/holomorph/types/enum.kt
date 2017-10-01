package just4fun.holomorph.types

import just4fun.holomorph.EnclosedEntries
import just4fun.holomorph.EntryBuilder
import just4fun.holomorph.Type
import just4fun.holomorph.evalError
import kotlin.reflect.KClass



class EnumType<T: Any>(override val typeKlas: KClass<T>, val values: Array<Enum<*>>): Type<T> {
	@Suppress("UNCHECKED_CAST")
	override fun newInstance(): T = values[0] as T// should not be called
	
	override fun asInstance(v: Any): T? = when (v) {
		is String -> fromName(v)
		is Int -> fromOrdinal(v)
		else -> null
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun fromName(name: String): T? = values.find { it.name == name } as? T
	
	@Suppress("UNCHECKED_CAST")
	private fun fromOrdinal(n: Int): T? = values.find { it.ordinal == n } as? T
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun fromEntry(value: ByteArray) = evalError("ByteArray", this)
	override fun fromEntry(value: String) = fromName(value)
	override fun fromEntry(value: Long) = fromOrdinal(value.toInt())
	override fun fromEntry(value: Int) = fromOrdinal(value)
	override fun fromEntry(value: Short) = fromOrdinal(value.toInt())
	override fun fromEntry(value: Byte) = fromOrdinal(value.toInt())
	override fun fromEntry(value: Double) = fromOrdinal(value.toInt())
	override fun fromEntry(value: Float) = fromOrdinal(value.toInt())
	override fun fromEntry(value: Boolean) = fromOrdinal(if (value) 1 else 0)
	@Suppress("UNCHECKED_CAST")
	override fun fromEntry(value: Char) = values.find { it.name.startsWith(value, true) } as? T
	
	override fun toEntry(value: T, name: String?, entryBuilder: EntryBuilder) = entryBuilder.Entry(name, (value as Enum<*>).name)
	
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
}