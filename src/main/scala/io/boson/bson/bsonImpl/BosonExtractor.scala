package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.boson.bson.{BosonJava, bsonValue}
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue.{BsSeq, BsValue}
import io.netty.util.ByteProcessor

class BosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends BosonJava {

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val bP: ByteProcessor = (value: Byte) => {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
      true
    }

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

    val boson: io.boson.bson.bsonImpl.Boson = new Boson(byteArray = Option(bsonByteEncoding))
    boson.getByteBuf.forEachByte(bP)

    //  call parse somewhere here
    //val resultParser: BsValue = callParse(boson, key, "first")
    val c =
      callParse(boson,"",expression) match {
      case (res: BsValue) =>
        println(s"++++++++++++++++++++++++ ${res.asInstanceOf[BsSeq].value.asInstanceOf[List[List[Any]]].head.head} +++++++++++++++++++++++++++")
        res.asInstanceOf[BsSeq].value.asInstanceOf[List[List[Any]]].head.head//.get.asInstanceOf[List[Array[Any]]].head.asInstanceOf[Map[String,Any]]
      case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
    }
    extractFunction.accept(c.asInstanceOf[T])
    new CompletableFuture[Array[Byte]]()
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(javaByteBuf = Option(bsonByteBufferEncoding))
    //  call parse somewhere here
    val c =
      boson.extract(boson.getByteBuf,expression,"SomethingForNow") match {
        case (res: T) => res
        case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
    extractFunction.accept(c)
    new CompletableFuture[ByteBuffer]()
  }

  override def fuse(boson: BosonJava): BosonJava = ??? //  return typpe is wrong

}
