package just4fun.holomorph.forms

import just4fun.holomorph.*
import just4fun.holomorph.types.SchemaType


/** The factory for Schema serialization form. */
class SchemaFactory<T: Any>(val schema: SchemaType<T>): ProduceFactory<T, T> {
	override fun invoke(input: T): EntryProvider<T> = SchemaProvider(input, schema)
	override fun invoke(): EntryConsumer<T> = SchemaConsumer(schema)
}


/* Provider */

class SchemaProvider<T: Any>(override val input: T, val schema: SchemaType<T>, props: List<Property<*>>? = null, private val nameless: Boolean? = null, private var initial: Boolean = true): EntryProvider<T> {
	private var index = 0
	private val named = props != null || nameless == false || (nameless == null && !schema.nameless)
	private val props = props ?: schema.properties
	
	@Suppress("UNCHECKED_CAST")
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		return if (initial) {
			entryBuilder.context.nameless = nameless
			initial = false; entryBuilder.StartEntry(null, named)
		} else if (index >= props.size) entryBuilder.EndEntry()
		else {
			val prop = props[index++] as Property<Any>
			val v = prop[input]
			if (v == null) entryBuilder.NullEntry(prop.name)
			else prop.type.toEntry(v, prop.name, entryBuilder)
		}
	}
}


/* Consumer */

class SchemaConsumer<T: Any>(schema: SchemaType<T>): EntryConsumer<T> by if (schema.emptyConstr) SchemaConsumerN(schema) else SchemaConsumerV(schema)



internal class SchemaConsumerN<T: Any>(val schema: SchemaType<T>, private var initial: Boolean = true, instance: T? = null): EntryConsumer<T> {
	private val instance: T = instance ?: schema.newInstance()
	@Suppress("UNCHECKED_CAST")
	private val props = schema.propertiesMap as Map<String, Property<Any>>
	private val propsList = schema.properties
	private var index = -1
	private val nextProp: Property<Any>? get() = if (++index >= propsList.size) null else propsList[index]
	
	override fun output(): T = instance
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntries(subEntries, expectNames))
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: String): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: Long): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: Int): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: Double): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: Float): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeEntry(name: String?, value: Boolean): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromEntry(value))
	}
	
	override fun consumeNullEntry(name: String?): Unit {
		val prop = if (name == null) nextProp else props[name]
		prop?.setChecked(instance, prop.type.fromNullEntry())
	}
}



internal class SchemaConsumerV<T: Any>(val schema: SchemaType<T>, private var initial: Boolean = true): EntryConsumer<T> {
	private val values = Array<Any?>(schema.allSize) {}
	private val params = schema.paramsMap!!
	private val paramSize = schema.paramsSize
	private val props = schema.propertiesMap
	private val propsList = schema.properties
	private var index = -1
	private val nextProp: Property<*>? get() = if (++index >= propsList.size) null else propsList[index]
	
	override fun output(): T = schema.newInstance(values)
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (initial) run { initial = false; subEntries.consume(); return }
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntries(subEntries, expectNames) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntries(subEntries, expectNames) }
	}
	
	override fun consumeEntry(name: String?, value: ByteArray): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: String): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: Long): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: Int): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: Double): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: Float): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeEntry(name: String?, value: Boolean): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromEntry(value) }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromEntry(value) }
	}
	
	override fun consumeNullEntry(name: String?): Unit {
		var prop: Property<*>? = null
		val n = name ?: nextProp?.let { prop = it; it.name } ?: return
		params[n]?.let { values[it.index] = it.type.fromNullEntry() }
		  ?: (prop ?: props[n])?.let { values[paramSize + it.ordinal] = it.type.fromNullEntry() }
	}
	
}
