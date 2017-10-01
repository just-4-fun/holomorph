package just4fun.holomorph.types

import just4fun.holomorph.*
import just4fun.kotlinkit.Result
import just4fun.holomorph.forms.SchemaConsumerN
import just4fun.holomorph.forms.SchemaConsumerV
import just4fun.holomorph.forms.SchemaFactory
import just4fun.holomorph.forms.SchemaProvider
import kotlin.reflect.*
import  just4fun.holomorph.Property.Companion.visibilityBound
import  just4fun.holomorph.PropertyVisibility.PRIVATE
import kotlin.reflect.full.*



/** Represents the schema of the underlying [typeKlas]. Responsible for selecting a constructor and serializable properties of the [typeKlas]. Provides production and utility methods for objects of type [T].
 * [See this guide for details.](https://just-4-fun.github.io/holomorph/#tuning-the-schema)
 * @constructor Creates the schema of the class [typeKlas] with optional [argTypes], [propsNames] and [nameless] parameters.
 */

class SchemaType<T: Any>(override val typeKlas: KClass<T>, argTypes: Array<out Type<*>>? = null, propsNames: Array<String>? = null, nameless: Boolean = false, useAccessors: Boolean = true): Type<T>, Producer<T>, EntryHelperDefault<T> {
	/** The list of serializable properties of the [typeKlas] */
	val properties: List<Property<Any>> = mutableListOf()
	/** The map of serializable properties of the [typeKlas] with their names as the key.*/
	val propertiesMap: Map<String, Property<*>> = mutableMapOf()
	private var params: List<Param>? = null
	internal var paramsMap: MutableMap<String, Param>? = null
	internal var allSize = 0
	internal var paramsSize = 0
	private lateinit var constructor: KFunction<T>
	internal val emptyConstr get() = paramsSize == 0
	/** Indicates the serialization mode. Can be set explicitly. Otherwise is set to true only if ordinal indexes of properties are positive and distinct. */
	var nameless = nameless; private set(v) = run { field = v }
	/** Optimization param indicating class properties have accessors, otherwise properties are accessed via Java field which is 2-3 times faster.*/
	var useAccessors = useAccessors; private set(v) = run { field = v }
	/** The [ProduceFactory] can be used with [Producer] methods and other which require [EntryProviderFactory] and [EntryConsumerFactory] as parameters. */
	val produceFactory by lazy { SchemaFactory(this) }
	private val listType by lazy { Types.getOrAddType(MutableList::class, this) { ListType(this) } }
	
	init {
		try {
			if (argTypes == null) {
				Types.setType(typeKlas) { this }
				init(propsNames, null)
			} else {
				Types.setType(typeKlas, argTypes) { this }
				init(propsNames, Types.typeArgTypes(typeKlas, argTypes))
			}
		} catch (x: Throwable) {
			Types.removeType(this)
			throw x
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun init(propsNames: Array<String>?, typeArgs: Map<String, Type<*>>?) {
		if (typeKlas.isInner) throw Exception("Failed to schemify $typeKlas since it is inner.")
		//
		val props = properties as MutableList<PropImpl<Any>>
		val propsMap = propertiesMap as MutableMap<String, PropImpl<*>>
		
		fun <T: Any> addRealProp(kProp: KProperty1<*, *>, index: Int, name: String, nullable: Boolean, type: Type<T>, rType: KType) {
			val prop = PropImpl(index, name, type, this, nullable) as PropImpl<Any>
			prop.rType = rType
			prop.initAccessors(kProp)
			props.add(prop)
			propsMap[name] = prop
		}
		
		fun initProps(visibility: Int) {
			var annotated = false
			var maxIndex = -1
			for (kProp in typeKlas.memberProperties) {
				var pAnn: Opt? = null
				var sAnn: DefineSchema? = null
				var tAnn: DefineType? = null
				var iAnn: Intercept? = null
				kProp.annotations.forEach {
					if (it is Opt) pAnn = it
					if (it is Intercept) iAnn = it
					if (it is DefineSchema) sAnn = it else if (it is DefineType) tAnn = it
				}
				if (pAnn == null && (annotated || kProp.visibility.let { it == null || it.ordinal > visibility })) continue
				val rType = kProp.returnType
//				println("Discovering prop '${kProp.typeName}':  ${rType}; tp? ${rType.classifier is KTypeParameter}")
				val klas = rType.classifier
				val type0 = if (klas is KClass<*>) Types.resolve(rType, klas, typeArgs, tAnn?.typeKlas, sAnn?.properties, sAnn?.nameless == true, sAnn?.useAccessors == true)
				else if (typeArgs != null && klas is KTypeParameter) typeArgs[klas.name]
				else null
				if (type0 == null) throw Exception("Failed detecting the Type of ${kProp.name}: $rType of $typeKlas ")// TODO specific
				val type = iAnn?.let {
					val icpr = it.interceptorKlas.createInstance() as? ValueInterceptor<*> ?: throw Exception("Failed creating the ${ValueInterceptor::class.simpleName} of '${kProp.name}' of $typeKlas  from ${it.interceptorKlas}")// TODO specific
					InterceptorType(type0 as Type<Any>, icpr as ValueInterceptor<Any>)
				} ?: type0
				val index = pAnn?.let {
					if (!annotated) run { props.clear(); annotated = true }
					if (it.ordinal > maxIndex) maxIndex = it.ordinal
					it.ordinal
				} ?: props.size
				//
				addRealProp(kProp, index, kProp.name, rType.isMarkedNullable, type, rType)
			}
			// check order
			if (maxIndex > -1) {
				if (maxIndex == props.size - 1) {
					props.sortBy { it.ordinal }
					nameless = props.all { it.ordinal == ++maxIndex - props.size }
				} else if (maxIndex >= props.size) {
					val array = arrayOfNulls<Property<*>>(maxIndex + 1)
					nameless = props.all { if (array[it.ordinal] == null) run { array[it.ordinal] = it; true } else false }
					if (nameless) array.forEachIndexed { ix, p ->
						val prop = (p ?: PropImpl(ix, "", UnitType, this, true)) as PropImpl<Any>
						if (ix < props.size) props[ix] = prop else props.add(prop)
					}
				}
				if (!nameless) {
					(props as List<PropImpl<*>>).forEachIndexed { ix, p -> p.ordinal = ix }
					System.err.println("Properties order of $typeKlas is inconsistent. To be nameless properties should be annotated with '@${Opt::class.simpleName}' and ordinal should be explicitly set, zero-based and distinct.")
				}
			}
		}
		
		fun initTargetProps(propNames: Array<String>) {
			for (name in propNames) {
				val kProp = typeKlas.memberProperties.find { it.name == name } ?: throw Exception("Property [$name] isn't found within $typeKlas. ")// TODO specific
				val rType = kProp.returnType
				val type = Types.resolve(rType, rType.classifier as KClass<*>) ?: throw Exception("Failed detecting the Type of $typeKlas.${kProp.name}: $rType ")// TODO specific
				addRealProp(kProp, props.size, name, rType.isMarkedNullable, type, rType)
			}
		}
		
		fun initConstructor() {
			// find most appropriate constructor
			var minPmsConstr: KFunction<T>? = null// min num of params
			var maxMatchConstr: KFunction<T>? = null// max matches with props
			var lastSize = 0
			var maxMatch = -1
			var minPms = 10000
			for (constr in typeKlas.constructors) {
				if (constr.findAnnotation<Opt>() != null) {
					minPmsConstr = constr; maxMatchConstr = constr; break
				}
				val size = constr.parameters.size
				var count = 0
				constr.parameters.forEach { if (propsMap[it.name]?.rType == it.type) count++ }
				if (maxMatch < count || (maxMatch == count && lastSize > size)) run { maxMatch = count; lastSize = size; maxMatchConstr = constr }
				if (size < minPms) run { minPms = size; minPmsConstr = constr }
//				println("SELECTING...  maxMatch=$maxMatch;  minPms= $minPms;  actualPms= ${constr.parameters.size}")
			}
			//prefer one with max matches OR if no matches prefer one with min num of params
			val constr = (if (maxMatch == 0) minPmsConstr else maxMatchConstr) ?: throw Exception("$typeKlas has no constructor.")
			constructor = constr
			if (constr.parameters.isEmpty()) return
			val prmMap = mutableMapOf<String, Param>()
			val prms = constr.parameters.map {
				val prop = propsMap[it.name]
				val type = if (prop?.rType == it.type) prop.type else Types.resolve(it.type, it.type.classifier as KClass<*>) ?: throw Exception("Failed detecting the Type of $typeKlas constructor parameter '${it.name}: ${it.type} ")// TODO specific
				val param = Param(it.index, it.name!!, type, it.isOptional, it.type.isMarkedNullable)
				prmMap[param.name] = param
				param
			}
			paramsSize = prms.size
			params = prms
			paramsMap = prmMap
		}

//		println("Schemifying: ${typeKlas};  ${typeKlas.typeParameters.map { "${it.typeName}: ${it.upperBounds}" }};  typeMap: $typeArgsMap")
		val names = typeKlas.findAnnotation<DefineSchema>()?.let {
			useAccessors = it.useAccessors
			nameless = it.nameless; it.properties
		} ?: propsNames
		// process properties
		if (names == null) initProps(visibilityBound.ordinal) else if (names.isEmpty()) initProps(PRIVATE.ordinal) else initTargetProps(names)
		allSize += props.size
		if (props.size == 0) throw Exception("Failed detecting any public or annotated with '@${Opt::class.simpleName}' property in $typeKlas.")// TODO specific
		//		println("$this properties: $properties")
		// process constructor
		initConstructor()// TODO optimize . consumes 65%
		if (params != null) allSize += paramsSize
		//		println("$this params: $params")
		props.forEach { it.rType = null }
	}
	
	override fun newInstance(): T = if (paramsSize == 0) typeKlas.java.newInstance() else newInstance(Array(allSize) {})
	internal fun newInstance(values: Array<Any?>): T {
		val params = this.params!!
		val constrVals = values.copyOf(paramsSize)
		constrVals.forEachIndexed { ix, v ->
			if (v == Unit || v == null) constrVals[ix] = params[ix].default// TODO can throw here
		}
//		val obj = constructor.javaConstructor!!.newInstance(*constrVals)
		val obj = constructor.call(*constrVals)
		for (n in paramsSize until allSize) {
			val v = values[n]
			if (v != Unit) properties[n - paramsSize].setChecked(obj, v)
		}
		return obj
	}
	
	
	fun <D: Any> instanceUpdateFrom(inst: T, input: D, factory: EntryProviderFactory<D>): Result<T> = Produce(factory(input), SchemaConsumerN(this, instance = inst))
	override fun <D: Any> instanceFrom(input: D, factory: EntryProviderFactory<D>): Result<T> = Produce(factory(input), if (paramsSize == 0) SchemaConsumerN(this) else SchemaConsumerV(this))
	override fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>): Result<D> = Produce(SchemaProvider(input, this), factory())
	fun <D: Any> instanceTo(input: T, factory: EntryConsumerFactory<D>, props: List<Property<*>>?, nameless: Boolean? = null): Result<D> = Produce(SchemaProvider(input, this, props, nameless), factory())
	fun <D: Any> instancesFrom(input: D, factory: EntryProviderFactory<D>): Result<MutableList<T>> = listType.instanceFrom(input, factory)
	fun <D: Any> instancesTo(input: List<T>, factory: EntryConsumerFactory<D>): Result<D> = listType.instanceTo(input as MutableList<T>, factory)
	
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean) = subEntries.intercept(if (paramsSize == 0) SchemaConsumerN(this, false) else SchemaConsumerV(this, false))
	
	override fun toEntry(value: T, name: String?, entryBuilder: EntryBuilder): Entry {
		val named = entryBuilder.context.nameless == false || (entryBuilder.context.nameless == null && !nameless)
		return entryBuilder.StartEntry(name, named, SchemaProvider(value, this, null, null, false))
	}
	
	override fun isInstance(v: Any): Boolean {
		return super.isInstance(v) || typeKlas.java.isAssignableFrom(v.javaClass)
	}
	
	override fun toString(v: T?, sequenceSizeLimit: Int): String {
		if (v == null) return "{}"
		val text = StringBuilder("{")
		val propsSize = properties.size
		properties.forEachIndexed { ix, p ->
			val value = p.type.toString(p[v], sequenceSizeLimit)
			text.append("${p.name}=").append("$value")
			if (ix < propsSize - 1) text.append(", ")
		}
		text.append("}")
		return text.toString()
	}
	
	private fun equalProps(v1: T, v2: T): Boolean = properties.all { p ->
		val r = p.type.equal(p[v1], p[v2])
		//todo for test
//		if (!r) println("NonEQ: ${typeKlas.simpleName}.${p.typeName}; PType= ${p.type};  V1:${p[v1]?.javaClass?.typeName}= ${p.type.toString(p[v1])};  V2:${p[v2]?.javaClass?.typeName}= ${p.type.toString(p[v2])}")
		r
	}
	
	override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && equalProps(v1, v2))
	override fun copy(v: T?, deep: Boolean): T? = if (v == null) null else {
		@Suppress("UNCHECKED_CAST")
		if (paramsSize == 0) newInstance().also { for (p in properties) p.setChecked(it, if (deep) p.type.copy(p[v], true) else p[v]) }
		else {
			val values = Array<Any?>(allSize) {}
			val params = paramsMap!!
			for (p in properties) {
				val value = if (deep) p.type.copy(p[v], true) else p[v]
				values[params[p.name]?.run { index } ?: (paramsSize + p.ordinal)] = value
			}
			newInstance(values)
		}
	}
	
	override fun hashCode(v: T): Int {
		var code = 1
		properties.forEach { p ->
			val pv = p[v]
			code = code * 31 + if (pv == null) 0 else p.type.hashCode(pv)
		}
		return code
	}
	
	override fun toString() = typeName
	override fun hashCode(): Int = typeKlas.hashCode()
	override fun equals(other: Any?) = this === other || (other is SchemaType<*> && typeKlas == other.typeKlas)
}
