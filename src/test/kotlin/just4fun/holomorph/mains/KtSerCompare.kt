//package just4fun.holomorph.test.mains
//
//import just4fun.holomorph.DefineSchema
//import just4fun.holomorph.Reflexion
//import just4fun.holomorph.DefaultFactory
//import just4fun.holomorph.getMeasuredTimeAvg
//import just4fun.holomorph.measureTime
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.json.JSON
//import kotlinx.serialization.serializer
//
//fun main(args: Array<String>) {
//	//
////	obj2str();obj2str();obj2str();obj2str();obj2str();obj2str();obj2str();obj2str();obj2str();obj2str();//35000
////	obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();obj2strKt();//42000
//	str2obj();str2obj();str2obj();str2obj();str2obj();str2obj();str2obj();str2obj();str2obj();str2obj();//40000
////	str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();str2objKt();//55000
//	println("measuredTimeAvg= ${measuredTimeAvg} ns")
//}
//
//val TIMES = 1
//val ref = Reflexion(Data::class)
//val sezer = Data::class.serializer()
//val str = """{"a0":"ok","a1":"ok","a2":"ok","a3":"ok","a4":"ok","a5":"ok","a6":"ok","a7":"ok","a8":"ok","a9":"ok"}"""
//
//fun obj2strKt() = measureTime("o>s  Kt", TIMES) {
//	val v = JSON.stringify(sezer, Data())
//}
//
//fun str2objKt() = measureTime("s>o  Kt", TIMES) {
//	val v = JSON.parse<Data>(sezer, str)
//}
//
//fun obj2str() = measureTime("o>s  HM", TIMES, antiSurgeRate = 1.3) {
//	val v = ref.instanceTo(Data(), DefaultFactory)
//}
//
//fun str2obj() = measureTime("s>o  HM", TIMES, antiSurgeRate = 1.3) {
//	val v = ref.instanceFrom(str, DefaultFactory)
//}
//
////@Serializable  @DefineSchema(useAccessors = false)
////class Data {
////	var a0: String = ""
////	var a1: String = ""
////	var a2: String = ""
////	var a3: String = ""
////	var a4: String = ""
////	var a5: String = ""
////	var a6: String = ""
////	var a7: String = ""
////	var a8: String = ""
////	var a9: String = ""
////}
//@Serializable @DefineSchema(useAccessors = false)
//class Data(var a0: String = "", var a1: String = "", var a2: String = "", var a3: String = "", var a4: String = "", var a5: String = "", var a6: String = "", var a7: String = "", var a8: String = "", var a9: String = "")
//
