package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.{BosonImpl, BosonImpl2}
import io.zink.boson.bson.bsonPath._
import shapeless.TypeCase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Boson instance which aims to handle extraction of primitive types or sequences of them.
  * These types doesn't compile with an implicit LabelledGeneric and for that reason a different class
  * is required.
  *
  * @param expression String given by the User designated as BsonPath.
  * @param extractFunction  Extract function given by the User to be applied after extraction.
  * @tparam T Type of Value to be extracted.
  */
class BosonExtractor[T](expression: String, extractFunction: T => Unit)(implicit tp: Option[TypeCase[T]]) extends Boson {

  private val boson: BosonImpl = new BosonImpl()

  private val interpreter: Interpreter[T] = new Interpreter[T](boson,expression, fExt = Option(extractFunction))

  //private val interpreter2 = new Interpreter[T](boson, expression, fExt = Option(extractFunction))

  /**
    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
    * statements used to instantiate the Interpreter.
    */
  // byteArr as argument to go to interpreter, Either[byte[],String]
  private def runInterpreter(bsonEncoded: Either[Array[Byte],String]): Unit = {
    interpreter.run(bsonEncoded)
  }

//  private def runInterpreter2(bsonEncoded: Seq[ByteBuf]): Any = {
//    val results =
//    bsonEncoded.par.map{ elem =>
//      interpreter2.runExtractors(Left(elem), interpreter2.keyList,interpreter2.limitList)
//    }.seq.flatten.map{ case e: ByteBuf => e.array()}
//    extractFunction(results.asInstanceOf[T])
//  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] = Future {
      runInterpreter(Left(bsonByteEncoding))
      bsonByteEncoding
    }
    future
  }

//
//  def go2(encodedStructures: Seq[ByteBuf]): Future[Seq[ByteBuf]] = {
//    val future: Future[Seq[ByteBuf]] =
//      Future {
//        runInterpreter2(encodedStructures)
//        encodedStructures
//
//      }
//    future
//  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        runInterpreter(Left(bsonByteBufferEncoding.array()))
          bsonByteBufferEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        //val boson: BosonImpl = new BosonImpl(stringJson = Option(bsonByteEncoding))
        runInterpreter(Right(bsonByteEncoding))
        //println("BosonExtractor GO")

        //val gen0 = new LabelledGeneric.Aux[T, L]

        bsonByteEncoding
      }
    future
  }
}
