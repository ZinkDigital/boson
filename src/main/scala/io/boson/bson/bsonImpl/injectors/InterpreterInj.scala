package io.boson.bson.bsonImpl.injectors

import io.boson.bson.bsonImpl.Boson
import io.boson.bson.bsonValue

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
        val inj = new Injector()
        val result: Try[Boson] = Try(inj.modify(Option(boson), key, f).get)
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
