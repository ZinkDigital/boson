package io.zink.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._

import scala.collection.mutable.ListBuffer
import scala.reflect.io.Streamable.Bytes
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
case class CustomException(smth: String) extends RuntimeException {
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
  * @param byteArray   to be removed
  * @param javaByteBuf to be removed
  * @param stringJson  to be removed
  */
class BosonImpl(
                 byteArray: Option[Array[Byte]] = None,
                 javaByteBuf: Option[ByteBuffer] = None,
                 stringJson: Option[String] = None
               ) {
//
//  /**
//    * Deprecated, used on previous implementations of Boson to verify which input was given by the user.
//    */
//  @deprecated
//  private val valueOfArgument: String =
//  if (javaByteBuf.isDefined) {
//    javaByteBuf.get.getClass.getSimpleName
//  } else if (byteArray.isDefined) {
//    byteArray.get.getClass.getSimpleName
//  } else if (stringJson.isDefined) {
//    stringJson.get.getClass.getSimpleName
//  } else EMPTY_CONSTRUCTOR
//
//  /**
//    * Deprecated for the same reason as the previous one.
//    */
//  @deprecated
//  private val nettyBuffer: Either[ByteBuf, String] = valueOfArgument match {
//    case ARRAY_BYTE =>
//      val b: ByteBuf = Unpooled.copiedBuffer(byteArray.get)
//      Left(b)
//    case JAVA_BYTEBUFFER =>
//      val buff: ByteBuffer = javaByteBuf.get
//      if (buff.position() != 0) {
//        buff.position(0)
//        val b: ByteBuf = Unpooled.copiedBuffer(buff)
//        buff.clear()
//        Left(b)
//      } else {
//        val b: ByteBuf = Unpooled.copiedBuffer(buff)
//        buff.clear()
//        Left(b)
//      }
//    case STRING =>
//      val b: String = stringJson.get
//      Right(b)
//    case EMPTY_CONSTRUCTOR =>
//      Unpooled.buffer()
//      Left(Unpooled.buffer())
//  }
//
//  /**
//    * Set of conditions to extract primitive types while traversing an Object.
//    */
//  private val eObjPrimitiveConditions: List[(String, String)] => Boolean =
//    keyList => {
//      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_NEXT) && !keyList.head._2.equals(C_ALLNEXT) && !keyList.head._2.equals(C_LIMITLEVEL)
//    }
//
//  /**
//    * Set of conditions to extract an Object while traversing an Object.
//    */
//  private val eObjConditions: List[(String, String)] => Boolean =
//    keyList => {
//      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_LIMITLEVEL)
//    }
//
//  /**
//    * Public method to trigger extraction.
//    *
//    * @param netty1    Encoded document.
//    * @param keyList   set of keys.
//    * @param limitList limits of arrays.
//    * @tparam T type to be extracted.
//    * @return List with extraction result.
//    */
//  //TODO param [T] - To be removed
//  def extract[T](netty1: Either[ByteBuf, String], keyList: List[(String, String)],
//                 limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
//
//    val nettyC: Codec = netty1 match {
//      case Right(x) => CodecObject.toCodec(x)
//      case Left(x) => CodecObject.toCodec(x)
//    }
//
//    val startReaderIndexCodec: Int = nettyC.getReaderIndex
//    Try(nettyC.readSize) match {
//      case Success(value) =>
//        val size: Int = value
//        val seqTypeCodec: SonNamedType = nettyC.rootType
//        seqTypeCodec match {
//          case SonZero => Nil
//          case SonArray(_, _) =>
//            val arrayFinishReaderIndex: Int = startReaderIndexCodec + size
//            keyList.head._1 match {
//              case C_DOT if keyList.lengthCompare(1) == 0 =>
//                Try(nettyC.getToken(SonArray(C_DOT)).asInstanceOf[SonArray].info) match {
//                  case Success(v) =>
//                    nettyC.release()
//                    List(v)
//                  case Failure(_) =>
//                    nettyC.release()
//                    Nil
//                }
//              case _ =>
//                Try(extractFromBsonArray(nettyC, size, arrayFinishReaderIndex, keyList, limitList)) match {
//                  case Success(v) =>
//                    nettyC.release()
//                    v
//                  case Failure(_) =>
//                    nettyC.release()
//                    Nil
//                }
//            }
//          case SonObject(_, _) =>
//            val bsonFinishReaderIndex: Int = startReaderIndexCodec + size
//            keyList.head._1 match {
//              case EMPTY_KEY if keyList.head._2.equals(C_LIMITLEVEL) => Nil
//              case C_DOT if keyList.lengthCompare(1) == 0 =>
//                Try(nettyC.getToken(SonObject(C_DOT)).asInstanceOf[SonObject].info) match {
//                  case Success(v) =>
//                    nettyC.release()
//                    List(v)
//                  case Failure(_) =>
//                    nettyC.release()
//                    Nil
//                }
//              /*
//              case _ if keyList.head._2.equals(C_BUILD)=>
//                Try( extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList))match{
//                  case Success(v)=>
//                    nettyC.release()
//                    v
//                  case Failure(_)=>
//                    nettyC.release()
//                    Nil
//                }
//              */
//              case _ =>
//                Try(extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList)) match {
//                  case Success(v) =>
//                    nettyC.release()
//                    v
//                  case Failure(_) =>
//                    nettyC.release()
//                    Nil
//
//                }
//            }
//        }
//      case Failure(msg) =>
//        throw new RuntimeException(msg)
//    }
//  }
//
//  /**
//    * Structure, types, and rules when traversing an Object.
//    * Structure:
//    * Total Length -> 4 bytes
//    * Type of next Item -> 1 byte
//    * Key bytes -> unknown length, ends with 0.
//    * Value -> Number of bytes depends on type.
//    * Types:
//    * Consult Dictionary Object under ENCODING CONSTANTS.
//    *
//    * @param codec                 Abstraction of Encoded Document.
//    * @param keyList               set of keys.
//    * @param bsonFinishReaderIndex last index of Object.
//    * @param limitList             limits of arrays.
//    * @tparam T type to be extracted.
//    * @return List with extraction result.
//    */
//  //TODO [T] - To be Removed
//  private def extractFromBsonObj[T](codec: Codec, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
//    val seqTypeCodec: Int = codec.readDataType
//    val finalValue: List[Any] =
//      seqTypeCodec match {
//        case D_FLOAT_DOUBLE =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0))
//              case _ => List(value0)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0))
//              case _ => List(value0)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_BSONOBJECT =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjConditions(keyList)) {
//            val bsonStartReaderIndex: Int = codec.getReaderIndex
//            val valueTotalLength: Int = codec.readSize
//            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
//            keyList.head._2 match {
//              case C_ALLNEXT | C_ALLDOTS =>
//                val value0 = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                codec.downOneLevel
//                List(List(value0), extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)).flatten
//              case C_BUILD =>
//                codec.downOneLevel
//                val res = extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)
//                List((key.toLowerCase, res))
//              case _ =>
//                val value0 = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                List(value0)
//
//            }
//          } else {
//            val bsonStartReaderIndex: Int = codec.getReaderIndex
//            val valueTotalLength: Int = codec.readSize
//            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
//            keyList.head._2 match {
//              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
//                codec.setReaderIndex(bFnshRdrIndex)
//                Nil
//              case _ =>
//                // Truque: Esta função nao faz nada no CodecBson mas no CodecJson avança o readerindex em uma unidade
//                codec.downOneLevel
//                extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)
//            }
//          }
//        case D_BSONARRAY =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched) {
//            val arrayStartReaderIndex: Int = codec.getReaderIndex
//            val valueLength: Int = codec.readSize
//            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
//            keyList.head._2 match {
//              case C_BUILD =>
//                codec.downOneLevel
//                val res = extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
//                List((key.toLowerCase, res))
//              case C_ALLNEXT | C_ALLDOTS =>
//                val value0 = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                codec.downOneLevel
//                val res = extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
//                List(List(value0), res).flatten
//              case C_LIMIT | C_LIMITLEVEL =>
//                codec.downOneLevel
//                traverseBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
//              case _ =>
//                val value0 = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                List(value0)
//            }
//          } else {
//            val arrayStartReaderIndex: Int = codec.getReaderIndex
//            val valueLength: Int = codec.readSize
//            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
//            keyList.head._2 match {
//              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
//                codec.setReaderIndex(arrayFinishReaderIndex)
//                Nil
//              case _ =>
//                codec.downOneLevel
//                extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
//
//            }
//          }
//        case D_BOOLEAN =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0 == 1))
//              case _ => List(value0 == 1)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_NULL =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info.asInstanceOf[String]
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0))
//              case _ => List(value0)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_INT =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0))
//              case _ => List(value0)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_LONG =>
//          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
//          if (matched && eObjPrimitiveConditions(keyList)) {
//            val value0 = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
//            keyList.head._2 match {
//              case C_BUILD => List((key.toLowerCase, value0))
//              case _ => List(value0)
//            }
//          } else {
//            codec.consumeValue(seqTypeCodec)
//            Nil
//          }
//        case D_ZERO_BYTE =>
//          Nil
//      }
//
//    //Update position
//    val actualPos: Int = bsonFinishReaderIndex - codec.getReaderIndex
//    finalValue.isEmpty match {
//      case true =>
//        actualPos match {
//          case x if x > 0 => extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//          case 0 => Nil
//        }
//      case false if keyList.head._2.equals(C_BUILD) =>
//        actualPos match {
//          case x if x > 0 =>
//            finalValue ++ extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//          case 0 => finalValue
//        }
//      case false if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) || (keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.eq(STAR)) =>
//        codec.setReaderIndex(bsonFinishReaderIndex)
//        finalValue
//      case false =>
//        finalValue ++ extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//    }
//  }
//
//  /**
//    * Structure, types, and rules when traversing an Object.
//    * Structure:
//    * Total Length -> 4 bytes
//    * Type of next Item -> 1 byte
//    * Position bytes -> unknown length, ends with 0.
//    * Value -> Number of bytes depends on type.
//    * Types:
//    * Consult Dictionary Object under ENCODING CONSTANTS.
//    *
//    * @param codec      Abstraction of Encoded Document.
//    * @param length     Size of Array Object.
//    * @param arrayFRIdx last index of Array Object.
//    * @param keyList    set of keys.
//    * @param limitList  limits of arrays.
//    * @tparam T type to be extracted.
//    * @return List with extraction result.
//    */
//  private def extractFromBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
//    keyList.head._1 match {
//      case EMPTY_KEY | STAR =>
//        traverseBsonArray(codec, length, arrayFRIdx, keyList, limitList)
//      case _ if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) => Nil
//      case _ if keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.equals(EMPTY_KEY) => Nil
//
//      case _ =>
//        val seqType2: Int = codec.readDataType
//        if (seqType2 != 0) {
//          codec.readArrayPosition
//        }
//        val finalValue: List[Any] =
//          seqType2 match {
//            case D_FLOAT_DOUBLE =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_BSONOBJECT =>
//              val bsonStartReaderIndex: Int = codec.getReaderIndex
//              val valueTotalLength: Int = codec.readSize
//              val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
//              codec.downOneLevel
//              extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//            case D_BSONARRAY =>
//              val startReaderIndex: Int = codec.getReaderIndex
//              val valueLength2: Int = codec.readSize
//              val finishReaderIndex: Int = startReaderIndex + valueLength2
//              codec.downOneLevel
//              extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
//            case D_BOOLEAN =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_NULL =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_INT =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_LONG =>
//              codec.consumeValue(seqType2)
//              Nil
//            case D_ZERO_BYTE =>
//              Nil
//          }
//        val actualPos2: Int = arrayFRIdx - codec.getReaderIndex
//        if (finalValue.isEmpty) {
//          actualPos2 match {
//            case x if x > 0 => extractFromBsonArray(codec, length, arrayFRIdx, keyList, limitList)
//            case 0 => Nil
//          }
//        } else {
//          actualPos2 match {
//            case x if x > 0 =>
//              finalValue ++ extractFromBsonArray(codec, length, arrayFRIdx, keyList, limitList)
//            case 0 => finalValue
//          }
//        }
//    }
//  }
//
//  /**
//    *
//    * @param codec Abstraction of Encoded Document.
//    * @param key   given by user.
//    * @return Tuple of Boolean and String, Boolean representing if keys match, String is the key.
//    */
//  private def compareKeys(codec: Codec, key: String): (Boolean, String) = {
//    val key0 = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].info.asInstanceOf[String]
//    (key.toCharArray.deep == key0.toCharArray.deep | isHalfword(key, key0), key0)
//  }
//
//
//  /**
//    * Traverse an array taking account the limits given.
//    *
//    * @param codec      Abstraction of Encoded Document.
//    * @param length     Size of Array Object.
//    * @param arrayFRIdx last index of Array Object.
//    * @param keyList    set of keys.
//    * @param limitList  limits of arrays.
//    * @tparam T type to be extracted.
//    * @return List with extraction result.
//    */
//  private def traverseBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
//
//    /**
//      * Same structure as extractFromBsonArray(), but handles limits.
//      * Limit list has 2 optionals and a String. The String could be either "end" or "until".
//      * The possible combination of optionals, with examples, are:
//      * Some-None -> Ex: [1 to end].
//      * None None -> Used to build the entire array.
//      * Some-Some -> Ex: [0 until 10]
//      *
//      * @param iter Int to keep track of current position.
//      * @return List with extraction result.
//      */
//    def constructWithLimits(iter: Int): List[Any] = {
//
//      val seqTypeCodec: Int = codec.readDataType
//      if (seqTypeCodec != 0) {
//        codec.readArrayPosition
//      }
//      val newSeq: List[Any] =
//        seqTypeCodec match {
//          case D_FLOAT_DOUBLE =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
//                val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
//                List(value)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
//                    val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
//                    List(value)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
//                        List(value)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
//                        List(value)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
//                val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
//                List(value0)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
//                    val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
//                    List(value0)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
//                        List(value0)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
//                        List(value0)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_BSONOBJECT =>
//            val bsonStartReaderIndex: Int = codec.getReaderIndex
//            val valueTotalLength: Int = codec.readSize
//            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
//                val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                List(List(buf), extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)).flatten
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
//                val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                List(buf)
//              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
//                val res: List[Any] = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//                if (res.isEmpty) Nil else res
//              case Some(_) =>
//                codec.setReaderIndex(bsonFinishReaderIndex)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
//                    val res = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//                    codec.getDataType match {
//                      case 0 =>
//                        codec.setReaderIndex(bsonStartReaderIndex)
//                        codec.readSize
//                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                        List(List(buf), res).flatten
//                      case _ => res
//                    }
//                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
//                    val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                    val res = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//                    List(List(buf), res).flatten
//                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
//                    /*
//                    codec.setReader(bsonFinish)
//                    codec.getDataType
//                      case codec.setReader(bsonStart)
//
//
//                     */
//                    codec.setReaderIndex(bsonFinishReaderIndex)
//                    codec.getDataType match {
//                      case 0 =>
//                        codec.setReaderIndex(bsonStartReaderIndex)
//                        codec.readSize
//                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                        List(buf)
//                      case _ =>
//                        //codec.setReaderIndex(bsonFinishReaderIndex)
//                        Nil
//                    }
//                  case Some(_) if iter >= limitList.head._1.get =>
//                    //println(s"found obj, in pos: $iter")
//                    val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                    List(buf)
//                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
//                    val res: List[Any] = extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//                    if (res.isEmpty) Nil else res
//                  case Some(_) =>
//                    codec.setReaderIndex(bsonFinishReaderIndex)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_LIMIT | C_LIMITLEVEL if keyList.lengthCompare(1) > 0 && keyList.drop(1).head._2.equals(C_FILTER) =>
//                        val copyCodec1: Codec = codec.duplicate
//                        val midResult = findElements(copyCodec1, keyList, limitList, bsonStartReaderIndex, bsonFinishReaderIndex)
//                        copyCodec1.release()
//                        if (midResult.isEmpty) {
//                          codec.setReaderIndex(bsonFinishReaderIndex)
//                          Nil
//                        } else {
//                          codec.setReaderIndex(bsonFinishReaderIndex)
//                          midResult
//                        }
//                      case C_BUILD =>
//                        codec.downOneLevel
//                        val res = extractFromBsonObj(codec, List((STAR, C_BUILD)), bsonFinishReaderIndex, List((None, None, EMPTY_RANGE)))
//                        List(res)
//                      case _ =>
//                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                        List(buf)
//
//                    }
//                }
//            }
//          case D_BSONARRAY =>
//            val startReaderIndex: Int = codec.getReaderIndex
//            val valueLength2: Int = codec.readSize
//            val finishReaderIndex: Int = startReaderIndex + valueLength2
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.head._2.equals(C_LIMIT) =>
//                val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                List(List(buf), extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)).flatten
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
//                val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                List(buf)
//              case Some(_) if keyList.head._2.equals(C_LIMIT) =>
//                val res: List[Any] = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
//                if (res.isEmpty) Nil else res
//              case Some(_) =>
//                codec.setReaderIndex(finishReaderIndex)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) && limitList.head._3.equals(C_END) =>
//                    val res = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
//                    codec.getDataType match {
//                      case 0 =>
//                        codec.setReaderIndex(startReaderIndex)
//                        codec.readSize
//                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                        List(List(buf), res).flatten
//                      case _ => res
//                    }
//                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
//                    val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                    val res = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
//                    List(List(buf), res).flatten
//                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
//                    codec.setReaderIndex(finishReaderIndex)
//                    codec.getDataType match {
//                      case 0 =>
//                        codec.setReaderIndex(startReaderIndex)
//                        codec.readSize
//                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                        List(buf)
//                      case _ =>
//                        Nil
//                    }
//                  case Some(_) if iter >= limitList.head._1.get =>
//                    codec.setReaderIndex(startReaderIndex)
//                    codec.readSize
//                    val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                    List(buf)
//                  case Some(_) if keyList.head._2.equals(C_LIMIT) =>
//                    val res: List[Any] = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
//                    if (res.isEmpty) Nil else res
//                  case Some(_) =>
//                    codec.setReaderIndex(finishReaderIndex)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        codec.downOneLevel
//                        extractFromBsonArray(codec, valueLength2, finishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
//                      case C_LIMIT | C_LIMITLEVEL if keyList.lengthCompare(1) > 0 && keyList.drop(1).head._2.equals(C_FILTER) =>
//                        codec.setReaderIndex(finishReaderIndex)
//                        Nil
//                      case _ =>
//                        codec.setReaderIndex(startReaderIndex)
//                        codec.readSize
//                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
//                        List(buf)
//                    }
//                }
//            }
//          case D_BOOLEAN =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
//                val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
//                List(value == 1)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
//                    val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
//                    List(value == 1)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
//                        List(value == 1)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
//                        List(value == 1)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_NULL =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
//                val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
//                List(value)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get =>
//                    val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
//                    List(value)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
//                        List(value)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
//                        List(value)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_INT =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
//                val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
//                List(value0)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
//                    val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
//                    List(value0)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
//                        List(value0)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
//                        List(value0)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_LONG =>
//            limitList.head._2 match {
//              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
//                val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
//                List(value)
//              case Some(_) =>
//                codec.consumeValue(seqTypeCodec)
//                Nil
//              case None =>
//                limitList.head._1 match {
//                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
//                    val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
//                    List(value)
//                  case Some(_) =>
//                    codec.consumeValue(seqTypeCodec)
//                    Nil
//                  case None =>
//                    keyList.head._2 match {
//                      case C_BUILD =>
//                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
//                        List(value)
//                      case _ if keyList.head._1.equals(STAR) =>
//                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
//                        List(value)
//                      case _ =>
//                        codec.consumeValue(seqTypeCodec)
//                        Nil
//
//                    }
//                }
//            }
//          case D_ZERO_BYTE =>
//            Nil
//        }
//      val actualPos2: Int = arrayFRIdx - codec.getReaderIndex //netty.readerIndex()
//      actualPos2 match {
//        case x if x > 0 =>
//          newSeq ++ constructWithLimits(iter + 1)
//        case 0 =>
//          newSeq
//      }
//    }
//
//    val seq: List[Any] = constructWithLimits(0)
//    limitList.head._3 match {
//      case UNTIL_RANGE => seq.take(seq.size - 1)
//      case _ => seq
//    }
//  }
//
//  /**
//    * Used to traverse an Object when Condition HasElem is required.
//    * It searches for an element on a sub-copy of the encoded document, limited to the object to be traversed.
//    *
//    * @param codec     Abstraction of Encoded sub-copy Document.
//    * @param keyList   set of keys.
//    * @param limitList limits of arrays.
//    * @param start     first index of Object.
//    * @param finish    last index of Object.
//    * @return List with extraction result.
//    */
//  private def findElements(codec: Codec, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)], start: Int, finish: Int): List[Any] = {
//    val seqType: Int = codec.readDataType
//    val finalValue: List[Any] =
//      seqType match {
//        case D_FLOAT_DOUBLE =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonNumber(CS_DOUBLE))
//            List(C_MATCH)
//          } else {
//            //val value0 = codec.readToken(SonNumber(CS_DOUBLE))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonString(CS_STRING))
//            List(C_MATCH)
//          } else {
//            // val value0 =  codec.readToken(SonString(CS_STRING))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_BSONOBJECT =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val bsonStartReaderIndex: Int = codec.getReaderIndex
//            val valueTotalLength: Int = codec.readSize
//            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
//            keyList.head._2 match {
//              case C_LIMITLEVEL =>
//                codec.setReaderIndex(bsonFinishReaderIndex)
//                List(C_MATCH)
//              case C_LIMIT =>
//                codec.setReaderIndex(start)
//                codec.readSize
//                extractFromBsonObj(codec, keyList, finish, limitList) match {
//                  case value if value.isEmpty => List(C_MATCH)
//                  case value =>
//                    codec.setReaderIndex(start)
//                    codec.readSize
//                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                    List(List(arr), value).flatten
//
//                }
//            }
//          } else {
//            val bsonStartReaderIndex: Int = codec.getReaderIndex
//            val valueTotalLength: Int = codec.readSize
//            val bsonFinishReaderIndex: Int = bsonStartReaderIndex + valueTotalLength
//            keyList.head._2 match {
//              case C_LIMITLEVEL =>
//                codec.setReaderIndex(bsonFinishReaderIndex)
//                Nil
//              case C_LIMIT =>
//                extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)
//
//            }
//          }
//        case D_BSONARRAY =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val arrayStartReaderIndex: Int = codec.getReaderIndex
//            val valueLength: Int = codec.readSize
//            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
//            keyList.head._2 match {
//              case C_LIMITLEVEL =>
//                codec.setReaderIndex(arrayFinishReaderIndex)
//                List(C_MATCH)
//              case C_LIMIT =>
//                codec.setReaderIndex(start)
//                codec.readSize
//                extractFromBsonObj(codec, keyList, finish, limitList) match {
//                  case value if value.isEmpty =>
//                    List(C_MATCH)
//                  case value =>
//                    codec.setReaderIndex(start)
//                    codec.readSize
//                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//                    List(List(arr), value).flatten
//
//                }
//            }
//          } else {
//            val arrayStartReaderIndex: Int = codec.getReaderIndex
//            val valueLength: Int = codec.readSize
//            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
//            keyList.head._2 match {
//              case C_LIMITLEVEL =>
//                codec.setReaderIndex(arrayFinishReaderIndex)
//                Nil
//              case C_LIMIT =>
//                codec.setReaderIndex(start)
//                codec.readSize
//                extractFromBsonObj(codec, keyList, finish, limitList) match {
//                  case value if value.isEmpty =>
//                    codec.setReaderIndex(arrayFinishReaderIndex)
//                    Nil
//                  case value =>
//                    codec.setReaderIndex(arrayFinishReaderIndex)
//                    value
//
//                }
//            }
//          }
//        case D_BOOLEAN =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
//            List(C_MATCH)
//          } else {
//            // val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_NULL =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonNull(CS_NULL))
//            List(C_MATCH)
//          } else {
//            //val value0 = codec.readToken(SonNull(CS_NULL))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_INT =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonNumber(CS_INTEGER))
//            List(C_MATCH)
//          } else {
//            //val value0 = codec.readToken(SonNumber(CS_INTEGER))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_LONG =>
//          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
//          if (matched) {
//            val value0 = codec.readToken(SonNumber(CS_LONG))
//            List(C_MATCH)
//          } else {
//            //val value0 =  codec.readToken(SonNumber(CS_LONG))
//            codec.consumeValue(seqType)
//            Nil
//          }
//        case D_ZERO_BYTE => Nil
//      }
//    val actualPos2: Int = finish - codec.getReaderIndex
//    actualPos2 match {
//      case x if x > 0 && finalValue.nonEmpty && finalValue.head.equals(C_MATCH) =>
//        val riAuz = codec.getReaderIndex
//        codec.setReaderIndex(start)
//        codec.readSize
//        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//        codec.setReaderIndex(riAuz)
//        List(arr) ++ findElements(codec, keyList, limitList, start, finish)
//      case 0 if finalValue.nonEmpty && finalValue.head.equals(C_MATCH) =>
//        codec.setReaderIndex(start)
//        codec.readSize
//        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
//        List(arr)
//      case x if x > 0 && finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) =>
//        finalValue ++ findElements(codec, keyList, limitList, start, finish)
//      case 0 if finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) => finalValue
//      case _ if finalValue.nonEmpty => finalValue
//      case x if x > 0 && finalValue.isEmpty =>
//        findElements(codec, keyList, limitList, start, finish)
//      case 0 if finalValue.isEmpty =>
//        Nil
//    }
//  }
//
//  /**
//    * Function used to create a duplicate BosonImpl
//    *
//    * @return A new Copy of BosonImpl
//    */
//  @deprecated
//  def duplicate: BosonImpl = nettyBuffer match {
//    case Right(x) => new BosonImpl(stringJson = Option(x))
//    case Left(x) => new BosonImpl(byteArray = Option(x.array))
//  }
//
//  /**
//    * Access to Encoded Document.
//    *
//    * @return Either[ByteBuf, String]
//    */
//  @deprecated
//  def getByteBuf: Either[ByteBuf, String] = this.nettyBuffer
//
//  /**
//    * Verifies if Key given by user is HalfWord and if it matches with the one extracted.
//    *
//    * @param fieldID   Key given by User.
//    * @param extracted Key extracted.
//    * @return
//    */
//  def isHalfword(fieldID: String, extracted: String): Boolean = {
//    if (fieldID.contains(STAR) & extracted.nonEmpty) {
//      val list: Array[String] = fieldID.split(STAR_CHAR)
//      (extracted, list.length) match {
//        case (_, 0) =>
//          true
//        case (x, 1) if x.startsWith(list.head) =>
//          true
//        case (x, 2) if x.startsWith(list.head) & x.endsWith(list.last) =>
//          true
//        case (x, i) if i > 2 =>
//          fieldID match {
//            case s if s.startsWith(STAR) =>
//              if (x.startsWith(list.apply(1)))
//                isHalfword(s.substring(1 + list.apply(1).length), x.substring(list.apply(1).length))
//              else {
//                isHalfword(s, x.substring(1))
//              }
//            case s if !s.startsWith(STAR) =>
//              if (x.startsWith(list.head)) {
//                isHalfword(s.substring(list.head.length), extracted.substring(list.head.length))
//              } else {
//                false
//              }
//          }
//        case _ =>
//          false
//      }
//    } else
//      false
//  }
//
//  /**
//    * Function used to apply the final function given bbyb the used
//    *
//    * @param f     Fucntion to be applied when the final value is extracted or when the position to inject has been found
//    * @param value Value on which the function is applied to
//    * @tparam T Type of the value to be extracted of injected
//    * @return Returns the value resulting from applying the value to the function
//    */
//  private def applyFunction[T](f: T => T, value: Any): T = {
//    Try(f(value.asInstanceOf[T])) match {
//      case Success(v) =>
//        v.asInstanceOf[T]
//      case Failure(e) =>
//        value match {
//          case x: Double =>
//            Try(f(x.toFloat.asInstanceOf[T])) match {
//              case Success(v) =>
//                v.asInstanceOf[T]
//              case Failure(e1) =>
//                throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
//            }
//          case x: Array[Byte] =>
//            Try(f(new String(x).asInstanceOf[T])) match {
//              case Success(v) =>
//                v.asInstanceOf[T]
//              case Failure(e1) =>
//                Try(f(Instant.parse(new String(x)).asInstanceOf[T])) match {
//                  case Success(v) =>
//                    v.asInstanceOf[T]
//                  case Failure(e2) =>
//                    throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
//                }
//            }
//          case _ =>
//            throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
//        }
//    }
//  }
//
//  // Injector Starts here
//
//  /**
//    * Function used to search for keys of interest recursively
//    *
//    * @param list    A list with pairs that contains the key of interest and the type of operation
//    * @param codec   The codec abstraction which contains the structure to work on
//    * @param fieldID Key of interest
//    * @param f       Function used to inject a new value in the structure
//    * @tparam T Type of value to be injected
//    * @return ByteBuf containing the new value injected
//    */
//  @deprecated
//  def modifyAll[T](list: List[(Statement, String)], codec: Codec, fieldID: String, f: T => T): Codec = {
//    /*
//    * If fieldID is empty, it returns the original ByteBuf
//    */
//    val startReader: Int = codec.getReaderIndex
//    val originalSize: Int = codec.readSize
//    while ((codec.getReaderIndex - startReader) < originalSize) {
//      val dataType: Int = codec.readDataType
//      dataType match {
//        case 0 => // Final
//          codec.writeToken(SonNumber(CS_BYTE, dataType))
//        case _ =>
//          val codecWithDataType = codec.writeToken(SonNumber(CS_BYTE, dataType))
//          val (key, b): (String, Int) = {
//            val key: String = codecWithDataType.readToken(SonString(CS_NAME)) match {
//              case SonString(_, keyString) => keyString.asInstanceOf[String]
//            }
//            val token = codecWithDataType.readToken(SonBoolean(C_ZERO))
//            val b: Byte = token match {
//              case SonBoolean(_, result) => result.asInstanceOf[Byte]
//            }
//            (key, b)
//          }
//          val codecWithKey = codecWithDataType.writeToken(SonString(CS_STRING, key)).writeToken(SonNumber(CS_BYTE, b))
//
//          key match {
//            case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted) =>
//              /*
//               * Found a field equal to the key.
//               * Perform injection
//               */
//              if (list.lengthCompare(1) == 0) { //If the size of the list is 1
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  dataType match {
//                    case D_BSONOBJECT | D_BSONARRAY =>
//                      val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT) else SonArray(CS_ARRAY)
//                      val partialData: Either[ByteBuf, String] = codecWithKey.readToken(token) match {
//                        case SonObject(_, result) => result match {
//                          case byteBuf: ByteBuf => Left(byteBuf)
//                          case jsonString: String => Right(jsonString)
//                        }
//                        case SonArray(_, result) => result match {
//                          case byteBuf: ByteBuf => Left(byteBuf)
//                          case jsonString: String => Right(jsonString)
//                        }
//                      }
//                      val modifiedCodec = execStatementPatternMatch(partialData, list, f)
//                      modifierAll(modifiedCodec, dataType, f)
//
//                    case _ =>
//                      modifierAll(codecWithKey, dataType, f)
//                  }
//                } else modifierAll(codecWithKey, dataType, f)
//
//              } else {
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  dataType match {
//                    case (D_BSONOBJECT | D_BSONARRAY) =>
//                      val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT) else SonArray(CS_ARRAY)
//                      val partialData: Either[ByteBuf, String] = codecWithKey.readToken(token) match {
//                        case SonObject(_, result) => result match {
//                          case byteBuf: ByteBuf => Left(byteBuf)
//                          case jsonString: String => Right(jsonString)
//                        }
//                        case SonArray(_, result) => result match {
//                          case byteBuf: ByteBuf => Left(byteBuf)
//                          case jsonString: String => Right(jsonString)
//                        }
//                      }
//                      val modifiedCodec: Codec = execStatementPatternMatch(partialData, list.drop(1), f)
//                      execStatementPatternMatch(modifiedCodec.getCodecData, list, f)
//                    //                      buf2.release()
//                    //                      result.writeBytes(buf3)
//                    //                      buf3.release()
//                    case _ => processTypesAll(list, dataType, codecWithKey, fieldID, f)
//                  }
//                } else {
//                  dataType match {
//                    case (D_BSONOBJECT | D_BSONARRAY) =>
//                      execStatementPatternMatch(codecWithKey.getCodecData, list.drop(1), f)
//                    case _ =>
//                      processTypesArray(dataType, codecWithKey)
//                  }
//                }
//              }
//            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
//              /*
//              * Didn't found a field equal to key
//              * Consume value and check deeper Levels
//              * */
//              if (list.head._2.contains(C_DOUBLEDOT))
//                processTypesAll(list, dataType, codecWithKey, fieldID, f)
//              else
//                processTypesArray(dataType, codecWithKey)
//          }
//          codecWithKey
//      }
//    }
//    codec //TODO Maybe change this ? is this correct
//    //    result.capacity(result.\ writerIndex())
//    //    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
//    //    result.release()
//    //    finalResult
//  }
//
//  /**
//    * Function used to copied the values inside arrays that aren´t of interest to the resulting structure
//    *
//    * @param dataType The type on the value we are reading and writting on the new structure
//    * @param codec    Structure from where we read the values
//    * @return Doesn't return anything because the changes are reflected in the result structure
//    */
//  @deprecated
//  private def processTypesArray(dataType: Int, codec: Codec): Codec = {
//    dataType match {
//      case D_ZERO_BYTE => throw new Exception //TODO - NOT here!!
//      case D_FLOAT_DOUBLE =>
//        codec.writeToken(codec.readToken(SonNumber(CS_DOUBLE)))
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        //        val valueLength: Int = codec.readSize
//        //        codec.writeToken(Array(valueLength.toByte))
//        //        result.writeIntLE(valueLength)
//        codec.writeToken(codec.readToken(SonString(CS_STRING)))
//      //        result.writeCharSequence(codec.readCharSequence(valueLength, charset), charset)
//      case D_BSONOBJECT =>
//        //        val length: Int = codec.readSize
//        //        val length: Int = codec.getIntLE(codec.readerIndex())
//        //        codec.writeInformation(Array(length.toByte)) //TODO - Maybe not??
//        codec.writeToken(codec.readToken(SonObject(CS_OBJECT))) // TODO - codec + codecAux ???
//      //        val bsonBuf: ByteBuf = buf.readBytes(length)
//      //        result.writeBytes(bsonBuf)
//      //        bsonBuf.release()
//      case D_BSONARRAY =>
//        //        val length: Int = codec.readSize
//        //        codec.writeInformation(Array(length.toByte)) //TODO - Maybe not??
//        //        val length: Int = codec.getIntLE(codec.readerIndex())
//        codec.writeToken(codec.readToken(SonArray(CS_ARRAY))) // TODO - codec + codecAux ???
//      //        val bsonBuf: ByteBuf = codec.readBytes(length)
//      //        result.writeBytes(bsonBuf)
//      //        bsonBuf.release()
//      case D_NULL => throw Exception
//      case D_INT =>
//        codec.writeToken(codec.readToken(SonNumber(CS_INTEGER)))
//      //        result.writeIntLE(codec.readIntLE())
//      case D_LONG =>
//        codec.writeToken(codec.readToken(SonNumber(CS_LONG)))
//      //        result.writeLongLE(codec.readLongLE())
//      case D_BOOLEAN =>
//
//        codec.writeToken(codec.readToken(SonBoolean(CS_BOOLEAN)))
//      //        result.writeBoolean(codec.readBoolean())
//    }
//  }
//
//  /**
//    * Function used to perform injection of the new values
//    *
//    * @param codec   Structure from which we are reading the old values
//    * @param seqType Type of the value found and processing
//    * @param f       Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    */
//  @deprecated
//  private def modifierAll[T](codec: Codec, seqType: Int, f: T => T): Codec = {
//    seqType match {
//      case D_FLOAT_DOUBLE =>
//        val value0 = codec.readToken(SonNumber(CS_DOUBLE))
//        applyFunction(f, value0) match {
//          case value: SonNumber => codec.writeToken(value)
//        }
//      /*
//      Option(value) match {
//        case Some(n: Float) =>
//          result.writeDoubleLE(n.toDouble)
//        case Some(n: Double) =>
//          result.writeDoubleLE(n)
//        case _ =>
//      }*/
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        //        val length: Int = codec.getSize
//        val value0 = codec.readToken(SonString(CS_STRING))
//        //        val array: ByteBuf = Unpooled.buffer(length - 1).writeBytes(codec.readCharSequence(length - 1, charset).toString.getBytes)
//        //        codec.readByte()
//        applyFunction(f, value0) match {
//          case value: SonString => codec.writeToken(value)
//        }
//      //        array.release()
//      /*
//      Option(value) match {
//        case Some(n: Array[Byte]) =>
//          result.writeIntLE(n.length + 1).writeBytes(n).writeZero(1)
//        case Some(n: String) =>
//          val aux: Array[Byte] = n.getBytes()
//          result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
//        case Some(n: Instant) =>
//          val aux: Array[Byte] = n.toString.getBytes()
//          result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
//        case _ =>
//      }
//      */
//      case D_BSONOBJECT =>
//        //        val length: Int = codec.getSize
//        //        val valueLength: Int = codec.getIntLE(codec.readerIndex())
//        val value0 = codec.readToken(SonObject(CS_OBJECT))
//        //        val buf: ByteBuf = Unpooled.buffer(bson.capacity()).writeBytes(bson)
//        //        val arrayBytes: Array[Byte] = buf.array()
//        //        buf.release()
//        //        val bsonBytes: Array[Byte] = arrayBytes
//        //        bson.release()
//        applyFunction(f, value0) match {
//          case value: SonObject => codec.writeToken(value) // TODO - codec + codecAux ???
//        }
//      //        (result.writeBytes(newValue), newValue.length - valueLength)
//      case D_BSONARRAY =>
//        //        val length: Int = codec.getSize
//        //        val valueLength: Int = codec.getIntLE(codec.readerIndex())
//        val value0 = codec.readToken(SonArray(CS_ARRAY))
//        //        val bson: ByteBuf = codec.readBytes(valueLength)
//        //        val buf: ByteBuf = Unpooled.buffer(bson.capacity()).writeBytes(bson)
//        //        val arrayBytes: Array[Byte] = buf.array()
//        //        buf.release()
//        //        val bsonBytes: Array[Byte] = arrayBytes
//        //        bson.release()
//        applyFunction(f, value0) match {
//          case value: SonArray => codec.writeToken(value) // TODO - codec + codecAux ???
//        }
//      //        (result.writeBytes(value), value.length - valueLength)
//      case D_BOOLEAN =>
//        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
//        applyFunction(f, value0) match {
//          case value: SonBoolean => codec.writeToken(value)
//        }
//      case D_NULL =>
//        throw new Exception //TODO
//      //        throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
//      case D_INT =>
//        val value0 = codec.readToken(SonNumber(CS_INTEGER))
//        applyFunction(f, value0) match {
//          case value: SonNumber => codec.writeToken(value)
//        }
//      case D_LONG =>
//        val value0 = codec.readToken(SonNumber(CS_LONG))
//        applyFunction(f, value0) match {
//          case value: SonNumber => codec.writeToken(value)
//        }
//    }
//  }
//
//  /**
//    * Function used to copy the values inside objects that aren't of interest to the resulting structure
//    *
//    * @param list    A list with pairs that contains the key of interest and the type of operation
//    * @param seqType Type of the value found and processing
//    * @param codec   Structure from which we are reading the old values
//    * @param fieldID name of the field we are searching
//    * @param f       Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    */
//  @deprecated
//  private def processTypesAll[T](list: List[(Statement, String)], seqType: Int, codec: Codec, fieldID: String, f: T => T): Codec = {
//    seqType match {
//      case D_FLOAT_DOUBLE =>
//        codec.writeToken(codec.readToken(SonNumber(CS_DOUBLE)))
//      //        result.writeDoubleLE(codec.readDoubleLE())
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        //        val length: Int = codec.readSize
//        //        val codecAux = codec.writeInformation(Array(length.toByte))
//        codec.writeToken(codec.readToken(SonString(CS_STRING)))
//      /*val valueLength: Int = codec.readSize
//      val codec.readNextInformation()
//      result.writeIntLE(valueLength)
//      val buf: ByteBuf = codec.readBytes(valueLength)
//      result.writeBytes(buf)
//      buf.release()*/
//      case D_BSONOBJECT =>
//        //        val length: Int = codec.readSize
//        val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT)) match {
//          case SonObject(_, result) => CodecObject.toCodec(result)
//        }
//        val codecAux = modifyAll(list, partialCodec, fieldID, f)
//        // TODO - codec + codecAux ???
//        codec.writeToken(codecAux.readToken(SonObject(CS_OBJECT)))
//      /*val length: Int = codec.getIntLE(codec.readerIndex())
//      val bsonBuf: ByteBuf = codec.readBytes(length)
//      val resultAux: ByteBuf = modifyAll(list, bsonBuf, fieldID, f)
//      bsonBuf.release()
//      result.writeBytes(resultAux)
//      resultAux.release()*/
//      case D_BSONARRAY =>
//        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY)) match {
//          case SonArray(_, result) => CodecObject.toCodec(result)
//        }
//        val codecAux = modifyAll(list, partialCodec, fieldID, f)
//        // TODO - codec + codecAux ???
//        codec.writeToken(codecAux.readToken(SonArray(CS_ARRAY)))
//      //        val resultAux: ByteBuf = modifyAll(list, bsonBuf, fieldID, f)
//      //        bsonBuf.release()
//      //        result.writeBytes(resultAux)
//      //        resultAux.release()
//      case D_NULL => throw Exception
//      case D_INT =>
//        codec.writeToken(codec.readToken(SonNumber(CS_INTEGER)))
//      //        result.writeIntLE(codec.readIntLE())
//      case D_LONG =>
//        codec.writeToken(codec.readToken(SonNumber(CS_LONG)))
//      //        result.writeLongLE(codec.readLongLE())
//      case D_BOOLEAN =>
//        codec.writeToken(codec.readToken(SonBoolean(CS_BOOLEAN)))
//    }
//  }
//
//  /**
//    * Function used to perform the injection on the last ocurrence of a field
//    *
//    * @param codec      Structure from which we are reading the old values
//    * @param seqType    Type of the value found and processing
//    * @param f          Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    */
//  @deprecated
//  private def modifierEnd[T](codec: Codec, seqType: Int, f: T => T): (Codec, Codec) = {
//    seqType match {
//      case D_FLOAT_DOUBLE =>
//        val value0 = codec.readToken(SonNumber(CS_DOUBLE))
////        resultCopy.writeDoubleLE(value0)
//        val codecCopy = codec.writeToken(value0)
//        val codecRes = applyFunction(f, value0) match {
//          case x: SonNumber => codec.writeToken(x)
//        }
//        (codecRes, codecCopy)
////        Option(value) match {
////          case Some(n: Float) =>
////            result.writeDoubleLE(n.toDouble)
////          case Some(n: Double) =>
////            result.writeDoubleLE(n)
////          case _ =>
////        }
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        //        val length: Int = codec.readIntLE()
//        val value0 = codec.readToken(SonString(CS_STRING))
////        val value0: Array[Byte] = Unpooled.buffer(length - 1).writeBytes(codec.readBytes(length - 1)).array()
////        resultCopy.writeIntLE(length).writeBytes(value0)
////        codec.readByte()
//        val codecCopy = codec.writeToken(value0)
//        val codeRes = applyFunction(f, value0) match {
//          case value: SonString => codec.writeToken(value)
//        }
//        (codeRes, codecCopy)
////        val value: Any = applyFunction(f, value0)
//        /*Option(value) match {
//          case Some(n: Array[Byte]) =>
//            result.writeIntLE(n.length + 1).writeBytes(n).writeZero(1)
//          case Some(n: String) =>
//            val aux: Array[Byte] = n.getBytes()
//            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
//          case Some(n: Instant) =>
//            val aux: Array[Byte] = n.toString.getBytes()
//            result.writeIntLE(aux.length + 1).writeBytes(aux).writeZero(1)
//          case _ =>
//        }*/
//      case D_BSONOBJECT =>
//        //        val valueLength: Int = codec.getIntLE(codec.readerIndex())
//        val value0 = codec.readToken(SonObject(CS_OBJECT))
//        val codecCopy = codec.writeToken(value0)
////        resultCopy.writeBytes(bsonObj.duplicate())
////        val buf: ByteBuf = Unpooled.buffer(bsonObj.capacity()).writeBytes(bsonObj)
////        val arrayBytes: Array[Byte] = buf.array()
////        buf.release()
////        val bsonBytes: Array[Byte] = arrayBytes
////        bsonObj.release()
//        val codecRes =applyFunction(f, value0) match {
//          case value: SonObject => codec.writeToken(value) // TODO - codec + codecAux ???
//        }
//        (codecRes, codecCopy)
////        result.writeBytes(newValue)
//      case D_BSONARRAY =>
//        val value0 = codec.readToken(SonArray(CS_ARRAY))
//        val codecCopy = codec.writeToken(value0)
////        val valueLength: Int = codec.getIntLE(codec.readerIndex())
////        val bsonArray: ByteBuf = codec.readBytes(valueLength)
////        resultCopy.writeBytes(bsonArray.duplicate())
////        val buf: ByteBuf = Unpooled.buffer(bsonArray.capacity()).writeBytes(bsonArray)
////        val arrayBytes: Array[Byte] = buf.array()
////        buf.release()
////        val bsonBytes: Array[Byte] = arrayBytes
////        bsonArray.release()
//        val codecRes = applyFunction(f, value0) match {
//          case value: SonArray => codec.writeToken(value) // TODO - codec + codecAux ???
//        }
//        (codecRes, codecCopy)
////        result.writeBytes(value)
//      case D_BOOLEAN =>
//        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
//        val codecCopy = codec.writeToken(value0)
////        val value0: Boolean = codec.readBoolean()
////        resultCopy.writeBoolean(value0)
//        val codecRes = applyFunction(f, value0) match {
//          case value: SonBoolean => codec.writeToken(value)
//        }
//        (codecRes, codecCopy)
////        result.writeBoolean(value)
//      case D_NULL =>
//        throw new Exception //TODO - Change
////        throw CustomException(s"NULL field. Can not be changed") //  returns empty buffer
//      case D_INT =>
//        val value0 = codec.readToken(SonNumber(CS_INTEGER))
//        val codecCopy = codec.writeToken(value0)
////        val value0: Int = codec.readIntLE()
////        resultCopy.writeIntLE(value0)
//        val codecRes = applyFunction(f, value0) match {
//          case value: SonNumber => codec.writeToken(value)
//        }
//        (codecRes, codecCopy)
////        result.writeIntLE(value)
//      case D_LONG =>
//        val value0 = codec.readToken(SonNumber(CS_LONG))
//        val codecCopy = codec.writeToken(value0)
////        val value0: Long = codec.readLongLE()
////        resultCopy.writeLongLE(value0)
//        val codecRes = applyFunction(f, value0) match {
//          case value: SonNumber => codec.writeToken(value)
//        }
//        (codecRes, codecCopy)
////        result.writeLongLE(value)
//    }
//  }
//
//  /**
//    * Function used to perform the search on the last element of an array
//    *
//    * @param list       A list with pairs that contains the key of interest and the type of operation
//    * @param codec     Structure from which we are reading the old values
//    * @param f          Function given by the user with the new value
//    * @param condition  Represents a type of injection, it can be END, ALL, FIRST, # TO #, # UNTIL #
//    * @param limitInf   Represent the inferior limit when a range is given
//    * @param limitSup   Represent the superior limit when a range is given
//    * @tparam T Type of the value being injected
//    * @return A new bosonImpl with the new values injected
//    */
//  @deprecated
//  def modifyArrayEnd[T](list: List[(Statement, String)], codec: Codec, f: T => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END): BosonImpl = {
//    /*
//    * If fieldID is empty returns the Original BosomImpl
//    * */
//    val startReaderIndex: Int = codec.getReaderIndex
//    val originalSize: Int = codec.readSize
//    val exceptions: ListBuffer[Throwable] = new ListBuffer[Throwable]
//    while ((codec.getReaderIndex - startReaderIndex) < originalSize && exceptions.size < 2) {
//      val dataType: Int = codec.readDataType
////      result.writeByte(dataType)
////      resultCopy.writeByte(dataType)
//      dataType match {
//        case 0 => codec.writeToken(SonNumber(CS_BYTE, dataType))
//        case _ =>
//          val codecWithDataType = codec.writeToken(SonNumber(CS_BYTE, dataType))
//          val (isArray, key, b): (Boolean, String, Int) = {
//            val key: String = codecWithDataType.readToken(SonString(CS_NAME)) match {
//              case SonString(_, keyString) => keyString.asInstanceOf[String]
//            }
////            val key: ListBuffer[Byte] = new ListBuffer[Byte]
////            while (codec.getByte(codec.getReaderIndex) != 0 || key.length < 1) {
////              val b: Byte = codec.readByte()
////              key.append(b)
////            }
//            val token = codecWithDataType.readToken(SonBoolean(C_ZERO))
//            val b: Byte = token match {
//              case SonBoolean(_, result) => result.asInstanceOf[Byte]
//            }
//            (key.forall(byte => byte.isDigit), key, b)
//          }
//          val codecWithKey = codecWithDataType.writeToken(SonString(CS_STRING,key))
////          result.writeBytes(key).writeByte(b)
////          resultCopy.writeBytes(key).writeByte(b)
//          val keyString: String = new String(key)
//          (keyString, condition, limitSup) match {
//            case (x, C_END, _) if isArray =>
//              exceptions.clear()
//              if (list.size == 1) {
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  dataType match {
//                    case (D_BSONOBJECT | D_BSONARRAY) =>
//                      if (exceptions.isEmpty) {
////                        val size: Int = codec.readSize
//                        //TODO - Create function to read Obj or Arr (Maybe) +
//                        val partialCodec: Either[ByteBuf,String] = codec.readToken(SonArray(CS_ARRAY)) match {
//                          case SonArray(_, value) => value.asInstanceOf[Either[ByteBuf,String]]
//                          case SonObject(_, value) => value.asInstanceOf[Either[ByteBuf,String]]
//                        }
////                        val bufRes: ByteBuf = Unpooled.buffer()
////                        val bufResCopy: ByteBuf = Unpooled.buffer()
////                        result.clear().writeBytes(resultCopy.duplicate()) //TODO - Why!?!
//                        val codec3: Codec = //TODO - Change names when I understand what they are
//                          if (list.head._1.isInstanceOf[ArrExpr])
//                            execStatementPatternMatch(partialCodec, list, f)
//                          else
//                            CodecObject.toCodec(partialCodec)
//
//                        Try(modifierEnd(codec3, dataType, f)) match {
//                          case Success(codecTuple) => codecTuple
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
////                        (codecRes,codecResCopy)
////                        result.writeBytes(bufRes)
////                        resultCopy.writeBytes(bufResCopy)
////                        buf1.release()
////                        bufRes.release()
////                        bufResCopy.release()
////                        buf3.release()
//                      }
//                    case _ =>
//                      if (exceptions.isEmpty) {
////                        result.clear().writeBytes(resultCopy.duplicate())
//                        Try(modifierEnd(codec, dataType, f)) match {
//                          case Success(codecTuple) => codecTuple
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
//                      }
//                  }
//                } else {
//                  if (exceptions.isEmpty) {
////                    result.clear().writeBytes(resultCopy.duplicate())
//                    Try(modifierEnd(codec, dataType, f)) match {
//                      case Success(codecTuple) => codecTuple
//                      case Failure(e) =>
//                        exceptions.append(e)
//                    }
//                  }
//                }
//              } else {
//                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
////                        result.clear().writeBytes(resultCopy.duplicate())
////                        val size: Int = codec.getIntLE(codec.readerIndex())
////                        val buf1: ByteBuf = codec.readBytes(size)
//                        val codec1 = codec.readToken(SonArray(CS_ARRAY)) match{
//                          case SonArray(_,value) => value.asInstanceOf[Either[ByteBuf,String]]
//                        }
//                        // val buf2: ByteBuf = execStatementPatternMatch(buf1.duplicate(), list, f)
//                        val buf2: Either[ByteBuf,String] =
//                          if (list.head._1.isInstanceOf[ArrExpr])
//                            execStatementPatternMatch(codec1, list, f).getCodecData //I DON'T KNOW!!!!
//                          else
//                            codec1
////                            Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
//                        //val buf3: ByteBuf =
//                        Try(execStatementPatternMatch(buf2, list.drop(1), f)) match {
//                          case Success(v) =>
//                            codec.writeToken(SonNumber(CS_BYTE,v))// Not right, v is a Codec
////                            result.writeBytes(v)
////                            v.release()
////                            resultCopy.writeBytes(buf2)
//                          case Failure(e) =>
//                            result.writeBytes(buf2.duplicate())
//                            resultCopy.writeBytes(buf2)
//                            exceptions.append(e)
//                        }
//
//                        //processTypesArray(dataType, buffer, resultCopy)
//                        buf1.release()
//                        buf2.release()
//                        //buf3.release()
//                      }
//                    case _ =>
//                      processTypesArrayEnd(list, EMPTY_KEY, dataType, codec, f, condition, limitInf, limitSup, result, resultCopy)
//                  }
//                } else {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
//                        result.clear().writeBytes(resultCopy.duplicate())
//                        // val res: ByteBuf =
//                        Try(execStatementPatternMatch(codec.duplicate(), list.drop(1), f)) match {
//                          case Success(v) =>
//                            result.writeBytes(v)
//                            v.release()
//                            processTypesArray(dataType, codec, resultCopy)
//                          case Failure(e) =>
//                            processTypesArray(dataType, codec.duplicate(), result)
//                            processTypesArray(dataType, codec, resultCopy)
//                            exceptions.append(e)
//                        }
//                      }
//                    case _ =>
//                      processTypesArray(dataType, codec.duplicate(), result)
//                      processTypesArray(dataType, codec, resultCopy)
//                  }
//                }
//              }
//            case (x, _, C_END) if isArray && limitInf.toInt <= keyString.toInt =>
//              if (list.size == 1) {
//                if (list.head._2.contains(C_DOUBLEDOT) /*&& !list.head._1.isInstanceOf[KeyWithArrExpr]*/ ) {
//                  dataType match {
//                    case (D_BSONOBJECT | D_BSONARRAY) =>
//                      if (exceptions.isEmpty) {
//                        val size: Int = codec.getIntLE(codec.readerIndex())
//                        val buf1: ByteBuf = codec.readBytes(size)
//                        val bufRes: ByteBuf = Unpooled.buffer()
//                        val bufResCopy: ByteBuf = Unpooled.buffer()
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        val buf3: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                        Try(modifierEnd(buf3.duplicate(), dataType, f, bufRes, bufResCopy)) match {
//                          case Success(_) =>
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
//                        result.writeBytes(bufRes)
//                        resultCopy.writeBytes(bufResCopy)
//                        buf1.release()
//                        bufRes.release()
//                        bufResCopy.release()
//                        buf3.release()
//                      } else
//                        exceptions.append(exceptions.head)
//                    case _ =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        Try(modifierEnd(codec, dataType, f, result, resultCopy)) match {
//                          case Success(_) =>
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
//                      } else
//                        exceptions.append(exceptions.head)
//                  }
//                } else {
//                  if (exceptions.isEmpty) {
//                    resultCopy.clear().writeBytes(result.duplicate())
//                    Try(modifierEnd(codec, dataType, f, result, resultCopy)) match {
//                      case Success(_) =>
//                      case Failure(e) =>
//                        exceptions.append(e)
//                    }
//                  } else
//                    exceptions.append(exceptions.head)
//                }
//              } else {
//                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        val size: Int = codec.getIntLE(codec.readerIndex())
//                        val buf1: ByteBuf = codec.duplicate().readRetainedSlice(size)
//                        val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                        buf1.release()
//                        // val buf3: ByteBuf =
//                        Try(execStatementPatternMatch(buf2.duplicate(), list.drop(1), f)) match {
//                          case Success(v) =>
//                            buf2.release()
//                            result.writeBytes(v)
//                            v.release()
//                            processTypesArray(dataType, codec, resultCopy)
//                          case Failure(e) =>
//                            processTypesArray(dataType, codec.duplicate(), result)
//                            processTypesArray(dataType, codec, resultCopy)
//                            exceptions.append(e)
//                        }
//                      } else {
//                        exceptions.append(exceptions.head)
//                      }
//
//                    case _ =>
//                      processTypesArrayEnd(list, EMPTY_KEY, dataType, codec, f, condition, limitInf, limitSup, result, resultCopy)
//                  }
//                } else {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        //val res: ByteBuf =
//                        Try(execStatementPatternMatch(codec.duplicate(), list.drop(1), f)) match {
//                          case Success(v) =>
//                            result.writeBytes(v)
//                            processTypesArray(dataType, codec, resultCopy)
//                            v.release()
//                          case Failure(e) =>
//                            processTypesArray(dataType, codec.duplicate(), result)
//                            processTypesArray(dataType, codec, resultCopy)
//                            exceptions.append(e)
//                        }
//                      } else
//                        exceptions.append(exceptions.head)
//                    case _ =>
//                      processTypesArray(dataType, codec.duplicate(), result)
//                      processTypesArray(dataType, codec, resultCopy)
//                  }
//                }
//              }
//            case (x, _, C_END) if isArray && limitInf.toInt > keyString.toInt =>
//              if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ )
//                dataType match {
//                  case (D_BSONOBJECT | D_BSONARRAY) =>
//                    val size: Int = codec.getIntLE(codec.readerIndex())
//                    val buf1: ByteBuf = codec.readBytes(size)
//                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                    result.writeBytes(buf2.duplicate())
//                    resultCopy.writeBytes(buf2.duplicate())
//                    buf1.release()
//                    buf2.release()
//                  case _ =>
//                    processTypesArray(dataType, codec.duplicate(), result)
//                    processTypesArray(dataType, codec, resultCopy)
//                }
//              else {
//                processTypesArray(dataType, codec.duplicate(), result)
//                processTypesArray(dataType, codec, resultCopy)
//              }
//            case (x, _, l) if isArray && (limitInf.toInt <= x.toInt && l.toInt >= x.toInt) =>
//              if (list.lengthCompare(1) == 0) {
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  dataType match {
//                    case (D_BSONOBJECT | D_BSONARRAY) =>
//                      if (exceptions.isEmpty) {
//                        val size: Int = codec.getIntLE(codec.readerIndex())
//                        val buf1: ByteBuf = codec.readBytes(size)
//                        val bufRes: ByteBuf = Unpooled.buffer()
//                        val bufResCopy: ByteBuf = Unpooled.buffer()
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        val buf3: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                        Try(modifierEnd(buf3.duplicate(), dataType, f, bufRes, bufResCopy)) match {
//                          case Success(_) =>
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
//                        result.writeBytes(bufRes)
//                        resultCopy.writeBytes(bufResCopy)
//                        buf1.release()
//                        bufRes.release()
//                        bufResCopy.release()
//                        buf3.release()
//                      } else
//                        exceptions.append(exceptions.head)
//                    case _ =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        Try(modifierEnd(codec, dataType, f, result, resultCopy)) match {
//                          case Success(_) =>
//                          case Failure(e) =>
//                            exceptions.append(e)
//                        }
//                      } else
//                        exceptions.append(exceptions.head)
//
//                  }
//                } else {
//                  if (exceptions.isEmpty) {
//                    resultCopy.clear().writeBytes(result.duplicate())
//                    Try(modifierEnd(codec, dataType, f, result, resultCopy)) match {
//                      case Success(_) =>
//                      case Failure(e) =>
//                        exceptions.append(e)
//                    }
//                  } else
//                    exceptions.append(exceptions.head)
//                }
//              } else {
//                if (list.head._2.contains(C_DOUBLEDOT) /*&& list.head._1.isInstanceOf[ArrExpr]*/ ) {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        val size: Int = codec.getIntLE(codec.readerIndex())
//                        val buf1: ByteBuf = codec.duplicate().readBytes(size)
//                        val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                        buf1.release()
//                        Try(execStatementPatternMatch(buf2.duplicate(), list.drop(1), f)) match {
//                          case Success(v) =>
//                            result.writeBytes(v)
//                            v.release()
//                            processTypesArray(dataType, codec, resultCopy)
//                          case Failure(e) =>
//                            processTypesArray(dataType, codec.duplicate(), result)
//                            processTypesArray(dataType, codec, resultCopy)
//                            exceptions.append(e)
//                        }
//                        buf2.release()
//                      } else
//                        exceptions.append(exceptions.head)
//                    case _ =>
//                      processTypesArrayEnd(list, EMPTY_KEY, dataType, codec, f, condition, limitInf, limitSup, result, resultCopy)
//                  }
//                } else {
//                  dataType match {
//                    case (D_BSONARRAY | D_BSONOBJECT) =>
//                      if (exceptions.isEmpty) {
//                        resultCopy.clear().writeBytes(result.duplicate())
//                        Try(execStatementPatternMatch(codec.duplicate(), list.drop(1), f)) match {
//                          case Success(v) =>
//                            result.writeBytes(v)
//                            processTypesArray(dataType, codec, resultCopy)
//                            v.release()
//                          case Failure(e) =>
//                            processTypesArray(dataType, codec.duplicate(), result)
//                            processTypesArray(dataType, codec, resultCopy)
//                            exceptions.append(e)
//                        }
//                      } else
//                        exceptions.append(exceptions.head)
//                    case _ =>
//                      processTypesArray(dataType, codec.duplicate(), result)
//                      processTypesArray(dataType, codec, resultCopy)
//                  }
//                }
//              }
//            case (x, _, l) if isArray && (limitInf.toInt > x.toInt || l.toInt < x.toInt) =>
//              if (list.head._2.contains(C_DOUBLEDOT))
//                dataType match {
//                  case (D_BSONOBJECT | D_BSONARRAY) =>
//                    val size: Int = codec.getIntLE(codec.readerIndex())
//                    val buf1: ByteBuf = codec.readBytes(size)
//                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                    result.writeBytes(buf2.duplicate())
//                    resultCopy.writeBytes(buf2.duplicate())
//                    buf1.release()
//                    buf2.release()
//                  case _ =>
//                    processTypesArray(dataType, codec.duplicate(), result)
//                    processTypesArray(dataType, codec, resultCopy)
//                }
//              else {
//                processTypesArray(dataType, codec.duplicate(), result)
//                processTypesArray(dataType, codec, resultCopy)
//              }
//            case (x, _, l) if !isArray =>
//              if (list.head._2.contains(C_DOUBLEDOT)) {
//                dataType match {
//                  case (D_BSONOBJECT | D_BSONARRAY) =>
//                    val size: Int = codec.getIntLE(codec.readerIndex())
//                    val buf1: ByteBuf = codec.readBytes(size)
//                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list, f)
//                    result.writeBytes(buf2.duplicate())
//                    resultCopy.writeBytes(buf2.duplicate())
//                    buf1.release()
//                    buf2.release()
//                  case _ =>
//                    processTypesArray(dataType, codec.duplicate(), result)
//                    processTypesArray(dataType, codec, resultCopy)
//                }
//              } else throw CustomException("*ModifyArrayEnd* Not a Array")
//          }
//      }
//    }
//    result.capacity(result.writerIndex())
//    resultCopy.capacity(resultCopy.writerIndex())
//    val a: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
//    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity() + 4).writeIntLE(resultCopy.capacity() + 4).writeBytes(resultCopy)
//    result.release()
//    resultCopy.release()
//    if (condition.equals(TO_RANGE))
//      if (exceptions.isEmpty) {
//        val bson: BosonImpl = new BosonImpl(byteArray = Option(a.array()))
//        a.release()
//        b.release()
//        bson
//      } else
//        throw exceptions.head
//    else if (condition.equals(UNTIL_RANGE)) {
//
//      if (exceptions.length <= 1) {
//        val bson: BosonImpl = new BosonImpl(byteArray = Option(b.array()))
//        a.release()
//        b.release()
//        bson
//      } else
//        throw exceptions.head
//    } else {
//
//      if (exceptions.isEmpty) {
//        val bson: BosonImpl = new BosonImpl(byteArray = Option(a.array()))
//        a.release()
//        b.release()
//        bson
//      } else
//        throw exceptions.head
//    }
//  }
//
//  /**
//    * Function used to copy the values of an array that aren't of interest while searching for the last element
//    *
//    * @param list            A list with pairs that contains the key of interest and the type of operation
//    * @param fieldID         Name of the field of interest
//    * @param dataType        Type of the value found and processing
//    * @param codec           Structure from which we are reading the old values
//    * @param f               Function given by the user with the new value
//    * @param condition       Represents a type of injection, it can be END, ALL, FIRST, # TO #, # UNTIL #
//    * @param limitInf        Represent the inferior limit when a range is given
//    * @param limitSup        Represent the superior limit when a range is given
//    * @param resultCodec     Structure to where we write the values
//    * @param resultCopyCodec Auxiliary structure to where we write the values in case the previous cycle was the last one
//    * @tparam T Type of the value being injected
//    */
//  @deprecated
//  private def processTypesArrayEnd[T](list: List[(Statement, String)], fieldID: String, dataType: Int, codec: Codec, f: T => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END, resultCodec: Codec, resultCopyCodec: Codec): (Codec, Codec) = {
//    dataType match {
//
//      case D_FLOAT_DOUBLE =>
//        val tokenRead = codec.readToken(SonNumber(CS_DOUBLE))
//        (resultCodec.writeToken(tokenRead), resultCopyCodec.writeToken(tokenRead))
//
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        val lengthToken = codec.readToken(SonNumber(CS_INTEGER))
//        val tokenRead = codec.readToken(SonString(CS_STRING))
//        val resultWithLength = resultCodec.writeToken(lengthToken)
//        val resultCopyWithLength = resultCopyCodec.writeToken(lengthToken)
//        (resultWithLength.writeToken(tokenRead), resultCopyWithLength.writeToken(tokenRead))
//
//      case D_BSONOBJECT =>
//        modifyArrayEndWithKey(list, codec, fieldID, f, condition, limitInf, limitSup, resultCodec, resultCopyCodec)
//
//      case D_BSONARRAY =>
//        modifyArrayEndWithKey(list, codec, fieldID, f, condition, limitInf, limitSup, resultCodec, resultCopyCodec)
//
//      case D_NULL => (codec, codec)
//
//      case D_INT =>
//        val tokenRead = codec.readToken(SonNumber(CS_INTEGER))
//        (resultCodec.writeToken(tokenRead), resultCopyCodec.writeToken(tokenRead))
//
//      case D_LONG =>
//        val tokenRead = codec.readToken(SonNumber(CS_LONG))
//        (resultCodec.writeToken(tokenRead), resultCopyCodec.writeToken(tokenRead))
//
//      case D_BOOLEAN =>
//        val tokenRead = codec.readToken(SonBoolean(CS_BOOLEAN))
//        (resultCodec.writeToken(tokenRead), resultCopyCodec.writeToken(tokenRead))
//    }
//  }
//
//  /**
//    * Function used to search for the last element of an array that corresponds to field with name fieldID
//    *
//    * @param list            A list with pairs that contains the key of interest and the type of operation
//    * @param codec           Structure from which we are reading the old values
//    * @param fieldID         Name of the field of interest
//    * @param f               Function given by the user with the new value
//    * @param condition       Represents a type of injection, it can me END, ALL, FIRST, # TO #, # UNTIL #
//    * @param limitInf        Represent the inferior limit when a range is given
//    * @param limitSup        Represent the superior limit when a range is given
//    * @param resultCodec     Structure to where we write the values
//    * @param resultCopyCodec Auxiliary structure to where we write the values in case the previous cycle was the last one
//    * @tparam T Type of the value being injected
//    * @return a new BosonImpl with the new value injected
//    */
//  @deprecated
//  def modifyArrayEndWithKey[T](list: List[(Statement, String)], codec: Codec, fieldID: String, f: T => T, condition: String, limitInf: String = C_ZERO, limitSup: String = C_END, resultCodec: Codec, resultCopyCodec: Codec): (Codec, Codec) = {
//    /*
//    * Se fieldID for vazia, então deve ser chamada a funcao modifyArrayEnd to work on Root
//    *ByteBuf tem de ser duplicado no input
//    * */
//    val startReaderIndex: Int = codec.getReaderIndex
//    val originalSize: Int = codec.readSize
//    while ((codec.getReaderIndex - startReaderIndex) < originalSize) {
//      val dataType: Int = codec.readDataType
//      val modifiedResultCodec = resultCodec.writeToken(SonNumber(CS_BYTE, dataType))
//      val modifiedResultCopiedCodec = resultCopyCodec.writeToken(SonNumber(CS_BYTE, dataType))
//      dataType match {
//        case 0 =>
//        case _ =>
//          val (key, b): (String, Byte) = {
//
//            val key: String = codec.readToken(SonString(CS_NAME)) match {
//              case SonString(_, keyStr) => keyStr.asInstanceOf[String]
//            }
//            val token = codec.readToken(SonBoolean(C_ZERO))
//            val b: Byte = token match {
//              case SonBoolean(_, res) => res.asInstanceOf[Byte]
//            }
//            (key, b)
//          }
//          val modResultCodec = modifiedResultCodec.writeToken(SonString(CS_STRING, key)).writeToken(SonNumber(CS_BYTE, b))
//          val modResultCopyCodec = modifiedResultCopiedCodec.writeToken(SonString(CS_STRING, key)).writeToken(SonNumber(CS_BYTE, b))
//          key match {
//            case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType == D_BSONARRAY =>
//              /*
//              * Found a field equal to key
//              * Perform Injection
//              * */
//              if (list.size == 1) {
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
//                    case SonString(_, result) => CodecObject.toCodec(result)
//                  }
//                  modifyArrayEnd(list, partialCodec, f, condition, limitInf, limitSup)
//
//                } else modifyArrayEnd(list, codec, f, condition, limitInf, limitSup)
//
//              } else {
//                if (list.head._2.contains(C_DOUBLEDOT)) {
//                  val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
//                    case SonString(_, result) => CodecObject.toCodec(result)
//                  }
//                  //                  val size: Int = buffer.getIntLE(buffer.readerIndex())
//                  //                  val buf1: ByteBuf = buffer.readRetainedSlice(size)
//                  //                  // val buf2: ByteBuf = modifyArrayEnd(list, buf1, f, condition, limitInf, limitSup).getByteBuf
//                  //                  buf1.release()
//                  //                  //                  if (condition.equals(TO_RANGE))
//                  //                  //                  //  result.writeBytes(buf2)
//                  //                  //                  else if (condition.equals(UNTIL_RANGE))
//                  //                  //                 //   resultCopy.writeBytes(buf2)
//                  //                  //                  else {
//                  //                  //                 //   result.writeBytes(buf2)
//                  //                  //                  }
//                  //                  /// buf2.release()
//
//                } else {
//                  val res = modifyArrayEnd(list, codec, f, condition, limitInf, limitSup)
//                  //                  if (condition.equals(TO_RANGE))
//                  //                //    result.writeBytes(res.getByteBuf)
//                  //                  else if (condition.equals(UNTIL_RANGE))
//                  //                 //   resultCopy.writeBytes(res.getByteBuf)
//                  //                  else {
//                  //                 //   result.writeBytes(res.getByteBuf)
//                  //                  }
//                  //                //  res.getByteBuf.release()
//                }
//              }
//            case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType != D_BSONARRAY =>
//              if (list.head._2.contains(C_DOUBLEDOT) && list.head._1.isInstanceOf[KeyWithArrExpr])
//                processTypesArrayEnd(list, fieldID, dataType, codec, f, condition, limitInf, limitSup, resultCodec, resultCopyCodec)
//              else {
//                processTypesArray(dataType, codec.duplicate(), result) //TODO stopped here
//                processTypesArray(dataType, buffer, resultCopy)
//              }
//            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
//              /*
//              * Didn't found a field equal to key
//              * Consume value and check deeper Levels
//              * */
//              if (list.head._2.contains(C_DOUBLEDOT) && list.head._1.isInstanceOf[KeyWithArrExpr]) {
//                processTypesArrayEnd(list, fieldID, dataType, buffer, f, condition, limitInf, limitSup, result, resultCopy)
//              } else {
//                processTypesArray(dataType, buffer.duplicate(), result)
//                processTypesArray(dataType, buffer, resultCopy)
//              }
//          }
//      }
//    }
//    result.capacity(result.writerIndex())
//    resultCopy.capacity(resultCopy.writerIndex())
//    val a: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
//    val b: ByteBuf = Unpooled.buffer(resultCopy.capacity() + 4).writeIntLE(resultCopy.capacity() + 4).writeBytes(resultCopy)
//    result.release()
//    resultCopy.release()
//    if (condition.equals(TO_RANGE))
//      new BosonImpl(byteArray = Option(a.array()))
//    else if (condition.equals(UNTIL_RANGE))
//      new BosonImpl(byteArray = Option(b.array()))
//    else {
//      // //////println("condition END")
//      new BosonImpl(byteArray = Option(a.array()))
//    }
//  }
//
//  /**
//    * Function used to search for a element within another element
//    *
//    * @param list  A list with pairs that contains the key of interest and the type of operation
//    * @param codec Structure from which we are reading the old values
//    * @param key   Name of the field of interest
//    * @param elem  Name of elements to search inside the obejcts inside an Array
//    * @param f     Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    * @return A structure with the new element injected
//    */
//  @deprecated
//  def modifyHasElem[T](list: List[(Statement, String)], codec: Codec, key: String, elem: String, f: Function[T, T]): Codec = {
//    val startReader: Int = codec.getReaderIndex
//    val size: Int = codec.readSize
//    while ((codec.getReaderIndex - startReader) < size) {
//      val dataType: Int = codec.readDataType
//
//      val codecWithDataType = codec.writeToken(SonNumber(CS_BYTE, dataType))
//      dataType match {
//        case 0 =>
//        case _ =>
//          val (key, b): (String, Byte) = {
//            val key: String = codecWithDataType.readToken(SonString(CS_NAME)) match {
//              case SonString(_, keyString) => keyString.asInstanceOf[String]
//            }
//            val token = codecWithDataType.readToken(SonBoolean(C_ZERO))
//            val b: Byte = token match {
//              case SonBoolean(_, result) => result.asInstanceOf[Byte]
//            }
//            (key, b)
//          }
//          val codecWithKey = codecWithDataType.writeToken(SonString(CS_STRING, key)).writeToken(SonNumber(CS_BYTE, b))
//          key match {
//            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && dataType == D_BSONARRAY =>
//            // val newBuf: ByteBuf = searchAndModify(list, buf, elem, f).getByteBuf
//            // result.writeBytes(newBuf)
//            // newBuf.release()
//            case x if (key.toCharArray.deep == x.toCharArray.deep || isHalfword(key, x)) && (dataType != D_BSONARRAY) =>
//              if (list.head._2.contains(C_DOUBLEDOT))
//                processTypesHasElem(list, dataType, key, elem, buf, f, result)
//              else
//                processTypesArray(dataType, buf, result)
//            case x if key.toCharArray.deep != x.toCharArray.deep && !isHalfword(key, x) =>
//              if (list.head._2.contains(C_DOUBLEDOT))
//                processTypesHasElem(list, dataType, key, elem, buf, f, result)
//              else
//                processTypesArray(dataType, buf, result)
//          }
//      }
//    }
//    result.capacity(result.writerIndex())
//    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
//    result.release()
//    finalResult
//  }
//
//  /**
//    * Function used to copy values that aren't of interest while searching for a element inside a object inside a array
//    *
//    * @param list     A list with pairs that contains the key of interest and the type of operation
//    * @param dataType Type of the value found and processing
//    * @param key      Name of the field of interest
//    * @param elem     Name of elements to search inside the objects inside an Array
//    * @param buf      Structure from which we are reading the old values
//    * @param f        Function given by the user with the new value
//    * @param result   Structure to where we write the values
//    * @tparam T Type of the value being injected
//    */
//  @deprecated
//  private def processTypesHasElem[T](list: List[(Statement, String)], dataType: Int, key: String, elem: String, buf: ByteBuf, f: (T) => T, result: ByteBuf): Unit = {
//    dataType match {
//      case D_FLOAT_DOUBLE =>
//        val value0: Double = buf.readDoubleLE()
//        result.writeDoubleLE(value0)
//      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//        val valueLength: Int = buf.readIntLE()
//        val bytes: ByteBuf = buf.readBytes(valueLength)
//        result.writeIntLE(valueLength)
//        result.writeBytes(bytes)
//        bytes.release()
//      case D_BSONOBJECT =>
//        val length: Int = buf.getIntLE(buf.readerIndex())
//        val bsonBuf: ByteBuf = buf.readBytes(length)
//        val newBsonBuf: ByteBuf = modifyHasElem(list, bsonBuf, key, elem, f)
//        bsonBuf.release()
//        result.writeBytes(newBsonBuf)
//        newBsonBuf.release()
//      case D_BSONARRAY =>
//        val length: Int = buf.getIntLE(buf.readerIndex())
//        val bsonBuf: ByteBuf = buf.readRetainedSlice(length)
//        val newBsonBuf: ByteBuf = modifyHasElem(list, bsonBuf, key, elem, f)
//        bsonBuf.release()
//        result.writeBytes(newBsonBuf)
//        newBsonBuf.release()
//      case D_NULL =>
//      case D_INT =>
//        val value0: Int = buf.readIntLE()
//        result.writeIntLE(value0)
//      case D_LONG =>
//        val value0: Long = buf.readLongLE()
//        result.writeLongLE(value0)
//      case D_BOOLEAN =>
//        val value0: Boolean = buf.readBoolean()
//        result.writeBoolean(value0)
//    }
//  }
//
//  /**
//    * Function used to search for an element inside a object inside a array after finding the key of interest
//    *
//    * @param list   A list with pairs that contains the key of interest and the type of operation
//    * @param buf    Structure from which we are reading the old values
//    * @param elem   Name of elements to search inside the objects inside an Array
//    * @param f      Function given by the user with the new value
//    * @param result Structure to where we write the values
//    * @tparam T Type of the value being injected
//    * @return a new BosonImpl with the new value injected
//    */
//  @deprecated
//  private def searchAndModify[T](list: List[(Statement, String)], buf: ByteBuf, elem: String, f: Function[T, T], result: ByteBuf = Unpooled.buffer()): BosonImpl = {
//    val startReader: Int = buf.readerIndex()
//    val size: Int = buf.readIntLE()
//    while ((buf.readerIndex() - startReader) < size) {
//      val dataType: Int = buf.readByte().toInt
//      result.writeByte(dataType)
//      dataType match {
//        case 0 =>
//        case _ =>
//          val (isArray, k, b): (Boolean, Array[Byte], Byte) = {
//            val key: ListBuffer[Byte] = new ListBuffer[Byte]
//            while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
//              val b: Byte = buf.readByte()
//              key.append(b)
//            }
//            val b: Byte = buf.readByte()
//            (key.forall(byte => byte.toChar.isDigit), key.toArray, b)
//          }
//          result.writeBytes(k).writeByte(b)
//          val keyString: String = new String(k)
//          dataType match {
//            case D_BSONOBJECT =>
//              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
//              val bsonBuf: ByteBuf = Unpooled.buffer(bsonSize)
//              buf.getBytes(buf.readerIndex(), bsonBuf, bsonSize)
//              val hasElem: Boolean = hasElement(bsonBuf.duplicate(), elem)
//              if (hasElem) {
//                if (list.size == 1) {
//                  if (list.head._2.contains(C_DOUBLEDOT)) {
//                    val buf1: ByteBuf = buf.readBytes(bsonSize)
//                    val buf2: ByteBuf = Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
//                    buf1.release()
//                    val bsonBytes: Array[Byte] = buf2.array()
//                    buf2.release()
//                    val newArray: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
//                    val newbuf: ByteBuf = Unpooled.buffer(newArray.length).writeBytes(newArray)
//                    val buf3: ByteBuf = execStatementPatternMatch(newbuf, list, f)
//                    newbuf.release()
//                    result.writeBytes(buf3)
//                    buf3.release()
//                    bsonBuf.release()
//                  } else {
//                    val buf1: ByteBuf = buf.readBytes(bsonSize)
//                    val buf2: ByteBuf = Unpooled.buffer(buf1.capacity()).writeBytes(buf1)
//                    val array: Array[Byte] = buf2.array()
//                    buf2.release()
//                    val bsonBytes: Array[Byte] = array
//                    buf1.release()
//                    val value1: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
//                    result.writeBytes(value1)
//                    bsonBuf.release()
//                  }
//                } else {
//                  if (list.head._2.contains(C_DOUBLEDOT)) {
//                    val size: Int = buf.getIntLE(buf.readerIndex())
//                    val buf1: ByteBuf = buf.readBytes(size)
//                    val buf2: ByteBuf = execStatementPatternMatch(buf1, list.drop(1), f)
//                    val buf3: ByteBuf = execStatementPatternMatch(buf2.duplicate(), list, f)
//                    result.writeBytes(buf3)
//                    buf1.release()
//                    buf2.release()
//                    buf3.release()
//                    bsonBuf.release()
//                  } else {
//                    val buf1: ByteBuf = buf.readBytes(bsonSize)
//                    result.writeBytes(execStatementPatternMatch(bsonBuf, list.drop(1), f))
//                    buf1.release()
//                    bsonBuf.release()
//                  }
//                }
//              } else {
//                val buf1: ByteBuf = buf.readBytes(bsonSize)
//                result.writeBytes(buf1)
//                buf1.release()
//              }
//            case _ =>
//              processTypesArray(dataType, buf, result)
//          }
//      }
//    }
//    result.capacity(result.writerIndex())
//    val finalResult: ByteBuf = Unpooled.buffer(result.capacity() + 4).writeIntLE(result.capacity() + 4).writeBytes(result)
//    result.release()
//    new BosonImpl(byteArray = Option(finalResult.array()))
//  }
//
//  /**
//    * Function used to see if an object contains a certain elem inside
//    *
//    * @param buf  Structure from which we are reading the old values
//    * @param elem Name of element to search inside the objects inside an Array
//    * @return A boolean meaning if the element has been found or not
//    */
//  @deprecated
//  def hasElement(buf: ByteBuf, elem: String): Boolean = {
//    val size: Int = buf.readIntLE()
//    val key: ListBuffer[Byte] = new ListBuffer[Byte]
//    while (buf.readerIndex() < size && (!elem.equals(new String(key.toArray)) && !isHalfword(elem, new String(key.toArray)))) {
//      key.clear()
//      val dataType: Int = buf.readByte()
//      dataType match {
//        case 0 =>
//        case _ =>
//          while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
//            val b: Byte = buf.readByte()
//            key.append(b)
//          }
//          buf.readByte()
//          dataType match {
//            case D_ZERO_BYTE =>
//            case D_FLOAT_DOUBLE =>
//              buf.readDoubleLE()
//            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
//              val valueLength: Int = buf.readIntLE()
//              buf.readBytes(valueLength).release()
//            case D_BSONOBJECT =>
//              val valueLength: Int = buf.getIntLE(buf.readerIndex())
//              buf.readBytes(valueLength).release()
//            case D_BSONARRAY =>
//              val valueLength: Int = buf.getIntLE(buf.readerIndex())
//              buf.readBytes(valueLength).release()
//            case D_BOOLEAN =>
//              buf.readByte()
//            case D_NULL =>
//            case D_INT =>
//              buf.readIntLE()
//            case D_LONG =>
//              buf.readLongLE()
//            //case _ =>
//          }
//      }
//    }
//    new String(key.toArray).toCharArray.deep == elem.toCharArray.deep || isHalfword(elem, new String(key.toArray))
//  }
//
//  /**
//    * Function used to perform the search process recursively, performing the search key by key, separated by '.' or '..' and performing the injection when the final key is reached
//    *
//    * @param netty1     Structure from which we are reading the old values, can be either a ByteBuf or a string
//    * @param statements A list with pairs that contains the key of interest and the type of operation
//    * @param f          Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    * @return A new structure with the final value injected
//    */
//  @deprecated
//  def execStatementPatternMatch[T](netty1: Either[ByteBuf, String], statements: List[(Statement, String)], f: Function[T, T]): Codec = {
//    val codec: Codec = netty1 match {
//      case Right(x) => CodecObject.toCodec(x)
//      case Left(x) => CodecObject.toCodec(x)
//    }
//
//    val statement: Statement = statements.head._1
//    val newStatementList: List[(Statement, String)] = statements
//
//    statement match {
//      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
//        val input: (String, Int, String, Any) =
//          (arrEx.leftArg, arrEx.midArg, arrEx.rightArg) match {
//            case (i, o1, o2) if o1.isDefined && o2.isDefined =>
//              (key, arrEx.leftArg, o1.get.value, o2.get)
//            case (i, o1, o2) if o1.isEmpty && o2.isEmpty =>
//              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg)
//            case (0, str, None) =>
//              str.get.value match {
//                case C_FIRST =>
//                  (key, 0, TO_RANGE, 0)
//                case C_END =>
//                  (key, 0, C_END, None)
//                case C_ALL =>
//                  (key, 0, TO_RANGE, C_END)
//              }
//          }
//        val res: ByteBuf = execArrayFunction(newStatementList, codec, f, input._1, input._2, input._3, input._4)
//        val finalResult: ByteBuf = resultByteBuf.writeBytes(res) //começar a alterar aqui
//        res.release()
//        finalResult.capacity(finalResult.writerIndex())
//
//
//      case ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) =>
//        val input: (String, Int, String, Any) =
//          (leftArg, midArg, rightArg) match {
//            case (i, o1, o2) if midArg.isDefined && rightArg.isDefined =>
//              (EMPTY_KEY, leftArg, midArg.get.value, rightArg.get)
//            case (i, o1, o2) if midArg.isEmpty && rightArg.isEmpty =>
//              (EMPTY_KEY, leftArg, TO_RANGE, leftArg)
//            case (0, str, None) =>
//              str.get.value match {
//                case C_FIRST => (EMPTY_KEY, 0, TO_RANGE, 0)
//                case C_END => (EMPTY_KEY, 0, C_END, None)
//                case C_ALL => (EMPTY_KEY, 0, TO_RANGE, C_END)
//              }
//          }
//        val res: ByteBuf = execArrayFunction(newStatementList, netty1, f, input._1, input._2, input._3, input._4)
//        val finalResult: ByteBuf = resultByteBuf.writeBytes(res).capacity(resultByteBuf.writerIndex())
//        res.release()
//        finalResult
//
//      case HalfName(half: String) => modifyAll(newStatementList, codec, half, f)
//
//      case HasElem(key: String, elem: String) => modifyHasElem(newStatementList, codec, key, elem, f)
//
//      case Key(key: String) => modifyAll(newStatementList, codec, key, f)
//
//      case ROOT =>
//        val res: ByteBuf = execRootInjection(codec, f)
//        resultByteBuf.writeBytes(res)
//        res.release()
//        resultByteBuf.capacity(resultByteBuf.writerIndex())
//
//      case _ => throw CustomException("Wrong Statements, Bad Expression.")
//      // Never Gets Here
//
//    }
//  }
//
//  /**
//    * Function used with 'execStatementPatternMatch' to perform the process of recursive search while dealing with arrays
//    *
//    * @param list   A list with pairs that contains the key of interest and the type of operation
//    * @param buf    Structure from which we are reading the old values
//    * @param f      Function given by the user with the new value
//    * @param key    Name of value to be used in search (can be empty)
//    * @param left   Left argument of the array conditions
//    * @param mid    Middle argument of the array conditions
//    * @param right  Right argument of array conditions
//    * @param result Structure to where we write the values
//    * @tparam T Type of the value being injected
//    * @return A new structure with the final value injected
//    **/
//  @deprecated
//  def execArrayFunction[T](list: List[(Statement, String)], codec: Codec, f: Function[T, T], key: String, left: Int, mid: String, right: Any, result: ByteBuf = Unpooled.buffer()): ByteBuf = {
//    (key, left, mid.toLowerCase(), right) match {
//      case (EMPTY_KEY, 0, C_END, None) =>
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readBytes(size)
//        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, C_END, 0.toString).getByteBuf
//        result.writeBytes(res)
//        res.release()
//        buf1.release()
//        result.capacity(result.writerIndex())
//      case (EMPTY_KEY, a, UNTIL_RANGE, C_END) =>
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readBytes(size)
//        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString).getByteBuf
//        result.writeBytes(res)
//        res.release()
//        buf1.release()
//        result.capacity(result.writerIndex())
//      case (EMPTY_KEY, a, TO_RANGE, C_END) => // "[# .. end]"
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readRetainedSlice(size)
//        val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString).getByteBuf
//        buf1.release()
//        result.writeBytes(res)
//        res.release()
//        result.capacity(result.writerIndex())
//      case (EMPTY_KEY, a, expr, b) if b.isInstanceOf[Int] =>
//        expr match {
//          case TO_RANGE =>
//            val size: Int = buf.getIntLE(buf.readerIndex())
//            val buf1: ByteBuf = buf.readRetainedSlice(size)
//            val res: ByteBuf = ??? // modifyArrayEnd(list, buf1, f, TO_RANGE, a.toString, b.toString).getByteBuf
//            buf1.release()
//            result.writeBytes(res)
//            res.release()
//            result.capacity(result.writerIndex())
//          case UNTIL_RANGE =>
//            val size: Int = buf.getIntLE(buf.readerIndex())
//            val buf1: ByteBuf = buf.readRetainedSlice(size)
//            val res: ByteBuf = ??? //modifyArrayEnd(list, buf1, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
//            buf1.release()
//            result.writeBytes(res)
//            res.release()
//            result.capacity(result.writerIndex())
//        }
//      case (k, 0, C_END, None) =>
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readBytes(size)
//        val res: ByteBuf = ??? // modifyArrayEndWithKey(list, buf1, k, f, C_END, 0.toString).getByteBuf
//        result.writeBytes(res)
//        res.release()
//        buf1.release()
//        result.capacity(result.writerIndex())
//
//      case (k, a, UNTIL_RANGE, C_END) =>
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readRetainedSlice(size)
//        val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString).getByteBuf
//        buf1.release()
//        result.writeBytes(res)
//        res.release()
//        result.capacity(result.writerIndex())
//      case (k, a, TO_RANGE, C_END) =>
//        val size: Int = buf.getIntLE(buf.readerIndex())
//        val buf1: ByteBuf = buf.readRetainedSlice(size)
//        val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString).getByteBuf
//        buf1.release()
//        result.writeBytes(res)
//        res.release()
//        result.capacity(result.writerIndex())
//      case (k, a, expr, b) if b.isInstanceOf[Int] =>
//        expr match {
//          case TO_RANGE =>
//            val size: Int = buf.getIntLE(buf.readerIndex())
//            val buf1: ByteBuf = buf.readRetainedSlice(size)
//            val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, TO_RANGE, a.toString, b.toString).getByteBuf
//            buf1.release()
//            result.writeBytes(res)
//            res.release()
//            result.capacity(result.writerIndex())
//          case UNTIL_RANGE =>
//            val size: Int = buf.getIntLE(buf.readerIndex())
//            val buf1: ByteBuf = buf.readRetainedSlice(size)
//            val res: ByteBuf = ??? //modifyArrayEndWithKey(list, buf1, k, f, UNTIL_RANGE, a.toString, b.toString).getByteBuf
//            buf1.release()
//            result.writeBytes(res)
//            res.release()
//            result.capacity(result.writerIndex())
//        }
//    }
//  }
//
//  /**
//    * Function used when the injection is done at Root level
//    *
//    * @param buffer Structure from which we are reading the old values
//    * @param f      Function given by the user with the new value
//    * @tparam T Type of the value being injected
//    * @return A new structure with the final value injected
//    */
//  @deprecated
//  def execRootInjection[T](buffer: ByteBuf, f: Function[T, T]): ByteBuf = {
//    val bsonBytes: Array[Byte] = buffer.array()
//    val newBson: Array[Byte] = applyFunction(f, bsonBytes).asInstanceOf[Array[Byte]]
//    Unpooled.buffer(newBson.length).writeBytes(newBson)
//  }
}
