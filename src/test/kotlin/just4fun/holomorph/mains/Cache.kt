package just4fun.holomorph.mains

import just4fun.holomorph.DefineType
import just4fun.holomorph.Reflexion
import just4fun.holomorph.Type
import just4fun.holomorph.Types
import just4fun.holomorph.types.*
import just4fun.holomorph.types.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldBeFalse
import org.jetbrains.spek.api.shouldBeTrue
import java.util.*


class Cache: Spek() { init {
	given("Test Types cache") {
		
		on("Spec Array") {
			class Obj1(val a: Array<Int>)
			class Obj2(val a: Array<Int>)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(IntArray::class) != null)
			}
		}
		on("Obj Array") {
			class Obj1(val a: Array<Long?>)
			class Obj2(val a: Array<Long?>)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(Array<Long?>::class) != null)
			}
		}
		on("Enum") {
			class Obj1(val e: CacheEnum)
			class Obj2(val e: CacheEnum)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(CacheEnum::class) != null)
			}
		}
		on("@DefineType / constructType") {
			class Obj1(@DefineType(DateType::class) val v: Date)
			class Obj2(val v: Date)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(Date::class) != null)
			}
		}
		on("DefineType / buildType") {
			it("") {
				Types.clearCache(true)
				Types.defineType(Date::class){ DateType() }
				class Obj1(val v: Date)
				class Obj2(val v: Date)
				
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(Date::class) != null)
			}
		}
		on("@DefineType Collection") {
			class Obj1(@DefineType(AListType::class) val v: ArrayList<Int>)
			class Obj2(val v: ArrayList<Int>)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(ArrayList::class, arrayOf(IntType)) != null)
			}
		}
		on("buildType Collection") {
			class Obj1(@DefineType(AListType::class) val v: ArrayList<Int>)
			class Obj2(val v: ArrayList<Int>)
			
			it("") {
				Types.clearCache(true)
				Types.defineType(AListType::class) { ets -> AListType(ets[0]) }
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(ArrayList::class, arrayOf(IntType)) != null)
			}
		}
		on("Generic schema") {
			class Obj<T>(val v: T)
			class Obj1(val v: Obj<Int>)
			class Obj2(val v: Obj<Int>)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(Obj::class, arrayOf(IntType)) != null)
				shouldBeFalse(Types.getType(Obj::class, arrayOf(LongType)) != null)
			}
		}
		on("construct Generic schema") {
			class Obj1(@DefineType(ObjType::class) val v: ObjT<Int>)
			class Obj2(val v: ObjT<Int>)
			
			it("") {
				Types.clearCache(true)
				val s1 = Reflexion(Obj1::class)
				val s2 = Reflexion(Obj2::class)
				shouldBeTrue(Types.getType(ObjT::class, arrayOf(IntType)) != null)
				shouldBeFalse(Types.getType(ObjT::class, arrayOf(LongType)) != null)
			}
		}
		
	}
}
}

enum class CacheEnum {A, B, C }

class DateType: LongBasedType<Date>(Date::class) {
	override fun newInstance(): Date  = Date()
	override fun fromValue(v: Long): Date? = Date().apply { time = v }
	override fun toValue(v: Date?): Long? = v?.time
}

class ObjT<T>(val v: T)

class ObjType(val type: Type<*>): StringBasedType<ObjT<*>>(ObjT::class) {
	override fun newInstance(): ObjT<*> = ObjT(type.newInstance())
	override fun fromValue(v: String): ObjT<*>? = ObjT(type.newInstance())
	override fun toValue(v: ObjT<*>?): String? = v?.v.toString()
}

@Suppress("UNCHECKED_CAST")
class AListType<E: Any>(elementType: Type<E>): CollectionType<ArrayList<*>, E>(ArrayList::class, elementType) {
	override fun newInstance(): ArrayList<E> = ArrayList()
	override fun addElement(e: E?, index: Int, seq: ArrayList<*>): ArrayList<E> = (seq as ArrayList<E>).also { (it as ArrayList<E?>) += e }
}
