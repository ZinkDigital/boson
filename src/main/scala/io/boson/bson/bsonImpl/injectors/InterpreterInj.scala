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

        case ArrExpr(left: Int, mid: String, right: Any) => // "[# .. #]"
          executeArraySelect(left, mid, right) match {
            case Seq() => bsonValue.BsObject.toBson(Seq.empty)
            case v =>
              bsonValue.BsObject.toBson {
                for(elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
              }
          }
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
           bsonValue.BsException.apply(e.getMessage)
        }
      case "all" =>
        val result: Try[Boson] = Try(new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f)._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
      case "last"=>
        val ocorrencias: Option[Int] = Option(boson.findOcorrences(boson.getByteBuf.duplicate(), key).size-1)
        val result: Try[Boson] = Try(new Boson(byteArray = Option(boson.modifyAll(boson.getByteBuf.duplicate(), key, f, ocor = ocorrencias)._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
    }
  }

  def executeArraySelect(left: Int, mid: String, right: Any): Seq[Any] = {
    ???
  }
}
