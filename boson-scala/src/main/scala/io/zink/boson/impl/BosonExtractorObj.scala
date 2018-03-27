package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.{BosonImpl, extractLabels}
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import shapeless.{HList, LabelledGeneric}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BosonExtractorObj[T, R <: HList](expression: String, extractFunction: T => Unit)(implicit
                                                                                       gen: LabelledGeneric.Aux[T, R],
                                                                                       extract: extractLabels[R]) extends Boson {

  private def callParse(boson: BosonImpl, expression: String): Any = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter[T](boson, r.asInstanceOf[Program], fExt = Option(extractFunction)).run()
        case parser.Error(msg, _) => throw new Exception(msg)
        case parser.Failure(msg, _) => throw new Exception(msg)
      }
    } catch {
      case e: RuntimeException => throw new Exception(e.getMessage)
    }
  }
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding
    * @return
    */
  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        val midRes: Any = callParse(boson, expression)
        println("BosonExtractorObj GO")
        println(midRes)

        val result: Seq[T] =
          midRes match {  //List[(String, Any)]
            case x if x.isInstanceOf[Seq[Any]] && x.asInstanceOf[Seq[Any]].nonEmpty =>
              val l = x.asInstanceOf[Seq[List[Any]]]
                l.head.nonEmpty match {
                case true if l.head.head.isInstanceOf[(String,Any)] =>
                  midRes.asInstanceOf[Seq[List[(String, Any)]]].map{ elem =>
                    extractLabels.to[T].from[gen.Repr](elem)
                  }.collect { case v if v.nonEmpty => v.get }
              }
          }
        result.foreach( elem => extractFunction(elem))
        bsonByteEncoding
      }
    future
  }

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding
    * @return
    */
  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
        callParse(boson, expression)
        bsonByteBufferEncoding
      }
    future
  }

  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in teh case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param the BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)
}
