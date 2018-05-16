package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.Interpreter
import shapeless.{HList, LabelledGeneric, TypeCase}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BosonInjectorObj[T, R <: HList](expression: String, injectFunction: Option[T => T] = None, injectSeqFunction: Option[Seq[T] => T] = None)(implicit
                                                                                                                                                gen: LabelledGeneric.Aux[T, R],
                                                                                                                                                inject: injectLabels[R],
                                                                                                                                                tp: Option[TypeCase[T]]) extends Boson {

  private val boson: BosonImpl = new BosonImpl()

  private val interpreter: Interpreter[T] = new Interpreter[T](boson,expression)

  private def runInterpreter(bsonEncoded: Either[Array[Byte],String]): Any = interpreter.run(bsonEncoded)

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = ???

  override def go(bsonByteEncoding: String): Future[String] = ???

//  override def fuse(boson: Boson): Boson = ???
}
