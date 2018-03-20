package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.{BosonImpl, extractLabels}
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import shapeless.{Generic, HList, LabelledGeneric, Typeable}
//import io.zink.boson.bson.bsonValue.{BsObject, BsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

class BosonExtractor[T](expression: String, extractFunction: T => Unit) extends Boson {
  //val genericObj = GenericObj[T]
//  def mytype[U](implicit m: scala.reflect.Manifest[U]) = m
//  def myTypeWithoutExtraParam = mytype[T]
//  val newInstance: Any = myTypeWithoutExtraParam
  //Generic[newInstance]
  //val tag = typeTag[T].tpe
  //Generic[tag]
  //println(s"TupeTag -> $tag")
  //FromList
  //println(s"Type of T: $newInstance")
  //FromList.to[newInstance].from(List(("","")))


  private def callParse(boson: BosonImpl, expression: String): Any = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter[T](boson, r.asInstanceOf[Program], fExt = Option(extractFunction)).run()
        case parser.Error(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          throw new Exception(msg)
        //BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException => throw new Exception(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
          callParse(boson, expression)
          bsonByteEncoding
      }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
          callParse(boson, expression)
          bsonByteBufferEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)

  //override def extractor[T](expression: String, extractFunction: Function[T, Unit]): Unit = ???
}

//val f: Future[Array[Byte]] = Future {
//val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
//callParse(boson,expression) match {
//case (res: BsValue) =>
//extractFunction.accept(res.asInstanceOf[T])
//case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
//}
//bsonByteEncoding
//}(ExecutionContext.global)
////future
//f