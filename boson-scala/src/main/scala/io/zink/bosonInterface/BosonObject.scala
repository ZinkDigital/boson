package io.zink.bosonInterface
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

object BosonObject extends Boson {
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding
    * @return
    */
  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = ???

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding
    * @return
    */
  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = ???

  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in teh case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param the BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  override def fuse(boson: Boson): Boson = ???
}
