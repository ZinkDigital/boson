package io.boson.bson.bsonImpl.injectors

import io.boson.bson.bsonImpl.Boson
import io.boson.bson.bsonValue
import io.boson.scalaInterface.ScalaInterface

import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 14/12/2017.
  */


class InterpreterInj(boson: Boson, key: String, f: Any => Any, program: ProgramInj) {

  def run(): bsonValue.BsValue = {
    start(program.statement)
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect(selectType)

      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  def executeSelect(selectType: String):  bsonValue.BsValue = {
    selectType match {
      case "first" =>

        val result: Try[Boson] = Try(boson.modify(Option(boson), key, f).get)
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            //println(e.getMessage)
           bsonValue.BsException.apply(e.getMessage)
          //println(e.getStackTrace.foreach(p => println(p.toString)))
        }
      case "all" =>

        //val newB: Boson = new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f).array()))
        val result: Try[Boson] = Try(new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f)._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            //println(e.getMessage)
            bsonValue.BsException.apply(e.getMessage)

          //println(e.getStackTrace.foreach(p => println(p.toString)))
        }

      case "last"=>

        //val newB: Boson = new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f).array()))
        //val ocorrencias: Option[Int] = Option(boson.findOcorrences(boson.getByteBuf.duplicate(), key).size-1)
        val result: Try[Boson] = Try(new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf.duplicate(), key, f, ocor = Option(1))._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            //println(e.getMessage)
            bsonValue.BsException.apply(e.getMessage)

          //println(e.getStackTrace.foreach(p => println(p.toString)))
        }
    }

  }
}
