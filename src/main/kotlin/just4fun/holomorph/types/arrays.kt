package just4fun.holomorph.types

import just4fun.holomorph.Entry
import just4fun.holomorph.EntryBuilder
import just4fun.holomorph.Type
import just4fun.holomorph.evalError
import java.util.*
import kotlin.reflect.KClass



sealed class ArrayPrimType<T: Any, E: Any>(override val elementType: Type<E>, override val typeKlas: KClass<T>): SequenceType<T, E> {
	abstract fun instance(size: Int): T
	abstract fun set(v: T, index: Int, e: E): Unit
	abstract fun get(v: T, index: Int): E
	abstract fun size(v: T): Int
	abstract fun copy(v: T, expectSize: Int): T
	
	override fun newInstance(): T = instance(bufferSize())
	private fun elementLike(v: Any?): Boolean = v is Number || v is String || v is Boolean || v is Char
	
	fun inlineCopy(v: Collection<*>): T? {
		return if (v.isEmpty() || elementLike(v.first()))
			instance(v.size).apply { v.forEachIndexed { i, item -> set(this, i, elementType.asInstance(item, false)!!) } }
		else run { evalError(v, this); null }
	}
	
	fun inlineCopy(v: Array<*>): T? {
		return if (v.isEmpty() || elementLike(v.first()))
			instance(v.size).apply { v.forEachIndexed { i, item -> set(this, i, elementType.asInstance(item, false)!!) } }
		else run { evalError(v, this); null }
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun asInstance(v: Any): T? = when {
		isInstance(v) -> v as T
		v is Collection<*> -> inlineCopy(v)
		v is Array<*> -> inlineCopy(v)
	// todo from spec arrays ?
	// case: xml node wrongly detected as Object due to limited info
		v is Map<*, *> -> instance(v.size).apply { for (e in v) set(this, 0, elementType.asInstance(e.value, false)!!) }
		else -> evalError(v, this)
	}
	
	override fun addElement(e: E?, index: Int, seq: T): T {
		if (e == null) return seq
		val size = size(seq)
		val newSeq = if (index == size) copy(seq, size + bufferSize()) else seq
		set(newSeq, index, e)
		return newSeq
	}
	
	override fun onComplete(seq: T, expectedSize: Int): T = if (expectedSize < size(seq)) copy(seq, expectedSize) else seq
	
	override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else copy(v, size(v))
	
	override fun toString(v: T?, sequenceSizeLimit: Int): String = if (v == null) "null" else {
		val buff = StringBuilder("[")
		var first = true
		iterator(v).forEach {
			if (first) first = false else buff.append(",")
			buff.append(it)
		}
		buff.append("]")
		buff.toString()
	}
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other || (other is ArrayPrimType<*, *> && typeKlas == other.typeKlas && elementType ==  other.elementType)
}



/*BYTE*/
object BytesType: ArrayPrimType<ByteArray, Byte>(ByteType, ByteArray::class) {
	override fun iterator(seq: ByteArray): Iterator<Byte> = seq.iterator()
	override fun instance(size: Int): ByteArray = ByteArray(size)
	override fun isInstance(v: Any): Boolean = v is ByteArray
	override fun set(v: ByteArray, index: Int, e: Byte) = run { v[index] = e }
	override fun get(v: ByteArray, index: Int): Byte = v[index]
	override fun size(v: ByteArray): Int = v.size
	override fun copy(v: ByteArray, expectSize: Int): ByteArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: ByteArray?, v2: ByteArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: ByteArray): Int = Arrays.hashCode(v)
	override fun fromEntry(value: ByteArray) = value
	override fun toEntry(value: ByteArray, name: String?, entryBuilder: EntryBuilder): Entry = entryBuilder.Entry(name, value)
}


/*LONG*/
object LongsType: ArrayPrimType<LongArray, Long>(LongType, LongArray::class) {
	override fun iterator(seq: LongArray): Iterator<Long> = seq.iterator()
	override fun instance(size: Int): LongArray = LongArray(size)
	override fun isInstance(v: Any): Boolean = v is LongArray
	override fun set(v: LongArray, index: Int, e: Long) = run { v[index] = e }
	override fun get(v: LongArray, index: Int): Long = v[index]
	override fun size(v: LongArray): Int = v.size
	override fun copy(v: LongArray, expectSize: Int): LongArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: LongArray?, v2: LongArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: LongArray): Int = Arrays.hashCode(v)
}


/*INT*/
object IntsType: ArrayPrimType<IntArray, Int>(IntType, IntArray::class) {
	override fun iterator(seq: IntArray): Iterator<Int> = seq.iterator()
	override fun instance(size: Int): IntArray = IntArray(size)
	override fun isInstance(v: Any): Boolean = v is IntArray
	override fun set(v: IntArray, index: Int, e: Int) = run { v[index] = e }
	override fun get(v: IntArray, index: Int): Int = v[index]
	override fun size(v: IntArray): Int = v.size
	override fun copy(v: IntArray, expectSize: Int): IntArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: IntArray?, v2: IntArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: IntArray): Int = Arrays.hashCode(v)
}


/*SHORT*/
object ShortsType: ArrayPrimType<ShortArray, Short>(ShortType, ShortArray::class) {
	override fun iterator(seq: ShortArray): Iterator<Short> = seq.iterator()
	override fun instance(size: Int): ShortArray = ShortArray(size)
	override fun isInstance(v: Any): Boolean = v is ShortArray
	override fun set(v: ShortArray, index: Int, e: Short) = run { v[index] = e }
	override fun get(v: ShortArray, index: Int): Short = v[index]
	override fun size(v: ShortArray): Int = v.size
	override fun copy(v: ShortArray, expectSize: Int): ShortArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: ShortArray?, v2: ShortArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: ShortArray): Int = Arrays.hashCode(v)
}


/*CHAR*/
object CharsType: ArrayPrimType<CharArray, Char>(CharType, CharArray::class) {
	override fun iterator(seq: CharArray): Iterator<Char> = seq.iterator()
	override fun instance(size: Int): CharArray = CharArray(size)
	override fun isInstance(v: Any): Boolean = v is CharArray
	override fun set(v: CharArray, index: Int, e: Char) = run { v[index] = e }
	override fun get(v: CharArray, index: Int): Char = v[index]
	override fun size(v: CharArray): Int = v.size
	override fun copy(v: CharArray, expectSize: Int): CharArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: CharArray?, v2: CharArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: CharArray): Int = Arrays.hashCode(v)
}


/*DOUBLE*/
object DoublesType: ArrayPrimType<DoubleArray, Double>(DoubleType, DoubleArray::class) {
	override fun iterator(seq: DoubleArray): Iterator<Double> = seq.iterator()
	override fun instance(size: Int): DoubleArray = DoubleArray(size)
	override fun isInstance(v: Any): Boolean = v is DoubleArray
	override fun set(v: DoubleArray, index: Int, e: Double) = run { v[index] = e }
	override fun get(v: DoubleArray, index: Int): Double = v[index]
	override fun size(v: DoubleArray): Int = v.size
	override fun copy(v: DoubleArray, expectSize: Int): DoubleArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: DoubleArray?, v2: DoubleArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: DoubleArray): Int = Arrays.hashCode(v)
}


/*FLOAT*/
object FloatsType: ArrayPrimType<FloatArray, Float>(FloatType, FloatArray::class) {
	override fun iterator(seq: FloatArray): Iterator<Float> = seq.iterator()
	override fun instance(size: Int): FloatArray = FloatArray(size)
	override fun isInstance(v: Any): Boolean = v is FloatArray
	override fun set(v: FloatArray, index: Int, e: Float) = run { v[index] = e }
	override fun get(v: FloatArray, index: Int): Float = v[index]
	override fun size(v: FloatArray): Int = v.size
	override fun copy(v: FloatArray, expectSize: Int): FloatArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: FloatArray?, v2: FloatArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: FloatArray): Int = Arrays.hashCode(v)
}

/*BOOLEAN*/
object BooleansType: ArrayPrimType<BooleanArray, Boolean>(BooleanType, BooleanArray::class) {
	override fun iterator(seq: BooleanArray): Iterator<Boolean> = seq.iterator()
	override fun instance(size: Int): BooleanArray = BooleanArray(size)
	override fun isInstance(v: Any): Boolean = v is BooleanArray
	override fun set(v: BooleanArray, index: Int, e: Boolean) = run { v[index] = e }
	override fun get(v: BooleanArray, index: Int): Boolean = v[index]
	override fun size(v: BooleanArray): Int = v.size
	override fun copy(v: BooleanArray, expectSize: Int): BooleanArray = Arrays.copyOf(v, expectSize)
	override fun equal(v1: BooleanArray?, v2: BooleanArray?): Boolean = Arrays.equals(v1, v2)
	override fun hashCode(v: BooleanArray): Int = Arrays.hashCode(v)
}
