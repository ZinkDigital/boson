package io.boson.bson.bsonImpl

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.time.Instant
import java.util
import Constants.{charset, _}
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import io.boson.bson.bsonPath._
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.immutable.Map.Map4
import io.boson.bson.bsonPath.Compose
import io.vertx.core.json.Json

import scala.collection.mutable


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
                 javaByteBuf: Option[ByteBuffer] = None,
                 scalaArrayBuf: Option[ArrayBuffer[Byte]] = None
               ) {



  /*private val valueOfArgument: String = this match {
    case _ if javaByteBuf.isDefined => javaByteBuf.get.getClass.getSimpleName
    case _ if byteArray.isDefined => byteArray.get.getClass.getSimpleName
    case _ if byteBuf.isDefined => byteBuf.get.getClass.getSimpleName
    case _ if vertxBuff.isDefined => vertxBuff.get.getClass.getSimpleName
    case _ if scalaArrayBuf.isDefined => scalaArrayBuf.get.getClass.getSimpleName
    case _ => EMPTY_CONSTRUCTOR
  }*/

  private val valueOfArgument: String =
    if (javaByteBuf.isDefined) {
      javaByteBuf.get.getClass.getSimpleName
    } else if (byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    } else if (scalaArrayBuf.isDefined) {
      scalaArrayBuf.get.getClass.getSimpleName
    } else EMPTY_CONSTRUCTOR

  private val nettyBuffer: ByteBuf = valueOfArgument match {
    case ARRAY_BYTE => // Array[Byte]
      val b: ByteBuf = Unpooled.copiedBuffer(byteArray.get)
      // b.writeBytes(byteArray.get)
      b
    case JAVA_BYTEBUFFER => // Java ByteBuffer
      val b: ByteBuf = Unpooled.copiedBuffer(javaByteBuf.get)
      javaByteBuf.get.clear()
      b
    case SCALA_ARRAYBUF => // Scala ArrayBuffer[Byte]
      val b: ByteBuf = Unpooled.copiedBuffer(scalaArrayBuf.get.toArray)
      b
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
  }

  private val comparingFunction = (netty: ByteBuf, key: String) => {
    //if (key.charAt(0).equals('*')) containsKey(netty,key.toCharArray.drop(1).mkString)
    //else compareKeys(netty, key)
    compareKeys(netty, key)
  }

  private val arrKeyDecode: ListBuffer[Byte] = new ListBuffer[Byte]()

  def extract(netty1: ByteBuf, keyList: List[(String, String)],
              limitList: List[(Option[Int], Option[Int], String)]): Option[Any] = {
    val netty: ByteBuf = netty1.duplicate()
    val startReaderIndex: Int = netty.readerIndex()
    val size: Int = netty.getIntLE(startReaderIndex)
    val seqType: Int = netty.getByte(startReaderIndex + 4).toInt
    seqType match {
      case 0 => None // end of obj
      case _ =>
        netty.getByte(startReaderIndex + 5).toInt match {
          case 48 => // root obj is BsonArray, call extractFromBsonArray
            netty.readIntLE()
            val arrayFinishReaderIndex: Int = startReaderIndex + size
            if (keyList.head._1.equals("*") && keyList.size == 1) {
              Some(traverseBsonArray(netty, size, arrayFinishReaderIndex, keyList, limitList))
            } else {
              val midResult: Iterable[Any] = extractFromBsonArray(netty, size, arrayFinishReaderIndex, keyList, limitList)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            }
          case _ => // root obj isn't BsonArray, call extractFromBsonObj
            if (keyList.head._1.isEmpty) {
              None // Doens't make sense to pass "" as a key when root isn't a BsonArray
            } else {
              netty.readIntLE()
              val bsonFinishReaderIndex: Int = startReaderIndex + size
              val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            }
        }
    }
  }

  // Extracts the value of a key inside a BsonObject
  private def extractFromBsonObj(netty: ByteBuf, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            //println(s"matched ${keyList.head._1} with a double")
            val value: Double = netty.readDoubleLE()
            Some(value)
          } else {
            netty.readDoubleLE()
            None
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            val valueLength: Int = netty.readIntLE()
            val arr: Array[Byte] = Unpooled.copiedBuffer(netty.readCharSequence(valueLength, charset), charset).array()
          // correcao de BUG
            val newArr: Array[Byte] = arr.filter(b => b!=0)

            Some(new String(newArr))
          } else {
            netty.readCharSequence(netty.readIntLE(), charset)
            None
          }
        case D_BSONOBJECT =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("limitLevel")) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val map: Map[Any, Any] = scala.collection.immutable.Map[Any, Any]()
            keyList.head._2 match {
              case "next" =>
                println("extractFromBsonObj; BsonObject case; condition = 'next'")
                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList.drop(1), bsonFinishReaderIndex, limitList.drop(1))
                println(s"midResult inside obj -> $midResult")
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
              case "all" if !keyList.head._1.equals("*")=>
                println("extractFromBsonObj; BsonObject case; condition = 'all'; returning obj and going to traverse obj looking for more occurrances of the same key")
                val midResult: Iterable[Any] = extractFromBsonObj(netty.duplicate(), keyList, bsonFinishReaderIndex, limitList)
                if (midResult.isEmpty) Some(traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList, limitList))
                else Some(resultComposer(Vector(Vector(traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList, limitList)),resultComposer(midResult.toVector))))
              case _ =>
                Some(traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList, limitList))
            }
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case "level" =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case "limitLevel" =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case _ =>
                //println(s"extractFromBsonObject:::case Object:::keyList: $keyList ::: limitList: $limitList")
                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList)
                if (midResult.isEmpty) None else{val res: Vector[Any] = resultComposer(midResult.toVector); Some(res)} //Some(resultComposer(midResult.toVector))
            }
          }
        case D_BSONARRAY =>
          if (comparingFunction(netty, keyList.head._1)) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            //println(s"condition is ${keyList.head._2}")
            keyList.head._2 match {
              case "next" if keyList.head._1.equals("*") && keyList.drop(1).head._1.equals("") =>  //case Book[#].*.[#]...
                println("case -> Book[#].*.[#]...")
                Some(extractFromBsonArray(netty,valueLength,arrayFinishReaderIndex,keyList.drop(1),limitList)) match {
                  case Some(value) if value.isEmpty => None
                  case Some(value) => Some(resultComposer(value.toVector))
                }
              case "next" if keyList.head._1.equals("*") && !keyList.drop(1).head._1.equals("") && !keyList.drop(1).head._1.equals("*") && !keyList.drop(1).head._2.equals("all") && !keyList.drop(1).head._2.equals("limit") => //case Book[#].*.key
                println("one entrance")
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case "next" =>
                println("extractFromBsonObj; BsonArray case; condition is next")
                val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList.drop(1), limitList.drop(1))
                println(s"midResult -> $midResult")
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
              case _ if keyList.size > 1 =>
                println("extractFromBsonObj; BsonArray case; calling goThrough")
                Some(goThroughArrayWithLimit(netty,valueLength,arrayFinishReaderIndex,keyList.drop(1),limitList)) match {
                  case Some(value) if value.isEmpty => None
                  case Some(value) => Some(resultComposer(value.toVector))
                }
              case _ =>
                println("constructing array matched")
                Some(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList).toArray[Any]) match {
                  case Some(value) if value.isEmpty => None
                  case Some(value) => Some(value)
                }
            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case "level" =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case "limitLevel" =>
                println("here")
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case _ =>
                val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            }
          }
        case D_BOOLEAN =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            val value: Int = netty.readByte()
            Some(value == 1)
          } else {
            netty.readByte()
            None
          }
        case D_NULL =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            Some("Null")
          } else {
            None
          }
        case D_INT =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            val value: Int = netty.readIntLE()
            Some(value)
          } else {
            netty.readIntLE()
            None
          }
        case D_LONG =>
          if (comparingFunction(netty, keyList.head._1) && !keyList.head._2.equals("limit") && !keyList.head._2.equals("next") && !keyList.head._2.equals("limitLevel")) {
            val value: Long = netty.readLongLE()
            Some(value)
          } else {
            netty.readLongLE()
            None
          }
        case D_ZERO_BYTE =>
          None
      }
    finalValue match {
      case None =>
        val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
        actualPos match {
          case x if x > 0 =>
            extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
          case 0 =>
            None
        }
      case Some(value) if keyList.head._2.equals("first") || keyList.head._2.equals("level") || (keyList.head._2.equals("limitLevel") && !keyList.head._1.eq("*"))  =>  //keyList.head._2.equals("level") ||
        netty.readerIndex(bsonFinishReaderIndex)
        Some(value).toVector
      case Some(_) =>
        val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
        actualPos match {
          case x if x > 0 =>
            (finalValue ++ extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)).toVector
          case 0 =>
            None
        }
    }
  }

  // Traverses the BsonArray looking for BsonObject or another BsonArray
  private def extractFromBsonArray(netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    keyList.head._1 match {
      case "" => // Constructs a new BsonArray, BsonArray is Root
          if (keyList.size < 2) {
            Some(traverseBsonArray(netty, length, arrayFRIdx, keyList, limitList).toArray[Any]) match {
              case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
              case Some(x) => Some(x)
            }
          } else {
            Some(goThroughArrayWithLimit(netty,length,arrayFRIdx,keyList.drop(1),limitList)) match {
              case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
              case Some(x) => x
            }
          }
      case "*" =>
        if (keyList.size < 2) {
          println("inside")
          Some(traverseBsonArray(netty, length, arrayFRIdx, keyList, limitList)) match {
            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
            case Some(x) => Some(x.toVector)
          }
      } else {
          Some(goThroughArrayWithLimit(netty,length,arrayFRIdx,keyList.drop(1),limitList)) match {
            case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
            case Some(x) => x
          }
        }
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
              //println(s"found BsonObject in this position of BsonArray, keylist: $keyList")
              val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
              if (midResult.isEmpty) None else {/*println(s"Finished BsonObject with ${resultComposer(midResult.toVector)}");println(s"current keylist: $keyList");*/Some(resultComposer(midResult.toVector))}
            case D_BSONARRAY =>
              val startReaderIndex: Int = netty.readerIndex()
              val valueLength2: Int = netty.readIntLE()
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
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
        finalValue match {
          case None =>
            val actualPos2: Int = arrayFRIdx - netty.readerIndex()
            actualPos2 match {
              case x if x > 0 =>
                extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
              case 0 =>
                None
            }
          case Some(_) =>
            keyList.head._2 match {
              case "first" =>
                finalValue
              case _ =>
                if (seqType2 != 0) {
                  finalValue ++ extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
                } else {
                  finalValue
                }
            }
        }
    }
  }

  private def extractKeys(netty: ByteBuf): Unit = {
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyDecode.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
  }

  private def containsKey(netty: ByteBuf, halfKey: String): Boolean = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
        while (netty.getByte(i) != 0) {
          list.append(netty.readByte())
          i += 1
        }
    netty.readByte()
    val extracted: String = list.map(b => b.toInt.toChar).toList.mkString
    extracted.contains(halfKey)
  }

  private def compareKeys(netty: ByteBuf, key: String): Boolean = {
    val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
    key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep | isHalfword(key, new String(arrKeyExtract.toArray))
  }

  private def resultComposer(list: Vector[Any]): Vector[Any] = {
    list match {
      case Seq() => Vector.empty[Any]
      case x +: Vector() if x.isInstanceOf[Vector[Any]] => (resultComposer(x.asInstanceOf[Vector[Any]]) +: Vector()).flatten
      case x +: IndexedSeq() => x +: Vector.empty[Any]
      case x +: xs if x.isInstanceOf[Vector[Any]] => (resultComposer(x.asInstanceOf[Vector[Any]]) +: resultComposer(xs) +: Vector()).flatten
      case x +: xs => ((x +: Vector()) +: resultComposer(xs) +: Vector()).flatten
    }
  }

  // Constructs a new BsonObject
  private def traverseBsonObj(netty: ByteBuf, mapper: Map[Any, Any], bsonFinishReaderIndex: Int, keyList: List[(String, String)],limitList: List[(Option[Int], Option[Int], String)]): Map[Any, Any] = {
    arrKeyDecode.clear()
    val seqType: Int = netty.readByte().toInt
    val newMap: Map[Any, Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          extractKeys(netty)
          val value: Double = netty.readDoubleLE()
          mapper + (new String(arrKeyDecode.toArray) -> value)
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          extractKeys(netty)
          val valueLength: Int = netty.readIntLE()
          val value: CharSequence = netty.readCharSequence(valueLength, charset)
          val newValue: String = value.toString.filter(p => p.!=(0))
          mapper + (new String(arrKeyDecode.toArray) -> newValue)
        case D_BSONOBJECT =>
          extractKeys(netty)
          val bsonStartReaderIndex: Int = netty.readerIndex()
          val valueTotalLength: Int = netty.readIntLE()
          val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
          val map: Map[Any, Any] = scala.collection.immutable.Map[Any, Any]()
          mapper + (new String(arrKeyDecode.toArray) -> traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList,limitList))
        case D_BSONARRAY =>
          extractKeys(netty)
          val arrayStartReaderIndex: Int = netty.readerIndex()
          val valueLength: Int = netty.readIntLE()
          val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
          mapper + (new String(arrKeyDecode.toArray) -> Compose.composer(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList).toArray))
        case D_BOOLEAN =>
          extractKeys(netty)
          val value: Int = netty.readByte()
          mapper + (new String(arrKeyDecode.toArray) -> (value == 1))
        case D_NULL =>
          extractKeys(netty)
          mapper + (new String(arrKeyDecode.toArray) -> null)
        case D_INT =>
          extractKeys(netty)
          val value: Int = netty.readIntLE()
          mapper + (new String(arrKeyDecode.toArray) -> value)
        case D_LONG =>
          extractKeys(netty)
          val value: Long = netty.readLongLE()
          mapper + (new String(arrKeyDecode.toArray) -> value)
        case D_ZERO_BYTE =>
          mapper
      }
    arrKeyDecode.clear()
    val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
    actualPos match {
      case x if x > 0 =>
        traverseBsonObj(netty, newMap, bsonFinishReaderIndex, keyList, limitList)
      case 0 =>
        newMap
    }
  }

  // Constructs a new BsonArray with limits
  private def traverseBsonArray(netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {

    def constructWithLimits(iter: Int): Iterable[Any] = {
      val seqType2: Int = netty.readByte().toInt
      if (seqType2 != 0) {
        readArrayPos(netty)
      }
      val newSeq: Option[Any] =
        seqType2 match {
          case D_FLOAT_DOUBLE =>
            val value: Double = netty.readDoubleLE()
            if(!keyList.head._2.equals("filter")){
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(value)
                  }
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(value)
                      }
                    case Some(_) => None
                    case None => Some(value)
                  }
              }
            } else {
              None
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            val valueLength: Int = netty.readIntLE()
            val field: CharSequence = netty.readCharSequence(valueLength - 1, charset)
            /*
            * filtrar field
            * */
            val newField: String = field.toString.filter(p => p != 0)
            netty.readByte()
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(newField)
                  }
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(newField)
                      }
                    case Some(_) => None
                    case None => Some(newField)
                  }
              }
            } else {
              None
            }
          case D_BSONOBJECT =>
//            println("traverseBsonArray found BsonObject")
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val map: Map[Any, Any] = scala.collection.immutable.Map[Any, Any]()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                keyList.head._2 match {
                  case "level" =>
                    val extracted: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                    if (extracted.size == 1) Some(extracted.head) else Some(extracted)  //TODO: rethink
                  case _ =>
                    //println(s"bsonObject, case _ , inside limits, keylist: $keyList")
                    val res: Map[Any, Any] = traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList, List((None,None,"")))
                    //println(s"res -> $res")
                    Some(res)
                }
              case Some(_) =>
                //println("not inside limits")
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get =>
                    //println(s"keyList inside limits: $keyList")
                    keyList.head._2 match {
                      case "level" =>
                        val extracted: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        //println(s"extracted in term 'all' -> $extracted")
                        if (extracted.size == 1) Some(extracted.head) else Some(extracted)  //TODO: rethink
                      case _ =>
                        Some(traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList,List((None,None,""))))
                    }
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case "filter" =>
                        val copyNetty: ByteBuf = netty.duplicate()
                        findElements(copyNetty,keyList,bsonFinishReaderIndex) match {
                          case Seq() =>
                            netty.readerIndex(copyNetty.readerIndex())
                            None
                          case _ =>
                            val res: Map[Any, Any] = traverseBsonObj(netty,map,bsonFinishReaderIndex,List((keyList.head._1,"all")),List((None,None,"")))  //TODO:refactor this section
                            if(keyList.size > 1) { //case when @elem ain't the last thing on keyList
                              if(keyList.drop(1).head._1.contains('*')){
                                //println("++++++"+res)
                                Some(res.collect{
                                  case elem if keyList.drop(1).head._1.split('*').forall(p => elem._1.asInstanceOf[String].contains(p))=>
                                   elem._2
                                })
                              } else {//println("getting ->"+res.get(keyList.drop(1).head._1))
                                res.get(keyList.drop(1).head._1)}
                            } else Some(res)
                        }
                      case _ => Some(traverseBsonObj(netty, map, bsonFinishReaderIndex, keyList,List((None,None,""))))
                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => Some(extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1)))
                    case _ => Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, List((None,None,""))).toArray[Any])
                  }
                case Some(_) =>
                  netty.readerIndex(finishReaderIndex)
                  None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => Some(extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList,limitList.drop(1)))
                        case _ => Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, List((None,None,""))).toArray[Any])
                      }
                    case Some(_) =>
                      netty.readerIndex(finishReaderIndex)
                      None
                    case None => Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList).toArray[Any])
                  }
              }
            } else {
              netty.readerIndex(finishReaderIndex)
              None
            }
          case D_BOOLEAN =>
            val value: Int = netty.readByte()
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(value == 1)
                  }
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(value == 1)
                      }
                    case Some(_) => None
                    case None => Some(value == 1)
                  }
              }
            } else {
              None
            }
          case D_NULL =>
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(null)
                  }
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(null)
                      }
                    case Some(_) => None
                    case None => Some(null)
                  }
              }
            } else {
              None
            }
          case D_INT =>
            val value: Int = netty.readIntLE()
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(value)
                  }
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      //println("inside limits")
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(value)
                      }
                    case Some(_) => None
                    case None => Some(value)
                  }
              }
            } else {
              None
            }
          case D_LONG =>
            val value: Long = netty.readLongLE()
            if(!keyList.head._2.equals("filter")) {
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.head._2 match {
                    case "level" => None
                    case _ => Some(value)
                  }
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.head._2 match {
                        case "level" => None
                        case _ => Some(value)
                      }
                    case Some(_) => None
                    case None => Some(value)
                  }
              }
            } else {
              None
            }
          case D_ZERO_BYTE =>
            None
        }
      val actualPos2: Int = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          newSeq ++ constructWithLimits(iter+1)
        case 0 =>
          newSeq
      }
    }
//    println(s"keyList: $keyList")
//    println(s"limitList: $limitList")
    val seq: Iterable[Any] = constructWithLimits(0)
    limitList.head._3 match {
      case "until" =>
        //println(s"before apply until -> $seq")
        //println(s"after apply until -> ${seq.take(seq.size-1)}")
        seq.take(seq.size-1)
      case _ =>
        //println(s"seq: $seq")
        seq
    }
  }

  private def goThroughArrayWithLimit(netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {

    val errorAnalyzer: (String) => Option[Char] = {
      case "until" | "to" => Some('!')
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
                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))  //check this drop
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
              case Some(_) =>
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get =>
                    println(s"inside limit, going to call extractFromBsonObj with keylist: $keyList")
                    val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                    if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    None
                  case None =>
                    println(s"going through arr, found BsonObj, matching keylist condition: ${keyList.head._2}")
                    keyList.head._2 match {
                      case "filter" =>
                        val copyNetty: ByteBuf = netty.duplicate()
                        findElements(copyNetty, keyList, bsonFinishReaderIndex) match {
                          case Seq() =>
                            netty.readerIndex(copyNetty.readerIndex())
                            None
                          case _ if keyList.size > 1 =>
                            val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList.drop(1), bsonFinishReaderIndex, limitList.drop(1))
                            if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                          case _ =>
                            val res: Map[Any, Any] = traverseBsonObj(netty,scala.collection.immutable.Map[Any, Any](),bsonFinishReaderIndex,List((keyList.head._1,"all")),limitList.drop(1))
                            Some(res)
                        }
                      case ("level" | "all" | "limitLevel") =>
                        println("goThrough; BsonObject case; condition = 'level' or 'all'")
                        val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      case "next" if keyList.head._1.equals("*") =>
                        println("goThrough; BsonObject case; condition = 'next' key = '*'")
                        val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList.drop(1))
                        if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      case _ =>
                        println("goThrough; BsonObject case; condition = '_'")
                        netty.readerIndex(bsonFinishReaderIndex)
                        None
                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            if(keyList.head._1.equals("")) {  //case [#..#].[#..#] TODO:test it
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                  keyList.size match {
                    case 1 => Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1)))
                    case _ =>
                      val midResult: Iterable[Any] = goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                      if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                  }
                case Some(_) =>
                  netty.readerIndex(finishReaderIndex)
                  None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get =>
                      keyList.size match {
                        case 1 => Some(traverseBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1)))
                        case _ =>
                          val midResult: Iterable[Any] = goThroughArrayWithLimit(netty, valueLength2, finishReaderIndex, keyList, limitList.drop(1))
                          if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
                      }
                    case Some(_) =>
                      netty.readerIndex(finishReaderIndex)
                      None
                    case None =>
                      netty.readerIndex(finishReaderIndex)
                      None // TODO:check this case
                  }
              }
            } else {
              netty.readerIndex(finishReaderIndex)
              limitList.head._2 match {
                case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get => errorAnalyzer(limitList.head._3)
                case Some(_) => None
                case None =>
                  limitList.head._1 match {
                    case Some(_) if iter >= limitList.head._1.get => errorAnalyzer(limitList.head._3)
                    case Some(_) => None
                    case None => None
                  }
              }} //this case needs to be more explored
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
          finalValue match {
            case Some(_) if flag.isDefined => throw new RuntimeException("Path of expression doesn't conform with the event")
            case Some(value) if value.equals('!') => finalValue ++ goThrough(iter+1,Some(true))
            case _ => finalValue ++ goThrough(iter+1)
          }
        case 0 => finalValue
      }
    }

    val seq: Iterable[Any] = goThrough(0)
    limitList.head._3 match {
      case "until" =>
        //println(s"case until -> $seq")
        val res: Vector[Any] = resultComposer(seq.take(seq.size-1).toVector)
        //res.take(res.size-1)
        res
      case _ =>
        if(seq.nonEmpty && seq.last.equals('!'))
          throw new RuntimeException("Path of expression doesn't conform with the event")
        else seq
    }
  }

  private def findElements(netty: ByteBuf, keyList: List[(String,String)], bsonFinishReaderIndex: Int): Iterable[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          if (comparingFunction(netty, keyList.head._1)) {
            val value: Double = netty.readDoubleLE()
            Some(value)
          } else {
            netty.readDoubleLE()
            None}
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          if (comparingFunction(netty, keyList.head._1)) {
            val valueLength: Int = netty.readIntLE()
            val arr: Array[Byte] = Unpooled.copiedBuffer(netty.readCharSequence(valueLength, charset), charset).array()
            val newArr: Array[Byte] = arr.filter(b => b!=0)
            Some(newArr)
          } else {
            netty.readCharSequence(netty.readIntLE(),charset)
            None}
        case D_BSONOBJECT =>
          if (comparingFunction(netty, keyList.head._1)) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val map: Map[Any, Any] = scala.collection.immutable.Map[Any, Any]()
            Some(traverseBsonObj(netty, map, bsonFinishReaderIndex, List((keyList.head._1,"all")), List((None,None,""))))
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            netty.readerIndex(bsonFinishReaderIndex)
            None}
        case D_BSONARRAY =>
          if (comparingFunction(netty, keyList.head._1)) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            Some(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, List((keyList.head._1,"all")),List((None,None,""))))
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            netty.readerIndex(arrayFinishReaderIndex)
            None}
        case D_BOOLEAN =>
          if (comparingFunction(netty, keyList.head._1)) {
            val value: Int = netty.readByte()
            Some(value == 1)
          } else {
            netty.readByte()
            None}
        case D_NULL =>
          if (comparingFunction(netty, keyList.head._1)) {
            Some("Null")
          } else None
        case D_INT =>
          if (comparingFunction(netty, keyList.head._1)) {
            val value: Double = netty.readIntLE()
            Some(value)
          } else {
            netty.readIntLE()
            None}
        case D_LONG =>
          if (comparingFunction(netty, keyList.head._1)) {
            val value: Double = netty.readLongLE()
            Some(value)
          } else {
            netty.readLongLE()
            None}
        case D_ZERO_BYTE => None
      }
    val actualPos2: Int = bsonFinishReaderIndex - netty.readerIndex()
    actualPos2 match {
      case x if x > 0 =>
        finalValue ++ findElements(netty,keyList,bsonFinishReaderIndex)
      case 0 =>
        finalValue
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

  def modify[T](nettyOpt: Option[BosonImpl], fieldID: String, f: (T) => T, selectType: String = ""): Option[BosonImpl] = {
    if (nettyOpt.isEmpty) {
      None
    } else {
      val netty: BosonImpl = nettyOpt.get
      val buffer: ByteBuf = netty.getByteBuf.duplicate()
      val buff: ByteBuf = Unpooled.buffer(4)
      buffer.getBytes(0, buff, 4)
      buff.readIntLE() // buffer.readIntLE()
      val seqType: Int = buffer.getByte(4).toInt
      seqType match {
        case 0 => None // end of obj
        case _ =>
          buffer.getByte(5).toInt match {
            case 48 => // root obj is BsonArray, call extractFromBsonArray
              if (fieldID.isEmpty) {
                Option(new BosonImpl())
              } else {
                val startRegionArray: Int = buffer.readerIndex()
                val valueTotalLength: Int = buffer.readIntLE()
                val indexOfFinishArray: Int = startRegionArray + valueTotalLength
                val (midResult, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer.duplicate(), fieldID, f)
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf) //  adding the global size to result buffer
                  Some(new BosonImpl(byteArray = Option(result.array())))
                } getOrElse {
                  None
                }
              }
            case _ => // root obj isn't BsonArray, call extractFromBsonObj
              if (fieldID.isEmpty) {
                Option(new BosonImpl())
              } else {
                val startRegion: Int = buffer.readerIndex()
                val valueTotalLength: Int = buffer.readIntLE()
                val indexOfFinish: Int = startRegion + valueTotalLength
                val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f, selectType)
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                  val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf)
                  val res = new BosonImpl(byteArray = Option(result.array()))
                  Some(res)
                } getOrElse {
                  None
                }
              }
          }
      }
    }
  }

  private def compareKeysInj(buffer: ByteBuf, key: String): Boolean = {
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    buffer.readByte() // consume the end String byte
    key.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep | isHalfword(key,new String(fieldBytes.toArray))
  }

  private def matcher[T](buffer: ByteBuf, fieldID: String, indexOfFinish: Int, f: T => T, selectType: String = ""): (Option[ByteBuf], Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    if (startReaderIndex < (indexOfFinish - 1)) { //  goes through entire object
      val seqType: Int = buffer.readByte().toInt
      if (compareKeysInj(buffer, fieldID)) { //  changes value if keys match
        val indexTillInterest: Int = buffer.readerIndex()
        val bufTillInterest: ByteBuf = buffer.slice(4, indexTillInterest - 4)
        val (bufWithNewValue, diff): (ByteBuf, Int) = modifier(buffer, seqType, f) //  change the value
        val indexAfterInterest: Int = buffer.readerIndex()
        val bufRemainder: ByteBuf = buffer.slice(indexAfterInterest, buffer.capacity() - indexAfterInterest)
        val midResult: ByteBuf = Unpooled.wrappedBuffer(bufTillInterest, bufWithNewValue, bufRemainder)
        (Some(midResult), diff)
      } else {
        processTypes(buffer, seqType, fieldID, f) match { //  consume the bytes of value, NEED to check for bsobj and bsarray before consume
          case Some((buf, diff)) => (Some(buf), diff)
          case None => matcher(buffer, fieldID, indexOfFinish, f)
        }
      }
    } else {
      buffer.readByte()
      (None, 0)
    }
  }

  private def modifier[T <: Any](buffer: ByteBuf, seqType: Int, f: T => T): (ByteBuf, Int) = {
    val newBuffer: ByteBuf = Unpooled.buffer() //  corresponds only to the new value
    seqType match {
      //  TODO: rethink and write it in a proper way
      case D_FLOAT_DOUBLE =>
        val value: Double =
          Try(f(buffer.getDoubleLE(buffer.readerIndex()).toFloat.asInstanceOf[T])) match {
            case Success(v) =>
              buffer.readDoubleLE() //consume
              v.asInstanceOf[Float].toDouble
            case Failure(_) =>
              Try(f(buffer.getDoubleLE(buffer.readerIndex()).asInstanceOf[T])) match {
                case Success(v) =>
                  buffer.readDoubleLE() //consume
                  v.asInstanceOf[Double]
                case Failure(m) =>
                  throw new RuntimeException(m)
              }
          }
        (newBuffer.writeDoubleLE(value), 0)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val chrSeq: CharSequence = buffer.getCharSequence(buffer.readerIndex(), length, charset).toString.replaceAll("\\p{C}", "")
        val value: Any =
          Try(f(new String(Unpooled.copiedBuffer(chrSeq, charset).array()).asInstanceOf[T])) match {
            case Success(v) => //String and Enum.toString
              buffer.readCharSequence(length, charset) //consume
              v.asInstanceOf[String]
            case Failure(_) =>
              Try(f(Unpooled.copiedBuffer(chrSeq, charset).array().asInstanceOf[T])) match {
                case Success(v) => //Array[Byte]
                  buffer.readCharSequence(length, charset) //consume
                  v.asInstanceOf[Array[Byte]]
                case Failure(_) =>
                  Try(f(chrSeq.asInstanceOf[T])) match {
                    case Success(v) => //CharSequence
                      buffer.readCharSequence(length, charset) //consume
                      v.asInstanceOf[CharSequence]
                    case Failure(_) =>
                      Try(f(Instant.parse(chrSeq).asInstanceOf[T])) match {
                        case Success(v) => //Instant
                          buffer.readCharSequence(length, charset) //consume
                          v.asInstanceOf[Instant]
                        case Failure(m) =>
                          throw new RuntimeException(m)
                      }
                  }
              }
          }
        value match {
          case n: Array[Byte] =>
            (newBuffer.writeIntLE(n.length + 1).writeBytes(n).writeByte(0), (n.length + 1) - length)
          case n: String =>
            val aux: Array[Byte] = n.getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0), (aux.length + 1) - length)
          case n: Instant =>
            val aux: Array[Byte] = n.toString.getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0), (aux.length + 1) - length)
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex()) //  length of current obj
      val bsonObject: Map[String, Any] = decodeBsonObject(buffer.readBytes(valueLength ))
        val newValue: Any = applyFunction(f, bsonObject)
        newValue match {
          case bsonObject1: java.util.Map[_, _] =>
            val buf: ByteBuf = encode(bsonObject1)
            (newBuffer.writeBytes(buf), buf.capacity() - valueLength)
          case bsonObject2: Map[String@unchecked, _] =>
            val buf: ByteBuf = encode(bsonObject2)
            (newBuffer.writeBytes(buf), buf.capacity() - valueLength)
          case _ =>
            if (newValue == null) {
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            } else {
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            }
        }
      case D_BSONARRAY =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonArray: List[Any] = decodeBsonArray(buffer.readBytes(valueLength))
        val newValue: T = applyFunction(f, bsonArray)
        newValue match {
          case bsonArray1: java.util.List[_] =>
            val arr:ByteBuf = encode(bsonArray1)
            (newBuffer.writeBytes(arr), arr.capacity() - valueLength) //  ZERO  for now, cant be zero
          case bsonArray2: List[Any] =>
            val arr: ByteBuf = encode(bsonArray2)
            (newBuffer.writeBytes(arr), arr.capacity() - valueLength) //  ZERO  for now, cant be zero
          case _ =>
            if (newValue == null) {
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)")
            } else {
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONARRAY (java List or scala Array)")
            }
        }
      case D_BOOLEAN =>
        val value: Boolean =
          Try(f(buffer.readBoolean().asInstanceOf[T])) match {
            case Success(v) =>
              v.asInstanceOf[Boolean]
            case Failure(m) =>
              throw new RuntimeException(m)
          }
        (newBuffer.writeBoolean(value), 0)
      case D_NULL => throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value: Int =
          Try(f(buffer.readIntLE().asInstanceOf[T])) match {
            case Success(v) =>
              v.asInstanceOf[Int]
            case Failure(m) =>
              throw new RuntimeException(m)
          }
        (newBuffer.writeIntLE(value), 0)
      case D_LONG =>
        val value: Long =
          Try(f(buffer.readLongLE().asInstanceOf[T])) match {
            case Success(v) =>
              v.asInstanceOf[Long]
            case Failure(m) =>
              throw new RuntimeException(m)
          }
        (newBuffer.writeLongLE(value), 0)
    }
  }

  private def findBsonObjectWithinBsonArray[T](buffer: ByteBuf, fieldID: String, f: T => T): (Option[ByteBuf], Int) = {
    val seqType: Int = buffer.readByte()
    if (seqType == 0) {
      (None, 0)
    } else {
      val index: Char = readArrayPos(buffer)
      processTypes(buffer, seqType, fieldID, f) match {
        case Some(elem) =>
          (Some(elem._1), elem._2)
        case None =>
          findBsonObjectWithinBsonArray(buffer, fieldID, f)
      }
    }
  }

  private def processTypes[T](buffer: ByteBuf, seqType: Int, fieldID: String, f: T => T): Option[(ByteBuf, Int)] = {
    seqType match {
      case D_ZERO_BYTE =>
        None
      case D_FLOAT_DOUBLE =>
        buffer.readDoubleLE()
        None
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        val b: ByteBuf = buffer.readBytes(valueLength)
        b.release()
        None
      case D_BSONOBJECT =>
        val startRegion: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.readIntLE()
        val indexOfFinish: Int = startRegion + valueTotalLength
        val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
        midResult map { b =>
          val oneBuf: ByteBuf = b.slice(0, startRegion - 4)
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size//previous till next object size
          val threeBuf: ByteBuf = b.slice(startRegion, b.capacity() - startRegion) //  from size till end
          (Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf), diff) //  previous buffs together
        }
      case D_BSONARRAY =>
        val startRegion: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.readIntLE()
        val (result, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer, fieldID, f)
        result map { b =>
          val oneBuf: ByteBuf = b.slice(0, startRegion - 4)
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size//previous till next object size
          val threeBuf: ByteBuf = b.slice(startRegion, b.capacity() - startRegion) //  from size till end
          (Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf), diff) //  previous buffs together
        }
      case D_BOOLEAN =>
        buffer.readByte()
        None
      case D_NULL =>
        None
      case D_INT =>
        buffer.readIntLE()
        None
      case D_LONG =>
        buffer.readLongLE()
        None
      case _ =>
        None
    }
  }

  private def isHalfword(fieldID: String, extracted: String): Boolean = {
    if(fieldID.contains('*')){
      val list: Array[String] = fieldID.split('*')
      list.forall(str => extracted.contains(str))
    }else{
      false
    }
  }

  def modifyAll[T](list: List[(Statement, String)],buffer:ByteBuf, fieldID:String, f:T=>T, result:ByteBuf=Unpooled.buffer()):ByteBuf={
    /*
    * Se fieldID for vazia devolve o Boson Origina
    * */
    val startReader: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    val resultSizeBuffer: ByteBuf = Unpooled.buffer(4)
    while((buffer.readerIndex()-startReader)<originalSize) {
      val dataType: Int = buffer.readByte().toInt
      dataType match {
        case 0 =>
          result.writeByte(dataType)
        case _ =>
          result.writeByte(dataType)
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.length<1) {
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
              if(list.size==1){
                modifierAll(buffer, dataType, f, result)
              }else{
                if(list.head._2.contains("..")){
                  val size:Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readBytes(size)
                  val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                  val buf3: ByteBuf = execStatementPatternMatch(buf2.duplicate(), list, f)
                  result.writeBytes(buf3)
                }else{

                  execStatementPatternMatch(buffer, list.drop(1), f, result)
                }
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x)=>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              if(list.head._2.contains(".."))
                processTypesAll(list, dataType,buffer,result,fieldID,f)
              else
                processTypesArray(dataType,buffer,result)
          }
      }
    }
    result.capacity(result.writerIndex())
    Unpooled.buffer(result.capacity()+4).writeIntLE(result.capacity()+4).writeBytes(result)
  }

  private def processTypesArray(dataType: Int, buffer: ByteBuf, result: ByteBuf) = {
    dataType match {
      case D_ZERO_BYTE =>
      case D_FLOAT_DOUBLE =>
        result.writeDoubleLE(buffer.readDoubleLE())
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        result.writeIntLE(valueLength)
        result.writeCharSequence(buffer.readCharSequence(valueLength,charset),charset)
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
      case _ =>
    }
  }

  private def modifierAll[T](buffer: ByteBuf, seqType: Int, f: T => T, result: ByteBuf): Unit = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0: Any = buffer.readDoubleLE()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Float) =>
            result.writeDoubleLE(n)
          case Some(n: Double) =>
            result.writeDoubleLE(n)
          case Some(n) =>
            throw CustomException(s"Wrong inject type. Injecting type ${n.getClass.getSimpleName}. Value type require D_FLOAT_DOUBLE")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_FLOAT_DOUBLE")
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val array: ByteBuf = Unpooled.buffer(length).writeBytes(buffer.readCharSequence(length,charset).toString.getBytes)
        val value: Any = applyFunction(f, new String(array.array()))
        Option(value) match {
          case Some(n: Array[Byte]) =>
            result.writeIntLE(n.length).writeBytes(n).writeZero(1)
          case Some(n: String) =>
            val aux: Array[Byte] = n.getBytes()
            result.writeIntLE(aux.length+1).writeBytes(aux).writeZero(1)
          case Some(n: Instant) =>
            val aux: Array[Byte] = n.toString.getBytes()
            result.writeIntLE(aux.length).writeBytes(aux).writeZero(1)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonObj: Map[String, Any] = decodeBsonObject(buffer.readBytes(valueLength))
        val newValue: Any = applyFunction(f, bsonObj)
        Option(newValue) match {
          case Some(x: util.Map[String@unchecked, _])  =>
            Try(encode(x)) match {
              case Success(v)=> (result.writeBytes(v), v.capacity()-valueLength)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x: Map[String@unchecked, _])  =>
            Try(encode(x)) match {
              case Success(v)=>
                (result.writeBytes(v), v.capacity()-valueLength)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x)=>
            throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
        }
      case D_BSONARRAY =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonArray: List[Any] = decodeBsonArray(buffer.readBytes(valueLength ))
        val value: Any = applyFunction(f, bsonArray)
        Option(value) match {
          case Some(x:util.List[_]) =>
            Try(encode(x)) match {
              case Success(v)=> (result.writeBytes(v), v.capacity()-valueLength)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x:List[Any]) =>
            Try(encode(x)) match {
              case Success(v)=> (result.writeBytes(v), v.capacity()-valueLength)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x:Array[Byte]) =>
            Try(encode(x)) match {
              case Success(v)=> (result.writeBytes(v), v.capacity()-valueLength)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_BSONARRAY (java List or scala Array)")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)")
        }
      case D_BOOLEAN =>
        val value0: Boolean = buffer.readBoolean()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(bool: Boolean) =>
            result.writeBoolean(bool)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_BOOLEAN")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BOOLEAN")
        }
      case D_NULL =>  throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value0: Any = buffer.readIntLE()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Int) =>
            result.writeIntLE(n)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_INT")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_INT")
        }
      case D_LONG =>
        val value0: Any = buffer.readLongLE()
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Long) =>
            result.writeLongLE(n)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_LONG")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_LONG")
        }
    }
  }

  private def applyFunction[T](f: T => T, value: Any) : T = {
    Try(f(value.asInstanceOf[T])) match {
      case Success(v) => v.asInstanceOf[T]
      case Failure(e) => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
    }
  }

  private def processTypesAll[T](list: List[(Statement, String)], seqType: Int, buffer: ByteBuf, result: ByteBuf, fieldID: String, f: T => T):Unit = {
    seqType match {
      case D_ZERO_BYTE =>
        result.writeZero(1)
      case D_FLOAT_DOUBLE =>
        result.writeDoubleLE(buffer.readDoubleLE())
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        result.writeIntLE(valueLength)
        result.writeBytes(buffer.readBytes(valueLength))
      case D_BSONOBJECT =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux:ByteBuf = modifyAll(list,bsonBuf, fieldID, f)
        result.writeBytes(resultAux)
      case D_BSONARRAY =>
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux: ByteBuf = modifyAll(list,bsonBuf, fieldID, f)
        result.writeBytes(resultAux)
      case D_NULL =>
      case D_INT =>
        result.writeIntLE(buffer.readIntLE())
      case D_LONG =>
        result.writeLongLE(buffer.readLongLE())
      case D_BOOLEAN =>
        result.writeBoolean(buffer.readBoolean())
      case _ =>
    }
  }

  def encode(bson: Any): ByteBuf = {
    val res: ByteBuf =  bson match {
      case list: util.List[_] => encodeBsonArray(list.asScala.toList)
      case list: List[Any] => encodeBsonArray(list)
      case map : util.Map[String@unchecked, _] => encodeBsonObject(map.asScala.toMap)
      case map : Map[String@unchecked, _] => encodeBsonObject(map)
      case array: Array[Byte] => encodeBsonArray(array.toList)
      case _ => throw CustomException("Wrong input type.")
    }
    if(res.hasArray) {
      res.array()
    }else{
      res.duplicate().array()
    }
    res
  }

  private def encodeBsonArray(list: List[Any]): ByteBuf = {
    val bufSize: ByteBuf = Unpooled.buffer(4)
    val buf: ByteBuf = Unpooled.buffer()
    val numElems: Int = list.size
    for( num <- 0 until numElems){
      val elem: Any = list(num)
      elem match {
        case x: Float =>
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(num.toString.getBytes).writeZero(1).writeDoubleLE(x.toDouble)
        case x: Double =>
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(num.toString.getBytes).writeZero(1).writeDoubleLE(x)
        case x: Array[Byte] =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x).writeZero(1)
        case x: Instant =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.toString.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: String =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x.getBytes).writeZero(1)
        case x: CharSequence =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: Map[String@unchecked, _] =>
          buf.writeByte(D_BSONOBJECT).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonObject(x))
        case x: List[Any] =>
          buf.writeByte(D_BSONARRAY).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonArray(x))
        case x if Option(x).isEmpty  =>
          buf.writeByte(D_NULL).writeBytes(num.toString.getBytes).writeZero(1)
        case x: Int =>
          buf.writeByte(D_INT).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x)
        case x: Long =>
          buf.writeByte(D_LONG).writeBytes(num.toString.getBytes).writeZero(1).writeLongLE(x)
        case x: Boolean =>
          buf.writeByte(D_BOOLEAN).writeBytes(num.toString.getBytes).writeZero(1).writeBoolean(x)
        case _ =>
      }
    }
    buf.writeZero(1)
    buf.capacity(buf.writerIndex())
    bufSize.writeIntLE(buf.capacity()+4)
    Unpooled.buffer(bufSize.capacity()+buf.capacity()).writeBytes(bufSize).writeBytes(buf)
}

  private def encodeBsonObject(map: Map[String, Any]): ByteBuf = {
    val bufSize: ByteBuf = Unpooled.buffer(4)
    val buf: ByteBuf = Unpooled.buffer()
    val numElems: List[(String, Any)] = map.toList
    for( num <- numElems){
      val elem: (String, Any) = num
      elem._2 match {
        case x: Float =>
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(elem._1.getBytes()).writeZero(1).writeDoubleLE(x.toDouble)
        case x: Double =>
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(elem._1.getBytes()).writeZero(1).writeDoubleLE(x)
        case x: Array[Byte] =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x).writeZero(1)
        case x: Instant =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.toString.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: String =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x.getBytes).writeZero(1)
        case x: CharSequence =>
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: Map[String@unchecked, _] =>
          buf.writeByte(D_BSONOBJECT).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonObject(x))
        case x: List[Any] =>
          buf.writeByte(D_BSONARRAY).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonArray(x))
        case x if Option(x).isEmpty  =>
          buf.writeByte(D_NULL).writeBytes(elem._1.getBytes()).writeZero(1)
        case x: Int =>
          buf.writeByte(D_INT).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x)
        case x: Long =>
          buf.writeByte(D_LONG).writeBytes(elem._1.getBytes()).writeZero(1).writeLongLE(x)
        case x: Boolean =>
          buf.writeByte(D_BOOLEAN).writeBytes(elem._1.getBytes()).writeZero(1).writeBoolean(x)
        case _ =>
      }
    }
    buf.writeZero(1)
    buf.capacity(buf.writerIndex())
    bufSize.writeIntLE(buf.capacity()+4)
    Unpooled.copiedBuffer(bufSize, buf)
  }

  def decodeBsonArray(buf: ByteBuf): List[Any] = {
     val startIndex: Int = buf.readerIndex()
     val list: ListBuffer[Any] = new ListBuffer[Any]
     val bufSize: Int = buf.readIntLE()
     while((buf.readerIndex()-startIndex)<bufSize) {
       val dataType: Int = buf.readByte()
       dataType match{
         case 0 =>
         case _ =>
           val key: ListBuffer[Byte] = new ListBuffer[Byte]
           while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
             val b: Byte = buf.readByte()
             key.append(b)
           }
           val b: Byte = buf.readByte()
           key.append(b)
           dataType match {
             case D_FLOAT_DOUBLE =>
               val number: Double = buf.readDoubleLE()
               list.append(number)
             case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
               val size: Int = buf.readIntLE()
               val str: Array[Byte] = Unpooled.copiedBuffer(buf.readBytes(size)).array()

               list.append(new String(str))
             case D_BSONOBJECT =>
               val bsonSize: Int = buf.getIntLE(buf.readerIndex())
               val bson: ByteBuf = buf.readBytes(bsonSize)
               val res: Map[String, _] = decodeBsonObject(bson)
               bson.release()
               list.append(res)
             case D_BSONARRAY =>
               val bsonSize: Int = buf.getIntLE(buf.readerIndex())
               val bson: ByteBuf = buf.readBytes(bsonSize)
               val res: List[Any] = decodeBsonArray(bson)
               bson.release()
               list.append(res)
             case D_INT =>
               val int: Int = buf.readIntLE()
               list.append(int)
             case D_NULL =>
             case D_LONG =>
               val long: Long = buf.readLongLE()
               list.append(long)
             case D_BOOLEAN =>
               val bool: Boolean = buf.readBoolean()
               list.append(bool)
             case _ =>
           }
         }
     }
    list.toList
    }

  def decodeBsonObject(buf: ByteBuf): Map[String, Any] = {
     val startIndex: Int = buf.readerIndex()
     val map =  new mutable.TreeMap[String, Any]()
     val bufSize: Int = buf.readIntLE()
     while((buf.readerIndex()-startIndex)<bufSize) {
       val dataType: Int = buf.readByte()
        dataType match {
          case 0 =>
          case _ =>
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            val strKey: String = new String(key.toArray)
            dataType match {
              case D_FLOAT_DOUBLE =>
                val number: Double = buf.readDoubleLE()
                map.put(strKey, number)
              case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
                val size: Int = buf.readIntLE()
                val str: Array[Byte] = Unpooled.copiedBuffer(buf.readCharSequence(size-1,charset).toString.getBytes).array()
                map.put(strKey, new String(str))
              case D_BSONOBJECT =>
                val bsonSize: Int = buf.getIntLE(buf.readerIndex())
                val bson: ByteBuf = buf.readBytes(bsonSize)
                val res: Map[String, Any] = decodeBsonObject(bson)
                map.put(strKey, res)
              case D_BSONARRAY =>
                val bsonSize: Int = buf.getIntLE(buf.readerIndex())
                val bson: ByteBuf = buf.readBytes(bsonSize)
                val res: List[Any] = decodeBsonArray(bson)
                map.put(strKey, res)
              case D_INT =>
                val int: Int = buf.readIntLE()
                map.put(strKey, int)
              case D_NULL =>
                map.put(strKey, null)
              case D_LONG =>
                val long: Long = buf.readLongLE()
                map.put(strKey, long)
              case D_BOOLEAN =>
                val bool: Boolean = buf.readBoolean()
                map.put(strKey, bool)
              case _ =>
            }
        }
     }
  map.toMap
  }

  private def readArrayPos(netty: ByteBuf): Char = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    while (netty.getByte(netty.readerIndex()) != 0) {
      list.+=(netty.readByte())
    }
    list.+=(netty.readByte()) //  consume the end Pos byte
    val stringList: ListBuffer[Char] = list.map(b => b.toInt.toChar)
    stringList.head
  }

  def modifyEnd[T](buffer: ByteBuf, fieldID:String, f:T=>T, result:ByteBuf=Unpooled.buffer(), resultCopy:ByteBuf=Unpooled.buffer()):(BosonImpl, BosonImpl)={
    val buf: ByteBuf = buffer.duplicate()
    val bufSize: Int = buf.readIntLE()
    while(buf.readerIndex() < bufSize){
      val dataType: Int = buf.readByte()
      result.writeByte(dataType)
      resultCopy.writeByte(dataType)
      dataType match{
        case 0 =>
        case _ =>
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(key).writeByte(b)
          resultCopy.writeBytes(key).writeByte(b)
          new String(key) match {
            case x if fieldID.toCharArray.deep == x.toCharArray.deep | isHalfword(fieldID, x)  =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              result.clear().writeBytes(resultCopy.duplicate())
              modifierEnd(buf, dataType, f, result, resultCopy)
            case x if fieldID.toCharArray.deep != x.toCharArray.deep =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              processTypesEnd(dataType, fieldID, buf, f, result, resultCopy)
          }
      }
    }
    result.capacity(result.writerIndex())
    resultCopy.capacity(resultCopy.writerIndex())
    val a: ByteBuf = Unpooled.buffer(result.capacity()+4).writeIntLE(result.capacity()+4).writeBytes(result)
    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity()+4).writeIntLE(resultCopy.capacity()+4).writeBytes(resultCopy)
    (new BosonImpl(byteArray = Option(a.array())),new BosonImpl(byteArray = Option(b.array())))
  }

  private def processTypesEnd[T](dataType: Int, fieldID: String, buf: ByteBuf, f: (T) => T, result: ByteBuf, resultCopy: ByteBuf) = {
    dataType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buf.readDoubleLE()
        result.writeDoubleLE(value0)
        resultCopy.writeDoubleLE(value0)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buf.readIntLE()
        val bytes: ByteBuf = buf.readBytes(valueLength)
        result.writeIntLE(valueLength)
        result.writeBytes(bytes)
        resultCopy.writeIntLE(valueLength)
        resultCopy.writeBytes(bytes)
      case D_BSONOBJECT =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readBytes(length)
        val resultAux: (BosonImpl,BosonImpl) = modifyEnd(bsonBuf, fieldID, f)
        val buf0: Array[Byte] = resultAux._1.getByteBuf.array()
        val buf1: Array[Byte] = resultAux._2.getByteBuf.array()
        if(buf0.zip(buf1).forall(p => p._1==p._2)){
          result.writeBytes(resultAux._1.getByteBuf)
          resultCopy.writeBytes(resultAux._2.getByteBuf)
        }else{
          result.clear().writeBytes(resultCopy.duplicate()).writeBytes(resultAux._1.getByteBuf)
          resultCopy.writeBytes(resultAux._2.getByteBuf)
        }
      case D_BSONARRAY =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readBytes(length)
        val resultAux: (BosonImpl,BosonImpl) = modifyEnd(bsonBuf, fieldID, f)
       val buf0: Array[Byte] = resultAux._1.getByteBuf.array()
        val buf1: Array[Byte] = resultAux._2.getByteBuf.array()
        if(buf0.zip(buf1).forall(p => p._1==p._2)){
          result.writeBytes(resultAux._1.getByteBuf)
          resultCopy.writeBytes(resultAux._2.getByteBuf)
        }else{
          result.clear().writeBytes(resultCopy.duplicate()).writeBytes(resultAux._1.getByteBuf)
          resultCopy.writeBytes(resultAux._2.getByteBuf)
        }
      case D_NULL =>
      case D_INT =>
        val value0: Int = buf.readIntLE()
        result.writeIntLE(value0)
        resultCopy.writeIntLE(value0)
      case D_LONG =>
        val value0: Long = buf.readLongLE()
        result.writeLongLE(value0)
        resultCopy.writeLongLE(value0)
      case D_BOOLEAN =>
        val value0: Boolean = buf.readBoolean()
        result.writeBoolean(value0)
        resultCopy.writeBoolean(value0)
      case _ =>
    }
  }

  private def modifierEnd[T](buffer: ByteBuf, seqType: Int, f: T => T, result: ByteBuf, resultCopy: ByteBuf): Unit = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buffer.readDoubleLE()
        resultCopy.writeDoubleLE(value0.asInstanceOf[Double])
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Float) =>
            result.writeDoubleLE(n)
          case Some(n: Double) =>
            result.writeDoubleLE(n)
          case Some(n) =>
            throw CustomException(s"Wrong inject type. Injecting type ${n.getClass.getSimpleName}. Value type require D_FLOAT_DOUBLE")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_FLOAT_DOUBLE")
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val value0: Array[Byte] = Unpooled.buffer(length-1).writeBytes(buffer.readBytes(length-1)).array()
        resultCopy.writeIntLE(length).writeBytes(value0)
         buffer.readByte()
        val value: Any = applyFunction(f, new String(value0))
        Option(value) match {
          case Some(n: Array[Byte]) =>
            result.writeIntLE(n.length ).writeBytes(n).writeByte(0)
          case Some(n: String) =>
            val aux: Array[Byte] = n.getBytes()
            result.writeIntLE(aux.length ).writeBytes(aux).writeByte(0)
          case Some(n: Instant) =>
            val aux: Array[Byte] = n.toString.getBytes()
            result.writeIntLE(aux.length ).writeBytes(aux).writeByte(0)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonObj: ByteBuf = buffer.readBytes(valueLength)
        resultCopy.writeBytes(bsonObj.duplicate())
        val map: Map[String, Any] = decodeBsonObject(bsonObj.duplicate())
        val newValue: Any = applyFunction(f, map)
        Option(newValue) match {
          case Some(x: util.Map[String@unchecked, _])  =>
            Try(encode(x)) match {
              case Success(v)=>
                result.writeBytes(v)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x: Map[String@unchecked, _])  =>
            Try(encode(x)) match {
              case Success(v)=>
                result.writeBytes(v)
                //resultCopy.writeBytes(bsonObj)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x)=>
            throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
        }
      case D_BSONARRAY =>
        val valueLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonArray: ByteBuf = buffer.readBytes(valueLength)
        resultCopy.writeBytes(bsonArray.duplicate())
        val list: List[Any] = decodeBsonArray(bsonArray.duplicate())
        val value: Any = applyFunction(f, list)
        Option(value) match {
          case Some(x:util.List[_]) =>
            Try(encode(x)) match {
              case Success(v)=>
                result.writeBytes(v)
              case Failure(e) => throw  CustomException(e.getMessage)
            }
          case Some(x:List[Any]) =>
            Try(encode(x)) match {
              case Success(v) =>
                result.writeBytes(v)
              case Failure(e) => throw CustomException(e.getMessage)
            }
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_BSONARRAY (java List or scala Array)")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)")
        }
      case D_BOOLEAN =>
        val value0: Boolean = buffer.readBoolean()
        resultCopy.writeBoolean(value0)
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(bool: Boolean) =>
            result.writeBoolean(bool)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${x.getClass.getSimpleName}. Value type require D_BOOLEAN")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BOOLEAN")
        }
      case D_NULL =>  throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value0: Int = buffer.readIntLE()
        resultCopy.writeIntLE(value0)
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Int) =>
            result.writeIntLE(n)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_INT")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_INT")
        }
      case D_LONG =>
        val value0: Long = buffer.readLongLE()
        resultCopy.writeLongLE(value0)
        val value: Any = applyFunction(f, value0)
        Option(value) match {
          case Some(n: Long) =>
            result.writeLongLE(n)
          case Some(x) =>
            throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_LONG")
          case None =>
            throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_LONG")
        }
    }
  }

  def modifyArrayEnd[T](list:List[(Statement, String)],buffer: ByteBuf, f:T=>T,condition: String, limitInf:String = "0",limitSup:String = "end", result:ByteBuf=Unpooled.buffer(), resultCopy:ByteBuf=Unpooled.buffer()):BosonImpl= {
    /*
    * Se fieldID for vazia devolve o Boson Original
    * */
    val startReaderIndex: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    val exceptions: ListBuffer[Throwable] = new ListBuffer[Throwable]
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
          (keyString, limitSup) match {
            case (x, "end") if isArray && limitInf.toInt <= keyString.toInt =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              if (list.size == 1) {
                if (exceptions.isEmpty) {
                  resultCopy.clear().writeBytes(result.duplicate())
                  Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                    case Success(_) =>
                    case Failure(e) => exceptions.append(e)
                  }
                } else exceptions.append(exceptions.head)
              } else {
                if (list.head._2.contains("..") && dataType==(D_BSONARRAY | D_BSONOBJECT)  /*&& list.head._1.isInstanceOf[ArrExpr]*/) {
                  resultCopy.clear().writeBytes(result.duplicate())
                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.duplicate().readBytes(size)
                  val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                  val buf3: ByteBuf = execStatementPatternMatch(buf2.duplicate(), list, f)
                  result.writeBytes(buf3)
                  processTypesArray(dataType, buffer, resultCopy)
                  buf1.release()
                  buf2.release()
                  buf3.release()
                } else {
                  resultCopy.clear().writeBytes(result.duplicate())
                  val res: ByteBuf = execStatementPatternMatch(buffer.duplicate(), list.drop(1), f)
                  result.writeBytes(res)
                  processTypesArray(dataType, buffer, resultCopy)
                  res.release()
                }
              }
            case (x, "end") if isArray && limitInf.toInt > keyString.toInt =>
              if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/)
                processTypesArrayEnd(list, "", dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
              else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
            case (x, l) if isArray && (limitInf.toInt <= x.toInt && l.toInt >= x.toInt) =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              if (list.size == 1) {
                if (exceptions.isEmpty) {
                  resultCopy.clear().writeBytes(result.duplicate())
                  Try(modifierEnd(buffer, dataType, f, result, resultCopy)) match {
                    case Success(_) =>
                    case Failure(e) => exceptions.append(e)
                  }
                } else exceptions.append(exceptions.head)
              } else {
                if (list.head._2.contains("..")/*&& list.head._1.isInstanceOf[ArrExpr]*/) {
                  resultCopy.clear().writeBytes(result.duplicate())
                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.duplicate().readBytes(size)
                  val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                  val buf3: ByteBuf = execStatementPatternMatch(buf2, list, f)
                  result.writeBytes(buf3)
                  processTypesArray(dataType, buffer, resultCopy)
                  buf1.release()
                  buf2.release()
                  buf3.release()
                } else {
                  resultCopy.clear().writeBytes(result.duplicate())
                  val res: ByteBuf = execStatementPatternMatch(buffer.duplicate(), list.drop(1), f)
                  result.writeBytes(res)
                  processTypesArray(dataType, buffer, resultCopy)
                  res.release()
                }

              }
            case (x, l) if isArray && (limitInf.toInt > x.toInt || l.toInt < x.toInt) =>
              if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/)
                processTypesArrayEnd(list, "", dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
              else {
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }

            case (x, l) if !isArray => // big TODO ..[#]
              if (list.head._2.contains("..") /*&& list.head._1.isInstanceOf[ArrExpr]*/) {
                dataType match {
                  case (D_BSONOBJECT | D_BSONARRAY) =>
                    val size: Int = buffer.getIntLE(buffer.readerIndex())
                    println(size)
                    println(list.head)
                    val buf1: ByteBuf = buffer.duplicate().readBytes(size)
                    Unpooled.buffer(buf1.capacity()).writeBytes(buf1.duplicate()).array().foreach(b=>println(b.toChar + "   " + b))
                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
                    result.writeBytes(buf2)
                    processTypesArray(dataType, buffer, resultCopy)
                    buf1.release()
                    buf2.release()
                  case _ =>
                    processTypesArray(dataType, buffer.duplicate(), result)
                    processTypesArray(dataType, buffer, resultCopy)
                }
              } else throw CustomException("Root is not a BsonArray")
          }
      }
    }
    result.capacity(result.writerIndex())
    resultCopy.capacity(resultCopy.writerIndex())
    val a: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity() + 4).writeIntLE(resultCopy.capacity() + 4).writeBytes(resultCopy)
    if (condition.equals("to"))
      if (exceptions.isEmpty) {
        val bson: BosonImpl = new BosonImpl(byteArray = Option(a.array()))
        a.release()
        b.release()
        bson
      } else
        throw exceptions.head
    else if (exceptions.length <= 1){
        val bson: BosonImpl = new BosonImpl(byteArray = Option(b.array()))
        a.release()
        b.release()
        bson
      }else
        throw exceptions.drop(1).head
  }

  private def processTypesArrayEnd[T](list: List[(Statement, String)],fieldID: String, dataType: Int, buf: ByteBuf, f: (T) => T,condition: String, limitInf:String = "0",limitSup:String = "end", result: ByteBuf, resultCopy: ByteBuf) = {
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
      case D_BSONOBJECT =>
        if(fieldID==""){
          val res: BosonImpl = modifyArrayEnd(list, buf, f,condition, limitInf, limitSup)
          if(condition.equals("to"))
            result.writeBytes(res.getByteBuf)
          else
            resultCopy.writeBytes(res.getByteBuf)

        }else {
          val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f,condition, limitInf, limitSup)
          if(condition.equals("to"))
              result.writeBytes(res.getByteBuf)
          else
              resultCopy.writeBytes(res.getByteBuf)
        }
      case D_BSONARRAY =>
        if(fieldID=="") {
          val length: Int = buf.getIntLE(buf.readerIndex())
          val bsonBuf: ByteBuf = buf.readBytes(length)
          result.writeBytes(bsonBuf.duplicate())
          resultCopy.writeBytes(bsonBuf.duplicate())
          None
        }else{
          val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f,condition, limitInf, limitSup)
          if(condition.equals("to"))
            result.writeBytes(res.getByteBuf)
          else
            resultCopy.writeBytes(res.getByteBuf)
        }
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
      case _ =>
        None
    }
  }

  def modifyArrayEndWithKey[T](list: List[(Statement, String)],buffer: ByteBuf, fieldID: String, f:T=>T,condition:String, limitInf:String = "0",limitSup:String = "end", result:ByteBuf=Unpooled.buffer(), resultCopy:ByteBuf=Unpooled.buffer()):BosonImpl={
    /*
    * Se fieldID for vazia, então deve ser chamada a funcao modifyArrayEnd to work on Root
    *ByteBuf tem de ser duplicado no input
    * */
    val startReaderIndex: Int = buffer.readerIndex()
    val originalSize: Int = buffer.readIntLE()
    while((buffer.readerIndex()-startReaderIndex)<originalSize) {
      val dataType: Int = buffer.readByte().toInt
      result.writeByte(dataType)
      resultCopy.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, key, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.length<1) {
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
              if(list.size==1) {
                val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
                if (condition.equals("to"))
                  result.writeBytes(res.getByteBuf)
                else
                  resultCopy.writeBytes(res.getByteBuf)
              }else{
                if(list.head._2.contains("..")){

                  val size:Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readBytes(size)
                  val buf2: ByteBuf = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup).getByteBuf
                  if (condition.equals("to"))
                    result.writeBytes(buf2)
                  else
                    resultCopy.writeBytes(buf2)
                }else{
                  val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
                  if (condition.equals("to"))
                    result.writeBytes(res.getByteBuf)
                  else
                    resultCopy.writeBytes(res.getByteBuf)
                }
              }
            case x if (fieldID.toCharArray.deep == x.toCharArray.deep || isHalfword(fieldID, x)) && dataType != D_BSONARRAY =>
              if(list.head._2.contains("..") && list.head._1.isInstanceOf[KeyWithArrExpr])
                processTypesArrayEnd(list, fieldID, dataType, buffer, f,condition,limitInf, limitSup, result, resultCopy)
              else{
                processTypesArray(dataType,buffer.duplicate(),result)
                processTypesArray(dataType,buffer,resultCopy)
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              if (list.head._2.contains("..") && list.head._1.isInstanceOf[KeyWithArrExpr]) {
               processTypesArrayEnd(list, fieldID, dataType, buffer, f, condition,limitInf, limitSup, result, resultCopy)
              }else{
                processTypesArray(dataType, buffer.duplicate(), result)
                processTypesArray(dataType, buffer, resultCopy)
              }
          }
      }
    }
    result.capacity(result.writerIndex())
    resultCopy.capacity(resultCopy.writerIndex())
    val a: ByteBuf = Unpooled.buffer(result.capacity()+4).writeIntLE(result.capacity()+4).writeBytes(result)
    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity()+4).writeIntLE(resultCopy.capacity()+4).writeBytes(resultCopy)
    if(condition.equals("to"))
      new BosonImpl(byteArray = Option(a.array()))
    else
      new BosonImpl(byteArray = Option(b.array()))
  }

  def modifyHasElem[T](list: List[(Statement, String)],buf: ByteBuf, key: String, elem: String, f: Function[T, T], result:ByteBuf=Unpooled.buffer()): ByteBuf = {
    val startReader: Int = buf.readerIndex()
    val size: Int = buf.readIntLE()
    while((buf.readerIndex()-startReader)<size) {
      val dataType: Int = buf.readByte().toInt
      result.writeByte(dataType)
      dataType match{
        case 0 =>
        case _ =>
          val (isArray, k, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(k).writeByte(b)
          val keyString: String = new String(k)
          keyString match {
            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && dataType==D_BSONARRAY =>
              val newBuf: ByteBuf = searchAndModify(list, buf, elem, f).getByteBuf
              result.writeBytes(newBuf)
            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && dataType!=D_BSONARRAY =>
              if(list.head._2.contains(".."))
                processTypesHasElem(list, dataType,key,elem,buf, f ,result)
              else
                processTypesArray(dataType, buf, result)
            case x if key.toCharArray.deep != x.toCharArray.deep && !isHalfword(key, x) =>
              if(list.head._2.contains(".."))
                processTypesHasElem(list, dataType,key,elem,buf, f ,result)
              else
                processTypesArray(dataType, buf, result)
          }
      }
    }
    result.capacity(result.writerIndex())
    val finalResult: ByteBuf = Unpooled.buffer(result.capacity()+4).writeIntLE(result.capacity()+4).writeBytes(result)
    finalResult
  }

  private def processTypesHasElem[T](list: List[(Statement, String)],dataType: Int, key: String,elem: String, buf: ByteBuf, f: (T) => T, result: ByteBuf) = {
    dataType match {
      case D_FLOAT_DOUBLE =>
        val value0: Double = buf.readDoubleLE()
        result.writeDoubleLE(value0)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buf.readIntLE()
        val bytes: ByteBuf = buf.readBytes(valueLength)
        result.writeIntLE(valueLength)
        result.writeBytes(bytes)
      case D_BSONOBJECT =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readBytes(length)
        val newBsonBuf: ByteBuf = modifyHasElem(list,bsonBuf,key, elem, f)
        result.writeBytes(newBsonBuf)
      case D_BSONARRAY =>
        val length: Int = buf.getIntLE(buf.readerIndex())
        val bsonBuf: ByteBuf = buf.readBytes(length)
        val newBsonBuf: ByteBuf = modifyHasElem(list, bsonBuf,key, elem, f)
        result.writeBytes(newBsonBuf)
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
      case _ =>
    }
  }

  private def searchAndModify[T](list: List[(Statement, String)], buf: ByteBuf, elem: String, f: Function[T, T], result: ByteBuf = Unpooled.buffer()): BosonImpl = {
    val startReader: Int = buf.readerIndex()
    val size: Int = buf.readIntLE()
    while((buf.readerIndex()-startReader)<size) {
      val dataType: Int = buf.readByte().toInt
      result.writeByte(dataType)
      dataType match {
        case 0 =>
        case _ =>
          val (isArray, k, b): (Boolean, Array[Byte], Byte) = {
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()
            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
          }
          result.writeBytes(k).writeByte(b)
          val keyString: String = new String(k)
          dataType match{
            case D_BSONOBJECT =>
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bsonBuf: ByteBuf = Unpooled.buffer(bsonSize)//buf.readBytes(bsonSize)
              buf.getBytes(buf.readerIndex(), bsonBuf, bsonSize)
              val hasElem: Boolean = hasElement(bsonBuf.duplicate(), elem)
              if(hasElem){
                if(list.size==1/*Dimensao 1*/) {
                  buf.readBytes(bsonSize)
                  val value0: Map[String, Any] = decodeBsonObject(bsonBuf)
                  val value1: Map[String, Any] = applyFunction(f, value0).asInstanceOf[Map[String@unchecked, Any]]
                  val newbuf: ByteBuf = encodeBsonObject(value1)
                  result.writeBytes(newbuf)
                }else{
                  if(list.head._2.contains("..")){
                    val size: Int = buf.getIntLE(buf.readerIndex())
                    val buf1: ByteBuf = buf.readBytes(size)

                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
                    val buf3: ByteBuf = execStatementPatternMatch(buf2.duplicate(), list, f)
                    result.writeBytes(buf3)


                  }else{
                    buf.readBytes(bsonSize)
                    result.writeBytes(execStatementPatternMatch(bsonBuf, list.drop(1), f))
                  }

                }
              }else{
                result.writeBytes(buf.readBytes(bsonSize))
              }
            case _ =>
              processTypesArray(dataType,buf, result)
          }
      }
    }
    result.capacity(result.writerIndex())
    val finalResult: ByteBuf = Unpooled.buffer(result.capacity()+4).writeIntLE(result.capacity()+4).writeBytes(result)
    new BosonImpl(byteArray = Option(finalResult.array()))
  }

  def hasElement(buf: ByteBuf, elem: String): Boolean = {
    val size: Int = buf.readIntLE()
    val key: ListBuffer[Byte] = new ListBuffer[Byte]
    while(buf.readerIndex()<size && (!elem.equals(new String(key.toArray)) &&  !isHalfword(elem, new String(key.toArray) )) ) {
      key.clear()
      val dataType: Int = buf.readByte()
      dataType match {
        case 0 =>
        case _ =>
          while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
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
              buf.readBytes(valueLength)
            case D_BSONOBJECT =>
              val valueLength: Int = buf.getIntLE(buf.readerIndex())
              buf.readBytes(valueLength)
            case D_BSONARRAY =>
              val valueLength: Int = buf.getIntLE(buf.readerIndex())
              buf.readBytes(valueLength)
            case D_BOOLEAN =>
              buf.readByte()
            case D_NULL =>
            case D_INT =>
              buf.readIntLE()
            case D_LONG =>
              buf.readLongLE()
            case _ =>
          }
       }
      }
    new String(key.toArray).toCharArray.deep == elem.toCharArray.deep || isHalfword(elem, new String(key.toArray))
  }

  def execStatementPatternMatch[T](buf: ByteBuf, statements: List[(Statement,String)], f :Function[T,T], result:ByteBuf=Unpooled.buffer()): ByteBuf = {
    val statement: Statement = statements.head._1
    val newStatementList: List[(Statement, String)] = statements
    statement match{
      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
        val input: (String, Int, String, Any) =
          if (arrEx.midArg.isDefined && arrEx.rightArg.isDefined){
            (key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get)
          }else{
            (key, arrEx.leftArg, "to", arrEx.leftArg)
          }
        execArrayFunction(newStatementList, buf, f, input._1, input._2, input._3, input._4, result)
      case ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any]) =>
       val input: (String, Int, String, Any) =
         if (midArg.isDefined && rightArg.isDefined){//[#..#]
           ("", leftArg, midArg.get, rightArg.get)
         }else{
           ("", leftArg, "to", leftArg)
         }
        execArrayFunction(newStatementList, buf, f, input._1, input._2, input._3, input._4, result)
      case HalfName(half: String) =>
        val res: ByteBuf = modifyAll(newStatementList,buf, half, f)
        result.writeBytes(res)
        result.capacity(result.writerIndex())
      case HasElem(key: String, elem: String) =>
        val res: ByteBuf = modifyHasElem(newStatementList, buf, key, elem,f)
        result.writeBytes(res)
        result.capacity(result.writerIndex())
      case Key(key: String) =>
        val res: ByteBuf = modifyAll(newStatementList,buf, key, f)
        result.writeBytes(res)
        result.capacity(result.writerIndex())
      case _ => throw CustomException("Wrong Statments, Bad Expression.")
    }
  }

  def execArrayFunction[T](list: List[(Statement, String)],buf: ByteBuf,f: Function[T,T], key: String, left:Int, mid:String, right: Any, result: ByteBuf):ByteBuf={
    (key, left, mid.toLowerCase(), right) match {
      case ("", a, "until", "end") =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res:ByteBuf  =  modifyArrayEnd(list, buf1, f,"until", a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case ("", a, "to", "end") => // "[# .. end]"
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf =  modifyArrayEnd(list, buf1, f,"to", a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case ("", a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readBytes(size)
            val res: ByteBuf =  modifyArrayEnd (list, buf1, f,"to", a.toString, b.toString).getByteBuf
            result.writeBytes(res)
            res.release()
            buf1.release()
            result.capacity(result.writerIndex())
          case "until" =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readBytes(size)
            val res: ByteBuf =  modifyArrayEnd(list, buf1, f,"until", a.toString, b.toString).getByteBuf
            result.writeBytes(res)
            buf1.release()
            res.release()
            result.capacity(result.writerIndex())

        }
      case (k, a, "until", "end") =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = modifyArrayEndWithKey(list, buf1,k, f,"until", a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (k, a, "to", "end") =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = modifyArrayEndWithKey(list, buf1,k, f,"to", a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readBytes(size)
            val res: ByteBuf = modifyArrayEndWithKey(list, buf1,k, f, "to", a.toString, b.toString).getByteBuf
            result.writeBytes(res)
            res.release()
            buf1.release()
            result.capacity(result.writerIndex())
          case "until" =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readBytes(size)
            val res: ByteBuf = modifyArrayEndWithKey(list, buf1,k, f,"until", a.toString, b.toString).getByteBuf
            result.writeBytes(res)
            buf1.release()
            res.release()
            result.capacity(result.writerIndex())
        }
      case _ => throw CustomException("Bad Array Expression!")
    }
  }
}

