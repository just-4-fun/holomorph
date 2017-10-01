package just4fun.holomorph.types

import just4fun.holomorph.*
import just4fun.kotlinkit.Result
import just4fun.holomorph.forms.SequenceConsumer
import just4fun.holomorph.forms.SequenceProvider
import java.util.*
import kotlin.reflect.KClass




/* SEQUENCE TYPE */
interface SequenceType<T: Any, E: Any>: Type<T>, Producer<T>, EntryHelperDefault<T> {
	val elementType: Type<E>
	override val typeName: String get() = "${typeKlas.simpleName}<${elementType.typeName}>"
	
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<T> = Produce(factory(input), SequenceConsumer(this))
	override fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>): Result<D> = Produce(SequenceProvider(input, this), factory())
	
	fun iterator(seq: T): Iterator<E>
	fun addElement(e: E?, index: Int, seq: T): T
	fun onComplete(seq: T, expectedSize: Int): T = seq
	fun bufferSize() = 100
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = subEntries.intercept(SequenceConsumer(this, false))
	override fun toEntry(value: T, name: String?, entryBuilder: EntryBuilder): Entry {
		return entryBuilder.StartEntry(name, false, SequenceProvider(value, this, false))
	}
}





/* ARRAY TYPE */
open class ArrayType<E: Any>(override final val elementType: Type<E>, override final val typeKlas: KClass<Array<E>>): SequenceType<Array<E>, E> {
	override fun newInstance(): Array<E> = instance(bufferSize())
	@Suppress("UNCHECKED_CAST")
	fun instance(size: Int): Array<E> = java.lang.reflect.Array.newInstance(elementType.typeKlas.javaObjectType, size) as Array<E>
	
	@Suppress("UNCHECKED_CAST")
	override fun asInstance(v: Any): Array<E>? = when (v) {
		is Array<*> ->
			if (v.javaClass.componentType == elementType.typeKlas.java) v as Array<E>
			else instance(v.size).apply { v.forEachIndexed { i, item -> this[i] = elementType.asInstance(item, false)!! } }
		is Collection<*> -> instance(v.size).apply { v.forEachIndexed { i, item -> this[i] = elementType.asInstance(item, false)!! } }
	// todo from spec arrays ?
	// case: xml node wrongly detected as Object due to limited info
		is Map<*, *> -> instance(v.size).apply { for (e in v) this[0] = elementType.asInstance(e.value, false)!! }
		else -> evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKlas.javaObjectType}"))
	}
	
	override fun copy(v: Array<E>?, deep: Boolean): Array<E>? = if (!deep || v == null) v else {
		val seq = instance(v.size)
		v.forEachIndexed { ix, e -> seq[ix] = elementType.copy(e, true)!! }
		seq
	}
	
	override fun equal(v1: Array<E>?, v2: Array<E>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		for (n in 0 until v1.size) if (!elementType.equal(v1[n], v2[n])) return false
		true
	})
	
	override fun hashCode(v: Array<E>): Int {
		var code = 1
		for (e in v) {
			code = code * 31 + elementType.hashCode(e)
		}
		return code
	}
	
	override fun toString(v: Array<E>?, sequenceSizeLimit: Int): String = v?.map { elementType.toString(it, sequenceSizeLimit) }?.joinToString(", ", "[", "]", sequenceSizeLimit) ?: "null"
	
	override fun iterator(seq: Array<E>): Iterator<E> = seq.iterator()
	override fun addElement(e: E?, index: Int, seq: Array<E>): Array<E> {
		val newSeq = if (index == seq.size) Arrays.copyOf(seq, seq.size + bufferSize()) else seq
		newSeq[index] = e
		return newSeq
	}
	
	override fun onComplete(seq: Array<E>, expectedSize: Int): Array<E> {
		return if (expectedSize < seq.size) Arrays.copyOf(seq, expectedSize) else seq
	}
	
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other || (other is ArrayType<*> && typeKlas == other.typeKlas && elementType ==  other.elementType)
	
}





