package io.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.boson.bson
import io.boson.bson.Boson

class BosonFuse(first: bson.Boson, second: bson.Boson) extends bson.Boson {

  override def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]] = {
    val future: CompletableFuture[Array[Byte]] =
      CompletableFuture.supplyAsync(() => {
            val firstFuture: CompletableFuture[Array[Byte]] = first.go(bsonByteEncoding)
            second.go(firstFuture.join()).join()
        })
        future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer] = {
    val future: CompletableFuture[ByteBuffer] =
      CompletableFuture.supplyAsync(() => {
        val firstFuture: CompletableFuture[ByteBuffer] = first.go(bsonByteBufferEncoding)
        second.go(firstFuture.join()).join()
      })
    future
  }

  override def fuse(boson: Boson) = new BosonFuse(this,boson)
}
