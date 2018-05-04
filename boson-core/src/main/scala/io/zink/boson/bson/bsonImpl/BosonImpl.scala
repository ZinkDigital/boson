package io.zink.boson.bson.bsonImpl

import java.nio.{ByteBuffer, ReadOnlyBufferException}
import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled, UnpooledHeapByteBuf}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._

import io.zink.boson.bson.codec._
import io.zink.boson.bson.codec.impl.CodecJson
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
case class CustomException(smth: String) extends Exception {
  override def getMessage: String = smth
}

//abstract class experience extends ByteBuf {
//  override def copy(index: Int, length: Int): ByteBuf
//}
//object experience {
//  def copy(source: ByteBuf,index: Int, length: Int): ByteBuf = {
//    val b = Unpooled.buffer()
//
//    System.arraycopy(source, index, b.array(), 0, length)
//    b
//  }
//}
/**
  * Class with all operations to be applied on a Netty ByteBuffer or a Json encoded String
  *
  * @param byteArray  to be removed
  * @param javaByteBuf  to be removed
  * @param stringJson to be removed
  */
class BosonImpl(
                 byteArray: Option[Array[Byte]] = None,
                 javaByteBuf: Option[ByteBuffer] = None,
                 stringJson: Option[String] = None
               ) {

  /**
    * Deprecated, used on previous implementations of Boson to verify which input was given by the user.
    */
  private val valueOfArgument: String =
    if (javaByteBuf.isDefined) {
      javaByteBuf.get.getClass.getSimpleName
    } else if (byteArray.isDefined) {
      byteArray.get.getClass.getSimpleName
    }else if (stringJson.isDefined) {
      stringJson.get.getClass.getSimpleName
  } else EMPTY_CONSTRUCTOR

  /**
    * Deprecated for the same reason as the previous one.
    */
  private val nettyBuffer: (Either[ByteBuf, String]) = valueOfArgument match {
    case ARRAY_BYTE =>
      val b: ByteBuf = Unpooled.copiedBuffer(byteArray.get)
      Left(b)
    case JAVA_BYTEBUFFER =>
      val buff: ByteBuffer = javaByteBuf.get
      if (buff.position() != 0) {
        buff.position(0)
        val b: ByteBuf = Unpooled.copiedBuffer(buff)
        buff.clear()
        Left(b)
      } else {
        val b: ByteBuf = Unpooled.copiedBuffer(buff)
        buff.clear()
        Left(b)
      }
    case STRING =>
      val b:String = stringJson.get
      Right(b)
    case EMPTY_CONSTRUCTOR =>
      Unpooled.buffer()
      Left(Unpooled.buffer())
}

  /**
    * Set of conditions to extract primitive types while traversing an Object.
    */
  private val eObjPrimitiveConditions: List[(String, String)] => Boolean =
    keyList => {
      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)
    }

  /**
    * Set of conditions to extract an Object while traversing an Object.
    */
  private val eObjConditions: List[(String, String)] => Boolean =
    keyList => {
      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_LIMITLEVEL)
    }

  /**
    * Public method to trigger extraction.
    *
    * @param netty1 Encoded document.
    * @param keyList  set of keys.
    * @param limitList  limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  def extract[T](netty1: Either[ByteBuf, String], keyList: List[(String, String)],
                 limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

    val nettyC: Codec = netty1 match {
      case Right(x) => CodecObject.toCodec(x)
      case Left(x) => CodecObject.toCodec(x)
    }

    val startReaderIndexCodec:Int = nettyC.getReaderIndex
    Try(nettyC.readSize) match {
      case Success(value) =>
        val size: Int = value
        val seqTypeCodec: SonNamedType = nettyC.rootType
        seqTypeCodec match {
          case SonZero => Nil
          case SonArray(_,_) =>
            val arrayFinishReaderIndex: Int = startReaderIndexCodec + size
            keyList.head._1 match {
              case C_DOT if keyList.lengthCompare(1) == 0 =>
                Try(nettyC.getToken(SonArray(C_DOT)).asInstanceOf[SonArray].result) match{
                  case Success(v)=>
                    nettyC.release()
                    List(v)
                  case Failure(_)=>
                    nettyC.release()
                    Nil
                }
              case _ =>
                Try(extractFromBsonArray(nettyC, size, arrayFinishReaderIndex, keyList, limitList))match{
                  case Success(v) =>
                    nettyC.release()
                    v
                  case Failure(_)=>
                    nettyC.release()
                    Nil
                }
            }
          case SonObject(_,_) =>
            val bsonFinishReaderIndex: Int = startReaderIndexCodec + size
            keyList.head._1 match {
              case EMPTY_KEY if keyList.head._2.equals(C_LIMITLEVEL) => Nil
              case C_DOT if keyList.lengthCompare(1) == 0 =>
                Try(nettyC.getToken(SonObject(C_DOT)).asInstanceOf[SonObject].result) match{
                  case Success(v)=>
                    nettyC.release()
                    List(v)
                  case Failure(_)=>
                    nettyC.release()
                    Nil
                }
              case _ if keyList.head._2.equals(C_BUILD)=>
                Try(extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList))match{
                  case Success(v)=>
                    nettyC.release()
                    v
                  case Failure(_)=>
                    nettyC.release()
                    Nil
                }
              case _ =>
                Try( extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList))match{
                  case Success(v)=>
                    nettyC.release()
                    v
                  case Failure(_)=>
                    nettyC.release()
                    Nil

                }
            }
        }
      case Failure(msg) =>
        throw new RuntimeException(msg)
    }
  }

  /**
    * Structure, types, and rules when traversing an Object.
    * Structure:
    *   Total Length -> 4 bytes
    *   Type of next Item -> 1 byte
    *   Key bytes -> unknown length, ends with 0.
    *   Value -> Number of bytes depends on type.
    * Types:
    *   Consult Dictionary Object under ENCODING CONSTANTS.
    *
    * @param codec  Abstraction of Encoded Document.
    * @param keyList  set of keys.
    * @param bsonFinishReaderIndex  last index of Object.
    * @param limitList  limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  private def extractFromBsonObj[T](codec: Codec, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    val seqTypeCodec: Int = codec.readDataType
    val finalValue: List[Any] =
      seqTypeCodec match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].result
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].result.asInstanceOf[String]
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_BSONOBJECT =>
          val (matched: Boolean, key: String) = compareKeys(codec,keyList.head._1)
          if (matched && eObjConditions(keyList)) {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int =codec.readSize
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_ALLNEXT | C_ALLDOTS =>
                val value0 = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                codec.downOneLevel
                List(List(value0), extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)).flatten
              case C_BUILD =>
                codec.downOneLevel
                 val res = extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)
                List((key.toLowerCase,res))
              case _ =>
                 val value0 = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                List(value0)

            }
          } else {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int =codec.readSize
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
                codec.setReaderIndex(bFnshRdrIndex)
                Nil
              case _ =>
                // Truque: Esta função nao faz nada no CodecBson mas no CodecJson avança o readerindex em uma unidade
                codec.downOneLevel
                extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)
            }
          }
        case D_BSONARRAY =>
          val (matched: Boolean, key: String) = compareKeys(codec,keyList.head._1)
          if (matched) {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int =codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_BUILD =>
                codec.downOneLevel
                val res = extractFromBsonArray( codec, valueLength, arrayFinishReaderIndex, List((EMPTY_KEY,C_BUILD)), List((None,None,EMPTY_RANGE)))
                List((key.toLowerCase,res))
              case C_ALLNEXT | C_ALLDOTS =>
                val value0 = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                codec.downOneLevel
                 val res = extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
                List(List(value0), res).flatten
              case C_LIMIT | C_LIMITLEVEL =>
                codec.downOneLevel
                 traverseBsonArray( codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
              case _ =>
                 val value0 = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                List(value0)
            }
          } else {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int =codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LEVEL | C_LIMITLEVEL | C_NEXT=>
                codec.setReaderIndex(arrayFinishReaderIndex)
                Nil
              case _ =>
                codec.downOneLevel
                extractFromBsonArray( codec, valueLength, arrayFinishReaderIndex, keyList, limitList)

            }
          }
        case D_BOOLEAN =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].result
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0 == 1))
              case _ => List(value0 == 1)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_NULL =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].result.asInstanceOf[String]
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_INT =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)){
            val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].result
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_LONG =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].result
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_ZERO_BYTE =>
          Nil
      }

    //Update position
    val actualPos: Int = bsonFinishReaderIndex - codec.getReaderIndex
    finalValue.isEmpty match {
      case true =>
        actualPos match {
          case x if x > 0 => extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
          case 0 => Nil
        }
      case false if keyList.head._2.equals(C_BUILD) =>
        actualPos match {
          case x if x > 0 =>
            finalValue ++ extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
          case 0 => finalValue
        }
      case false if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) || (keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.eq(STAR)) =>
        codec.setReaderIndex(bsonFinishReaderIndex)
        finalValue
      case false =>
        finalValue ++ extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
    }
  }

  /**
    * Structure, types, and rules when traversing an Object.
    * Structure:
    *   Total Length -> 4 bytes
    *   Type of next Item -> 1 byte
    *   Position bytes -> unknown length, ends with 0.
    *   Value -> Number of bytes depends on type.
    * Types:
    *   Consult Dictionary Object under ENCODING CONSTANTS.
    * @param codec  Abstraction of Encoded Document.
    * @param length Size of Array Object.
    * @param arrayFRIdx last index of Array Object.
    * @param keyList  set of keys.
    * @param limitList  limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  private def extractFromBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    keyList.head._1 match {
      case EMPTY_KEY | STAR =>
        traverseBsonArray(codec, length, arrayFRIdx, keyList, limitList)
      case _ if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) => Nil
      case _ if keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.equals(EMPTY_KEY) => Nil

      case _ =>
        val seqType2: Int = codec.readDataType
        if (seqType2 != 0) {
         codec.readArrayPosition
        }
        val finalValue: List[Any] =
          seqType2 match {
            case D_FLOAT_DOUBLE =>
              codec.consumeValue(seqType2)
              Nil
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              codec.consumeValue(seqType2)
              Nil
            case D_BSONOBJECT =>
              val bsonStartReaderIndex: Int = codec.getReaderIndex
              val valueTotalLength: Int = codec.readSize
              val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
              codec.downOneLevel
              extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
            case D_BSONARRAY =>
              val startReaderIndex: Int = codec.getReaderIndex
              val valueLength2: Int = codec.readSize
              val finishReaderIndex: Int = startReaderIndex + valueLength2
              codec.downOneLevel
              extractFromBsonArray( codec, valueLength2, finishReaderIndex, keyList, limitList)
            case D_BOOLEAN =>
              codec.consumeValue(seqType2)
              Nil
            case D_NULL =>
              codec.consumeValue(seqType2)
              Nil
            case D_INT =>
              codec.consumeValue(seqType2)
              Nil
            case D_LONG =>
              codec.consumeValue(seqType2)
              Nil
            case D_ZERO_BYTE =>
              Nil
          }
        val actualPos2: Int = arrayFRIdx - codec.getReaderIndex
        if(finalValue.isEmpty) {
          actualPos2 match {
            case x if x > 0 => extractFromBsonArray(codec,  length, arrayFRIdx, keyList, limitList)
            case 0 => Nil
          }
        } else {
          actualPos2 match {
            case x if x > 0 =>
              finalValue ++ extractFromBsonArray(codec,  length, arrayFRIdx, keyList, limitList)
            case 0 => finalValue
          }
        }
    }
  }

  /**
    *
    * @param codec  Abstraction of Encoded Document.
    * @param key  given by user.
    * @return Tuple of Boolean and String, Boolean representing if keys match, String is the key.
    */
  private def compareKeys(codec: Codec, key: String): (Boolean, String) = {
    val key0 = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].result.asInstanceOf[String]
    (key.toCharArray.deep == key0.toCharArray.deep | isHalfword(key, key0),key0)
  }


  /**
    * Traverse an array taking account the limits given.
    *
    * @param codec  Abstraction of Encoded Document.
    * @param length Size of Array Object.
    * @param arrayFRIdx last index of Array Object.
    * @param keyList  set of keys.
    * @param limitList  limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  private def traverseBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

    /**
      * Same structure as extractFromBsonArray(), but handles limits.
      * Limit list has 2 optionals and a String. The String could be either "end" or "until".
      * The possible combination of optionals, with examples, are:
      * Some-None -> Ex: [1 to end].
      * None None -> Used to build the entire array.
      * Some-Some -> Ex: [0 until 10]
      *
      * @param iter Int to keep track of current position.
      * @return List with extraction result.
      */
    def constructWithLimits(iter: Int): List[Any] = {

      val seqTypeCodec: Int = codec.readDataType
      if (seqTypeCodec != 0) {
        codec.readArrayPosition
      }
      val newSeq: List[Any] =
        seqTypeCodec match {
          case D_FLOAT_DOUBLE =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].result
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].result
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].result
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].result
                        List(value)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].result
                List(value0)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].result
                    List(value0)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].result
                        List(value0)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].result
                        List(value0)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_BSONOBJECT =>
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int = codec.readSize
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                List(List(buf), extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)).flatten
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                List(buf)
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: List[Any] = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
                if (res.isEmpty) Nil else res
              case Some(_) =>
                codec.setReaderIndex(bsonFinishReaderIndex)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
                    val res = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(bsonStartReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                        List(List(buf), res).flatten
                      case _ => res
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                    val res = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
                    List(List(buf), res).flatten
                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
                    /*
                    codec.setReader(bsonFinish)
                    codec.getDataType
                      case codec.setReader(bsonStart)


                     */
                  codec.setReaderIndex(bsonFinishReaderIndex)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(bsonStartReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                        List(buf)
                      case _ =>
                        //codec.setReaderIndex(bsonFinishReaderIndex)
                        Nil
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    //println(s"found obj, in pos: $iter")
                    val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                    List(buf)
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    val res: List[Any] = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
                    if (res.isEmpty) Nil else res
                  case Some(_) =>
                    codec.setReaderIndex(bsonFinishReaderIndex)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_LIMIT | C_LIMITLEVEL if keyList.lengthCompare(1) > 0 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        val copyCodec1: Codec = codec.duplicate
                        val midResult = findElements(copyCodec1,keyList,limitList,bsonStartReaderIndex,bsonFinishReaderIndex)
                        copyCodec1.release()
                        if (midResult.isEmpty) {
                          codec.setReaderIndex(bsonFinishReaderIndex)
                          Nil
                        } else {
                          codec.setReaderIndex(bsonFinishReaderIndex)
                          midResult
                        }
                      case C_BUILD =>
                        codec.downOneLevel
                        val res = extractFromBsonObj(codec, List((STAR, C_BUILD)), bsonFinishReaderIndex, List((None, None, EMPTY_RANGE)))
                        List(res)
                      case _ =>
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                        List(buf)

                    }
                }
            }
          case D_BSONARRAY =>
            val startReaderIndex: Int = codec.getReaderIndex
            val valueLength2: Int = codec.readSize
            val finishReaderIndex: Int = startReaderIndex + valueLength2
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
                val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                List(List(buf), extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)).flatten
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                List(buf)
              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                val res: List[Any] = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
                if (res.isEmpty) Nil else res
              case Some(_) =>
                codec.setReaderIndex(finishReaderIndex)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
                    val res = extractFromBsonArray(codec,  valueLength2, finishReaderIndex, keyList, limitList)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(startReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                        List(List(buf), res).flatten
                      case _ => res
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                    val res = extractFromBsonArray( codec, valueLength2, finishReaderIndex, keyList, limitList)
                    List(List(buf), res).flatten
                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
                   codec.setReaderIndex(finishReaderIndex)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(startReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                        List(buf)
                      case _ =>
                        Nil
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    codec.setReaderIndex(startReaderIndex)
                    codec.readSize
                    val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                    List(buf)
                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
                    val res: List[Any] = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
                    if (res.isEmpty) Nil else res
                  case Some(_) =>
                    codec.setReaderIndex(finishReaderIndex)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        codec.downOneLevel
                        extractFromBsonArray(codec, valueLength2, finishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
                      case C_LIMIT | C_LIMITLEVEL if keyList.lengthCompare(1) > 0 && keyList.drop(1).head._2.equals(C_FILTER) =>
                        codec.setReaderIndex(finishReaderIndex)
                        Nil
                      case _ =>
                        codec.setReaderIndex(startReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].result
                        List(buf)
                    }
                }
            }
          case D_BOOLEAN =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value= codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].result
                List(value == 1)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value= codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].result
                    List(value == 1)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value= codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].result
                        List(value == 1)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value= codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].result
                        List(value == 1)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_NULL =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].result
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get =>
                    val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].result
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].result
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].result
                        List(value)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_INT =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].result
                List(value0)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].result
                    List(value0)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].result
                        List(value0)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].result
                        List(value0)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_LONG =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].result
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].result
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].result
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].result
                        List(value)
                      case _ =>
                        codec.consumeValue(seqTypeCodec)
                        Nil

                    }
                }
            }
          case D_ZERO_BYTE =>
            Nil
        }
      val actualPos2: Int = arrayFRIdx - codec.getReaderIndex//netty.readerIndex()
      actualPos2 match {
        case x if x > 0 =>
          newSeq ++ constructWithLimits(iter + 1)
        case 0 =>
          newSeq
      }
    }

    val seq: List[Any] = constructWithLimits(0)
    limitList.head._3 match {
      case UNTIL_RANGE => seq.take(seq.size - 1)
      case _ => seq
    }
  }

  /**
    * Used to traverse an Object when Condition HasElem is required.
    * It searches for an element on a sub-copy of the encoded document, limited to the object to be traversed.
    *
    * @param codec  Abstraction of Encoded sub-copy Document.
    * @param keyList  set of keys.
    * @param limitList limits of arrays.
    * @param start first index of Object.
    * @param finish last index of Object.
    * @return List with extraction result.
    */
  private def findElements(codec: Codec, keyList: List[(String,String)], limitList: List[(Option[Int], Option[Int], String)],start: Int, finish: Int): List[Any] = {
    val seqType: Int = codec.readDataType
    val finalValue: List[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNumber(CS_DOUBLE))
            List(C_MATCH)
          } else {
            //val value0 = codec.readToken(SonNumber(CS_DOUBLE))
            codec.consumeValue(seqType)
            Nil
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonString(CS_STRING))
            List(C_MATCH)
          } else {
           // val value0 =  codec.readToken(SonString(CS_STRING))
            codec.consumeValue(seqType)
            Nil
          }
        case D_BSONOBJECT =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int = codec.readSize
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                codec.setReaderIndex(bsonFinishReaderIndex)
                List(C_MATCH)
              case C_LIMIT =>
                codec.setReaderIndex(start)
                codec.readSize
                extractFromBsonObj(codec,keyList,finish,limitList) match {
                  case value if value.isEmpty => List(C_MATCH)
                  case value =>
                    codec.setReaderIndex(start)
                    codec.readSize
                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                    List(List(arr), value).flatten

                }
            }
          } else {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int = codec.readSize
            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                codec.setReaderIndex(bsonFinishReaderIndex)
                Nil
              case C_LIMIT =>
                extractFromBsonObj(codec,keyList,bsonFinishReaderIndex,limitList)

            }
          }
        case D_BSONARRAY =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int = codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                codec.setReaderIndex(arrayFinishReaderIndex)
                List(C_MATCH)
              case C_LIMIT =>
                codec.setReaderIndex(start)
                codec.readSize
                extractFromBsonObj(codec,keyList,finish,limitList) match {
                  case value if value.isEmpty =>
                    List(C_MATCH)
                  case value =>
                    codec.setReaderIndex(start)
                    codec.readSize
                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
                    List(List(arr), value).flatten

                }
            }
          } else {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int = codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LIMITLEVEL =>
                codec.setReaderIndex(arrayFinishReaderIndex)
                Nil
              case C_LIMIT =>
                codec.setReaderIndex(start)
               codec.readSize
                extractFromBsonObj(codec,keyList,finish,limitList) match {
                  case value if value.isEmpty =>
                    codec.setReaderIndex(arrayFinishReaderIndex)
                    Nil
                  case value =>
                    codec.setReaderIndex(arrayFinishReaderIndex)
                    value

                }
            }
          }
        case D_BOOLEAN =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
            List(C_MATCH)
          } else {
           // val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
            codec.consumeValue(seqType)
            Nil
          }
        case D_NULL =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNull(CS_NULL))
            List(C_MATCH)
          } else{
            //val value0 = codec.readToken(SonNull(CS_NULL))
            codec.consumeValue(seqType)
            Nil
          }
        case D_INT =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNumber(CS_INTEGER))
            List(C_MATCH)
          } else {
            //val value0 = codec.readToken(SonNumber(CS_INTEGER))
            codec.consumeValue(seqType)
            Nil
          }
        case D_LONG =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 =  codec.readToken(SonNumber(CS_LONG))
            List(C_MATCH)
          } else {
            //val value0 =  codec.readToken(SonNumber(CS_LONG))
            codec.consumeValue(seqType)
            Nil
          }
        case D_ZERO_BYTE => Nil
      }
    val actualPos2: Int = finish - codec.getReaderIndex
    actualPos2 match {
      case x if x > 0 && finalValue.nonEmpty && finalValue.head.equals(C_MATCH) =>
        val riAuz = codec.getReaderIndex
        codec.setReaderIndex(start)
        codec.readSize
        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
        codec.setReaderIndex(riAuz)
        List(arr) ++ findElements(codec,  keyList, limitList, start, finish)
      case 0 if finalValue.nonEmpty && finalValue.head.equals(C_MATCH) =>
        codec.setReaderIndex(start)
        codec.readSize
        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].result
        List(arr)
      case x if x > 0 && finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) =>
        finalValue ++ findElements(codec,  keyList,limitList,start,finish)
      case 0 if finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) => finalValue
      case _ if finalValue.nonEmpty => finalValue
      case x if x > 0 && finalValue.isEmpty =>
        findElements(codec,  keyList,limitList,start,finish)
      case 0 if finalValue.isEmpty =>
        Nil
    }
  }
  //TODO: we shoudn't be instantiating a new BosonImpl, rethink this
  def duplicate: BosonImpl = nettyBuffer match{
    case Right(x) => new BosonImpl(stringJson = Option(x))
    case Left(x) => new BosonImpl(byteArray = Option(x.array))
  }

  /**
    * Access to Encoded Document.
    * @return Either[ByteBuf, String]
    */
  def getByteBuf: Either[ByteBuf, String] = this.nettyBuffer

  /**
    * Verifies if Key given by user is HalfWord and if it matches with the one extracted.
    * @param fieldID  Key given by User.
    * @param extracted  Key extracted.
    * @return
    */
  def isHalfword(fieldID: String, extracted: String): Boolean = {
    if (fieldID.contains(STAR) & extracted.nonEmpty) {
      val list: Array[String] = fieldID.split(STAR_CHAR)
      (extracted, list.length) match {
        case (_, 0) =>
          true
        case (x, 1) if x.startsWith(list.head) =>
          true
        case (x, 2) if x.startsWith(list.head) & x.endsWith(list.last) =>
          true
        case (x, i) if i > 2 =>
          fieldID match {
            case s if s.startsWith(STAR) =>
              if (x.startsWith(list.apply(1)))
                isHalfword(s.substring(1 + list.apply(1).length), x.substring(list.apply(1).length))
              else {
                isHalfword(s, x.substring(1))
              }
            case s if !s.startsWith(STAR) =>
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

  // Injector Starts here

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
            while (buffer.getByte(buffer.readerIndex()) != 0 || key.lengthCompare(1) < 0) {
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
              if (list.lengthCompare(1) == 0) {
                if (list.head._2.contains(C_DOUBLEDOT)) {
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
                if (list.head._2.contains(C_DOUBLEDOT)) {
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
              if (list.head._2.contains(C_DOUBLEDOT))
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

  def modifyArrayEnd[T](list: List[(Statement, String)], buffer: ByteBuf, f: T => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END, result: ByteBuf = Unpooled.buffer(), resultCopy: ByteBuf = Unpooled.buffer()): BosonImpl = {
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
                if (list.head._2.contains(C_DOUBLEDOT)) {
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
                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
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
                      }
                    case _ =>
                      processTypesArray(dataType, buffer.duplicate(), result)
                      processTypesArray(dataType, buffer, resultCopy)
                  }
                }
              }
            case (x, _, C_END) if isArray && limitInf.toInt <= keyString.toInt =>
              if (list.size == 1) {
                if (list.head._2.contains(C_DOUBLEDOT) /*&& !list.head._1.isInstanceOf[KeyWithArrExpr]*/ ) {
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
                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
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
              if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ )
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
              if (list.lengthCompare(1) == 0) {
                if (list.head._2.contains(C_DOUBLEDOT)) {
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
                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
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
              if (list.head._2.contains(C_DOUBLEDOT))
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
              if (list.head._2.contains(C_DOUBLEDOT)) {
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

  private def processTypesArrayEnd[T](list: List[(Statement, String)], fieldID: String, dataType: Int, buf: ByteBuf, f: (T) => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END, result: ByteBuf, resultCopy: ByteBuf) = {
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
//        if (condition.equals(TO_RANGE)) // || condition.equals(C_END))
//          //result.writeBytes(res.getByteBuf)
//        else if (condition.equals(C_END))
//         // result.writeBytes(res.getByteBuf)
//        else
         // resultCopy.writeBytes(res.getByteBuf)
       // res.getByteBuf.release()
      case D_BSONARRAY =>
        val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f, condition, limitInf, limitSup)
//        if (condition.equals(TO_RANGE))
//       //   result.writeBytes(res.getByteBuf)
//        else if (condition.equals(C_END))
//       //   result.writeBytes(res.getByteBuf)
//        else
      //    resultCopy.writeBytes(res.getByteBuf)

      //  res.getByteBuf.release()
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

  def modifyArrayEndWithKey[T](list: List[(Statement, String)], buffer: ByteBuf, fieldID: String, f: T => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END, result: ByteBuf = Unpooled.buffer(), resultCopy: ByteBuf = Unpooled.buffer()): BosonImpl = {
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
                if (list.head._2.contains(C_DOUBLEDOT)) {
                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readBytes(size)
                  val res: BosonImpl = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup)
                  //val buf2: ByteBuf = execStatementPatternMatch(res.getByteBuf.duplicate(), list, f)

//                  if (condition.equals(TO_RANGE))
//                  //  result.writeBytes(buf2)
//                  else if (condition.equals(UNTIL_RANGE))
//                  //  resultCopy.writeBytes(buf2)
//                  else {
                    // //////println("condition END")
                 //   result.writeBytes(buf2)
                    //resultCopy.writeBytes(res.getByteBuf)
                 // }

                  buf1.release()
                  //buf2.release()
                 // res.getByteBuf.release()
                } else {
                  val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
//                  if (condition.equals(TO_RANGE))
//                 //   result.writeBytes(res.getByteBuf)
//                  else if (condition.equals(UNTIL_RANGE))
//                 //   resultCopy.writeBytes(res.getByteBuf)
//                  else {
//                 //   result.writeBytes(res.getByteBuf)
//                  }
//                 // res.getByteBuf.release()
                }
              } else {
                if (list.head._2.contains(C_DOUBLEDOT)) {

                  val size: Int = buffer.getIntLE(buffer.readerIndex())
                  val buf1: ByteBuf = buffer.readRetainedSlice(size)
                 // val buf2: ByteBuf = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup).getByteBuf
                  buf1.release()
//                  if (condition.equals(TO_RANGE))
//                  //  result.writeBytes(buf2)
//                  else if (condition.equals(UNTIL_RANGE))
//                 //   resultCopy.writeBytes(buf2)
//                  else {
//                 //   result.writeBytes(buf2)
//                  }
                 /// buf2.release()

                } else {
                  val res: BosonImpl = modifyArrayEnd(list, buffer, f, condition, limitInf, limitSup)
//                  if (condition.equals(TO_RANGE))
//                //    result.writeBytes(res.getByteBuf)
//                  else if (condition.equals(UNTIL_RANGE))
//                 //   resultCopy.writeBytes(res.getByteBuf)
//                  else {
//                 //   result.writeBytes(res.getByteBuf)
//                  }
//                //  res.getByteBuf.release()
                }
              }
            case x if (fieldID.toCharArray.deep == x.toCharArray.deep || isHalfword(fieldID, x)) && dataType != D_BSONARRAY =>
              if (list.head._2.contains(C_DOUBLEDOT) && list.head._1.isInstanceOf[KeyWithArrExpr])
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
              if (list.head._2.contains(C_DOUBLEDOT) && list.head._1.isInstanceOf[KeyWithArrExpr]) {
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
      // //////println("condition END")
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
             // val newBuf: ByteBuf = searchAndModify(list, buf, elem, f).getByteBuf
             // result.writeBytes(newBuf)
             // newBuf.release()
            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && (dataType != D_BSONARRAY) =>
              if (list.head._2.contains(C_DOUBLEDOT))
                processTypesHasElem(list, dataType, key, elem, buf, f, result)
              else
                processTypesArray(dataType, buf, result)
            case x if key.toCharArray.deep != x.toCharArray.deep && !isHalfword(key, x) =>
              if (list.head._2.contains(C_DOUBLEDOT))
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
                  if (list.head._2.contains(C_DOUBLEDOT)) {
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
                  if (list.head._2.contains(C_DOUBLEDOT)) {
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
                case C_FIRST =>
                  (key, 0, TO_RANGE, 0)
                case C_END =>
                  (key, 0, C_END, None)
                case C_ALL =>
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
                case C_FIRST => (EMPTY_KEY, 0, TO_RANGE, 0)
                case C_END => (EMPTY_KEY, 0, C_END, None)
                case C_ALL => (EMPTY_KEY, 0, TO_RANGE, C_END)
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
        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, C_END, 0.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, UNTIL_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, TO_RANGE, C_END) => // "[# .. end]"
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (EMPTY_KEY, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case TO_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = ??? // modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
          case UNTIL_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
        }
      case (k, 0, C_END, None) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readBytes(size)
        val res: ByteBuf = ??? // modifyArrayEndWithKey(list, buf1, k, f, C_END, 0.toString).getByteBuf
        result.writeBytes(res)
        res.release()
        buf1.release()
        result.capacity(result.writerIndex())

      case (k, a, UNTIL_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (k, a, TO_RANGE, C_END) =>
        val size: Int = buf.getIntLE(buf.readerIndex())
        val buf1: ByteBuf = buf.readRetainedSlice(size)
        val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString).getByteBuf
        buf1.release()
        result.writeBytes(res)
        res.release()
        result.capacity(result.writerIndex())
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case TO_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = ??? //vmodifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString, b.toString).getByteBuf
            buf1.release()
            result.writeBytes(res)
            res.release()
            result.capacity(result.writerIndex())
          case UNTIL_RANGE =>
            val size: Int = buf.getIntLE(buf.readerIndex())
            val buf1: ByteBuf = buf.readRetainedSlice(size)
            val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
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
