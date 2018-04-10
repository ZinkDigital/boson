package io.zink.boson.impl

import java.nio.ByteBuffer
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.{BosonImpl, extractLabels}
import io.zink.boson.bson.bsonPath.{DSLParser, Interpreter}
import shapeless.{HList, LabelledGeneric, TypeCase}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Whenever this class is instantiated the extraction value type is either a Case Class or a sequence of Case Classes.
  *
  *
  * @param expression String parsed to build the extractors.
  * @param extractFunction  Function to apply to a Case Class
  * @param extractSeqFunction Function to apply to a sequence of Case Classes
  * @param gen  LabelledGeneric of the Case Class
  * @param extract  Trait that transforms HList into Case Classe Instances
  * @tparam T Case Class
  * @tparam R HList of the Case Class originated by the LabelledGeneric.Aux
  */
class BosonExtractorObj[T, R <: HList](expression: String, extractFunction: Option[T => Unit] = None, extractSeqFunction: Option[Seq[T] => Unit] = None)(implicit
                                                                                                                                                         gen: LabelledGeneric.Aux[T, R],
                                                                                                                                                         extract: extractLabels[R]) extends Boson {

  /**
    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
    * statements used to instantiate the Interpreter.
    *
    * @param boson  Instance of BosonImpl where extraction/injection is implemented.
    * @param expression String parsed to build the extractors.
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
private def callParse(boson: BosonImpl, expression: String): Any = {
  val parser = new DSLParser(expression)
  try {
    parser.Parse() match {
      case Success(result) =>
        new Interpreter[T](boson, result).run()
      case Failure(exc) => throw exc
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
    * @param bsonByteEncoding Array[Byte] encoded
    * @return Future with original Array[Byte].
    */
  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val boson: BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        val midRes: Any = callParse(boson, expression)
        val seqTuples = TypeCase[Seq[List[(String,Any)]]]
        val result: Seq[T] =
          midRes match {
            case seqTuples(vs) =>
              vs.par.map{ elem =>
                extractLabels.to[T].from[gen.Repr](elem)
              }.seq.collect { case v if v.nonEmpty => v.get }
            case _ => Seq.empty[T]
          }
        if(extractSeqFunction.isDefined)  extractSeqFunction.get(result) else result.foreach( elem => extractFunction.get(elem))
        bsonByteEncoding
      }
    future
  }

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        val boson: BosonImpl = new BosonImpl(stringJson = Option(bsonByteEncoding))
        val midRes: Any = callParse(boson, expression)
        val seqTuples = TypeCase[Seq[List[(String,Any)]]]
        val result: Seq[T] =
          midRes match {
            case seqTuples(vs) =>
              vs.par.map{ elem =>
                extractLabels.to[T].from[gen.Repr](elem)
              }.seq.collect { case v if v.nonEmpty => v.get }
            case _ => Seq.empty[T]
          }
        if(extractSeqFunction.isDefined)  extractSeqFunction.get(result) else result.foreach( elem => extractFunction.get(elem))
        bsonByteEncoding
      }
    future
  }

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding Array[Byte] encoded wrapped in a ByteBuffer.
    * @return Future with original ByteBuffer.
    */
  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
        val midRes: Any = callParse(boson, expression)
        val seqTuples = TypeCase[Seq[List[(String,Any)]]]
        val result: Seq[T] =
          midRes match {
            case seqTuples(vs) =>
              vs.par.map{ elem =>
                extractLabels.to[T].from[gen.Repr](elem)
              }.seq.collect { case v if v.nonEmpty => v.get }
            case _ => Seq.empty[T]
          }
        if(extractSeqFunction.isDefined)  extractSeqFunction.get(result) else result.foreach( elem => extractFunction.get(elem))
        bsonByteBufferEncoding
      }
    future
  }

  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in teh case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param boson BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)
}
