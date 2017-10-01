package just4fun.holomorph.forms

import just4fun.holomorph.*
import just4fun.holomorph.types.*
import just4fun.holomorph.types.*




/* RAW ARRAY factory  */
/** The factory for Array<Any?> serialization form. */
object ArrayFactory: ProduceFactory<Array<*>, Array<*>> {
	@Suppress("UNCHECKED_CAST")
	val seqType = ArrayType(AnyType, Array<Any>::class) as SequenceType<Array<*>, Any>
	override fun invoke(input: Array<*>): EntryProvider<Array<*>> = SequenceProvider(input, seqType)
	override fun invoke(): EntryConsumer<Array<*>> = SequenceConsumer(seqType)
}







/* SEQUENCE  */
/* Provider */
class SequenceProvider<T: Any, E: Any>(override val input: T, private val seqType: SequenceType<T, E>, private var initial: Boolean = true): EntryProvider<T> {
	private val iterator = seqType.iterator(input)
	
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		return if (initial) run { initial = false; entryBuilder.StartEntry(null,  false) }
		else if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val v = iterator.next()
			seqType.elementType.toEntry(v, null, entryBuilder)
		}
	}
}

/* Consumer */
class SequenceConsumer<T: Any, E: Any>(private val seqType: SequenceType<T, E>, private var initial: Boolean = true): EntryConsumer<T> {
	private var instance: T = seqType.newInstance()
	private var size = 0
	
	override fun output(): T = seqType.onComplete(instance, size)
	
	private fun addElement(value: Any?) {
		val v = seqType.elementType.asInstance(value, true)
		instance = seqType.addElement(v, size, instance)
		size++
	}
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		addElement(seqType.elementType.fromEntries(subEntries, expectNames))
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: String): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Long): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Int): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Double): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Float): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Boolean): Unit = addElement(value)
	override fun consumeNullEntry(name: String?): Unit = addElement(null)
}










/* RAW COLLECTION */

/* Provider */
class RawCollectionProvider(override val input: Collection<Any?>, private var initial: Boolean = true): EntryProvider<Collection<Any?>> {
	private val iterator = input.iterator()
	
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		return if (initial) run { initial = false; entryBuilder.StartEntry(null, false) }
		else if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val v = iterator.next()
			if (v == null) entryBuilder.NullEntry()
			else {
				val type = AnyType.detectType(v)
				if (type == null) StringType.toEntry(v.toString(), null, entryBuilder)
				else type.toEntry(v, null, entryBuilder)
			}
		}
	}
}

/* Consumer */
// todo typeUtils detect type?
class RawCollectionConsumer(private var initial: Boolean = true): EntryConsumer<Collection<Any?>> {
	var instance: Collection<Any?> = RawCollectionType.newInstance()
	
	override fun output(): Collection<Any?> {
		return RawCollectionType.onComplete(instance, instance.size)
	}
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		val v = subEntries.intercept(if (expectNames) RawMapConsumer(false) else RawCollectionConsumer(false))
		addElement(v)
	}
	
	private fun addElement(value: Any?) {
		instance = RawCollectionType.addElement(value, instance.size, instance)
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: String): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Long): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Int): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Double): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Float): Unit = addElement(value)
	override fun consumeEntry(name: String?, value: Boolean): Unit = addElement(value)
	override fun consumeNullEntry(name: String?): Unit = addElement(null)
}
