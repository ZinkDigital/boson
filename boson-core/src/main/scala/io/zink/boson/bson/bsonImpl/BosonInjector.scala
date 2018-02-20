package io.zink.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Function

import io.zink.boson.bson.Boson
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsBoson, BsException, BsObject, BsValue}

import scala.compat.java8.FunctionConverters._


class BosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Boson {

  val anon: T => T = injectFunction.asScala

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String):BsValue = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program], Option(injectFunction)).run()
        case parser.Error(msg, _) =>
          BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(() =>{
      val r: Array[Byte] = parseInj(boson, anon, expression) match {
        case ex: BsException => println(ex.getValue)
          bsonByteEncoding
        case nb: BsBoson =>
          nb.getValue.getByteBuf.array()
        case _ =>
          bsonByteEncoding
      }
      r
    })
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: CompletableFuture[ByteBuffer] =
    CompletableFuture.supplyAsync(() =>{
      val r:ByteBuffer = parseInj(boson, anon, expression) match {
        case ex: BsException => println(ex.getValue)
          bsonByteBufferEncoding
        case nb: BsBoson => nb.getValue.getByteBuf.nioBuffer()
        case _ =>
          bsonByteBufferEncoding
      }
      r
    })
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this,boson)
}
