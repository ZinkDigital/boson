package io.boson.bson.bsonImpl

import java.io.{ByteArrayOutputStream, IOException, OutputStream}
import java.nio.charset.Charset
import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.time.Instant
import java.util

import Constants.{charset, _}
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.util.ByteProcessor
import io.vertx.core.VertxException
import io.vertx.core.json.{JsonArray, JsonObject}

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.{Failure, Success, Try}
import scala.collection.JavaConverters._

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
/**
  * This class encapsulates one Netty ByteBuf
  *
  */

case class CustomException(smth:String) extends Exception {
  override def getMessage: String = smth
}

object Mapper {
  val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())
  def encode(obj: Any): Array[Byte] = {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Try(mapper.writeValue(os, obj)) match {
      case Success(_) =>
        os.flush()
        os.toByteArray
      case Failure(e) =>
        throw new RuntimeException(e)
    }
  }
  def encodeToBarray(obj: Any): Array[Byte] = try {
    val os = new ByteArrayOutputStream
    encodeBson(obj, os)
    os.flush()
    os.toByteArray
  } catch {
    case e: IOException =>
      throw new VertxException(e)
  }

  def encodeBson(obj: Any, outputStream: OutputStream): Unit = {
    try
      mapper.writeValue(outputStream, obj)
    catch {
      case e: Exception =>
        throw new RuntimeException(e)
    }
  }
}



class Boson(
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
      val b = Unpooled.copiedBuffer(byteArray.get)
      // b.writeBytes(byteArray.get)
      b
    case JAVA_BYTEBUFFER => // Java ByteBuffer
      val b = Unpooled.copiedBuffer(javaByteBuf.get)
      javaByteBuf.get.clear()
      b
    case SCALA_ARRAYBUF => // Scala ArrayBuffer[Byte]
      val b = Unpooled.copiedBuffer(scalaArrayBuf.get.toArray)
      b
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
  }


  private val arrKeyDecode: ListBuffer[Byte] = new ListBuffer[Byte]()
  private val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]()

  def extract(netty1: ByteBuf, key: String, condition: String,
              limitA: Option[Int] = None, limitB: Option[Int] = None): Option[Any] = {
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
            val midResult = extractFromBsonArray(netty, size, arrayFinishReaderIndex, key, condition, limitA, limitB)
            if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
          case _ => // root obj isn't BsonArray, call extractFromBsonObj
            if (key.isEmpty) {
              None // Doens't make sense to pass "" as a key when root isn't a BsonArray
            } else {
              netty.readIntLE()
              val bsonFinishReaderIndex: Int = startReaderIndex + size
              val midResult = extractFromBsonObj(netty, key, bsonFinishReaderIndex, condition, limitA, limitB)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            }
        }
    }
  }

  // Extracts the value of a key inside a BsonObject
  private def extractFromBsonObj(netty: ByteBuf, key: String, bsonFinishReaderIndex: Int, condition: String, limitA: Option[Int], limitB: Option[Int]): Iterable[Any] = {
    arrKeyExtract.clear()
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val value: Double = netty.readDoubleLE()
            Some(value)
          } else {
            netty.readDoubleLE()
            None
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val valueLength: Int = netty.readIntLE()
            val arr: Array[Byte] = Unpooled.copiedBuffer(netty.readCharSequence(valueLength,charset),charset).array()
            Some(arr)
          } else {
            netty.readCharSequence(netty.readIntLE(), charset)
            None
          }
        case D_BSONOBJECT =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val map = scala.collection.immutable.Map[Any, Any]()
            Some(traverseBsonObj(netty, map, bsonFinishReaderIndex))
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            val midResult = extractFromBsonObj(netty, key, bFnshRdrIndex, condition, limitA, limitB)
            if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
          }
        case D_BSONARRAY =>
          if (compareKeys(netty, key)) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            Some(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, Seq.empty[Any], limitA, limitB).toArray[Any]) match {
              case Some(value) if value.isEmpty => None
              case Some(value) => Some(value)
            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            val midResult = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, key, condition, limitA, limitB)
            if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
          }
        case D_BOOLEAN =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val value: Int = netty.readByte()
            Some(value == 1)
          } else {
            netty.readByte()
            None
          }
        case D_NULL =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            Some("Null")
          } else {
            None
          }
        case D_INT =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val value: Int = netty.readIntLE()
            Some(value)
          } else {
            netty.readIntLE()
            None
          }
        case D_LONG =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val value: Long = netty.readLongLE()
            Some(value)
          } else {
            netty.readLongLE()
            None
          }
        case D_ZERO_BYTE =>
          None
      }

    arrKeyExtract.clear()
    finalValue match {
      case None =>
        val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
        actualPos match {
          case x if x > 0 =>
            extractFromBsonObj(netty, key, bsonFinishReaderIndex, condition, limitA, limitB)
          case 0 =>
            None
        }
      case Some(value) if condition.equals("first") || condition.equals("limit") =>
        netty.readerIndex(bsonFinishReaderIndex) //  TODO: review this line
        Some(value)
      case Some(_) =>
        val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
        actualPos match {
          case x if x > 0 =>
            finalValue ++ extractFromBsonObj(netty, key, bsonFinishReaderIndex, condition, limitA, limitB)
          case 0 =>
            None
        }
    }
  }

  // Traverses the BsonArray looking for BsonObject or another BsonArray
  private def extractFromBsonArray(netty: ByteBuf, length: Int, arrayFRIdx: Int, key: String, condition: String, limitA: Option[Int], limitB: Option[Int]): Iterable[Any] = {
    key match {
      case "" => // Constructs a new BsonArray, BsonArray is Root
        val result = Some(traverseBsonArray(netty, length, arrayFRIdx, Seq.empty[Any], limitA, limitB).toArray[Any])
        result match {
          case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
          case Some(_) => result
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
              val midResult = extractFromBsonObj(netty, key, bsonFinishReaderIndex, condition, limitA, limitB)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            case D_BSONARRAY =>
              val startReaderIndex: Int = netty.readerIndex()
              val valueLength2: Int = netty.readIntLE()
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              val midResult = extractFromBsonArray(netty, valueLength2, finishReaderIndex, key, condition, limitA, limitB)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
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
            val actualPos2 = arrayFRIdx - netty.readerIndex()
            actualPos2 match {
              case x if x > 0 =>
                extractFromBsonArray(netty, length, arrayFRIdx, key, condition, limitA, limitB)
              case 0 =>
                None
            }
          case Some(_) =>
            condition match {
              case "first" =>
                finalValue
              case "last" | "all" | "max" | "min" | "limit" =>
                if (seqType2 != 0) {
                  finalValue ++ extractFromBsonArray(netty, length, arrayFRIdx, key, condition, limitA, limitB)
                } else {
                  finalValue
                }
            }
        }
    }
  }

  private def readArrayPos(netty: ByteBuf): Unit = {
    var i = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      netty.readByte()
      i += 1
    }
    netty.readByte() //  consume the end Pos byte
  }

  private def extractKeys(netty: ByteBuf): Unit = {
    var i = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyDecode.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
  }

  private def compareKeys(netty: ByteBuf, key: String): Boolean = {
    var i = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
    key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep
  }

  private def resultComposer(list: Seq[Any]): Seq[Any] = {
    list match {
      case Seq() => Seq.empty[Any]
      case x :: Seq() if x.isInstanceOf[Seq[Any]] => (resultComposer(x.asInstanceOf[Seq[Any]]) +: Seq()).flatten
      case x :: Nil => x +: Seq.empty[Any]
      case x :: xs if x.isInstanceOf[Seq[Any]] => (resultComposer(x.asInstanceOf[Seq[Any]]) +: resultComposer(xs) +: Seq()).flatten
      case x :: xs => ((x +: Seq()) +: resultComposer(xs) +: Seq()).flatten
    }
  }

  // Constructs a new BsonObject
  private def traverseBsonObj(netty: ByteBuf, mapper: Map[Any, Any], bsonFinishReaderIndex: Int): Map[Any, Any] = {
    arrKeyDecode.clear()
    val seqType: Int = netty.readByte().toInt
    val newMap =
      seqType match {
        case D_FLOAT_DOUBLE =>
          extractKeys(netty)
          val value: Double = netty.readDoubleLE()
          mapper + (new String(arrKeyDecode.toArray) -> value)
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          extractKeys(netty)
          val valueLength: Int = netty.readIntLE()
          val value: CharSequence = netty.readCharSequence(valueLength - 1, charset)
          netty.readByte()
          mapper + (new String(arrKeyDecode.toArray) -> value)
        case D_BSONOBJECT =>
          extractKeys(netty)
          val bsonStartReaderIndex: Int = netty.readerIndex()
          val valueTotalLength: Int = netty.readIntLE()
          val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
          val map = scala.collection.immutable.Map[Any, Any]()
          mapper + (new String(arrKeyDecode.toArray) -> traverseBsonObj(netty, map, bsonFinishReaderIndex))
        case D_BSONARRAY =>
          extractKeys(netty)
          val arrayStartReaderIndex: Int = netty.readerIndex()
          val valueLength: Int = netty.readIntLE()
          val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
          mapper + (new String(arrKeyDecode.toArray) -> traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, Seq.empty[Any]))
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
        traverseBsonObj(netty, newMap, bsonFinishReaderIndex)
      case 0 =>
        newMap
    }
  }

  // Constructs a new BsonArray with limits
  private def traverseBsonArray(netty: ByteBuf, length: Int, arrayFRIdx: Int, seq: Seq[Any], limitA: Option[Int] = None, limitB: Option[Int] = None): Seq[Any] = {

    def constructWithLimits(iter: Int, seq: Seq[Any]): Seq[Any] = {
      val seqType2: Int = netty.readByte().toInt
      if (seqType2 != 0) {
        readArrayPos(netty)
      }
      val newSeq =
        seqType2 match {
          case D_FLOAT_DOUBLE =>
            val value: Double = netty.readDoubleLE()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(value)
                seq.:+(value)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(value)
                    seq.:+(value)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.add(value)
                    seq.:+(value)
                }
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            val valueLength: Int = netty.readIntLE()
            val field: CharSequence = netty.readCharSequence(valueLength - 1, charset)
            netty.readByte()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(field)
                seq.:+(field)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(field)
                    seq.:+(field)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.add(field)
                    seq.:+(field)
                }
            }
          case D_BSONOBJECT =>
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val map = scala.collection.immutable.Map[Any, Any]()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(traverseBsonObj(netty, map, bsonFinishReaderIndex))
                seq.:+(traverseBsonObj(netty, map, bsonFinishReaderIndex))
              case Some(_) =>
                netty.readerIndex(bsonFinishReaderIndex)
                seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(m)
                    seq.:+(traverseBsonObj(netty, map, bsonFinishReaderIndex))
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    seq
                  case None =>
                    //bsonArr.add(traverseBsonObj(netty, map, bsonFinishReaderIndex))
                    seq.:+(traverseBsonObj(netty, map, bsonFinishReaderIndex))
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
                seq.:+(traverseBsonArray(netty, valueLength2, finishReaderIndex, Seq.empty[Any]))
              case Some(_) =>
                netty.readerIndex(finishReaderIndex)
                seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
                    seq.:+(traverseBsonArray(netty, valueLength2, finishReaderIndex, Seq.empty[Any]))
                  case Some(_) =>
                    netty.readerIndex(finishReaderIndex)
                    seq
                  case None =>
                    //bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
                    seq.:+(traverseBsonArray(netty, valueLength2, finishReaderIndex, Seq.empty[Any]))
                }
            }
          case D_BOOLEAN =>
            val value: Int = netty.readByte()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(value == 1)
                seq.:+(value == 1)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(value == 1)
                    seq.:+(value == 1)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.add(value == 1)
                    seq.:+(value == 1)
                }
            }
          case D_NULL =>
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.addNull()
                seq.:+(null)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.addNull()
                    seq.:+(null)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.addNull()
                    seq.:+(null)
                }
            }
          case D_INT =>
            println("FOUND AN INT")
            val value: Int = netty.readIntLE()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(value)
                seq.:+(value)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(value)
                    seq.:+(value)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.add(value)
                    seq.:+(value)
                }
            }
          case D_LONG =>
            val value: Long = netty.readLongLE()
            limitB match {
              case Some(_) if iter >= limitA.get && iter <= limitB.get =>
                //bsonArr.add(value)
                seq.:+(value)
              case Some(_) => seq
              case None =>
                limitA match {
                  case Some(_) if iter >= limitA.get =>
                    //bsonArr.add(value)
                    seq.:+(value)
                  case Some(_) => seq
                  case None =>
                    //bsonArr.add(value)
                    seq.:+(value)
                }
            }
          case D_ZERO_BYTE =>
            seq
        }
      val actualPos2 = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          constructWithLimits(iter + 1, newSeq)
        case 0 =>
          newSeq
      }
    }

    constructWithLimits(0, seq)
  }


  def duplicate: Boson =
    new Boson(byteArray = Option(this.nettyBuffer.duplicate().array()))

  def getByteBuf: ByteBuf = this.nettyBuffer

  def readerIndex: Int = {
    nettyBuffer.readerIndex()
  }

  def writerIndex: Int = {
    nettyBuffer.writerIndex()
  }

  def array: Array[Byte] = {
    if (nettyBuffer.isReadOnly) {
      throw new ReadOnlyBufferException()
    } else {
      nettyBuffer.array()
    }
  }

  def capacity: Int = {
    nettyBuffer.capacity()
  }

  def asReadOnly: Boson = {
    new Boson(byteArray = Option(nettyBuffer.asReadOnly().array()))
  }

  def isReadOnly: Boolean = {
    nettyBuffer.isReadOnly
  }

  def isReadable: Boolean = {
    nettyBuffer.isReadable
  }

  def isReadable(size: Int): Boolean = {
    nettyBuffer.isReadable(size)
  }

  def readableBytes: Int = {
    nettyBuffer.readableBytes()
  }

  def readBoolean: Boolean = {
    nettyBuffer.readBoolean()
  }


  def readByte: Byte = {
    nettyBuffer.readByte()
  }

  def readBytes(arr: Array[Byte]): Boson = {
    new Boson(Option(nettyBuffer.readBytes(arr).array()))
  }

  def readBytes(arr: Array[Byte], dstIndex: Int, length: Int): Boson = {
    new Boson(Option(nettyBuffer.readBytes(arr, dstIndex, length).array()))
  }

  def readBytes(buf: Boson): Boson = {
    buf.writerIndex match {
      case 0 =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        nettyBuffer.readBytes(byteBuf)
        new Boson(Option(byteBuf.array()))
      case length =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        byteBuf.writeBytes(buf.array, 0, length)
        nettyBuffer.readBytes(byteBuf)
        new Boson(Option(byteBuf.array()))
    }
  }

  def readBytes(buf: Boson, length: Int): Boson = {
    buf.writerIndex match {
      case 0 =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        nettyBuffer.readBytes(byteBuf, length)
        new Boson(Option(byteBuf.array()))
      case _ =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        byteBuf.writeBytes(buf.array, 0, buf.writerIndex)
        nettyBuffer.readBytes(byteBuf, length)
        new Boson(Option(byteBuf.array()))
    }
  }

  def readBytes(buf: Boson, dstIndex: Int, length: Int): Boson = {
    buf.writerIndex match {
      case 0 =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        nettyBuffer.readBytes(byteBuf, dstIndex, length)
        new Boson(Option(byteBuf.array()))
      case _ =>
        val byteBuf: ByteBuf = Unpooled.buffer()
        byteBuf.writeBytes(buf.array, 0, buf.writerIndex)
        nettyBuffer.readBytes(byteBuf, dstIndex, length)
        new Boson(Option(byteBuf.array()))
    }
  }

  def readBytes(length: Int): Boson = {
    val bB: ByteBuf = Unpooled.buffer()
    nettyBuffer.readBytes(bB, length)
    new Boson(byteArray = Option(bB.array()))
  }

  def readChar: Char = {
    nettyBuffer.readChar()
  }

  def readCharSequence(length: Int, charset: Charset): CharSequence = {
    nettyBuffer.readCharSequence(length, charset)
  }

  def readDouble: Double = {
    nettyBuffer.readDouble()
  }

  def readerIndex(readerIndex: Int): Boson = {
    new Boson(Option(nettyBuffer.readerIndex(readerIndex).array()))
  }

  def hasArray: Boolean = {
    nettyBuffer.hasArray
  }

  def readFloat: Float = {
    nettyBuffer.readFloat()
  }

  def readInt: Int = {
    nettyBuffer.readInt()
  }

  def readIntLE: Int = {
    nettyBuffer.readIntLE()
  }

  def readLong: Long = {
    nettyBuffer.readLong()
  }

  def readLongLE: Long = {
    nettyBuffer.readLongLE()
  }

  def readMedium: Int = {
    nettyBuffer.readMedium()
  }

  def readMediumLE: Int = {
    nettyBuffer.readMediumLE()
  }

  def readRetainedSlice(length: Int): Boson = {
    new Boson(Option(nettyBuffer.readRetainedSlice(length).array()))
  }

  def readShort: Short = {
    nettyBuffer.readShort()
  }

  def readShortLE: Short = {
    nettyBuffer.readShortLE()
  }

  def readSlice(length: Int): Boson = {
    new Boson(Option(nettyBuffer.readSlice(length).array()))
  }

  def readUnsignedByte: Short = {
    nettyBuffer.readUnsignedByte()
  }

  def readUnsignedInt: Long = {
    nettyBuffer.readUnsignedInt()
  }

  def readUnsignedIntLE: Long = {
    nettyBuffer.readUnsignedIntLE()
  }

  def readUnsignedMedium: Int = {
    nettyBuffer.readUnsignedMedium()
  }

  def readUnsignedMediumLE: Int = {
    nettyBuffer.readUnsignedMediumLE()
  }

  def readUnsignedShort: Int = {
    nettyBuffer.readUnsignedShort()
  }

  def readUnsignedShortLE: Int = {
    nettyBuffer.readUnsignedShortLE()
  }

  override def toString: String = nettyBuffer.toString

  def toString(charset: Charset): String = {
    nettyBuffer.toString(charset)
  }

  def toString(index: Int, length: Int, charset: Charset): String =
    nettyBuffer.toString(index, length, charset)

  def touch: Boson = new Boson(Option(nettyBuffer.touch().array()))

  def touch(hint: Object): Boson = new Boson(Option(nettyBuffer.touch(hint).array()))

  def writableBytes: Int = {
    nettyBuffer.writableBytes()
  }


  def modify(nettyOpt: Option[Boson], fieldID: String, f: (Any) => Any): Option[Boson] = {
    /*val bP: ByteProcessor = (value: Byte) => {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
      true
    }*/
    if (nettyOpt.isEmpty) {
      println(s" Input Boson is Empty. ")
      None
    } else {
      val netty: Boson = nettyOpt.get
      println(s"input buf size = ${netty.getByteBuf.capacity()} ")
      val buffer: ByteBuf = netty.getByteBuf.duplicate()
      val buff: ByteBuf = Unpooled.buffer(4)
      buffer.getBytes(0, buff, 4)
      val bufferSize: Int = buff.readIntLE() // buffer.readIntLE()
      println(s"buffer size = $bufferSize ")

      val seqType: Int = buffer.getByte(4).toInt
      println(s"seqType: $seqType")
      seqType match {
        case 0 => None // end of obj
        case _ =>
          buffer.getByte(5).toInt match {
            case 48 => // root obj is BsonArray, call extractFromBsonArray
              println("Root is BsonArray")
              if (fieldID.isEmpty) {
                Option(new Boson())
              } else {
                println("Input capacity = " + buffer.capacity())
                val startRegionArray: Int = buffer.readerIndex()
                println(s"startRegionArray -> $startRegionArray")
                val valueTotalLength: Int = buffer.readIntLE()
                println(s"valueTotalLength -> $valueTotalLength")
                val indexOfFinishArray: Int = startRegionArray + valueTotalLength
                println(s"indexOfFinish -> $indexOfFinishArray")
                val (midResult, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer.duplicate(), fieldID, f) //buffer is intact so far
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf) //  adding the global size to result buffer
                  Some(new Boson(byteArray = Option(result.array())))
                } getOrElse {
                  println("DIDN'T FOUND THE FIELD OF CHOICE TO INJECT, bsonarray as root, returning None")
                  None
                }
              }
            case _ => // root obj isn't BsonArray, call extractFromBsonObj
              println("Root is BsonObject")
              if (fieldID.isEmpty) {
                Option(new Boson())
              } else {
                println("Input capacity = " + buffer.capacity())
                val startRegion: Int = buffer.readerIndex()
                println(s"startRegion -> $startRegion")
                val valueTotalLength: Int = buffer.readIntLE()
                println(s"valueTotalLength -> $valueTotalLength")
                val indexOfFinish: Int = startRegion + valueTotalLength
                println(s"indexOfFinish -> $indexOfFinish")
                val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf)
                  val res = new Boson(byteArray = Option(result.array()))
                  Some(res)
                } getOrElse {
                  println("DIDN'T FOUND THE FIELD OF CHOICE TO INJECT, bsonobject as root, returning None")
                  None
                }
              }
          }
      }
    }
  }

  def modifyAll(buffer: ByteBuf, fieldID: String, f: (Any) => Any, result: ByteBuf = Unpooled.buffer(), ocor: Option[Int] = None): (ByteBuf, Option[Int]) = {
    /*
    * Se fieldID for vazia devolve o Boson Original
    *
    * */
    var ocorrencias: Option[Int] = ocor
    val originalSize: Int = buffer.readIntLE()
    val resultSizeBuffer: ByteBuf = Unpooled.buffer(4)
    while(buffer.readerIndex()<originalSize) {
      val dataType: Int = buffer.readByte().toInt
      println("Data Type= " + dataType)


      dataType match {
        case 0 =>
          result.writeByte(dataType)
          //modifyAll(buffer, fieldID, f, result)
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
          println(s"isArray=$isArray  String=${new String(key)}")
          result.writeBytes(key).writeByte(b)
          new String(key) match {
            case x if fieldID.toCharArray.deep == x.toCharArray.deep =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              println(s"Found Field $fieldID == ${new String(x)}")

              ocorrencias match{
                case None => modifierAll(buffer, dataType, f, result)
                case Some(y: Int) if y == 0 =>modifierAll(buffer, dataType, f, result)
                  ocorrencias = Option(ocorrencias.get-1)
                case Some(y: Int) if y != 0 =>
                  ocorrencias = processTypesAll(dataType,buffer,result,fieldID,f, ocor = Option(ocorrencias.get-1))


              }


              //???
            case x if fieldID.toCharArray.deep != x.toCharArray.deep =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              println(s"Didn't Found Field $fieldID == ${new String(x)}")
              ocorrencias = processTypesAll(dataType,buffer,result,fieldID,f, ocor = ocorrencias)
              //???
          }
      }
      /*
      * modifyAll ??
      * */
    }
    /*
    * TODO - glue the bytebuf together [Size Result] - Not tested
    * */
    result.capacity(result.writerIndex())
    (Unpooled.copiedBuffer(resultSizeBuffer.writeIntLE(result.capacity()+4), result), ocorrencias)
  }

  private def compareKeysInj(buffer: ByteBuf, key: String): Boolean = {
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    buffer.readByte() // consume the end String byte

    println(s"............... $key")
    println(s"............... ${new String(fieldBytes.toArray)}")

    key.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep
  }

  private def matcher(buffer: ByteBuf, fieldID: String, indexOfFinish: Int, f: Any => Any): (Option[ByteBuf], Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    //    val totalSize = indexOfFinish - startReaderIndex
    println(s"matcher..............startReaderIndex: $startReaderIndex")
   if (startReaderIndex < (indexOfFinish - 1)) { //  goes through entire object
      val seqType: Int = buffer.readByte().toInt
      println(s"matcher...........seqType: $seqType")
      val s: (Option[ByteBuf], Int) = if (compareKeysInj(buffer, fieldID)) { //  changes value if keys match
        println("FOUND FIELD")
        val indexTillInterest: Int = buffer.readerIndex()
        println(s"indexTillInterest -> $indexTillInterest")
        val bufTillInterest: ByteBuf = buffer.slice(4, indexTillInterest - 4)
        val (bufWithNewValue, diff): (ByteBuf, Int) = modifier(buffer, seqType, f) //  change the value
        val indexAfterInterest: Int = buffer.readerIndex()
        println(s"indexAfterInterest -> $indexAfterInterest")
        val bufRemainder: ByteBuf = buffer.slice(indexAfterInterest, buffer.capacity() - indexAfterInterest)
        /* verificação de gramatica para saber se continua ou se pára.*/



        /* **************************** */
        val midResult: ByteBuf = Unpooled.wrappedBuffer(bufTillInterest, bufWithNewValue, bufRemainder)
        val newSi: Int = bufTillInterest.capacity()+bufWithNewValue.capacity()
        (Some(midResult), diff)
      } else {
        println("DIDNT FOUND FIELD")

        //consume(seqType, buffer, fieldID, f, selectType)
        processTypes(seqType, buffer, fieldID, f) match { //  consume the bytes of value, NEED to check for bsobj and bsarray before consume
          case Some((buf, diff)) =>
            (Some(buf), diff)
          case None =>
           matcher(buffer, fieldID, indexOfFinish, f)
        }
      }
      s
      //apos alterar um valor verificar se deve continuar ou nao


    } else {
      println("OBJECT FINISHED")
      buffer.readByte()
      (None, 0)
    }
  }

  private def modifier(buffer: ByteBuf, seqType: Int, f: Any => Any): (ByteBuf, Int) = {
    val newBuffer: ByteBuf = Unpooled.buffer() //  corresponds only to the new value
    val result: (ByteBuf, Int) = seqType match {
      case D_FLOAT_DOUBLE =>
        val value: Any = f(buffer.readDoubleLE())
        value match {
          case n: Float =>
            (newBuffer.writeDoubleLE(n), 0)
          case n: Double =>
            (newBuffer.writeDoubleLE(n), 0)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_FLOAT_DOUBLE") //  [IT,OT] => IT != OT
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_FLOAT_DOUBLE") //  [IT,OT] => IT != OT
            }
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val value: Any = f(new String(Unpooled.copiedBuffer(buffer.readBytes(length)).array()))
        //println("returning type = " + value.getClass.getSimpleName)
        value match {
          case n: Array[Byte] =>
            (newBuffer.writeIntLE(n.length + 1).writeBytes(n).writeZero(1), (n.length + 1) - length)
          case n: String =>
            val aux: Array[Byte] = n.getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1), (aux.length + 1) - length)
          case n: Instant =>
            val aux: Array[Byte] = n.toString.getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1), (aux.length + 1) - length)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ") //  [IT,OT] => IT != OT
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ") //  [IT,OT] => IT != OT
            }
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.readIntLE() //  length of current obj
      val bsonObject: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonObject)
        newValue match {
          case bsonObject1: java.util.Map[_, _] =>
            val buf: Array[Byte] = Mapper.encode(bsonObject1)
            (newBuffer.writeBytes(buf), buf.length - valueLength)
          case bsonObject2: scala.collection.immutable.Map[_, Any] =>
            val buf: Array[Byte] = Mapper.encode(bsonObject2)
            (newBuffer.writeBytes(buf), buf.length - valueLength)
          case _ =>
            if(newValue == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            }

        }
      case D_BSONARRAY =>
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonArray)
        newValue match {
          case bsonArray1: java.util.List[_] =>
            // function to encode bsonArray list properly
            val arr: Array[Byte] = Mapper.encode(bsonArray1)
            arr.foreach(u => print(u.toChar))
            (newBuffer.writeBytes(arr), arr.length - valueLength) //  ZERO  for now, cant be zero

          case bsonArray2: Array[Any] =>
            val arr: Array[Byte] = Mapper.encode(bsonArray2)
            (newBuffer.writeBytes(arr), arr.length - valueLength) //  ZERO  for now, cant be zero
          case _ =>
            if(newValue == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONARRAY (java List or scala Array)")
            }

        }
      case D_BOOLEAN =>
        val value: Any = f(buffer.readBoolean())
        value match {
          case bool: Boolean =>
            (newBuffer.writeBoolean(bool), 0)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BOOLEAN")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_BOOLEAN")
            }
        }
      case D_NULL =>  throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value: Any = f(buffer.readIntLE())
        value match {
          case n: Int =>
            (newBuffer.writeIntLE(n), 0)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_INT")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_INT")
            }
        }
      case D_LONG =>
        val value: Any = f(buffer.readLongLE())
        value match {
          case n: Long =>
            (newBuffer.writeLongLE(n), 0)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_LONG")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_LONG")
            }
        }
    }

    /**Será aqui?***/
    result
  }


  private def modifierAll(buffer: ByteBuf, seqType: Int, f: Any => Any, result: ByteBuf): Unit = {
    //val res: (ByteBuf, Int) =
      seqType match {
      case D_FLOAT_DOUBLE =>
        val value: Any = f(buffer.readDoubleLE())
        value match {
          case n: Float =>
            result.writeDoubleLE(n)
          case n: Double =>
            result.writeDoubleLE(n)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_FLOAT_DOUBLE") //  [IT,OT] => IT != OT
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_FLOAT_DOUBLE") //  [IT,OT] => IT != OT
            }
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val value: Any = f(new String(Unpooled.copiedBuffer(buffer.readBytes(length)).array()))
        //println("returning type = " + value.getClass.getSimpleName)
        value match {
          case n: Array[Byte] =>
            result.writeIntLE(n.length + 1).writeBytes(n).writeByte(0)
          case n: String =>
            val aux: Array[Byte] = n.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0)
          case n: Instant =>
            val aux: Array[Byte] = n.toString.getBytes()
            result.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ") //  [IT,OT] => IT != OT
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_ARRAYB_INST_STR_ENUM_CHRSEQ") //  [IT,OT] => IT != OT
            }
        }
      case D_BSONOBJECT =>
        /*val valueLength: Int = buffer.readIntLE() //  length of current obj
      val bsonObject: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonObject)
        newValue match {
          case bsonObject1: java.util.Map[_, _] =>
            val buf: Array[Byte] = Mapper.encode(bsonObject1)
            (result.writeBytes(buf), buf.length - valueLength)
          case bsonObject2: scala.collection.immutable.Map[_, Any] =>
            val buf: Array[Byte] = Mapper.encode(bsonObject2)
            (result.writeBytes(buf), buf.length - valueLength)
          case _ =>
            if(newValue == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONOBJECT (java util.Map[String, _] or scala Map[String, Any])")
            }
        }*/
      case D_BSONARRAY =>
       /* val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonArray)
        newValue match {
          case bsonArray1: java.util.List[_] =>
            // function to encode bsonArray list properly
            val arr: Array[Byte] = Mapper.encode(bsonArray1)
            arr.foreach(u => print(u.toChar))
            (result.writeBytes(arr), arr.length - valueLength) //  ZERO  for now, cant be zero

          case bsonArray2: Array[Any] =>
            val arr: Array[Byte] = Mapper.encode(bsonArray2)
            (result.writeBytes(arr), arr.length - valueLength) //  ZERO  for now, cant be zero
          case _ =>
            if(newValue == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BSONARRAY (java List or scala Array)")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${newValue.getClass.getSimpleName}. Value type require D_BSONARRAY (java List or scala Array)")
            }
        }*/
      case D_BOOLEAN =>
        val value: Any = f(buffer.readBoolean())
        value match {
          case bool: Boolean =>
            result.writeBoolean(bool)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_BOOLEAN")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_BOOLEAN")
            }
        }
      case D_NULL =>  throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
      case D_INT =>
        val value: Any = f(buffer.readIntLE())
        value match {
          case n: Int =>
            result.writeIntLE(n)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_INT")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_INT")
            }
        }
      case D_LONG =>
        val value: Any = f(buffer.readLongLE())
        value match {
          case n: Long =>
            result.writeLongLE(n)
          case _ =>
            if(value == null){
              throw CustomException(s"Wrong inject type. Injecting type NULL. Value type require D_LONG")
            }else{
              throw CustomException(s"Wrong inject type. Injecting type ${value.getClass.getSimpleName}. Value type require D_LONG")
            }
        }
    }
   // res
  }


 /* private def consume(seqType: Int, buffer: ByteBuf, fieldID: String, f: Any => Any, selectType: Option[String] = None): Option[(ByteBuf, Int)] = {
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
        None
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        buffer.readBytes(valueLength)
        None
      case D_BSONOBJECT =>
        println("BSONOBJECT ")
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE() //  length of next BsonObject
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength //  where the next BsonObject ends
      val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
        midResult map { buf =>
          val oneBuf: ByteBuf = buf.slice(0, startRegion - 4) //previous till next object size
        val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size
        val threeBuf: ByteBuf = buf.slice(startRegion, buf.capacity() - startRegion) //  from size till end
        val fourBuf: ByteBuf = Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf) //  previous buffs together

          (fourBuf, diff)
        }
      case D_BSONARRAY =>
        println("D_BSONARRAY")
        println("Input capacity = " + buffer.capacity())
        val startRegionArray: Int = buffer.readerIndex()
        println(s"startRegionArray -> $startRegionArray")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinishArray: Int = startRegionArray + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinishArray")
        val (midResult, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer, fieldID, f) //buffer is intact so far, with buffer.duplicate doesnt work
        midResult map { buf =>
          val oneBuf: ByteBuf = buf.slice(0, startRegionArray - 4) //previous till next object size
        val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size
        val threeBuf: ByteBuf = buf.slice(startRegionArray, buf.capacity() - startRegionArray) //  from size till end
        val fourBuf: ByteBuf = Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf) //  previous buffs together
          (fourBuf, diff)
        }
      case D_BOOLEAN =>
        println("D_BOOLEAN")
        buffer.readByte()
        None
      case D_NULL =>
        println("D_NULL")
        None
      case D_INT =>
        println("D_INT")
        buffer.readIntLE()
        None
      case D_LONG =>
        println("D_LONG")
        buffer.readLongLE()
        None
      case _ =>
        println("Something happened")
        None
    }
  }*/

  private def readArrayPosInj(netty: ByteBuf): Char = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      list.+=(netty.readByte())
      i += 1
    }
    list.+=(netty.readByte()) //  consume the end Pos byte
    //val a: String = ""
    val stringList: ListBuffer[Char] = list.map(b => b.toInt.toChar)
    println(list)
    stringList.head
  }

  private def findBsonObjectWithinBsonArray(buffer: ByteBuf, fieldID: String, f: Any => Any): (Option[ByteBuf], Int) = {
    val seqType: Int = buffer.readByte()
    println(s"findBsonObjectWithinBsonArray____________________________seqType: $seqType")
    if (seqType == 0) {
      println("inside seqType == 0")
      //buffer.readerIndex(0) //  returns all buffer, including global size
      (None,0)
    } else { // get the index position of the array
      val index: Char = readArrayPosInj(buffer)
      println(s"findBsonObjectWithinBsonArray____________________________Index: $index")
      // match and treat each type
      processTypes(seqType,buffer,  fieldID, f) match {
        case Some(elem) =>
          println("out of processTypes and got Some")
          (Some(elem._1),elem._2)
        case None =>
          println("Another None AGAIN")
          findBsonObjectWithinBsonArray(buffer, fieldID, f)
      }
    }
  }

  private def processTypes(seqType: Int, buffer: ByteBuf,  fieldID: String, f: Any => Any): Option[(ByteBuf, Int)] = {
    seqType match {
      case D_ZERO_BYTE =>
        println("case zero_byte")
        //buffer
        None
      case D_FLOAT_DOUBLE =>
        // process Float or Double
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        buffer.readBytes(valueLength)
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_BSONOBJECT =>
        // process BsonObjects
        println("BSONOBJECT ")
        println("Input capacity = " + buffer.capacity())
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinish")
        val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
        midResult map { b =>
          val oneBuf: ByteBuf = b.slice(0, startRegion - 4)
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size//previous till next object size
        val threeBuf: ByteBuf = b.slice(startRegion, b.capacity() - startRegion) //  from size till end
          (Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf), diff) //  previous buffs together
        }
      case D_BSONARRAY =>
        // process BsonArrays
        println("D_BSONARRAY")
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinish")
        val (result, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer, fieldID, f)
        result map { buf => (buf,diff) }
      case D_BOOLEAN =>
        // process Booleans
        println("D_BOOLEAN")
        buffer.readByte()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_NULL =>
        // process Null
        println("D_NULL")
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_INT =>
        // process Ints
        println("D_INT")
        buffer.readIntLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_LONG =>
        // process Longs
        println("D_LONG")
        buffer.readLongLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case _ =>
        //buffer
        println("Something happened")
        None
    }
  }

  private def processTypesAll(seqType: Int, buffer: ByteBuf, result: ByteBuf, fieldID: String, f: Any => Any, ocor: Option[Int]): Option[Int] = {
    var ocorrencias: Option[Int] = ocor
    seqType match {
      case D_ZERO_BYTE =>
        println("case zero_byte")
        result.writeZero(1)
        None
      case D_FLOAT_DOUBLE =>
        // process Float or Double
        println("D_FLOAT_DOUBLE")
        result.writeDoubleLE(buffer.readDoubleLE())
        ocor
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        result.writeIntLE(valueLength)
        result.writeBytes(buffer.readBytes(valueLength))
        ocor
      case D_BSONOBJECT =>
        // process BsonObjects
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val newSizeBuf: ByteBuf = Unpooled.buffer(4)
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux: (ByteBuf, Option[Int]) = modifyAll(bsonBuf, fieldID, f, ocor = ocorrencias)
        result.writeBytes(resultAux._1)
        resultAux._2
      case D_BSONARRAY =>
        // process BsonArrays
        val length: Int = buffer.getIntLE(buffer.readerIndex())
        val newSizeBuf: ByteBuf = Unpooled.buffer(4)
        val bsonBuf: ByteBuf = buffer.readBytes(length)
        val resultAux: (ByteBuf, Option[Int]) = modifyAll(bsonBuf, fieldID, f, ocor = ocorrencias)
        result.writeBytes(resultAux._1)
        resultAux._2
      case D_NULL =>
        println("D_NULL")
        ocor
      case D_INT =>
        println("D_INT")
        result.writeIntLE(buffer.readIntLE())
        ocor
      case D_LONG =>
        // process Longs
        println("D_LONG")
        result.writeLongLE(buffer.readLongLE())
        ocor
      case D_BOOLEAN =>
        // process Longs
        println("D_BOOLEAN")
        result.writeBoolean(buffer.readBoolean())
        ocor
      case _ =>
        println("Something happened")
        ocor
    }
  }


  def findOcorrences(buf: ByteBuf, fieldID: String): ListBuffer[Int] = {
    val list: ListBuffer[Int] = new ListBuffer[Int]
    //val buf: ByteBuf = this.getByteBuf.duplicate()
    val size: Int = buf.readIntLE()


    while (buf.readerIndex() < size) {
      val dataType: Int = buf.readByte().toInt

      dataType match {
        case 0 =>   //some size
        case _ =>
            val startIndex: Int = buf.readerIndex()
            val key: ListBuffer[Byte] = new ListBuffer[Byte]
            while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
              val b: Byte = buf.readByte()
              key.append(b)
            }
            val b: Byte = buf.readByte()

          new String(key.toArray) match {
            case x if fieldID.toCharArray.deep == x.toCharArray.deep =>
              /*
              * Found a field equal to key
              * Perform Injection
              * */
              println(s"Found Field $fieldID == ${new String(x)}")
              list.append(startIndex)
              dataType match {
                case D_FLOAT_DOUBLE =>
                  buf.readDoubleLE()
                case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
                  // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
                  println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
                  val valueLength: Int = buf.readIntLE()
                  buf.readBytes(valueLength)
                case D_BSONOBJECT =>
                  // process BsonObjects
                  val index: ListBuffer[Int] = findOcorrences(buf, fieldID)
                  index.foreach(i => list.append(i))
                case D_BSONARRAY =>
                  // process BsonArrays
                  val index: ListBuffer[Int] = findOcorrences(buf, fieldID)
                  index.foreach(i => list.append(i))
                case D_NULL =>
                  println("D_NULL")
                case D_INT =>
                  println("D_INT")
                  buf.readIntLE()
                case D_LONG =>
                  // process Longs
                  println("D_LONG")
                  buf.readLongLE()
                case D_BOOLEAN =>
                  // process Longs
                  println("D_BOOLEAN")
                  buf.readBoolean()
                case _ =>
                  println("Something happened")
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep =>
              /*
              * Didn't found a field equal to key
              * Consume value and check deeper Levels
              * */
              println(s"Didn't Found Field $fieldID == ${new String(x)}")
              dataType match {
                case D_FLOAT_DOUBLE =>
                  buf.readDoubleLE()
                case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
                  // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
                  println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
                  val valueLength: Int = buf.readIntLE()
                  buf.readBytes(valueLength)
                case D_BSONOBJECT =>
                  // process BsonObjects
                  val index: ListBuffer[Int] = findOcorrences(buf, fieldID)
                  index.foreach(i => list.append(i))
                case D_BSONARRAY =>
                  // process BsonArrays
                  val index: ListBuffer[Int] = findOcorrences(buf, fieldID)
                  index.foreach(i => list.append(i))
                case D_NULL =>
                  println("D_NULL")

                case D_INT =>
                  println("D_INT")
                  buf.readIntLE()

                case D_LONG =>
                  // process Longs
                  println("D_LONG")
                  buf.readLongLE()
                case D_BOOLEAN =>
                  // process Longs
                  println("D_BOOLEAN")
                  buf.readBoolean()
                case _ =>
                  println("Something happened")

              }
          }

      }

    }
    list
  }

  private def encode(buffer: Array[Byte]): Array[Byte] = {
    val sizeBuf: ByteBuf = Unpooled.buffer(4)
    val originalBuf: ByteBuf = Unpooled.buffer().writeBytes(buffer).capacity(buffer.length)
    val encodedBuf: ByteBuf = Unpooled.buffer()
    println(originalBuf.getByte(originalBuf.readerIndex()).toChar)
    originalBuf.readByte().toChar match {
      case '[' => encodeBsonArray(originalBuf, encodedBuf, 0)
      case '{' => encodeBsonObject(originalBuf, encodedBuf)
      case _ => throw CustomException("Input should start with '[' or '{'")
    }
    encodedBuf.capacity(encodedBuf.writerIndex())
    sizeBuf.writeIntLE(encodedBuf.capacity())
    val result: ByteBuf = Unpooled.copiedBuffer(sizeBuf, encodedBuf)
    if(result.hasArray){
      result.array()
    }else{
      val resultByteArray: Array[Byte] = new Array[Byte](result.capacity())
      result.readBytes(resultByteArray)
      resultByteArray
    }
  }

  def encodeBsonArray(buf: ByteBuf, buf1: ByteBuf, nextIdx: Int): ByteBuf = {
    ???
  }

  def encodeBsonObject(buf: ByteBuf, buf1: ByteBuf): ByteBuf = {
    ???
  }

  def test(map: mutable.Map[String, AnyRef]): Unit ={

    val json = new JsonObject(map.asJava)
    println("json = " + json)

    val value: AnyRef = json.getValue("fridgeTemp")
    println("value = " +value)
    val valueClass: String = value.getClass.getSimpleName
    println("valueClass = " + valueClass)
    valueClass match {
      case "JsonObject" => {
        //val json = new JsonObject(value)
        println("BsonObject = " + json)
        val valueBO = json.getValue("fridgeTemp")
        println("valueBO = " +valueBO)
        println("valueBO type = " + valueBO.getClass.getSimpleName)
      }
      case "JsonArray" => {
        val json = new JsonArray(value.toString)
        println("bsonArray = " + json)
        println("BsonObject = " + json)
        val valueBO = json.getValue(0)
        println("valueBO = " +valueBO)
        println("valueBO type = " + valueBO.getClass.getSimpleName)
      }
    }

  }
}
