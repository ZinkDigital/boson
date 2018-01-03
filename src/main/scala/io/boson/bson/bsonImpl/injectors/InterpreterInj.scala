package io.boson.bson.bsonImpl.injectors

import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue
import io.boson.bson.bsonValue.{BsBoson, BsValue}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 14/12/2017.
  */
class InterpreterInj[T](boson: BosonImpl, f: T => T, program: ProgramInj) {

  def run(): bsonValue.BsValue = {
    start(program.statement)
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case KeyWithGrammar(k,grammar) => //key.grammar
          executeSelect(k,grammar.selectType)
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect("",selectType)

        case ArrExpr(left: Int, mid: String, right: Any) => // "[# .. #]"
          executeArraySelect("", left, mid, right)
        case KeyWithArrExpr(k,arrEx) =>    //key.[#..#]

          executeArraySelect(k,arrEx.leftArg,arrEx.midArg,arrEx.rightArg)

          /*executeArraySelect(k,arrEx.leftArg,arrEx.midArg,arrEx.rightArg) match {
            case Seq() => bsonValue.BsObject.toBson(Seq.empty)
            case v =>
              bsonValue.BsObject.toBson {
                for(elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
              }
          }*/
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  def executeSelect(key: String, selectType: String):  bsonValue.BsValue = {
    selectType match {
      case "first" =>
        val result: Try[BosonImpl] = Try(boson.modify(Option(boson), key, f).get)
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
           bsonValue.BsException.apply(e.getMessage)
        }
      case "all" =>
        val result: Try[BosonImpl] = Try(new BosonImpl(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f)._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
      case "last"=>
       // val ocorrencias: Option[Int] = Option(boson.findOcorrences(boson.getByteBuf.duplicate(), key).size-1)
        val result: Try[BosonImpl] = Try(boson.modifyEnd(boson.getByteBuf.duplicate(), key, f)._1)
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
    }
  }

  def executeArraySelect(key: String,left: Int, mid: String, right: Any): bsonValue.BsValue = {

    (key, left, mid.toLowerCase(), right) match {
      case ("", a, "until", "end") =>

        val midResult = Try(boson.modifyArrayEnd( f, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)

        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case ("", a, "to", "end") => // "[# .. end]"
        val midResult = Try(boson.modifyArrayEnd( f, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._1)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case ("", a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val range: Range = a to b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEnd( f, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEnd( f, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      case (k, a, "until", "end") =>


        val midResult = Try(boson.modifyArrayEndWithKey(k, f, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }
      case (k, a, "to", "end") =>


        val midResult = Try(boson.modifyArrayEndWithKey(k, f, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._1)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val range: Range = a to b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEndWithKey(k, f, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEndWithKey(k,f, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
       /* val some: Try[Option[Any]] = Try(boson.extract(boson.getByteBuf, k, "until", Option(a)))
        val firstResult: Equals = some match {
          case Success(v) =>  v.map { v =>
        v.asInstanceOf[Seq[Array[Any]]]
        }.getOrElse (Seq.empty)
            //bsonValue.BsObject.toBson(v)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }


        val midResult = Try(boson.modifyArrayEnd(f, a.toString))


        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)

        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }*/
      case _ => bsonValue.BsException.apply("Invalid Expression.")
    }
  }


}
