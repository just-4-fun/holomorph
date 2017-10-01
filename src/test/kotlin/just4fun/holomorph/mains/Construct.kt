package just4fun.holomorph.mains

import just4fun.holomorph.DefineSchema
import just4fun.holomorph.Opt
import just4fun.holomorph.Reflexion
import just4fun.holomorph.Types
import just4fun.kotlinkit.Safely
import just4fun.holomorph.types.SchemaType
import just4fun.holomorph.forms.DefaultFactory
import just4fun.holomorph.mains.javas.Point
import just4fun.holomorph.mains.javas.Point3d
import just4fun.holomorph.mains.javas.Point4d
import org.jetbrains.spek.api.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


class TestProduction: Spek() { init {
	fun <T: Any> from(klas: KClass<T>, str: String): T = SchemaType(klas).instanceFrom(str, DefaultFactory).valueOrThrow
	
	given("Test object construction via schema") {
		
		on("delegqate property") {
			class Obj {
				val a: Int = 0
				val bla: Int by lazy { 10 }
			}
			
			val text = Reflexion(Obj::class).instanceTo(Obj(), DefaultFactory).valueOrThrow
			it("Check results") {
				shouldThrow(Exception::class.java) {
					val obj = from(Obj::class, "{a:1,bla:2}")
				}
				shouldEqual(text, """{"a":0,"bla":10}""")
			}
		}
		on("Constructor: no; Body: val a, val b") {
			class Obj {
				val a: Int = 0
				val b: Int = 0
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: no; Body: val a, val b") {
			class Obj {
				internal val a: Int = 0
				val b: Int = 0
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 0)// as 'a' is not public
				shouldEqual(obj.b, 2)
			}
		}
		on("Pick all props") {
			@DefineSchema class Obj {
				internal val a: Int = 0
				val b: Int = 0
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val a, val b; Body: no") {
			class Obj(val b: Int, val a: Int)
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val a, val b; Body: no; a=null") {
			class Obj(val b: Int, val a: Int)
			
			val obj = from(Obj::class, "{a:null,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 0)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val a, val b; Body: no; a=null") {
			class Obj(val b: Int, val a: Int?)
			
			val obj = from(Obj::class, "{a:null,b:2}")
			it("Check results") {
				shouldEqual(obj.a, null)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b, par a; Body: val a") {
			class Obj(val b: Int, a: Int) {
				val a: Int = 0
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 0)// param and val aren't related
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b; Body: val a") {
			class Obj(val b: Int) {
				val a: Int = 0
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b, par a; Body: val a") {
			class Obj(val b: Int, a: Int) {
				val a: Int = a + 1
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b, par c=3; Body: val a") {
			class Obj(val b: Int, c: Int) {
				val a: Int = c
			}
			
			val obj = from(Obj::class, "{a:1,b:2,c:3}")
			it("Check results") {
				shouldEqual(obj.a, 1)// because after constr. prop a is reassigned
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b, par c; Body: val a") {
			class Obj(val b: Int, c: Int) {
				val a: Int = c
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)// because after constr. prop a is reassigned
				shouldEqual(obj.b, 2)
			}
		}
		on("Constructor: val b, par c; Body: val a") {
			class Obj(val b: Int = 2, c: Int) {
				val a: Int = c
			}
			
			val obj = from(Obj::class, "{a:1}")
			it("Check results") {
				shouldEqual(obj.a, 1)// because after constr. prop a is reassigned
				shouldEqual(obj.b, 0)
			}
		}
		on("Constructor: val b, par c; Body: val a") {
			class Obj(b: Int, a: Int) {
				val a: Int = a + 2
				val b: Int = b + 2
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 3)
				shouldEqual(obj.b, 4)
			}
		}
		on("Constructor empty: val b, par c; Body: val a") {
			class Obj(val b: Int = 2, c: Int = 3) {
				val a: Int = c
			}
			
			val obj = from(Obj::class, "{a:1}")
			it("Check results") {
				shouldEqual(obj.a, 1)// because after constr. prop a is reassigned
				shouldEqual(obj.b, 0)
				shouldNotBeNull(Safely { Obj::class.createInstance() })// uses empty constructor
			}
		}
		on("Ordered. Constructor: no; Body: val a, val b;") {
			class Obj {
				@Opt(1) internal val a: Int = 0
				@Opt(0) internal val b: Int = 0
			}
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: par b; par a; Body: val a, val b;") {
			class Obj(b: Int, a: Int) {
				@Opt(1) val a: Int = a
				@Opt(0) val b: Int = b
			}
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: no; Body: val a, val b;") {
			class Obj(a: Int, b: Int) {
				@Opt(1) val a: Int = a
				@Opt(0) val b: Int = b
			}
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: val a, val b; Body: no;") {
			class Obj(@Opt(1) val a: Int, @Opt(0) val b: Int)
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: val a, val b; Body: no;") {
			class Obj(@Opt(0) val b: Int, @Opt(1) val a: Int)
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: no; Body: val a, val b;") {
			class Obj(a: Int, @Opt(0) val b: Int) {
				@Opt(1) val a: Int = a + 1
			}
			
			val obj = from(Obj::class, "[1,2]")
			it("Check results") {
				shouldEqual(obj.a, 3)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: val a, val b; Values no;") {
			class Obj(@Opt(0) val b: Int, @Opt(1) val a: Int)
			
			val obj = from(Obj::class, "[]")
			it("Check results") {
				shouldEqual(obj.a, 0)
				shouldEqual(obj.b, 0)
			}
		}
		on("Ordered. Constructor: val a, val b; Values less;") {
			class Obj(@Opt(0) val b: Int, @Opt(1) val a: Int)
			
			val obj = from(Obj::class, "[1]")
			it("Check results") {
				shouldEqual(obj.a, 0)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: val a, val b; Values more;") {
			class Obj(@Opt(0) val b: Int, @Opt(1) val a: Int)
			
			val obj = from(Obj::class, "[1,2,3]")
			it("Check results") {
				shouldEqual(obj.a, 2)
				shouldEqual(obj.b, 1)
			}
		}
		on("Ordered. Constructor: val a, val b; Hole;") {
			class Obj(@Opt(0) val b: Int, @Opt(2) val a: Int)
			
			val obj = from(Obj::class, "[1,2,3]")
			it("Check results") {
				shouldEqual(obj.a, 3)
				shouldEqual(obj.b, 1)
			}
		}
		on("Selecting props: private is skipped") {
			class Obj {
				private val a: Int = 0
				val b: Int = 0
				fun getA() = a
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.getA(), 0)
				shouldEqual(obj.b, 2)
				shouldBeNull(Reflexion(Obj::class).schema.propertiesMap["a"])
			}
		}
		on("Selecting props: private as constr param") {
			class Obj(private val a: Int, val b: Int) {
				fun getA() = a
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.getA(), 1)
				shouldEqual(obj.b, 2)
				shouldBeNull(Reflexion(Obj::class).schema.propertiesMap["a"])
			}
		}
		on("Selecting props: non-annotated is skipped") {
			class Obj {
				@Opt private val a: Int = 0
				val b: Int = 0
				fun getA() = a
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.getA(), 1)
				shouldEqual(obj.b, 0)
				shouldBeNull(Reflexion(Obj::class).schema.propertiesMap["b"])
			}
		}
		on("Selecting props: non-annotated as constr param") {
			class Obj(@Opt private val a: Int, val b: Int) {
				fun getA() = a
			}
			
			val obj = from(Obj::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.getA(), 1)
				shouldEqual(obj.b, 2)
				shouldBeNull(Reflexion(Obj::class).schema.propertiesMap["b"])
			}
		}
		on("Java constructor: no prop specified, no public fields found") {
			it("Check results") {
				shouldThrow(Exception::class.java) {
					SchemaType(Point::class)
				}
			}
		}
		on("Java constructor: wrong prop specified") {
			it("Check results") {
				shouldThrow(Exception::class.java) {
					SchemaType(Point::class, propsNames = arrayOf("x", "a"))
				}
			}
		}
		on("Inner class") {
			it("Check results") {
				shouldThrow(Exception::class.java) {
					class Outer {
						inner class Inner(val a: Int)
					}
					SchemaType(Outer.Inner::class)
				}
			}
		}
		on("Type not found") {
			it("Check results") {
				class Obj(@Opt val b: Runnable)
				shouldThrow(Exception::class.java) {
					SchemaType(Obj::class)
				}
			}
		}
		on("Java constructor: has specified properties") {
			val schema = SchemaType(Point::class, propsNames = arrayOf("x", "y")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("{x:1,y:2}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 1)
				shouldEqual(obj.y, 2)
			}
		}
		on("Java constructor: input has no relevant data") {
			val schema = SchemaType(Point::class, propsNames = arrayOf("x", "y")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("{a:1,b:2}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 10)
				shouldEqual(obj.y, 10)
			}
		}
		on("Java constructor: nameless input") {
			val schema = SchemaType(Point::class, propsNames = arrayOf("x", "y")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("[1,2]", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 1)
				shouldEqual(obj.y, 2)
			}
		}
		on("Java constructor: just for fun") {
			val schema = SchemaType(Point::class, propsNames = arrayOf("x", "y")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("{arg0:1,arg1:2}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 11)
				shouldEqual(obj.y, 12)
			}
		}
		on("Java constructor: prefers properties") {
			val schema = SchemaType(Point::class, propsNames = arrayOf("x", "y")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("{x:1,y:2,arg0:1,arg1:2}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 1)
				shouldEqual(obj.y, 2)
			}
		}
		on("Java constructor: properties are more than params") {
			val schema = SchemaType(Point3d::class, propsNames = arrayOf("x", "y", "z")).apply { Types.clearCache(true) }
			val obj = schema.instanceFrom("{x:1,y:2,z:3}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.x, 1)
				shouldEqual(obj.y, 2)
				shouldEqual(obj.z, 3)
			}
		}
		on("Config schema in constructor;") {
			Types.clearCache(true)
			class Obj(@DefineSchema(arrayOf("x", "y")) val point: Point, val a: Int, val b: Int) {
				val c = point.x + point.y
			}
			
			val obj = from(Obj::class, "{point:{x:1,y:2}, a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
				shouldEqual(obj.c, 3)
			}
		}
		on("Config schema in prop;") {
			Types.clearCache(true)
			class Obj(val a: Int, val b: Int) {
				@DefineSchema(arrayOf("x", "y", "z"))
				val c: Point3d? = null
			}
			
			val obj = from(Obj::class, "{c:{x:1,y:2,z:3}, a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
				shouldEqual(obj.c!!.x, 1)
				shouldEqual(obj.c.y, 2)
				shouldEqual(obj.c.z, 3)
			}
		}
		on("Constructor args") {
			Types.clearCache(true)
			class Obj(@DefineSchema(arrayOf("x", "y", "z", "w")) var point: Point4d, val a: Int, val b: Int) {
				val c = point.x + point.y
			}
			
			val obj = from(Obj::class, "{point:{x:1,y:2}, a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
				shouldEqual(obj.c, 3)
			}
		}
		on("Constructor args") {
			Types.clearCache(true)
			class Obj(@DefineSchema(arrayOf("x", "y", "z", "w")) val point: Point4d, val a: Int, val b: Int) {
				val c = point.x + point.y
			}
			
			val obj = from(Obj::class, "{point:{arg0:1,arg1:2}, a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
				shouldEqual(obj.c, 23)
			}
		}
		
		on("Constructor selection") {
			val obj = from(Cons0::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 0)
				shouldEqual(obj.b, 0)
				shouldEqual(obj.c, 1)
			}
		}
		on("Constructor selection") {
			val obj = from(Cons1::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
				shouldEqual(obj.c, 0)
			}
		}
		on("Constructor selection") {
			val obj = from(Cons2::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
				shouldEqual(obj.c, 0)
			}
		}
		on("Constructor selection") {
			val obj = from(Cons3::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
				shouldEqual(obj.c, 1)
			}
		}
		on("Constructor selection") {
			val obj = from(Cons4::class, "{a:1,b:2}")
			it("Check results") {
				shouldEqual(obj.a, 1)
				shouldEqual(obj.b, 2)
				shouldEqual(obj.c, 1)
			}
		}
		on("Generic class") {
			class Obj<T>(val a: Int, val b: Int)
			
			val obj = from(Obj::class, "{a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
			}
		}
		on("Generic class") {
			open class Base<T: Any> {
				val a: T? = null
				lateinit var b: T
			}
			
			class Obj: Base<Int>()
			
			val obj = from(Obj::class, "{a:4,b:5}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
			}
		}
		on("Generic class") {
			class Obj<T>(val a: T, val b: T)
			it("Check results") {
				shouldThrow(Exception::class.java) {
					val obj = from(Obj::class, "{a:4,b:5}")
				}
			}
		}
		on("Annotated constr") {
			class Obj(val a: Int, val b: Int) {
				@Opt constructor(x: Int, y: Int, z: Int): this(x + z, y + z)
			}
			
			val obj = from(Obj::class, "{x:1, y:2, z:3}")
			it("Check results") {
				shouldEqual(obj.a, 4)
				shouldEqual(obj.b, 5)
			}
		}
		on("Change of a property place doesn't matter") {
			class Obj1 {
				val a = 0
				val b = 0
			}
			
			class Obj2 {
				val b = 0
				val a = 0
			}
			
			val obj1 = Reflexion(Obj1::class).instanceFrom("[1,2]", DefaultFactory).valueOrThrow
			val obj2 = Reflexion(Obj2::class).instanceFrom("[1,2]", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj1.a, 1)
				shouldEqual(obj2.a, 1)
				shouldEqual(obj1.b, 2)
				shouldEqual(obj2.b, 2)
			}
		}
		on("Recursive types") {
			class Obj(val a: Int, var next: Obj? = null)
			
			val ref = Reflexion(Obj::class)
			val obj = ref.instanceFrom("{a:10}", DefaultFactory).valueOrThrow
			obj.next = Obj(11)
			val text = ref.instanceTo(obj, DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.a, 10)
				shouldEqual(text, """{"a":10,"next":{"a":11,"next":null}}""")
			}
		}
		
	}
}
}



class Cons0() {
	val a: Int = 0
	val b: Int = 0
	var c = 0
	
	constructor(a: Int, b: Int): this() {
		c = 1
	}
}

class Cons1(val a: Int, val b: Int) {
	var c = 0
	
	constructor(): this(3, 4) {
		c = 1
	}
}

class Cons2(a: Int, b: Int) {
	val a: Int = a
	val b: Int = b
	var c = 0
	
	constructor(): this(3, 4) {
		c = 1
	}
}

class Cons3(a: Int, b: Int, x: Int) {
	val a: Int = a
	val b: Int = b
	var c = 0
	
	constructor(a: Int, b: Int): this(a, b, 5) {
		c = 1
	}
	
	constructor(): this(3, 4) {
		c = 2
	}
}

class Cons4(x: Int, y: Int) {
	val a: Int = 0
	val b: Int = 0
	var c = 0
	
	constructor(x: Int): this(3, 4) {
		c = 1
	}
}


