package io.zink.boson

import java.nio.ByteBuffer

import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl.{BosonExtractor, BosonInjector, BosonValidate}
import shapeless.{HList, LabelledGeneric, Lazy, the}
//import java.util.function.Consumer

import scala.concurrent.Future
import scala.reflect.runtime.universe._


object Boson {


//  def validate[T, R <: HList](expression: String, validateFunction: T => Unit)(implicit
//                                                                   f: LabelledGeneric.Aux[T, R],
//                                                                   extract: extractLabels[R]) = new BosonValidate[T,R](expression, validateFunction)
  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */


  def extractor[T, R <: HList](expression: String, extractFunction: T => Unit)(implicit
                                                                               f: LabelledGeneric.Aux[T, R],
                                                                               extract: extractLabels[R]) = {
    new BosonExtractor[T,R](expression, extractFunction)
  }

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
//  def injector[T, R <: HList](expression: String, injectFunction: T => T)(implicit
//                                                                          f: LabelledGeneric.Aux[T, R],
//                                                                          extract: extractLabels[R]) = new BosonInjector[T,R](expression, injectFunction)
}

trait Boson {


//  def apply[A](implicit f: Lazy[Generic[A]]): Generic[A]
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
