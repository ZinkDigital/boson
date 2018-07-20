package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{DSLParser, Interpreter}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class BosonValidate[T](expression: String, validateFunction: T => Unit) extends Boson{

//  private def callParse(boson: BosonImpl, expression: String): Unit = {
//    val parser = new DSLParser(expression)
//    try {
//      parser.Parse() match {
//        case Success(result) =>
//          new Interpreter[T](boson, result, fExt = Option(validateFunction)).run()
//        case Failure(exc) => throw exc
//      }
//    } catch {
//      case e: RuntimeException => throw new Exception(e.getMessage)
//    }
//  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    Future{
//      val boson:BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
//        callParse(boson,expression)
        bsonByteEncoding
    }
  }


//  override def fuse(boson: Boson) = ???
  //new BosonFuse(this,boson)
  override def go(bsonByteEncoding: String): Future[String] = ???
}
