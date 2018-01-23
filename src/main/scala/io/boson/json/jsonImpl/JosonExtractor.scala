package io.boson.json.jsonImpl

import java.util.concurrent.CompletableFuture

import io.boson.bson.bsonImpl.{BosonImpl, CustomException}
import io.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.{BsBoson, BsValue}
import io.boson.json.Joson
import io.boson.json.jsonPath.{JsonInterpreter, JsonProgram, JsonTinyLanguage}
import io.netty.buffer.ByteBuf

import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
class JosonExtractor[T](expression: String, extractFunction: java.util.function.Consumer[T]) extends Joson {
  /**
    * Apply this Joson to the String that arrives and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param the Json string.
    * @return
    */
  private def callParse(boson: BosonImpl, expression: String): io.boson.bson.bsonValue.BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          new Interpreter(boson, r.asInstanceOf[Program]).run()
        case parser.Error(msg, _) => bsonValue.BsObject.toBson(msg)
        case parser.Failure(msg, _) => bsonValue.BsObject.toBson(msg)
      }
    } catch {
      case e: RuntimeException => bsonValue.BsObject.toBson(e.getMessage)
    }
  }

  override def go(jsonStr: String): CompletableFuture[String] = {
    val parser = new JsonTinyLanguage
    val buffer: ByteBuf = Try(parser.parseAll(parser.programJson, jsonStr)) match{
      case Success(v) =>
        val input: JsonProgram = v.get
        new JsonInterpreter().runJsonEncoder(input)
      case Failure(e) => throw CustomException(e.getMessage)
    }

    val bsonByteEncoding: Array[Byte] = buffer.array()

    val future: CompletableFuture[String] =
      CompletableFuture.supplyAsync(() => {
        val boson: io.boson.bson.bsonImpl.BosonImpl = new BosonImpl(byteArray = Option(bsonByteEncoding))
        callParse(boson, expression) match {
          case (res: BsValue) =>
            //TODO decode Boson to Json

            extractFunction.accept(res.asInstanceOf[T])
          case _ => throw new RuntimeException("JosonExtractor -> go() default case!!!")
        }
        jsonStr
      })
    future
  }

  /**
    * Fuse one Joson to another. The joson that is 'this' should be executed first before the
    * joson that is the parameter in teh case of update/read conflicts.
    * the immutable String being returned unmodified.
    *
    * @param the Joson to fuse to.
    * @return the fused Joson
    */
  override def fuse(joson: Joson): Joson = ???
}
