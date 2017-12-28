package io.boson.bson.bsonImpl.injectors

import io.boson.bson.bsonImpl.{Boson, CustomException}
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.BsBoson
import io.boson.scalaInterface.ScalaInterface

import scala.collection.mutable.ListBuffer
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
          executeArraySelect(left, mid, right)
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

  def executeArraySelect(left: Int, mid: String, right: Any): bsonValue.BsValue = {

    (left, mid.toLowerCase(), right) match {
      case (a, "until", "end") =>

        val list: ListBuffer[String] = boson.countArrayPositions
        list.-=(list.last)
        val midResult = Try(boson.modifyArrayWithList(list.toList, f))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)

        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case (a, "to", "end") => // "[# .. end]"
        val list: ListBuffer[String] = boson.countArrayPositions
        val midResult = Try(boson.modifyArrayWithList(list.toList, f))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val range: Range = a to b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayWithList(list.toList, f))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayWithList(list.toList, f))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      case _ => bsonValue.BsException.apply("Invalid Expression.")
    }
  }


}
