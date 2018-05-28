package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.Interpreter
import shapeless.TypeCase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BosonInjector[T](expression: String, injectFunction: T => T)(implicit tp: Option[TypeCase[T]]) extends Boson {

  //  val anon: T => T = injectFunction

  //  private val boson: BosonImpl = new BosonImpl()

  private val boson: BosonImpl = new BosonImpl()
  private val interpreter: Interpreter[T] = new Interpreter[T](boson, expression, fInj = Some(injectFunction))

  //  val boson: BosonImpl = new BosonImpl()
  //
  //  val parsedStatements: ProgStatement = new DSLParser(expression).Parse() match {
  //    case Success(result) => result
  //    case Failure(excp) => throw excp
  //  }
  //
  //
  //  /**
  //    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
  //    * statements used to instantiate the Interpreter.
  //    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
  //    */
  //  // byteArr as argument to go to interpreter, Either[byte[],String]

  /**
    * This is new Code!!!
    *
    * @param bsonEncoded - ???
    */
  private def runInterpreter(bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] = {
    interpreter.run(bsonEncoded).asInstanceOf[Either[Array[Byte], String]]
  }

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
