package io.zink.boson

import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture

import io.zink.bosonInterface.Boson
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class BosonFuse(first: Boson, second: Boson) extends Boson {

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        val firstFuture: Future[Array[Byte]] = first.go(bsonByteEncoding)
        firstFuture.value.get match {
          case Success(value) =>
            val secondFuture = second.go(value)
            secondFuture.value.get match {
              case Success(secondValue) =>
                secondValue
              case Failure(e) =>
                bsonByteEncoding
            }
          case Failure(e) =>
            bsonByteEncoding
        }
      }
        future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        val firstFuture: Future[ByteBuffer] = first.go(bsonByteBufferEncoding)
        firstFuture.value.get match {
          case Success(value) =>
            val secondFuture = second.go(value)
            secondFuture.value.get match {
              case Success(secondValue) =>
                secondValue
              case Failure(e) =>
                bsonByteBufferEncoding
            }
          case Failure(e) =>
            bsonByteBufferEncoding
        }
      }
    future
  }

  override def fuse(boson: Boson) = new BosonFuse(this,boson)
}
