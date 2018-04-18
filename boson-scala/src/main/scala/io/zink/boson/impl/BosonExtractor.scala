package io.zink.boson.impl

import java.nio.ByteBuffer

import io.zink.boson.Boson
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import shapeless.{TypeCase, Typeable}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Boson instance which aims to handle extraction of primitive types or sequences of them.
  * These types doesn't compile with an implicit LabelledGeneric and for that reason a different class
  * is required.
  *
  * @param expression String given by the User designated as BsonPath.
  * @param extractFunction  Extract function given by the User to be applied after extraction.
  * @tparam T Type of Value to be extracted.
  */
class BosonExtractor[T](expression: String, extractFunction: T => Unit)(implicit tp: Option[TypeCase[T]]) extends Boson {

  //val print = println("BosonExtractor instantiated")

  val boson: BosonImpl = new BosonImpl()

  val parsedStatements: ProgStatement = new DSLParser(expression).Parse() match {
    case Success(result) => result
    case Failure(excp) => throw excp
  }

  val (keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]) =
    buildExtractors(parsedStatements.statementList.head, parsedStatements.statementList.tail, parsedStatements.dotsList)

  val returnInsideSeqFlag: Boolean = returnInsideSeq(keyList,limitList,parsedStatements.dotsList)

  /**
    * BuildExtractors takes a statementList provided by the parser and transforms it into two lists used to extract.
    *
    * @param statementList  List of statements used to create the pairs of (Key,Condition) and (Range,Condition)
    * @return Tuple with (KeyList,LimitList)
    */
  private def buildExtractors(firstStatement: Statement, statementList: List[Statement],dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList, limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      firstStatement match {
        case Key(key) if dotsList.head.equals(C_DOT)=> if (statementList.nonEmpty) (List((key, C_NEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_LEVEL)), List((None, None, EMPTY_KEY)))
        case Key(key) => if (statementList.nonEmpty) (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
        case KeyWithArrExpr(key, arrEx) if dotsList.head.equals(C_DOT)=> (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case ArrExpr(l, m, r) if dotsList.head.equals(C_DOT)=> (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
        case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
        case HalfName(halfName) =>
          halfName.equals(STAR) match {
            case true => (List((halfName, C_ALL)), List((None, None, STAR)))
            case false if statementList.nonEmpty && dotsList.head.equals(C_DOT)=> (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
            case false if statementList.nonEmpty => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
            case false if dotsList.head.equals(C_DOT)=> (List((halfName, C_LEVEL)), List((None, None, EMPTY_KEY)))
            case false => (List((halfName, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
          }
        case HasElem(key, elem) if dotsList.head.equals(C_DOT) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case ROOT => (List((C_DOT, C_DOT)), List((None, None, EMPTY_RANGE)))
      }

    if(statementList.nonEmpty) {
      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for {
          statement <- statementList.zip(dotsList.tail)
        } yield {
          statement._1 match {
            case Key(key) if statement._2.equals(C_DOT)=> (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
            case Key(key) => (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
            case KeyWithArrExpr(key, arrEx) if statement._2.equals(C_DOT)=> (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case ArrExpr(l, m, r) if statement._2.equals(C_DOT)=> (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
            case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
            case HalfName(halfName) =>
              halfName.equals(STAR) match {
                case true => (List((halfName, C_ALL)), List((None, None, STAR)))
                case false if dotsList.head.equals(C_DOT)=> (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
                case false  => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
              }
            case HasElem(key, elem) if statement._2.equals(C_DOT)=> (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
            case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))

          }
        }
      val secondList: List[(String, String)] = firstList ++ forList.flatMap(_._1)
      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(_._2)

      statementList.last match {
        case HalfName(halfName) if !halfName.equals(STAR) && dotsList.last.equals(C_DOT) => (secondList.take(secondList.size - 1) ++ List((halfName, C_LEVEL)), limitList2)
        case HalfName(halfName) if !halfName.equals(STAR) => (secondList.take(secondList.size - 1) ++ List((halfName, C_ALL)), limitList2)
        case Key(k) if dotsList.last.equals(C_DOT)=> (secondList.take(secondList.size - 1) ++ List((k, C_LEVEL)), limitList2)
        case Key(k) => (secondList.take(secondList.size - 1) ++ List((k, C_ALL)), limitList2)
        case _ => (secondList, limitList2)
      }
    } else {
      (firstList,limitList1)
    }

  }

  /**
    * DefineLimits takes a set of arguments that represent a range defined by the User through BsonPath and transforms it
    * into a Tuple3.
    *
    * @param left Integer representing the lower limit of a Range.
    * @param mid  String indicating which type of Range it is.
    * @param right  Integer representing the upper limit of a Range.
    * @return Returns a Tuple3 used to represent a range.
    */
  private def defineLimits(left: Int, mid: Option[RangeCondition], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
    mid.isDefined match {
      case true if right.isEmpty =>
        mid.get.value match {
          case C_FIRST => List((Some(0), Some(0), TO_RANGE))
          case C_ALL => List((Some(0), None, TO_RANGE))
          case C_END => List((Some(0), None, C_END))
        }
      case true if right.isDefined =>
        (left, mid.get.value.toLowerCase, right.get) match {
          case (a, UNTIL_RANGE, C_END) => List((Some(a), None, UNTIL_RANGE))
          case (a, _, C_END) => List((Some(a), None, TO_RANGE))
          case (a, expr, b) if b.isInstanceOf[Int] =>
            expr.toLowerCase match {
              case TO_RANGE => List((Some(a), Some(b.asInstanceOf[Int]), TO_RANGE))
              case UNTIL_RANGE => List((Some(a), Some(b.asInstanceOf[Int] - 1), TO_RANGE))
            }
        }
      case false =>
        List((Some(left), Some(left), TO_RANGE))
    }
  }

  /**
    * ReturnInsideSeq returns a Boolean which indicates whether the extracted result should be returned inside
    * a sequence or not.
    *
    * @param limitList  List of Tuple3 with Ranges and Conditions
    * @return Boolean
    */
  private def returnInsideSeq(keyList: List[(String, String)],limitList: List[(Option[Int], Option[Int], String)], dotsList: Seq[String]): Boolean =
    limitList.exists { elem =>
      elem._1.isDefined match {
        case true if elem._2.isEmpty => if(elem._3.equals(C_END))false else true
        case true if elem._2.isDefined && elem._2.get != elem._1.get => true
        case true if elem._2.isDefined && elem._2.get == elem._1.get => false
        case false => false
      }
    } || dotsList.exists(e => e.equals(C_DOUBLEDOT)) ||  keyList.exists(e => e._2.equals(C_FILTER) || e._1.equals(STAR))

  /**
    * CallParse instantiates the parser where a set of rules is verified and if the parsing is successful it returns a list of
    * statements used to instantiate the Interpreter.
    */
  // byteArr as argument to go to interpreter, Either[byte[],String]
  private def runInterpreter(bsonEncoded: Either[Array[Byte],String]): Unit = {
    new Interpreter[T](boson, bsonEncoded, keyList,limitList,returnInsideSeqFlag, fExt = Option(extractFunction)).run()
  }

  override def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]] = {
    val future: Future[Array[Byte]] =
      Future {
        runInterpreter(Left(bsonByteEncoding))
        bsonByteEncoding
      }
    future
  }

  override def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer] = {
    val future: Future[ByteBuffer] =
      Future {
        runInterpreter(Left(bsonByteBufferEncoding.array()))
          bsonByteBufferEncoding
      }
    future
  }

  override def fuse(boson: Boson): Boson = new BosonFuse(this, boson)

}
