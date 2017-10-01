package just4fun.holomorph.types

import just4fun.holomorph.Type
import just4fun.holomorph.evalError
import kotlin.reflect.KClass



/* COLLECTION TYPE */
abstract class CollectionType<T: Collection<*>, E: Any>(override final val typeKlas: KClass<T>, override final val elementType: Type<E>): SequenceType<T, E> {
	@Suppress("UNCHECKED_CAST")
	override fun iterator(seq: T): Iterator<E> = seq.iterator() as Iterator<E>
	
	@Suppress("UNCHECKED_CAST")
	override fun asInstance(v: Any): T? {
		var index = -1
		return when {
			isInstance(v) -> {
				// still can miss if non-first element is null
				if ((v as Collection<*>).isNotEmpty() && v.first().let { e -> e == null || !elementType.isInstance(e) }) {
					var coll = newInstance()
					for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
					onComplete(coll, coll.size)
				} else v as T
			}
			v is Collection<*> -> {
				var coll = newInstance()
				for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
				onComplete(coll, coll.size)
			}
			v is Array<*> -> {
				var coll = newInstance()
				for (e in v) coll = addElement(elementType.asInstance(e, false)!!, index++, coll)
				onComplete(coll, coll.size)
			}
		// case: xml node wrongly detected as Object due to limited info
			v is Map<*, *> && v.size <= 1 -> {
				var coll = newInstance()
				for (e in v) coll = addElement(elementType.asInstance(e.value, false)!!, index++, coll)
				onComplete(coll, coll.size)
			}
			else -> evalError(v, this, Exception("${v.javaClass.kotlin.javaObjectType} is not ${typeKlas.javaObjectType}"))
		}
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun copy(v: T?, deep: Boolean): T? = if (!deep || v == null) v else (v.map { elementType.copy(it as E?, true) } as T)
	
	@Suppress("UNCHECKED_CAST")
	override fun equal(v1: T?, v2: T?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		val itr1 = v1.iterator()
		val itr2 = v2.iterator()
		while (itr1.hasNext()) if (!elementType.equal(itr1.next() as E?, itr2.next() as E?)) return false
		true
	})
	
	@Suppress("UNCHECKED_CAST")
	override fun hashCode(v: T): Int {
		var code = 1
		v.forEach { code = code * 31 + elementType.hashCode(it as E) }
		return code
	}
	
	@Suppress("UNCHECKED_CAST")
	override fun toString(v: T?, sequenceSizeLimit: Int): String = v?.map { elementType.toString(it as E?, sequenceSizeLimit) }?.joinToString(", ", "[", "]", sequenceSizeLimit) ?: "null"
	
	override fun hashCode(): Int = typeKlas.hashCode() * 31 + elementType.typeKlas.hashCode()
	override fun toString() = typeName
	override fun equals(other: Any?) = this === other || (other is CollectionType<*, *> && typeKlas == other.typeKlas && elementType ==  other.elementType)
	
}




/* LIST  TYPE */
@Suppress("UNCHECKED_CAST")
class ListType<E: Any>(elementType: Type<E>): CollectionType<MutableList<E>, E>(MutableList::class as KClass<MutableList<E>>, elementType) {
	override fun newInstance(): MutableList<E> = mutableListOf()
	override fun addElement(e: E?, index: Int, seq: MutableList<E>): MutableList<E> = seq.also { (it as MutableList<E?>) += e }
}




/* SET  TYPE */
@Suppress("UNCHECKED_CAST")
class SetType<E: Any>(elementType: Type<E>): CollectionType<MutableSet<E>, E>(MutableSet::class as KClass<MutableSet<E>>, elementType) {
	override fun newInstance(): MutableSet<E> = mutableSetOf()
	override fun addElement(e: E?, index: Int, seq: MutableSet<E>): MutableSet<E> =seq.also {  (it as MutableSet<E?>) += e }
	override fun equal(v1: MutableSet<E>?, v2: MutableSet<E>?): Boolean = v1 === v2 || (v1 != null && v2 != null && v1.size == v2.size && run {
		for (e1 in v1) if (v2.none { e2 -> elementType.equal(e1, e2) }) return false
		true
	})
}

//TODO
//	ArrayList<E> = java.util.ArrayList<E>
//	LinkedHashMap<K, V> = java.util.LinkedHashMap<K, V>
//	HashMap<K, V> = java.util.HashMap<K, V>
//	LinkedHashSet<E> = java.util.LinkedHashSet<E>
//	HashSet<E> = java.util.HashSet<E>
//	SortedSet<E> = java.util.SortedSet<E>
//	TreeSet<E> = java.util.TreeSet<E>}
