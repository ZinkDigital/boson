package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
//import io.zink.boson.bson.bsonValue.{BsObject, BsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BosonValidate[T](expression: String, validateFunction: Function[T, Unit]) extends Boson{

  private def callParse(boson: BosonImpl, expression: String): Unit = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program], fExt = Option(validateFunction)).run()
        case parser.Error(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException =>
        throw new Exception(e.getMessage)
      //BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Array[Byte] = {
    //Future{
      val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        callParse(boson,expression)
        bsonByteEncoding
//      match {
//        case (res: BsValue) =>
//          validateFunction(res.getValue.asInstanceOf[T])
//        case _ =>
//          throw new RuntimeException("BosonExtractor -> go() default case!!!")
//      }
    //}
  }

  override def go(bsonByteBufferEncoding: ByteBuffer):Future[ByteBuffer] = {
    Future{
      val boson:BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
        callParse(boson,expression)
        bsonByteBufferEncoding
//      match {
//        case (res: BsValue) =>
//          validateFunction(res.getValue.asInstanceOf[T])
//        case _ =>
//          throw new RuntimeException("BosonExtractor -> go() default case!!!")
//      }
    }
  }

  override def fuse(boson: Boson) = ???
  //new BosonFuse(this,boson)
}
