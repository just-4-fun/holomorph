package just4fun.holomorph.mains

import just4fun.holomorph.types.SchemaType
import just4fun.holomorph.forms.DefaultFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual



class InheritanceTest: Spek() { init {
	given("") {
		
		
		on("") {
			open class ObjBase(val id: Long)
			
			class ObjExt(id:Long, val a: Int, val b: Int): ObjBase(id)
			
			val schema = SchemaType(ObjExt::class)
			val obj = ObjExt(1, 2, 3)
			val result = schema.instanceTo(obj, DefaultFactory).valueOrThrow
			val out = schema.instanceFrom(result, DefaultFactory).valueOrThrow
			println("$result")
			it("") {
				shouldEqual(result, """{"a":2,"b":3,"id":1}""")
			}
			it("") {
				shouldEqual(out.id, 1)
				shouldEqual(out.a, 2)
				shouldEqual(out.b, 3)
			}
		}
		
	}
}
}