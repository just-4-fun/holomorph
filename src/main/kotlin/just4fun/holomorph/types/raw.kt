package just4fun.holomorph.types

import just4fun.holomorph.*
import just4fun.kotlinkit.Result
import just4fun.holomorph.forms.RawCollectionConsumer
import just4fun.holomorph.forms.RawCollectionProvider
import just4fun.holomorph.forms.RawMapConsumer
import just4fun.holomorph.forms.RawMapProvider
import kotlin.reflect.KClass



/* RAW COLLECTION */
object RawCollectionType: Type<Collection<Any?>>, Producer<Collection<Any?>>, EntryHelperDefault<Collection<Any?>> {
	override final val typeKlas = Collection::class
	override val typeName = "${typeKlas}<Any?>"
	
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<Collection<Any?>> = Produce(factory(input), RawCollectionConsumer())
	override fun <D: Any> instanceTo(input: Collection<Any?>, factory: EntryConsumerFactory<D>): Result<D> = Produce(RawCollectionProvider(input), factory())
	
	override fun newInstance(): MutableList<Any?> = mutableListOf()
	
	override fun asInstance(v: Any): Collection<*>? = when (v) {
		is Collection<*> -> v
		is Array<*> -> newInstance().apply { for (e in v) add(e) }
	// case: xml node wrongly detected as Object due to limited info
		is Map<*, *> -> {
			var coll = newInstance()
			for (e in v) coll.add(0, e.value)
			coll
		}
		else -> evalError(v, this)
	}
	
	fun addElement(e: Any?, index: Int, seq: Collection<Any?>): Collection<Any?> {
		return seq + e
	}
	
	fun onComplete(seq: Collection<Any?>, expectedSize: Int): Collection<Any?> = seq
	
	override fun copy(v: Collection<*>?, deep: Boolean): Collection<*>? {
		return if (v == null || !deep) v
		else newInstance().apply { for (e in v) add(AnyType.copy(e, deep)) }
	}
	
	override fun equal(v1: Collection<*>?, v2: Collection<*>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		val itr1 = v1.iterator()
		val itr2 = v2.iterator()
		while (itr1.hasNext()) if (!AnyType.equal(itr1.next(), itr2.next())) return false
		true
	})
	
	override fun toString(v: Collection<*>?, sequenceSizeLimit: Int): String {
		return if (v == null) "null"
		else v.map { e -> AnyType.toString(e, sequenceSizeLimit) }.joinToString(",", "[", "]", sequenceSizeLimit)
	}
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = subEntries.intercept(RawCollectionConsumer(false))
	override fun toEntry(value: Collection<Any?>, name: String?, entryBuilder: EntryBuilder): Entry {
		return entryBuilder.StartEntry(name, false, RawCollectionProvider(value, false))
	}
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other
	
}






/* RAW MAP */
object RawMapType: Type<MutableMap<Any, Any?>>, Producer<MutableMap<Any, Any?>>, EntryHelperDefault<MutableMap<Any, Any?>> {
	@Suppress("UNCHECKED_CAST")
	override final val typeKlas = MutableMap::class as KClass<MutableMap<Any, Any?>>
	
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<MutableMap<Any, Any?>> = Produce(factory(input), RawMapConsumer())
	override fun <D: Any> instanceTo(input: MutableMap<Any, Any?>, factory: EntryConsumerFactory<D>): Result<D> = Produce(RawMapProvider(input), factory())
	
	override fun newInstance(): MutableMap<Any, Any?> = mutableMapOf()
	
	@Suppress("UNCHECKED_CAST")
	override fun asInstance(v: Any): MutableMap<Any, Any?>? = when (v) {
		is Map<*, *> -> v as? MutableMap<Any, Any?>
		else -> evalError(v, this)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun equal(v1: MutableMap<Any, Any?>?, v2: MutableMap<Any, Any?>?): Boolean {
		return v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			val m1 = v1 as Map<Any?, Any?>
			val m2 = v2 as Map<Any?, Any?>
			val m0 = m2.keys.associateBy { it.toString() }// map ["key" -> key]
			m1.entries.all { entry -> m0.containsKey(entry.key.toString()) && AnyType.equal(entry.value, m2[m0[entry.key.toString()]]) }
		})
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun copy(v: MutableMap<Any, Any?>?, deep: Boolean) = if (!deep || v == null) v else {
		val c = newInstance()
		(v as Map<Any, Any?>).forEach { entry -> c[entry.key] = AnyType.copy(entry.value, deep) }
		c
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun toString(v: MutableMap<Any, Any?>?, sequenceSizeLimit: Int): String {
		return if (v == null) "null"
		else (v as Map<Any?, Any?>).map { e -> "${e.key}=${AnyType.toString(e.value, sequenceSizeLimit)}" }.joinToString(",", "{", "}", sequenceSizeLimit)
	}
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = if (expectNames) subEntries.intercept(RawMapConsumer(false)) else default
	override fun toEntry(value: MutableMap<Any, Any?>, name: String?, entryBuilder: EntryBuilder): Entry {
		return entryBuilder.StartEntry(name, true, RawMapProvider(value, false))
	}
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other
}

