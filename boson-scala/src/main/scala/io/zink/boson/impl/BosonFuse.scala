package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class BosonFuse(first: Boson, second: Boson) extends Boson {

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    // TODO Fuse functions should only allow extractors fuse for now, and should be done in parallel
    val future: Future[Array[Byte]] =
      Future {
        val firstFuture: Future[Array[Byte]] = first.go(bsonByteEncoding)
        Await.result(firstFuture, Duration.Inf)
        firstFuture.value.get match {
          case Success(value) =>
            val secondFuture = second.go(value)
            Await.result(secondFuture, Duration.Inf)
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
        //firstFuture.
        //Await.result(firstFuture, Duration.Inf)
        firstFuture.value.get match {
          case Success(value) =>
            val secondFuture = second.go(value)
            //Await.result(secondFuture, Duration.Inf)
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

  override def go(bsonByteEncoding: String): Future[String] = ???
}
