# boson
Streaming Data Access for BSON and JSON encoded documents


## Basic Usage

### Extracting a Bson (Scala)

```scala
//  Create a BsonObject/BsonArray
val globalObj: BsonObject = new BsonObject().put("Salary", 1000).put("AnualSalary", 12000L)
    .put("Unemployed", false).put("Residence", "Lisboa").putNull("Experience")

//  Instantiate a NettyBson that receives a buffer containing the Bson encoded
val nettyBson: NettyBson = new NettyBson(vertxBuff = Option(globalObj.encode()))

//  Call extract method on nettyBson, the arguments being the netty byteBuf of NettyBson object,
//  the key of the value to extract, and an expression from Table 1(shwon further down in this document).
val result: Option[Any] = nettyBson.extract(nettyBson.getByteBuf, "AnualSalary", "first")
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

### Using ScalaInterface

```scala

//  Creates a new instance of the Interface
val sI: ScalaInterface = new ScalaInterface

//  The key is the identifier of the value wished to be extracted.
//  This key can be empty or filled depending on users choice.
//  Since the BsonArray doesn't have the structure (key -> value) like
//  the BsonObject, the key can be empty assuming that the Root Object is a BsonArray,
//  otherwise the result will be an empty list.
val key: String = ""

//  The expression is a String containing the terms chosen by the user to extract something.
//  The available terms to use are shown further down in the README document.
val expression: String = "first"

//  NettyBson is an Object that encapsulates a certain type of buffer
//  and transforms it into a Netty buffer.
//  Available types are shown further down in the README document.
val netty: NettyBson = sI.createNettyBson(bsonObject.encode())

//  To extract from the Netty buffer, method parse is called with the key and expression.
//  The result can be one of three types depending on the used terms in expression.
val result: Any = sI.parse(netty, key, expression)
```

### Using JavaInterface

```java

//  Boson has a JavaInterface as well like the ScalaInterface implemented the same way
JavaInterface jI = new JavaInterface();
String key = "Something";
String expression = "all";
NettyBson netty = jI.createNettyBson(bsonObject.encode().getBytes());
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
[2 to 5] | Returns a list with all of BsonArrays occurrences of a key, filtered by the limits established
[2 until 5] | Instead of 'to' its possible to use 'until'
[1 to end] | The ending limit can be 'end' instead of a number, it can be used with 'until' as well

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

#### A few examples of mixing terms:
Expression Terms | Output
---------------- | ------
first [2 to 5] | Returns a list with the first element filtered by the limits
last [2 until 5] | Returns a list with the last element filtered by the limits
all [2 until end] | Returns a list with all elements filtered by the limits

It is possible to mix terms of tables 1 and 2.

Expression Terms | Output
---------------- | ------
all size | Returns the size of all elements filtered
first isEmpty | Returns true/false depending on if the first occurrence is empty or not

It is also possible to mix terms of tables 1 and 3.

Expression Terms | Output
---------------- | ------
[3 to 4] size | Returns a list with sizes of the filtered values
[0 until end] isEmpty | Returns true/false depending on if the filtered list has content or not

It is possible as well to join terms of tables 2 and 3.

Expression Terms | Output
---------------- | ------
all [2 until 5] size | Returns a list with sizes of all filtered values
first [1 to end] isEmpty | Returns true/false depending if the filtered list is empty or not

Lastly its possible to join terms of tables 1, 2 and 3.

### Available Buffer Types
* Array of Bytes
* Netty ByteBuf
* Java ByteBuffer
* Vertx Buffer
* Scala ArrayBuffer