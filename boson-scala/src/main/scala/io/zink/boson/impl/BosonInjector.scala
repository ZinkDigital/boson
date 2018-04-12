package io.zink.boson.impl

import java.nio.ByteBuffer

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{DSLParser, Interpreter, ProgStatement}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class BosonInjector[T](expression: String, injectFunction: Function[T, T]) extends Boson {

  val anon: T => T = injectFunction

//  val boson: BosonImpl = new BosonImpl()
//
//  val parsedStatements: ProgStatement = new DSLParser(expression).Parse() match {
//    case Success(result) => result
//    case Failure(excp) => throw excp
//  }
//
//
//  /**
//    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
//    * statements used to instantiate the Interpreter.
//    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
//    */
//  // byteArr as argument to go to interpreter, Either[byte[],String]
//  private def runInterpreter(bsonEncoded: Either[Array[Byte],String]): Unit = {
//    new Interpreter[T](boson, bsonEncoded, keyList,limitList,returnInsideSeqFlag).run()
//  }
//
//  private def parseInj(netty: BosonImpl): Unit = {
//    val parser = new DSLParser(expression)
//    try {
//      parser.Parse() match {
//        case Success(result) =>
//          new Interpreter[T](netty, result, fInj = Option(anon)).run()
//        case Failure(exc) => throw exc
//      }
//    } catch {
//      case e: RuntimeException => throw new Exception(e.getMessage)
//    }
//  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    //val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
    val future: Future[Array[Byte]] =
      Future{
      //val r: Array[Byte] = parseInj(boson).asInstanceOf[Array[Byte]]
      //r
        bsonByteEncoding
    }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    //val boson: BosonImpl = new BosonImpl(javaByteBuf = Option(bsonByteBufferEncoding))
    val future: Future[ByteBuffer] =
    Future{
//      val r: Array[Byte] = parseInj(boson).asInstanceOf[Array[Byte]]
//      val b: ByteBuf = Unpooled.copiedBuffer(r)
//      b.nioBuffer()
      bsonByteBufferEncoding
    }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this,boson)
}
