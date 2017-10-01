package just4fun.holomorph.mains

import just4fun.holomorph.Produce
import just4fun.holomorph.Reflexion
import just4fun.holomorph.forms.ArrayFactory
import just4fun.holomorph.Opt
import just4fun.holomorph.types.SchemaType
import just4fun.holomorph.forms.DefaultFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual
import org.jetbrains.spek.api.shouldThrow



class Production: Spek() { init {
	given("") {
		
		on("Option: nameless") {
			class InObj(val x: Int, val y: Int)
			class Obj(@Opt(0) val a: Int, @Opt(1) val b: Int, @Opt(2) val c: InObj)
			
			val schema = SchemaType(Obj::class)
			val obj = Obj(1, 2, InObj(3, 4))
			val res1 = schema.instanceTo(obj, DefaultFactory)
			val res2 = schema.instanceTo(obj, DefaultFactory, null, null)
			val res3 = schema.instanceTo(obj, DefaultFactory, null, true)
			val res4 = schema.instanceTo(obj, DefaultFactory, null, false)
			println("${res1}")
			println("${res2}")
			println("${res3}")
			println("${res4}")
			
			it("") {
				shouldEqual(res1, """[1,2,{"x":3,"y":4}]""")
				shouldEqual(res2, """[1,2,{"x":3,"y":4}]""")
				shouldEqual(res3, """[1,2,[3,4]]""")
				shouldEqual(res4, """{"a":1,"b":2,"c":{"x":3,"y":4}}""")
			}
		}
		
		on("No properties to ordinal") {
			class Obj
			it("") {
				shouldThrow(Exception::class.java) {
					val sch = SchemaType(Obj::class)
				}
			}
		}
		on("No properties to ordinal") {
			class Obj {
				val a: Array<Any?>? = null
			}
			
			val schema = SchemaType(Obj::class)
			val obj = schema.instanceFrom("{a:[1, 2, true]}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a!!.size, 3)
			}
		}
		on("From one obj to another") {
			class Obj1(val a: Int, val b: String, x: Int)
			class Obj2(val a: Int, val b: String, y: Boolean)
			
			val sch1 = Reflexion(Obj1::class)
			val sch2 = Reflexion(Obj2::class)
			val obj1 = Obj1(1, "ok", 2)
			val obj2 = Produce(sch1.schema.produceFactory(obj1), sch2.schema.produceFactory()).valueOrThrow
			it("") {
				shouldEqual(obj1.a, obj2.a)
				shouldEqual(obj1.a, 1)
				shouldEqual(obj1.b, obj2.b)
				shouldEqual(obj1.b, "ok")
			}
		}
		on("Update instance") {
			class Obj(val a: Int, val b: String, val c: Boolean)
			val obj = Obj(1, "ok", true)
			val sch = SchemaType(Obj::class)
			sch.instanceUpdateFrom(obj, "{b: Oops}", DefaultFactory)
			it("") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, "Oops")
				shouldEqual(obj.c, true)
			}
		}
		on("Object to array") {
			class Obj(val a: Int, val b: String, val c: Boolean)
			val obj = Obj(1, "ok", true)
			val sch = SchemaType(Obj::class)
			val array = sch.instanceTo(obj, ArrayFactory).valueOrThrow
			it("") {
				shouldEqual(array[0], 1)
				shouldEqual(array[1], "ok")
				shouldEqual(array[2], true)
			}
		}
		
		
		//		on("") {
		//			it("") {
		//
		//			}
		//		}
		
	}
	
}
}