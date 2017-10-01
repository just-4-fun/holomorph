package just4fun.holomorph.forms

import just4fun.holomorph.*

/** The factory for JSON serialization form. */
object DefaultFactory: ProduceFactory<String, String> {
	override fun invoke(input: String): EntryProvider<String> = DefaultProvider(input)
	override fun invoke(): EntryConsumer<String> = DefaultConsumer()
}


/* READER */

class DefaultProvider(override val input: String): EntryProvider<String> {
	private val chars: CharArray = input.toCharArray()
	private var cursor = 0
	private val lastIndex = chars.size - 1
	private var char: Char = if (lastIndex < 0) '\u0000' else chars[0]
	
	override fun provideNextEntry(entryBuilder: EntryBuilder, provideName: Boolean): Entry {
		skipSpacesSafely()
		val entry = if (cursor >= lastIndex) entryBuilder.EndEntry()
		else if (char == ']' || char == '}') {
			stepSafely()
			skipSpacesSafely()
			if (char == ',') step()
			entryBuilder.EndEntry()
		} else {
			val name = if (provideName) readName() else null
			//
			when (char) {
				'[' -> run { step(); entryBuilder.StartEntry(name, false) }
				'{' -> run { step(); entryBuilder.StartEntry(name, true) }
				else -> {
					val entry = when (char) {
						'"' -> entryBuilder.Entry(name, nextString())
						in '0'..'9', '-' -> nextNum(entryBuilder, name)
						else -> nextLiteral(entryBuilder, name)
					}
					skipSpaces()
					if (char == ',') step()
					entry
				}
			}
		}
		//
		//		println("NXT>> ${if(currentContainerIsObject())"input" else "input"};  $entry;")
		return entry
	}
	
	private fun readName(): String {
		val name = if (char == '"') nextString()
		else {
			val buff = StringBuilder()
			do {
				buff.append(char)
				step()
			} while (char != ':' && char != ' ')
			buff.toString()
		}
		while (char != ':') step()
		step()
		skipSpaces()
		return name
	}
	
	private fun nextString(): String {
		step()
		val buff = StringBuilder()
		while (char != '"') {
			if (char == '\\') chars[cursor + 1].let { if (it == '"' || it == '\\') step() }
			buff.append(char)
			step()
		}
		step()
		return buff.toString()
	}
	
	private fun nextNum(entryBuilder: EntryBuilder, name: String?): Entry {
		val buff = StringBuilder()
		var frac = false
		var valid = true
		val neg = if (char == '-') run { step(); true } else false
		val n = if (neg) 1 else 0
		//
		while (char != ',' && char != ']' && char != '}' && char != ' ') {
			when {
				char >= '0' && char <= '9' -> if (!frac && cursor - n > 17) valid = false// long overflow
				char == '.' -> if (frac) valid = false else frac = true
				else -> valid = false
			}
			buff.append(char)
			step()
		}
		val v = buff.toString()
		return if (!valid) entryBuilder.Entry(name, if (neg) "-$v" else v)
		else if (v.length == 0) entryBuilder.Entry(name, 0)
		else if (frac) if (neg) entryBuilder.Entry(name, -v.toDouble()) else entryBuilder.Entry(name, v.toDouble())
		else if (v.length < 10) if (neg) entryBuilder.Entry(name, -v.toInt()) else entryBuilder.Entry(name, v.toInt())
		else if (neg) entryBuilder.Entry(name, -v.toLong()) else entryBuilder.Entry(name, v.toLong())
	}
	
	private fun nextLiteral(entryBuilder: EntryBuilder, name: String?): Entry = when (char) {
		'n' -> if (step() == 'u' && step() == 'l' && step() == 'l' && stepIsEnd()) entryBuilder.NullEntry(name) else fail()
		'f' -> if (step() == 'a' && step() == 'l' && step() == 's' && step() == 'e' && stepIsEnd()) entryBuilder.Entry(name, false) else fail()
		't' -> if (step() == 'r' && step() == 'u' && step() == 'e' && stepIsEnd()) entryBuilder.Entry(name, true) else fail()
		else -> {
			val buff = StringBuilder()
			do buff.append(char) while (!stepIsEnd())
			entryBuilder.Entry(name, buff.toString())
		}
	}
	
	private fun stepIsEnd(): Boolean {
		if (cursor < lastIndex) char = chars[++cursor] else fail()
		return char == ',' || char == ']' || char == '}' || char == ' '
	}
	
	private fun skipSpaces(): Char {
		while (char == ' ') char = chars[++cursor]
		return char
	}
	
	private fun skipSpacesSafely(): Char {
		while (char == ' ') if (cursor < lastIndex) char = chars[++cursor] else char = '\u0000'
		return char
	}
	
	private fun stepSafely(): Char {
		if (cursor < lastIndex) char = chars[++cursor] else char = '\u0000'
		return char
	}
	
	private fun step(): Char {
		char = chars[++cursor]
		//		if (cursor < lastIndex) char = chars[++cursor] else fail()
		return char
	}
	
	private fun fail(): Nothing {
		val msg = if (cursor >= lastIndex) "Unexpected end of input" else "Parsing failed at position: $cursor; char: '$char'."
		throw Exception(msg)
	}
}


/* WRITER */

class DefaultConsumer: EntryConsumer<String> {
	private val buff = StringBuilder()
	private var begin = true
	
	override fun output(): String = if (buff.isNotEmpty()) buff.toString() else "{}"
	
	override fun consumeEntries(name: String?, subEntries: EnclosedEntries, expectNames: Boolean) {
		checkBegin(true)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(if (expectNames) '{' else '[')
		subEntries.consume()
		buff.append(if (expectNames) '}' else ']')
	}
	
	override fun consumeEntry(name: String?, value: String) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append('"').append(value).append('"')
	}
	
	override fun consumeEntry(name: String?, value: Long) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}
	
	override fun consumeEntry(name: String?, value: Int) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}
	
	override fun consumeEntry(name: String?, value: Double) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}
	
	override fun consumeEntry(name: String?, value: Float) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}
	
	override fun consumeEntry(name: String?, value: Boolean) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append(value)
	}
	
	override fun consumeNullEntry(name: String?) {
		checkBegin(false)
		if (name != null) buff.append('"').append(name).append('"').append(':')
		buff.append("null")
	}
	
	private fun checkBegin(reset: Boolean) {
		if (begin) begin = false else buff.append(',')
		if (reset) begin = true
	}
}

