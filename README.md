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