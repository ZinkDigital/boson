package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.boson.bson
import io.boson.bson.bsonValue
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.{BsSeq, BsValue}

class BosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends bson.Boson {

  def callParse(boson: Boson, key: String, expression: String): io.boson.bson.bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, key, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) =>  bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>  bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e:RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {

    val boson: io.boson.bson.bsonImpl.Boson = new Boson(byteArray = Option(bsonByteEncoding))

    //  call parse somewhere here
    //val resultParser: BsValue = callParse(boson, key, "first")
    val c =
      callParse(boson,"",expression) match {
      case (res: BsValue) =>
        //println(s"++++++++++++++++++++++ ${res.asInstanceOf[BsSeq].value}")
        //res.asInstanceOf[BsSeq].value.asInstanceOf[List[List[Any]]].head.head
        res
      case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
    }
    extractFunction.accept(c.asInstanceOf[T])
    new CompletableFuture[Array[Byte]]()
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(javaByteBuf = Option(bsonByteBufferEncoding))
    //  call parse somewhere here
    val c =
      callParse(boson,"",expression) match {
        case (res: BsValue) => res
        case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
    extractFunction.accept(c.asInstanceOf[T])
    new CompletableFuture[ByteBuffer]()
  }

  override def fuse(boson: bson.Boson): bson.Boson = ??? //  return typpe is wrong

}
