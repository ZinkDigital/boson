package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Function

import io.boson.bson
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.netty.util.ByteProcessor
import bson.bsonValue
import io.boson.bson.bsonValue.{BsBoson, BsException, BsSeq}

import scala.compat.java8.FunctionConverters._


class BosonInjector[T](expression: String, injectFunction: Function[T, T]) extends bson.Boson {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }

  val anon: T => T = injectFunction.asScala

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String):bsonValue.BsValue = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(injectFunction)).run()
        case parser.Error(msg, _) =>
          bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          bsonValue.BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
//    println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
//    boson.getByteBuf.forEachByte(bP)
//    println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(() =>{

      val r: Array[Byte] = parseInj(boson, anon, expression) match {
        case ex: BsException => println(ex.getValue)
          bsonByteEncoding
        case nb: BsBoson => nb.getValue.getByteBuf.array()
        case x =>

          bsonByteEncoding
      }

      r
    })
//    boson.getByteBuf.forEachByte(bP)
//    println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
    //future.get()
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val future: CompletableFuture[ByteBuffer] = new CompletableFuture[ByteBuffer]()
    /*val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: CompletableFuture[ByteBuffer] =
      CompletableFuture.supplyAsync(() =>{

       parseInj(boson, anon, expression) match {
        case ex: BsException => println(ex.getValue)
          bsonByteBufferEncoding
        case nb: BsBoson => nb.getValue.getByteBuf.nioBuffer()
        case _ =>
          println("Error")
          bsonByteBufferEncoding
      }




        })
    //    boson.getByteBuf.forEachByte(bP)
    //    println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")

    future*/
    future
  }

  override def fuse(boson: bson.Boson): bson.Boson = ??? //  return typpe is wrong
}
