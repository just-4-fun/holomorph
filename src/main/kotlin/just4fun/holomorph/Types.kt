package just4fun.holomorph

import just4fun.holomorph.types.*
import just4fun.kotlinkit.Safely
import just4fun.holomorph.types.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal typealias CollType = CollectionType<*, *>


/** Manages type resolvers ([Type]). Serves as the resolvers cache and registry. */
object Types: TypesCache()


open class TypesCache {
	private val cache = mutableMapOf<Int, Type<*>>()
	private val builders = mutableMapOf<Int, (Array<out Type<*>>) -> Type<*>>()
	private val EA = emptyArray<Type<*>>()
	
	init {
		defineType(MutableList::class) { et -> ListType(et[0]) }// MutableList::class == List::class
		defineType(MutableSet::class) { et -> SetType(et[0]) }// MutableList::class == List::class
		defineType(MutableMap::class) { et -> MapType(et[0], et[1]) }// MutableList::class == List::class
	}
	
	
	/** Registers the custom type resolver returned by the [builder]. [kClass] is the class of resolver's type. Optional [argType]s are resolvers of type parameters of the [kClass] if it's generic. The [kClass] and [argType]s are identification keys. The type resolver with corresponding keys will be created on demand. */
	fun defineType(kClass: KClass<*>, vararg argType: Type<*>, builder: (Array<out Type<*>>) -> Type<*>) {
		builders[hash(kClass, argType)] = builder
	}
	
	/** Adds the new type resolver returned by the [builder] to the cache by keys [kClass] and [argType]s. [kClass] is the class of resolver's type. Optional [argType]s are resolvers of type parameters of the [kClass] if it's generic. */
	fun <T: Type<*>> addType(kClass: KClass<*>, vararg argType: Type<*>, builder: () -> T): T {
		@Suppress("UNCHECKED_CAST") return setType(kClass, argType, builder) as T
	}
	
	/** Returns the type resolver from the cache by keys [kClass] and [argType]s. [kClass] is the class of resolver's type. Optional [argType]s are resolvers of type parameters of the [kClass] if it's generic.  If the resolver wasn't present in the cache at this moment, it's created by [builder] and cached.  */
	fun <T: Type<*>> getOrAddType(kClass: KClass<*>, vararg argType: Type<*>, builder: () -> T): T {
		val type: Type<*>? = getType(kClass, argType) ?: buildType(kClass, argType) ?: setType(kClass, argType, builder)
		@Suppress("UNCHECKED_CAST") return type as T
	}
	
	/** Removes the type resolver [type].  */
	fun removeType(type: Type<*>) {
		cache.entries.find { it.value == type }?.let { cache.remove(it.key) }
	}
	
	/** Clears the cache. It's better to be done after all [Reflexion]s are initialized. [clearBuilders] also clears registered resolvers. */
	fun clearCache(clearBuilders: Boolean) {
		cache.clear()
		if (clearBuilders) builders.clear()
	}
	
	internal inline fun setType(klas: KClass<*>, argTypes: Array<out Type<*>> = EA, builder: () -> Type<*>): Type<*>? {
		return Safely { builder().also { cache[hash(klas, argTypes)] = it } }
	}
	
	internal fun getType(klas: KClass<*>, argTypes: Array<out Type<*>> = EA): Type<*>? {
		return cache[hash(klas, argTypes)]
	}
	
	private fun buildType(klas: KClass<*>, argTypes: Array<out Type<*>> = EA): Type<*>? {
		val builder = builders[hash(klas, argTypes)] ?: if (argTypes.isNotEmpty()) builders[hash(klas, EA)] else null
		return builder?.let { Safely { it.invoke(argTypes) } }?.also { cache[hash(klas, argTypes)] = it }
	}
	
	private fun hash(klas: KClass<*>, argTypes: Array<out Type<*>>) = klas.hashCode() + if (argTypes.isEmpty()) 0 else argTypes.sumBy { it.hashCode() * 31 }
	
	/* resolve */
	internal fun resolve(kType: KType, klas: KClass<*>, typeArgs: Map<String, Type<*>>? = null, typeKlas: KClass<*>? = null, propsNames: Array<String>? = null, ordered: Boolean = false, useAccessors:Boolean = true): Type<*>? {
		val clas = klas.java
		val klasName = klas.simpleName!!
		return when {
			clas.isPrimitive -> primitive(klasName)
			klas == StringType.typeKlas -> StringType
			clas.isArray -> getType(klas) ?: discoverArray(kType, klas, klasName, typeArgs)
			clas.isEnum -> getType(klas) ?: discoverEnum(klas, clas)
			else -> kType.arguments.let { tArgs ->
				if (tArgs.isEmpty()) when {
					klas == AnyType.typeKlas -> AnyType
					klas == UnitType.typeKlas -> UnitType
					clas.name.startsWith("java.lang.") -> primJava(clas.simpleName)
					else -> getType(klas) ?:
					  if (typeKlas == null) buildType(klas) ?: setType(klas) { SchemaType(klas, null, propsNames, ordered, useAccessors) }
					  else constructType(klas, typeKlas)
				} else when {
					klas.isSubclassOf(Collection::class) -> discoverColl(klas, tArgs, typeKlas, typeArgs)
					klas.isSubclassOf(Map::class) -> discoverMap(klas, tArgs, typeKlas, typeArgs)
					else -> discoverGeneric(klas, tArgs, typeArgs, typeKlas, propsNames, ordered, useAccessors)
				}
			}
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun discoverEnum(klas: KClass<*>, clas: Class<out Any>): Type<*>? {
		return setType(klas) { EnumType(klas, clas.enumConstants as Array<Enum<*>>) }
	}
	
	private fun discoverGeneric(klas: KClass<*>, tArgs: List<KTypeProjection>, typeArgs: Map<String, Type<*>>?, typeKlas: KClass<*>?, propsNames: Array<String>?, ordered: Boolean, useAccessors:Boolean): Type<*>? {
		val argTypes = Array(tArgs.size) { ix ->
			val argKType = tArgs[ix].type ?: return logError("Cannot detect the Type of argument ${tArgs[ix]} of $klas")
			detectArgType(argKType, typeArgs, { null }) ?: return logError("Cannot detect the argument Type of $argKType of $klas")
		}
		return getType(klas, argTypes) ?:
		  if (typeKlas == null) buildType(klas, argTypes) ?: setType(klas, argTypes) { SchemaType(klas, argTypes, propsNames, ordered, useAccessors) }
		  else constructType(klas, typeKlas, argTypes)
	}
	
	private fun discoverArray(kType: KType, klas: KClass<*>, klasName: String, typeArgs: Map<String, Type<*>>?): Type<*>? {
		val eKType = kType.arguments.firstOrNull()?.type ?: return primArray(klasName)
		val eType = detectArgType(eKType, typeArgs, { if (it.java.isPrimitive) primitiveArray(it.simpleName!!) else null }) ?: return logError("No matching Type found for elements  $eKType of $klas")
		@Suppress("UNCHECKED_CAST")
		return setType(klas) { ArrayType(eType as Type<Any>, klas as KClass<Array<Any>>) }
	}
	
	private fun discoverMap(klas: KClass<*>, tArgs: List<KTypeProjection>, typeKlas: KClass<*>?, typeArgs: Map<String, Type<*>>?): Type<*>? {
		if (tArgs.size < 2 || tArgs[0].type == null || tArgs[1].type == null) return RawMapType
		val keyKType = tArgs[0].type!!
		val keyType = detectArgType(keyKType, typeArgs, { null }) ?: return logError("No matching Type found for  $keyKType of $klas")
		val valKType = tArgs[1].type!!
		val valType = detectArgType(valKType, typeArgs, { null }) ?: return logError("No matching Type found for  $valKType of $klas")
		val argTypes = arrayOf(keyType, valType)
		return getType(klas, argTypes) ?:
		  if (typeKlas == null) buildType(klas, argTypes) ?: logError("Can't build $klas<$keyType,$valType>")
		  else constructType(klas, typeKlas, argTypes)
	}
	
	private fun discoverColl(klas: KClass<*>, tArgs: List<KTypeProjection>, typeKlas: KClass<*>?, typeArgs: Map<String, Type<*>>?): Type<*>? {
		if (tArgs[0].type == null) return RawCollectionType
		val eKType = tArgs[0].type!!
		val eType = detectArgType(eKType, typeArgs, { null }) ?: return logError("No matching Type found for  elements  $eKType of $klas")
		val argTypes = arrayOf(eType)
		return getType(klas, argTypes) ?:
		  if (typeKlas == null) buildType(klas, argTypes) ?: logError("Can't build $klas<$eType>")
		  //TODO UniCollectionType
		  else constructType(klas, typeKlas, argTypes)
	}
	
	inline private fun detectArgType(kType: KType, typeArgs: Map<String, Type<*>>?, f: (KClass<*>) -> Type<*>?): Type<*>? {
		val argKlas = kType.classifier
		return if (argKlas is KClass<*>) f(argKlas) ?: resolve(kType, argKlas, typeArgs)
		else if (typeArgs != null && argKlas is KTypeParameter) typeArgs[argKlas.name]
		else null
	}
	
	private fun constructType(klas: KClass<*>, typeKlas: KClass<*>, argTypes: Array<out Type<*>> = EA): Type<*>? {
		val type = setType(klas, argTypes, { typeKlas.primaryConstructor!!.call(*argTypes) as Type<*> }) ?: return logError("Can't construct $typeKlas as Type class of $klas")
		if (type.typeKlas != klas) return logError("Supplied $typeKlas is not a valid Type class of $klas")
		return type
	}
	
	private fun primJava(name: String): Type<*>? = when (name) {
		"Integer" -> IntType
		"Long" -> LongType
		"Short" -> ShortType
		"Byte" -> ByteType
		"Character" -> CharType
		"Double" -> DoubleType
		"Float" -> FloatType
		"Boolean" -> BooleanType
		"Object" -> AnyType
		"Void" -> UnitType
		else -> logError("No matching type found for $name")
	}
	
	private fun primitive(name: String): Type<*>? = when (name) {
		"Int" -> IntType
		"Long" -> LongType
		"Short" -> ShortType
		"Byte" -> ByteType
		"Char" -> CharType
		"Double" -> DoubleType
		"Float" -> FloatType
		"Boolean" -> BooleanType
		else -> logError("No matching type found for $name")
	}
	
	private fun primitiveArray(name: String): Type<*>? = when (name) {
		"Byte" -> BytesType
		"Int" -> IntsType
		"Long" -> LongsType
		"Short" -> ShortsType
		"Char" -> CharsType
		"Double" -> DoublesType
		"Float" -> FloatsType
		"Boolean" -> BooleansType
		else -> logError("No matching type found for $name")
	}
	
	private fun primArray(name: String): Type<*>? = when (name) {
		"ByteArray" -> BytesType
		"IntArray" -> IntsType
		"LongArray" -> LongsType
		"ShortArray" -> ShortsType
		"CharArray" -> CharsType
		"DoubleArray" -> DoublesType
		"FloatArray" -> FloatsType
		"BooleanArray" -> BooleansType
		else -> logError("No matching type found for $name")
	}
	
	
	/**/
	
	internal fun typeArgTypes(klas: KClass<*>, argTypes: Array<out Type<*>>): Map<String, Type<*>> = mutableMapOf<String, Type<*>>().apply { klas.typeParameters.forEachIndexed { i, t -> this[t.name] = argTypes[i] } }
	
}



internal fun <T: Any> logError(msg:String): T? {
	System.err.println(msg)
	return null
}

internal fun <T: Any> evalError(v: Any?, type: Type<T>, e: Throwable? = null): T? {
	System.err.println("Cast of $v to ${type.typeKlas} failed${if (e == null) "" else ".  Caused by\n    " + e.toString()}")
	return type.default
}

