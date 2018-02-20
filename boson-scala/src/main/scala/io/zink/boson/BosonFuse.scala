package io.zink.boson

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.zink.bosonInterface.Boson

class BosonFuse(first: Boson, second: Boson) extends Boson {

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
