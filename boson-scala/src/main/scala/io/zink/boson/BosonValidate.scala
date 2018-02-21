package io.zink.boson

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.zink.bosonInterface.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsObject, BsValue}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BosonValidate[T](expression: String, validateFunction: Function[T, Unit]) extends Boson{

  private def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) => BsObject.toBson(msg)
        case parser.Failure(msg, _) => BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException => BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    Future{
      val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
      callParse(boson,expression) match {
        case (res: BsValue) =>
          validateFunction(res.getValue.asInstanceOf[T])
        case _ =>
          throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
      bsonByteEncoding
    }
  }

  override def go(bsonByteBufferEncoding: ByteBuffer):Future[ByteBuffer] = {
    Future{
      val boson:BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
      callParse(boson,expression) match {
        case (res: BsValue) =>
          validateFunction(res.getValue.asInstanceOf[T])
        case _ =>
          throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
      bsonByteBufferEncoding
    }
  }

  override def fuse(boson: Boson) = new BosonFuse(this,boson)
}
