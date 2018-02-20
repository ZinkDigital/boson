package io.zink.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.zink.boson.bson.Boson
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsObject, BsValue}

class BosonValidate[T](expression: String, validateFunction: java.util.function.Consumer[T]) extends Boson{

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

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    CompletableFuture.supplyAsync(() => {
      val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
      callParse(boson,expression) match {
        case (res: BsValue) =>
          validateFunction.accept(res.getValue.asInstanceOf[T])
        case _ =>
          throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
      bsonByteEncoding
    })
  }

  override def go(bsonByteBufferEncoding: ByteBuffer) = ???

  override def fuse(boson: Boson) = ???
}
