package just4fun.holomorph.mains

import just4fun.holomorph.*
import just4fun.holomorph.types.LongBasedType
import just4fun.holomorph.types.SchemaType
import just4fun.holomorph.forms.DefaultFactory
import just4fun.holomorph.types.CollectionType
import just4fun.holomorph.types.MapType
import just4fun.holomorph.forms_experimental.JsonFactory
import just4fun.holomorph.forms_experimental.XmlFactory
import org.jetbrains.spek.api.On
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldBeTrue
import java.util.*
import kotlin.reflect.KClass


enum class EnumValues {OK, FAIL }

class ProductionBig: Spek() { init {
	
	given("Schema normal") {
		println("${Date(2000000000000)}")
		class Simple(val x: Int, var y: String)
		
		class DateType: LongBasedType<Date>(Date::class) {
			override fun newInstance(): Date = Date()
			override fun fromValue(v: Long): Date? = Date().apply { time = v }
			override fun toValue(v: Date?): Long? = v?.time
			override fun toString(v: Date?, sequenceSizeLimit: Int): String = v?.time?.toString() ?: "null"
			override fun fromEntry(value: String): Date? = Date(value)
		}
		
		@Suppress("UNCHECKED_CAST")
		class AListType<E: Any>(elementType: Type<E>): CollectionType<ArrayList<E>, E>(ArrayList::class as KClass<ArrayList<E>>, elementType) {
			override fun newInstance(): ArrayList<E> = ArrayList()
			override fun addElement(e: E?, index: Int, seq: ArrayList<E>): ArrayList<E> = seq.also { (it as ArrayList<E?>) += e }
		}
		
		class DateMapType(kt: DateType, mt: DateType): MapType<Date, Date>(kt, mt) {
			override fun nameToKey(name: String): Date? = Date(name.toLong())
			override fun keyToName(key: Date): String = key.time.toString()
		}
		
		Types.run {
			//
//			defineType(Date::class) { DateType() }
//			defineType(ArrayList::class) { et -> AListType(et[0]) }
			val dateType = addType(DateType::class) { DateType() }
			defineType(MutableMap::class, dateType, dateType) { ts -> DateMapType(ts[0] as DateType, ts[1] as DateType) }
		}
		
		
		open class ObjBase {
			open var p0: Long = 100L
			open var p1: Int = 100
			open var p2: Short = 100
			open var p3: Byte = 100
			open var p4: Double? = 0.01
			open var p5: Float = 0.01f
			open var p6: Char = '\u1002'
			open var p7: Boolean = true
			open var p8: String? = "00Aa"
			//	var p9: STUB
			var p10: Array<String> = arrayOf("qwer", "1234")
			var p11: ByteArray = byteArrayOf(1, 2, 3)
			var p12: BooleanArray = booleanArrayOf(true, false, true)
			var p13: List<Char>? = listOf('a', 'A', '0')
			var p14: MutableList<Float> = mutableListOf(0.12f, 12.0f)
			var p15: Set<Double>? = setOf(123.01, 0.123)
			var p16: MutableSet<Double> = mutableSetOf(123.01, 0.123)
			@DefineType(DateType::class)
			var p17: Date = Date(1234567890)
			var p18: List<Date?>? = listOf(Date(1234), Date(5678))
			var p19: Array<Array<String>> = arrayOf(arrayOf("asd", "123"), arrayOf("xyz", "555"))
			var p20: Simple = Simple(123, "ABC")
			var p21: MutableSet<Simple?>? = mutableSetOf(Simple(123, "ABC"), Simple(344, "XZY"))
			var p22: MutableMap<Date, Date?> = mutableMapOf(Date(1L) to Date(1L), Date(2L) to Date(2L))
			var p23: MutableMap<String, Any?> = mutableMapOf("k0" to 22, "k1" to listOf("ok", "oops"), "k2" to arrayOf("x", "8"), "k3" to mapOf("a" to 12, 12 to "a"))
			var p24: Array<Any?>? = arrayOf(123, "abc", listOf("ok", "oops"), arrayOf("x", "8"), mapOf("a" to 12, 12 to "a"))
			@DefineType(AListType::class)
			val p25: ArrayList<Simple> = ArrayList<Simple>().apply { add(Simple(1, "abc")) }
			val p26 = Triple(true, 1, "ok")
			val p27 = EnumValues.FAIL
		}
		
		class ObjN: ObjBase()
		
		class ObjC(override var p0: Long, override var p1: Int, override var p2: Short, override var p3: Byte, override var p4: Double?, override var p5: Float, override var p6: Char, override var p7: Boolean, override var p8: String?): ObjBase()
		
		val schN = SchemaType(ObjN::class)
		val schC = SchemaType(ObjC::class)
		val objN = ObjN()
		val objC = ObjC(201L, 202, 203, 104, 20.5, 20.6f, 'S', true, "manual")
		
		fun <O: Any, T: Any> test(on: On, obj: O, schema: SchemaType<O>, providerFactory: EntryProviderFactory<T>, consumerFactory: EntryConsumerFactory<T>, info: String) {
			println("TEST::   Obj: ${obj::class.simpleName}  ;  Prod: ${providerFactory::class.simpleName};  Cons: ${consumerFactory::class.simpleName}")
			val prod1 = schema.instanceTo(obj, consumerFactory).valueOrThrow
			val obj1 = schema.instanceFrom(prod1, providerFactory).valueOrNull
//			println("Obj1? ${obj1 != null};  Pro1\n$prod1")
			on.it(info) {
				if (obj1 == null) println("Obj1 is null")
				else if (!schema.equal(obj, obj1)) println("Prod\n$prod1\nprod1\n${schema.instanceTo(obj1!!, consumerFactory)}")
				shouldBeTrue(schema.equal(obj, obj1))
			}
		}
		
		
		on("Json Factoy") {
			val factory = JsonFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
		on("Default Factoy") {
			val factory = DefaultFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
		on("XML Factoy") {
			val factory = XmlFactory
			test(this, objN, schN, factory, factory, "without constructor")
			test(this, objC, schC, factory, factory, "with constructor")
		}
	}
	
}
	// todo test extreme cases (empty schema/seq etc)
	// todo test Sequence read/write
}
