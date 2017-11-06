# boson
Streaming Data Access for BSON and JSON encoded documents


## Basic Usage

### Using ScalaInterface

```scala

//  Creates a new instance of the Interface
val sI: ScalaInterface = new ScalaInterface

//  The key is the identifier of the value wished to be extracted.
//  This key can be empty or filled depending on users choice.
//  Since the BsonArray doens't have the structure (key -> value) like
//  the BsonObject, the key can be empty assuming that the Root Object is a BsonArray,
//  otherwise the result will be an empty list.
val key: String = ""

//  The expression is a String containing the terms choosen by the user to extract something.
//  The available terms to use are shown further down in the README document.
val expression: String = "first"

//  NettyBson is an Object that encapsulates a certain type of buffer
//  and transformes it into a Netty buffer.
//  Available types are shown further down in the README document.
val netty: NettyBson = sI.createNettyBson(ba1.encode())

//  To extract from the Netty buffer method parse is called with the key and expression.
//  The result can have three types depending on the used terms in expression.
val result: Any = sI.parse(netty, key, expression)
```

### Using JavaInterface

```java

//  Boson has a JavaInterface aswell like the ScalaInterface implemented the same way
JavaInterface jI = new JavaInterface();
String key = "Something";
String expression = "all";
NettyBson netty = jI.createNettyBson(bsonEvent.encode().getBytes());
Object result = jI.parse(netty, key, expression);
```

## Extracting Available Terms

### Table 1
Expression Terms | Output
---------------- | ------
all | Returns a list of all occurrences of a key
first | Returns a list with the first occurrence of a key
last | Returns a list with the last occurrence of a key

### Table 2
Expression Terms | Output
---------------- | ------
[2 to 5] | Returns a list with all of BsonArrays occurences of a key, filtered by the limits established
[2 until 5] | Instead of 'to' its possible to use 'until'
[1 to end] | The ending limit can be 'end' instead of a number, it can be used with 'until' aswell

### Table 3
Expression Terms | Output
---------------- | ------
size | Returns the size of a value
isEmpty | Returns true/false depending on if its empty or not

The terms in table 3 can't be used alone, they can be used with terms in table 1, 2 or both.

### Table 4
Expression Terms | Output
---------------- | ------
in | Returns true/false depending on if the buffer contains a certain key
Nin | Returns true/false depending on if the buffer contains a certain key

The terms in table 4 can't be used with other tables terms.

#### A few examples of mixing terms
Expression Terms | Output
---------------- | ------
first [2 to 5] | Returns a list with the first element filtered by the limits
last [2 until 5] | Returns a list with the last element filtered by the limits
all [2 until end] | Returns a list with all elements filtered by the limits

It is possible to mix terms of tables 1 and 2.

Expression Terms | Output
---------------- | ------
all size | Returns a list with sizes of the containing values
first isEmpty | Returns true/false depending on if the first occurrence is empty or not

It is also possible to mix terms of tables 1 and 3.

Expression Terms | Output
---------------- | ------
[3 to 4] size | Returns a list with sizes of the filtered values
[0 until end] isEmpty | Returns true/false depending if the filtered list has content or not

It is possible aswell to join terms of tables 2 and 3.

Expression Terms | Output
---------------- | ------
all [2 until 5] size | Returns a list with sizes of all filtered values
first [1 to end] isEmpty | Returns true/false depending if the first element of the filtered list is empty or not

Lastly its possible to join terms of tables 1, 2 and 3.

### Available Buffer Types
* Array of Bytes
* Netty ByteBuf
* Java ByteBuffer
* Vertx Buffer
* Scala ArrayBuffer