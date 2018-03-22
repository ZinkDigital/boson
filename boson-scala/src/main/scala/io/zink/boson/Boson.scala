package io.zink.boson

import java.nio.ByteBuffer
import java.time.Instant

import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl.{BosonExtractor, BosonExtractorObj, BosonInjector, BosonValidate}
import shapeless.{HList, LabelledGeneric}

import scala.concurrent.Future


object Boson {

  trait extractor[A] {
    def extract(expression: String, extractFunction: A => Unit): Boson
  }

  object extractor {

    def apply[A](expression: String, extractFunction: A => Unit)(implicit ext: extractor[A]): Boson = ext.extract(expression, extractFunction)

    implicit def caseClass[A, L <: HList](implicit
                                          f: LabelledGeneric.Aux[A, L],
                                          ext: extractLabels[L]): extractor[A] =
      new extractor[A] {
        def extract(expression: String, extractFunction: A => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractFunction = Option(extractFunction))
      }

    implicit def seqCaseClass[A, L <: HList](implicit
                                             f: LabelledGeneric.Aux[A, L],
                                             ext: extractLabels[L]): extractor[Seq[A]] = {
      new extractor[Seq[A]] {
        def extract(expression: String, extractFunction: Seq[A] => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractSeqFunction = Option(extractFunction))(f, ext)

      }
    }

    implicit def seqLiterals[A, Coll[X]]: extractor[Coll[A]] =
      new extractor[Coll[A]] {
        def extract(expression: String, extractFunction: Coll[A] => Unit): Boson =
          new BosonExtractor[Coll[A]](expression, extractFunction)
      }

    implicit val arrByte: extractor[Array[Byte]] =
      new extractor[Array[Byte]] {
        override def extract(expression: String, extractFunction: Array[Byte] => Unit): Boson =
          new BosonExtractor[Array[Byte]](expression,extractFunction)
      }

    implicit val double: extractor[Double] =
      new extractor[Double] {
        override def extract(expression: String, extractFunction: Double => Unit): Boson =
          new BosonExtractor[Double](expression, extractFunction)
      }

    implicit val float: extractor[Float] =
      new extractor[Float] {
        override def extract(expression: String, extractFunction: Float => Unit): Boson =
          new BosonExtractor[Float](expression, extractFunction)
      }

    implicit val instant: extractor[Instant] =
      new extractor[Instant] {
        override def extract(expression: String, extractFunction: Instant => Unit): Boson =
          new BosonExtractor[Instant](expression, extractFunction)
      }

    implicit val long: extractor[Long] =
      new extractor[Long] {
        override def extract(expression: String, extractFunction: Long => Unit): Boson =
          new BosonExtractor[Long](expression, extractFunction)
      }

    implicit val int: extractor[Int] =
      new extractor[Int] {
        override def extract(expression: String, extractFunction: Int => Unit): Boson =
          new BosonExtractor[Int](expression, extractFunction)
      }

    implicit val string: extractor[String] =
      new extractor[String] {
        override def extract(expression: String, extractFunction: String => Unit): Boson =
          new BosonExtractor[String](expression, extractFunction)
      }

    implicit val boolean: extractor[Boolean] =
      new extractor[Boolean] {
        override def extract(expression: String, extractFunction: Boolean => Unit): Boson =
          new BosonExtractor[Boolean](expression, extractFunction)
      }
  }

  def validate[T, R <: HList](expression: String, validateFunction: T => Unit)(implicit
                                                                               f: LabelledGeneric.Aux[T, R],
                                                                               extract: extractLabels[R]) = new BosonValidate[T, R](expression, validateFunction)

  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
  def injector[T, R <: HList](expression: String, injectFunction: T => T)(implicit
                                                                          f: LabelledGeneric.Aux[T, R],
                                                                          extract: extractLabels[R]) =
    new BosonInjector[T, R](expression, injectFunction)

}


trait Boson {
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding
    * @return
    */
  def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]]

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding
    * @return
    */
  def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer]


  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in teh case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param the BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  def fuse(boson: Boson): Boson

}
