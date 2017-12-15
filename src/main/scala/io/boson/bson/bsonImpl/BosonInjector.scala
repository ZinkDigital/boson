package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.function.Function
import io.boson.bson

class BosonInjector[T](expression: String, injectFunction: Function[T,T]) extends bson.Boson{

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(
        () => boson.extract(boson.getByteBuf,expression,"SomethingForNow").get.asInstanceOf[Array[Byte]] //  asInstance works to test, must be injector
          //boson.modify(Option(boson),"",injectFunction) map { b =>
          //b.getByteBuf.array()
        //} getOrElse { boson.getByteBuf.array() }
      )
    //val result = injectFunction("fridges") // this func is passed to the injector to be applied latter
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: CompletableFuture[ByteBuffer] =
      CompletableFuture.supplyAsync(
        () => boson.extract(boson.getByteBuf,expression,"SomethingForNow").get.asInstanceOf[ByteBuffer] //  asInstance works to test, must be injector
      )
    //val result = injectFunction("fridges") // this func is passed to the injector to be applied latter
    future
  }

  override def fuse(boson: bson.Boson): bson.Boson = ??? //  return typpe is wrong
}
