//package io.zink.joson.impl
//
//import java.io.ByteArrayOutputStream
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.databind.module.SimpleModule
//import de.undercouch.bson4jackson.BsonFactory
//import io.vertx.core.json.{JsonArray, JsonObject}
//import io.zink.boson.bson.bsonImpl.BosonImpl
//import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
//import io.zink.joson.Joson
//import io.zink.joson.Joson.{JsonArraySerializer, JsonObjectSerializer}
//import shapeless.Typeable
//
//import scala.concurrent.ExecutionContext.Implicits.global
//import scala.concurrent.Future
//
//
//
///**
//  * Created by Ricardo Martins on 19/01/2018.
//  */
//
//
//
//class JosonExtractor[T](expression: String, extractFunction: T => Unit) extends Joson {
//  /**
//    * Apply this Joson to the String that arrives and at some point in the future complete
//    * the future with the resulting String. In the case of an Extractor this will result in
//    * the immutable String being returned unmodified.
//    *
//    * @param the Json string.
//    * @return
//    */
//  private def callParse(boson: BosonImpl, expression: String): Unit = {
//    val parser = new TinyLanguage
//    try {
//      parser.parseAll(parser.program, expression) match {
//        case parser.Success(r, _) =>
//          //new Interpreter[T](boson, r.asInstanceOf[Program], fExt = Option(extractFunction)).run()
//        case parser.Error(msg, _) =>
//          throw new Exception(msg)
//          //BsObject.toBson(msg)
//        case parser.Failure(msg, _) =>
//          throw new Exception(msg)
//          //BsObject.toBson(msg)
//      }
//    } catch {
//      case e: RuntimeException =>
//        throw new Exception(e.getMessage)
//        //BsObject.toBson(e.getMessage)
//    }
//  }
//
//  override def go(jsonStr: String):Future[String] = {
//    val a: JsonObject = new JsonObject(jsonStr)
//
//    val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
//    val os = new ByteArrayOutputStream
//    val module = new SimpleModule
//    module.addSerializer(classOf[JsonObject],new JsonObjectSerializer)
//    module.addSerializer(classOf[JsonArray], new JsonArraySerializer)
//    mapper.registerModule(module)
//
//    mapper.writeValue(os, a)
//
//    val bsonByteEncoding: Array[Byte] = os.toByteArray
//    os.flush()
//
//    val future: Future[String] =
//      Future{
//        val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
//          callParse(boson, expression)
//          jsonStr
////        match {
////          case (res: BsValue) =>
////            extractFunction(res.asInstanceOf[T])
////          case _ => throw new RuntimeException("JosonExtractor -> go() default case!!!")
////        }
//      }
//    future
//  }
//
//  /**
//    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
//    * joson that is the parameter in teh case of update/read conflicts.
//    * the immutable String being returned unmodified.
//    *
//    * @param the Joson to fuse to.
//    * @return the fused Joson
//    */
//  override def fuse(joson: Joson): Joson = new JosonFuse(this, joson)
//}
