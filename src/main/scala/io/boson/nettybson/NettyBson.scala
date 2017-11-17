package io.boson.nettybson


import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.nio.charset.Charset
import io.boson.bson.{BsonArray, BsonObject}
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * Created by Ricardo Martins on 18/09/2017.
  */
/**
  * This class encapsulates one Netty ByteBuf
  *
  */
class NettyBson( byteArray: Option[Array[Byte]] = None,
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
    if(javaByteBuf.isDefined){
      javaByteBuf.get.getClass.getSimpleName
    } else if(byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    } else if(scalaArrayBuf.isDefined) {
      scalaArrayBuf.get.getClass.getSimpleName
    } else EMPTY_CONSTRUCTOR

  private val nettyBuffer: ByteBuf = valueOfArgument match {
    case ARRAY_BYTE => // Array[Byte]
      val b = Unpooled.buffer()
      b.writeBytes(byteArray.get)
    case JAVA_BYTEBUFFER => // Java ByteBuffer
      val b = Unpooled.buffer()
      //javaByteBuf.get.flip()
      b.writeBytes(javaByteBuf.get)
      javaByteBuf.get.clear()
      b
    case SCALA_ARRAYBUF => // Scala ArrayBuffer[Byte]
      val b = Unpooled.buffer()
      b.writeBytes(scalaArrayBuf.get.toArray)
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
  }


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
            if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
          case _ =>  // root obj isn't BsonArray, call extractFromBsonObj
            if(key.isEmpty){
              None  // Doens't make sense to pass "" as a key when root isn't a BsonArray
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
            if(midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
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
            if(midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
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
    new NettyBson(byteArray = Option(nettyBuffer.asReadOnly().array()))
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

  def readBytes(arr: Array[Byte]): NettyBson = {
      new NettyBson(Option(nettyBuffer.readBytes(arr).array()))
  }

  def readBytes(arr: Array[Byte], dstIndex: Int, length: Int): NettyBson = {
      new NettyBson(Option(nettyBuffer.readBytes(arr, dstIndex, length).array()))
  }

  def readBytes(buf: NettyBson): NettyBson = {
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
  }

  def readBytes(buf: NettyBson, length: Int): NettyBson = {
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
  }

  def readBytes(buf: NettyBson, dstIndex: Int, length: Int): NettyBson = {
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
  }

  def readBytes(length: Int): NettyBson = {
      val bB: ByteBuf = Unpooled.buffer()
      nettyBuffer.readBytes(bB, length)
      new NettyBson(byteArray = Option(bB.array()))
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

  def readerIndex(readerIndex: Int): NettyBson = {
      new NettyBson(Option(nettyBuffer.readerIndex(readerIndex).array()))
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

  def readRetainedSlice(length: Int): NettyBson = {
      new NettyBson(Option(nettyBuffer.readRetainedSlice(length).array()))
  }

  def readShort: Short = {
      nettyBuffer.readShort()
  }

  def readShortLE: Short = {
      nettyBuffer.readShortLE()
  }

  def readSlice(length: Int): NettyBson = {
      new NettyBson(Option(nettyBuffer.readSlice(length).array()))
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

  def touch: NettyBson = new NettyBson(Option(nettyBuffer.touch().array()))

  def touch(hint: Object): NettyBson = new NettyBson(Option(nettyBuffer.touch(hint).array()))

  def writableBytes: Int = {
    nettyBuffer.writableBytes()
  }

}
