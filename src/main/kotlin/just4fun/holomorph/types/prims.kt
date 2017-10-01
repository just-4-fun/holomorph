package just4fun.holomorph.types

import just4fun.holomorph.*
import just4fun.holomorph.forms.RawCollectionConsumer
import just4fun.holomorph.forms.RawMapConsumer
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType


abstract class PrimType<T: Any>(override final val typeKlas: KClass<T>): Type<T> {
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
}


object AnyType: PrimType<Any>(Any::class) {
	override fun newInstance() = Any()
	override fun isInstance(v: Any): Boolean = true
	override fun asInstance(v: Any): Any? = v
	
	override fun fromEntry(value: String) = value
	override fun fromEntry(value: Long) = value
	override fun fromEntry(value: Int) = value
	override fun fromEntry(value: Short) = value
	override fun fromEntry(value: Byte) = value
	override fun fromEntry(value: Char) = value
	override fun fromEntry(value: Double) = value
	override fun fromEntry(value: Float) = value
	override fun fromEntry(value: Boolean) = value
	override fun fromEntry(value: ByteArray) = value
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = subEntries.intercept(if (expectNames) RawMapConsumer(false) else RawCollectionConsumer(false))
	override fun toEntry(value: Any, name: String?, entryBuilder: EntryBuilder): Entry = detectType(value)?.toEntry(value, name, entryBuilder) ?: entryBuilder.Entry(name, value.toString())
	
	override fun copy(v: Any?, deep: Boolean): Any? = if (v == null) null else detectType(v)?.copy(v, deep) ?: v
	override fun toString(v: Any?, sequenceSizeLimit: Int): String = if (v == null) "null" else detectType(v)?.toString(v, sequenceSizeLimit) ?: v.toString()
	override fun equal(v1: Any?, v2: Any?): Boolean = when {
		v1 == null -> v2 == null
		v2 == null -> false
		else -> {
			val type = detectType(v1)
			when (type) {
				null -> v1 == v2
				else -> type.equal(type.asInstance(v1), type.asInstance(v2))
			}
		}
	}
	
	fun <T: Any> detectType(v: T): Type<T>? {
		var typ: Type<*>? = when (v) {
			is String -> StringType
			is Int -> IntType
			is Long -> LongType
			is Double -> DoubleType
			is Float -> FloatType
			is Short -> ShortType
			is Byte -> ByteType
			is Boolean -> BooleanType
			is Char -> CharType
			else -> {
				val klas = v::class
				Types.resolve(klas.starProjectedType, klas) ?: logError("Can not detect Type of $v")
			}
		}
		@Suppress("UNCHECKED_CAST")
		return typ as Type<T>?
	}
	
}

object LongType: PrimType<Long>(Long::class) {
	override fun newInstance() = 0L
	override val default get() = 0L
	override fun isInstance(v: Any): Boolean = v is Long
	override fun asInstance(v: Any): Long? = when (v) {
		is Long -> v
		is Number -> v.toLong()
		is String -> v.toNumber(String::toLong, Double::toLong)
		is Boolean -> if (v) 1L else 0L
		is Char -> v.toLong()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toLong, Double::toLong)
	override fun fromEntry(value: Long) = value
	override fun fromEntry(value: Int) = value.toLong()
	override fun fromEntry(value: Short) = value.toLong()
	override fun fromEntry(value: Byte) = value.toLong()
	override fun fromEntry(value: Char) = value.toLong()
	override fun fromEntry(value: Double) = value.toLong()
	override fun fromEntry(value: Float) = value.toLong()
	override fun fromEntry(value: Boolean) = if (value) 1L else 0L
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Long, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object IntType: PrimType<Int>(Int::class) {
	override fun newInstance() = 0
	override val default: Int get() = 0
	override fun isInstance(v: Any): Boolean = v is Int
	override fun asInstance(v: Any): Int? = when (v) {
		is Int -> v
		is Number -> v.toInt()
		is String -> v.toNumber(String::toInt, Double::toInt)
		is Boolean -> if (v) 1 else 0
		is Char -> v.toInt()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toInt, Double::toInt)
	override fun fromEntry(value: Long) = value.toInt()
	override fun fromEntry(value: Int) = value
	override fun fromEntry(value: Short) = value.toInt()
	override fun fromEntry(value: Byte) = value.toInt()
	override fun fromEntry(value: Char) = value.toInt()
	override fun fromEntry(value: Double) = value.toInt()
	override fun fromEntry(value: Float) = value.toInt()
	override fun fromEntry(value: Boolean) = if (value) 1 else 0
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Int, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object ShortType: PrimType<Short>(Short::class) {
	override fun newInstance(): Short = 0
	override val default: Short get() = 0
	override fun isInstance(v: Any): Boolean = v is Short
	override fun asInstance(v: Any): Short? = when (v) {
		is Short -> v
		is Number -> v.toShort()
		is String -> v.toNumber(String::toShort, Double::toShort)
		is Boolean -> if (v) 1 else 0
		is Char -> v.toShort()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toShort, Double::toShort)
	override fun fromEntry(value: Long) = value.toShort()
	override fun fromEntry(value: Int) = value.toShort()
	override fun fromEntry(value: Short) = value
	override fun fromEntry(value: Byte) = value.toShort()
	override fun fromEntry(value: Char) = value.toShort()
	override fun fromEntry(value: Double) = value.toShort()
	override fun fromEntry(value: Float) = value.toShort()
	override fun fromEntry(value: Boolean) = if (value) 1.toShort() else 0.toShort()
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Short, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object ByteType: PrimType<Byte>(Byte::class) {
	override fun newInstance(): Byte = 0
	override val default: Byte get() = 0
	override fun isInstance(v: Any): Boolean = v is Byte
	override fun asInstance(v: Any): Byte? = when (v) {
		is Byte -> v
		is Number -> v.toByte()
		is String -> v.toNumber(String::toByte, Double::toByte)
		is Boolean -> if (v) 1 else 0
		is Char -> v.toByte()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toByte, Double::toByte)
	override fun fromEntry(value: Long) = value.toByte()
	override fun fromEntry(value: Int) = value.toByte()
	override fun fromEntry(value: Short) = value.toByte()
	override fun fromEntry(value: Byte) = value
	override fun fromEntry(value: Char) = value.toByte()
	override fun fromEntry(value: Double) = value.toByte()
	override fun fromEntry(value: Float) = value.toByte()
	override fun fromEntry(value: Boolean) = if (value) 1.toByte() else 0.toByte()
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Byte, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object DoubleType: PrimType<Double>(Double::class) {
	override fun newInstance(): Double = 0.0
	override val default: Double get() = 0.0
	override fun isInstance(v: Any): Boolean = v is Double
	override fun asInstance(v: Any): Double? = when (v) {
		is Double -> v
		is Number -> v.toDouble()
		is String -> v.toNumber(String::toDouble, Double::toDouble)
		is Boolean -> if (v) 1.0 else 0.0
		is Char -> v.toDouble()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toDouble, Double::toDouble)
	override fun fromEntry(value: Long) = value.toDouble()
	override fun fromEntry(value: Int) = value.toDouble()
	override fun fromEntry(value: Short) = value.toDouble()
	override fun fromEntry(value: Byte) = value.toDouble()
	override fun fromEntry(value: Char) = value.toDouble()
	override fun fromEntry(value: Double) = value
	override fun fromEntry(value: Float) = value.toDouble()
	override fun fromEntry(value: Boolean) = if (value) 1.0 else 0.0
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Double, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object FloatType: PrimType<Float>(Float::class) {
	override fun newInstance(): Float = 0f
	override val default: Float get() = 0f
	override fun isInstance(v: Any): Boolean = v is Float
	override fun asInstance(v: Any): Float? = when (v) {
		is Float -> v
		is Number -> v.toFloat()
		is String -> v.toNumber(String::toFloat, Double::toFloat)
		is Boolean -> if (v) 1.0f else 0.0f
		is Char -> v.toFloat()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toNumber(String::toFloat, Double::toFloat)
	override fun fromEntry(value: Long) = value.toFloat()
	override fun fromEntry(value: Int) = value.toFloat()
	override fun fromEntry(value: Short) = value.toFloat()
	override fun fromEntry(value: Byte) = value.toFloat()
	override fun fromEntry(value: Char) = value.toFloat()
	override fun fromEntry(value: Double) = value.toFloat()
	override fun fromEntry(value: Float) = value
	override fun fromEntry(value: Boolean) = if (value) 1.0f else 0.0f
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Float, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object CharType: PrimType<Char>(Char::class) {
	override fun newInstance(): Char = '\u0000'
	override val default: Char get() = '\u0000'
	override fun isInstance(v: Any): Boolean = v is Char
	override fun asInstance(v: Any): Char? = when (v) {
		is Char -> v
		is Int -> v.toChar()// glitch : is Number fails
		is String -> if (v.isEmpty()) '\u0000' else try {
			Integer.parseInt(v).toChar()
		} catch (e: Throwable) {
			v[0]
		}
		is Long -> v.toChar()
		is Double -> v.toChar()
		is Boolean -> if (v) '1' else '0'
		is Float -> v.toChar()
		is Short -> v.toChar()
		is Byte -> v.toChar()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = if (value.isEmpty()) '\u0000' else if (value.length == 1) value[0] else if (value.all { Character.isDigit(it) }) Integer.parseInt(value).toChar() else evalError(value, this)
	override fun fromEntry(value: Long) = value.toChar()
	override fun fromEntry(value: Int) = value.toChar()
	override fun fromEntry(value: Short) = value.toChar()
	override fun fromEntry(value: Byte) = value.toChar()
	override fun fromEntry(value: Char) = value
	override fun fromEntry(value: Double) = value.toChar()
	override fun fromEntry(value: Float) = value.toChar()
	override fun fromEntry(value: Boolean) = if (value) '1' else '0'
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Char, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object BooleanType: PrimType<Boolean>(Boolean::class) {
	var falseLike = listOf("", "0", "null", "0.0", "0,0")
	override fun newInstance(): Boolean = false
	override val default: Boolean get() = false
	override fun isInstance(v: Any): Boolean = v is Boolean
	override fun asInstance(v: Any): Boolean? = when (v) {
		is Boolean -> v
		is Number -> v.toInt() != 0
		"false" -> false
		"true" -> true
		is String -> v !in falseLike
		is Char -> v != '0'
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value.toLowerCase().let { if (it == "false") false else if (it == "true") true else it !in falseLike }
	override fun fromEntry(value: Long) = value.toInt() != 0
	override fun fromEntry(value: Int) = value != 0
	override fun fromEntry(value: Short) = value.toInt() != 0
	override fun fromEntry(value: Byte) = value.toInt() != 0
	override fun fromEntry(value: Char) = value.toInt() != 0
	override fun fromEntry(value: Double) = value.toInt() != 0
	override fun fromEntry(value: Float) = value.toInt() != 0
	override fun fromEntry(value: Boolean) = value
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: Boolean, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

object StringType: PrimType<String>(String::class) {
	override fun newInstance(): String = ""
	override fun isInstance(v: Any): Boolean = v is String
	override fun asInstance(v: Any): String? = when (v) {
		is String -> v
		is Number -> v.toString()
		is Boolean -> v.toString()
		is Char -> v.toString()
		else -> evalError(v, this)
	}
	
	override fun fromEntry(value: String) = value
	override fun fromEntry(value: Long) = value.toString()
	override fun fromEntry(value: Int) = value.toString()
	override fun fromEntry(value: Short) = value.toString()
	override fun fromEntry(value: Byte) = value.toString()
	override fun fromEntry(value: Char) = value.toString()
	override fun fromEntry(value: Double) = value.toString()
	override fun fromEntry(value: Float) = value.toString()
	override fun fromEntry(value: Boolean) = value.toString()
	override fun fromEntry(value: ByteArray) = evalError(value, this)
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = evalError("Entries", this)
	override fun toEntry(value: String, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}

//todo reasoning for that Unit values
object UnitType: PrimType<Unit>(Unit::class) {
	override fun newInstance(): Unit = Unit
	override val default: Unit get() = Unit
	override fun asInstance(v: Any): Unit? = Unit
	override fun copy(v: Unit?, deep: Boolean): Unit? = Unit
	override fun equal(v1: Unit?, v2: Unit?): Boolean = true
	override fun isInstance(v: Any): Boolean = v == Unit
	
	override fun fromEntry(value: String) = Unit
	override fun fromEntry(value: Long) = Unit
	override fun fromEntry(value: Int) = Unit
	override fun fromEntry(value: Short) = Unit
	override fun fromEntry(value: Byte) = Unit
	override fun fromEntry(value: Char) = Unit
	override fun fromEntry(value: Double) = Unit
	override fun fromEntry(value: Float) = Unit
	override fun fromEntry(value: Boolean) = Unit
	override fun fromEntry(value: ByteArray) = Unit
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = Unit
	override fun toEntry(value: Unit, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.NullEntry(name)
}




internal val NumPattern = """[\D&&[^.,\-]]*(-?[\D&&[^.,]]*)(\d*)([.,]*)(\d*).*""".toRegex()

internal inline fun <T: Any> String.toNumber(fromString: String.() -> T, fromDouble: Double.() -> T): T {
	return try {
		fromString()
	} catch (e: NumberFormatException) {
		// call cost 30000 ns
		NumPattern.matchEntire(this)?.run {
			var (sig, r, pt, f) = destructured
			var mult = if (sig.endsWith("-")) -1 else 1
			if (r.isEmpty()) r = "0"
			if (f.isEmpty()) f = "0"
			if (pt.length > 1) {
				if (r != "0") f = "0" else mult = 1
			}
			val n = "$r.$f".toDouble() * mult
			//		println("string2double: $v VS $sig$r $pt $f  >  $n")
			n.fromDouble()
		} ?: 0.0.fromDouble()
	}
}

