![holo-logo](https://github.com/just-4-fun/holomorph/blob/master/docs/images/holomorph.png)  
> **1. Serializes objects to/from various formats and sources**  
> **2. Materializes class properties for building data queries or a custom DSL**

## Features:
- [Built-in serialization forms: json, object, collection, map](#guick-guide)
- [Direct conversion of one serialization form into another](#serialization-forms)
- [Extensible framework to define more serialization forms](#serialization-forms)
- [Automatic tunable class schema generation](#tuning-the-schema)
- [Comprehensive extensible type support (primitives, string, enums, arrays, standard collections, map, custom classes, including generic)](#property-types)
- [Deserialization via a class constructor](#deserialization)
- [Utility methods: copy, equal, hashCode, toString](#guick-guide)
- [Compact (nameless) serialization mode](#nameless-mode)
- ...  

## Content:  
- [Quick guide](#guick-guide)
- [Basic terms](#basic-terms)
- [Tuning the schema](#tuning-the-schema)
- [Deserialization](#deserialization)
- [Property types](#property-types)
- [Serialization forms](#serialization-forms)
- [Nameless mode](#nameless-mode)
- [Schema evolution](#schema-evolution)  

## Quick guide
**The way to get started is to instantiate or extend the  `Reflexion`  class.**   
```scala
val pets = Reflexion(Pet::class)
class Pet (val name: String, val kind: String, val age: Int)
```   
- From now on you can get a new pet from a text:   
```scala
val text = "{name: Ben, kind: Hedgehog, age: 2}"
val newPet = pets.instanceFrom(text, DefaultFactory)
// DefaultFactory is a built-in json factory, in which place a factory of any form can be
```   
- And get a text from your pet:   
```scala
val myPet = Pet(“Zeus”, “Fish”, 1)
val text = pets.instanceTo(myPet, DefaultFactory) 
// yields {name:Zeus,kind:Fish,age:1}
```   
- And use other production methods: `instanceUpdateFrom`, `instancesFrom`, `instancesTo`; and schema based utility methods: `copy`, `equal`, `hashCode`, `toString`, ...  

**Another application of the  `Reflexion`  is the materialization of class properties.**  
```scala
object Pets: Reflexion<Pet>(Pet::class)  {
    val name by property
    val kind by property
}
```  
- These materialized properties are combined into the `Reflexion.properties` list and can be used for filtering a producing output:   
```scala
val text = Pets.instanceTo(myPet, DefaultFactory, Pets.properties) 
// yields {name:Zeus,kind:Fish} !! no ‘age’
```  
- And for building a query to a database:  
```scala
val query = “SELECT $name, $kind FROM Pets”
// yields “SELECT name, kind FROM Pets”
```   
- Also, the class of materialized property `Property<T>` can be extended to allow more flexible use. For example, to be a part of a DSL.  
```scala
class Pty<T: Any>(val base: Property<T>): Property<T> by base {
    val nameUpper = base.name.toUpperCase()
}
object Pets: Reflexion<Pet>(Pet::class)  {
    val name by property<String>()
    val age by property<Int>()
    override fun <T: Any> newProperty(base: Property<T>): Property<T> = Pty(base)
}
fun Property<*>.nameUpper():String = (this as Pty).nameUpper
println(Pets.name.nameUpper) // prints 'NAME'
fun Property<Int>.assign(pet: Pet, v: Int) = set(pet, v) // just to show the way typed property works.
Pets.age.assign(pet, 1) // compiles
Pets.name.assign(pet, 200) // compiler complains: 'Type mismatch: Required Property<Int>'
```   

## Basic terms
- **class** _(hereinafter in bold)_: _a custom serializable class_
- property: _any (`val` or `var`) property of the_ **class**
- property type: _the class of property values_
- type resolver: _an instance of `Type` handling de/serialization of some property type_
- schema: _an instance of `SchemaType` generated for the_ **class** _reflecting its serializable structure (properties)_

## Tuning the schema
Which properties of a **class** take part in de/serialization depends on the **class** schema (instance of `SchemaType`) that is automatically generated and cached.    
The schema chooses serializable properties of the **class** in the following way:
- A list of property names can be passed to the schema in which case they’ll be associated with **class** properties.  
   There are three ways to pass the list:
  - Annotate with `@DefineSchema` a **class** for which the schema is   
  ```scala
    @DefineSchema(["x","y"]) class Point (x: Int, y: Int)
  ```   
  - Annotate with `@DefineSchema` a property for which type the schema is. This should be done once.
  ```scala
    class Shape {@DefineSchema(["x","y"]) var top: Point, var bottom: Point}
  ```   
  - Instantiate the `SchemaType` class directly or via `Types.defineType` passing the list to constructor
  ```scala
    Types.defineType(Point::class){ SchemaType(Point::class, propsNames= ["x","y"]) }
  ```   
  
  If an empty non-null list is passed the schema selects **all** properties of the **class**.  
  ```scala
  @DefineSchema class Point (private val x: Int, private val y: Int) // all properties will be selected 
  ```   
- Otherwise, if the schema encounters properties annotated with `@Opt` it selects only those ones. 
  ```scala
  class Credentials(@Opt val name: String, val password: String) // only name will be selected
  ```   
- Otherwise, the schema selects only those properties which visibility is less restrictive than `Property.visibilityBound`, that is public by default.  
  ```scala
  class Person(val name: String, private val age: Int) // only name will be selected
  ```   

So if no adjustments are made the schema collects only public properties of the underlying **class**.

## Deserialization
The schema is also responsible for deserialization of an object via its constructor.   
If the **class** has more than one constructor, the schema selects it as following:
- first constructor annotated with `@Opt`
  ```scala
  class Shape(center: Point) {@Opt constructor(x: Int, y: Int)...}
  // the annotated constructor will be selected 
  ```   
- otherwise, constructor with a maximum number of parameters matching properties
  ```scala
  class Thing(a: Int) {constructor(a: Int, b: Int)... val a: Int ... val b: Int ...}
  // the second constructor will be selected 
  ```   
- otherwise, constructor with a minimum number of parameters  
  ```scala
  class Thing(a: Int) {constructor()... val x: Int ...}
  // the empty constructor will be selected 
  ```   

_Note that constructor of a Java class does not maintain parameter names at runtime so its parameters have generated names: `arg0`, `arg1` and so on._   
```java
class JavaPoint(int x, int y){...}
// argument runtime names: arg0, arg1
```  

While deserializing an object the schema picks entry by entry from a source data and for each entry:
- looks for a name match among constructor parameters
- if none is found looks for a name match among properties
- if none is found the entry is ignored  
Finally for those constructor parameters which weren't assigned from the source data the default values corresponding to their types are assigned.  
```scala
class Point3d(val x: Int, val y: Int) {val z: Int...}
// deserializing from json: {x:1, oops:2, z:3}
// yields the point: x=1, y=0, z=3. Entry 'oops:2' is ignored
```  

## Property types  
To be able to de/serialize value of a particular **class** property its type should have the corresponding implementation of the type resolver (`Type`) class.  
Types supported out-of-the-box: _primitives, String, Any, Unit, enums, arrays, (Mutable)List, (Mutable)Set, (Mutable)Map as well as custom **classes** with the generated schema, including generic ones._  
If a type doesn’t fit any of the above it should have custom resolver defined and registered. Otherwise it’s treated as a custom **class** with the generated schema.  
A custom resolver can be defined by extending:
- `LongBasedType`, `StringBasedType`, `BytesBasedType`: suitable in case when the underlying type can be de/serialized as a value of `Long`, `String` or `ByteArray`. For example, the class `Date` as a `Long` value
  ```scala
   class DateType: LongBasedType<Date>(Date::class) {
      override fun newInstance(): Date = Date()
      override fun fromValue(v: Long): Date? = Date(v)
      override fun toValue(v: Date?): Long? = v?.time
   }
  ```   
- `CollectionType`: suitable for collections
  ```scala
   class AListType<E: Any>(elementType: Type<E>): CollectionType<ArrayList<E>, E>(ArrayList::class as KClass<ArrayList<E>>, elementType) {
      override fun newInstance(): ArrayList<E> = ArrayList()
      override fun addElement(e: E?, index: Int, seq: ArrayList<E>): ArrayList<E> = seq.also { (it as ArrayList<E?>) += e }
   }
  ```   
- `Type`: if none of the above fits  

A custom resolver can be registered:
- via the `Types.defineType` method
  ```scala
  Types.defineType(Date::class){ DateType() }
  ```   
- by annotating a **class** property of the type in question with `@DefineType`, passing it the resolver class.  
  ```scala
  class Person(val name: String, @DefineType(DateType::class) val birth: Date)
  ```   
- by instantiating it and adding it via the `Types.addType`

_Registration should happen before usage._  
_Type resolvers have themselves cached. At the point when all `Reflexion`s (i.e. schemas) are initialized the cache may be cleared by calling the `Types.clearCache`._  

## Serialization forms   
The library actually produces an **output** from an **input**.   
```scala
val output = Produce ( EntryProviderImpl(input), EntryConsumerImpl() )
```  
So all it takes to define new serialization form is the corresponding implementation of the `EntryProvider` for the **input** and the `EntryConsumer` for the **output**.   
The **input** and the **output** are sources _(structured object, stream, string or byte array)_ of various formats _(like json, xml)_.   
The `EntryProvider` disassembles the **input** into a container of entries providing them to the `EntryConsumer` which consumes them assembling the **output**.   
The `Produce` class orchestrates the entire procedure according to the following _Entry Flow Algorithm_:
![entry_flow_algorithm](https://github.com/just-4-fun/holomorph/blob/master/docs/images/entry_flow_algorithm.jpg)   

> Let's teke json format as an example of a valid serialization form.  The record:  
`{name:'circle', radius:2, center:{x:0,y:0}, color:[0,0,255]}`  
can be represented as the sequence of the named entries (i.e. named container):  
`name:'circle'`, `radius:2`, `center:{x:0,y:0}`, `color:[0,0,255]`  
Where the second entry `radius:2` has name 'radius' and value '2'.   
The third entry's value is itself a named container with the named entries: `x:0`, `y:0`.   
Whilst the fourth entry's value is a nameless container with the nameless entries: `0`, `0`, `255`.   

The library has a built-in json serialization form implementation accessible through the `DefaultFactory` object.  

#### The EntryProvider and EntryConsumer implementation.  
The `EntryProvider<IN>` takes the `input:IN` as the constructor parameter, and overrides the function  
`fun provideNextEntry (entryBuilder: EntryBuilder, provideName: Boolean): Entry`  
which disassembles the `input` into entries one per each call and returns that `Entry` by calling one of the `entryBuilder` `...Entry` functions, thereby providing the entry.  
Once the entry has been provided by `EntryProvider` the `EntryConsumer<OUT>`'s `consumeEntry` function of the corresponding value type is called. The `EntryConsumer` accumulates consumption results and returns the aggregate result with the `output:OUT` function.  
To be able to use an `EntryProvider` and `EntryConsumer` in the `Reflexion` methods there should be an `EntryProviderFactory` and `EntryConsumerFactory` defined. The interface `ProduceFactory` combines both for the sake of simplicity. The `invoke` methods should return a new instance of a provider and consumer respectively.   
> This is how the default json factory is  defined:  
```scala
object DefaultFactory: ProduceFactory<String, String> {
   override fun invoke(input: String): EntryProvider<String> = DefaultProvider(input)
   override fun invoke(): EntryConsumer<String> = DefaultConsumer()
}
```  

## Nameless mode
Normally an object is serialized as a sequence of named entries because name-based deserialization is straightforward since each name is associated with a **class** property.  
By this logic, if properties are associated with ordinal indexes, thereby are serialized and deserialized in the same order, then the object can be represented as a sequence of nameless entries (i.e. just values).  
Such elimination of names benefits performance and reduces the data size.   
> Compare for example, the data size of the same object represented by the following named and nameless sequences in JSON format:  
   named: `{"name":"circle", "radius":2, "center":{"x":0, "y":0}, "color":{"red":0, "green":0, "blue":255}}`  
   nameless: `["circle", 2, [0,0], [0,0,255]]`  
   Total chars: 90 (named) VS 28 (nameless)  

The _**nameless mode**_ can be controlled:
- by annotating a **class** with `@DefineSchema` with parameter `nameless` set to true
  ```scala
    @DefineSchema(nameless=true) class Point (val x: Int, val y: Int)
  ```   
- by annotating serializable **class** properties with `@Opt`, which parameter `ordinal` denotes the fixed distinct zero-based ordinal number of a property
  ```scala
    class Point (@Opt(0) val x: Int, @Opt(1) val y: Int)
  ```   
- by controlling the parameter `nameless` of the `Reflexion.instanceTo` method:  
  - `null` lets each schema be serialized according to its `nameless` value
  - `true` makes each schema be serialized as a nameless sequence
  - `false` makes each schema be serialized as named sequence   
  ```scala
    // this class schema's 'nameless' is implicitly set to true 
    class Point (@Opt(0) val x: Int, @Opt(1) val y: Int)
    // this class schema's 'nameless' is implicitly set to false 
    class Circle (val radius:Int, val center: Point)
    val obj = Circle(4, Point(1,2))
    val circles = Reflexion(Circle::class)
    val v1 = circles.instanceTo(obj, DefaultFactory) // v1= {radius:4, [1,2]}
    val v2 = circles.instanceTo(obj, DefaultFactory, nameless=true) // v2= [4, [1,2]]
    val v3 = circles.instanceTo(obj, DefaultFactory, nameless=false) // v3= {radius:4, {x:1, y:2}}
  ```   

## Schema evolution
The _**nameless mode**_ also lets modify property names without an influence on the schema.    
What happens in particular when the schema evolves in this mode:
- if a property is added to the schema its `@Opt` `ordinal` should have the current maximum value + 1.    
  ```scala
    class Thing (@Opt(0) val pty1: Int, @Opt(1) val pty2: Int, @Opt(2) val pty3: Int)
    // 'pty3' was added with ordinal=2
    // now the sequence may look like: [1, 2, 3]
  ```   
- if a property is removed from the schema a null-value entry (“hole”) appears in a serialized sequence of entries in the position of its ordinal.  
  ```scala
    class Thing (@Opt(0) val pty1: Int, @Opt(2) val pty3: Int)
    // 'pty2' was removed
    // now the sequence may look like: [1, 0, 3] where 0 is the "hole" left after removed pty2
  ```   
   _There is a caution though: if the schema is very dynamic and a lot of properties were removed during its evolution, the resulted serialized sequence can happen to be over cluttered with “holes” that causes the adverse effect._
- if the property name is changed there’s nothing to care about;
- if the property type is changed its type resolver tries to convert the incoming entry value to the underlying type. If the conversion is failed the default value (or `null` if allowed) is set.   
   - Conversion between simple value types (numeric types, `Boolean`, `String`) is quite obvious.    
     > For example, if these are two versions of the same class:  
     ```scala
     class Thing (val p1: String, val p2: String,  val p3: Int)
     class Thing (val p1: Int,    val p2: Int,     val p3: Boolean)
     ```   
     > Then the conversion from one to another in json looks like:  
     `{p1:"10", p2:"ok", p3:1   }`  
     `{p1:10,   p2:0,    p3:true}`  

   - Conversion backed by a custom type resolver (subclass of `LongBasedType`, `StringBasedType`, `BytesBasedType` as well as `Type`) lies on implementation of its methods `fromEntry(...)`.   
     > For example, if these are two versions of the same class:  
     ```scala
     class Thing (val p1: String)
     class Thing (val p1: Date)
     class DateType: LongBasedType<Date>(Date::class) {
        ...
        override fun fromEntry(value: String): Date? = Date(value)
     }
     ```   
     > Then the conversion from one to another in json looks like:  
     `{p1:"Wed May 18 06:33:20 EEST 2033"}`   
     `{p1:2000000000000}`  
     
   - Conversion can also be adjusted by annotating a **class** property with `@Intercept`, with a subclass of the `ValueInterceptor` passed to the parameter `interceptor` and overriding one or more of its methods. If a method returns `null` then the property type resolver handles a conversion.    
     > For example, if these are two versions of the same class:  
     ```scala
     class Thing (val p1: String)
     class Thing (@Intercept(Interceptor::class) val p1: Int)
     class Interceptor: ValueInterceptor<Int> {
        override fun intercept(value: String): Int? = value.length
     }
     ```   
     > Then the conversion from one to another in json looks like:  
     `{p1:"test"}`  
     `{p1:4}`  

// TODO: notes about ProGuard  
// TODO: alias trick  
// TODO: warn: constructor doesn't use constructor default values  
// TODO warn: delegate serializable property isn't supported  
