package just4fun.holomorph.mains

import just4fun.holomorph.Reflexion
import just4fun.holomorph.Types
import just4fun.holomorph.forms.DefaultFactory
import just4fun.holomorph.measureTime



fun main(a: Array<String>) {
	val n = 1
//	measureConstr(n)//350.000 ns
	measureNoConstr(n)// 150.000 ns
}

fun measureCreation() {
	
	class NObj {
		val prop0: Int = 0
		val prop1: Int = 0
		val prop2: Int = 0
		val prop3: Int = 0
		val prop4: Int = 0
		val prop5: Int = 0
		val prop6: Int = 0
		val prop7: Int = 0
		val prop8: Int = 0
		val prop9: Int = 0
	}
}

fun measureConstr(n: Int) {
	class Obj(val prop0: Int = 0, val prop1: Int = 0, val prop2: Int = 0, val prop3: Int = 0, val prop4: Int = 0, val prop5: Int = 0, val prop6: Int = 0, val prop7: Int = 0, val prop8: Int = 0, val prop9: Int = 0)
	
	//500.000 - 600.000 ns
	fun define() = measureTime("DFN CONSTR", n) {
		Reflexion(Obj::class).apply { Types.clearCache(true) }
	}
	
	val schema = define()//
	val input = "{prop0:10, prop1:1, prop2:2, prop3:3, prop4:4, prop5:5, prop6:6, prop7:7, prop8:8, prop9:9}"
	// 80.000 - 120.000 ns
	fun construct() = measureTime("CONSTR", n) { schema.instanceFrom(input, DefaultFactory) }
	
	construct();
}

fun measureNoConstr(n: Int) {
	class Obj {
		val prop0: Int = 0
		val prop1: Int = 0
		val prop2: Int = 0
		val prop3: Int = 0
		val prop4: Int = 0
		val prop5: Int = 0
		val prop6: Int = 0
		val prop7: Int = 0
		val prop8: Int = 0
		val prop9: Int = 0
	}
	
	//150.000 - 180.000 ns
	fun define() = measureTime("DFN NON-CONSTR", n) {
		Reflexion(Obj::class).apply { Types.clearCache(true) }
	}
	
	val schema = define()//
	val input = "{prop0:10, prop1:1, prop2:2, prop3:3, prop4:4, prop5:5, prop6:6, prop7:7, prop8:8, prop9:9}"
	//85.000 - 107.000 ns
	fun construct() = measureTime("NON-CONSTR", n) { schema.instanceFrom(input, DefaultFactory) }
	
	construct();
}



