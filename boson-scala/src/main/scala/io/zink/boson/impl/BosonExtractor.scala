package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath._
import shapeless.TypeCase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Boson instance which aims to handle extraction of primitive types or sequences of them.
  * These types doesn't compile with an implicit LabelledGeneric and for that reason a different class
  * is required.
  *
  * @param expression      String given by the User designated as BsonPath.
  * @param extractFunction Extract function given by the User to be applied after extraction.
  * @tparam T Type of Value to be extracted.
  */
class BosonExtractor[T](expression: String, extractFunction: T => Unit)(implicit tp: Option[TypeCase[T]]) extends Boson {

  private val boson: BosonImpl = new BosonImpl()

  private val interpreter: Interpreter[T] = new Interpreter[T](boson, expression, fExt = Some(extractFunction))

  private def runInterpreter(bsonEncoded: Either[Array[Byte], String]): Unit = {
    interpreter.run(bsonEncoded)
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] = Future {
      runInterpreter(Left(bsonByteEncoding))
      bsonByteEncoding
    }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        runInterpreter(Left(bsonByteBufferEncoding.array()))
        bsonByteBufferEncoding
      }
    future
  }

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        runInterpreter(Right(bsonByteEncoding))
        bsonByteEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)


}
