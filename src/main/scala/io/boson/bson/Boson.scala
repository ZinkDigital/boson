package io.boson.bson

import java.nio.ByteBuffer
import java.util.function.Consumer
import java.util.concurrent.CompletableFuture

import io.boson.bson

trait Boson {

  class BosonEx(_expression: String, _extractFunction: CompletableFuture[_]) {
    val expression: String = this._expression
    val extractFunction: CompletableFuture[_] = this._extractFunction

  }

  object BosonEx {
    def extractor(expression: String, extractFunction: CompletableFuture[_]): io.boson.bson.bsonImpl.Boson = {  //  instantiates a BosonExtractor
      val bsonValidatedEncoded: Array[Byte] = Array(1,2,3)
      new io.boson.bson.bsonImpl.Boson(byteArray = Option(bsonValidatedEncoded))
    }
  }

  class BosonIj(_expression: String, _injectFunction: (Any) => Any) {
    val expression: String = this._expression
    val injectFunction: (Any) => Any = this._injectFunction
  }

  object BosonIj {
    def injector(_expression: String, _injectFunction: (Any) => Any): io.boson.bson.bsonImpl.Boson = {
      val bsonValidatedEncoded: Array[Byte] = Array(1,2,3)
      new io.boson.bson.bsonImpl.Boson(byteArray = Option(bsonValidatedEncoded))
    }
  }

  def go(bsonByteEncoding: Array[Byte]): CompletableFuture[Array[Byte]]

  def go(bsonByteBufferEncoding: ByteBuffer): CompletableFuture[ByteBuffer]

  def fuse(boson: io.boson.bson.bsonImpl.Boson): io.boson.bson.bsonImpl.Boson
}



object Driver extends Boson {

  import java.util.concurrent.CompletableFuture

  val result = new CompletableFuture[String]
  val bsonValidatedEncoded: Array[Byte] = Array(1,2,3)


  val bosonEx: BosonEx = new BosonEx("first", result)
  val boson: io.boson.bson.bsonImpl.Boson = BosonEx.extractor(bosonEx.expression, bosonEx.extractFunction)
  //  boson.go(bsonValidatedEncoded)

  val bosonIj: BosonIj = new BosonIj("first", _=> "Adeus")
  val boson2: io.boson.bson.bsonImpl.Boson = BosonIj.injector(bosonIj.expression, bosonIj.injectFunction)
  //val result: CompletableFuture[Array[Byte]] = boson.go(bsonValidatedEncoded)


  override def go(bsonByteEncoding: Array[Byte]) = ???

  override def go(bsonByteBufferEncoding: ByteBuffer) = ???

  override def fuse(boson: bsonImpl.Boson) = ???
}
