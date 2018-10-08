package io.zink.boson.bson.bsonPath

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary.{oneString, _}
import io.zink.boson.bson.bsonImpl._
import io.zink.boson.bson.value.Value
import shapeless.TypeCase

import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */

/**
  * Class that handles both processes of Injection and Extraction.
  *
  * @param fInj Function used in Injection process.
  * @param fExt Function used in Extraction process.
  * @tparam T Type specified by the User.
  */
class Interpreter[T](expression: String,
                     fInj: Option[T => T] = None,
                     vInj: Option[Value] = None,
                     fExt: Option[T => Unit] = None)(implicit tCase: Option[TypeCase[T]], convertFunction: Option[List[(String, Any)] => T] = None) {

  val parsedStatements: ProgStatement = new DSLParser(expression).Parse().fold(excp => throw excp, parsedStatements => parsedStatements)

  val (keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]) =
    buildExtractors(parsedStatements.statementList.head, parsedStatements.statementList.tail, parsedStatements.dotsList)

  val returnInsideSeqFlag: Boolean = returnInsideSeq(keyList, limitList, parsedStatements.dotsList)

  private val isJsonObjOrArrExtraction = (strgs: List[String], returnInsideSeqFlag: Boolean, tCase: Option[TypeCase[T]]) => {
    strgs.nonEmpty && ((returnInsideSeqFlag && tCase.get.unapply(strgs).isEmpty) || (!returnInsideSeqFlag && tCase.get.unapply(strgs.head).isEmpty)) && (strgs.head.startsWith("{") || strgs.head.startsWith("["))
  }

  /**
    * BuildExtractors takes a statementList provided by the parser and transforms it into two lists used to extract.
    *
    * @param statementList List of statements used to create the pairs of (Key,Condition) and (Range,Condition)
    * @return Tuple with (KeyList,LimitList)
    */
  private def buildExtractors(firstStatement: Statement, statementList: List[Statement], dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList, limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      firstStatement match {
        case Key(key) if dotsList.head.equals(C_DOT) => if (statementList.nonEmpty) (List((key, C_NEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_LEVEL)), List((None, None, EMPTY_KEY)))
        case Key(key) => if (statementList.nonEmpty) (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
        case KeyWithArrExpr(key, arrEx) if dotsList.head.equals(C_DOT) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case ArrExpr(l, m, r) if dotsList.head.equals(C_DOT) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
        case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
        case HalfName(halfName) =>
          halfName.equals(STAR) match {
            case true => (List((halfName, C_ALL)), List((None, None, STAR)))
            case false if statementList.nonEmpty && dotsList.head.equals(C_DOT) => (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
            case false if statementList.nonEmpty => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
            case false if dotsList.head.equals(C_DOT) => (List((halfName, C_LEVEL)), List((None, None, EMPTY_KEY)))
            case false => (List((halfName, C_ALLDOTS)), List((None, None, EMPTY_KEY)))
          }
        case HasElem(key, elem) if dotsList.head.equals(C_DOT) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case ROOT => (List((C_DOT, C_DOT)), List((None, None, EMPTY_RANGE)))
      }

    if (statementList.nonEmpty) {
      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for {
          statement <- statementList.zip(dotsList.tail)
        } yield {
          statement._1 match {
            case Key(key) if statement._2.equals(C_DOT) => (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
            case Key(key) => (List((key, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
            case KeyWithArrExpr(key, arrEx) if statement._2.equals(C_DOT) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMIT)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case ArrExpr(l, m, r) if statement._2.equals(C_DOT) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
            case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMIT)), defineLimits(l, m, r))
            case HalfName(halfName) =>
              halfName.equals(STAR) match {
                case true => (List((halfName, C_ALL)), List((None, None, STAR)))
                case false if dotsList.head.equals(C_DOT) => (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
                case false => (List((halfName, C_ALLNEXT)), List((None, None, EMPTY_KEY)))
              }
            case HasElem(key, elem) if statement._2.equals(C_DOT) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
            case HasElem(key, elem) => (List((key, C_LIMIT), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))

          }
        }
      val secondList: List[(String, String)] = firstList ++ forList.flatMap(_._1)
      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(_._2)

      statementList.last match {
        case HalfName(halfName) if !halfName.equals(STAR) && dotsList.last.equals(C_DOT) => (secondList.take(secondList.size - 1) ++ List((halfName, C_LEVEL)), limitList2)
        case HalfName(halfName) if !halfName.equals(STAR) => (secondList.take(secondList.size - 1) ++ List((halfName, C_ALL)), limitList2)
        case Key(k) if dotsList.last.equals(C_DOT) => (secondList.take(secondList.size - 1) ++ List((k, C_LEVEL)), limitList2)
        case Key(k) => (secondList.take(secondList.size - 1) ++ List((k, C_ALL)), limitList2)
        case _ => (secondList, limitList2)
      }
    } else {
      (firstList, limitList1)
    }

  }

  /**
    * DefineLimits takes a set of arguments that represent a range defined by the User through BsonPath and transforms it
    * into a Tuple3.
    *
    * @param left  Integer representing the lower limit of a Range.
    * @param mid   String indicating which type of Range it is.
    * @param right Integer representing the upper limit of a Range.
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
    * @param limitList List of Tuple3 with Ranges and Conditions
    * @return Boolean
    */
  private def returnInsideSeq(keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)], dotsList: Seq[String]): Boolean =
    limitList.exists { elem =>
      elem._1.isDefined match {
        case true if elem._2.isEmpty => if (elem._3.equals(C_END)) false else true
        case true if elem._2.isDefined && elem._2.get != elem._1.get => true
        case true if elem._2.isDefined && elem._2.get == elem._1.get => false
        case false => false
      }
    } || dotsList.exists(e => e.equals(C_DOUBLEDOT)) || keyList.exists(e => e._2.equals(C_FILTER) || e._1.equals(STAR))

  /**
    * Run is the only public method of the object Interpreter and depending on which function it was instantiated with it chooses whether it starts
    * an injection or an extraction process.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), in the case of an Injection it returns the modified event as an encoded Array[Byte].
    */
  def run(bsonEncoded: Either[Array[Byte], String]): Any = if (fInj.isDefined || vInj.isDefined) startInjector(bsonEncoded) else start(bsonEncoded)

  /**
    * Method that initiates the process of extraction based on a Statement list provided by the parser.
    *
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  private def start(bsonEncoded: Either[Array[Byte], String]): Any = {
    bsonEncoded match {
      case Left(byteArr) =>
        val buf: ByteBuf = Unpooled.copiedBuffer(byteArr)
        extract(Left(buf), keyList, limitList)
      case Right(jsonString) => extract(Right(jsonString), keyList, limitList)
    }
  }

  /**
    * This method does the final extraction of an Object.
    *
    * @param encodedEither Sequence of BsonObjects encoded.
    * @param keyList       Pairs of Keys and Conditions used to decode the encodedSeqByteArray
    * @param limitList     Pairs of Ranges and Conditions used to decode the encodedSeqByteArray
    * @return List of Tuples corresponding to pairs of Key and Value used to build case classes
    */
  private def constructObj(encodedEither: Either[List[ByteBuf], List[String]], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Seq[List[(String, Any)]] = {
    encodedEither match {
      case Left(bufList) => bufList.par.map { encoded =>
        val res: List[Any] = runExtractors(Left(encoded), keyList, limitList)
        res match {
          case tuples(list) => list
          case _ => throw CustomException("Error building tuples to fulfill case class.")
        }
      }.seq
      case Right(stringList) => stringList.par.map { encoded =>
        val res: List[Any] = runExtractors(Right(encoded), keyList, limitList)
        res match {
          case tuples(list) => list
          case _ => throw CustomException("Error building tuples to fulfill case class.")
        }
      }.seq
    }
  }

  /**
    * Extract is the method which puts together the process of extraction and applies the function provided by the User
    * or, in case of Object extraction, provides a list of pairs (Key,Value) extracted from the desired Object.
    *
    * @param encodedStructure data structure that encodes the event
    * @return On an extraction of an Object it returns a list of pairs (Key,Value), the other cases doesn't return anything.
    */
  private def extract(encodedStructure: Either[ByteBuf, String], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Any = {
    val result: List[Any] = runExtractors(encodedStructure, keyList, limitList)
    val typeClass: Option[String] =
      result.size match {
        case 0 => None
        case 1 => Some(result.head.getClass.getSimpleName)
        case _ =>
          if (result.tail.forall { p => result.head.getClass.equals(p.getClass) }) Some(result.head.getClass.getSimpleName)
          else Some(ANY)
      }
    validateTypes(result, typeClass, returnInsideSeqFlag)
  }

  /**
    * RunExtractors is the method that iterates over KeyList, LimitList and encodedStructure doing the bridge
    * between Interpreter with BosonImpl.
    *
    * @param encodedStructure data structure that encodes the event.
    * @param keyList          List of pairs (Key,Condition) used to perform extraction according to the User.
    * @param limitList        List of Tuple3 (Range,Range,Condition) used to perform extraction according to the User.
    * @return Extracted result.
    */
  def runExtractors(encodedStructure: Either[ByteBuf, String], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = keyList.size match {
    case 1 =>
      BosonImpl.extract(encodedStructure, keyList, limitList)

    case 2 if keyList.drop(1).head._2.equals(C_FILTER) =>
      BosonImpl.extract(encodedStructure, keyList, limitList)

    case _ =>
      val filtered: Seq[Either[ByteBuf, String]] =
        BosonImpl.extract(encodedStructure, keyList, limitList) collect {
          case buf: ByteBuf => Left(buf)
          case str: String if isJson(str) => Right(str)
        }

      filtered.size match {
        case 0 => Nil
        case _ =>
          filtered.par.flatMap { elem =>
            if (keyList.drop(1).head._2.equals(C_FILTER)) runExtractors(elem, keyList.drop(2), limitList.drop(2))
            else runExtractors(elem, keyList.drop(1), limitList.drop(1))
          }.toList
      }
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

  /**
    * Function used to determine if an input string is a Json string
    *
    * @param str - the input string
    * @return a boolean containing the information wether the string is a Json(true) string or not(false)
    */
  def isJson(str: String): Boolean = if ((str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"))) true else false

  private def validateTypes(result: List[Any], typeClass: Option[String], returnInsideSeqFlag: Boolean): Any = {
    tCase.isDefined match {
      case true if typeClass.isDefined =>
        typeClass.get match {

          case STRING =>
            val str: List[String] = result.asInstanceOf[List[String]]
            if (isJsonObjOrArrExtraction(str, returnInsideSeqFlag, tCase))
              constructObj(Right(str), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE)))
            else
              str.foreach(res => applyFunction(List((STRING, res), (INSTANT, res))))


          case INTEGER => result.foreach(res => applyFunction(List((INTEGER, res))))

          case DOUBLE =>
            val res = result.asInstanceOf[List[Double]]
            res.foreach(num => applyFunction(List((DOUBLE, num), (FLOAT, num.toFloat))))

          case LONG => result.foreach(res => applyFunction(List((LONG, res))))

          case BOOLEAN => result.foreach(res => applyFunction(List((BOOLEAN, res))))

          case ANY =>
            val res = result.map {
              case buf: ByteBuf => buf.array
              case elem => elem
            }
            res.foreach(obj => applyFunction(List((ANY, obj))))

          case COPY_BYTEBUF => constructObj(Left(result.asInstanceOf[List[ByteBuf]]), List((STAR, C_BUILD)), List((None, None, EMPTY_RANGE))) //TODO check this case

        }
      case false if typeClass.isDefined =>
        typeClass.get match {
          case COPY_BYTEBUF if fExt.isDefined =>
            val res = result.asInstanceOf[List[ByteBuf]].map(_.array)
            res.foreach(byteArr => fExt.get(byteArr.asInstanceOf[T]))
          case STRING =>

            val res = result.asInstanceOf[List[String]].map(java.util.Base64.getDecoder.decode(_))
            res.foreach(byteArr => fExt.get(byteArr.asInstanceOf[T]))
        }

      case _ => //fExt.get.apply(result.asInstanceOf[T]) //TODO: when no results, handle this situation differently
    }
  }

  /**
    * Method that initiates the process of injection based on a Statement list provided by the parser
    *
    * @param bsonEncoded - encoded structured to be modified
    * @return Either an Array of Byte or a String containing the resulting structure of the injection
    */
  private def startInjector(bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] = {
    val zipped = parsedStatements.statementList.zip(parsedStatements.dotsList)
    if(fInj.isDefined) executeMultipleKeysInjector(zipped, bsonEncoded)
    else executeValueInjection(zipped, bsonEncoded)
  }

  /**
    * Function that handles the injection
    *
    * @param statements  - List of statements provided by the parser
    * @param bsonEncoded - encoded structured to be modified
    * @return a structure with the result of the injection
    */
  private def executeMultipleKeysInjector(statements: List[(Statement, String)], bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] = {
    val input: Either[ByteBuf, String] = bsonEncoded match {
      case Left(byteArr) =>
        val buf: ByteBuf = Unpooled.copiedBuffer(byteArr) //TODO - This line is consuming a lot of CPU
        Left(buf)
      case Right(jsString) => Right(jsString)
    }
//    Try(BosonImpl.inject(input, statements, fInj.get)) match {
//      case Success(resultCodec) =>
//        resultCodec.getCodecData match {
//          case Left(byteBuf) => Left(byteBuf.array)
//          case Right(string) => Right(string)
//        }
//      case Failure(exception) => throw CustomException(exception.getMessage)
//    }

    BosonImpl.inject(input, statements, fInj.get).getCodecData match {
                case Left(byteBuf) => Left(byteBuf.array)
                case Right(string) => Right(string)
              }
  }

  private def executeValueInjection(statements: List[(Statement, String)], bsonEncoded: Either[Array[Byte], String]): Either[Array[Byte], String] = {
    val input: Either[ByteBuf, String] = bsonEncoded match {
      case Left(byteArr) =>
        val buf: ByteBuf = Unpooled.copiedBuffer(byteArr)
        Left(buf)
      case Right(jsString) => Right(jsString)
    }

    BosonImpl.injectValue(input, statements, vInj.get).getCodecData match {
      case Left(byteBuf) => Left(byteBuf.array)
      case Right(string) => Right(string)
    }
  }

}

