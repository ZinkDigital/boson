
# Boson  
  
Streaming Data Access for BSON and JSON encoded documents  
  
[![Build Status](https://api.travis-ci.org/ZinkDigital/boson.svg)](https://travis-ci.org/ZinkDigital/boson)

Bosonscala

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.zink/bosonscala/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.zink/bosonscala)

Bosonjava

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.zink/bosonjava/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.zink/bosonjava)

# Table of Contents
  
- [Introduction to Boson](#id-introductionToBoson)  
- [Scala QuickStart Guide](#id-quickStartGuideScala)  
   * [Boson](#id-BosonScala)  
      * [Extractor](#id-bosonExtractionScala)  
      * [Injector](#id-bosonInjectionScala)  
      * [Fuse](#id-bosonFuseScala)
- [Java QuickStart Guide](#id-quickStartGuideJava)  
   * [Boson](#id-BosonJava)  
      * [Extractor](#id-extractionJava)  
      * [Injector](#id-injectionJava)  
      * [Fuse](#id-bosonFuseJava)  
- [Documentation](#documentation)  
   * [BsonPath](#bsonpath)  
      * [Operators](#operators)  
      * [Comparison with JsonPath](#comparison-with-jsonpath)
   * [Java Profiler](#java-profiler)
  
<div id='id-introductionToBoson'/>  
  
## Introduction to Boson

Boson is a library written in Scala with both Scala and Java APIs and it is built to extract and inject data to and from various ‘wire encodings’. It currently implements two input codecs, JSON documents encoded in the form of UTF8 strings, and BSON documents encoded in the form of binary arrays.

Through the use of Shapeless, Boson allows the use of user created classes as data extraction/injection types.

In the following points we show how to use Boson in both Scala ans Java in a QuickStart Guide.

    
<div id='id-quickStartGuideScala'/>  
  
## Scala QuickStart Guide  
  
Boson is available through the Central Maven Repository.  
To include Boson in your projects you need to had the dependencies listed bellow.

For SBT users, add the following to your build.sbt: 
```scala
//For the Scala API
libraryDependencies += “io.zink” % “bosonscala” % “0.5.0”
```
```scala
//For the Java API
libraryDependencies += “io.zink” % “bosonjava” % “0.5.0"
```

For Maven users, add the following to your pom.xml:
```xml
<!--For the Scala API-->
<dependency>
    <groupId>io.zink</groupId>
    <artifactId>bosonsala</artifactId>
    <version>0.5.0</version>
</dependency>
```
```xml
<!--For the Java API-->
<dependency>
    <groupId>io.zink</groupId>
    <artifactId>bosonjava</artifactId>
    <version>0.5.0</version>
</dependency>
```
  
<div id='id-BosonScala'/>  
  
### Boson  

A "Boson" is an object created when constructing an extractor/injector that includes a Bson encoded as an Array[Byte] or a Json encoded as a String in a Netty buffer and processes it according to a given expression, traversing the buffer only once.

<div id='id-bosonExtractionScala'/>  
  
#### Extraction  
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON and an Higher-Order Function. The Extractor instance is built only once and can be reused multiple times to extract from different encoded BSON.  
  
```scala  
//Encode Bson:  
val validBson : Array[Byte] = bsonEvent.encode.getBytes  
  
//BsonPath expression:  
val expression: String = "fridge[1].fanVelocity"  
  
// Want to put the result onto a Stream.  
val valueStream : ValueStream = ValueStream()  
  
//Simple Extractor:  
val boson: Boson = Boson.extractor(expression, (in: Long) => {  
  valueStream.add(in);  
})  
  
//Trigger extraction with encoded Bson:  
boson.go(validBson)  
  
// Function will be called as a result of calling 'go'  
```  
<div id='id-bosonInjectionScala'/>  
  
#### Injection  
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples and syntax), an encoded BSON and an Higher-Order Function. The returned result is a Future[Array[Byte]]. The Injector instance is built only once and can be reused to inject different encoded BSON.  

```scala  
//Encode Bson:  
val validBsonArray: Array[Byte] = bsonEvent.encode.getBytes 
  
//BsonPath expression:  
val expression: String = "Store..name"  
  
//Simple Injector:  
val boson: Boson = Boson.injector(expression, (in: String) => "newName")  
  
//Trigger injection with encoded Bson:  
val result: Future[Array[Byte]] = boson.go(validBsonArray)

// Function will be called as a result of calling 'go' 
```  
<div id='id-bosonFuseScala'>  
  
### Fuse  
Fusion requires  a [Boson Extractor](#id-bosonExtractionScala) and a [Boson Injector](#id-bosonInjectionScala) or two Boson of the same type. The order in 
which fuse is applied is left to the discretion of the user. This fusion is executed sequentially at the moment.  

```scala  
//First step is to construct both Boson.injector and Boson.extractor by providing the necessary arguments.  
val validatedByteArray: Array[Byte] = bsonEvent.encode.getBytes 
  
val expression = "name"  
  
val ext: Boson = Boson.extractor(expression, (in: BsValue) => {  
  // Use 'in' value, this is the value extracted.  
})  
  
val inj: Boson = Boson.injector(expression, (in: String) => "newName")  
  
//Then call fuse() on injector or extractor, it returns a new BosonObject.  
val fused: Boson = ext.fuse(inj)  
  
//Finally call go() providing the byte array or a ByteBuffer on the new Boson object.  
val result: Future[Array[Byte]] = fused.go(validatedByteArray) 
```  
<div id='id-quickStartGuideJava'/>  
  
## Java QuickStart Guide  
  
For Maven users, please add the following dependency in your pom.xml:  

```xml  
<dependency>  
    <groupId>io.zink</groupId>  
    <artifactId>boson</artifactId>  
    <version>0.5</version>  
</dependency>  
```  
<div id='id-BosonJava'/>  
  
### Boson  

A "Boson" is an object created when constructing an extractor/injector that includes a 
Bson encoded as a byte[] or a Json encoded as a String in a Netty buffer and processes 
it according to a given expression, traversing the buffer only once.

<div id='id-bosonExtractionJava'/>  
  
#### Extraction  
Extraction requires a "BsonPath" expression (see [Operators](#operators) table for 
examples and syntax), an encoded BSON and a lambda expression. The Extractor instance 
is built only once and can be reused multiple times to extract from different encoded 
BSON.  
  
```java  
//Encode Bson:  
byte[] validatedByteArray = bsonEvent.encode().getBytes();  
  
//BsonPath expression:  
String expression = "Store..SpecialEditions[@Extra]";  
  
// Want to put the result onto a Stream.  
ValueStream valueStream = ValueStream()  
  
//Simple Extractor:  
Boson boson = Boson.extractor(expression, obj-> {  
   valueStream.add(in);
});  
  
//Trigger extraction with encoded Bson:  
boson.go(validatedByteArray);  
  
// Function will be called as a result of calling 'go'  
```  
<div id='id-injectionJava'/>  
  
#### Injection  
Injection requires a "BsonPath" expression (see [Operators](#operators) table for examples
and syntax), an encoded BSON and a lambda expression. The returned result is a
CompletableFuture<byte[]>. The Injector instance is built only once and can be reused to 
inject different encoded BSON.  

```java  
//Encode Bson:  
byte[] validatedByteArray = bsonEvent.encode().getBytes();  
  
//BsonPath expression:  
String expression = "..Store.[2 until 4]";  
  
//Simple Injector:  
Boson boson = Boson.injector(expression,  (Map<String, Object> in) -> {  
   in.put("WHAT", 10);  
   return in;  
});  
  
//Trigger injection with encoded Bson:  
byte[] result = boson.go(validatedByteArray).join();  
```  

### Fuse  
Fusion requires  a [Boson Extractor](#id-bosonExtractionScala) and a 
[Boson Injector](#id-bosonInjectionScala) or two Boson of the same type. The order in 
which fuse is applied is left to the discretion of the user. This fusion is executed 
sequentially at the moment.  

```java  
//First step is to construct both Boson.injector and Boson.extractor by providing the necessary arguments.  
final byte[] validatedByteArray  = bsonEvent.encode().array();  
  
final String expression = "name";  
  
final Boson ext = Boson.extractor(expression, (in: BsValue) -> {  
  // Use 'in' value, this is the value extracted.  
});  
  
final Boson inj = Boson.injector(expression, (in: String) -> "newName");  
  
//Then call fuse() on injector or extractor, it returns a new BosonObject.  
final Boson fused = ext.fuse(inj);  
  
//Finally call go() providing the byte array or a ByteBuffer on the new Boson object.  
final byte[] result = fused.go(validatedByteArray).join();  
```  
  
# Documentation  
## BsonPath  
  

BsonPath expressions targets a Bson structure with the same logic as JsonPath expressions target JSON structure and XPath targeted a XML document. Unlike JsonPath there is no reference of a "root member object", instead if you want to specify a path starting from the root, the expression must begin with a dot (`.key`).

  
BsonPath expressions use the dot-notation: `key1.key2[0].key3`.  
  
Expressions whose path doesn't necessarily start from the root can be expressed in two ways:  
* No dot - ` key`  
* Two dots - `..key`  
  
### Operators  
  
Operator | Description  
---------|----------  
`.` | Child.  
`..` | Deep scan. Available anywhere a name is required.  
`@` | Current node.  
`[<number> ((to,until) <number>)]` | Array index or indexes.  
`[@<key>]` | Filter expression.  
`[first | end | all]` | Array index through condition.
`*` | Wildcard. Available anywhere a name is required.  
  
### Comparison with JsonPath  
Given the json  
```json  
{  
   "Store":{  
      "Book":[  
           {  
               "Title":"Java",  
               "Price":15.5,  
               "SpecialEditions":[  
                   {  
                       "Title":"JavaMachine",  
                       "Price":39  
                   }  
                ]  
           },  
           {  
               "Title":"Scala",  
               "Pri":21.5,  
               "SpecialEditions":[  
                   {  
                       "Title":"ScalaMachine",  
                       "Price":40  
                   }  
                ]  
           },  
           {  
               "Title":"C++",  
               "Price":12.6,  
               "SpecialEditions":[  
                   {  
                       "Title":"C++Machine",  
                       "Price":38  
                   }  
                ]  
           }  
           ],  
       "Hat":[  
            {  
                "Price":48,  
                "Color":"Red"  
            },  
            {  
                "Price":35,  
                "Color":"White"  
            },  
            {  
                "Price":38,  
                "Color":"Blue"  
            }  
        ]  
    }  
}  
```  
BsonPath | JsonPath  
---------|---------  
`.Store` | `$.Store`  
`.Store.Book[@Price]` | `$.Store.Book[?(@.Price)]`  
`Book[@Price]..Title` | `$..Book[?(@.Price)]..Title`  
`Book[1]` | `$..Book[1]`  
`Book[0 to end]..Price` | `$..Book[:]..Price`  
`Book[0 to end].*..Title` | `$..Book[:].*..Title`  
`Book[0 until end]..Price` | `$..Book[:-1]..Price`
`Book[0 until end].*..Title` | `$..Book[:-1].*..Title`
`.*` | `$.*`  
`Book.*.[0 to end]` | `$..Book.*.[:]`  
`.Store..Book[1 until end]..SpecialEditions[@Price]` | `$.Store..Book[1:-1]..SpecialEditions[?(@.Price)]`  
`Bo*k`, `*ok` or `Bo*`  | `Non existent.`  
`*ok[@Pri*]..SpecialEd*.Price` | `Non existent.`  
  
**Note: JsonPath doesn't support the *halfkey* (`B*ok`).**


## Java Profiler

Boson is a library that relies on high performance BSON data manipulation, and so performance monitoring is of paramount importance. The chosen java profiler is [YourKit](https://www.yourkit.com/) for being a supporter of open source projects and one of the most innovative and intelligent tools for profiling [Java](https://www.yourkit.com/java/profiler/) & [.NET](https://www.yourkit.com/.net/profiler/) applications as well .

![](https://www.yourkit.com/images/yklogo.png)

