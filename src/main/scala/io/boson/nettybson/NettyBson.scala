package io.boson.nettybson


import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.nio.charset.{Charset, UnsupportedCharsetException}
import io.boson.bson.{BsonArray, BsonObject}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._
import io.vertx.core.buffer.Buffer

import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * Created by Ricardo Martins on 18/09/2017.
  */
/**
  * This class encapsulates one Netty ByteBuf
  *
  */
class NettyBson(byteArray: Option[Array[Byte]] = None, byteBuf: Option[ByteBuf] = None, javaByteBuf: Option[ByteBuffer] = None,
                vertxBuff: Option[Buffer] = None, scalaArrayBuf: Option[ArrayBuffer[Byte]] = None) {

  /*private val valueOfArgument: String = this match {
    case _ if javaByteBuf.isDefined => javaByteBuf.get.getClass.getSimpleName
    case _ if byteArray.isDefined => byteArray.get.getClass.getSimpleName
    case _ if byteBuf.isDefined => byteBuf.get.getClass.getSimpleName
    case _ if vertxBuff.isDefined => vertxBuff.get.getClass.getSimpleName
    case _ if scalaArrayBuf.isDefined => scalaArrayBuf.get.getClass.getSimpleName
    case _ => EMPTY_CONSTRUCTOR
  }*/

  private val valueOfArgument: String =
    if(javaByteBuf.isDefined){
      javaByteBuf.get.getClass.getSimpleName
    } else if(byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    } else if(byteBuf.isDefined) {
      byteBuf.get.getClass.getSimpleName
    } else if(vertxBuff.isDefined) {
      vertxBuff.get.getClass.getSimpleName
    } else if(scalaArrayBuf.isDefined) {
      scalaArrayBuf.get.getClass.getSimpleName
    } else EMPTY_CONSTRUCTOR

  private val nettyBuffer: ByteBuf = valueOfArgument match {
    case NETTY_READONLY_BUF => // Netty
      val b = Unpooled.buffer()
      b.writeBytes(byteBuf.get)
    case NETTY_DEFAULT_BUF => // Netty
      val b = Unpooled.buffer()
      b.writeBytes(byteBuf.get)
    case ARRAY_BYTE => // Array[Byte]
      val b = Unpooled.buffer()
      b.writeBytes(byteArray.get)
    case JAVA_BYTEBUFFER => // Java ByteBuffer
      val b = Unpooled.buffer()
      //javaByteBuf.get.flip()
      b.writeBytes(javaByteBuf.get)
      javaByteBuf.get.clear()
      b
    case VERTX_BUF => // Vertx Buffer
      val b = Unpooled.buffer()
      b.writeBytes(vertxBuff.get.getBytes)
    case SCALA_ARRAYBUF => // Scala ArrayBuffer[Byte]
      val b = Unpooled.buffer()
      b.writeBytes(scalaArrayBuf.get.toArray)
    case NETTY_DUPLICATED_BUF => // Netty
      val b = Unpooled.buffer()
      b.writeBytes(byteBuf.get)
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
  }

  // This classes Netty ByteBuf
  /*val nettyBuffer: ByteBuf = byteArray match {
    case Some(array) =>
      val b =  Unpooled.buffer()
      b.writeBytes(array)
    case None =>
      byteBuf match {
        case Some(buf) =>
          val b =  Unpooled.buffer()
          b.writeBytes(buf)
        case None =>
          //val b =  Unpooled.buffer()
          //b
          javaByteBuf match {
            case Some(jBuf) =>
              val b = Unpooled.buffer()
              b.writeBytes(jBuf.array())
              b
            case None =>
              Unpooled.buffer()
          }
      }
  }*/

  private val arrKeyDecode: ListBuffer[Byte] = new ListBuffer[Byte]()
  private val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]()

  def extract(netty: ByteBuf, key: String, condition: String,
              limitA: Option[Int] = None, limitB: Option[Int] = None): Option[Any] = {
    val startReaderIndex: Int = netty.readerIndex()
    val size: Int = netty.getIntLE(startReaderIndex)
    val seqType: Int = netty.getByte(startReaderIndex+4).toInt
    seqType match {
      case 0 => None // end of obj
      case _ =>
        netty.getByte(startReaderIndex+5).toInt match {
          case 48 =>  // root obj is BsonArray, call extractFromBsonArray
            netty.readIntLE()
            val arrayFinishReaderIndex: Int = startReaderIndex + size
            val midResult = extractFromBsonArray(netty, size, arrayFinishReaderIndex, key, condition, limitA, limitB)
            if (midResult.isEmpty) None else Some(resultComposer(midResult.toList))
          case _ =>  // root obj isn't BsonArray, call extractFromBsonObj
            if(key.isEmpty){
              None  // Doens't make sense to pass "" as a key when root isn't a BsonArray
            } else {
              netty.readIntLE()
              val bsonFinishReaderIndex: Int = startReaderIndex + size
              val midResult = extractFromBsonObj(netty, key, bsonFinishReaderIndex, condition, limitA, limitB)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toList))
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
            val help: ByteBuf = Unpooled.buffer()
            help.writeBytes(netty.readBytes(valueLength))
            Some(help.array())
          } else {
            netty.readCharSequence(netty.readIntLE(), charset)
            None
          }
        case D_BSONOBJECT =>
          if (compareKeys(netty, key) && !condition.equals("limit")) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            Some(traverseBsonObj(netty,  new BsonObject(), bsonFinishReaderIndex))
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            val midResult = extractFromBsonObj(netty, key, bFnshRdrIndex, condition, limitA, limitB)
            if(midResult.isEmpty) None else Some(resultComposer(midResult.toList))
          }
        case D_BSONARRAY =>
          if (compareKeys(netty, key)) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            Some(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, new BsonArray(), limitA, limitB)) match {
              case Some(value) if value.isEmpty => None
              case Some(value) => Some(value)
            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            val midResult = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, key, condition, limitA, limitB)
            if(midResult.isEmpty) None else Some(resultComposer(midResult.toList))
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
      case Some(value) if condition.equals("first") || condition.equals("limit")=>
        netty.readerIndex(bsonFinishReaderIndex)
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
      case "" =>  // Constructs a new BsonArray, BsonArray is Root
        val result = Some(traverseBsonArray(netty, length, arrayFRIdx, new BsonArray(), limitA, limitB))
        result match {
          case Some(x) if x.isEmpty => None   // indexOutOfBounds treatment
          case Some(_) => result
        }
      case _ =>
        val seqType2: Int = netty.readByte().toInt
        if (seqType2 != 0) {
          netty.readByte()
          netty.readByte()
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
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toList))
            case D_BSONARRAY =>
              val startReaderIndex: Int = netty.readerIndex()
              val valueLength2: Int = netty.readIntLE()
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              val midResult = extractFromBsonArray(netty, valueLength2, finishReaderIndex, key, condition, limitA, limitB)
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toList))
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
          case Some(value) =>
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

  private def extractKeys(netty: ByteBuf): Unit = {
    var i = netty.readerIndex()
    while (netty.getByte(i).toChar.isLetter) {
      arrKeyDecode.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
  }

  private def compareKeys(netty: ByteBuf, key: String): Boolean = {
    var i = netty.readerIndex()
    while (netty.getByte(i).toChar.isLetter) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte

    key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep
  }

  private def resultComposer(list: List[Any]): List[Any] = {
    list match {
      case Nil => Nil
      case x :: Nil if x.isInstanceOf[List[Any]] => (resultComposer(x.asInstanceOf[List[Any]]) :: Nil).flatten
      case x :: Nil => x :: Nil
      case x :: xs if x.isInstanceOf[List[Any]] => (resultComposer(x.asInstanceOf[List[Any]]) :: resultComposer(xs) :: Nil).flatten
      case x :: xs => ((x :: Nil) :: resultComposer(xs) :: Nil).flatten
    }
  }

  // Constructs a new BsonObject
  private def traverseBsonObj(netty: ByteBuf, bsonObj: BsonObject, bsonFinishReaderIndex: Int): BsonObject = {
    arrKeyDecode.clear()
    val seqType: Int = netty.readByte().toInt
    seqType match {
      case D_FLOAT_DOUBLE =>
        extractKeys(netty)
        val value: Double = netty.readDoubleLE()
        bsonObj.put(new String(arrKeyDecode.toArray), value)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        extractKeys(netty)
        val valueLength: Int = netty.readIntLE()
        val value: CharSequence = netty.readCharSequence(valueLength - 1, charset)
        bsonObj.put(new String(arrKeyDecode.toArray), value)
        netty.readByte()
      case D_BSONOBJECT =>
        extractKeys(netty)
        val bsonStartReaderIndex: Int = netty.readerIndex()
        val valueTotalLength: Int = netty.readIntLE()
        val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
        bsonObj.put(new String(arrKeyDecode.toArray), traverseBsonObj(netty, new BsonObject(), bsonFinishReaderIndex))
      case D_BSONARRAY =>
        extractKeys(netty)
        val arrayStartReaderIndex: Int = netty.readerIndex()
        val valueLength: Int = netty.readIntLE()
        val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
        bsonObj.put(new String(arrKeyDecode.toArray), traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, new BsonArray()))
      case D_BOOLEAN =>
        extractKeys(netty)
        val value: Int = netty.readByte()
        bsonObj.put(new String(arrKeyDecode.toArray), value == 1)
      case D_NULL =>
        extractKeys(netty)
        bsonObj.putNull(new String(arrKeyDecode.toArray))
      case D_INT =>
        extractKeys(netty)
        val value: Int = netty.readIntLE()
        bsonObj.put(new String(arrKeyDecode.toArray), value)
      case D_LONG =>
        extractKeys(netty)
        val value: Long = netty.readLongLE()
        bsonObj.put(new String(arrKeyDecode.toArray), value)
      case D_ZERO_BYTE =>
    }
    arrKeyDecode.clear()
    val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
    actualPos match {
      case x if x > 0 =>
        traverseBsonObj(netty, bsonObj, bsonFinishReaderIndex)
      case 0 =>
        bsonObj
    }
  }

  // Constructs a new BsonArray with limits
  private def traverseBsonArray(netty: ByteBuf, length: Int, arrayFRIdx: Int, bsonArr: BsonArray, limitA: Option[Int] = None, limitB: Option[Int] = None): BsonArray = {

    def constructWithLimits(iter: Int): BsonArray = {
      val seqType2: Int = netty.readByte().toInt
      if (seqType2 != 0) {
        netty.readByte()
        netty.readByte()
      }
      seqType2 match {
        case D_FLOAT_DOUBLE =>
          val value: Double = netty.readDoubleLE()
        limitB match {
          case Some(_) if iter >= limitA.get && iter <= limitB.get =>
            bsonArr.add(value)
          case Some(_) =>
          case None =>
            limitA match {
              case Some(_) if iter >= limitA.get=>
                bsonArr.add(value)
              case Some(_) =>
              case None =>
                bsonArr.add(value)
            }
        }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val valueLength: Int = netty.readIntLE()
          val field: CharSequence = netty.readCharSequence(valueLength - 1, charset)
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(field)
            case Some(_) =>
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(field)
                case Some(_) =>
                case None =>
                  bsonArr.add(field)
              }
          }
          netty.readByte()
        case D_BSONOBJECT =>
          val bsonStartReaderIndex: Int = netty.readerIndex()
          val valueTotalLength: Int = netty.readIntLE()
          val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(traverseBsonObj(netty, new BsonObject(), bsonFinishReaderIndex))
            case Some(_) =>
              netty.readerIndex(bsonFinishReaderIndex)
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(traverseBsonObj(netty, new BsonObject(), bsonFinishReaderIndex))
                case Some(_) => netty.readerIndex(bsonFinishReaderIndex)
                case None =>
                  bsonArr.add(traverseBsonObj(netty, new BsonObject(), bsonFinishReaderIndex))
              }
          }
        case D_BSONARRAY =>
          val startReaderIndex: Int = netty.readerIndex()
          val valueLength2: Int = netty.readIntLE()
          val finishReaderIndex: Int = startReaderIndex + valueLength2
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
            case Some(_) =>
              netty.readerIndex(finishReaderIndex)
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
                case Some(_) => netty.readerIndex(finishReaderIndex)
                case None =>
                  bsonArr.add(traverseBsonArray(netty, valueLength2, finishReaderIndex, new BsonArray()))
              }
          }
        case D_BOOLEAN =>
          val value: Int = netty.readByte()
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(value == 1)
            case Some(_) =>
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(value == 1)
                case Some(_) =>
                case None =>
                  bsonArr.add(value == 1)
              }
          }
        case D_NULL =>
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.addNull()
            case Some(_) =>
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.addNull()
                case Some(_) =>
                case None =>
                  bsonArr.addNull()
              }
          }
        case D_INT =>
          val value: Int = netty.readIntLE()
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(value)
            case Some(_) =>
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(value)
                case Some(_) =>
                case None =>
                  bsonArr.add(value)
              }
          }
        case D_LONG =>
          val value: Long = netty.readLongLE()
          limitB match {
            case Some(_) if iter >= limitA.get && iter <= limitB.get =>
              bsonArr.add(value)
            case Some(_) =>
            case None =>
              limitA match {
                case Some(_) if iter >= limitA.get=>
                  bsonArr.add(value)
                case Some(_) =>
                case None =>
                  bsonArr.add(value)
              }
          }
        case D_ZERO_BYTE =>
      }
      val actualPos2 = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          constructWithLimits(iter + 1)
        case 0 =>
          bsonArr
      }
    }
    constructWithLimits(0)
  }




  def getByteBuf: ByteBuf = this.nettyBuffer

  def readerIndex: Int = {
    nettyBuffer.readerIndex()
  }

  def writerIndex: Int = {
    nettyBuffer.writerIndex()
  }

  /*def toByteArray: Array[Byte] = {
    val arraybuf: Array[Byte] = new Array[Byte](nettyBuffer.writerIndex())
    nettyBuffer.getBytes(0, arraybuf)
    arraybuf
  }*/

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

  def asReadOnly: NettyBson = {
    new NettyBson(byteBuf = Option(nettyBuffer.asReadOnly()))
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
    try {
      nettyBuffer.readBoolean()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }


  def readByte: Byte = {
    try {
      nettyBuffer.readByte()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(arr: Array[Byte]): NettyBson = {
    try {
      new NettyBson(Option(nettyBuffer.readBytes(arr).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(arr: Array[Byte], dstIndex: Int, length: Int): NettyBson = {
    try {
      new NettyBson(Option(nettyBuffer.readBytes(arr, dstIndex, length).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(buf: NettyBson): NettyBson = {
    try {
      buf.writerIndex match {
        case 0 =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          nettyBuffer.readBytes(byteBuf)
          new NettyBson(Option(byteBuf.array()))
        case length =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          byteBuf.writeBytes(buf.array, 0, length)
          nettyBuffer.readBytes(byteBuf)
          new NettyBson(Option(byteBuf.array()))
      }
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(buf: NettyBson, length: Int): NettyBson = {
    try {
      buf.writerIndex match {
        case 0 =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          nettyBuffer.readBytes(byteBuf, length)
          new NettyBson(Option(byteBuf.array()))
        case _ =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          byteBuf.writeBytes(buf.array, 0, buf.writerIndex)
          nettyBuffer.readBytes(byteBuf, length)
          new NettyBson(Option(byteBuf.array()))
      }
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(buf: NettyBson, dstIndex: Int, length: Int): NettyBson = {
    try {
      buf.writerIndex match {
        case 0 =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          nettyBuffer.readBytes(byteBuf, dstIndex, length)
          new NettyBson(Option(byteBuf.array()))
        case _ =>
          val byteBuf: ByteBuf = Unpooled.buffer()
          byteBuf.writeBytes(buf.array, 0, buf.writerIndex)
          nettyBuffer.readBytes(byteBuf, dstIndex, length)
          new NettyBson(Option(byteBuf.array()))
      }
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readBytes(length: Int): NettyBson = {
    try {
      val bB: ByteBuf = Unpooled.buffer()
      nettyBuffer.readBytes(bB, length)
      new NettyBson(byteBuf = Option(bB))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readChar: Char = {
    try {
      nettyBuffer.readChar()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readCharSequence(length: Int, charset: Charset): CharSequence = {
    try {
      nettyBuffer.readCharSequence(length, charset)

    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readDouble: Double = {
    try {
      nettyBuffer.readDouble()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readerIndex(readerIndex: Int): NettyBson = {
    try {
      new NettyBson(Option(nettyBuffer.readerIndex(readerIndex).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def hasArray: Boolean = {
    nettyBuffer.hasArray
  }

  def readFloat: Float = {
    try {
      nettyBuffer.readFloat()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readInt: Int = {
    try {
      nettyBuffer.readInt()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readIntLE: Int = {
    try {
      nettyBuffer.readIntLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readLong: Long = {
    try {
      nettyBuffer.readLong()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readLongLE: Long = {
    try {
      nettyBuffer.readLongLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readMedium: Int = {
    try {
      nettyBuffer.readMedium()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readMediumLE: Int = {
    try {
      nettyBuffer.readMediumLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readRetainedSlice(length: Int): NettyBson = {
    try {
      new NettyBson(Option(nettyBuffer.readRetainedSlice(length).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readShort: Short = {
    try {
      nettyBuffer.readShort()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readShortLE: Short = {
    try {
      nettyBuffer.readShortLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readSlice(length: Int): NettyBson = {
    try {
      new NettyBson(Option(nettyBuffer.readSlice(length).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedByte: Short = {
    try {
      nettyBuffer.readUnsignedByte()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedInt: Long = {
    try {
      nettyBuffer.readUnsignedInt()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedIntLE: Long = {
    try {
      nettyBuffer.readUnsignedIntLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedMedium: Int = {
    try {
      nettyBuffer.readUnsignedMedium()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedMediumLE: Int = {
    try {
      nettyBuffer.readUnsignedMediumLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedShort: Int = {
    try {
      nettyBuffer.readUnsignedShort()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def readUnsignedShortLE: Int = {
    try {
      nettyBuffer.readUnsignedShortLE()
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  override def toString: String = nettyBuffer.toString

  def toString(charset: Charset): String = {
    try {
      nettyBuffer.toString(charset)
    } catch {
      case e: UnsupportedCharsetException =>
        throw new UnsupportedCharsetException(e.getMessage)
    }
  }

  def toString(index: Int, length: Int, charset: Charset): String =
    nettyBuffer.toString(index, length, charset)

  def touch: NettyBson = new NettyBson(Option(nettyBuffer.touch().array()))

  def touch(hint: Object): NettyBson = new NettyBson(Option(nettyBuffer.touch(hint).array()))

  def writableBytes: Int = {
    nettyBuffer.writableBytes()
  }

  def writeBoolean(field: String, value: Boolean): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BOOLEAN_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BOOLEAN_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(1)
      nettyBuffer.writeBoolean(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeByte(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(1)
      nettyBuffer.writeByte(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeBytes(field: String, arr: Array[Byte]): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(arr.length)
      nettyBuffer.writeBytes(arr)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeBytes(field: String, arr: Array[Byte], scrIndex: Int, length: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(length)
      nettyBuffer.writeBytes(arr, scrIndex, length)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeBytes(field: String, src: NettyBson): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(src.writerIndex)
      nettyBuffer.writeBytes(src.getByteBuf)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeBytes(field: String, src: NettyBson, length: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(length)
      nettyBuffer.writeBytes(src.getByteBuf, length)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeBytes(field: String, src: NettyBson, srcIndex: Int, length: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(length)
      nettyBuffer.writeBytes(src.getByteBuf, srcIndex, length)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeChar(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + CHAR_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(CHAR_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(2)
      nettyBuffer.writeChar(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeCharSequence(field: String, sequence: CharSequence, chrset: Charset): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + CHARSEQUENCE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(CHARSEQUENCE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(sequence.length())
      nettyBuffer.writeCharSequence(sequence, chrset)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeDouble(field: String, value: Double): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + DOUBLE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(DOUBLE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(8)
      nettyBuffer.writeDouble(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeFloat(field: String, value: Float): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + FLOAT_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(FLOAT_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(4)
      nettyBuffer.writeFloat(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeInt(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + INT_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(INT_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(4)
      nettyBuffer.writeInt(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeIntLE(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + INT_LE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(INT_LE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(4)
      nettyBuffer.writeIntLE(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeLong(field: String, value: Long): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + LONG_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(LONG_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(8)
      nettyBuffer.writeLong(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeLongLE(field: String, value: Long): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + LONG_LE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(LONG_LE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(8)
      nettyBuffer.writeLongLE(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeMedium(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + MEDIUM_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(MEDIUM_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(3)
      nettyBuffer.writeMedium(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeMediumLE(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + MEDIUM_LE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(MEDIUM_LE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(3)
      nettyBuffer.writeMediumLE(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writerIndex(writerIndex: Int): NettyBson = {
    try {
      new NettyBson(byteArray = Option(nettyBuffer.writerIndex(writerIndex).array()))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeShort(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + SHORT_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(SHORT_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(2)
      nettyBuffer.writeShort(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }

  def writeShortLE(field: String, value: Int): NettyBson = {
    try {
      nettyBuffer.writeInt(field.length + SEPARATOR_SIZE + SHORT_LE_VALUE.length)
      val completeField: CharSequence = field.concat(SEPARATOR.toString).concat(SHORT_LE_VALUE)
      nettyBuffer.writeCharSequence(completeField, charset)
      nettyBuffer.writeInt(2)
      nettyBuffer.writeShortLE(value)
      new NettyBson(byteBuf = Option(nettyBuffer))
    } catch {
      case e: IndexOutOfBoundsException =>
        throw new IndexOutOfBoundsException(e.getMessage)
    }
  }


  ////////////////////////////////////////////////////////////////////////////////////////

  //Function to convert a BsonObject into a Netty Buffer
  //Netty Buffer Structure
  // | <-Field 1 Size-><-Field 1-><-Value 1 Size-><-Value-><-Field 2 Size-><-Field 1-><-Value 2 Size-><-Value-> ...
  // Working for Strings and Booleans Only
  @throws[IllegalArgumentException]
  def Bson2ByteBuf(bson: BsonObject): NettyBson = {
    // val buf: ByteBuf = Unpooled.buffer()
    val newByteBuf: ByteBuf = Unpooled.buffer()
    // println(Option(newByteBuf))
    bson.fieldNames().forEach(field => {
      if (field.equals("")) throw new IllegalArgumentException("Fields can not be empty.")

      //  val value: AnyRef = bson.getMap.get(field).getClass
      val value: AnyRef = bson.getMap.get(field).getClass.getSimpleName
      //println("value name " + value)
      value match {
        case STRING_VALUE =>
          val str: String = bson.getMap.get(field).asInstanceOf[String]
          writeCharSequenceFromBson(field, str, newByteBuf)
        case BOOLEAN_VALUE =>
          val bool: Boolean = bson.getMap.get(field).asInstanceOf[Boolean]
          writeBooleanFromBson(field, bool, newByteBuf)
        case BYTES_VALUE =>
          val array: Array[Byte] = bson.getMap.get(field).asInstanceOf[Array[Byte]]
          writeBytesFromBson(field, array, newByteBuf)
        case BYTE_VALUE =>
          val byte: Byte = bson.getMap.get(field).asInstanceOf[Byte]
          writeByteFromBson(field, byte, newByteBuf)
        case CHAR_VALUE =>
          val char: Char = bson.getMap.get(field).asInstanceOf[Char]
          writeCharFromBson(field, char, newByteBuf)
        case DOUBLE_VALUE =>
          val double: Double = bson.getMap.get(field).asInstanceOf[Double]
          writeDoubleFromBson(field, double, newByteBuf)
        case FLOAT_VALUE =>
          val float: Float = bson.getMap.get(field).asInstanceOf[Float]
          writeFloatFromBson(field, float, newByteBuf)
        case INT_VALUE =>
          val int: Int = bson.getMap.get(field).asInstanceOf[Int]
          writeIntFromBson(field, int, newByteBuf)
        case INT_LE_VALUE =>
          val intLE: Int = bson.getMap.get(field).asInstanceOf[Int]
          writeIntLEFromBson(field, intLE, newByteBuf)
        case LONG_VALUE =>
          val long: Long = bson.getMap.get(field).asInstanceOf[Long]
          writeLongFromBson(field, long, newByteBuf)
        case LONG_LE_VALUE =>
          val longLE: Long = bson.getMap.get(field).asInstanceOf[Long]
          writeLongLEFromBson(field, longLE, newByteBuf)
        case MEDIUM_VALUE =>
          val medium: Int = bson.getMap.get(field).asInstanceOf[Int]
          writeMediumFromBson(field, medium, newByteBuf)
        case MEDIUM_LE_VALUE =>
          val mediumLE: Int = bson.getMap.get(field).asInstanceOf[Int]
          writeMediumLEFromBson(field, mediumLE, newByteBuf)
        case SHORT_VALUE =>
          val short: Short = bson.getMap.get(field).asInstanceOf[Short]
          writeShortFromBson(field, short, newByteBuf)
        case SHORT_LE_VALUE =>
          val shortLE: Short = bson.getMap.get(field).asInstanceOf[Short]
          writeShortLEFromBson(field, shortLE, newByteBuf)
        /* case UNSIGNED_BYTE_VALUE =>
           val ubyte: Byte = bson.getMap.get(field).asInstanceOf[Byte]
           writeUnsignedByteFromBson(field, ubyte, newByteBuf)
         case UNSIGNED_INT_VALUE =>
           val uint: Int = bson.getMap.get(field).asInstanceOf[Int]
           writeUnsignedIntFromBson(field, uint, newByteBuf)
         case UNSIGNED_INT_LE_VALUE =>
           val uintLE: Int = bson.getMap.get(field).asInstanceOf[Int]
           writeUnsignedIntLEFromBson(field, uintLE, newByteBuf)
         case UNSIGNED_MEDIUM_VALUE =>
           val umedium: Int = bson.getMap.get(field).asInstanceOf[Int]
           writeUnsignedMediumFromBson(field, umedium, newByteBuf)
         case UNSIGNED_MEDIUM_LE_VALUE =>
           val umediumLE: Int = bson.getMap.get(field).asInstanceOf[Int]
           writeUnsignedMediumLEFromBson(field, umediumLE, newByteBuf)
         case UNSIGNED_SHORT_VALUE =>
           val ushort: Short = bson.getMap.get(field).asInstanceOf[Short]
           writeUnsignedShortFromBson(field, ushort, newByteBuf)
         case UNSIGNED_SHORT_LE_VALUE =>
           val ushortLE: Short = bson.getMap.get(field).asInstanceOf[Short]
           writeUnsignedShortLEFromBson(field, ushortLE, newByteBuf)*/

      }
    })
    //return a new one or modified the inner bytebuf ????
    new NettyBson(byteBuf = Option(newByteBuf))
  }

  //Function to write a CharSequence directly in a ByteBuf
  def writeCharSequenceFromBson(field: String, value: String, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + CHARSEQUENCE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(CHARSEQUENCE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(value.length)
    newBB.writeCharSequence(value, charset)
  }

  //Function to write a Boolean directly in a ByteBuf
  def writeBooleanFromBson(field: String, value: Boolean, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + BOOLEAN_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(BOOLEAN_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(BOOLEAN_SIZE)
    newBB.writeBoolean(value)
  }

  //Function to write a Array[Byte] directly in a ByteBuf
  def writeBytesFromBson(field: String, value: Array[Byte], newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + BYTES_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(BYTES_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(value.length)
    newBB.writeBytes(value)
  }

  //Function to write a Byte directly in a ByteBuf
  def writeByteFromBson(field: String, value: Byte, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + BYTE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(BYTE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(BYTE_SIZE)
    newBB.writeByte(value)
  }

  //Function to write a Char directly in a ByteBuf
  def writeCharFromBson(field: String, value: Char, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + CHAR_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(CHAR_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(CHAR_SIZE)
    newBB.writeChar(value)
  }

  //Function to write a Double directly in a ByteBuf
  def writeDoubleFromBson(field: String, value: Double, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + DOUBLE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(DOUBLE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(DOUBLE_SIZE)
    newBB.writeDouble(value)
  }

  //Function to write a Float directly in a ByteBuf
  def writeFloatFromBson(field: String, value: Float, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + FLOAT_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(FLOAT_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(FLOAT_SIZE)
    newBB.writeFloat(value)
  }

  //Function to write a Int directly in a ByteBuf
  def writeIntFromBson(field: String, value: Int, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + INT_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(INT_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(INT_SIZE)
    newBB.writeInt(value)
  }

  //Function to write a IntLE directly in a ByteBuf
  def writeIntLEFromBson(field: String, value: Int, newBB: ByteBuf): Unit = {
    /* convert int into int le ??*/
    newBB.writeInt(field.length + SEPARATOR_SIZE + INT_LE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(INT_LE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(INT_LE_SIZE)
    newBB.writeIntLE(value)
  }

  //Function to write a Long directly in a ByteBuf
  def writeLongFromBson(field: String, value: Long, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + LONG_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(LONG_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(LONG_SIZE)
    newBB.writeLong(value)
  }

  //Function to write a LongLE directly in a ByteBuf
  def writeLongLEFromBson(field: String, value: Long, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + LONG_LE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(LONG_LE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(LONG_LE_SIZE)
    newBB.writeLongLE(value)
  }

  //Function to write a MediumLE directly in a ByteBuf
  def writeMediumFromBson(field: String, value: Int, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + MEDIUM_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(MEDIUM_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(MEDIUM_SIZE)
    newBB.writeMedium(value)
  }

  //Function to write a MediumLE directly in a ByteBuf
  def writeMediumLEFromBson(field: String, value: Int, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + MEDIUM_LE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(MEDIUM_LE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(MEDIUM_LE_SIZE)
    newBB.writeMediumLE(value)
  }

  //Function to write a Short directly in a ByteBuf
  def writeShortFromBson(field: String, value: Short, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + SHORT_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(SHORT_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(SHORT_SIZE)
    newBB.writeShort(value)
  }

  //Function to write a ShortLE directly in a ByteBuf
  def writeShortLEFromBson(field: String, value: Short, newBB: ByteBuf): Unit = {
    newBB.writeInt(field.length + SEPARATOR_SIZE + SHORT_LE_VALUE.length)
    val str1: String = field.concat(SEPARATOR.toString).concat(SHORT_LE_VALUE)
    newBB.writeCharSequence(str1, charset)
    newBB.writeInt(SHORT_LE_SIZE)
    newBB.writeShortLE(value)
  }

  def extractField(field: String, bB: Option[NettyBson] = None): Any = {
    val byteBuf: NettyBson = bB match {
      case Some(buf) => buf
      case None =>
        new NettyBson(byteBuf = Option(nettyBuffer.duplicate()))
    }
    if (byteBuf.readerIndex < byteBuf.writerIndex) {
      val size: Int = byteBuf.readInt

      val str: String = byteBuf.readCharSequence(size, charset).toString

      val strArray: Array[String] = str.split(SEPARATOR)
      val strField: String = strArray(0)
      val valueType: String = strArray(1)


      Option(strField) match {
        case Some(value) if value.equals(field) =>
          // byteBuf.readInt()
          extractValue(valueType, byteBuf) //byteBuf.readBoolean()
        case Some(_) =>
          val sizeOfNext: Int = byteBuf.readInt
          byteBuf.readerIndex(byteBuf.readerIndex + sizeOfNext)
          extractField(field, Option(byteBuf))
        case None =>
      }
    } else {
      throw new NoSuchFieldException(s"This field does not exist $field.")
    }
  }

  def extractValue(valueType: String, buf: NettyBson): Any = {
    valueType match {
      case CHARSEQUENCE_VALUE =>
        val size: Int = buf.readInt
        val str: CharSequence = buf.readCharSequence(size, charset)
        str
      case BOOLEAN_VALUE =>
        buf.readInt
        val bool: Boolean = buf.readBoolean
        bool
      case BYTES_VALUE =>
        val size: Int = buf.readInt
        //val bB: NettyBson = new NettyBson()
        val b: NettyBson = buf.readBytes(size)
        val array1: Array[Byte] = b.array
        array1
      case BYTE_VALUE =>
        buf.readInt
        val byte: Byte = buf.readByte
        byte
      case CHAR_VALUE =>
        buf.readInt
        val char: Char = buf.readChar
        char
      case DOUBLE_VALUE =>
        buf.readInt
        val double: Double = buf.readDouble
        double
      case FLOAT_VALUE =>
        buf.readInt
        val float: Float = buf.readFloat
        float
      case INT_VALUE =>
        buf.readInt
        val int: Int = buf.readInt
        int
      case INT_LE_VALUE =>
        buf.readInt
        val intLE: Int = buf.readIntLE
        intLE
      case LONG_VALUE =>
        buf.readInt
        val long: Long = buf.readLong
        long
      case LONG_LE_VALUE =>
        buf.readInt
        val longLE: Long = buf.readLongLE
        longLE
      case MEDIUM_VALUE =>
        buf.readInt
        val medium: Int = buf.readMedium
        medium
      case MEDIUM_LE_VALUE =>
        buf.readInt
        val mediumLE: Int = buf.readMediumLE
        mediumLE
      case SHORT_VALUE =>
        buf.readInt
        val short: Short = buf.readShort
        short
      case SHORT_LE_VALUE =>
        buf.readInt
        val shortLE: Short = buf.readShortLE
        shortLE
      case UNSIGNED_BYTE_VALUE =>
        buf.readInt
        val unByte: Short = buf.readUnsignedByte
        unByte
      case UNSIGNED_INT_VALUE =>
        buf.readInt
        val unInt: Long = buf.readUnsignedInt
        unInt
      case UNSIGNED_INT_LE_VALUE =>
        buf.readInt
        val unIntLE: Long = buf.readUnsignedIntLE
        unIntLE
      case UNSIGNED_MEDIUM_VALUE =>
        buf.readInt
        val unMedium: Int = buf.readUnsignedMedium
        unMedium
      case UNSIGNED_MEDIUM_LE_VALUE =>
        buf.readInt
        val unMediumLE: Int = buf.readUnsignedMediumLE
        unMediumLE
      case UNSIGNED_SHORT_VALUE =>
        buf.readInt
        val unShort: Int = buf.readUnsignedShort
        unShort
      case UNSIGNED_SHORT_LE_VALUE =>
        buf.readInt
        val unShortLE: Int = buf.readUnsignedShortLE
        unShortLE
    }
  }
}
