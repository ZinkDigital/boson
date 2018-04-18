package io.zink.boson.bson.bsonImpl

import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.time.Instant
import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
/**
  * This class encapsulates one Netty ByteBuf
  *
  */
case class CustomException(smth: String) extends Exception {
  override def getMessage: String = smth
}

class BosonImpl(
                 byteArray: Option[Array[Byte]] = None,
                 javaByteBuf: Option[ByteBuffer] = None
               ) {

  private val valueOfArgument: String =
    if (javaByteBuf.isDefined) {
      javaByteBuf.get.getClass.getSimpleName
    } else if (byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    } else EMPTY_CONSTRUCTOR

  private val nettyBuffer: ByteBuf = valueOfArgument match {
    case ARRAY_BYTE =>
      val b: ByteBuf = Unpooled.copiedBuffer(byteArray.get)
      b
    case JAVA_BYTEBUFFER =>
      val buff: ByteBuffer = javaByteBuf.get
      if (buff.position() != 0) {
        buff.position(0)
        val b: ByteBuf = Unpooled.copiedBuffer(buff)
        buff.clear()
        b
      } else {
        val b: ByteBuf = Unpooled.copiedBuffer(javaByteBuf.get)
        javaByteBuf.get.clear()
        b
      }
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
  }

  private val eObjPrimitiveConditions: List[(String, String)] => Boolean =
    keyList => {
      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)
    }

  private val eObjConditions: List[(String, String)] => Boolean =
    keyList => {
      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_LIMITLEVEL)
    }

  lazy val buildExtractorsLists: (Statement,List[Statement],List[String]) => (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
    (firstStatement,statementList,dotsList) => buildExtractors(firstStatement,statementList,dotsList)

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

  def extract[T](netty1: ByteBuf, keyList: List[(String, String)],
                 limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    /*
    CODEC here
     */
    val netty: ByteBuf = netty1.duplicate()
    val startReaderIndex: Int = netty.readerIndex()
    Try(netty.getIntLE(startReaderIndex)) match {
      case Success(value) =>
        val size: Int = value
        val seqType: Int = netty.getByte(startReaderIndex + 4).toInt
        seqType match {
          case 0 => List()
          case _ =>
            netty.getByte(startReaderIndex + 5).toInt match {
              case 48 =>
                netty.readIntLE()
                val arrayFinishReaderIndex: Int = startReaderIndex + size
                keyList.head._1 match {
                  case C_DOT if keyList.lengthCompare(1) == 0 =>
                    //val arr: Array[Byte] = new Array[Byte](size)
                    val buf = netty.copy(startReaderIndex,size)
                    //netty.getBytes(startReaderIndex, arr, 0, size)
                    netty.readerIndex(arrayFinishReaderIndex)
                    List(buf)
                  case _ =>
                    val midResult = extractFromBsonArray(netty, size, arrayFinishReaderIndex, keyList, limitList)
                    if (midResult.isEmpty) List() else resultComposer(midResult)
                }
              case _ =>
                netty.readIntLE()
                val bsonFinishReaderIndex: Int = startReaderIndex + size
                keyList.head._1 match {
                  case EMPTY_KEY if keyList.head._2.equals(C_LIMITLEVEL) => List()
                  case C_DOT if keyList.lengthCompare(1) == 0 =>
                    //val arr: Array[Byte] = new Array[Byte](size)
                    val buf = netty.copy(startReaderIndex,size)
                    //netty.getBytes(startReaderIndex, arr, 0, size)
                    netty.readerIndex(bsonFinishReaderIndex)
                    List(buf)
                  case _ if keyList.head._2.equals(C_BUILD) =>
                    val midResult = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList).asInstanceOf[List[List[(String, Any)]]].flatten
                    midResult
                  case _ =>
                    val midResult = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    if (midResult.isEmpty) List() else resultComposer(midResult)
                }
            }
        }
      case Failure(msg) =>
        throw new RuntimeException(msg)
    }

  }

  // Extracts the value of a key inside a BsonObject
  private def extractFromBsonObj[T](netty: ByteBuf, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value: Double = netty.readDoubleLE()
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(8)
            None
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val valueLength: Int = netty.readIntLE()
            val arr: Array[Byte] = Unpooled.copiedBuffer(netty.readCharSequence(valueLength, charset), charset).array()
            val newArr: Array[Byte] = arr.filter(b => b != 0)
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, new String(newArr)))
              case _ => Some(new String(newArr))
            }
          } else {
            netty.skipBytes(netty.readIntLE())
            None
          }
        case D_BSONOBJECT =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjConditions(keyList)) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            //val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            keyList.head._2 match {
              case C_ALLNEXT | C_ALLDOTS =>
                //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                val buf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                Some(resultComposer(List(List(buf), resultComposer(extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList)))))
              case C_BUILD =>
                val res = extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList).asInstanceOf[List[List[(String, Any)]]].flatten
                Some(Iterable(key, res))
              case _ =>
                //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                val buf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                netty.readerIndex(bFnshRdrIndex)
                Some(buf)
            }
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case _ =>
                val midResult: List[Any] = extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult))
            }
          }
        case D_BSONARRAY =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            //val arr: Array[Byte] = new Array[Byte](valueLength)
            keyList.head._2 match {
              case C_BUILD =>
                val res = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
                Some(List(key, res))
              case C_ALLNEXT | C_ALLDOTS =>
                val buf = netty.copy(arrayStartReaderIndex,valueLength)
                //netty.getBytes(arrayStartReaderIndex, arr, 0, valueLength)
                val res = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                Some(resultComposer(List(List(buf), resultComposer(res))))
              case C_LIMIT | C_LIMITLEVEL =>
                val midResult = traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult))
              case _ =>
                val buf = netty.copy(arrayStartReaderIndex,valueLength)
                netty.readerIndex(arrayFinishReaderIndex)
                Some(buf)
            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case _ =>
                val midResult: List[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult))
            }
          }
        case D_BOOLEAN =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value: Int = netty.readByte()
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, value == 1))
              case _ => Some(value == 1)
            }
          } else {
            netty.skipBytes(1)
            None
          }
        case D_NULL =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, V_NULL))
              case _ => Some(V_NULL)
            }
          } else {
            None
          }
        case D_INT =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value: Int = netty.readIntLE()
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(4)
            None
          }
        case D_LONG =>
          val (matched: Boolean, key: String) = compareKeys(netty, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value: Long = netty.readLongLE()
            keyList.head._2 match {
              case C_BUILD => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(8)
            None
          }
        case D_ZERO_BYTE =>
          None
      }
    val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
    finalValue match {
      case None =>
        actualPos match {
          case x if x > 0 => extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
          case 0 => List()
        }
      case Some(_) if keyList.head._2.equals(C_BUILD) =>
        actualPos match {
          case x if x > 0 =>
            List(finalValue.get) ++ extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
          case 0 => List(finalValue.get)
        }
      case Some(value) if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) || (keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.eq(STAR)) => //keyList.head._2.equals("first") ||
        netty.readerIndex(bsonFinishReaderIndex)
        List(value)
      case Some(_) =>
        List(finalValue.get) ++ extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
    }
  }

  // Traverses the BsonArray looking for BsonObject or another BsonArray
  private def extractFromBsonArray[T](netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    keyList.head._1 match {
      case EMPTY_KEY | STAR =>
        traverseBsonArray(netty, length, arrayFRIdx, keyList, limitList)
      case _ if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) => List()
      case _ if keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.equals(EMPTY_KEY) => List()
      case _ =>
        val seqType2: Int = netty.readByte().toInt
        if (seqType2 != 0) {
          readArrayPos(netty)
        }
        val finalValue: Option[Any] =
          seqType2 match {
            case D_FLOAT_DOUBLE =>
              netty.readDoubleLE()
              None
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              val valueLength: Int = netty.readIntLE()
              netty.readCharSequence(valueLength, charset)
              None
            case D_BSONOBJECT =>
              val bsonStartReaderIndex: Int = netty.readerIndex()
              val valueTotalLength: Int = netty.readIntLE()
              val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
              val midResult: List[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
              if (midResult.isEmpty) None else Some(resultComposer(midResult))
            case D_BSONARRAY =>
              val startReaderIndex: Int = netty.readerIndex()
              val valueLength2: Int = netty.readIntLE()
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              val midResult: List[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
              if (midResult.isEmpty) None else Some(resultComposer(midResult))
            case D_BOOLEAN =>
              netty.readByte()
              None
            case D_NULL =>
              None
            case D_INT =>
              netty.readIntLE()
              None
            case D_LONG =>
              netty.readLongLE()
              None
            case D_ZERO_BYTE =>
              None
          }
        val actualPos2: Int = arrayFRIdx - netty.readerIndex()
        finalValue match {
          case None =>
            actualPos2 match {
              case x if x > 0 => extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
              case 0 => List()
            }
          case Some(_) =>
            actualPos2 match {
              case x if x > 0 =>
                List(finalValue.get) ++ extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
              case 0 => List(finalValue.get)
            }
        }
    }
  }

  private def compareKeys(netty: ByteBuf, key: String): (Boolean, String) = {
    val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
    (key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep | isHalfword(key, new String(arrKeyExtract.toArray)), new String(arrKeyExtract.toArray))
  }

  private def resultComposer(list: List[Any]): List[Any] = {
    list match {
      case List() => List.empty[Any]
      case x +: List() if x.isInstanceOf[List[Any]] => (resultComposer(x.asInstanceOf[List[Any]]) +: List()).flatten
      case x +: List() => x +: List.empty[Any]
      case x +: xs if x.isInstanceOf[List[Any]] => (resultComposer(x.asInstanceOf[List[Any]]) +: resultComposer(xs) +: List()).flatten
      case x +: xs => ((x +: List()) +: resultComposer(xs) +: List()).flatten
    }
  }

  // Constructs a new BsonArray with limits
  private def traverseBsonArray[T](netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

    def constructWithLimits(iter: Int): Iterable[Any] = {
      val seqType2: Int = netty.readByte().toInt
      if (seqType2 != 0) {
        readArrayPos(netty)
      }
      val newSeq: Option[Any] =
        seqType2 match {
          case D_FLOAT_DOUBLE =>
            val value: Double = netty.readDoubleLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(value))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value)
                      case _ if keyList.head._1.equals(STAR) => Some(value)
                      case _ => None
                    }
                }
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            val valueLength: Int = netty.readIntLE()
            val field: CharSequence = netty.readCharSequence(valueLength, charset)
            val newField: String = field.toString.filter(p => p != 0)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(newField))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(newField))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(newField)
                      case _ if keyList.head._1.equals(STAR) => Some(newField)
                      case _ => None
                    }
                }
            }
          case D_BSONOBJECT =>
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            //val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                //Some(resultComposer(Seq(Seq(arr), extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList).toSeq)))
                Some(resultComposer(List(List(buf), extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList))))
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                netty.readerIndex(bsonFinishReaderIndex)
                //Some(Seq(arr))
                Some(List(buf))
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                if (res.isEmpty) None else Some(res)
              case Some(_) =>
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
                    val res = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    netty.getByte(bsonFinishReaderIndex).toInt match {
                      case 0 =>
                        //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                        val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                        //Some(resultComposer(Seq(Seq(arr), res.toSeq)))
                        Some(resultComposer(List(List(buf), res)))
                      case _ => Some(resultComposer(res))
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                    val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                    val res = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    //Some(resultComposer(List(Seq(arr), res)))
                    Some(resultComposer(List(List(buf), res)))
                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
                    netty.getByte(bsonFinishReaderIndex).toInt match {
                      case 0 =>
                        //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                        val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                        netty.readerIndex(bsonFinishReaderIndex)
                        //Some(Seq(arr))
                        Some(List(buf))
                      case _ =>
                        netty.readerIndex(bsonFinishReaderIndex)
                        None
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                    val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                    netty.readerIndex(bsonFinishReaderIndex)
                    //Some(Seq(arr))
                    Some(List(buf))
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    val res: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    if (res.isEmpty) None else Some(res)
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_LIMIT | C_LIMITLEVEL if keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        val copyNetty1: ByteBuf = netty.duplicate()
                        val midResult = findElements(copyNetty1, keyList, limitList, bsonStartReaderIndex, bsonFinishReaderIndex)
                        if (midResult.isEmpty) {
                          netty.readerIndex(bsonFinishReaderIndex)
                          None
                        } else {
                          netty.readerIndex(bsonFinishReaderIndex)
                          Some(resultComposer(midResult))
                        }
                      case C_BUILD =>
                        val res = extractFromBsonObj(netty, List((STAR, C_BUILD)), bsonFinishReaderIndex, List((None, None, EMPTY_RANGE))).asInstanceOf[List[List[(String, Any)]]].flatten
                        Some(res)
                      case _ =>
                        //netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                        val buf: ByteBuf = netty.copy(bsonStartReaderIndex,valueTotalLength)
                        netty.readerIndex(bsonFinishReaderIndex)
                        //Some(arr)
                        Some(buf)
                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            //val arr: Array[Byte] = new Array[Byte](valueLength2)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                //Some(resultComposer(Seq(Seq(arr), extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList).toSeq)))
                Some(resultComposer(List(List(buf), extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList))))
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                netty.readerIndex(finishReaderIndex)
                //Some(Seq(arr))
                Some(List(buf))
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                if (res.isEmpty) None else Some(res)
              case Some(_) =>
                netty.readerIndex(finishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
                    val res = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                    netty.getByte(finishReaderIndex).toInt match {
                      case 0 =>
                        //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                        val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                        //Some(resultComposer(Seq(Seq(arr), res.toSeq)))
                        Some(resultComposer(List(List(buf), res)))
                      case _ => Some(resultComposer(res))
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    val res = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                    //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                    val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                    //Some(resultComposer(Seq(Seq(arr), res.toSeq)))
                    Some(resultComposer(List(List(buf), res)))
                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
                    netty.getByte(finishReaderIndex).toInt match {
                      case 0 =>
                        //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                        val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                        netty.readerIndex(finishReaderIndex)
                        Some(List(buf))
                        //Some(Seq(arr))
                      case _ =>
                        netty.readerIndex(finishReaderIndex)
                        None
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                    val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                    netty.readerIndex(finishReaderIndex)
                    //Some(Seq(arr))
                    Some(List(buf))
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    val res: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                    if (res.isEmpty) None else Some(res)
                  case Some(_) =>
                    netty.readerIndex(finishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val res = extractFromBsonArray(netty, valueLength2, finishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
                        Some(List(res))
                      case C_LIMIT | C_LIMITLEVEL if keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        netty.readerIndex(finishReaderIndex)
                        None
                      case _ =>
                        //netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                        val buf: ByteBuf = netty.copy(startReaderIndex,valueLength2)
                        netty.readerIndex(finishReaderIndex)
                        //Some(arr)
                        Some(buf)
                    }
                }
            }
          case D_BOOLEAN =>
            val value: Int = netty.readByte()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(value == 1))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(value == 1))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value == 1)
                      case _ if keyList.head._1.equals(STAR) => Some(value == 1)
                      case _ => None
                    }
                }
            }
          case D_NULL =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(V_NULL))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(V_NULL))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(V_NULL)
                      case _ if keyList.head._1.equals(STAR) => Some(V_NULL)
                      case _ => None
                    }
                }
            }
          case D_INT =>
            val value: Int = netty.readIntLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(value))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value)
                      case _ if keyList.head._1.equals(STAR) => Some(value)
                      case _ => None
                    }
                }
            }
          case D_LONG =>
            val value: Long = netty.readLongLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                Some(List(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 => Some(List(value))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value)
                      case _ if keyList.head._1.equals(STAR) => Some(value)
                      case _ => None
                    }
                }
            }
          case D_ZERO_BYTE =>
            None
        }
      val actualPos2: Int = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          newSeq ++ constructWithLimits(iter + 1)
        case 0 =>
          newSeq
      }
    }

    val seq: List[Any] = constructWithLimits(0).toList
    limitList.head._3 match {
      case UNTIL_RANGE => seq.take(seq.size - 1)
      case _ => seq
    }
  }

  /*private def  goThroughArrayWithLimit(netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] =  {

    val errorAnalyzer: (String) => Option[Char] = {
      case UNTIL_RANGE | TO_RANGE => Some(WARNING_CHAR)
      case _ => None
    }

    def goThrough(iter: Int, flag: Option[Boolean] = None): Iterable[Any] = {
      val seqType2: Int = netty.readByte().toInt
      if (seqType2 != 0) {
        readArrayPos(netty)
      }
      val finalValue: Option[Any] =
        seqType2 match {
          case D_FLOAT_DOUBLE =>
            netty.readDoubleLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            val valueLength: Int = netty.readIntLE()
            netty.readCharSequence(valueLength, charset)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_BSONOBJECT =>
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1)) //check this drop
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
              case Some(_) =>
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if limitList.head._3.equals(C_END) =>
                    arrayFRIdx - bsonFinishReaderIndex match {
                      case 1 =>
                        val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      case _ =>
                        netty.readerIndex(bsonFinishReaderIndex)
                        None
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                    if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case (C_LIMIT | C_LIMITLEVEL) if keyList.size >1 && keyList.drop(1).head._2.equals(C_FILTER) && !limitList.head._3.equals(STAR) =>
                        val copyNetty1: ByteBuf = netty.duplicate()
                        val copyNetty2: ByteBuf = netty.duplicate()
                        val res: Iterable[Any] = findElements(copyNetty1,copyNetty2, keyList, limitList, bsonStartReaderIndex, bsonFinishReaderIndex)  // keylist still with ("",...)("",filter)
                        if(res.isEmpty) {
                          netty.readerIndex(bsonFinishReaderIndex)
                          None
                        } else {
                          netty.readerIndex(bsonFinishReaderIndex)
                          Some(resultComposer(res.toVector))
                        }
                      case (C_LEVEL | C_ALL | C_LIMITLEVEL | C_LIMIT) =>
                        val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      case C_NEXT if keyList.head._1.equals(STAR) =>
                        val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            if(keyList.head._1.equals(EMPTY_KEY)) {  //case [#..#].[#..#]
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.size match {
                    case 1 =>
                      Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))) match {
                        case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                        case Some(x) => Some(x.toVector)
                      }
                    case _ =>
                      val midResult: Iterable[Any] = goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))
                      if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                  }
                case Some(_) =>
                  netty.readerIndex(finishReaderIndex)
                  None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if limitList.head._3.equals(C_END) =>
                      arrayFRIdx - finishReaderIndex match {
                        case 1 =>
                          keyList.size match {
                            case 1 =>
                              val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                              if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                            case _ =>
                              val midResult: Iterable[Any] = goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))
                              if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                          }
                        case _ =>
                          netty.readerIndex(finishReaderIndex)
                          None
                      }
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.size match {
                        case 1 =>
                          val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                          if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                        case _ =>
                          val midResult: Iterable[Any] = goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))
                          if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      }
                    case Some(_) =>
                      netty.readerIndex(finishReaderIndex)
                      None
                  }
              }
            } else {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._1 match {
                    case STAR if keyList.size < 2 =>
                      Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))) match {
                        case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                        case Some(x) => Some(x.toVector)
                      }
                    case STAR if keyList.size > 1 =>
                      Some(goThroughArrayWithLimit(netty,valueLength2,finishReaderIndex,keyList.drop(1),limitList.drop(1))) match {
                        case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                        case Some(x) => Some(resultComposer(x.toVector))
                      }
                    case _ if keyList.head._2.equals(C_NEXT) | keyList.head._2.equals(C_LEVEL) => errorAnalyzer(limitList.head._3)
                    case _ =>
                      val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                      if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                  }
                case Some(_) =>
                  netty.readerIndex(finishReaderIndex)
                  None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if limitList.head._3.equals(C_END) =>
                      arrayFRIdx - finishReaderIndex match {
                        case 1 =>
                          keyList.head._1 match {
                            case STAR if keyList.size < 2 =>
                              Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))) match {
                                case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                                case Some(x) => Some(x.toVector)
                              }
                            case STAR if keyList.size > 1 =>
                              Some(goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))) match {
                                case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                                case Some(x) => Some(resultComposer(x.toVector))
                              }
                            case _ if keyList.head._2.equals(C_NEXT) | keyList.head._2.equals(C_LEVEL) => errorAnalyzer(limitList.head._3)
                            case _ =>
                              val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                              if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                          }
                        case _ =>
                          netty.readerIndex(finishReaderIndex)
                          None
                      }
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._1 match {
                        case STAR if keyList.size < 2 =>
                          Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))) match {
                            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                            case Some(x) => Some(x.toVector)
                          }
                        case STAR if keyList.size > 1 =>
                          Some(goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))) match {
                            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                            case Some(x) => Some(resultComposer(x.toVector))
                          }
                        case _ if keyList.head._2.equals(C_NEXT) | keyList.head._2.equals(C_LEVEL) => errorAnalyzer(limitList.head._3)
                        case _ =>
                          val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                          if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      }
                    case Some(_) =>
                      netty.readerIndex(finishReaderIndex)
                      None
                    case None =>
                      keyList.head._1 match {
                        case STAR if keyList.size < 2 =>
                          Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))) match {
                            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                            case Some(x) => Some(x.toVector)
                          }
                        case STAR if keyList.size > 1 =>
                          Some(goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList.drop(1), limitList.drop(1))) match {
                            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
                            case Some(x) => Some(resultComposer(x.toVector))
                          }
                        case _ if keyList.head._2.equals(C_LIMIT) && keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER)=>
                          val midResult: Iterable[Any] = extractFromBsonArray(netty,valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                          if(midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                        case _ =>
                          netty.readerIndex(finishReaderIndex)
                          None
                      }
                  }
              }}
          case D_BOOLEAN =>
            netty.readByte()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_NULL =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_INT =>
            netty.readIntLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_LONG =>
            netty.readLongLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                  case Some(_) => None
                  case None => None
                }
            }
          case D_ZERO_BYTE =>
            None
        }
      val actualPos2: Int = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          finalValue ++ goThrough(iter+1)
        case 0 => finalValue
      }
    }

    val seq: Iterable[Any] = goThrough(0)
    limitList.head._3 match {
      case UNTIL_RANGE =>
        val res: Vector[Any] = resultComposer(seq.take(seq.size-1).toVector)
        res.collect { case value if !value.equals(WARNING_CHAR)=> value }
      case _ =>
        //if(seq.nonEmpty && seq.last.equals('!'))
          //throw new RuntimeException("Path of expression doesn't conform with the event")
        //else
        //println(s"case not until____ seq: $seq")
        seq.collect { case value if !value.equals(WARNING_CHAR)=> value }
    }
  }*/

  private def findElements(netty: ByteBuf, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)], start: Int, finish: Int): List[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            netty.readDoubleLE()
            Some(C_MATCH)
          } else {
            netty.readDoubleLE()
            None
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            val valueLength: Int = netty.readIntLE()
            netty.readCharSequence(valueLength, charset)
            Some(C_MATCH)
          } else {
            netty.readCharSequence(netty.readIntLE(), charset)
            None
          }
        case D_BSONOBJECT =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(bsonFinishReaderIndex)
                Some(C_MATCH)
              case C_LIMIT =>
                Some(extractFromBsonObj(netty.readerIndex(start + 4), keyList, finish, limitList)) match {
                  case Some(value) if value.isEmpty => Some(C_MATCH)
                  case Some(value) =>
                    //val arr: Array[Byte] = new Array[Byte](finish - start)
                    val buf = netty.copy(start,finish-start)
                    //netty.getBytes(start, arr, 0, finish - start)
                    Some(resultComposer(List(List(buf), resultComposer(value))))
                }
            }
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case C_LIMIT =>
                val midResult: List[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult))
            }
          }
        case D_BSONARRAY =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(arrayFinishReaderIndex)
                Some(C_MATCH)
              case C_LIMIT =>
                Some(extractFromBsonObj(netty.readerIndex(start + 4), keyList, finish, limitList)) match {
                  case Some(value) if value.isEmpty =>
                    Some(C_MATCH)
                  case Some(value) =>
                    //val arr: Array[Byte] = new Array[Byte](finish - start)
                    val buf = netty.copy(start,finish - start)
                    //netty.getBytes(start, arr, 0, finish - start)
                    Some(resultComposer(List(List(buf), resultComposer(value))))
                }
            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case C_LIMIT =>
                Some(extractFromBsonObj(netty.readerIndex(start + 4), keyList, finish, limitList)) match {
                  case Some(value) if value.isEmpty =>
                    netty.readerIndex(arrayFinishReaderIndex)
                    None
                  case Some(value) =>
                    netty.readerIndex(arrayFinishReaderIndex)
                    Some(resultComposer(value))
                }
            }
          }
        case D_BOOLEAN =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            netty.readByte()
            Some(C_MATCH)
          } else {
            netty.readByte()
            None
          }
        case D_NULL =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            Some(C_MATCH)
          } else None
        case D_INT =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            netty.readIntLE()
            Some(C_MATCH)
          } else {
            netty.readIntLE()
            None
          }
        case D_LONG =>
          val (matched: Boolean, _: String) = compareKeys(netty, keyList.drop(1).head._1)
          if (matched) {
            netty.readLongLE()
            Some(C_MATCH)
          } else {
            netty.readLongLE()
            None
          }
        case D_ZERO_BYTE => None
      }
    val actualPos2: Int = finish - netty.readerIndex()
    actualPos2 match {
      case x if x > 0 && finalValue.isDefined && finalValue.get.equals(C_MATCH) =>
        //val arr: Array[Byte] = new Array[Byte](finish - start)
        val buf = netty.copy(start,finish-start)
        //netty.getBytes(start, arr, 0, finish - start)
        List(buf) ++ findElements(netty, keyList, limitList, start, finish)
      case 0 if finalValue.isDefined && finalValue.get.equals(C_MATCH) =>
        //val arr: Array[Byte] = new Array[Byte](finish - start)
        //netty.getBytes(start, arr, 0, finish - start)
        val buf = netty.copy(start,finish-start)
        List(buf)
      case x if x > 0 && finalValue.isDefined && keyList.head._2.equals(C_LIMIT) =>
        List(finalValue.get) ++ findElements(netty, keyList, limitList, start, finish)
      case 0 if finalValue.isDefined && keyList.head._2.equals(C_LIMIT) => List(finalValue.get)
      case _ if finalValue.isDefined => List(finalValue.get)
      case x if x > 0 && finalValue.isEmpty => findElements(netty, keyList, limitList, start, finish)
      case 0 if finalValue.isEmpty => List()
    }
  }

  def duplicate: BosonImpl = new BosonImpl(byteArray = Option(this.nettyBuffer.duplicate().array()))

  def getByteBuf: ByteBuf = this.nettyBuffer

  def array: Array[Byte] = {
    if (nettyBuffer.isReadOnly) {
      throw new ReadOnlyBufferException()
    } else {
      nettyBuffer.array()
    }
  }

  // Injector Starts here

  def isHalfword(fieldID: String, extracted: String): Boolean = {
    if (fieldID.contains("*") & extracted.nonEmpty) {
      val list: Array[String] = fieldID.split('*')
      (extracted, list.length) match {
        case (_, 0) =>
          true
        case (x, 1) if x.startsWith(list.head) =>
          true
        case (x, 2) if x.startsWith(list.head) & x.endsWith(list.last) =>
          true
        case (x, i) if i > 2 =>
          fieldID match {
            case s if s.startsWith("*") =>
              if (x.startsWith(list.apply(1)))
                isHalfword(s.substring(1 + list.apply(1).length), x.substring(list.apply(1).length))
              else {
                isHalfword(s, x.substring(1))
              }
            case s if !s.startsWith("*") =>
              if (x.startsWith(list.head)) {
                isHalfword(s.substring(list.head.length), extracted.substring(list.head.length))
              } else {
                false
              }
          }
        case _ =>
          false
      }
    } else
      false
  }

  def modifyAll[T](list: List[(Statement, String)], buffer: ByteBuf, fieldID: String, f: T => T, result: ByteBuf = Unpooled.buffer()): ByteBuf = {
    /*
    * Se fieldID for vazia devolve o Boson Origina
    * */
    val startReader: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    while ((buffer.readerIndex() - startReader) < originalSize) {
      val dataType: Int = buffer.readByte().toInt
      dataType match {
        case 0 =>
          result.writeByte(dataType)
        case _ =>
          result.writeByte(dataType)
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buffer.readByte()
              key.append(b)
            }
            val b: Byte = buffer.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(key).writeByte(b)
          new String(key) match {
            case x if fieldID.toCharArray.deep == x.toCharArray.deep || isHalfword(fieldID, x) =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              if (list.size == 1) {
                if (list.head._2.contains("..")) {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      val size: Int = buffer.getIntLE(buffer.readerIndex())
                      val buf1: ByteBuf = buffer.readRetainedSlice(size)
                      val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                      buf1.release()
                      val buf3: ByteBuf = Unpooled.buffer()
                      modifierAll(buf2, dataType, f, buf3)
                      buf2.release()
                      buf3.capacity(buf3.writerIndex())
                      result.writeBytes(buf3)
                      buf3.release()
                    case _ =>
                      modifierAll(buffer, dataType, f, result)
                  }
                } else {
                  modifierAll(buffer, dataType, f, result)
                }
              } else {
                if (list.head._2.contains("..")) {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      val size: Int = buffer.getIntLE(buffer.readerIndex())
                      val buf1: ByteBuf = buffer.readBytes(size)
                      val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                      buf1.release()
                      val buf3: ByteBuf = execStatementPatternMatch(buf2, list, f)
                      buf2.release()
                      result.writeBytes(buf3)
                      buf3.release()
                    case _ =>
                      processTypesAll(list, dataType, buffer, result, fieldID, f)
                  }
                } else {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      val res: ByteBuf = execStatementPatternMatch(buffer, list.drop(1), f)
                      result.writeBytes(res)
                      res.release()
                    case _ =>
                      processTypesArray(dataType, buffer, result)
                  }
                }
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              if (list.head._2.contains(".."))
                processTypesAll(list, dataType, buffer, result, fieldID, f)
              else
                processTypesArray(dataType, buffer, result)
          }
      }
    }
    result.capacity(result.writerIndex())
    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    result.release()
    finalResult
  }

  private def processTypesArray(dataType: Int, buffer: ByteBuf, result: ByteBuf) = {
    dataType match {
      case D_ZERO_BYTE =>
      case D_FLOAT_DOUBLE =>
        result.writeDoubleLE(buffer.readDoubleLE())
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        result.writeIntLE(valueLength)
        result.writeCharSequence(buffer.readCharSequence(valueLength, charset), charset)
      case D_BSONOBJECT =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        result.writeBytes(bsonBuf)
        bsonBuf.release()
      case D_BSONARRAY =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        result.writeBytes(bsonBuf)
        bsonBuf.release()
      case D_NULL =>
      case D_INT =>
        result.writeIntLE(buffer.readIntLE())
      case D_LONG =>
        result.writeLongLE(buffer.readLongLE())
      case D_BOOLEAN =>
        result.writeBoolean(buffer.readBoolean())
      //case _ =>
    }
  }

  private def modifierAll[T](buffer: ByteBuf, seqType: Int, f: T => T, result: ByteBuf): Unit = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0: Any = buffer.readDoubleLE()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Float) =>
            result.writeDoubleLE(n.toDouble)
          case Some(n: Double) =>
            result.writeDoubleLE(n)
          case _ =>
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val array: ByteBuf = Unpooled.buffer(length - 1).writeBytes(buffer.readCharSequence(length - 1, charset).toString.getBytes)
        buffer.readByte()
        val value: Any = applyFunction(f, array.array())
        array.release()
        Option(value) match {
          case Some(n: Array[Byte]) =>
            result.writeIntLE(n.length + 1).writeBytes(n).writeZero(1)
          case Some(n: String) =>
            val aux: Array[Byte] = n.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
          case Some(n: Instant) =>
            val aux: Array[Byte] = n.toString.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
          case _ =>
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bson: ByteBuf = buffer.readBytes(valueLength)
        val buf: ByteBuf = Unpooled.buffer(bson.capacity()).writeBytes(bson)
        val arrayBytes: Array[Byte] = buf.array()
        buf.release()
        val bsonBytes: Array[Byte] = arrayBytes
        bson.release()
        val newValue: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
        (result.writeBytes(newValue), newValue.length - valueLength)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bson: ByteBuf = buffer.readBytes(valueLength)
        val buf: ByteBuf = Unpooled.buffer(bson.capacity()).writeBytes(bson)
        val arrayBytes: Array[Byte] = buf.array()
        buf.release()
        val bsonBytes: Array[Byte] = arrayBytes
        bson.release()
        val value: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
        (result.writeBytes(value), value.length - valueLength)
      case D_BOOLEAN =>
        val value0: Boolean = buffer.readBoolean()
        val value: Any = applyFunction(f, value0)
        result.writeBoolean(value.asInstanceOf[Boolean])
      case D_NULL =>
        throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value0: Any = buffer.readIntLE()
        val value: Any = applyFunction(f, value0)
        result.writeIntLE(value.asInstanceOf[Int])
      case D_LONG =>
        val value0: Any = buffer.readLongLE()
        val value: Any = applyFunction(f, value0)
        result.writeLongLE(value.asInstanceOf[Long])
    }
  }

  private def applyFunction[T](f: T => T, value: Any): T = {
    Try(f(value.asInstanceOf[T])) match {
      case Success(v) =>
        v.asInstanceOf[T]
      case Failure(e) =>
        value match {
          case x: Double =>
            Try(f(x.toFloat.asInstanceOf[T])) match {
              case Success(v) =>
                v.asInstanceOf[T]
              case Failure(e1) =>
                throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
            }
          case x: Array[Byte] =>
            Try(f(new String(x).asInstanceOf[T])) match {
              case Success(v) =>
                v.asInstanceOf[T]
              case Failure(e1) =>
                Try(f(Instant.parse(new String(x)).asInstanceOf[T])) match {
                  case Success(v) =>
                    v.asInstanceOf[T]
                  case Failure(e2) =>
                    throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
                }
            }
          case _ =>
            throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
        }
    }
  }

  private def processTypesAll[T](list: List[(Statement, String)], seqType: Int, buffer: ByteBuf, result: ByteBuf, fieldID: String, f: T => T): Unit = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        result.writeDoubleLE(buffer.readDoubleLE())
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        result.writeIntLE(valueLength)
        val buf: ByteBuf = buffer.readBytes(valueLength)
        result.writeBytes(buf)
        buf.release()
      case D_BSONOBJECT =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux: ByteBuf = modifyAll(list, bsonBuf, fieldID, f)
        bsonBuf.release()
        result.writeBytes(resultAux)
        resultAux.release()
      case D_BSONARRAY =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux: ByteBuf = modifyAll(list, bsonBuf, fieldID, f)
        bsonBuf.release()
        result.writeBytes(resultAux)
        resultAux.release()
      case D_NULL =>
      case D_INT =>
        result.writeIntLE(buffer.readIntLE())
      case D_LONG =>
        result.writeLongLE(buffer.readLongLE())
      case D_BOOLEAN =>
        result.writeBoolean(buffer.readBoolean())
    }
  }

  private def readArrayPos(netty: ByteBuf): Char = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    while (netty.getByte(netty.readerIndex()) != 0) {
      list.+=(netty.readByte())
    }
    list.+=(netty.readByte()) //  consume the end Pos byte
    val stringList: ListBuffer[Char] = list.map(b => b.toInt.toChar)
    //println(s"readArrayPos: ${stringList}")
    stringList.head
  }

  private def modifierEnd[T](buffer: ByteBuf, seqType: Int, f: T => T, result: ByteBuf, resultCopy: ByteBuf): Unit = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buffer.readDoubleLE()
        resultCopy.writeDoubleLE(value0)
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Float) =>
            result.writeDoubleLE(n.toDouble)
          case Some(n: Double) =>
            result.writeDoubleLE(n)
          case _ =>
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val value0: Array[Byte] = Unpooled.buffer(length - 1).writeBytes(buffer.readBytes(length - 1)).array()
        resultCopy.writeIntLE(length).writeBytes(value0)
        buffer.readByte()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Array[Byte]) =>
            result.writeIntLE(n.length + 1).writeBytes(n).writeZero(1)
          case Some(n: String) =>
            val aux: Array[Byte] = n.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
          case Some(n: Instant) =>
            val aux: Array[Byte] = n.toString.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
          case _ =>
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonObj: ByteBuf = buffer.readBytes(valueLength)
        resultCopy.writeBytes(bsonObj.duplicate())
        val buf: ByteBuf = Unpooled.buffer(bsonObj.capacity()).writeBytes(bsonObj)
        val arrayBytes: Array[Byte] = buf.array()
        buf.release()
        val bsonBytes: Array[Byte] = arrayBytes
        bsonObj.release()
        val newValue: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
        result.writeBytes(newValue)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonArray: ByteBuf = buffer.readBytes(valueLength)
        resultCopy.writeBytes(bsonArray.duplicate())
        val buf: ByteBuf = Unpooled.buffer(bsonArray.capacity()).writeBytes(bsonArray)
        val arrayBytes: Array[Byte] = buf.array()
        buf.release()
        val bsonBytes: Array[Byte] = arrayBytes
        bsonArray.release()
        val value: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
        result.writeBytes(value)
      case D_BOOLEAN =>
        val value0: Boolean = buffer.readBoolean()
        resultCopy.writeBoolean(value0)
        val value: Boolean = applyFunction(f, value0).asInstanceOf[Boolean]
        result.writeBoolean(value)
      case D_NULL =>
        throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value0: Int = buffer.readIntLE()
        resultCopy.writeIntLE(value0)
        val value: Int = applyFunction(f, value0).asInstanceOf[Int]
        result.writeIntLE(value)
      case D_LONG =>
        val value0: Long = buffer.readLongLE()
        resultCopy.writeLongLE(value0)
        val value: Long = applyFunction(f, value0).asInstanceOf[Long]
        result.writeLongLE(value)
    }
  }

  def modifyArrayEnd[T](list: List[(Statement, String)], buffer: ByteBuf, f: T => T, condition: String, limitInf: String = "0", limitSup: String = C_END, result: ByteBuf = Unpooled.buffer(), resultCopy: ByteBuf = Unpooled.buffer()): BosonImpl = {
    /*
    * Se fieldID for vazia devolve o Boson Original
    * */
    val startReaderIndex: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    val exceptions: ListBuffer[Throwable] = new ListBuffer[Throwable]
    while ((buffer.readerIndex() - startReaderIndex) < originalSize && exceptions.size < 2) {
      val dataType: Int = buffer.readByte().toInt
      result.writeByte(dataType)
      resultCopy.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buffer.readByte()
              key.append(b)
            }
            val b: Byte = buffer.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(key).writeByte(b)
          resultCopy.writeBytes(key).writeByte(b)
          val keyString: String = new String(key)
          (keyString, condition, limitSup) match {
            case (x, C_END, _) if isArray =>
              exceptions.clear()
              if (list.size == 1) {
                if (list.head._2.contains("..")) {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      if (exceptions.isEmpty) {
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.readBytes(size)
                        val bufRes: ByteBuf = Unpooled.buffer()
                        val bufResCopy: ByteBuf = Unpooled.buffer()
                        result.clear().writeBytes(resultCopy.duplicate())
                        val buf3: ByteBuf =
                          if (list.head._1.isInstanceOf[ArrExpr])
                            execStatementPatternMatch(buf1, list, f)
                          else
                            Unpooled.buffer(buf1.capacity()).writeBytes(buf1)


                        Try(modifierEnd(buf3.duplicate(), dataType, f, bufRes, bufResCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                        result.writeBytes(bufRes)
                        resultCopy.writeBytes(bufResCopy)
                        buf1.release()
                        bufRes.release()
                        bufResCopy.release()
                        buf3.release()
                      }
                    case _ =>
                      if (exceptions.isEmpty) {
                        result.clear().writeBytes(resultCopy.duplicate())
                        Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                      }
                  }
                } else {
                  if (exceptions.isEmpty) {
                    result.clear().writeBytes(resultCopy.duplicate())
                    Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                      case Success(_) =>
                      case Failure(e) =>
                        exceptions.append(e)
                    }
                  }
                }
              } else {
                if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        result.clear().writeBytes(resultCopy.duplicate())
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.readBytes(size)
                        // val buf2: ByteBuf = execStatementPatternMatch(buf1.duplicate(), list, f)
                        val buf2: ByteBuf =
                          if (list.head._1.isInstanceOf[ArrExpr])
                            execStatementPatternMatch(buf1.duplicate(), list, f)
                          else
                            Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
                        //val buf3: ByteBuf =
                        Try(execStatementPatternMatch(buf2.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            result.writeBytes(v)
                            v.release()
                            resultCopy.writeBytes(buf2)
                          case Failure(e) =>
                            result.writeBytes(buf2.duplicate())
                            resultCopy.writeBytes(buf2)
                            exceptions.append(e)
                        }

                        //processTypesArray(dataType, buffer, resultCopy)
                        buf1.release()
                        buf2.release()
                        //buf3.release()
                      }
                    case _ =>
                      processTypesArrayEnd(list, EMPTY_KEY, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
                  }
                } else {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        result.clear().writeBytes(resultCopy.duplicate())
                        // val res: ByteBuf =
                        Try(execStatementPatternMatch(buffer.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            result.writeBytes(v)
                            v.release()
                            processTypesArray(dataType, buffer, resultCopy)
                          case Failure(e) =>
                            processTypesArray(dataType, buffer.duplicate(), result)
                            processTypesArray(dataType, buffer, resultCopy)
                            exceptions.append(e)
                        }
                        //println("RES SIZE=" + res.capacity())
                      }
                    case _ =>
                      processTypesArray(dataType, buffer.duplicate(), result)
                      processTypesArray(dataType, buffer, resultCopy)
                  }
                }
              }
            case (x, _, C_END) if isArray && limitInf.toInt <= keyString.toInt =>
              if (list.size == 1) {
                if (list.head._2.contains("..") /*&& !list.head._1.isInstanceOf[KeyWithArrExpr]*/ ) {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      if (exceptions.isEmpty) {
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.readBytes(size)
                        val bufRes: ByteBuf = Unpooled.buffer()
                        val bufResCopy: ByteBuf = Unpooled.buffer()
                        resultCopy.clear().writeBytes(result.duplicate())
                        val buf3: ByteBuf = execStatementPatternMatch(buf1, list, f)
                        Try(modifierEnd(buf3.duplicate(), dataType, f, bufRes, bufResCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                        result.writeBytes(bufRes)
                        resultCopy.writeBytes(bufResCopy)
                        buf1.release()
                        bufRes.release()
                        bufResCopy.release()
                        buf3.release()
                      } else
                        exceptions.append(exceptions.head)
                    case _ =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                      } else
                        exceptions.append(exceptions.head)
                  }
                } else {
                  if (exceptions.isEmpty) {
                    resultCopy.clear().writeBytes(result.duplicate())
                    Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                      case Success(_) =>
                      case Failure(e) =>
                        exceptions.append(e)
                    }
                  } else
                    exceptions.append(exceptions.head)
                }
              } else {
                if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.duplicate().readRetainedSlice(size)
                        val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                        buf1.release()
                        // val buf3: ByteBuf =
                        Try(execStatementPatternMatch(buf2.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            buf2.release()
                            result.writeBytes(v)
                            v.release()
                            processTypesArray(dataType, buffer, resultCopy)
                          case Failure(e) =>
                            processTypesArray(dataType, buffer.duplicate(), result)
                            processTypesArray(dataType, buffer, resultCopy)
                            exceptions.append(e)
                        }
                      } else {
                        exceptions.append(exceptions.head)
                      }

                    case _ =>
                      processTypesArrayEnd(list, EMPTY_KEY, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
                  }
                } else {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        //val res: ByteBuf =
                        Try(execStatementPatternMatch(buffer.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            result.writeBytes(v)
                            processTypesArray(dataType, buffer, resultCopy)
                            v.release()
                          case Failure(e) =>
                            processTypesArray(dataType, buffer.duplicate(), result)
                            processTypesArray(dataType, buffer, resultCopy)
                            exceptions.append(e)
                        }
                      } else
                        exceptions.append(exceptions.head)
                    case _ =>
                      processTypesArray(dataType, buffer.duplicate(), result)
                      processTypesArray(dataType, buffer, resultCopy)
                  }
                }
              }
            case (x, _, C_END) if isArray && limitInf.toInt > keyString.toInt =>
              if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/ )
                dataType match {
                  case (D_BSONOBJECT | D_BSONARRAY) =>
                    val size: Int = buffer.getIntLE(buffer.readerIndex())
                    val buf1: ByteBuf = buffer.readBytes(size)
                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                    result.writeBytes(buf2.duplicate())
                    resultCopy.writeBytes(buf2.duplicate())
                    buf1.release()
                    buf2.release()
                  case _ =>
                    processTypesArray(dataType, buffer.duplicate(), result)
                    processTypesArray(dataType, buffer, resultCopy)
                }
              else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
            case (x, _, l) if isArray && (limitInf.toInt <= x.toInt && l.toInt >= x.toInt) =>
              if (list.size == 1) {
                if (list.head._2.contains("..")) {
                  dataType match {
                    case (D_BSONOBJECT | D_BSONARRAY) =>
                      if (exceptions.isEmpty) {
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.readBytes(size)
                        val bufRes: ByteBuf = Unpooled.buffer()
                        val bufResCopy: ByteBuf = Unpooled.buffer()
                        resultCopy.clear().writeBytes(result.duplicate())
                        val buf3: ByteBuf = execStatementPatternMatch(buf1, list, f)
                        Try(modifierEnd(buf3.duplicate(), dataType, f, bufRes, bufResCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                        result.writeBytes(bufRes)
                        resultCopy.writeBytes(bufResCopy)
                        buf1.release()
                        bufRes.release()
                        bufResCopy.release()
                        buf3.release()
                      } else
                        exceptions.append(exceptions.head)
                    case _ =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                          case Success(_) =>
                          case Failure(e) =>
                            exceptions.append(e)
                        }
                      } else
                        exceptions.append(exceptions.head)

                  }
                } else {
                  if (exceptions.isEmpty) {
                    resultCopy.clear().writeBytes(result.duplicate())
                    Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                      case Success(_) =>
                      case Failure(e) =>
                        exceptions.append(e)
                    }
                  } else
                    exceptions.append(exceptions.head)
                }
              } else {
                if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        val size: Int = buffer.getIntLE(buffer.readerIndex())
                        val buf1: ByteBuf = buffer.duplicate().readBytes(size)
                        val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                        buf1.release()
                        Try(execStatementPatternMatch(buf2.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            result.writeBytes(v)
                            v.release()
                            processTypesArray(dataType, buffer, resultCopy)
                          case Failure(e) =>
                            processTypesArray(dataType, buffer.duplicate(), result)
                            processTypesArray(dataType, buffer, resultCopy)
                            exceptions.append(e)
                        }
                        buf2.release()
                      } else
                        exceptions.append(exceptions.head)
                    case _ =>
                      processTypesArrayEnd(list, EMPTY_KEY, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
                  }
                } else {
                  dataType match {
                    case (D_BSONARRAY | D_BSONOBJECT) =>
                      if (exceptions.isEmpty) {
                        resultCopy.clear().writeBytes(result.duplicate())
                        Try(execStatementPatternMatch(buffer.duplicate(), list.drop(1), f)) match {
                          case Success(v) =>
                            result.writeBytes(v)
                            processTypesArray(dataType, buffer, resultCopy)
                            v.release()
                          case Failure(e) =>
                            processTypesArray(dataType, buffer.duplicate(), result)
                            processTypesArray(dataType, buffer, resultCopy)
                            exceptions.append(e)
                        }
                      } else
                        exceptions.append(exceptions.head)
                    case _ =>
                      processTypesArray(dataType, buffer.duplicate(), result)
                      processTypesArray(dataType, buffer, resultCopy)
                  }
                }
              }
            case (x, _, l) if isArray && (limitInf.toInt > x.toInt || l.toInt < x.toInt) =>
              if (list.head._2.contains(".."))
                dataType match {
                  case (D_BSONOBJECT | D_BSONARRAY) =>
                    val size: Int = buffer.getIntLE(buffer.readerIndex())
                    val buf1: ByteBuf = buffer.readBytes(size)
                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                    result.writeBytes(buf2.duplicate())
                    resultCopy.writeBytes(buf2.duplicate())
                    buf1.release()
                    buf2.release()
                  case _ =>
                    processTypesArray(dataType, buffer.duplicate(), result)
                    processTypesArray(dataType, buffer, resultCopy)
                }
              else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
            case (x, _, l) if !isArray =>
              if (list.head._2.contains("..")) {
                dataType match {
                  case (D_BSONOBJECT | D_BSONARRAY) =>
                    val size: Int = buffer.getIntLE(buffer.readerIndex())
                    val buf1: ByteBuf = buffer.readBytes(size)
                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                    result.writeBytes(buf2.duplicate())
                    resultCopy.writeBytes(buf2.duplicate())
                    buf1.release()
                    buf2.release()
                  case _ =>
                    processTypesArray(dataType, buffer.duplicate(), result)
                    processTypesArray(dataType, buffer, resultCopy)
                }
              } else throw CustomException("*ModifyArrayEnd* Not a Array")
          }
      }
    }
    result.capacity(result.writerIndex())
    resultCopy.capacity(resultCopy.writerIndex())
    val a: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity() + 4).writeIntLE(resultCopy.capacity() + 4).writeBytes(resultCopy)
    result.release()
    resultCopy.release()
    if (condition.equals(TO_RANGE))
      if (exceptions.isEmpty) {
        val bson: BosonImpl = new BosonImpl(byteArray = Option(a.array()))
        a.release()
        b.release()
        bson
      } else
        throw exceptions.head
    else if (condition.equals(UNTIL_RANGE)) {

      if (exceptions.length <= 1) {
        val bson: BosonImpl = new BosonImpl(byteArray = Option(b.array()))
        a.release()
        b.release()
        bson
      } else
        throw exceptions.head
    } else {

      if (exceptions.isEmpty) {
        val bson: BosonImpl = new BosonImpl(byteArray = Option(a.array()))
        a.release()
        b.release()
        bson
      } else
        throw exceptions.head
    }
  }

  private def processTypesArrayEnd[T](list: List[(Statement, String)], fieldID: String, dataType: Int, buf: ByteBuf, f: (T) => T, condition: String, limitInf: String = "0", limitSup: String = C_END, result: ByteBuf, resultCopy: ByteBuf) = {
    dataType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buf.readDoubleLE()
        result.writeDoubleLE(value0)
        resultCopy.writeDoubleLE(value0)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buf.readIntLE()
        val bytes: ByteBuf = buf.readBytes(valueLength)
        result.writeIntLE(valueLength)
        result.writeBytes(bytes.duplicate())
        resultCopy.writeIntLE(valueLength)
        resultCopy.writeBytes(bytes.duplicate())
        bytes.release()
      case D_BSONOBJECT =>
        val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f, condition, limitInf, limitSup)
        if (condition.equals(TO_RANGE)) // || condition.equals(C_END))
          result.writeBytes(res.getByteBuf)
        else if (condition.equals(C_END))
          result.writeBytes(res.getByteBuf)
        else
          resultCopy.writeBytes(res.getByteBuf)
        res.getByteBuf.release()
      case D_BSONARRAY =>
        val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f, condition, limitInf, limitSup)
        if (condition.equals(TO_RANGE))
          result.writeBytes(res.getByteBuf)
        else if (condition.equals(C_END))
          result.writeBytes(res.getByteBuf)
        else
          resultCopy.writeBytes(res.getByteBuf)

        res.getByteBuf.release()
      case D_NULL =>
        None
      case D_INT =>
        val value0: Int = buf.readIntLE()
        result.writeIntLE(value0)
        resultCopy.writeIntLE(value0)
        None
      case D_LONG =>
        val value0: Long = buf.readLongLE()
        result.writeLongLE(value0)
        resultCopy.writeLongLE(value0)
        None
      case D_BOOLEAN =>
        val value0: Boolean = buf.readBoolean()
        result.writeBoolean(value0)
        resultCopy.writeBoolean(value0)
        None
    }
  }

  def modifyArrayEndWithKey[T](list: List[(Statement, String)], buffer: ByteBuf, fieldID: String, f: T => T, condition: String, limitInf: String = "0", limitSup: String = C_END, result: ByteBuf = Unpooled.buffer(), resultCopy: ByteBuf = Unpooled.buffer()): BosonImpl = {
    /*
    * Se fieldID for vazia, então deve ser chamada a funcao modifyArrayEnd to work on Root
    *ByteBuf tem de ser duplicado no input
    * */
    val startReaderIndex: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    while ((buffer.readerIndex() - startReaderIndex) < originalSize) {
      val dataType: Int = buffer.readByte().toInt
      result.writeByte(dataType)
      resultCopy.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buffer.readByte()
              key.append(b)
            }
            val b: Byte = buffer.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(key).writeByte(b)
          resultCopy.writeBytes(key).writeByte(b)
          val keyString: String = new String(key)
          keyString match {
            case x if (fieldID.toCharArray.deep == x.toCharArray.deep || isHalfword(fieldID, x)) && dataType == D_BSONARRAY =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              if (list.size == 1) {
                if (list.head._2.contains("..")) {
                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readBytes(size)
                  val res: BosonImpl = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup)
                  val buf2: ByteBuf = execStatementPatternMatch(res.getByteBuf.duplicate(), list, f)

                  if (condition.equals(TO_RANGE))
                    result.writeBytes(buf2)
                  else if (condition.equals(UNTIL_RANGE))
                    resultCopy.writeBytes(buf2)
                  else {
                    // println("condition END")
                    result.writeBytes(buf2)
                    resultCopy.writeBytes(res.getByteBuf)
                  }

                  buf1.release()
                  buf2.release()
                  res.getByteBuf.release()
                } else {
                  val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
                  if (condition.equals(TO_RANGE))
                    result.writeBytes(res.getByteBuf)
                  else if (condition.equals(UNTIL_RANGE))
                    resultCopy.writeBytes(res.getByteBuf)
                  else {
                    result.writeBytes(res.getByteBuf)
                  }
                  res.getByteBuf.release()
                }
              } else {
                if (list.head._2.contains("..")) {

                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readRetainedSlice(size)
                  val buf2: ByteBuf = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup).getByteBuf
                  buf1.release()
                  if (condition.equals(TO_RANGE))
                    result.writeBytes(buf2)
                  else if (condition.equals(UNTIL_RANGE))
                    resultCopy.writeBytes(buf2)
                  else {
                    result.writeBytes(buf2)
                  }
                  buf2.release()

                } else {
                  val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
                  if (condition.equals(TO_RANGE))
                    result.writeBytes(res.getByteBuf)
                  else if (condition.equals(UNTIL_RANGE))
                    resultCopy.writeBytes(res.getByteBuf)
                  else {
                    result.writeBytes(res.getByteBuf)
                  }
                  res.getByteBuf.release()
                }
              }
            case x if (fieldID.toCharArray.deep == x.toCharArray.deep || isHalfword(fieldID, x)) && dataType != D_BSONARRAY =>
              if (list.head._2.contains("..") && list.head._1.isInstanceOf[KeyWithArrExpr])
                processTypesArrayEnd(list, fieldID, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
              else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              if (list.head._2.contains("..") && list.head._1.isInstanceOf[KeyWithArrExpr]) {
                processTypesArrayEnd(list, fieldID, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
              } else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
          }
      }
    }
    result.capacity(result.writerIndex())
    resultCopy.capacity(resultCopy.writerIndex())
    val a: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity() + 4).writeIntLE(resultCopy.capacity() + 4).writeBytes(resultCopy)
    result.release()
    resultCopy.release()
    if (condition.equals(TO_RANGE))
      new BosonImpl(byteArray = Option(a.array()))
    else if (condition.equals(UNTIL_RANGE))
      new BosonImpl(byteArray = Option(b.array()))
    else {
      // println("condition END")
      new BosonImpl(byteArray = Option(a.array()))
    }
  }

  def modifyHasElem[T](list: List[(Statement, String)], buf: ByteBuf, key: String, elem: String, f: Function[T, T], result: ByteBuf = Unpooled.buffer()): ByteBuf = {
    val startReader: Int = buf.readerIndex()
    val size: Int = buf.readIntLE()
    while ((buf.readerIndex() - startReader) < size) {
      val dataType: Int = buf.readByte().toInt
      result.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, k, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(k).writeByte(b)
          val keyString: String = new String(k)
          keyString match {
            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && dataType == D_BSONARRAY =>
              val newBuf: ByteBuf = searchAndModify(list, buf, elem, f).getByteBuf
              result.writeBytes(newBuf)
              newBuf.release()
            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && (dataType != D_BSONARRAY) =>
              if (list.head._2.contains(".."))
                processTypesHasElem(list, dataType, key, elem, buf, f, result)
              else
                processTypesArray(dataType, buf, result)
            case x if key.toCharArray.deep != x.toCharArray.deep && !isHalfword(key, x) =>
              if (list.head._2.contains(".."))
                processTypesHasElem(list, dataType, key, elem, buf, f, result)
              else
                processTypesArray(dataType, buf, result)
          }
      }
    }
    result.capacity(result.writerIndex())
    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    result.release()
    finalResult
  }

  private def processTypesHasElem[T](list: List[(Statement, String)], dataType: Int, key: String, elem: String, buf: ByteBuf, f: (T) => T, result: ByteBuf) = {
    dataType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buf.readDoubleLE()
        result.writeDoubleLE(value0)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buf.readIntLE()
        val bytes: ByteBuf = buf.readBytes(valueLength)
        result.writeIntLE(valueLength)
        result.writeBytes(bytes)
        bytes.release()
      case D_BSONOBJECT =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readBytes(length)
        val newBsonBuf: ByteBuf = modifyHasElem(list, bsonBuf, key, elem, f)
        bsonBuf.release()
        result.writeBytes(newBsonBuf)
        newBsonBuf.release()
      case D_BSONARRAY =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readRetainedSlice(length)
        val newBsonBuf: ByteBuf = modifyHasElem(list, bsonBuf, key, elem, f)
        bsonBuf.release()
        result.writeBytes(newBsonBuf)
        newBsonBuf.release()
      case D_NULL =>
      case D_INT =>
        val value0: Int = buf.readIntLE()
        result.writeIntLE(value0)
      case D_LONG =>
        val value0: Long = buf.readLongLE()
        result.writeLongLE(value0)
      case D_BOOLEAN =>
        val value0: Boolean = buf.readBoolean()
        result.writeBoolean(value0)
    }
  }

  private def searchAndModify[T](list: List[(Statement, String)], buf: ByteBuf, elem: String, f: Function[T, T], result: ByteBuf = Unpooled.buffer()): BosonImpl = {
    val startReader: Int = buf.readerIndex()
    val size: Int = buf.readIntLE()
    while ((buf.readerIndex() - startReader) < size) {
      val dataType: Int = buf.readByte().toInt
      result.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, k, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(k).writeByte(b)
          val keyString: String = new String(k)
          dataType match {
            case D_BSONOBJECT =>
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bsonBuf: ByteBuf = Unpooled.buffer(bsonSize)
              buf.getBytes(buf.readerIndex(), bsonBuf, bsonSize)
              val hasElem: Boolean = hasElement(bsonBuf.duplicate(), elem)
              if (hasElem) {
                if (list.size == 1) {
                  if (list.head._2.contains("..")) {
                    val buf1: ByteBuf = buf.readBytes(bsonSize)
                    val buf2: ByteBuf = Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
                    buf1.release()
                    val bsonBytes: Array[Byte] = buf2.array()
                    buf2.release()
                    val newArray: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
                    val newbuf: ByteBuf = Unpooled.buffer(newArray.length).writeBytes(newArray)
                    val buf3: ByteBuf = execStatementPatternMatch(newbuf, list, f)
                    newbuf.release()
                    result.writeBytes(buf3)
                    buf3.release()
                    bsonBuf.release()
                  } else {
                    val buf1: ByteBuf = buf.readBytes(bsonSize)
                    val buf2: ByteBuf = Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
                    val array: Array[Byte] = buf2.array()
                    buf2.release()
                    val bsonBytes: Array[Byte] = array
                    buf1.release()
                    val value1: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
                    result.writeBytes(value1)
                    bsonBuf.release()
                  }
                } else {
                  if (list.head._2.contains("..")) {
                    val size: Int = buf.getIntLE(buf.readerIndex())
                    val buf1: ByteBuf = buf.readBytes(size)
                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                    val buf3: ByteBuf = execStatementPatternMatch(buf2.duplicate(), list, f)
                    result.writeBytes(buf3)
                    buf1.release()
                    buf2.release()
                    buf3.release()
                    bsonBuf.release()
                  } else {
                    val buf1: ByteBuf = buf.readBytes(bsonSize)
                    result.writeBytes(execStatementPatternMatch(bsonBuf, list.drop(1), f))
                    buf1.release()
                    bsonBuf.release()
                  }
                }
              } else {
                val buf1: ByteBuf = buf.readBytes(bsonSize)
                result.writeBytes(buf1)
                buf1.release()
              }
            case _ =>
              processTypesArray(dataType, buf, result)
          }
      }
    }
    result.capacity(result.writerIndex())
    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    result.release()
    new BosonImpl(byteArray = Option(finalResult.array()))
  }

  def hasElement(buf: ByteBuf, elem: String): Boolean = {
    val size: Int = buf.readIntLE()
    val key: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buf.readerIndex() < size && (!elem.equals(new String(key.toArray)) && !isHalfword(elem, new String(key.toArray)))) {
      key.clear()
      val dataType: Int = buf.readByte()
      dataType match {
        case 0 =>
        case _ =>
          while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
            val b: Byte = buf.readByte()
            key.append(b)
          }
          buf.readByte()
          dataType match {
            case D_ZERO_BYTE =>
            case D_FLOAT_DOUBLE =>
              buf.readDoubleLE()
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              val valueLength: Int = buf.readIntLE()
              buf.readBytes(valueLength).release()
            case D_BSONOBJECT =>
              val valueLength: Int = buf.getIntLE(buf.readerIndex())
              buf.readBytes(valueLength).release()
            case D_BSONARRAY =>
              val valueLength: Int = buf.getIntLE(buf.readerIndex())
              buf.readBytes(valueLength).release()
            case D_BOOLEAN =>
              buf.readByte()
            case D_NULL =>
            case D_INT =>
              buf.readIntLE()
            case D_LONG =>
              buf.readLongLE()
            //case _ =>
          }
      }
    }
    new String(key.toArray).toCharArray.deep == elem.toCharArray.deep || isHalfword(elem, new String(key.toArray))
  }

  def execStatementPatternMatch[T](buf: ByteBuf, statements: List[(Statement, String)], f: Function[T, T]): ByteBuf = {
    val result: ByteBuf = Unpooled.buffer()
    val statement: Statement = statements.head._1
    val newStatementList: List[(Statement, String)] = statements
    statement match {
      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
        val input: (String, Int, String, Any) =
          (arrEx.leftArg, arrEx.midArg, arrEx.rightArg) match {
            case (i, o1, o2) if o1.isDefined && o2.isDefined =>
              (key, arrEx.leftArg, o1.get.value, o2.get)
            case (i, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg)
            case (0, str, None) =>
              str.get.value match {
                case "first" =>
                  (key, 0, TO_RANGE, 0)
                case "end" =>
                  (key, 0, C_END, None)
                case "all" =>
                  (key, 0, TO_RANGE, C_END)
              }
          }
        val res: ByteBuf = execArrayFunction(newStatementList, buf, f, input._1, input._2, input._3, input._4)
        val finalResult: ByteBuf = result.writeBytes(res)
        res.release()
        finalResult.capacity(finalResult.writerIndex())
      case ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) =>
        val input: (String, Int, String, Any) =
          (leftArg, midArg, rightArg) match {
            case (i, o1, o2) if midArg.isDefined && rightArg.isDefined =>
              (EMPTY_KEY, leftArg, midArg.get.value, rightArg.get)
            case (i, o1, o2) if midArg.isEmpty && rightArg.isEmpty =>
              (EMPTY_KEY, leftArg, TO_RANGE, leftArg)
            case (0, str, None) =>
              str.get.value match {
                case "first" => (EMPTY_KEY, 0, TO_RANGE, 0)
                case "end" => (EMPTY_KEY, 0, C_END, None)
                case "all" => (EMPTY_KEY, 0, TO_RANGE, C_END)
              }
          }
        val res: ByteBuf = execArrayFunction(newStatementList, buf, f, input._1, input._2, input._3, input._4)
        val finalResult: ByteBuf = result.writeBytes(res).capacity(result.writerIndex())
        res.release()
        finalResult
      case HalfName(half: String) =>
        val res: ByteBuf = modifyAll(newStatementList, buf, half, f)
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case HasElem(key: String, elem: String) =>
        val res: ByteBuf = modifyHasElem(newStatementList, buf, key, elem, f)
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case Key(key: String) =>
        val res: ByteBuf = modifyAll(newStatementList, buf, key, f)
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case ROOT =>
        val res: ByteBuf = execRootInjection(buf, f)
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case _ => throw CustomException("Wrong Statements, Bad Expression.")
      // Never Gets Here

    }
  }

  def execArrayFunction[T](list: List[(Statement, String)], buf: ByteBuf, f: Function[T, T], key: String, left: Int, mid: String, right: Any, result: ByteBuf = Unpooled.buffer()): ByteBuf = {
    (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, 0, C_END, None) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = modifyArrayEnd(list, buf1, f, C_END, 0.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, UNTIL_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, TO_RANGE, C_END) => // "[# .. end]"
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case TO_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
          case UNTIL_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
        }
      case (k, 0, C_END, None) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = modifyArrayEndWithKey(list, buf1, k, f, C_END, 0.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())

      case (k, a, UNTIL_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (k, a, TO_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = modifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case TO_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = modifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
          case UNTIL_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
        }
    }
  }

  def execRootInjection[T](buffer: ByteBuf, f: Function[T, T]): ByteBuf = {
    val bsonBytes: Array[Byte] = buffer.array()
    val newBson: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
    Unpooled.buffer(newBson.length).writeBytes(newBson)
  }
}
