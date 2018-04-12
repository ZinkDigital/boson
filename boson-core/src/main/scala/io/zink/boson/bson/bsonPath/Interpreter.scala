package io.zink.boson.bson.bsonPath

import java.time.Instant
import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonImpl._
import shapeless.TypeCase
import scala.util.{Failure, Success, Try}
import scala.collection.optimizer._

/**
  * Created by Tiago Filipe on 02/11/2017.
  */

/**
  * Class that handles both processes of Injection and Extraction.
  *
  * @param boson Instance of BosonImpl.
  * @param fInj Function used in Injection process.
  * @param fExt Function used in Extraction process.
  * @tparam T Type specified by the User.
  */
class Interpreter[T](boson: BosonImpl,
                     bsonEncoded: Either[Array[Byte],String],
                     keyList: List[(String, String)],
                     limitList: List[(Option[Int], Option[Int], String)],
                     returnInsideSeqFlag: Boolean,
                     fInj: Option[T => T] = None,
                     fExt: Option[T => Unit] = None) {

  /**
    * Run is the only public method of the object Interpreter and depending on which function it was instantiated with it chooses whether it starts
    * an injection or an extraction process.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), in the case of an Injection it returns the modified event as an encoded Array[Byte].
    */
  def run(): Any = {
    //if (fInj.isDefined) startInjector(program)
    //else
    start()
  }

  /**
    * Method that initiates the proccess of extraction based on a Statement list provided by the parser.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  private def start(): Any = {
    //instead boson.get.. as argument replace with Either[..]
    bsonEncoded match {
      case Left(byteArr) =>
        val buf: ByteBuf = Unpooled.copiedBuffer(byteArr)
        extract(buf, keyList, limitList)
      case Right(jsonString) => ???
    }
  }

  /**
    * This method does the final extraction of an Object.
    *
    * @param encodedSeqByteArray  Sequence of BsonObjects encoded.
    * @param keyList  Pairs of Keys and Conditions used to decode the encodedSeqByteArray
    * @param limitList  Pairs of Ranges and Conditions used to decode the encodedSeqByteArray
    * @return List of Tuples corresponding to pairs of Key and Value used to build case classes
    */
  private def constructObj(encodedSeqByteArray: Seq[Array[Byte]], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Seq[List[(String, Any)]] = {
    val seqTuples = TypeCase[Seq[List[Any]]]
    val listTuples = TypeCase[List[Any]]
    /**
      *
      * @param list List with Keys and Values from extracted objects
      * @return List of Tuples corresponding to pairs of Key and Value used to build case classes
      */
    def toTuples(list: Iterable[Any]): List[(String, Any)] = {
      list match {
        case x: List[Any] if x.isEmpty => List()
        case x: List[Any] if x.lengthCompare(2) >= 0 && x.tail.head.isInstanceOf[Seq[Any]] =>
          x.tail.head match {
            case seqTuples(value) => List((x.head.asInstanceOf[String],value.map(toTuples(_))))
            case listTuples(value) => List((x.head.asInstanceOf[String], toTuples(value)))
          }
        case x: List[Any] if x.lengthCompare(2) >= 0 => List((x.head.asInstanceOf[String], x.tail.head)) ++ toTuples(x.drop(2))
      }
    }

    val res: Seq[List[(String, Any)]] =
    encodedSeqByteArray.par.map { encodedByteArray =>
      val res: Iterable[Any] = runExtractors(Unpooled.copiedBuffer(encodedByteArray), keyList, limitList)
      val l: List[(String, Any)] = toTuples(res).map(elem => (elem._1.toLowerCase, elem._2))
      l
    }.seq
    res
  }
//
//  /**
//    * BuildExtractors takes a statementList provided by the parser and transforms it into two lists used to extract.
//    *
//    * @param statementList  List of statements used to create the pairs of (Key,Condition) and (Range,Condition)
//    * @return Tuple with (KeyList,LimitList)
//    */
//  private def buildExtractors(firstStatement: Statement, statementList: List[Statement],dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
//    val (firstList, limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
//      firstStatement match {
//        case Key(key) if dotsList.head.equals(C_DOT)=> if (statementList.nonEmpty) (List((key, C_NEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_LEVEL)), List((None, None, EMPTY_KEY)))
//        case Key(key) => if (statementList.nonEmpty) (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
//        case KeyWithArrExpr(key, arrEx) if dotsList.head.equals(C_DOT)=> (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
//        case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
//        case ArrExpr(l, m, r) if dotsList.head.equals(C_DOT)=> (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
//        case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
//        case HalfName(halfName) =>
//          halfName.equals(STAR) match {
//            case true => (List((halfName, C_ALL)), List((None, None, STAR)))
//            case false if statementList.nonEmpty && dotsList.head.equals(C_DOT)=> (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
//            case false if statementList.nonEmpty => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
//            case false if dotsList.head.equals(C_DOT)=> (List((halfName, C_LEVEL)), List((None, None, EMPTY_KEY)))
//            case false => (List((halfName, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
//          }
//        case HasElem(key, elem) if dotsList.head.equals(C_DOT) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
//        case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
//        case ROOT => (List((C_DOT, C_DOT)), List((None, None, EMPTY_RANGE)))
//      }
//
//    if(statementList.nonEmpty) {
//      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
//        for {
//          statement <- statementList.zip(dotsList.tail)
//        } yield {
//          statement._1 match {
//            case Key(key) if statement._2.equals(C_DOT)=> (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
//            case Key(key) => (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
//            case KeyWithArrExpr(key, arrEx) if statement._2.equals(C_DOT)=> (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
//            case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
//            case ArrExpr(l, m, r) if statement._2.equals(C_DOT)=> (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
//            case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
//            case HalfName(halfName) =>
//              halfName.equals(STAR) match {
//                case true => (List((halfName, C_ALL)), List((None, None, STAR)))
//                case false if dotsList.head.equals(C_DOT)=> (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
//                case false  => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
//              }
//            case HasElem(key, elem) if statement._2.equals(C_DOT)=> (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
//            case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
//
//          }
//        }
//      val secondList: List[(String, String)] = firstList ++ forList.flatMap(_._1)
//      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(_._2)
//
//      statementList.last match {
//        case HalfName(halfName) if !halfName.equals(STAR) && dotsList.last.equals(C_DOT) => (secondList.take(secondList.size - 1) ++ List((halfName, C_LEVEL)), limitList2)
//        case HalfName(halfName) if !halfName.equals(STAR) => (secondList.take(secondList.size - 1) ++ List((halfName, C_ALL)), limitList2)
//        case Key(k) if dotsList.last.equals(C_DOT)=> (secondList.take(secondList.size - 1) ++ List((k, C_LEVEL)), limitList2)
//        case Key(k) => (secondList.take(secondList.size - 1) ++ List((k, C_ALL)), limitList2)
//        case _ => (secondList, limitList2)
//      }
//    } else {
//      (firstList,limitList1)
//    }
//
//  }

  /**
    * Extract is the method which puts together the process of extraction and applies the function provided by the User
    * or, in case of Object extraction, provides a list of pairs (Key,Value) extracted from the desired Object.
    *
    * @param encodedStructure ByteBuf wrapping an Array[Byte] encoded representing the Event.
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  private def extract(encodedStructure: ByteBuf, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Any = {
    val result: Iterable[Any] = runExtractors(encodedStructure, keyList, limitList)
    val typeClass: Option[String] =
      result.size match {
        case 0 => None
        case 1 => Some(result.head.getClass.getSimpleName)
        case _ =>
          if (result.tail.forall { p => result.head.getClass.equals(p.getClass) }) Some(result.head.getClass.getSimpleName)
          else Some(ANY)
      }
    applyFunction(result,keyList,limitList,typeClass,returnInsideSeqFlag)
  }

//  /**
//    * ReturnInsideSeq returns a Boolean which indicates whether the extracted result should be returned inside
//    * a sequence or not.
//    *
//    * @param limitList  List of Tuple3 with Ranges and Conditions
//    * @return Boolean
//    */
//  private def returnInsideSeq(keyList: List[(String, String)],limitList: List[(Option[Int], Option[Int], String)], dotsList: Seq[String]): Boolean =
//    limitList.exists { elem =>
//      elem._1.isDefined match {
//        case true if elem._2.isEmpty => if(elem._3.equals(C_END))false else true
//        case true if elem._2.isDefined && elem._2.get != elem._1.get => true
//        case true if elem._2.isDefined && elem._2.get == elem._1.get => false
//        case false => false
//      }
//    } || dotsList.exists(e => e.equals(C_DOUBLEDOT)) ||  keyList.exists(e => e._2.equals(C_FILTER) || e._1.equals(STAR))

  /**
    * RunExtractors is the method that iterates over KeyList, LimitList and encodedStructure doing the bridge
    * between Interpreter with BosonImpl.
    *
    * @param encodedStructure ByteBuf wrapping an Array[Byte] encoded representing the Event.
    * @param keyList  List of pairs (Key,Condition) used to perform extraction according to the User.
    * @param limitList  List of Tuple3 (Range,Range,Condition) used to perform extraction according to the User.
    * @return Extracted result.
    */
  private def runExtractors(encodedStructure: ByteBuf, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    val value: Iterable[Any] =
      keyList.size match {
        case 1 =>
          boson.extract(encodedStructure, keyList, limitList)
        case 2 if keyList.drop(1).head._2.equals(C_FILTER)=>
          boson.extract(encodedStructure, keyList, limitList)
        case _ =>
          val res: Iterable[Any] = boson.extract(encodedStructure, keyList, limitList)
          val filtered = res.collect{ case buf: ByteBuf => buf}
          filtered.size match {
            case 0 => Seq() //throw CustomException("The given path doesn't correspond with the event structure.")
            case _ =>
              val result: Iterable[Iterable[Any]] =
                filtered.par.map { elem =>
                  if(keyList.drop(1).head._2.equals(C_FILTER)) runExtractors(elem, keyList.drop(2), limitList.drop(2))
                  else runExtractors(elem, keyList.drop(1), limitList.drop(1))
                }.seq//.view
                  result.reduce(_++_)
//              if(result.nonEmpty){
//                //println(resultComposer(result))
//                //resultComposer(result)
//                result.reduce(_++_)
//              } else result
          }
      }
    //println(s"final value from runing extractors ----> $value")
    value
  }

//  def thing(list: Iterable[Iterable[Any]]): Iterable[Any] = optimize {
//    list.reduce(_ ++ _)
//  }

  //TODO: rethink a better strategy to verify if T and type of extracted are the same
  private def applyFunction(result: Iterable[Any],keyList: List[(String, String)],limitList: List[(Option[Int], Option[Int], String)], typeClass: Option[String], returnInsideSeqFlag: Boolean): Any = {
    if (returnInsideSeqFlag) {
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING if fExt.isDefined =>
            val res: Seq[String] = result.asInstanceOf[Iterable[String]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[String] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Instant] => Unit], res.map(elem => Instant.parse(elem)))) match {
                case Success(_) =>
                case Failure(_) =>
                  val extracted: Seq[Array[Byte]] = res.map(elem =>java.util.Base64.getDecoder.decode(elem))
                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], extracted)) match {
                    case Success(_) =>
                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
                  }
              }
            }
          case INTEGER if fExt.isDefined =>
            val res: Seq[Int] = result.asInstanceOf[Iterable[Int]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Int] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case LONG if fExt.isDefined =>
            val res: Seq[Long] = result.asInstanceOf[Iterable[Long]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Long] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case BOOLEAN if fExt.isDefined =>
            val res: Seq[Boolean] = result.asInstanceOf[Iterable[Boolean]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Boolean] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case DOUBLE if fExt.isDefined =>
            val res: Seq[Double] = result.asInstanceOf[Iterable[Double]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Double] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Float] => Unit], res.map(_.toFloat))) match {
                case Success(_) =>
                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
              }
            }
          case COPY_BYTEBUF if fExt.isDefined =>
            //println("Seq[byte[]], fExt.isDefined")
            val res = result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array)
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) =>  throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case COPY_BYTEBUF =>
            constructObj(result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
          case ANY =>
            val res: Seq[Any] = result.toSeq.map {
              case buf: ByteBuf => buf.array
              case elem => elem
            }
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Any] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    } else {
      //println("return without Seq")
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING if fExt.isDefined =>
            val res: String = result.head.asInstanceOf[String]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[String => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Instant => Unit], Instant.parse(res))) match {
                case Success(_) =>
                case Failure(_) =>
                  val extracted: Array[Byte] = java.util.Base64.getDecoder.decode(res)
                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], extracted)) match {
                    case Success(_) =>
                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
                  }
              }
            }
          case INTEGER if fExt.isDefined =>
            val res: Int = result.head.asInstanceOf[Int]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Int => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case LONG if fExt.isDefined =>
            val res: Long = result.head.asInstanceOf[Long]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Long => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case BOOLEAN if fExt.isDefined =>
            val res: Boolean = result.head.asInstanceOf[Boolean]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Boolean => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case DOUBLE if fExt.isDefined =>
            val res: Double = result.head.asInstanceOf[Double]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Double => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Float => Unit], res.toFloat)) match {
                case Success(_) =>
                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
              }
            }
          case COPY_BYTEBUF if fExt.isDefined =>
            val res = result.asInstanceOf[Seq[ByteBuf]].head.array
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case COPY_BYTEBUF =>
            constructObj(result.asInstanceOf[Seq[ByteBuf]].map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    }
  }

//  /**
//    * DefineLimits takes a set of arguments that represent a range defined by the User through BsonPath and transforms it
//    * into a Tuple3.
//    *
//    * @param left Integer representing the lower limit of a Range.
//    * @param mid  String indicating which type of Range it is.
//    * @param right  Integer representing the upper limit of a Range.
//    * @return Returns a Tuple3 used to represent a range.
//    */
//  private def defineLimits(left: Int, mid: Option[RangeCondition], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
//    mid.isDefined match {
//      case true if right.isEmpty =>
//        mid.get.value match {
//          case C_FIRST => List((Some(0), Some(0), TO_RANGE))
//          case C_ALL => List((Some(0), None, TO_RANGE))
//          case C_END => List((Some(0), None, C_END))
//        }
//      case true if right.isDefined =>
//        (left, mid.get.value.toLowerCase, right.get) match {
//          case (a, UNTIL_RANGE, C_END) => List((Some(a), None, UNTIL_RANGE))
//          case (a, _, C_END) => List((Some(a), None, TO_RANGE))
//          case (a, expr, b) if b.isInstanceOf[Int] =>
//            expr.toLowerCase match {
//              case TO_RANGE => List((Some(a), Some(b.asInstanceOf[Int]), TO_RANGE))
//              case UNTIL_RANGE => List((Some(a), Some(b.asInstanceOf[Int] - 1), TO_RANGE))
//            }
//        }
//      case false =>
//        List((Some(left), Some(left), TO_RANGE))
//    }
//  }

  private def startInjector(statement: Statement): Array[Byte] = {
    val stat: ProgStatement = statement.asInstanceOf[ProgStatement]
    val united: List[Statement] = stat.statementList.+:(stat.statementList.head)
    val zipped: List[(Statement, String)] =
      stat.statementList.head match {
        case ROOT => united.map(e => (e, C_DOT))
        case _ => united.zip(stat.dotsList)
      }
    executeMultipleKeysInjector(zipped)
  }
  //  TODO: replace Statement -> Statement, etc..
  private def executeMultipleKeysInjector(statements: List[(Statement, String)]): Array[Byte] = {
    val result: Array[Byte] =
      Try(boson.execStatementPatternMatch(boson.getByteBuf, statements, fInj.get)) match {
        case Success(v) =>
          //val bsResult: bsonValue.BsValue = bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
          //v.release()
          //bsResult
          v.array
        case Failure(e) =>
          throw CustomException(e.getMessage)
        //bsonValue.BsException(e.getMessage)
      }
    boson.getByteBuf.release()
    result
  }
}
