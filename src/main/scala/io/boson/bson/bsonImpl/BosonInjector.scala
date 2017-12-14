package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Function

import io.boson.bson.BosonJava

class BosonInjector[T](expression: String, injectFunction: Function[T,T]) extends BosonJava{

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(byteArray = Option(bsonByteEncoding))
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(
        () => boson.extract(boson.getByteBuf,expression,"SomethingForNow").get.asInstanceOf[Array[Byte]]  //  asInstance works to test
      )
    //val result = injectFunction("fridges") // this func is passed to the injector to be applied latter
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: io.boson.bson.bsonImpl.Boson = new Boson(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: CompletableFuture[ByteBuffer] =
      CompletableFuture.supplyAsync(
        () => boson.extract(boson.getByteBuf,expression,"SomethingForNow").get.asInstanceOf[ByteBuffer] //  asInstance works to test
      )
    //val result = injectFunction("fridges") // this func is passed to the injector to be applied latter
    future
  }

  override def fuse(boson: BosonJava): BosonJava = ??? //  return typpe is wrong
}
