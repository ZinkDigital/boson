package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.Interpreter
import shapeless.TypeCase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BosonInjector[T](expression: String, injectFunction: T => T)(implicit tp: Option[TypeCase[T]]) extends Boson {

  private val interpreter: Interpreter[T] = new Interpreter[T](expression, fInj = Some(injectFunction))

  /**
    * Methon that delegates the injection process to Interperter passing to it the data structure to be used (either a byte array or a String)
    *
    * @param bsonEncoded - Data structure to be used in the injection process
    */
  private def runInterpreter(bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] =
    interpreter.run(bsonEncoded).asInstanceOf[Either[Array[Byte], String]]


  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] = Future {
      runInterpreter(Left(bsonByteEncoding)) match {
        case Left(byteArr) => byteArr
      }
    }
    future
  }

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        runInterpreter(Right(bsonByteEncoding)) match {
          case Right(jsonString) => jsonString
        }
      }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = { //TODO isn't this to be forgotten ?
    //val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: Future[ByteBuffer] =
    Future {
      //      val r: Array[Byte] = parseInj(boson).asInstanceOf[Array[Byte]]
      //      val b: ByteBuf = Unpooled.copiedBuffer(r)
      //      b.nioBuffer()
      bsonByteBufferEncoding
    }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)
}
