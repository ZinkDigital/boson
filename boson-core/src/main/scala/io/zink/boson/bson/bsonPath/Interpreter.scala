package io.zink.boson.bson.bsonPath

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary.{oneString, _}
import io.zink.boson.bson.bsonImpl._
import shapeless.{TypeCase, Typeable}

import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */

/**
  * Class that handles both processes of Injection and Extraction.
  *
  * @param boson Instance of BosonImpl.
  * @param fInj  Function used in Injection process.
  * @param fExt  Function used in Extraction process.
  * @tparam T Type specified by the User.
  */
class Interpreter[T](boson: BosonImpl,
                     expression: String,
                     fInj: Option[T => T] = None,
                     fExt: Option[T => Unit] = None)(implicit tCase: Option[TypeCase[T]]) {

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
      //println(s"secondList -> $secondList")
      //println(s"limitList2 -> $limitList2")

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
    * Run is the only public method of the object Interpreter and depending on which function it was instantiated with it chooses whether it starts
    * an injection or an extraction process.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), in the case of an Injection it returns the modified event as an encoded Array[Byte].
    */
  def run(bsonEncoded: Either[Array[Byte], String]): Any = {
    //if (fInj.isDefined) startInjector(program)
    //else
    start(bsonEncoded)
  }

  /**
    * Method that initiates the proccess of extraction based on a Statement list provided by the parser.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  private def start(bsonEncoded: Either[Array[Byte], String]): Any = {
    //instead boson.get.. as argument replace with Either[..]
    bsonEncoded match {
      case Left(byteArr) =>
        //val buf: ByteBuf = Unpooled.copiedBuffer(byteArr)
        extract(Left(byteArr), keyList, limitList)
      case Right(jsonString) => extract(Right(jsonString), keyList, limitList)
    }
  }

  /**
    * This method does the final extraction of an Object.
    *
    * @param encodedSeqByteBuf Sequence of BsonObjects encoded.
    * @param keyList             Pairs of Keys and Conditions used to decode the encodedSeqByteArray
    * @param limitList           Pairs of Ranges and Conditions used to decode the encodedSeqByteArray
    * @return List of Tuples corresponding to pairs of Key and Value used to build case classes
    */
  private def constructObj(encodedSeqByteBuf: List[Array[Byte]], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Seq[List[(String, Any)]] = {


    /**
      *
      * @param list List with Keys and Values from extracted objects
      * @return List of Tuples corresponding to pairs of Key and Value used to build case classes
      */
    def toTuples(list: List[Any]): List[(String, Any)] = {
      list match {
        case x: List[Any] if x.isEmpty => Nil
        case x: List[Any] if x.lengthCompare(2) >= 0 && x.tail.head.isInstanceOf[List[Any]] =>
          x.tail.head match {
            case seqTuples(value) => List((x.head.asInstanceOf[String], value.map(toTuples)))
            case listTuples(value) => List((x.head.asInstanceOf[String], toTuples(value)))
          }
        case x: List[Any] if x.lengthCompare(2) >= 0 => List((x.head.asInstanceOf[String], x.tail.head)) ++ toTuples(x.drop(2))
      }
    }

    val res: Seq[List[(String, Any)]] =
      encodedSeqByteBuf.par.map { encoded =>
        val res: List[Any] = runExtractors(Left(encoded), keyList, limitList)
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
  private def extract(encodedStructure: Either[Array[Byte], String], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Any = {
    val result: List[Any] = runExtractors(encodedStructure, keyList, limitList)
    //println(s"result -> $result")
    val typeClass: Option[String] =
      result.size match {
        case 0 => None
        case 1 => Some(result.head.getClass.getSimpleName)
        case _ =>
          if (result.tail.forall { p => result.head.getClass.equals(p.getClass) }) Some(result.head.getClass.getSimpleName)
          else Some(ANY)
      }
    //println(s"typeClass: $typeClass")
    validateTypes(result, typeClass, returnInsideSeqFlag)
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
    * @param keyList          List of pairs (Key,Condition) used to perform extraction according to the User.
    * @param limitList        List of Tuple3 (Range,Range,Condition) used to perform extraction according to the User.
    * @return Extracted result.
    */

  def runExtractors(encodedStructure: Either[Array[Byte], String], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    val value: List[Any] =
      keyList.size match {
        case 1 =>
          boson.extract(encodedStructure, keyList, limitList)
        case 2 if keyList.drop(1).head._2.equals(C_FILTER) =>
          boson.extract(encodedStructure, keyList, limitList)
        case _ =>
          val res: List[Any] = boson.extract(encodedStructure, keyList, limitList)
          val filtered = res.collect {
            //case buf: ByteBuf => buf
            case buf: Array[Byte] => Left(buf)
            case str: String if isJson(str) => Right(str)
             }
          //println(s"filtered -> $filtered")
          filtered.size match {
            case 0 => Nil //throw CustomException("The given path doesn't correspond with the event structure.")
            case _ =>
              val result: List[List[Any]] =
                filtered.par.map { elem =>
                  if (keyList.drop(1).head._2.equals(C_FILTER)) runExtractors(elem, keyList.drop(2), limitList.drop(2))
                  else runExtractors(elem, keyList.drop(1), limitList.drop(1))
                }.seq.toList
              result.flatten//.reduce(_ ++ _)
          }
      }
    value
  }


  def applyFunction(typesNvalues: List[(String, Any)]): Unit = {
    typesNvalues.head._2 match {
      case oneString(value) =>
        tCase.get.unapply(value).map(v => fExt.get(v)).orElse {
          val defInst: Instant = Instant.now()
          tCase.get.unapply(defInst).map(_ => fExt.get(Instant.parse(value).asInstanceOf[T]))
            .orElse(throw CustomException(s"Type designated doesn't correspond with extracted type: ${typesNvalues.head._1}"))
        }
      case seqString(seq) =>
        tCase.get.unapply(seq).map(v => fExt.get(v)).orElse {
          val defInst: Seq[Instant] = List(Instant.now())
          tCase.get.unapply(defInst).map(_ => fExt.get(seq.map(Instant.parse(_)).asInstanceOf[T]))
            .orElse(throw CustomException(s"Type designated doesn't correspond with extracted type: ${typesNvalues.head._1}"))
        }
      case _ =>
        tCase.get.unapply(typesNvalues.head._2) match {
          case Some(_) => fExt.get(typesNvalues.head._2.asInstanceOf[T])
          case None =>
            if (typesNvalues.tail.isEmpty) throw CustomException(s"Type designated doesn't correspond with extracted type: ${typesNvalues.head._1}") else applyFunction(typesNvalues.tail)
        }
    }
  }
            
 def isJson(str: String):Boolean = if((str.startsWith("{") && str.endsWith("}"))||(str.startsWith("[") && str.endsWith("]"))) true else false
            
  private def validateTypes(result: List[Any], typeClass: Option[String], returnInsideSeqFlag: Boolean): Any = {
      tCase.isDefined match {
        case true if typeClass.isDefined =>
          typeClass.get match {
            case STRING =>
              val str: List[String] = result.asInstanceOf[List[String]]
              if(returnInsideSeqFlag) applyFunction(List((STRING, str), (INSTANT, str))) else applyFunction(List((STRING, str.head), (INSTANT, str.head)))
            case INTEGER => if(returnInsideSeqFlag) applyFunction(List((INTEGER, result))) else applyFunction(List((INTEGER, result.head)))
            case DOUBLE =>
              val res = result.asInstanceOf[List[Double]]
              if(returnInsideSeqFlag) applyFunction(List((DOUBLE, res), (FLOAT, res.map(_.toFloat)))) else applyFunction(List((DOUBLE, res.head), (FLOAT, res.head.toFloat)))
            case LONG => if(returnInsideSeqFlag) applyFunction(List((LONG, result))) else applyFunction(List((LONG, result.head)))
            case BOOLEAN => if(returnInsideSeqFlag) applyFunction(List((BOOLEAN, result))) else applyFunction(List((BOOLEAN, result.head)))
            case ANY =>
              val res = result.map {
                case buf: ByteBuf => buf.array
                case elem => elem
              }
              if(returnInsideSeqFlag) applyFunction(List((ANY, res))) else applyFunction(List((ANY, res.head)))
            case ARRAY_BYTE  => constructObj(result.asInstanceOf[List[Array[Byte]]], List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
          }
        case false if typeClass.isDefined =>
          typeClass.get match {
            case ARRAY_BYTE  if fExt.isDefined =>
              val res = result.asInstanceOf[List[Array[Byte]]]//.map(_.array)
              if(returnInsideSeqFlag) fExt.get(res.asInstanceOf[T]) else fExt.get(res.head.asInstanceOf[T])
            case STRING =>
              val arrB = result.asInstanceOf[List[String]].map(java.util.Base64.getDecoder.decode(_))
              if(returnInsideSeqFlag) fExt.get(arrB.asInstanceOf[T]) else fExt.get(arrB.head.asInstanceOf[T])
          }
        case _ => fExt.get.apply(result.asInstanceOf[T]) //TODO: when no results, handle this situation differently
      }
/*    returnInsideSeqFlag match {
      case false if typeClass.isDefined && tCase.isDefined => // all single values, results exists and its types ain't byte[]
        typeClass.get match {
          case STRING =>
            val str: String = result.head.asInstanceOf[String]
            applyFunction(List((STRING, str), (INSTANT, str)))
          case INTEGER => applyFunction(List((INTEGER, result.head)))
          case DOUBLE =>
            val res = result.head.asInstanceOf[Double]
            applyFunction(List((DOUBLE, res), (FLOAT, res.toFloat)))
          case LONG => applyFunction(List((LONG, result.head)))
          case BOOLEAN => applyFunction(List((BOOLEAN, result.head)))
          case ANY =>
            val res = result.map {
              case buf: ByteBuf => buf.array
              case elem => elem
            }
            applyFunction(List((ANY, res.head)))
          case COPY_BYTEBUF => constructObj(result.asInstanceOf[List[ByteBuf]].map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
        }
      case false if typeClass.isDefined => // all single values and results exists, case of byte[]
        typeClass.get match {
          case COPY_BYTEBUF if fExt.isDefined =>
            val res = result.asInstanceOf[List[ByteBuf]].head.array
            fExt.get(res.asInstanceOf[T])
          case STRING =>
            val arrB: Array[Byte] = java.util.Base64.getDecoder.decode(result.head.asInstanceOf[String])
            fExt.get(arrB.asInstanceOf[T])
        }
      case true if typeClass.isDefined && tCase.isDefined => // all Seq(values), results exists and its types ain't Seq(byte[])
        typeClass.get match {
          case STRING =>
            val str: List[String] = result.asInstanceOf[List[String]]
            applyFunction(List((STRING, str), (INSTANT, str)))
          case INTEGER =>
            applyFunction(List((INTEGER, result)))
          case DOUBLE =>
            val res = result.asInstanceOf[List[Double]]
            applyFunction(List((DOUBLE, res), (FLOAT, res.map(_.toFloat))))
          case LONG =>
            applyFunction(List((LONG, result)))
          case BOOLEAN => applyFunction(List((BOOLEAN, result)))
          case ANY =>
            val res = result.map {
              case buf: ByteBuf => buf.array
              case elem => elem
            }
            applyFunction(List((ANY, res)))
          case COPY_BYTEBUF => constructObj(result.asInstanceOf[List[ByteBuf]].map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
        }
      case true if typeClass.isDefined => // all Seq(values), results exists and type is Seq(byte[])
        typeClass.get match {
          case COPY_BYTEBUF if fExt.isDefined =>
            val res = result.asInstanceOf[List[ByteBuf]].map(_.array)
            fExt.get(res.asInstanceOf[T])
          case STRING =>
            val arrB = result.asInstanceOf[List[String]].map(java.util.Base64.getDecoder.decode(_))
            fExt.get(arrB.asInstanceOf[T])
        }
      case _ =>
        fExt.get.apply(result.asInstanceOf[T])
    }
    */

    //    if(tCase.isDefined) {
    //      println("tCase is defined")
    //      tCase.get match {
    //        //  Single Result
    //        case oneString(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(STRING) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[String => Unit], result.head.asInstanceOf[String])
    //        case oneInstant(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(INSTANT) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Instant => Unit], Instant.parse(result.head.asInstanceOf[String]))
    //        case oneDouble(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(DOUBLE) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Double => Unit], result.head.asInstanceOf[Double])
    //        case oneFloat(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(FLOAT) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Float => Unit], result.head.asInstanceOf[Double].toFloat)
    //        case oneInt(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(INTEGER) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Int => Unit], result.head.asInstanceOf[Int])
    //        case oneLong(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(LONG) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Long => Unit], result.head.asInstanceOf[Long])
    //        case oneBoolean(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(BOOLEAN) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Boolean => Unit], result.head.asInstanceOf[Boolean])
    //        case oneAny(_) if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(ANY) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Any => Unit], result.head)
    //        case _ if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(COPY_BYTEBUF) =>  //  CaseClass => Unit
    //          constructObj(result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
    //        // Inside Seq
    //        case seqString(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(STRING) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[String] => Unit], result.asInstanceOf[Iterable[String]].toSeq)
    //        case seqInstant(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(INSTANT) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Instant] => Unit], result.asInstanceOf[Iterable[String]].toSeq.map(elem => Instant.parse(elem)))
    //        case seqDouble(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(DOUBLE) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Double] => Unit], result.asInstanceOf[Iterable[Double]].toSeq)
    //        case seqFloat(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(FLOAT) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Float] => Unit], result.asInstanceOf[Iterable[Double]].map(_.toFloat).toSeq)
    //        case seqInt(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(INTEGER) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Int] => Unit], result.asInstanceOf[Iterable[Int]].toSeq)
    //        case seqLong(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(LONG) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Long] => Unit], result.asInstanceOf[Iterable[Long]].toSeq)
    //        case seqBoolean(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(BOOLEAN) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Boolean] => Unit], result.asInstanceOf[Iterable[Boolean]].toSeq)
    //        case seqAny(_) if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(ANY) =>
    //          val res = result.toSeq.map {
    //            case buf: ByteBuf => buf.array
    //            case elem => elem
    //          }
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Any] => Unit], res)
    //        case _ if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(COPY_BYTEBUF) =>  //  CaseClass => Unit
    //          constructObj(result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
    //        case _ => throw new Exception("No results extracted.")
    //      }
    //    } else {  // case:  byte[] or Seq[byte[]]
    //      tCase match {
    //        case _ if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(COPY_BYTEBUF) && fExt.isDefined => //  Array[Byte] => Unit
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], result.asInstanceOf[Seq[ByteBuf]].head.array)
    //        case _ if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(COPY_BYTEBUF) && fExt.isDefined => //  Seq[Array[Byte]] => Unit
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array))
    //        case _ if !returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(STRING) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], java.util.Base64.getDecoder.decode(result.head.asInstanceOf[String]))
    //        case _ if returnInsideSeqFlag && typeClass.isDefined && typeClass.get.equals(STRING) =>
    //          Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], result.asInstanceOf[Iterable[String]].toSeq.map(elem =>java.util.Base64.getDecoder.decode(elem)))
    //        case _ => throw new Exception("No results extracted.")
    //      }
    //    }

    //    if (returnInsideSeqFlag) {
    //      if (typeClass.isDefined) {
    //        typeClass.get match {
    //          case STRING if fExt.isDefined =>
    //            val res: Seq[String] = result.asInstanceOf[Iterable[String]].toSeq
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[String] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Instant] => Unit], res.map(elem => Instant.parse(elem)))) match {
    //                case Success(_) =>
    //                case Failure(_) =>
    //                  val extracted: Seq[Array[Byte]] = res.map(elem =>java.util.Base64.getDecoder.decode(elem))
    //                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], extracted)) match {
    //                    case Success(_) =>
    //                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //                  }
    //              }
    //            }
    //          case INTEGER if fExt.isDefined =>
    //            val res: Seq[Int] = result.asInstanceOf[Iterable[Int]].toSeq
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Int] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //            }
    //          case LONG if fExt.isDefined =>
    //            val res: Seq[Long] = result.asInstanceOf[Iterable[Long]].toSeq
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Long] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //            }
    //          case BOOLEAN if fExt.isDefined =>
    //            val res: Seq[Boolean] = result.asInstanceOf[Iterable[Boolean]].toSeq
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Boolean] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //            }
    //          case DOUBLE if fExt.isDefined =>
    //            val res: Seq[Double] = result.asInstanceOf[Iterable[Double]].toSeq
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Double] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Float] => Unit], res.map(_.toFloat))) match {
    //                case Success(_) =>
    //                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //              }
    //            }
    //          case COPY_BYTEBUF if fExt.isDefined =>
    //            //println("Seq[byte[]], fExt.isDefined")
    //            val res = result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array)
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) =>  throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //            }
    //          case COPY_BYTEBUF =>
    //            constructObj(result.asInstanceOf[Iterable[ByteBuf]].toSeq.map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
    //          case ANY =>
    //            val res: Seq[Any] = result.toSeq.map {
    //              case buf: ByteBuf => buf.array
    //              case elem => elem
    //            }
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Any] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
    //            }
    //        }
    //      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    //    } else {
    //      //println("return without Seq")
    //      if (typeClass.isDefined) {
    //        typeClass.get match {
    //          case STRING if fExt.isDefined =>
    ////            println("before")
    //            val res: String = result.head.asInstanceOf[String]
    ////            Transform.toPrimitive[String](fExt.get,res)
    ////            println("after")
    //
    //
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[String => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Instant => Unit], Instant.parse(res))) match {
    //                case Success(_) =>
    //                case Failure(_) =>
    //                  val extracted: Array[Byte] = java.util.Base64.getDecoder.decode(res)
    //                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], extracted)) match {
    //                    case Success(_) =>
    //                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //                  }
    //              }
    //            }
    //          case INTEGER if fExt.isDefined =>
    //            val res: Int = result.head.asInstanceOf[Int]
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Int => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //            }
    //          case LONG if fExt.isDefined =>
    //            val res: Long = result.head.asInstanceOf[Long]
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Long => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //            }
    //          case BOOLEAN if fExt.isDefined =>
    //            val res: Boolean = result.head.asInstanceOf[Boolean]
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Boolean => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //            }
    //          case DOUBLE if fExt.isDefined =>
    //            val res: Double = result.head.asInstanceOf[Double]
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Double => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Float => Unit], res.toFloat)) match {
    //                case Success(_) =>
    //                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //              }
    //            }
    //          case COPY_BYTEBUF if fExt.isDefined =>
    //            val res = result.asInstanceOf[Seq[ByteBuf]].head.array
    //            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], res)) match {
    //              case Success(_) =>
    //              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
    //            }
    //          case COPY_BYTEBUF =>
    //            constructObj(result.asInstanceOf[Seq[ByteBuf]].map(_.array), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
    //        }
    //      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    //    }
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
    ???
    //executeMultipleKeysInjector(zipped)
  }

  //  TODO: replace Statement -> Statement, etc..
//    private def executeMultipleKeysInjector(statements: List[(Statement, String)]): Array[Byte] = {
//      val value = Left(boson.getByteBuf)
//      val result: Array[Byte] =
//        Try(boson.execStatementPatternMatch(boson.getByteBuf, statements, fInj.get)) match {
//          case Success(v) =>
//            //val bsResult: bsonValue.BsValue = bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
//            //v.release()
//            //bsResult
//            v.array
//          case Failure(e) =>
//            throw CustomException(e.getMessage)
//          //bsonValue.BsException(e.getMessage)
//        }
//      //boson.getByteBuf.release()
//      result
//    }
//  }
}

