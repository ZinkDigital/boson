package io.zink.bosonInterface

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

import io.zink.boson.{BosonExtractor, BosonInjector, BosonValidate}


trait Boson{
  def validate[T](expression: String, validateFunction: Consumer[T]) = new BosonValidate[T](expression, validateFunction)
  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */
  def extractor[T](expression: String, extractFunction: Consumer[T]) = new BosonExtractor[T](expression, extractFunction)

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
  def injector[T](expression: String, injectFunction: java.util.function.Function[T, T]) = new BosonInjector[T](expression, injectFunction)

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding
    * @return
    */
  def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]]

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding
    * @return
    */
  def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer]


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
