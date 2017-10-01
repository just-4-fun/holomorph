package just4fun.holomorph

import kotlin.reflect.KClass


/* Property annotation */
/* Constructor annotation */

/** Prompts the class schema to opt the annotated property as serializable. If one property of the class is annotated with @[Opt] the schema will select only the rest annotated with @[Opt].
 * @param [ordinal]  - the fixed distinct zero-based ordinal number of a property
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
annotation class Opt(val ordinal: Int = -1)


/* Define Schema Type */

/** Registers schema [Type] resolver with adjustments.
 * @param [properties]  the list of class' properties to be indexed by schema. Empty list suggests to pick all properties if none is annotated with @[Opt].
 * @param [nameless]  object is to be represented as nameless (true) or named (false) sequence of values.
 * @param [useAccessors]  optimization param:  true - uses properties accessors; false - uses Java field counterparts which is 2-3 times faster.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class DefineSchema(val properties: Array<String> = emptyArray(), val nameless: Boolean = false, val useAccessors:Boolean = true)


/* Define Type */

/** Registers the custom [Type] resolver for the annotated property.
 * @param [typeKlas]  - A [Type] resolver subclass meant to handle the type of the annotated property.
 * If the type is generic, params of  [typeKlas]'s primary constructor should be [Type] resolvers of its generic args.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class DefineType(val typeKlas: KClass<*>)


/* Type Interceptor */

/** Adds interceptor of entry values being consumed to the actual type resolver of the annotated property.
 * @param [interceptorKlas]  - A [ValueInterceptor] subclass meant to intercept value and convert it to the type of the annotated property.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class Intercept(val interceptorKlas: KClass<*>)
