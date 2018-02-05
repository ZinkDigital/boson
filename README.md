# boson
[![Build Status](https://api.travis-ci.org/ZinkDigital/boson.svg)](https://travis-ci.org/ZinkDigital/boson)

Streaming Data Access for BSON and JSON encoded documents


## Basic Usage

### Extracting from an encoded Bson (Scala)

```scala
//  This is the Bson encoded to a byte array
val validatedByteArray: Array[Byte] = bsonEvent.encode().array()

//  Expression is a String with a key representing the value to be extracted
//  followed by a term that in this case implies that the key represents a BsonArray,
//  the last element is a second key that will filter this position looking for it.
//  There are more combinations of expressions, further down in this document are tables
//  with possible combinations and outputs.
val expression: String = "fridgeReadings.[1].fanVelocity"

//  This CompletableFuture has the purpose of allowing asynchronicity.
val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()

//  Next step is to construct the extractor object, it takes as arguments the previous expression
//  and a Consumer. The value extracted will always be a BsValue so the CompletableFuture has to have
//  the same type.
val boson: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))

//  Calling this method triggers the extractor object to extract on the given byte array.
//  This way it's possible to create only once the extractor object and call this method
//  several times with different byte arrays.
boson.go(validatedByteArray)

// Final result
val result: BsValue = future.join()
```

### Extracting from an encoded Bson (Java)
```java
final byte[] validBsonArray  = bsonEvent.encodeToBarray();

final String expression = "fridges[3].serialCode";

final CompletableFuture<String> result = new CompletableFuture<>();

final Boson boson = Boson.extractor(expression, (String in) -> result.complete(in) );

boson.go(validBsonArray);

BsValue extracted = result.join()
```

### Injecting in an encoded Bson (Scala)
```scala
//  This is the Bson encoded to a byte array
val validBsonArray: Array[Byte] = bsonEvent.encodeToBarray

//  Expression is a String with a key representing the value to be extracted
//  followed by a term that in this case implies the injection to the first
//  ocorrence of 'name'
val expression = "name.first"


//  Next step is to construct the injector object, it takes as arguments the previous expression
//  and injector function of type T => T, this implies that the function needs to respect the type
//  of the element we are changing and injecting.
val boson: Boson = Boson.injector(expression, (in: String) => "newName")

//  Calling this method triggers the injector object to inject on the given byte array.
//  This way it's possible to create only once the injector object and call this method
//  several times with different byte arrays.
//  In this case, this method returns a CompletableFuture with the type of the input buffer,
//  in this case Array[Byte], allowing asynchronicity.
val result: CompletableFuture[Array[Byte]] = boson.go(validBsonArray)

//  To obtain the new buffer after the injection we need to retrieved it from the CompletableFuture
val resultValue: Array[Byte] = result.join()
```

### Injecting in an encoded Bson (Java)
```java
byte[] validBsonArray = bson.encodeToBarray();

String expression = "name.first";

Boson boson = Boson.injector(expression, (String in) -> "newName");

CompletableFuture<byte[]> result = boson.go(validBsonArray);

byte[] resultValue =  result.join();
```

### Fusing Boson(extractor and injector) (Scala)
```scala
// First step is to construct both Boson.injector and Boson.extractor by providing the necessary arguments.
val validatedByteArray: Array[Byte] = bsonEvent.encode().array()
val expression = "name.first"
val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
val ext: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))

val inj: Boson = Boson.injector(expression, (in: String) => "newName")

// Then call fuse() on injector or extractor, it returns a new BosonObject
val fused: Boson = ext.fuse(inj)

// Finally call go() providing the byte array or a ByteBuffer on the new Boson object
val finalFuture: CompletableFuture[Array[Byte]] = fused.go(validatedByteArray)
finalFuture.join()
```

### Fusing Boson(extractor and injector) (Java)
```java
final byte[] validatedByteArray  = bsonEvent.encodeToBarray();
final String expression = "name.first";
final CompletableFuture<BsValue> future = new CompletableFuture<>();
final Boson ext = Boson.extractor(expression, (in: BsValue) -> future.complete(in));

final Boson inj = Boson.injector(expression, (in: String) -> "newName");

final Boson fused = ext.fuse(inj);

final CompletableFuture<byte[]> finalFuture = fused.go(validatedByteArray);
finalFuture.join();
```

### Extracting a Json (Java)

```java
//  JsonObject represented by a String
final String json = "{\"value\": 27, \"onclick\": \"CreateNewDoc()\", \"bool\": false }";

//  Create a parser to read Strings and the argument being the json String
JsonParser parser = Json.createParser(new StringReader(json));

//  Knowing the JsonObject choose which value to be extracted with a key
//  and an ObjectExtractor, specifying the type of the ObjectExtractor
//  like in this case a StringExtractor.
JsonExtractor<String> ext = new ObjectExtractor( new StringExtractor("onclick") );

//  Apply the extractor to the parser to get the result
String result = ext.apply(parser).getResult().toString();
```

## Extracting Available Terms

### Table 1
Expression Terms | Output
---------------- | ------
all | List representing the Root Array
first | List with the first element of the Root Array
last | List with the last element of the Root Array

### Table 2
Expression Terms | Output
---------------- | ------
[2 to 5] | List with elements of an array, filtered by the limits established
[2 until 5] | Instead of 'to' its possible to use 'until'
[1 to end] | The ending limit can be 'end' instead of a number, it can be used with 'until' as well
[2] |   List with an element of an array

Expressions terms of both tables don't work together in the same expression.

#### Examples of expressions:
Expression  | Output
----------- | ------
key.[2 to 5] | Returns a list representing an array with elements filtered by the limits
key.[2 until 5].secondKey | Returns a list with elements filtered by the limits and secondKey
[2 until end] | Returns a list representing the Root array with elements filtered by the limits
[2].secondKey | Returns a list representing the Root array with element filtered by the limit and secondKey
key.first | Returns a list with the first occurrence of a key
[2] | Returns a list with an element of the Root array
all | Returns a list representing the Root Array

### Available Buffer Types
* Array of Bytes
* Java ByteBuffer
* Scala ArrayBuffer

### BsValue
BsValue is a trait representing any return type of the Boson. This type is extended by case classes that represent the
possible outputs of the Boson.
* BsNumber
* BsSeq
* BsBoolean
* BsException
