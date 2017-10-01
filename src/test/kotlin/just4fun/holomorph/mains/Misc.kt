package just4fun.holomorph.mains

import just4fun.kotlinkit.async.AsyncTask

fun main(a: Array<String>) {
	AsyncTask{
		val v: Any? = null
		println("${ v as? String}")
		println("${ v as String?}")
	}.onComplete { AsyncTask.sharedContext.shutdown() }
	
}


/*
// TEST ACCESSORS PERFORMANCE
fun main(a: Array<String>) {
//	getA;getA;getA;getA;getA;getA;getA;getA;getA;getA;//4000
//	getF;getF;getF;getF;getF;getF;getF;getF;getF;getF;//1400
//	get;get;get;get;get;get;get;get;get;get;//450
//	setA;setA;setA;setA;setA;setA;setA;setA;setA;setA;//4300
//	setF;setF;setF;setF;setF;setF;setF;setF;setF;setF;//1400
	set;set;set;set;set;set;set;set;set;set;//500
	println("measuredTimeAvg= ${measuredTimeAvg} ns")
	println("${obj.x}")
	
}

val pty = X::class.memberProperties.find { it.name == "x" }!!
val getter = pty.getter.apply { isAccessible = true } as KCallable<Int>
val setter = (pty as KMutableProperty<*>).setter.apply { isAccessible = true } as KCallable<*>
val field = pty.javaField?.apply { isAccessible = true }!!
val obj:X = X(42)

val getA get() = measureTime("GETTER", 1, antiSurgeRate = 1.3) { getter.call(obj) }
val getF get() = measureTime("GET FIELD", 1, antiSurgeRate = 1.3) { field.get(obj) }
val setA get() = measureTime("SETTER", 1, antiSurgeRate = 1.3) { setter.call(obj, 44) }
val setF get() = measureTime("FIELD SET", 1, antiSurgeRate = 1.3) { field.set(obj, 44) }
val get get() = measureTime("GET", 1) { obj.x }
val set get() = measureTime("SET", 1) { run { obj.x = 44 } }

class X(var x: Int)
*/
