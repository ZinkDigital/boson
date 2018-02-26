package io.zink.boson.impl

import java.nio.ByteBuffer

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
//import io.zink.boson.bson.bsonValue.{BsBoson, BsException, BsObject, BsValue}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class BosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Boson {

  val anon: T => T = injectFunction

  def parseInj[K](netty: BosonImpl, injectFunction: K => K , expression: String): Array[Byte] = {
    val parser = new TinyLanguage
    try{
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r,_) =>
          new Interpreter(netty, r.asInstanceOf[Program],fInj = Option(injectFunction)).run()
        case parser.Error(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
        case parser.Failure(msg, _) =>
          throw new Exception(msg)
          //BsObject.toBson(msg)
      }
    }catch {
      case e: RuntimeException => throw new Exception(e.getMessage)
        //BsObject.toBson(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
    val future: Future[Array[Byte]] =
      Future{
      val r: Array[Byte] = parseInj(boson, anon, expression)
//      match {
//        case ex: BsException => println(ex.getValue)
//          bsonByteEncoding
//        case nb: BsBoson =>
//          nb.getValue.getByteBuf.array()
//        case _ =>
//          bsonByteEncoding
//      }
      r
    }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: Future[ByteBuffer] =
    Future{
      val r: Array[Byte] = parseInj(boson, anon, expression)
      val b: ByteBuf = Unpooled.copiedBuffer(r)
      b.nioBuffer()
//      match {
//        case ex: BsException => println(ex.getValue)
//          bsonByteBufferEncoding
//        case nb: BsBoson => nb.getValue.getByteBuf.nioBuffer()
//        case _ =>
//          bsonByteBufferEncoding
//      }
//      r
    }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this,boson)
}
