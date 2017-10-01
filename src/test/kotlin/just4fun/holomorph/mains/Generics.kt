package just4fun.holomorph.mains

import just4fun.holomorph.Reflexion
import just4fun.holomorph.forms.DefaultFactory
import just4fun.holomorph.types.IntType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldEqual



class Generics: Spek() { init {
	given("Generic type  parameters") {
		on("Generic type as property") {
			class Obj(val pair: Pair<Int, String>, val triple: Triple<Boolean, Int, String>)
			
			val sch = Reflexion(Obj::class)
			val obj = sch.instanceFrom("{pair:{first:1,second:ok},triple:{first:1,second:2,third:oops}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.pair.second, "ok")
				shouldEqual(obj.triple.third, "oops")
			}
		}
		on("Generic type as property type") {
			class ObjA<A>(val array: Array<A>)
			class Obj(val a: ObjA<Int>)
			
			val sch = Reflexion(Obj::class)
			val obj = sch.instanceFrom("{a:{array:[1, 2, 3]}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a.array.size, 3)
				shouldEqual(obj.a.array[2], 3)
			}
		}
		on("Generic type initial") {
			class Obj<T>(val a:T)
			
			val sch = Reflexion(Obj::class, IntType)
			val obj = sch.instanceFrom("{a:4}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a, 4)
			}
		}
		on("Generic type initial as prop type") {
			class ObjA<A>(val array: Array<A>)
			class Obj<T>(val a:ObjA<T>)
			
			val sch = Reflexion(Obj::class, IntType)
			val obj = sch.instanceFrom("{a:{array:[1, 2, 3]}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a.array.size, 3)
				shouldEqual(obj.a.array[2], 3)
			}
		}
		on("Generic complex type as property type") {
			class ObjA<A, B>(val list: List<Pair<A, B>>)
			class Obj(val a: ObjA<Int, String>)
			
			val sch = Reflexion(Obj::class)
			val obj = sch.instanceFrom("{a:{list:[{first:1,second:ok}, {first:2,second:oops}]}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a.list.size, 2)
				shouldEqual(obj.a.list[1].second, "oops")
			}
		}
		on("Generic bound type as property type") {
			class ObjA<A, B, out L: List<Pair<A, B>>>(val list: L)
			class Obj(val a: ObjA<Int, String, List<Pair<Int, String>>>)
			
			val sch = Reflexion(Obj::class)
			val obj = sch.instanceFrom("{a:{list:[{first:1,second:ok}, {first:2,second:oops}]}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.a.list.size, 2)
				shouldEqual(obj.a.list[1].second, "oops")
			}
		}
		on("Generic bound type as property type") {
			class ObjA<A>(val a: A)
			class ObjB<B>(val objA: ObjA<B>)
			class Obj(val objB: ObjB<Int>)
			
			val sch = Reflexion(Obj::class)
			val obj = sch.instanceFrom("{objB:{objA:{a:1}}}", DefaultFactory).valueOrThrow
			it("") {
				shouldEqual(obj.objB.objA.a, 1)
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