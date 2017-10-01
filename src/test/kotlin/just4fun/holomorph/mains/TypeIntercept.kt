package just4fun.holomorph.mains

import just4fun.holomorph.EnclosedEntries
import just4fun.holomorph.Intercept
import just4fun.holomorph.Reflexion
import just4fun.holomorph.ValueInterceptor
import just4fun.holomorph.forms.DefaultFactory
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual



class TypeIntercept: Spek() { init {
	given("") {
		
		on("") {
			class Interceptor: ValueInterceptor<Int> {
				override fun intercept(value: String): Int? = 5
			}
			
			class Obj {
				@Intercept(Interceptor::class) val a: Int = -1
				val b: Int = -1
			}
			
			val rx = Reflexion(Obj::class)
			val obj = rx.instanceFrom("{a:ok, b:ok}", DefaultFactory).valueOrThrow
			val text = rx.instanceTo(obj, DefaultFactory)
			it("Check results") {
				shouldEqual(obj.a, 5)
				shouldEqual(obj.b, 0)
				shouldEqual(text, """{"a":5,"b":0}""")
			}
		}
		on("") {
			class Wrapper(val n: Int)
			
			class Interceptor: ValueInterceptor<Int> {
				val wrx = Reflexion(Wrapper::class)
				override fun intercept(subEntries: EnclosedEntries, expectNames: Boolean): Int? = wrx.schema.fromEntries(subEntries, expectNames)!!.n
			}
			
			class Obj {
				@Intercept(Interceptor::class) val a: Int = -1
				val b: Int = -1
			}
			
			val rx = Reflexion(Obj::class)
			val obj = rx.instanceFrom("{a:{n:5}, b:ok}", DefaultFactory).valueOrThrow
			it("Check results") {
				shouldEqual(obj.a, 5)
				shouldEqual(obj.b, 0)
			}
		}
		
	}
}
}
