package just4fun.holomorph.types

import just4fun.holomorph.EnclosedEntries
import just4fun.holomorph.ValueInterceptor
import just4fun.holomorph.Type



class InterceptorType<T: Any>(private val base: Type<T>, private val interceptor: ValueInterceptor<T>): Type<T> by base {
	override fun fromEntry(value: ByteArray): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: String): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Long): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Int): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Short): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Byte): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Char): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Double): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Float): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromEntry(value: Boolean): T? = interceptor.intercept(value) ?: base.fromEntry(value)
	override fun fromNullEntry(): T? =  interceptor.interceptNull() ?: base.fromNullEntry()
	override fun fromEntries(subEntries: EnclosedEntries, expectNames: Boolean): T? = interceptor.intercept(subEntries, expectNames) ?: base.fromEntries(subEntries, expectNames)
}
