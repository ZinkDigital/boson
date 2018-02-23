package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.{BosonImpl, CustomException}
import io.boson.bson.bsonValue

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import io.boson.bson.bsonImpl.Dictionary._

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter[T, R](boson: BosonImpl, program: Program, fInj: Option[Function[T,T]] = None, fExt: Option[Function[R,Unit]] = None) {

  def run(): bsonValue.BsValue = {
    fInj.isDefined match {
      case true => startInjector(program.statement)
      case false if fExt.isDefined => start(program.statement)
      case false => throw new IllegalArgumentException("Construct Boson object with at least one Function.")
    }
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case MoreKeys(first, list, dots) if first.isInstanceOf[ROOT]=>
          //println(s"statements: ${List(first) ++ list}")
          //println(s"dotList: $dots")
          executeMoreKeys(first, list, dots)
        case MoreKeys(first, list, dots) =>
          //println(s"statements: ${List(first) ++ list}")
          //println(s"dotList: $dots")
          executeMoreKeys(first, list, dots)
        case _ => throw new RuntimeException("Something went wrong!!!")
      }
    }else throw new RuntimeException("List of statements is empty.")
  }

  private def buildKeyList(first: Statement, statementList: List[Statement], dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList, limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      first match {
        case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
        case HalfName(halfName) =>
          halfName.equals(STAR) match {
            case true => (List((halfName, C_ALL)), List((None, None, STAR)))
            case false if statementList.nonEmpty => (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
            case false => (List((halfName, C_LEVEL)), List((None, None, EMPTY_KEY)))
          }
        case HasElem(key, elem) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case Key(key) => if (statementList.nonEmpty) (List((key, C_NEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_LEVEL)), List((None, None, EMPTY_KEY)))
        case ROOT() => (List((C_DOT,C_DOT)), List((None,None,EMPTY_RANGE)))
        case _ => throw CustomException("Error building key list")
      }
    if (statementList.nonEmpty) {
      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for (statement <- statementList) yield {
          statement match {
            case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
            case HalfName(halfName) =>if(halfName.equals(STAR)) (List((halfName, C_ALL)), List((None, None, STAR))) else (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY))) //TODO: treat '*'
            case HasElem(key, elem) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
            case Key(key) => (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
            case _ => throw CustomException("Error building key list")
          }
        }
      val secondList: List[(String, String)] = firstList ++ forList.flatMap(p => p._1)
      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(p => p._2)

      val thirdList: List[(String, String)] = secondList.zipWithIndex map {elem =>
        elem._1._2 match {
          case C_LIMITLEVEL => if(dotsList.take(elem._2+1).last.equals(C_DOUBLEDOT)) (elem._1._1,C_LIMIT) else elem._1
          case C_LEVEL => println("----- NOT POSSIBLE----"); elem._1
          case C_FILTER => elem._1
          case C_NEXT => elem._1
          case C_ALL => (elem._1._1,C_NEXT)//elem._1
          case _ => throw CustomException("Error building key list with dots")
        }
      }
      dotsList.last match {
        case C_DOT =>
          statementList.last match {
            case HalfName(halfName) if !halfName.equals(STAR) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_LEVEL)), limitList2)
            case HalfName(halfName) if halfName.equals(STAR) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_ALL)), limitList2)
            case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, C_LEVEL)), limitList2)
            case _ => (thirdList, limitList2)
          }
        case C_DOUBLEDOT =>
          statementList.last match {
            case HalfName(halfName) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_ALL)), limitList2) //TODO: treat '*'
            case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, C_ALL)), limitList2)
            case _ => (thirdList, limitList2)
          }
      }
    } else {
      (firstList.map { elem =>
        elem._2 match {
          case C_LIMITLEVEL => if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_LIMIT) else elem
          case C_LEVEL => if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_ALL) else elem
          case C_FILTER => elem
          case C_NEXT => println("----- NOT POSSIBLE----");if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_ALL) else elem
          case C_ALL => elem
          case C_DOT => elem
        }
      },limitList1)
    }
  }

  private def defineLimits(left: Int, mid: Option[String], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
    mid.isDefined match {
      case true if right.isEmpty=>
        mid.get match {
          case C_FIRST => List((Some(0),Some(0),TO_RANGE))
          case C_ALL => List((Some(0),None,TO_RANGE))
          case C_END => List((Some(0),None,C_END))
        }
      case true if right.isDefined =>
        (left, mid.get.toLowerCase, right.get) match {
          case (a, UNTIL_RANGE, C_END) => List((Some(a),None,UNTIL_RANGE))
          case (a, _, C_END) => List((Some(a),None,TO_RANGE))
          case (a, expr, b) if b.isInstanceOf[Int] =>
            expr.toLowerCase match {
              case TO_RANGE => List((Some(a),Some(b.asInstanceOf[Int]),TO_RANGE))
              case UNTIL_RANGE => List((Some(a),Some(b.asInstanceOf[Int]-1),TO_RANGE))
            }
        }
      case false =>
        List((Some(left),Some(left),TO_RANGE))
    }
//    if(mid.isDefined && right.isDefined) {
//      (left, mid.get.toLowerCase, right.get) match {
//        case (a, UNTIL_RANGE, C_END) => List((Some(a),None,UNTIL_RANGE))
//        case (a, _, C_END) => List((Some(a),None,TO_RANGE))
//        case (a, expr, b) if b.isInstanceOf[Int] =>
//          expr.toLowerCase match {
//            case TO_RANGE => List((Some(a),Some(b.asInstanceOf[Int]),TO_RANGE))
//            case UNTIL_RANGE => List((Some(a),Some(b.asInstanceOf[Int]-1),TO_RANGE))
//          }
//      }
//    } else { //[#]
//      List((Some(left),Some(left),TO_RANGE))
//    }
  }

  private def executeMoreKeys(first: Statement, list: List[Statement], dotsList: List[String]): bsonValue.BsValue = {
    val keyList: (List[(String, String)], List[(Option[Int], Option[Int], String)]) = buildKeyList(first, list, dotsList)
    //println("after build keylist -> " + keyList._1)
    //println("after build limitlist -> " + keyList._2)
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, keyList._1, keyList._2) map { v =>
             v.asInstanceOf[Seq[Any]]
      } getOrElse Seq.empty[Any]
    result match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty[Any])
      case v => bsonValue.BsObject.toBson {
        (for (elem <- v) yield {
          elem match {
            case e: Array[Any] =>
              Compose.composer(e)
            case e => e
          }
        }).toVector
      }
    }
  }

  private def startInjector(statement: List[Statement]): bsonValue.BsValue = {
    val stat: MoreKeys = statement.head.asInstanceOf[MoreKeys]
    val united: List[Statement] = stat.list.+:(stat.first)
    val zipped: List[(Statement, String)] =
      if(stat.first.isInstanceOf[ROOT]){
        united.map(e => (e, C_DOT))
      }else{
       united.zip(stat.dotList)
      }
    println(zipped)
    executeMultipleKeysInjector(zipped)
  }

  private def executeMultipleKeysInjector(statements: List[(Statement, String)]): bsonValue.BsValue = {
    val result:bsonValue.BsValue=
      Try(boson.execStatementPatternMatch(boson.getByteBuf, statements, fInj.get ))match{
        case Success(v)=>

          val bsResult: bsonValue.BsValue = bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
          v.release()
          bsResult
        case Failure(e)=>bsonValue.BsException(e.getMessage)      }
    boson.getByteBuf.release()
    result
  }
}

object Compose {

  def composer(value: Array[Any]): Seq[Any] = {
    val help: ListBuffer[Any] = new ListBuffer[Any]
    for (elem <- value) {
      elem match {
        case e: Array[Any] =>
          help.append(composer(e))
        case e: List[Any] =>
          for (elem <- e) {
            elem match {
              case v: Array[Any] =>
                help.append(composer(v))
              case v =>
                help.append(v)
            }
          }
        case e =>
          help.append(e)
      }
    }
    help.toList
  }

}
