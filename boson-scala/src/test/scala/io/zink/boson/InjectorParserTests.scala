//package io.zink.boson
//
//
//
//import java.util.concurrent.CompletableFuture
//
//import bsonLib.{BsonArray, BsonObject}
//import io.netty.buffer.{ByteBuf, Unpooled}
//import io.netty.util.ResourceLeakDetector
//import io.zink.boson.bson.bsonImpl.BosonImpl
//import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
//import io.zink.boson.bson.bsonValue._
//import io.zink.boson.bson.bsonValue
//import mapper.Mapper
//import org.junit.runner.RunWith
//
//import scala.concurrent.duration.Duration
//import scala.concurrent.{Await, Future}
////import org.scalameter.Events.Failure
//import org.junit.Assert.assertTrue
//import org.scalatest.FunSuite
//import org.scalatest.junit.JUnitRunner
//
//import scala.collection.immutable.List
//import scala.util.{Failure, Success, Try}
///**
//  * Created by Ricardo Martins on 15/12/2017.
//  */
///*object Mapper  {
//  val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
//  val module = new SimpleModule
//  module.addSerializer(classOf[util.Map[String, _]], BsonObjectSerializer)
//  /*module.addSerializer(classOf[util.List[_]], BsonArraySerializer)*/
//  mapper.registerModule(module)
//
//
//
//  def encode(obj: Any): Array[Byte] = {
//    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
//    Try(mapper.writeValue(os, obj)) match {
//      case Success(_) =>
//        os.flush()
//        os.toByteArray
//      case Failure(e) =>
//        throw new RuntimeException(e)
//    }
//  }
//
//
//
//
//}*/
//
//
//
//@RunWith(classOf[JUnitRunner])
//class InjectorParserTests extends FunSuite {
//  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
//  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5L).put("fanVelocity", 20.5).put("doorOpen", false).putNull("null")
//  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 6L).put("fanVelocity", 20.6).put("doorOpen", false)
//  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3L).put("fanVelocity", 20.5).put("doorOpen", true)
//  val obj4: BsonObject = new BsonObject().put("vhjxfjgcv", obj1).put("fanVelocity", 0.0).put("doorOpen", true)
//  val obj5: BsonObject = new BsonObject().put("fanVelocity", 20.5).put("doorOpen", true)
//  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//  val arr1: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(obj5)
//  val arr3: BsonArray = new BsonArray().add(obj4).add(arr)
//
//  val arr4: BsonArray = new BsonArray().add(4).add(5).add(3)
//  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)
//  val bsonEvent1: BsonObject = new BsonObject().put("fridgeReadings", arr3)
//  val bsonEventArray: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(5)
//  val bsonEventArray1: BsonArray = new BsonArray().add(1).add(2).add(3).add(4).add(true)
//  val arr2: BsonArray = new BsonArray().add(1).add(2).add(3)
//  val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//  val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//  val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//
//  /*val bP: ByteProcessor = (value: Byte) => {
//    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
//    true
//  }*/
//
//  def callParse(boson: BosonImpl, expression: String): BsValue = {
//    val parser = new TinyLanguage
//    try {
//      parser.parseAll(parser.program, expression) match {
//        case parser.Success(r, _) =>
//          val interpreter = new Interpreter(boson, r.asInstanceOf[Program], fExt = Option((in: BsValue) => {}))
//          interpreter.run()
//        case parser.Error(_, _) => bsonValue.BsObject.toBson("Error parsing!")
//        case parser.Failure(_, _) => bsonValue.BsObject.toBson("Failure parsing!")
//      }
//    } catch {
//      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
//    }
//  }
//
//  def parseInj[T](netty: BosonImpl,f: T => T, expression: String):bsonValue.BsValue = {
//    val parser = new TinyLanguage
//    try{
//      parser.parseAll(parser.program, expression) match {
//        case parser.Success(r,_) =>
//          new Interpreter(netty, r.asInstanceOf[Program], fInj = Option(f)).run()
//        case parser.Error(msg, _) => println("Error")
//          bsonValue.BsObject.toBson(msg)
//
//        case parser.Failure(msg, _) => println("Failure")
//          bsonValue.BsObject.toBson(msg)
//
//      }
//    }catch {
//      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
//    }
//  }
//
//  test("All") {
//    val key: String = "fridgeTemp"
//    val expression: String = "fridgeReadings[0 to end].fridgeTemp"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEvent.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Long]*4L, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//      case nb: BsBoson => callParse(BsBoson.unapply(nb).get,expression)
//      case _ => List()
//    }
//    println( resultParser.asInstanceOf[BsSeq])
//    assert(BsSeq(Vector(20, 24, 12)) === resultParser.asInstanceOf[BsSeq])
//  }
//  test("[0 to end] BsonArray as Root") {
//    val expression: String = "[0 to end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x:Int) => x*4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    //println( resultParser.asInstanceOf[BsSeq])
//    assert(BsSeq(Vector(4, 8, 12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
//  }
//  test("[0 until end] BsonArray as Root") {
//    val key: String = ""
//    val expression: String = "[0 until end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    println( resultParser.asInstanceOf[BsSeq])
//    assert(BsSeq(Vector(4, 8, 12, 16)) === resultParser.asInstanceOf[BsSeq])
//  }
//  test("[2 until 4] BsonArray as Root") {
//    val key: String = ""
//    val expression: String = "[2 until 4]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson =>callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    println( resultParser.asInstanceOf[BsSeq])
//    assert(BsSeq(Vector(12, 16)) === resultParser.asInstanceOf[BsSeq])
//  }
//  test("[2 to 4] BsonArray as Root") {
//    val key: String = ""
//    val expression: String =  "[2 to 4]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x:Any) => x.asInstanceOf[Int]*4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    println( resultParser.asInstanceOf[BsSeq])
//    assert(BsSeq(Vector(12, 16, 20)) === resultParser.asInstanceOf[BsSeq])
//  }
//  test("[2 to 4] BsonArray as Root Test Type Consistency") {
//    val key: String = ""
//    val expression: String =  "[2 to 4]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray1.encodeToBarray()))
//    val resultBoson: BsValue = parseInj(boson, (x:Int) => x*4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException =>
//        println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, ".all")
//      case _ => List()
//    }
//    println( resultParser)
//    assert("Type Error. Cannot Cast boolean inside the Injector Function." === resultParser.asInstanceOf[BsException].getValue)
//  }
//  test("All [0 to end] BsonArray as Root") {
//    val key: String = ""
//    val expression: String = "[0 to end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(4, 8, 12, 16, 20) === resultParser.asInstanceOf[BsSeq].getValue)
//  }
//  test("All [0 until end] BsonArray as Root") {
//    val expression: String = "[0 until end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(4, 8, 12, 16) === resultParser.asInstanceOf[BsSeq].getValue)
//    }
//  test("[1 until end] BsonArray as Root") {
//    val expression: String = "[1 until end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(8, 12, 16) === resultParser.asInstanceOf[BsSeq].getValue)
//  }
//  test("[1 to end] BsonArray as Root") {
//    val expression: String = "[1 to end]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(8, 12, 16, 20) === resultParser.asInstanceOf[BsSeq].getValue)
//  }
//  test("[1 to 2] BsonArray as Root") {
//    val expression: String = "[1 to 2]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(8, 12) === resultParser.asInstanceOf[BsSeq].getValue)
//  }
//  test("[1 until 2] BsonArray as Root") {
//    val expression: String = "[1 until 2]"
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonEventArray.encode().getBytes))
//    val resultBoson: BsValue = parseInj(boson, (x: Any) => x.asInstanceOf[Int] * 4, expression)
//    val resultParser: Any = resultBoson match {
//      case ex: BsException => println(ex.getValue)
//        ex
//      case nb: BsBoson => callParse(nb.getValue, expression)
//      case _ => List()
//    }
//    assert(Vector(8) === resultParser.asInstanceOf[BsSeq].getValue)
//  }
//
//  test("fridgeReadings.[1 until 2] Int => Int"){
//
//    val arr2: BsonArray = new BsonArray().add(1).add(2).add(3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    println(bsonArrayEvent)
//
//    val expression: String = "[0 to end].fridgeReadings[0 to end]"
//      val expression1: String = "[0 to end].fridgeReadings[1 until 2]"
//    //lazy val res: (BosonImpl, BosonImpl) = boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(), key,(x:Int) => x*4 , "1", "2")
//
//    lazy val resultBoson: BsValue = parseInj(boson,(x:Int) => x*4, expression1 )
//    //resultBoson.asInstanceOf[BsBoson].getValue.getByteBuf.forEachByte(bP)
//
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) => v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> callParse(nb, expression)
//      case _ =>
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    println(result2)
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    assert(Vector(1, 8, 3, 1, 8, 3, 1, 8, 3) === resultParser)
//  }
//  test("fridgeReadings.[1 until 2] BsonObject => BsonObject"){
//
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28).put("country", "Spain")
//    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28).put("country", "Germany")
//    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    val expression1: String = "[0 to end].fridgeReadings[1 until 2]"
//
//    lazy val resultBoson: BsValue = parseInj(boson1,(x: Array[Byte]) => {
//      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
//      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
//      val newM: Map[String, Any] = m.+(("nick", "Ritchy"))
//      val res: ByteBuf = Mapper.encode(newM)
//      if(res.hasArray)
//        res.array()
//      else {
//        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
//        val array: Array[Byte] = buf.array()
//        buf.release()
//        array
//      }
//    }, expression1 )
//
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) =>
//        v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=>callParse(nb, expression1)
//      case _ =>
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//
//    val _obj2: BsonObject = new BsonObject().put("age", 28).put("country", "Spain").put("name", "Tiago").put("nick", "Ritchy")
//    val expected: Vector[Array[Byte]] = Vector(_obj2.encodeToBarray(),_obj2.encodeToBarray(),_obj2.encodeToBarray())
//    val result = resultParser.asInstanceOf[Vector[Array[Any]]]
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//  test("fridge*Readings BsonArray => BsonArray"){
//    ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28).put("country", "Spain")
//    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28).put("country", "Germany")
//    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28).put("country", "France")
//    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    val expression1: String = "[0 to end].fridge*Readings"
//    lazy val resultBoson: BsValue = parseInj(boson1,(x: Array[Byte]) => {
//      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
//      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
//      val newL: List[Any] = l:+ Mapper.convert(obj4)
//      val res: ByteBuf = Mapper.encode(newL)
//      if(res.hasArray)
//        res.array()
//      else {
//        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
//        val array: Array[Byte] = buf.array()
//        buf.release()
//        array
//      }
//    }, expression1 )
//
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) =>
//        v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsBoson(nb)=>callParse(nb, "[0 to end].fridge*Readings.[3 to 3]")
//      case _ => /* Never Gets Here */ println("Never Gets Here")
//    }
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    val _obj4: BsonObject = new BsonObject().put("age", 28).put("country", "France").put("name", "Pedro")
//    val expected: Vector[Array[Byte]] = Vector(_obj4.encodeToBarray(),_obj4.encodeToBarray(),_obj4.encodeToBarray())
//    val result = resultParser.asInstanceOf[Vector[Array[Any]]]
//    assert(expected.size === result.size)
//    assertTrue(expected.zip(result).forall(b => b._1.sameElements(b._2)))
//  }
//  test("age.all Double => Double"){
//
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
//    val obj3: BsonObject = new BsonObject().put("name", "Joao").put("age", 28.0).put("country", "Germany")
//    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
//    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    val key: String = "age"
//    val expression: String = "[0 to end].fridgeReadings[0 to end].age"
//    val expression1: String = "[0 to end].fridgeReadings[0 to end].age"
//
//    lazy val resultBoson: BsValue = parseInj(boson1,(x:Double) => x*2.0, expression1 )
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) =>
//        v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsBoson(nb)=>callParse(nb, expression)
//      case _ => /* Never Gets Here */ println("Never Gets Here")
//    }
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    assert(Vector(56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0, 56.0)=== resultParser)
//  }
//
//  test("name.all String => String"){
//
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
//    val obj3: BsonObject = new BsonObject().put("name", "João").put("age", 28.0).put("country", "Germany")
//    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
//    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    val key: String = "name"
//    val expression: String = "[0 to end].fridgeReadings[0 to end].name"
//    val expression1: String = "[0 to end].fridgeReadings[0 to end].name"
//
//    lazy val resultBoson: BsValue = parseInj(boson1,(x:String) => x.concat("MINE"), expression1 )
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) =>
//        v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsBoson(nb)=>callParse(nb, expression)
//      case _ => /* Never Gets Here */ println("Never Gets Here")
//    }
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    assert( Vector("RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE")
//      === resultParser)
//  }
//
//  test("name String => String"){
//
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("age", 28.0).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("name", "Tiago").put("age", 28.0).put("country", "Spain")
//    val obj3: BsonObject = new BsonObject().put("name", "João").put("age", 28.0).put("country", "Germany")
//    val obj4: BsonObject = new BsonObject().put("name", "Pedro").put("age", 28.0).put("country", "France")
//    val arr2: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
//    val bE: BsonObject = new BsonObject().put("fridgeReadings", arr2)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bE).add(bE).add(bE)
//    val boson1: BosonImpl = new BosonImpl(byteArray = Option(bsonArrayEvent.encode().getBytes))
//
//    val key: String = "name"
//    val expression: String = "[0 to end].fridgeReadings[0 to end].name"
//    val expression1: String = "[0 to end].fridgeReadings[0 to end].name"
//
//    lazy val resultBoson: BsValue = parseInj(boson1,(x:String) => x.concat("MINE"), expression1 )
//    lazy val result1: BsValue = Try(resultBoson) match {
//      case Success(v) =>
//        v
//      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
//    }
//    val result2: Any = result1 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsBoson(nb)=>callParse(nb, expression)
//      case _ => /* Never Gets Here */ println("Never Gets Here")
//    }
//    val resultParser: Any = result2 match {
//      case BsException(ex) =>
//        println(ex)
//        ex
//      case BsSeq(e) => e//.replaceAll("\\p{C}", "")) //TODO problema com strings
//      case BsBoson(nb)=> nb
////      case BsNumber(n) => n
////      case BsBoolean(b) => b
//    }
//    assert( Vector("RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE", "RicardoMINE", "TiagoMINE", "JoãoMINE")
//
//      === resultParser)
//  }
//
//  test("key[@key1]"){
//    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
//    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
//    //val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false).put("string", "the").put("bson", bAux)
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
//    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)
//
//    //val newFridgeSerialCode: String = " what?"
//    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
//    val expression = "array[@damnnn]"
//    //val boson: Boson = Boson.injector(expression, (in: Map[String, Any]) => in.+(("WHAT!!!", 10)))
//    val boson: Boson = Boson.injector(expression, (x: Array[Byte]) => {
//      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
//      val m: Map[String,Any] = Mapper.decodeBsonObject(b.getByteBuf)
//      val newM: Map[String, Any] = m.+(("WHAT!!!", 10))
//      val res: ByteBuf = Mapper.encode(newM)
//      if(res.hasArray)
//        res.array()
//      else {
//        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
//        val array: Array[Byte] = buf.array()
//        buf.release()
//        array
//      }
//    })
//
//    val result: Future[Array[Byte]] = boson.go(validBsonArray)
//
//    // apply an extractor to get the new serial code as above.
//
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    val resultValue: Array[Byte] = Await.result(result, Duration.Inf)
//    boson1.go(resultValue)
//    val finalResult: BsValue = future.join()
//
//    val expected: Vector[Array[Byte]] = Vector(bAux.put("WHAT!!!", 10).encodeToBarray(),bAux.put("WHAT!!!", 10).encodeToBarray(),bAux.put("WHAT!!!", 10).encodeToBarray())
//    val res = finalResult.getValue.asInstanceOf[Vector[Array[Any]]]
//    assert(expected.size === res.size)
//    assertTrue(expected.zip(res).forall(b => b._1.sameElements(b._2)))
//  }
//
//  test(".*"){
//    val bAux: BsonObject = new BsonObject().put("damnnn", "DAMMN")
//    val bAux1: BsonObject = new BsonObject().put("creep", "DAMMN")
//    val bsonArrayEvent: BsonArray = new BsonArray().add(bAux).add(bAux).add(bAux).add(bAux1)
//    val bsonObjectRoot: BsonObject = new BsonObject().put("array", bsonArrayEvent)
//    val validBsonArray: Array[Byte] = bsonObjectRoot.encodeToBarray
//    val expression = ".*"
//    //val boson: Boson = Boson.injector(expression, (in: List[Any]) => in.:+(Mapper.convertBsonObject(new BsonObject().put("WHAT!!!", 10))))
//    val boson: Boson = Boson.injector(expression,     (x: Array[Byte]) => {
//      val b: BosonImpl = new BosonImpl(byteArray = Option(x))
//      val l: List[Any] = Mapper.decodeBsonArray(b.getByteBuf)
//      val newL: List[Any] = l.:+(Mapper.convertBsonObject(new BsonObject().put("WHAT!!!", 10)))
//      val res: ByteBuf = Mapper.encode(newL)
//      if(res.hasArray)
//        res.array()
//      else {
//        val buf: ByteBuf = Unpooled.buffer(res.capacity()).writeBytes(res)
//        val array: Array[Byte] = buf.array()
//        buf.release()
//        array
//      }
//    })
//    val result: Future[Array[Byte]] = boson.go(validBsonArray)
//    // apply an extractor to get the new serial code as above.
//   // val resultValue: Array[Byte] = result.join()
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson1: Boson = Boson.extractor(expression, (in: BsValue) => future.complete(in))
//    val resultValue: Array[Byte] = Await.result(result, Duration.Inf)
//    boson1.go(resultValue)
//    val finalResult: BsValue = future.join()
//
//    val expected: Vector[Array[Byte]] = Vector(bsonArrayEvent.add(new BsonObject().put("WHAT!!!", 10)).encodeToBarray())
//    val res = finalResult.getValue.asInstanceOf[Vector[Array[Any]]]
//    assert(expected.size === res.size)
//    assertTrue(expected.zip(res).forall(b => b._1.sameElements(b._2)))
//  }
//  /*test("test"){
//    import scala.collection.JavaConverters._
//    val obj1: BsonObject = new BsonObject().put("name", "Ricardo").put("sage", 28).put("age", 28).put("country", "Portugal")
//    val obj2: BsonObject = new BsonObject().put("country", "Portugal").put("name", "Ricardo").put("sage", 28).put("age", 28)
//    val obj1Enc: ByteBuf = obj1.encode().getByteBuf
//
//   val boson: BosonImpl = new BosonImpl(byteArray = Option(obj1Enc.duplicate().array()))
//
//   val m: Map[String, Any] = boson.decodeBsonObject(obj1Enc)
//
//   /* val enc: ByteBuf = boson.encode(m)
//
//    println(obj1Enc.capacity() + "    " + enc.capacity())
//    val ziped: Array[(Byte, Byte)] = enc.array().zip(obj1Enc.array())
//
//    ziped.foreach(c => println(s"enc=${c._1.toChar}   Obj1Enc=${c._2.toChar}" ))*/
//
//    val map1: Any = Mapper.convert(obj1)
//    println(map1)
//    val map2: Any = Mapper.convert(obj2)
//    println(map2)
//    println(m)
//  assert(map2 == m)
//  }*/
//
///*
//  test("Ex store"){
//     val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
//     val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
//     val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
//     val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
//     val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
//     val sEditions3 = new BsonArray().add(edition3)
//     val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
//     val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
//     val sEditions2 = new BsonArray().add(edition2).add(5L).add(true)
//     val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5).put("SpecialEditions", sEditions2)
//     val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
//     val sEditions1 = new BsonArray().add(edition1)
//     val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
//     val books = new BsonArray().add(title1).add(title2).add(title3)
//     val store = new BsonObject().put("Book", books).put("Hat", hats)
//     val bson = new BsonObject().put("Store", store).put("tables#", 5L).put("buy?", true)
//
//    val bsonEvent: BsonObject = bson
//    val validatedByteArr: Array[Byte] = bsonEvent.encodeToBarray()
//
//    val expression = "Book.*..SpecialEditions"
//    val future: CompletableFuture[BsValue] = new CompletableFuture[BsValue]()
//    val boson: Boson = Boson.extractor(expression, (out: BsValue) => future.complete(out))
//    boson.go(validatedByteArr)
//
//    val res = future.join()
//    println(res)
//  }*/
//}
