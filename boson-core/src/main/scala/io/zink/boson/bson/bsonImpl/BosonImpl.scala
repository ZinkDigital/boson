package io.zink.boson.bson.bsonImpl

import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.time.Instant

import io.netty.buffer.{ByteBuf, ByteBufAllocator, PooledByteBufAllocator, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
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
                 javaByteBuf: Option[ByteBuffer] = None,
                 scalaArrayBuf: Option[ArrayBuffer[Byte]] = None,
                 stringJson: Option[String] = None
               ) {

  private val valueOfArgument: String =
    if (javaByteBuf.isDefined) {
      javaByteBuf.get.getClass.getSimpleName
    } else if (byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    } else if (scalaArrayBuf.isDefined) {
      scalaArrayBuf.get.getClass.getSimpleName
    }else if (stringJson.isDefined) {
      stringJson.get.getClass.getSimpleName
  } else EMPTY_CONSTRUCTOR
  //println("ValueOfArgument " + valueOfArgument)

  /*
  Check the input type and get the correct codec accordingly to the Type
   */
  private val (nettyBuffer, codec): (ByteBuf, Codec) = valueOfArgument match {
    case ARRAY_BYTE =>
      val b: ByteBuf = Unpooled.copiedBuffer(byteArray.get)
      val codec = CodecObject.toCodec(byteArray.get)
      (b, codec)
    case JAVA_BYTEBUFFER =>
      val buff: ByteBuffer = javaByteBuf.get
      val codec = CodecObject.toCodec(buff.array())
      if (buff.position() != 0) {
        buff.position(0)
        val b: ByteBuf = Unpooled.copiedBuffer(buff)
        buff.clear()
        (b, codec)
      } else {
        val b: ByteBuf = Unpooled.copiedBuffer(javaByteBuf.get)
        javaByteBuf.get.clear()
        (b, codec)
      }
    case SCALA_ARRAYBUF =>
      val b: ByteBuf = Unpooled.copiedBuffer(scalaArrayBuf.get.toArray)
      val codec = CodecObject.toCodec(scalaArrayBuf.get.toArray)
      (b, codec)
    case "String" =>
      val b: ByteBuf = Unpooled.copiedBuffer(stringJson.get.getBytes)
      val codec = CodecObject.toCodec(stringJson.get)
      (b, codec)

    case EMPTY_CONSTRUCTOR =>
      val codec = CodecObject.toCodec(Unpooled.buffer().array())
      Unpooled.buffer()
      (Unpooled.buffer(), codec)
}


  private val comparingFunction = (netty: ByteBuf, key: String) => {
    compareKeys(netty, key)
  }

  def extract[T](netty1: ByteBuf, keyList: List[(String, String)],
                 limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    //println("---------------------------------------------------------")
    /////////////
    // Check the type of the codec [byte[] or String]
    //println(codec.getClass.getSimpleName)

    /////////////
    val netty: ByteBuf = netty1.duplicate()
    // Create a duplicate
    val nettyC: Codec = codec.duplicate
    val startReaderIndex: Int = 0//netty.readerIndex()
    //Get the reader index
    val startReaderIndexCodec:Int = nettyC.getReaderIndex
    //println(s"ReaderIndex: (C: $startReaderIndexCodec)")
    //val size = 0//netty.readIntLE()//startReaderIndex)
    // Reads the size, the codec needs to consume the values, not only get them,
    val sizeCodec = nettyC.readSize
   // println(s"Size: (C: $sizeCodec)")

    Try(sizeCodec) match {
      case Success(value) =>
        val size: Int = value
        val seqType: Int = netty.getByte(startReaderIndex + 4).toInt
        seqType match {
          case 0 => None
          case _ =>
            netty.getByte(startReaderIndex + 5).toInt match {
              case 48 =>
                netty.readIntLE()
                val arrayFinishReaderIndex: Int = startReaderIndex + size
                //extractFromBsonArray(netty, size, arrayFinishReaderIndex, keyList, limitList)
                keyList.head._1 match {
                  case C_DOT if keyList.lengthCompare(1) == 0 =>
                    val arr: Array[Byte] = new Array[Byte](size)
                    netty.getBytes(startReaderIndex, arr, 0, size)
                    netty.readerIndex(arrayFinishReaderIndex)
                    Some(arr)
                  case _ =>
                    println("arr root")
                    val midResult = extractFromBsonArray(netty, size, arrayFinishReaderIndex, keyList, limitList)
                    //println(s"final midResult -> $midResult")
                    //println(s"final midResult with resultComposer -> ${resultComposer(midResult.toSeq)}")
                    if (midResult.isEmpty) None else resultComposer(midResult.toSeq)
                }
              case _ =>
                netty.readIntLE()
                val bsonFinishReaderIndex: Int = startReaderIndex + size
                //extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                keyList.head._1 match {
                  case EMPTY_KEY if keyList.head._2.equals(C_LIMITLEVEL) => None
                  case C_DOT if keyList.lengthCompare(1) == 0 =>
                    val arr: Array[Byte] = new Array[Byte](size)
                    netty.getBytes(startReaderIndex, arr, 0, size)
                    netty.readerIndex(bsonFinishReaderIndex)
                    Some(arr)
                  case _ if keyList.head._2.equals(C_BUILD)=>
                    val midResult = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList).asInstanceOf[List[List[(String,Any)]]].flatten
                    //List(List(Title, Scala), List(Price, 30.5), List(List(Title, ScalaMachine, Price, 40, Availability, true)))
                    //List(Title, Scala, Price, 30.5, List(Title, ScalaMachine, Price, 40, Availability, true)) with flatten
                    //println(s"final midResult -> $midResult")
                    //println(s"final midResult with resultComposer -> ${resultComposer(midResult.toSeq)}")
                    midResult
                    //if (midResult.isEmpty) None else resultComposer(midResult.toSeq)
                  case _ =>
                    val midResult = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    if (midResult.isEmpty) None else resultComposer(midResult.toSeq)
                }
            }
        }
      case Failure(msg) =>
        throw new RuntimeException(msg)
    }

  }

  // Extracts the value of a key inside a BsonObject
  private def extractFromBsonObj[T](netty: ByteBuf, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            val value: Double = netty.readDoubleLE()
            //            Transform.toPrimitive(fExt.asInstanceOf[Double => Unit], value)
            //            None
            keyList.head._2 match {
              case "build" => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(8)
            //netty.readDoubleLE()
            None
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            val valueLength: Int = netty.readIntLE()
            val arr: Array[Byte] = Unpooled.copiedBuffer(netty.readCharSequence(valueLength, charset), charset).array()
            val newArr: Array[Byte] = arr.filter(b => b != 0)
            //println(s"Matched ${keyList.head._1} with $key -> ${new String(newArr)}")
            keyList.head._2 match {
              case "build" => Some(Iterable(key, new String(newArr)))
              case _ => Some(new String(newArr))
            }
          } else {
            netty.skipBytes(netty.readIntLE())
            //netty.readCharSequence(netty.readIntLE(), charset)
            None
          }
        case D_BSONOBJECT =>
          val (condition: Boolean, key: String) = _compareKeys(netty,keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            //println(s"matched ${keyList.head._1} with an object")
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            keyList.head._2 match {
              case C_ALLNEXT | C_ALLDOTS =>
                netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                Some(resultComposer(Seq(Seq(arr), resultComposer(extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList).toSeq))))
              case C_BUILD =>
                val res = extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList).asInstanceOf[List[List[(String,Any)]]].flatten
                Some(Iterable(key,res))
              case _ =>
                //println("matched an object, returning it")
                netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                netty.readerIndex(bFnshRdrIndex)
                Some(arr)
            }
            //            keyList.head._2 match {
            //              case C_NEXT =>
            //                val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            //                netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
            //                Some(arr)
            ////                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList.drop(1), bsonFinishReaderIndex, limitList.drop(1))
            ////                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            //              case C_ALL if !keyList.head._1.equals(STAR)=>
            //                val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            //                netty.getBytes(bsonStartReaderIndex,arr,0,valueTotalLength)
            //                val midResult: Iterable[Any] = extractFromBsonObj(netty.duplicate(), keyList, bsonFinishReaderIndex, limitList)
            //                netty.readerIndex(bsonFinishReaderIndex)
            //                if (midResult.isEmpty) Some(arr)
            //                else Some(resultComposer(Vector(Vector(arr),resultComposer(midResult.toVector))))
            //              case _ =>
            //                val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            //                netty.getBytes(bsonStartReaderIndex,arr,0,valueTotalLength)
            //                //netty.readerIndex(bsonFinishReaderIndex)
            //                Some(arr)
            //            }
          } else {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            //            netty.readerIndex(bFnshRdrIndex)
            //            None
            keyList.head._2 match {
              case C_LEVEL =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case C_LIMITLEVEL =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case C_NEXT =>
                netty.readerIndex(bFnshRdrIndex)
                None
              case _ =>
                //println(s"found obj in the way, going deeper to find ${keyList.head._1}")
                val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bFnshRdrIndex, limitList)
                //println(s"and the obj result was -> $midResult")
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            }
          }
        case D_BSONARRAY =>
          val (condition: Boolean, key: String) = _compareKeys(netty,keyList.head._1)
          if (condition) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            val arr: Array[Byte] = new Array[Byte](valueLength)
            keyList.head._2 match {
              case C_BUILD =>
                val res = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, List((EMPTY_KEY,C_BUILD)), List((None,None,EMPTY_RANGE)))
                Some(Seq(key,res))
              case C_ALLNEXT | C_ALLDOTS =>
                netty.getBytes(arrayStartReaderIndex, arr, 0, valueLength)
                //println("matched with an ARRAY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11 going to look inside for another match!")
                val res = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                //println(s"THIS IS  what i got for looking -> $res")
                Some(resultComposer(Seq(Seq(arr), resultComposer(res.toSeq))))
              case C_LIMIT | C_LIMITLEVEL =>
                println(s"found arr and matched ${keyList.head._1}, going to traverse it according with limits")
                val midResult = traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                println(s"extracted from arr -> $midResult")
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
              case _ =>
                netty.getBytes(arrayStartReaderIndex, arr, 0, valueLength)
                netty.readerIndex(arrayFinishReaderIndex)
                Some(arr)
            }
            //            keyList.head._2 match {
            //              case C_NEXT if keyList.head._1.equals(STAR) && keyList.drop(1).head._1.equals(EMPTY_KEY) =>  //case Book[#].*.[#]...
            //                Some(extractFromBsonArray(netty,valueLength,arrayFinishReaderIndex,keyList.drop(1),limitList)) match {
            //                  case Some(value) if value.isEmpty => None
            //                  case Some(value) => Some(resultComposer(value.toVector))
            //                }
            //              case C_NEXT if keyList.head._1.equals(STAR) && !keyList.drop(1).head._1.equals(EMPTY_KEY) && !keyList.drop(1).head._1.equals(STAR) && !keyList.drop(1).head._2.equals(C_ALL) && !keyList.drop(1).head._2.equals(C_LIMIT) => //case Book[#].*.key
            //                netty.readerIndex(arrayFinishReaderIndex)
            //                None
            //              case C_NEXT =>
            //                val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList.drop(1), limitList.drop(1))
            //                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            //              case C_LEVEL | C_ALL =>
            //                val arr: Array[Byte] = new Array[Byte](valueLength)
            //                netty.getBytes(arrayStartReaderIndex,arr,0,valueLength)
            //                netty.readerIndex(arrayFinishReaderIndex)
            //                Some(arr)
            //              case (C_LIMIT | C_LIMITLEVEL) if keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER)=>
            //                Some(goThroughArrayWithLimit(netty,valueLength,arrayFinishReaderIndex,keyList,limitList)) match {
            //                  case Some(value) if value.isEmpty => None
            //                  case Some(value) =>
            //                    Some(resultComposer(value.toVector))
            //                }
            //              case C_LIMIT =>
            //                val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, List((C_MATCH,C_MATCH))++keyList, limitList)
            //                if (midResult.isEmpty) None else Some(resultComposer(midResult.toVector))
            //              case _ if keyList.size > 1 =>
            //                Some(goThroughArrayWithLimit(netty,valueLength,arrayFinishReaderIndex,keyList.drop(1),limitList)) match {
            //                  case Some(value) if value.isEmpty => None
            //                  case Some(value) => Some(resultComposer(value.toVector))
            //                }
            //              case _ =>
            //                Some(traverseBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)) match {
            //                  case Some(value) if value.isEmpty => None
            //                  case Some(value) => Some(value.toVector)
            //                }
            //            }
          } else {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            //            netty.readerIndex(arrayFinishReaderIndex)
            //            None
            keyList.head._2 match {
              case C_LEVEL =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case C_LIMITLEVEL =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case C_NEXT =>
                netty.readerIndex(arrayFinishReaderIndex)
                None
              case _ =>
                //println(s"found arr in the way, going deeper to find ${keyList.head._1}")
                val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength, arrayFinishReaderIndex, keyList, limitList)
                //println(s"and the arr result was -> $midResult")
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            }
          }
        case D_BOOLEAN =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            val value: Int = netty.readByte()
            //            Transform.toPrimitive(fExt.asInstanceOf[Boolean => Unit], value == 1)
            //            None
            keyList.head._2 match {
              case "build" => Some(Iterable(key, value == 1))
              case _ => Some(value == 1)
            }
          } else {
            netty.skipBytes(1)
            //netty.readByte()
            None
          }
        case D_NULL =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            keyList.head._2 match {
              case "build" => Some(Iterable(key, "Null"))
              case _ => Some(V_NULL)
            }
            //TODO: take care of case Null
            //Some(V_NULL)
          } else {
            None
          }
        case D_INT =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            val value: Int = netty.readIntLE()
            //            Transform.toPrimitive(fExt.asInstanceOf[Int => Unit], value)
            //            None
            keyList.head._2 match {
              case "build" => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(4)
            //netty.readIntLE()
            None
          }
        case D_LONG =>
          val (condition: Boolean, key: String) = _compareKeys(netty, keyList.head._1)
          if (condition && !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)) {
            val value: Long = netty.readLongLE()
            //            Transform.toPrimitive(fExt.asInstanceOf[Long => Unit], value)
            //            None
            keyList.head._2 match {
              case "build" => Some(Iterable(key, value))
              case _ => Some(value)
            }
          } else {
            netty.skipBytes(8)
            //netty.readLongLE()
            None
          }
        case D_ZERO_BYTE =>
          None
      }
    val actualPos: Int = bsonFinishReaderIndex - netty.readerIndex()
    finalValue match {
      case None =>
        actualPos match {
          case x if x > 0 =>
            extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
          case 0 =>
            None
        }
      case Some(_) if keyList.head._2.equals(C_BUILD) =>
        actualPos match {
          case x if x > 0 =>
            //println("Continuing")
            finalValue ++ extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
          case 0 =>
            //println("Finished")
            finalValue
        }
      case Some(value) if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) || (keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.eq(STAR)) => //keyList.head._2.equals("first") ||
        netty.readerIndex(bsonFinishReaderIndex)
        Some(value)
      case Some(_) =>
        println("was here")
        finalValue ++ extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
    }
  }

  // Traverses the BsonArray looking for BsonObject or another BsonArray
  private def extractFromBsonArray[T](netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
    keyList.head._1 match {
      //      case EMPTY_KEY if keyList.size < 2  && keyList.head._2.equals(C_LIMIT)=> // case expression = ..[#] only!!
      //        val constructed: Iterable[Any] = traverseBsonArray(netty.duplicate(), length, arrayFRIdx, keyList, limitList)
      //        Some(goThroughArrayWithLimit(netty,length,arrayFRIdx,keyList,List((Some(0),None,TO_RANGE))++limitList)) match {
      //          case Some(x) if x.isEmpty => Some(resultComposer(constructed.toVector))
      //          case Some(value) => Some(Vector(resultComposer(constructed.toVector),resultComposer(value.toVector)))
      //        }
      //      case EMPTY_KEY if keyList.head._2.equals(C_LIMIT) =>
      //        val constructed: Iterable[Any] = goThroughArrayWithLimit(netty.duplicate(), length, arrayFRIdx, keyList.drop(1), limitList)
      //        Some(goThroughArrayWithLimit(netty, length, arrayFRIdx, keyList, List((Some(0), None, TO_RANGE)) ++ limitList)) match {
      //          case Some(x) if x.isEmpty => Some(resultComposer(constructed.toVector))
      //          case Some(value) => Some(Vector(resultComposer(constructed.toVector), resultComposer(value.toVector)))
      //        }
      //      case _ if keyList.size < 3 && keyList.head._2.equals(C_MATCH) && keyList.drop(1).head._2.equals(C_LIMIT)=>  // case expression = ..key[#] and matched only!!
      //        val constructed: Iterable[Any] = traverseBsonArray(netty.duplicate(), length, arrayFRIdx, keyList.drop(1), limitList)
      //        Some(goThroughArrayWithLimit(netty, length, arrayFRIdx, keyList.drop(1), List((Some(0), None, TO_RANGE)) ++ limitList)) match {
      //          case Some(x) if x.isEmpty => Some(resultComposer(constructed.toVector))
      //          case Some(value) => Some(Vector(resultComposer(constructed.toVector), resultComposer(value.toVector)))
      //        }
      //      case _ if keyList.head._2.equals(C_MATCH) && keyList.drop(1).head._2.equals(C_LIMIT)=>
      //        val constructed: Iterable[Any] = goThroughArrayWithLimit(netty.duplicate(), length, arrayFRIdx, keyList.drop(2), limitList)
      //        Some(goThroughArrayWithLimit(netty, length, arrayFRIdx, keyList.drop(1), List((Some(0), None, TO_RANGE)) ++ limitList)) match {
      //          case Some(x) if x.isEmpty => Some(resultComposer(constructed.toVector))
      //          case Some(value) => Some(Vector(resultComposer(constructed.toVector), resultComposer(value.toVector)))
      //        }
      case EMPTY_KEY /*if keyList.size < 2*/ =>
        //println("extractFromBsonArray, case empty_key, calling traverseBsonArray")
        traverseBsonArray(netty, length, arrayFRIdx, keyList, limitList)
      /*case EMPTY_KEY =>
        Some(goThroughArrayWithLimit(netty,length,arrayFRIdx,keyList.drop(1),limitList)) match {
          case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
          case Some(x) => x
        }*/
      case STAR /*if keyList.size < 2 */=>
        Some(traverseBsonArray(netty, length, arrayFRIdx, keyList, limitList))
      //      case STAR =>
      //        Some(goThroughArrayWithLimit(netty,length,arrayFRIdx,keyList.drop(1),limitList)) match {
      //          case Some(x) if x.isEmpty => None // indexOutOfBounds treatment
      //          case Some(x) => x
      //        }
      case _ if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) => None
      case _ if keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.equals(EMPTY_KEY) => None
      case _ =>
        val seqType2: Int = netty.readByte().toInt
        //println(s"SeqType2: $seqType2")
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
              //println(s"found obj traversing the array")
              val midResult: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
              println(s"extracted from obj -> $midResult")
              if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            case D_BSONARRAY =>
              val startReaderIndex: Int = netty.readerIndex()
              val valueLength2: Int = netty.readIntLE()
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              val midResult: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
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
        val actualPos2: Int = arrayFRIdx - netty.readerIndex()
        finalValue match {
          case None =>
            actualPos2 match {
              case x if x > 0 =>
                extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
              case 0 =>
                None
            }
          case Some(_) =>
            actualPos2 match {
              case x if x > 0 =>
                //println("going to another pos")
                finalValue ++ extractFromBsonArray(netty, length, arrayFRIdx, keyList, limitList)
              case 0 =>
                //println("reached final pos")
                finalValue
            }
        }
    }
  }

  private def _compareKeys(netty: ByteBuf, key: String): (Boolean, String) = {
    val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    netty.readByte() // consume the end String byte
    (key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep | isHalfword(key, new String(arrKeyExtract.toArray)), new String(arrKeyExtract.toArray))
  }

  private def compareKeys(netty: ByteBuf, key: String): Boolean = {
    val arrKeyExtract: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      arrKeyExtract.append(netty.readByte())
      i += 1
    }
    println(s"Key: ${new String(arrKeyExtract.toArray)}")
    netty.readByte() // consume the end String byte
    key.toCharArray.deep == new String(arrKeyExtract.toArray).toCharArray.deep | isHalfword(key, new String(arrKeyExtract.toArray))
  }

  private def resultComposer(list: Seq[Any]): Seq[Any] = {
    list match {
      case Seq() => Seq.empty[Any]
      case x +: Seq() if x.isInstanceOf[Seq[Any]] => (resultComposer(x.asInstanceOf[Seq[Any]]) +: Seq()).flatten
      case x +: IndexedSeq() => x +: Seq.empty[Any]
      case x +: xs if x.isInstanceOf[Seq[Any]] => (resultComposer(x.asInstanceOf[Seq[Any]]) +: resultComposer(xs) +: Seq()).flatten
      case x +: xs => ((x +: Seq()) +: resultComposer(xs) +: Seq()).flatten
    }
  }

  // Constructs a new BsonArray with limits
  private def traverseBsonArray[T](netty: ByteBuf, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {

    def constructWithLimits(iter: Int): (Iterable[Any], Int) = {
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
                //Transform.toPrimitive(fExt.asInstanceOf[Double => Unit], value)
                //None
                Some(Seq(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 =>
                    //Transform.toPrimitive(fExt.asInstanceOf[Double => Unit], value)
                    //None
                    Some(Seq(value))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                    case C_BUILD =>
                      Some(value)
                    case _ if keyList.head._1.equals(STAR)=>  Some(value)
                    case _ =>
                      println("case none none")
                      None
                  }


                }
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            val valueLength: Int = netty.readIntLE()
            val field: CharSequence = netty.readCharSequence(valueLength, charset)
            val newField: String = field.toString.filter(p => p != 0)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                //Transform.toPrimitive(fExt.asInstanceOf[String => Unit], newField)
                //None
                Some(Seq(newField))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 =>
                    //Transform.toPrimitive(fExt.asInstanceOf[String => Unit], newField)
                    //None
                    Some(Seq(newField))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        Some(newField)
                      case _ if keyList.head._1.equals(STAR)=> Some(newField)
                      case _ =>
                        println("case none none")
                        None
                    }


                }
            }
          case D_BSONOBJECT =>
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            val arr: Array[Byte] = new Array[Byte](valueTotalLength)
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                //println(s"inside traverseBsonArray, obj in this Pos: $iter, returning it as byte[]")
                netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                Some(resultComposer(Seq(Seq(arr), resultComposer(extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList).toSeq))))
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                netty.readerIndex(bsonFinishReaderIndex)
                Some(Seq(arr))
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                if (res.isEmpty) None else Some(res)
              case Some(_) =>
                netty.readerIndex(bsonFinishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    //println(s"inside traverseBsonArray, obj in this Pos: $iter, returning it as byte[]")
                    netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                    val res = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList).toSeq
                    //println(s"traverseBsonArray, extracted from obj in pos: $iter -> $arr and $res")
                    Some(resultComposer(Seq(Seq(arr), resultComposer(res))))
                  //netty.readerIndex(bsonFinishReaderIndex)
                  //Some(arr)
                  case Some(_) if iter >= limitList.head._1.get =>
                    println(s"inside traverseBsonArray, obj in this Pos: $iter, returning it as byte[]")
                    netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                    netty.readerIndex(bsonFinishReaderIndex)
                    Some(Seq(arr))
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    //println("inside traverseBsonArray, obj in this Pos: $iter, not inside initial range, going to search inside")
                    val res: Iterable[Any] = extractFromBsonObj(netty, keyList, bsonFinishReaderIndex, limitList)
                    if (res.isEmpty) None else Some(res)
                  case Some(_) =>
                    netty.readerIndex(bsonFinishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_LIMIT | C_LIMITLEVEL if keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        println(s"Traversing array, found OBJ and condition of next key is C_FILTER")
                        val copyNetty1: ByteBuf = netty.duplicate()
                        val copyNetty2: ByteBuf = netty.duplicate()
                        val midResult = findElements(copyNetty1,copyNetty2,keyList,limitList,bsonStartReaderIndex,bsonFinishReaderIndex)
                        if (midResult.isEmpty) {
                          println("findElements didnt found nothing")
                          netty.readerIndex(bsonFinishReaderIndex)
                          None
                        } else {
                          println(s"res from findElements: ${resultComposer(midResult.toSeq)}")
                          netty.readerIndex(bsonFinishReaderIndex)
                          Some(resultComposer(midResult.toSeq))
                        }
                      case C_BUILD =>
                        val res = extractFromBsonObj(netty, List((STAR, C_BUILD)), bsonFinishReaderIndex, List((None, None, EMPTY_RANGE))).asInstanceOf[List[List[(String,Any)]]].flatten
                        Some(res)
                      case _ =>
                        println("case none none")
                        //None
                        netty.getBytes(bsonStartReaderIndex, arr, 0, valueTotalLength)
                        netty.readerIndex(bsonFinishReaderIndex)
                        Some(arr)
                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = netty.readerIndex()
            val valueLength2: Int = netty.readIntLE()
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            val arr: Array[Byte] = new Array[Byte](valueLength2)
            //println(s"traverseBsonArray, found ARR in pos: $iter")
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                //println(s"inside traverseBsonArray, arr in this Pos: $iter, returning it as byte[]")
                netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                Some(resultComposer(Seq(Seq(arr), resultComposer(extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList).toSeq))))
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                netty.readerIndex(finishReaderIndex)
                Some(Seq(arr))
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                if (res.isEmpty) None else Some(res)
              case Some(_) =>
                netty.readerIndex(finishReaderIndex)
                None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    //println(s"inside traverseBsonArray, arr in this Pos: $iter, returning it as byte[]")
                    val res = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                    netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                    //println(s"extracted from array in pos: $iter -> $arr and $res")
                    Some(resultComposer(Seq(Seq(arr), resultComposer(res.toSeq))))
                  case Some(_) if iter >= limitList.head._1.get =>
                    println(s"inside traverseBsonArray, arr in this Pos: $iter, returning it as byte[]")
                    netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                    netty.readerIndex(finishReaderIndex)
                    Some(Seq(arr))
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    val res: Iterable[Any] = extractFromBsonArray(netty, valueLength2, finishReaderIndex, keyList, limitList)
                    if (res.isEmpty) None else Some(res)
                  case Some(_) =>
                    netty.readerIndex(finishReaderIndex)
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val res = extractFromBsonArray(netty, valueLength2, finishReaderIndex, List((EMPTY_KEY,C_BUILD)), List((None,None,EMPTY_RANGE)))  //TODO:remains untested
                        Some(Seq(res))
                      case C_LIMIT | C_LIMITLEVEL if keyList.size > 1 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        netty.readerIndex(finishReaderIndex)
                        None
                      case _ =>
                        netty.getBytes(startReaderIndex, arr, 0, valueLength2)
                        netty.readerIndex(finishReaderIndex)
                        Some(arr)
                    }
                }
            }
          case D_BOOLEAN =>
            val value: Int = netty.readByte()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                //Transform.toPrimitive(fExt.asInstanceOf[Boolean => Unit], value == 1)
                //None
                Some(Seq(value == 1))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 =>
                    //println(s"inside traverseBsonArray, bool in this Pos: $iter, returning it")
                    //Transform.toPrimitive(fExt.asInstanceOf[Boolean => Unit], value == 1)
                    //None
                    Some(Seq(value == 1))
                  case Some(_) =>
                    //println(s"inside traverseBsonArray, bool in this Pos: $iter, BUT keylist > 1")
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value == 1)
                      case _ if keyList.head._1.equals(STAR)=> Some(value == 1)
                      case _ =>
                        println("case none none")
                        None
                    }

                }
            }
          case D_NULL =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
              Some(V_NULL)
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get =>  Some(V_NULL)
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(V_NULL)
                      case _ if keyList.head._1.equals(STAR)=>  Some(V_NULL)
                      case _ => None
                    }

                }
            }
          case D_INT =>
            val value: Int = netty.readIntLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                //Transform.toPrimitive(fExt.asInstanceOf[Int => Unit], value)
                //None
                Some(Seq(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 =>
                    //println(s"traversing BsonArray, in pos: $iter, found an Int -> $value")
                    //Transform.toPrimitive(fExt.asInstanceOf[Int => Unit], value)
                    //None
                    Some(Seq(value))
                  case Some(_) =>
                    //println(s"traversing BsonArray, in pos: $iter, found an Int -> $value, BUT keylist > 1 ")
                    None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value)
                      case _ if keyList.head._1.equals(STAR)=>  Some(value)
                      case _ =>
                        println("case none none")
                        None
                    }

                }
            }
          case D_LONG =>
            val value: Long = netty.readLongLE()
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.size == 1 =>
                //Transform.toPrimitive(fExt.asInstanceOf[Long => Unit], value)
                //None
                Some(Seq(value))
              case Some(_) => None
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.size == 1 =>
                    //Transform.toPrimitive(fExt.asInstanceOf[Long => Unit], value)
                    //None
                    Some(Seq(value))
                  case Some(_) => None
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD => Some(value)
                      case _ if keyList.head._1.equals(STAR)=>  Some(value)
                      case _ =>
                        println("case none none")
                        None
                    }

                }
            }
          case D_ZERO_BYTE =>
            None
        }
      val actualPos2: Int = arrayFRIdx - netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          //println(s"current result: $newSeq")
          val pair = constructWithLimits(iter + 1)
          (newSeq ++ pair._1, pair._2)
        case 0 =>
          (newSeq, iter)
      }
    }

    val (seq: Iterable[Any], iter: Int) = constructWithLimits(0)
    val finalIteration = iter - 1
    println(s"------------------------------------------------------------------------------------------------------------- FINALITERATION -> $finalIteration")
    val finalSeq: Iterable[Any] =
      limitList.head._3 match {
        case UNTIL_RANGE => seq.take(seq.size - 1)
        case C_END =>
          //println("res from traversing an array with END-> "+seq + " to "+seq.drop(seq.size - 1));seq.drop(seq.size - 1)
          //println(s"Coll before END evaluation -> ${seq.zipWithIndex}, type of Seq: ${seq.getClass.getSimpleName}")
          val help = seq.zipWithIndex.collect {
            case (elem: Seq[_], pos: Int) if pos != finalIteration && seq.size == 1 => elem
            case (elem: Seq[_], pos: Int) if pos == finalIteration => elem
            case (elem: Seq[_], pos: Int) if pos != finalIteration && elem.size >= 1=> elem.drop(1)
          }
          //println("res from traversing an array with END-> " + seq + " to " + help)
          help
        case _ => seq
      }
    finalSeq
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

  private def findElements(netty: ByteBuf, nettyUntouched: ByteBuf, keyList: List[(String,String)], limitList: List[(Option[Int], Option[Int], String)],start: Int, finish: Int): Iterable[Any] = {
    val seqType: Int = netty.readByte().toInt
    val finalValue: Option[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            netty.readDoubleLE()
            Some(C_MATCH)
          } else {
            netty.readDoubleLE()
            None}
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            val valueLength: Int = netty.readIntLE()
            netty.readCharSequence(valueLength, charset)
            Some(C_MATCH)
          } else {
            netty.readCharSequence(netty.readIntLE(),charset)
            None}
        case D_BSONOBJECT =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            val bsonStartReaderIndex: Int = netty.readerIndex()
            val valueTotalLength: Int = netty.readIntLE()
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(bsonFinishReaderIndex)
                Some(C_MATCH)
              case C_LIMIT =>
                Some(extractFromBsonObj(netty.readerIndex(start+4),keyList,finish,limitList)) match {
                  case Some(value) if value.isEmpty => Some(C_MATCH)
                  case Some(value) =>
                    val arr: Array[Byte] = new Array[Byte](finish - start)
                    netty.getBytes(start, arr, 0, finish - start)
                    Some(resultComposer(Seq(Seq(arr),resultComposer(value.toSeq))))
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
                val midResult: Iterable[Any] = extractFromBsonObj(netty,keyList,bsonFinishReaderIndex,limitList)
                if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
            }
          }
        case D_BSONARRAY =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            val arrayStartReaderIndex: Int = netty.readerIndex()
            val valueLength: Int = netty.readIntLE()
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                netty.readerIndex(arrayFinishReaderIndex)
                Some(C_MATCH)
              case C_LIMIT =>
                Some(extractFromBsonObj(netty.readerIndex(start+4),keyList,finish,limitList)) match {
                  case Some(value) if value.isEmpty =>
                    Some(C_MATCH)
                  case Some(value) =>
                    val arr: Array[Byte] = new Array[Byte](finish - start)
                    netty.getBytes(start, arr, 0, finish - start)
                    Some(resultComposer(Seq(Seq(arr),resultComposer(value.toSeq))))
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
                    Some(resultComposer(value.toVector))
                }
            }
          }
        case D_BOOLEAN =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            netty.readByte()
            Some(C_MATCH)
          } else {
            netty.readByte()
            None}
        case D_NULL =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            Some(C_MATCH)
          } else None
        case D_INT =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            netty.readIntLE()
            Some(C_MATCH)
          } else {
            netty.readIntLE()
            None}
        case D_LONG =>
          if (comparingFunction(netty, keyList.drop(1).head._1)) {
            netty.readLongLE()
            Some(C_MATCH)
          } else {
            netty.readLongLE()
            None}
        case D_ZERO_BYTE => None
      }
    val actualPos2: Int = finish - netty.readerIndex()
    actualPos2 match {
      case x if x > 0 && finalValue.isDefined && finalValue.get.equals(C_MATCH) /*&& keyList.size == 2*/=>
        val arr: Array[Byte] = new Array[Byte](finish - start)
        netty.getBytes(start, arr, 0, finish - start)
        Some(Seq(arr)) ++ findElements(netty,nettyUntouched,keyList,limitList,start,finish)
//      case _ if finalValue.isDefined && finalValue.get.equals(C_MATCH) && keyList.size > 2 && !keyList.drop(2).head._1.equals(EMPTY_KEY) =>
//        val midResult: Iterable[Any] = extractFromBsonObj(nettyUntouched,keyList.drop(2),finish,limitList.drop(2))
//        if (midResult.isEmpty) None else Some(resultComposer(midResult.toSeq))
      case 0 if finalValue.isDefined && finalValue.get.equals(C_MATCH) /*&& keyList.size == 2*/ =>
        val arr: Array[Byte] = new Array[Byte](finish - start)
        netty.getBytes(start, arr, 0, finish - start)
        Some(Seq(arr))
      case x if x > 0 && finalValue.isDefined && keyList.head._2.equals(C_LIMIT) =>
        Some(finalValue.get) ++ findElements(netty,nettyUntouched,keyList,limitList,start,finish)
      case 0 if finalValue.isDefined && keyList.head._2.equals(C_LIMIT) => Some(finalValue.get)
      case _ if finalValue.isDefined => Some(finalValue.get)
      case x if x > 0 && finalValue.isEmpty => findElements(netty,nettyUntouched,keyList,limitList,start,finish)
      case 0 if finalValue.isEmpty => None
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
              (key, arrEx.leftArg, o1.get, o2.get)
            case (i, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg)
            case (0, str, None) =>
              str.get match {
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
      case ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any]) =>
        val input: (String, Int, String, Any) =
          (leftArg, midArg, rightArg) match {
            case (i, o1, o2) if midArg.isDefined && rightArg.isDefined =>
              (EMPTY_KEY, leftArg, midArg.get, rightArg.get)
            case (i, o1, o2) if midArg.isEmpty && rightArg.isEmpty =>
              (EMPTY_KEY, leftArg, TO_RANGE, leftArg)
            case (0, str, None) =>
              str.get match {
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
      case ROOT() =>
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
