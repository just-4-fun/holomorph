package just4fun.holomorph.mains

import just4fun.holomorph.DefineSchema
import just4fun.holomorph.Type
import just4fun.holomorph.Types
import just4fun.holomorph.types.CollectionType
import just4fun.holomorph.mains.javas.Point4d
import just4fun.holomorph.measureTime
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType

val N = 10

fun main(a: Array<String>) {
	measurePrims()
	measureString()
	measurePrimArrays()
	measureArrayPrims()
	measureArrayAny()
	measureArrayString()
	measureArrayUnknown()
	measureCollStandard()
	measureArrayList()
	measureMap()
	measureUnit()
	measureMix1()
	measureEnum()
	discover(listOf("ok")::class.starProjectedType)// converts to java.utils... some private class
	//
	class Obj(@DefineSchema(arrayOf("x", "y", "z", "w")) val point: Point4d, val a: Int, val b: Int)
	discover(Obj::class.starProjectedType)
}

fun discover(types: List<KType>) = types.forEach { discover(it) }
fun discover(type: KType) = Types.resolve(type, type.classifier as KClass<*>) ?: println("Failed dicovery of $type")
fun types(klas: KClass<*>) = klas.memberProperties.map { it.returnType }

fun measurePrims() {
	val klasses = listOf(Int::class, Long::class, Short::class, Byte::class, Double::class, Float::class, Char::class, Boolean::class)
	val types = klasses.map { it.starProjectedType }
	measureTime("PRIMS", N) { discover(types) }
}//20000 ns  /8

fun measureString() {
	//val type = java.lang.String::class.starProjectedType
	val type = String::class.starProjectedType
	measureTime("STRING", N) { discover(type) }
}//4000 ns

fun measurePrimArrays() {
	val klasses = listOf(IntArray::class, LongArray::class, ShortArray::class, ByteArray::class, DoubleArray::class, FloatArray::class, CharArray::class, BooleanArray::class)
	val types = klasses.map { it.starProjectedType }// turns all to Array<*>
	measureTime("PRIM ARRs", N) { discover(types) }
}//40000 ns  /8

fun measureArrayPrims() {
	class Obj(val v1: Array<Int>, val v2: Array<Long>, val v3: Array<Short>, val v4: Array<Byte>, val v5: Array<Double>, val v6: Array<Float>, val v7: Array<Char>, val v8: Array<Boolean>)
	measureTime("ARR PRIMs", N) { discover(types(Obj::class)) }
}//50000 ns  /8

fun measureArrayAny() {
	class Obj(val v1: Array<Any?>)
	measureTime("ARR ANY", N) { discover(types(Obj::class)) }
}//10000 ns

fun measureArrayString() {
	//val setArray = run{ PropType.cache.add(ARRAYof(STRING))}
	class Obj(val v1: Array<String?>)
	measureTime("ARR STR", N) { discover(types(Obj::class)) }
}//10000 ns

fun measureArrayUnknown() {
	class Obj(val v1: Array<Obj>, val v2: Array<Obj?>)
	measureTime("ARR ???", N) { discover(types(Obj::class)) }
}//11000 ns

fun measureCollStandard() {
	class Obj(val v1: List<Int?>, val v2: MutableList<Int>, val v3: Set<Int?>, val v4: MutableSet<Int>)
	measureTime("COLL STD ANY", N) { discover(types(Obj::class)) }
}//105000 ns  /4

fun measureArrayList() {
	class Obj(val v1: ArrayList<Int?>, val v2: ArrayList<Obj?>)
	@Suppress("UNCHECKED_CAST")
	class AListType<E: Any>(elementType: Type<E>): CollectionType<ArrayList<*>, E>(ArrayList::class, elementType) {
		override fun newInstance(): ArrayList<E>  = ArrayList()
		override fun addElement(e: E?, index: Int, seq: ArrayList<*>): ArrayList<E> = (seq as ArrayList<E>).also { (it as ArrayList<E?>) += e }
	}
	Types.defineType(ArrayList::class) { et -> AListType(et[0]) }
	measureTime("ARRLIST ANY", N) { discover(types(Obj::class)) }
}//140000 ns  /2

fun measureMap() {
	class Obj(val v1: MutableMap<String, Any>, val v2: Map<String, Any?>)
	measureTime("MAP ANY", N) { discover(types(Obj::class)) }
}//55000 ns  /2

fun measureUnit() {
	//class UnitObj(val v1: Unit)
	class Obj(val v1: Unit, val v2: java.lang.Void)
	measureTime("UNIT", N) { discover(types(Obj::class)) }
}//26000 ns


fun measureMix1() {
	class Obj(val v0: Int, val v1: Long, val v2: Short, val v3: Byte, val v4: Char, val v5: Double, val v6: Float, val v7: Boolean, val v8: String?)
	measureTime("MIX 1", N) { discover(types(Obj::class)) }
}//30000 ns  /8

enum class Vals {OK, FAIL }

fun measureEnum() {
	class Obj(val v0: Vals)
	measureTime("ENUM", N) { discover(types(Obj::class)) }
}//30000 ns  /8







