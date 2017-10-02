package just4fun.holomorph

import just4fun.kotlinkit.Result



/*CONSTRUCTION*/

/** Carries out the production of an [EntryConsumer.output] from a [EntryProvider.input]. Supposed to be used via [Produce.invoke] or [Reflexion] methods.

> [See the guide](https://just-4-fun.github.io/holomorph/#serialization-forms)
 */
class Produce: EntryBuilder, Entry, EnclosedEntries, ProduceContext {
// TODO: schema guide for non-schema conversions: @ Produce(xml, json, schema)
	companion object {
		/** Creates a [Produce] instance and starts production.
		 * @return [Result.Value] with an output [OUT] or [Result.Failure] with an failure.
		 * [See the guide](https://just-4-fun.github.io/holomorph/#serialization-forms)
		 */
		operator fun <IN: Any, OUT: Any> invoke(provider: EntryProvider<IN>, consumer: EntryConsumer<OUT>): Result<OUT> {
			return Result { Produce().start(provider, consumer) }
		}
	}
	
	override val context = this
	override var nameless: Boolean? = null
	
	private lateinit var provider: EntryProvider<*>
	private lateinit var consumer: EntryConsumer<*>
	private var startContainer = false
	private var endContainer = false
	private var isNamedContainer = false
	private var startEntryName: String? = null
	private var interceptor: EntryProvider<*>? = null
	private var consumed = false
	
	private fun <IN: Any, OUT: Any> start(p: EntryProvider<IN>, c: EntryConsumer<OUT>): OUT {
		provider = p
		consumer = c
		processContainer()
		return c.output()
	}
	
	private fun processContainer() {
		val named = isNamedContainer
		consumed = true
		do {
			isNamedContainer = named
			provider.provideNextEntry(this, isNamedContainer)
			// context changed
			if (startContainer) {
				startContainer = false
				consumed = false
				val temp = provider
				provider = interceptor ?: provider
				// causes Consumer to call subEntries.consume > processContainer()
				consumer.consumeEntries(startEntryName, this, isNamedContainer)
				provider = temp
				if (!consumed) intercept(SkipAllConsumer)
			}
		} while (!endContainer.apply { endContainer = false })
	}
	
	override fun consume() = processContainer()
	override fun <T: Any> intercept(interceptor: EntryConsumer<T>): T? {
		val temp = consumer
		consumer = interceptor
		processContainer()
		consumer = temp
		return interceptor.output()
	}
	
	override fun StartEntry(name: String?, containNames: Boolean, interceptor: EntryProvider<*>?): Entry = apply {
		startEntryName = if (isNamedContainer) name else null
		this.interceptor = interceptor
		isNamedContainer = containNames
		startContainer = true
		/** Notes: [EntryConsumer.consumeEntries] can not be called from here since underlying [EntryProvider.provideNextEntry] call  have to finish its execution. */
	}
	
	override fun Entry(name: String?, value: String): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: ByteArray): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Long): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Int): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Short): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Byte): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Char): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Double): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Float): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun Entry(name: String?, value: Boolean): Entry = apply {
		consumer.consumeEntry(if (isNamedContainer) name else null, value)
	}
	
	override fun NullEntry(name: String?): Entry = apply {
		consumer.consumeNullEntry(if (isNamedContainer) name else null)
	}
	
	override fun EndEntry(): Entry = apply { endContainer = true }
}


/* ENTRY */
/** Entry marker. The only intended use is to be returned from the body of [EntryProvider.provideNextEntry] method by calling its `entryBuilder`'s methods.  [See the guide](https://just-4-fun.github.io/holomorph/#serialization-forms)
 */
interface Entry


/* CONTENT CONSUMERS */
/** Represents the current entry value as a container of entries. */
interface EnclosedEntries {
	/** Indicates that the current entry value is a container of entries and calls [EntryConsumer.consumeEntries] of the [interceptor].
	 * @return resulting [interceptor]'s [EntryConsumer.output] */
	fun <T: Any> intercept(interceptor: EntryConsumer<T>): T?
	
	/** Indicates that the current entry value is a container of entries and calls [EntryConsumer.consumeEntries] of the current [EntryConsumer] */
	fun consume(): Unit
}


/* ENTRY BUILDER */
/** Helper object to create an Entry of particular kind that should be returned from the body of [EntryProvider.provideNextEntry] method.  [See the guide](https://just-4-fun.github.io/holomorph/#serialization-forms) */
interface EntryBuilder {
	/** The context of the enclosing [Produce] context. */
	val context: ProduceContext
	
	/** Indicates the end of the current enclosed container of entries. */
	fun EndEntry(): Entry
	
	/** New entry which value is an enclosed container of entries.
	 * @param [name] is the name of the current entry if one is  named;
	 * @param [containNames] tells that the enclosed entries are named if true or nameless if false;
	 * @param [interceptor] tells [Produce] context that the enclosed content will be provided by [interceptor] if non-null; */
	fun StartEntry(name: String? = null, containNames: Boolean, interceptor: EntryProvider<*>? = null): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: String): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: ByteArray): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Long): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Int): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Short): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Byte): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Char): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Double): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Float): Entry
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun Entry(name: String? = null, value: Boolean): Entry
	
	/** New entry with the value of 'null' and the name [name] if named or `null` if nameless*/
	fun NullEntry(name: String? = null): Entry
}


/** The context of the enclosing [Produce] context */
interface ProduceContext {
	/** if false - all containers should be named; true - all containers should be nameless (compact); null - at container's discretion.  */
	var nameless: Boolean?
}


/* FACTORY */

/** Combines both [EntryProviderFactory] and [EntryConsumerFactory] of the particular serialization form. */
interface ProduceFactory<IN: Any, OUT: Any>: EntryProviderFactory<IN>, EntryConsumerFactory<OUT>

/* PROVIDER */

/** Constructs an [EntryProvider] instance with supplied 'input' */
interface EntryProviderFactory<T: Any> {
	operator fun invoke(input: T): EntryProvider<T>
}


/** Provides entries from the [input] of [T]*/
interface EntryProvider<T: Any> {
	/** The source data of [T] in the supported serialization form to be parsed and provided as a sequence of entries..*/
	val input: T
	
	/** Disassembles the [input] into a sequence of entries returning them one by one per call  */
	fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry
}


/* CONSUMER */

/** Constructs an [EntryConsumer] instance for the target type [T].' */
interface EntryConsumerFactory<T: Any> {
	operator fun invoke(): EntryConsumer<T>
}

/** Consumes entries combining intermediate results into the aggregate [output] of [T]*/
interface EntryConsumer<T: Any> {
	/** The aggregate result of consumed entries. */
	fun output(): T
	
	/** Indicates that the new entry value is an enclosed container of entries which can be consumed or intercepted via [subEntries]
	 * @param [name]  the name of the current entry;
	 * @param [subEntries] use to tells the enclosing [Produce] context to [EnclosedEntries.consume] enclosed entries by this object or to [EnclosedEntries.intercept] them by an `interceptor` [EntryConsumer].
	 * @param [expectNames] indicates whether sub-entries are named (`true`) or nameless (`false`)
	 */
	fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: String): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Long): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Int): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Double): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Float): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Boolean): Unit
	
	/** New entry with the value of `null` and the name [name] if named or `null` if nameless*/
	fun consumeNullEntry(name: String?): Unit
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Short): Unit = consumeEntry(name, value.toInt())
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Byte): Unit = consumeEntry(name, value.toInt())
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless*/
	fun consumeEntry(name: String?, value: Char): Unit = consumeEntry(name, value.toInt())
	
	/** New entry with the value [value] and the name [name] if named or `null` if nameless. If not overridden transforms the [value] into the enclosed container of entries each of which is a [Byte] value. */
	fun consumeEntry(name: String?, value: ByteArray) {
		val entries = object: EnclosedEntries {
			override fun consume() = run { for (n in value) consumeEntry(null, n) }
			override fun <T: Any> intercept(interceptor: EntryConsumer<T>): T? = throw UnsupportedOperationException()
		}
		consumeEntries(name, entries, false)
	}
}




interface Producer<T: Any> {
	/** Produces an output of [T] from [input] of [D] by means of [EntryProvider] supplied by the [factory].and [EntryConsumer] supplied implicitely for type [T] */
	fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<T>
	
	/** Produces an output of [D] from [input] of [T] by means of [EntryProvider] supplied implicitely for type [T] and [EntryConsumer] supplied by the [factory] */
	fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>): Result<D>
}




/* DUMMY Consumer */
object SkipAllConsumer: EntryConsumer<Any> {
	override fun output(): Any = Unit
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) = subEntries.consume()
	override fun consumeEntry(name: String?, value: String) = Unit
	override fun consumeEntry(name: String?, value: Long) = Unit
	override fun consumeEntry(name: String?, value: Int) = Unit
	override fun consumeEntry(name: String?, value: Double) = Unit
	override fun consumeEntry(name: String?, value: Float) = Unit
	override fun consumeEntry(name: String?, value: Boolean) = Unit
	override fun consumeEntry(name: String?, value: ByteArray) = Unit
	override fun consumeNullEntry(name: String?) = Unit
}






/** Type resolver [Type] implements this interface to act in production by the [Produce].*/
interface EntryHelper<T: Any> {
	/** Should call the relevant [entryBuilder] method returning an [Entry] with the name [name] and the value [value] */
	fun toEntry(value: T, name: String?, entryBuilder: EntryBuilder): Entry
	
	/** Converts the enclosed container of entries [subEntries] to a value of [T] or null if it's impossible. [expectNames] indicates the enclosed container is named (`true`) or nameless (`false`) */
	fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: ByteArray): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: String): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Long): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Int): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Short): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Byte): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Char): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Double): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Float): T?
	
	/** Converts the [value] to  the type [T] or null if it's impossible. */
	fun fromEntry(value: Boolean): T?
	
	/** Converts `null` to  the type [T] if required. */
	fun fromNullEntry(): T? = null
}


/**@suppress [EntryHelperDefault]*/
interface EntryHelperDefault<T: Any>: EntryHelper<T> {
	override fun fromEntry(value: ByteArray): T? = null
	override fun fromEntry(value: String): T? = null
	override fun fromEntry(value: Long): T? = null
	override fun fromEntry(value: Int): T? = null
	override fun fromEntry(value: Short): T? = null
	override fun fromEntry(value: Byte): T? = null
	override fun fromEntry(value: Char): T? = null
	override fun fromEntry(value: Double): T? = null
	override fun fromEntry(value: Float): T? = null
	override fun fromEntry(value: Boolean): T? = null
}





/** Used with the annotation @[ValueInterceptor] as its parameter. Aimed to intercept values of consuming entries and convert them to the type [T] in custom way. If a method returns `null` the call is redirected to original type resolver ([Type]). */
interface ValueInterceptor<T: Any> {
	/** intercepts an entry which value is an enclosed container of entries and converts it  to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(subEntries: EnclosedEntries, expectNames: Boolean): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: ByteArray): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: String): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Long): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Int): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Short): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Byte): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Char): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Double): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Float): T? = null
	
	/** intercepts an entry and converts the value to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun intercept(value: Boolean): T? = null
	
	/** intercepts an null-entry and converts it to the type [T] or returns `null` thereby redirecting the call to original type resolver */
	fun interceptNull(): T? = null
}
