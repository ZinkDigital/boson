package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsObject, BsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BosonExtractor[T](expression: String, extractFunction: Function[T, Unit]) extends Boson {

  private def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program], fExt =  Option(extractFunction)).run()
        case parser.Error(msg, _) => BsObject.toBson(msg)
        case parser.Failure(msg, _) => BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException =>
        BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        callParse(boson, expression) match {
          case (res: BsValue) =>
            extractFunction(res.asInstanceOf[T])
          case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
        }
        bsonByteEncoding
      }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
          callParse(boson,expression) match {
            case (res: BsValue) =>
              extractFunction(res.asInstanceOf[T])
            case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
          }
        bsonByteBufferEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this,boson)

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