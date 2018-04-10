package io.zink.boson.impl

import java.nio.ByteBuffer

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{DSLParser, Interpreter}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class BosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Boson {

  val anon: T => T = injectFunction

  private def parseInj(netty: BosonImpl): Unit = {
    val parser = new DSLParser(expression)
    try {
      parser.Parse() match {
        case Success(result) =>
          new Interpreter[T](netty, result, fInj = Option(anon)).run()
        case Failure(exc) => throw exc
      }
    } catch {
      case e: RuntimeException => throw new Exception(e.getMessage)
    }
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
    val future: Future[Array[Byte]] =
      Future{
      val r: Array[Byte] = parseInj(boson).asInstanceOf[Array[Byte]]
      r
    }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: Future[ByteBuffer] =
    Future{
      val r: Array[Byte] = parseInj(boson).asInstanceOf[Array[Byte]]
      val b: ByteBuf = Unpooled.copiedBuffer(r)
      b.nioBuffer()
    }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this,boson)

  override def go(bsonByteEncoding: String): Future[String] = ???
}
