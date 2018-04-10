package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{DSLParser, Interpreter}
import scala.util.{Failure, Success}
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
class BosonExtractor[T](expression: String, extractFunction: T => Unit) extends Boson {

  /**
    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
    * statements used to instantiate the Interpreter.
    *
    * @param boson Instance of BosonImpl.
    * @param expression String parsed to build the extractors.
    */
private def callParse(boson: BosonImpl, expression: String): Unit = {
  val parser = new DSLParser(expression)
  try {
    parser.Parse() match {
      case Success(result) =>
        new Interpreter[T](boson, result, fExt = Option(extractFunction)).run()
      case Failure(exc) => throw exc
    }
  } catch {
    case e: RuntimeException => throw new Exception(e.getMessage)
  }
}

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
          callParse(boson, expression)
        //println("BosonExtractor GO")

        //val gen0 = new LabelledGeneric.Aux[T, L]

          bsonByteEncoding
      }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
          callParse(boson, expression)
          bsonByteBufferEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        val boson: BosonImpl = new BosonImpl(stringJson = Option(bsonByteEncoding))
        callParse(boson, expression)
        //println("BosonExtractor GO")

        //val gen0 = new LabelledGeneric.Aux[T, L]

        bsonByteEncoding
      }
    future
  }
}
