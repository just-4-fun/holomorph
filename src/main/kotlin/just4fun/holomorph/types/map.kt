package just4fun.holomorph.types

import just4fun.holomorph.*
import just4fun.kotlinkit.Result
import just4fun.holomorph.forms.MapConsumer
import just4fun.holomorph.forms.MapProvider


/* MAP */
open class MapType<K: Any, V: Any>(val keyType: Type<K>, val valueType: Type<V>): Type<MutableMap<*, *>>, Producer<MutableMap<*, *>>, EntryHelperDefault<MutableMap<*, *>> {
	override val typeKlas = MutableMap::class
	override val typeName: String get() = "${typeKlas.simpleName}<${keyType.typeName},${valueType.typeName}>"
	
	open fun nameToKey(name: String) = keyType.fromEntry(name)
	open fun keyToName(key: K) = key.toString()
	
	override fun newInstance() = mutableMapOf<K, V>()
	override fun asInstance(v: Any): MutableMap<*, *>? = when (v) {
		is Map<*, *> -> v as? MutableMap<*, *>
		else -> evalError(v, this)
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun equal(v1: MutableMap<*, *>?, v2: MutableMap<*, *>?): Boolean {
		return v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
			v1.entries.all { entry ->
				v2.containsKey(entry.key) && valueType.equal(entry.value as V, v2[entry.key] as V)
			}
		})
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun copy(v: MutableMap<*, *>?, deep: Boolean) = if (!deep || v == null) v else {
		val m = newInstance() as MutableMap<Any, Any?>
		v.forEach { entry -> m[entry.key as Any] = valueType.copy(entry.value as? V, deep) }
		m
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun toString(v: MutableMap<*, *>?, sequenceSizeLimit: Int): String {
		return if (v == null) "null"
		else v.map { e -> "${e.key}=${valueType.toString(e.value as? V, sequenceSizeLimit)}" }.joinToString(",", "{", "}", sequenceSizeLimit)
	}
	
	
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<MutableMap<*, *>> = Produce(factory(input), MapConsumer(this))
	override fun <D: Any> instanceTo(input: MutableMap<*, *>, factory: EntryConsumerFactory<D>): Result<D> = Produce(MapProvider(input, this), factory())
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean): MutableMap<*, *>? {
		return if (expectNames) subEntries.intercept(MapConsumer(this, false)) else null
	}
	
	override fun toEntry(value: MutableMap<*, *>, name: String?, entryBuilder: EntryBuilder): Entry {
		return entryBuilder.StartEntry(name, true, MapProvider(value, this, false))
	}
	
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode() + keyType.typeKlas.hashCode() * 31 + valueType.typeKlas.hashCode() * 31
	override fun equals(other: Any?) = this === other
}