package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.boson.bson.BosonJava

class BosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends BosonJava {

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(byteArray = Option(bsonByteEncoding))
    //  call parse somewhere here
    val c =
      boson.extract(boson.getByteBuf,expression,"SomethingForNow") match {
      case (res: T) => res
      case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
    }
    extractFunction.accept(c)
    new CompletableFuture[Array[Byte]]()
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(javaByteBuf = Option(bsonByteBufferEncoding))
    //  call parse somewhere here
    val c =
      boson.extract(boson.getByteBuf,expression,"SomethingForNow") match {
        case (res: T) => res
        case _ => throw new RuntimeException("BosonExtractor -> go() default case!!!")
      }
    extractFunction.accept(c)
    new CompletableFuture[ByteBuffer]()
  }

  override def fuse(boson: BosonJava): BosonJava = ??? //  return typpe is wrong

}
