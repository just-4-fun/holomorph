package just4fun.holomorph.forms_experimental

import com.fasterxml.jackson.core.JsonFactory as JFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken.*
import just4fun.holomorph.*
import java.io.StringWriter




object JsonFactory : ProduceFactory<String, String> {
	private val factory = JFactory()
	override fun invoke(input: String): EntryProvider<String> = JsonProvider(input, factory.createParser(input))
	override fun invoke(): EntryConsumer<String> = JsonConsumer(factory.createGenerator(StringWriter()))
}


/* READER */

class JsonProvider(override val input: String, val parser: JsonParser) : EntryProvider<String> {
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		val token = parser.nextValue() ?: return entryBuilder.EndEntry()
		val name = parser.currentName
		return when {
			token >= VALUE_EMBEDDED_OBJECT -> when (token) {
				VALUE_STRING -> entryBuilder.Entry(name, parser.text)
				VALUE_NUMBER_INT -> entryBuilder.Entry(name, parser.valueAsLong)
				VALUE_NUMBER_FLOAT -> entryBuilder.Entry(name, parser.valueAsDouble)
				VALUE_NULL -> entryBuilder.NullEntry(name)
				VALUE_FALSE -> entryBuilder.Entry(name, parser.valueAsBoolean)
				VALUE_TRUE -> entryBuilder.Entry(name, parser.valueAsBoolean)
				else -> entryBuilder.Entry(name, parser.text)
			}
			token == START_ARRAY -> entryBuilder.StartEntry(name, false)
			token == START_OBJECT -> entryBuilder.StartEntry(name, true)
			else -> entryBuilder.EndEntry()
		}
	}
}


/* WRITER */

class JsonConsumer(private val generator: JsonGenerator) : EntryConsumer<String> {
	override fun output(): String {
		generator.close()
		return generator.outputTarget?.toString() ?: "{}"
	}

	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		if (name != null) generator.writeFieldName(name)
		if (expectNames) generator.writeStartObject() else generator.writeStartArray()
		subEntries.consume()
		if (expectNames) generator.writeEndObject() else generator.writeEndArray()
	}

	override fun consumeEntry(name: String?, value: String) {
		if (name != null) generator.writeFieldName(name)
		generator.writeString(value)
	}

	override fun consumeEntry(name: String?, value: Long) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun consumeEntry(name: String?, value: Int) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun consumeEntry(name: String?, value: Double) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun consumeEntry(name: String?, value: Float) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNumber(value)
	}

	override fun consumeEntry(name: String?, value: Boolean) {
		if (name != null) generator.writeFieldName(name)
		generator.writeBoolean(value)
	}

	override fun consumeNullEntry(name: String?) {
		if (name != null) generator.writeFieldName(name)
		generator.writeNull()
	}

//	override fun consumeEntry(value: ByteArray, typeName: String?, ordinal: Int) {
//		if (typeName != null) generator.writeFieldName(typeName)
//		// todo ???
//		generator.writeStartArray()
//		value.forEachIndexed { ix, b ->  consumeEntry(b, null, ix)}
//		generator.writeEndArray()
//	}
}
