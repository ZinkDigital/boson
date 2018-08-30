package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bosonPath.Interpreter
import shapeless.TypeCase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BosonInjector[T](expression: String, injectFunction: T => T)(implicit tp: Option[TypeCase[T]]) extends Boson {

  private val interpreter: Interpreter[T] = new Interpreter[T](expression, fInj = Some(injectFunction))(tp, None)

  /**
    * Method that delegates the injection process to Interpreter passing to it the data structure to be used (either a byte array or a String)
    *
    * @param bsonEncoded - Data structure to be used in the injection process
    */
  private def runInterpreter(bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] =
    interpreter.run(bsonEncoded).asInstanceOf[Either[Array[Byte], String]]


  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding Array[Byte] encoded
    * @return Future with original or a modified Array[Byte].
    */
  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    Future {
      runInterpreter(Left(bsonByteEncoding)) match {
        case Left(byteArr) => byteArr
      }
    }
  }

  /**
    * Apply this BosonImpl to the String and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param bsonByteEncoding bson encoded into a String
    * @return Future with original or a modified String.
    */
  override def go(bsonByteEncoding: String): Future[String] = {
    Future {
      runInterpreter(Right(bsonByteEncoding)) match {
        case Right(jsonString) => jsonString
      }
    }
  }

//  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)
}
