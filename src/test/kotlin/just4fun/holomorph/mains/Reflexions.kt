package just4fun.holomorph.mains

import just4fun.holomorph.*
import just4fun.holomorph.forms.MapConsumer
import just4fun.holomorph.forms.MapProvider
import just4fun.holomorph.types.AnyType
import just4fun.holomorph.types.MapType
import just4fun.holomorph.types.StringType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.shouldBeFalse
import org.jetbrains.spek.api.shouldBeTrue
import org.jetbrains.spek.api.shouldEqual
import kotlin.reflect.KClass



class ProjectionTest: Spek() { init {
	given("Projection") {
		class Pty<T: Any>(val base: Property<T>): Property<T> by base {
			override val name = base.name.toUpperCase()
			val objectKlas: KClass<*> get() = base.schema.typeKlas
		}
		class Obj(val a: String, val b: Int, val c: Int)
		
		class ObjProjection: Reflexion<Obj>(Obj::class) {
			val a by property<String>()
			val b by property<Int>()
			override fun <T: Any> newProperty(base: Property<T>): Property<T> = Pty(base)
		}
		
		fun Property<Int>.objectKlas():KClass<*> = (this as Pty).objectKlas
		fun Property<Int>.assign(obj: Obj, v: Int) = set(obj, v)
		fun Property<String>.assign(obj: Obj, v: String) = set(obj, v)
		
		on("Producing string from object") {
			val mType = MapType(StringType, AnyType)
			class MapFactory: ProduceFactory<MutableMap<*, *>, MutableMap<*, *>> {
				override fun invoke(input: MutableMap<*, *>): EntryProvider<MutableMap<*, *>> = MapProvider(input, mType)
				override fun invoke(): EntryConsumer<MutableMap<*, *>> = MapConsumer(mType)
			}
			
			val factory = MapFactory()
			val projection = ObjProjection()
			val obj = Obj("oops", 2, 3)
			projection.a.assign(obj, "ok")
			projection.b.assign(obj, 1)
			val projMap = projection.instanceTo(obj, factory, projection.properties).valueOrThrow
			val schMap = projection.instanceTo(obj, factory).valueOrThrow
			println("$projMap")
			println("$schMap")
			it("Projection properties are actual") {
				shouldEqual(projection.a.name, "A")
				shouldEqual(projection.b.name, "B")
//				shouldEqual(projection.a.objectKlas(), Obj::class)// expected error: "Required Property<Int>"
				shouldEqual(projection.b.objectKlas(), Obj::class)
			}
			it("Results for schema") {
				shouldBeTrue(schMap.contains("a"))
				shouldBeTrue(schMap.contains("b"))
				shouldBeTrue(schMap.contains("c"))
			}
			it("Results for projection") {
				shouldBeTrue(projMap.contains("A"))
				shouldBeTrue(projMap.contains("B"))
				shouldBeFalse(projMap.contains("c"))
				shouldBeFalse(projMap.contains("C"))
			}
		}
		
	}
}
}