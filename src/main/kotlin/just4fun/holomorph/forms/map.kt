package just4fun.holomorph.forms

import just4fun.holomorph.*
import just4fun.holomorph.types.AnyType
import just4fun.holomorph.types.MapType
import just4fun.holomorph.types.RawMapType
import just4fun.holomorph.types.StringType
import just4fun.holomorph.types.*


/*MAP*/
/* Provider */
class MapProvider<K: Any, V: Any>(override val input: MutableMap<*,*>, private val mapType: MapType<K, V>, private var initial: Boolean = true): EntryProvider<MutableMap<*, *>> {
	@Suppress("UNCHECKED_CAST")
	private val iterator = input.iterator() as MutableIterator<MutableMap.MutableEntry<K, V>>
	private val type = mapType.valueType
	
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		return if (initial) run { initial = false; entryBuilder.StartEntry(null, true) }
		else if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val pair = iterator.next()
			val v = pair.value
			if (v == null) entryBuilder.NullEntry()
			else type.toEntry(v, mapType.keyToName(pair.key), entryBuilder)
		}
	}
}

/*Consumer*/
class MapConsumer<K: Any, V: Any>(private val mapType: MapType<K, V>, private var initial: Boolean = true): EntryConsumer<MutableMap<*, *>> {
	@Suppress("UNCHECKED_CAST")
	private var instance = mapType.newInstance() as MutableMap<Any, Any?>
	private val type = mapType.valueType
	
	override fun output() = instance
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		val v = type.fromEntries(subEntries, expectNames)
		addEntry(v, name)
	}
	
	private fun addEntry(value: V?, name: String?) {
		if (name != null) mapType.nameToKey(name)?.let {  instance.put(it, value) }
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: String): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: Long): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: Int): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: Double): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: Float): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeEntry(name: String?, value: Boolean): Unit = addEntry(type.fromEntry(value), name)
	override fun consumeNullEntry(name: String?): Unit = addEntry(null, name)
}







/* RAW MAP  */

/** The factory for MutableMap<Any, Any?> serialization form. */
object RawMapFactory: ProduceFactory<MutableMap<Any, Any?>, MutableMap<Any, Any?>> {
	override fun invoke(input: MutableMap<Any, Any?>): EntryProvider<MutableMap<Any, Any?>> = RawMapProvider(input)
	override fun invoke(): EntryConsumer<MutableMap<Any, Any?>> = RawMapConsumer()
}


/* Provider */
class RawMapProvider(override val input: MutableMap<Any, Any?>, private var initial: Boolean = true): EntryProvider<MutableMap<Any, Any?>> {
	private val iterator = input.iterator() as Iterator<Map.Entry<Any, Any?>>
	
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		return if (initial) run { initial = false; entryBuilder.StartEntry(null, true) }
		else if (!iterator.hasNext()) entryBuilder.EndEntry()
		else {
			val pair = iterator.next()
			val v = pair.value
			if (v == null) entryBuilder.NullEntry()
			else {
				val type = AnyType.detectType(v)
				if (type == null) StringType.toEntry(v.toString(), pair.key.toString(), entryBuilder)
				else type.toEntry(v, pair.key.toString(), entryBuilder)
			}
		}
	}
}

/*Consumer*/
// todo typeUtils detect type?
class RawMapConsumer(private var initial: Boolean = true): EntryConsumer<MutableMap<Any, Any?>> {
	var instance = RawMapType.newInstance()
	
	override fun output() = instance
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		val v = subEntries.intercept(if (expectNames) RawMapConsumer(false) else RawCollectionConsumer(false))
		addEntry(v, name)
	}
	
	private fun addEntry(value: Any?, name: String?) {
		if (name != null) instance.put(name, value)
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: String): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: Long): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: Int): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: Double): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: Float): Unit = addEntry(value, name)
	override fun consumeEntry(name: String?, value: Boolean): Unit = addEntry(value, name)
	override fun consumeNullEntry(name: String?): Unit = addEntry(null, name)
}


