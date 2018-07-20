package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.{BosonImpl, extractLabels}
import io.zink.boson.bson.bsonPath._
import shapeless.{HList, LabelledGeneric, TypeCase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Whenever this class is instantiated the extraction value type is either a Case Class or a sequence of Case Classes.
  *
  * @param expression         String parsed to build the extractors.
  * @param extractFunction    Function to apply to a Case Class
  * @param extractSeqFunction Function to apply to a sequence of Case Classes
  * @param gen                LabelledGeneric of the Case Class
  * @param extract            Trait that transforms HList into Case Classe Instances
  * @tparam T Case Class
  * @tparam R HList of the Case Class originated by the LabelledGeneric.Aux
  */
class BosonExtractorObj[T, R <: HList](expression: String,
                                       extractFunction: Option[T => Unit] = None,
                                       extractSeqFunction: Option[Seq[T] => Unit] = None
                                      )(implicit
                                        gen: LabelledGeneric.Aux[T, R],
                                        extract: extractLabels[R],
                                        tp: Option[TypeCase[T]]) extends Boson {

  private val interpreter: Interpreter[T] = new Interpreter[T](expression, fExt = extractFunction)(tp, None)

  /**
    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
    * statements used to instantiate the Interpreter.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  // byteArr as argument to go to interpreter, Either[byte[],String]
  private def runInterpreter(bsonEncoded: Either[Array[Byte], String]): Any = {
    interpreter.run(bsonEncoded)
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
    val future: Future[Array[Byte]] = Future {
      val midRes: Any = runInterpreter(Left(bsonByteEncoding))
      println(midRes)
      val seqTuples = TypeCase[Seq[List[(String, Any)]]]
      val result: Seq[T] =
        midRes match {
          case seqTuples(vs) =>
            vs.par.map { elem =>
              extractLabels.to[T].from[gen.Repr](elem)
            }.seq.collect { case v if v.nonEmpty => v.get }
          case _ => Seq.empty[T]
        }
      if (extractSeqFunction.isDefined) extractSeqFunction.get(result) else result.foreach(elem => extractFunction.get(elem))
      bsonByteEncoding
    }
    future
  }

  override def go(bsonByteEncoding: String): Future[String] = {
    val future: Future[String] =
      Future {
        //val boson: BosonImpl = new BosonImpl(stringJson = Option(bsonByteEncoding))
        val midRes: Any = runInterpreter(Right(bsonByteEncoding))
        val seqTuples = TypeCase[Seq[List[(String, Any)]]]
        val result: Seq[T] =
          midRes match {
            case seqTuples(vs) =>
              vs.par.map { elem =>
                extractLabels.to[T].from[gen.Repr](elem)
              }.seq.collect { case v if v.nonEmpty => v.get }
            case _ => Seq.empty[T]
          }
        if (extractSeqFunction.isDefined) extractSeqFunction.get(result) else result.foreach(elem => extractFunction.get(elem))
        bsonByteEncoding
      }
    future
  }


  //  /**
  //    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
  //    * boson that is the parameter in teh case of update/read conflicts.
  //    * the immutable byte array being returned unmodified.
  //    *
  //    * @param boson BosonImpl to fuse to.
  //    * @return the fused BosonImpl
  //    */
  //  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)
}
