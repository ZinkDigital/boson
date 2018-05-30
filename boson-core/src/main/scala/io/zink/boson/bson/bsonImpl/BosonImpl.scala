package io.zink.boson.bson.bsonImpl

import java.nio.ByteBuffer
import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._

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


  type DataStructure = Either[ByteBuf, String]
  type StatementsList = List[(Statement, String)]

  /**
    * Deprecated, used on previous implementations of Boson to verify which input was given by the user.
    */
  @deprecated
  private val valueOfArgument: String =
  if (javaByteBuf.isDefined) {
    javaByteBuf.get.getClass.getSimpleName
  } else if (byteArray.isDefined) {
    byteArray.get.getClass.getSimpleName
  } else if (stringJson.isDefined) {
    stringJson.get.getClass.getSimpleName
  } else EMPTY_CONSTRUCTOR

  /**
    * Deprecated for the same reason as the previous one.
    */
  @deprecated
  private val nettyBuffer: Either[ByteBuf, String] = valueOfArgument match {
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
      val b: String = stringJson.get
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
    * @param netty1    Encoded document.
    * @param keyList   set of keys.
    * @param limitList limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  //TODO param [T] - To be removed
  def extract[T](netty1: Either[ByteBuf, String], keyList: List[(String, String)],
                 limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

    val nettyC: Codec = netty1 match {
      case Right(x) => CodecObject.toCodec(x)
      case Left(x) => CodecObject.toCodec(x)
    }

    val startReaderIndexCodec: Int = nettyC.getReaderIndex
    Try(nettyC.readSize) match {
      case Success(value) =>
        val size: Int = value
        val seqTypeCodec: SonNamedType = nettyC.rootType
        seqTypeCodec match {
          case SonZero => Nil
          case SonArray(_, _) =>
            val arrayFinishReaderIndex: Int = startReaderIndexCodec + size
            keyList.head._1 match {
              case C_DOT if keyList.lengthCompare(1) == 0 =>
                Try(nettyC.getToken(SonArray(C_DOT)).asInstanceOf[SonArray].info) match {
                  case Success(v) =>
                    nettyC.release()
                    List(v)
                  case Failure(_) =>
                    nettyC.release()
                    Nil
                }
              case _ =>
                Try(extractFromBsonArray(nettyC, size, arrayFinishReaderIndex, keyList, limitList)) match {
                  case Success(v) =>
                    nettyC.release()
                    v
                  case Failure(_) =>
                    nettyC.release()
                    Nil
                }
            }
          case SonObject(_, _) =>
            val bsonFinishReaderIndex: Int = startReaderIndexCodec + size
            keyList.head._1 match {
              case EMPTY_KEY if keyList.head._2.equals(C_LIMITLEVEL) => Nil
              case C_DOT if keyList.lengthCompare(1) == 0 =>
                Try(nettyC.getToken(SonObject(C_DOT)).asInstanceOf[SonObject].info) match {
                  case Success(v) =>
                    nettyC.release()
                    List(v)
                  case Failure(_) =>
                    nettyC.release()
                    Nil
                }
              /*
              case _ if keyList.head._2.equals(C_BUILD)=>
                Try( extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList))match{
                  case Success(v)=>
                    nettyC.release()
                    v
                  case Failure(_)=>
                    nettyC.release()
                    Nil
                }
              */
              case _ =>
                Try(extractFromBsonObj(nettyC, keyList, bsonFinishReaderIndex, limitList)) match {
                  case Success(v) =>
                    nettyC.release()
                    v
                  case Failure(_) =>
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
    * Total Length -> 4 bytes
    * Type of next Item -> 1 byte
    * Key bytes -> unknown length, ends with 0.
    * Value -> Number of bytes depends on type.
    * Types:
    * Consult Dictionary Object under ENCODING CONSTANTS.
    *
    * @param codec                 Abstraction of Encoded Document.
    * @param keyList               set of keys.
    * @param bsonFinishReaderIndex last index of Object.
    * @param limitList             limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  //TODO [T] - To be Removed
  private def extractFromBsonObj[T](codec: Codec, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    val seqTypeCodec: Int = codec.readDataType
    val finalValue: List[Any] =
      seqTypeCodec match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
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
            val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
            keyList.head._2 match {
              case C_BUILD => List((key.toLowerCase, value0))
              case _ => List(value0)
            }
          } else {
            codec.consumeValue(seqTypeCodec)
            Nil
          }
        case D_BSONOBJECT =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjConditions(keyList)) {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int = codec.readSize
            val bFnshRdrIndex: Int = bsonStartReaderIndex + valueTotalLength
            keyList.head._2 match {
              case C_ALLNEXT | C_ALLDOTS =>
                val value0 = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
                codec.downOneLevel
                List(List(value0), extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)).flatten
              case C_BUILD =>
                codec.downOneLevel
                val res = extractFromBsonObj(codec, keyList, bFnshRdrIndex, limitList)
                List((key.toLowerCase, res))
              case _ =>
                val value0 = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
                List(value0)

            }
          } else {
            val bsonStartReaderIndex: Int = codec.getReaderIndex
            val valueTotalLength: Int = codec.readSize
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
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched) {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int = codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_BUILD =>
                codec.downOneLevel
                val res = extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, List((EMPTY_KEY, C_BUILD)), List((None, None, EMPTY_RANGE)))
                List((key.toLowerCase, res))
              case C_ALLNEXT | C_ALLDOTS =>
                val value0 = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                codec.downOneLevel
                val res = extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
                List(List(value0), res).flatten
              case C_LIMIT | C_LIMITLEVEL =>
                codec.downOneLevel
                traverseBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)
              case _ =>
                val value0 = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                List(value0)
            }
          } else {
            val arrayStartReaderIndex: Int = codec.getReaderIndex
            val valueLength: Int = codec.readSize
            val arrayFinishReaderIndex: Int = arrayStartReaderIndex + valueLength
            keyList.head._2 match {
              case C_LEVEL | C_LIMITLEVEL | C_NEXT =>
                codec.setReaderIndex(arrayFinishReaderIndex)
                Nil
              case _ =>
                codec.downOneLevel
                extractFromBsonArray(codec, valueLength, arrayFinishReaderIndex, keyList, limitList)

            }
          }
        case D_BOOLEAN =>
          val (matched: Boolean, key: String) = compareKeys(codec, keyList.head._1)
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
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
            val value0 = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info.asInstanceOf[String]
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
          if (matched && eObjPrimitiveConditions(keyList)) {
            val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
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
            val value0 = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
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
    * Total Length -> 4 bytes
    * Type of next Item -> 1 byte
    * Position bytes -> unknown length, ends with 0.
    * Value -> Number of bytes depends on type.
    * Types:
    * Consult Dictionary Object under ENCODING CONSTANTS.
    *
    * @param codec      Abstraction of Encoded Document.
    * @param length     Size of Array Object.
    * @param arrayFRIdx last index of Array Object.
    * @param keyList    set of keys.
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
              extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
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
        if (finalValue.isEmpty) {
          actualPos2 match {
            case x if x > 0 => extractFromBsonArray(codec, length, arrayFRIdx, keyList, limitList)
            case 0 => Nil
          }
        } else {
          actualPos2 match {
            case x if x > 0 =>
              finalValue ++ extractFromBsonArray(codec, length, arrayFRIdx, keyList, limitList)
            case 0 => finalValue
          }
        }
    }
  }

  /**
    *
    * @param codec Abstraction of Encoded Document.
    * @param key   given by user.
    * @return Tuple of Boolean and String, Boolean representing if keys match, String is the key.
    */
  private def compareKeys(codec: Codec, key: String): (Boolean, String) = {
    val key0 = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].info.asInstanceOf[String]
    (key.toCharArray.deep == key0.toCharArray.deep | isHalfword(key, key0), key0)
  }


  /**
    * Traverse an array taking account the limits given.
    *
    * @param codec      Abstraction of Encoded Document.
    * @param length     Size of Array Object.
    * @param arrayFRIdx last index of Array Object.
    * @param keyList    set of keys.
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
                val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info
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
                val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
                List(value0)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
                    List(value0)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
                        List(value0)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info
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
                val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
                List(List(buf), extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)).flatten
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
                        List(List(buf), res).flatten
                      case _ => res
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    val buf = codec.getToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
                        List(buf)
                      case _ =>
                        //codec.setReaderIndex(bsonFinishReaderIndex)
                        Nil
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    //println(s"found obj, in pos: $iter")
                    val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                        val midResult = findElements(copyCodec1, keyList, limitList, bsonStartReaderIndex, bsonFinishReaderIndex)
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
                        val buf = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                List(List(buf), extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)).flatten
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get =>
                val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
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
                    val res = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(startReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                        List(List(buf), res).flatten
                      case _ => res
                    }
                  case Some(_) if iter >= limitList.head._1.get && keyList.head._2.equals(C_LIMIT) =>
                    val buf = codec.getToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                    val res = extractFromBsonArray(codec, valueLength2, finishReaderIndex, keyList, limitList)
                    List(List(buf), res).flatten
                  case Some(_) if iter >= limitList.head._1.get && limitList.head._3.equals(C_END) =>
                    codec.setReaderIndex(finishReaderIndex)
                    codec.getDataType match {
                      case 0 =>
                        codec.setReaderIndex(startReaderIndex)
                        codec.readSize
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                        List(buf)
                      case _ =>
                        Nil
                    }
                  case Some(_) if iter >= limitList.head._1.get =>
                    codec.setReaderIndex(startReaderIndex)
                    codec.readSize
                    val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
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
                        val buf = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info
                        List(buf)
                    }
                }
            }
          case D_BOOLEAN =>
            limitList.head._2 match {
              case Some(_) if iter >= limitList.head._1.get && iter <= limitList.head._2.get && keyList.lengthCompare(1) == 0 =>
                val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
                List(value == 1)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
                    List(value == 1)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
                        List(value == 1)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info
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
                val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get =>
                    val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNull(CS_NULL)).asInstanceOf[SonNull].info
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
                val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
                List(value0)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
                    List(value0)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
                        List(value0)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info
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
                val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
                List(value)
              case Some(_) =>
                codec.consumeValue(seqTypeCodec)
                Nil
              case None =>
                limitList.head._1 match {
                  case Some(_) if iter >= limitList.head._1.get && keyList.lengthCompare(1) == 0 =>
                    val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
                    List(value)
                  case Some(_) =>
                    codec.consumeValue(seqTypeCodec)
                    Nil
                  case None =>
                    keyList.head._2 match {
                      case C_BUILD =>
                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
                        List(value)
                      case _ if keyList.head._1.equals(STAR) =>
                        val value = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info
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
      val actualPos2: Int = arrayFRIdx - codec.getReaderIndex //netty.readerIndex()
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
    * @param codec     Abstraction of Encoded sub-copy Document.
    * @param keyList   set of keys.
    * @param limitList limits of arrays.
    * @param start     first index of Object.
    * @param finish    last index of Object.
    * @return List with extraction result.
    */
  private def findElements(codec: Codec, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)], start: Int, finish: Int): List[Any] = {
    val seqType: Int = codec.readDataType
    val finalValue: List[Any] =
      seqType match {
        case D_FLOAT_DOUBLE =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNumber(CS_DOUBLE))
            List(C_MATCH)
          } else {
            codec.consumeValue(seqType)
            Nil
          }
        case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonString(CS_STRING))
            List(C_MATCH)
          } else {
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
                extractFromBsonObj(codec, keyList, finish, limitList) match {
                  case value if value.isEmpty => List(C_MATCH)
                  case value =>
                    codec.setReaderIndex(start)
                    codec.readSize
                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                extractFromBsonObj(codec, keyList, bsonFinishReaderIndex, limitList)

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
                extractFromBsonObj(codec, keyList, finish, limitList) match {
                  case value if value.isEmpty =>
                    List(C_MATCH)
                  case value =>
                    codec.setReaderIndex(start)
                    codec.readSize
                    val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
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
                extractFromBsonObj(codec, keyList, finish, limitList) match {
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
            codec.consumeValue(seqType)
            Nil
          }
        case D_NULL =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNull(CS_NULL))
            List(C_MATCH)
          } else {
            codec.consumeValue(seqType)
            Nil
          }
        case D_INT =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNumber(CS_INTEGER))
            List(C_MATCH)
          } else {
            codec.consumeValue(seqType)
            Nil
          }
        case D_LONG =>
          val (matched: Boolean, _: String) = compareKeys(codec, keyList.drop(1).head._1)
          if (matched) {
            val value0 = codec.readToken(SonNumber(CS_LONG))
            List(C_MATCH)
          } else {
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
        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
        codec.setReaderIndex(riAuz)
        List(arr) ++ findElements(codec, keyList, limitList, start, finish)
      case 0 if finalValue.nonEmpty && finalValue.head.equals(C_MATCH) =>
        codec.setReaderIndex(start)
        codec.readSize
        val arr = codec.readToken(SonObject(CS_OBJECT)).asInstanceOf[SonObject].info
        List(arr)
      case x if x > 0 && finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) =>
        finalValue ++ findElements(codec, keyList, limitList, start, finish)
      case 0 if finalValue.nonEmpty && keyList.head._2.equals(C_LIMIT) => finalValue
      case _ if finalValue.nonEmpty => finalValue
      case x if x > 0 && finalValue.isEmpty =>
        findElements(codec, keyList, limitList, start, finish)
      case 0 if finalValue.isEmpty =>
        Nil
    }
  }

  /**
    * Function used to create a duplicate BosonImpl
    *
    * @return A new Copy of BosonImpl
    */
  @deprecated
  def duplicate: BosonImpl = nettyBuffer match {
    case Right(x) => new BosonImpl(stringJson = Option(x))
    case Left(x) => new BosonImpl(byteArray = Option(x.array))
  }

  /**
    * Access to Encoded Document.
    *
    * @return Either[ByteBuf, String]
    */
  @deprecated
  def getByteBuf: Either[ByteBuf, String] = this.nettyBuffer


  //----------------------------------------------------- INJECTORS ----------------------------------------------------


  /**
    * Starter method for the injection process, this method will pattern match the statements in the statements list
    * and will delegate to other helper methods
    *
    * @param dataStructure - The data structure in which to perform the injection process (either a ByteBuf or a String)
    * @param statements    - The statements with information regarding where to perform the injection
    * @param injFunction   - The injection function to be applied
    * @tparam T - The type of the input and output of the injection function
    * @return a new codec with the changes applied to it
    */
  def inject[T](dataStructure: DataStructure, statements: StatementsList, injFunction: T => T): Codec = {
    println(statements)
    val codec: Codec = dataStructure match {
      case Right(jsonString) => CodecObject.toCodec(jsonString)
      case Left(byteBuf) => CodecObject.toCodec(byteBuf)
    }

    statements.head._1 match {
      case ROOT => rootInjection(codec, injFunction)

      case Key(key: String) => modifyAll(statements, codec, key, injFunction)

      case HalfName(half: String) => modifyAll(statements, codec, half, injFunction)

      case HasElem(key: String, elem: String) => modifyHasElem(statements, codec, key, elem, injFunction)

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
        val modified = arrayInjection(statements, codec, codec.duplicate, injFunction, input._1, input._2, input._3, input._4)
        codec + modified

      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
        val input: (String, Int, String, Any) =
          (arrEx.leftArg, arrEx.midArg, arrEx.rightArg) match {

            case (_, o1, o2) if o1.isDefined && o2.isDefined =>
              (key, arrEx.leftArg, o1.get.value, o2.get) //User sent, for example, Key[1 TO 3] translate to - Key[1 TO 3]

            case (_, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg) //User sent, for example, Key[1] translates to - Key[1 TO 1]

            case (0, str, None) =>
              str.get.value match {
                case C_FIRST =>
                  (key, 0, TO_RANGE, 0) //User sent Key[first] , translates to - Key[0 TO 0]
                case C_END =>
                  (key, 0, C_END, None)
                case C_ALL =>
                  (key, 0, TO_RANGE, C_END) //user sent Key[all], translates to - Key[0 TO END]
              }
          }
        val modifiedCodec = arrayInjection(statements, codec, codec.duplicate, injFunction, input._1, input._2, input._3, input._4)
        codec + modifiedCodec

      case _ => throw CustomException("Wrong Statements, Bad Expression.")
    }
  }

  /**
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param fieldID        - Name of the field of interest
    * @param injFunction    - The injection function to be applied
    * @tparam T - The type of input and output of the injection function
    * @return A Codec containing the alterations made
    */
  private def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T): Codec = {

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec - the codec to be modified
      * @param startReader  - the initial reader index of the codec passed in modifyAll
      * @param originalSize - the original size of the codec passed in modifyAll
      * @return A Codec containing the alterations made
      */
    def writeCodec(currentCodec: Codec, startReader: Int, originalSize: Int): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) currentCodec
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte))
        val newCodec = dataType match {
          case 0 => writeCodec(codecWithDataType, startReader, originalSize) // This is the end
          case _ =>
            val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)

            key match {
              case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep => //add isHalWord Later
                if (statementsList.lengthCompare(1) == 0) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT) else SonArray(CS_ARRAY)
                        val partialData = codec.readToken(token) match {
                          case SonObject(_, result) => result match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case string: String => Right(string)
                          }
                          case SonArray(_, result) => result match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case string: String => Right(string)
                          }
                        }
                        val partialCodec = inject(partialData, statementsList, injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                        modifierAll(codec, codecWithKey + partialCodec, dataType, injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                      case _ =>
                        modifierAll(codec, codecWithKey, dataType, injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                    }
                  } else {
                    modifierAll(codec, codecWithKey, dataType, injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT) else SonArray(CS_ARRAY)
                        val partialData = codec.readToken(token) match {
                          case SonObject(_, result) => result match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case string: String => Right(string)
                          }
                          case SonArray(_, result) => result match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case string: String => Right(string)
                          }
                        }
                        val modifiedPartialCodec = inject(partialData, statementsList.drop(1), injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                        inject((codecWithKey + modifiedPartialCodec).getCodecData, statementsList, injFunction) //TODO ADD THE SIZE TO THE SUB CODEC
                      //TODO PROBABLY WE WILL NEED TO SET THE READER INDEX TO SKIP THE BYTES THE SUBCODEC ALREADY READ

                      case _ =>
                        processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                    }
                  } else {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val subCodecNoSize = inject(codec.getCodecData, statementsList.drop(1), injFunction)
                        subCodecNoSize.removeEmptySpace //remove empty space from the subcodec
                      val subCodec = subCodecNoSize.addSize //add the size of the subcodec to itself
                      val mergedCodecs = codecWithKey + subCodec
                        mergedCodecs.setReaderIndex(subCodec.getSize) //Skip the bytes that the subcodec already read
                        mergedCodecs
                      case _ => processTypesArray(dataType, codec, codecWithKey)
                    }
                  }
                }
              case x if fieldID.toCharArray.deep != x.toCharArray.deep => //TODO - add !isHalfWord
                if (statementsList.head._2.contains(C_DOUBLEDOT))
                  processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                else
                  processTypesArray(dataType, codec, codecWithKey)
            }
        }
        writeCodec(newCodec, startReader, originalSize)
      }
    }

    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val emptyCodec: Codec = createEmptyCodec(codec)

    val codecWithoutSize = writeCodec(emptyCodec, startReader, originalSize)
    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSize.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace
    codecWithSize + codecWithoutSize
  }

  /**
    * Function used to search for a element within an object
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param fieldID        - Name of the field of interest
    * @param elem           - Name of the element to look for inside the objects inside an Array
    * @param injFunction    - The injection function to be applied
    * @tparam T - The type of input and output of the injection function
    * @return a modified Codec where the injection function may have been applied to the desired element (if it exists)
    */
  private def modifyHasElem[T](statementsList: StatementsList, codec: Codec, fieldID: String, elem: String, injFunction: T => T): Codec = {

    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val emptyDataStructure: Codec = createEmptyCodec(codec)

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to be modified
      * @return A modified codec
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType.toByte)) //write the read byte to a Codec
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType)
          case _ => //In case its not the end

            val (codecWithKeyByte, key) = writeKeyAndByte(codec, codecWithDataType)

            //We only want to modify if the dataType is an Array and if the extractedKey matches with the fieldID
            //or they're halfword's
            //in all other cases we just want to copy the data from one codec to the other (using "process" like functions)
            key match {
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType == D_BSONARRAY =>
                //the key is a halfword and matches with the extracted key, dataType is an array
                val modifiedCodec: Codec = searchAndModify(statementsList, codec, fieldID, injFunction, codecWithKeyByte)
                iterateDataStructure(modifiedCodec)

              case _ =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  val processedCodec: Codec = processTypesHasElem(statementsList, dataType, fieldID, elem, codec, codecWithKeyByte, injFunction)
                  iterateDataStructure(processedCodec)
                } else {
                  val processedCodec: Codec = processTypesArray(dataType, codec, codecWithKeyByte)
                  iterateDataStructure(processedCodec)
                }
            }
        }
      }
    }

    val codecWithoutSize = iterateDataStructure(emptyDataStructure) //Initiate recursion with an empty data structure
    val size = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    emptyDataStructure.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, size)) + codecWithoutSize
  }

  /**
    * Function used to search for an element inside an object inside an array after finding the key of interest
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param fieldID        - Name of the field of interest
    * @param injFunction    - The injection function to be applied
    * @param writableCodec  - Structure to where we write the values
    * @tparam T - The type of input and output of the injection function
    * @return a new Codec with the value injected
    */
  private def searchAndModify[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T, writableCodec: Codec): Codec = {
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to be modified
      * @return A modified codec
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val dataType = codec.readDataType
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType.toByte)) //write the read byte to a Codec
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType)
          case _ =>
            val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)
            dataType match {
              case D_BSONOBJECT =>
                val bsonSize = codec.getSize
                val partialCodec: Codec = codec.getToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
                  case SonObject(_, dataStruct) => dataStruct match {
                    case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                    case jsonString: String => CodecObject.toCodec(jsonString)
                  }
                } //Obtain only the size and the object itself of this BsonObject
                if (hasElem(partialCodec.duplicate, fieldID)) {

                  if (statementsList.size == 1) {

                    val codecWithObj: Codec = codec.readSpecificSize(bsonSize)
                    val byteArr: Array[Byte] = codecWithObj.getCodecData match {
                      case Left(byteBuff) => byteBuff.array
                      case Right(jsonString) => jsonString.getBytes
                    }
                    val newArray: Array[Byte] = applyFunction(injFunction, byteArr).asInstanceOf[Array[Byte]]
                    val codecFromByteArr = codec.fromByteArray(newArray)
                    if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                      val newCodec = inject(codec.getCodecData, statementsList, injFunction)
                      iterateDataStructure(codecWithKey + newCodec)
                    } else {
                      iterateDataStructure(codecWithKey + codecFromByteArr)
                    }

                  } else {

                    if (statementsList.head._2.contains(C_DOUBLEDOT)) {

                      val dataStruct: Either[ByteBuf, String] = codec.getToken(SonString(CS_ARRAY)) match {
                        case SonString(_, data) => data match {
                          case byteBuf: ByteBuf => Left(byteBuf)
                          case jsonString: String => Right(jsonString)
                        }
                      }
                      val newCodec = inject(dataStruct, statementsList.drop(1), injFunction)
                      val modifiedCodec = inject(newCodec.duplicate.getCodecData, statementsList, injFunction)
                      iterateDataStructure(codecWithKey + modifiedCodec)

                    } else {

                      codec.readSpecificSize(bsonSize) //read the size of the object
                      val newCodec = inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                      iterateDataStructure(codecWithKey + newCodec)

                    }
                  }

                } else {
                  val newCodec = codec.readSpecificSize(bsonSize) //read the size of the object
                  iterateDataStructure(codecWithKey + newCodec)
                }

              case _ => processTypesArray(dataType, codec, writableCodec)
            }
        }
      }
      //TODO maybe do something more here ?
    }

    iterateDataStructure(writableCodec)
  }

  /**
    * Method used to see if an object contains a certain element inside it
    *
    * @param codec - The structure in which to look for the element
    * @param elem  - The name of the element to look for
    * @return A boolean value saying if the given element is present in that object
    */
  private def hasElem(codec: Codec, elem: String): Boolean = {
    val size: Int = codec.readSize
    var key: String = ""
    //Iterate through all of the keys from the dataStructure in order to see if it contains the elem
    while (codec.getReaderIndex < size && (!elem.equals(key) && !isHalfword(elem, key))) {
      key = "" //clear the key
      val dataType = codec.readDataType
      dataType match {
        case 0 => //TODO what do we do in this case
        case _ =>
          val key: String = codec.readToken(SonString(CS_NAME)) match {
            case SonString(_, keyString) => keyString.asInstanceOf[String]
          }
          codec.readToken(SonBoolean(C_ZERO)) //read the closing byte of the key, we're not interested in this value
          dataType match {
            case D_ZERO_BYTE => //TODO what do we do in this case

            case D_FLOAT_DOUBLE => codec.readToken(SonNumber(CS_DOUBLE))

            case D_ARRAYB_INST_STR_ENUM_CHRSEQ => codec.readToken(SonString(CS_ARRAY))

            case D_BSONOBJECT | D_BSONARRAY => codec.getToken(SonString(CS_ARRAY))

            case D_BOOLEAN => codec.readToken(SonBoolean(CS_BOOLEAN))

            case D_NULL => //TODO what do we do in this case

            case D_INT => codec.readToken(SonNumber(CS_INTEGER))

            case D_LONG => codec.readToken(SonNumber(CS_LONG))
          }
      }
    }
    key.toCharArray.deep == elem.toCharArray.deep || isHalfword(elem, key)
  }

  /**
    * Method that will perform the injection in the root of the data structure
    *
    * @param codec       - Codec encapsulating the data structure to inject in
    * @param injFunction - The injection function to be applied
    * @tparam T - The type of elements the injection function receives
    * @return - A new codec with the injFunction applied to it
    */
  private def rootInjection[T](codec: Codec, injFunction: T => T): Codec = {
    codec.getCodecData match {
      case Left(byteBuf) =>
        val bsonBytes: Array[Byte] = byteBuf.array() //extract the bytes from the bytebuf
      /*
        We first cast the result of applyFunction as String because when we input a byte array we apply the injFunction to it
        as a String. We return that modified String and convert it back to a byte array in order to create a ByteBuf from it
       */
      val modifiedBytes: Array[Byte] = applyFunction(injFunction, bsonBytes).asInstanceOf[String].getBytes()
        val newBuf = Unpooled.buffer(modifiedBytes.length).writeBytes(modifiedBytes) //create a new ByteBuf from those bytes
        CodecObject.toCodec(newBuf)

      case Right(jsonString) => //TODO not sure if this is correct for CodecJson
        val modifiedString: String = applyFunction(injFunction, jsonString).asInstanceOf[String]
        CodecObject.toCodec(modifiedString)
    }
  }

  /**
    * Function used to copy values that aren't of interest while searching for a element inside a object inside a array
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param dataType       - Type of the value found
    * @param fieldID        - Name of the field of interest
    * @param elem           - Name of the element to search inside the objects inside an Array
    * @param codec          - Structure from which we are reading the old values
    * @param resultCodec    - Structure to where we write the values
    * @param injFunction    - Injection function to be applied
    * @tparam T - The type of input and output of the injection function
    * @return a new Codec with the copied information
    */
  private def processTypesHasElem[T](statementsList: StatementsList, dataType: Int, fieldID: String, elem: String, codec: Codec, resultCodec: Codec, injFunction: T => T): Codec = dataType match {
    case D_BSONOBJECT =>
      val bsonObjectCodec: Codec = codec.getToken(SonString(CS_ARRAY)) match {
        case SonString(_, data) => data match {
          case byteBuff: ByteBuf => CodecObject.toCodec(byteBuff)
          case jsonString: String => CodecObject.toCodec(jsonString)
        }
      }
      val modifiedCodec: Codec = modifyHasElem(statementsList, bsonObjectCodec, fieldID, elem, injFunction)
      resultCodec + modifiedCodec

    case D_BSONARRAY =>
      val length = codec.getSize
      val partialCodec: Codec = codec.readSlice(length)
      val modifiedCodec: Codec = modifyHasElem(statementsList, partialCodec, fieldID, elem, injFunction)
      resultCodec + modifiedCodec

    case D_FLOAT_DOUBLE => codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_DOUBLE)))

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ => codec.writeToken(resultCodec, SonString(CS_ARRAY_WITH_SIZE))

    case D_INT => codec.writeToken(resultCodec, SonNumber(CS_INTEGER))

    case D_LONG => codec.writeToken(resultCodec, SonNumber(CS_LONG))

    case D_BOOLEAN => codec.writeToken(resultCodec, SonBoolean(CS_BOOLEAN))

    case D_NULL => ??? //TODO what do we do in this case
  }

  /**
    *
    * @param dataType        - Type of the value found and processing
    * @param codec           - Structure from which we are reading the values
    * @param currentResCodec - Structure that contains the information already processed and where we write the values
    * @return A Codec containing the alterations made
    */
  private def processTypesArray(dataType: Int, codec: Codec, currentResCodec: Codec): Codec = {
    dataType match {
      case D_ZERO_BYTE =>
        currentResCodec
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        codec.writeToken(currentResCodec, codec.readToken(SonString(CS_STRING)))
      case D_BSONOBJECT =>
        codec.writeToken(currentResCodec, codec.readToken(SonObject(CS_OBJECT)))
      case D_BSONARRAY =>
        codec.writeToken(currentResCodec, codec.readToken(SonArray(CS_ARRAY)))
      case D_NULL => throw new Exception
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
      case D_BOOLEAN =>
        codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
    }
  }

  /**
    *
    * @param codec           - Structure from which we are reading the values
    * @param currentResCodec - Structure that contains the information already processed and where we write the values
    * @param seqType         - Type of the value found and processing
    * @param injFunction     - Function given by the user with the new value
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifierAll[T](codec: Codec, currentResCodec: Codec, seqType: Int, injFunction: T => T): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0 = codec.readToken(SonNumber(CS_DOUBLE)) match {
          case SonNumber(_, data) => data.asInstanceOf[Double]
        }
        applyFunction(injFunction, value0) match {
          case value: Double => codec.writeToken(currentResCodec, SonNumber(CS_DOUBLE, value))
        }

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        applyFunction(injFunction, value0) match {
          case value: String => codec.writeToken(currentResCodec, SonString(CS_STRING, value))
        }

      case D_BSONOBJECT =>
        codec.readToken(SonObject(CS_OBJECT)) match {
          case SonObject(_, data) => data match {
            case byteBuf: ByteBuf =>
              applyFunction(injFunction, byteBuf.array()) match {
                case arr: Array[Byte] => codec.writeToken(currentResCodec, SonArray(CS_ARRAY, arr))
              }
            case str: String =>
              applyFunction(injFunction, str) match {
                case resString: String => codec.writeToken(currentResCodec, SonString(CS_STRING, resString))
              }
          }
        }

      case D_BSONARRAY =>
        codec.readToken(SonArray(CS_ARRAY)) match {
          case SonArray(_, data) => data match {
            case byteBuf: ByteBuf =>
              applyFunction(injFunction, byteBuf.array()) match {
                case arr: Array[Byte] => codec.writeToken(currentResCodec, SonArray(CS_ARRAY, arr))
              }
            case str: String =>
              applyFunction(injFunction, str) match {
                case resString: String => codec.writeToken(currentResCodec, SonString(CS_STRING, resString))
              }
          }
        }

      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)) match {
          case SonBoolean(_, data) => data.asInstanceOf[Boolean]
        }
        applyFunction(injFunction, value0) match {
          case value: Boolean => codec.writeToken(currentResCodec, SonBoolean(CS_BOOLEAN, value))
        }

      case D_NULL => throw new Exception //TODO

      case D_INT =>
        val value0 = codec.readToken(SonNumber(CS_INTEGER)) match {
          case SonNumber(_, data) => data.asInstanceOf[Int]
        }
        applyFunction(injFunction, value0) match {
          case value: Int => codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value))
        }

      case D_LONG =>
        val value0 = codec.readToken(SonNumber(CS_LONG)) match {
          case SonNumber(_, data) => data.asInstanceOf[Long]
        }
        applyFunction(injFunction, value0) match {
          case value: Long => codec.writeToken(currentResCodec, SonNumber(CS_LONG, value))
        }
    }
  }

  /**
    *
    * @param codec        - Structure from which we are reading the values
    * @param dataType     - Type of the value found and processing
    * @param injFunction  - Function given by the user with the new value
    * @param codecRes     - Structure that contains the information already processed and where we write the values
    * @param codecResCopy - Auxiliary structure to where we write the values in case the previous cycle was the last one
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made and an Auxiliary Codec
    */
  private def modifierEnd[T](codec: Codec, dataType: Int, injFunction: T => T, codecRes: Codec, codecResCopy: Codec): (Codec, Codec) = dataType match {

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      val token = codec.readToken(SonString(CS_STRING))
      val value0 = token match {
        case SonString(_, data) => data.asInstanceOf[String]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case str: String => codec.writeToken(codecRes, SonString(CS_STRING, str))
      }
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_BSONOBJECT =>
      val token = codec.readToken(SonObject(CS_OBJECT))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      token match {
        case SonObject(_, data) => data match {
          case byteBuf: ByteBuf =>
            val resCodec = applyFunction(injFunction, byteBuf.array()) match {
              case arr: Array[Byte] => codec.writeToken(codecRes, SonArray(CS_ARRAY, arr))
            }
            (resCodec, resCodecCopy)

          case str: String =>
            val resCodec = applyFunction(injFunction, str) match {
              case resString: String => codec.writeToken(codecRes, SonString(CS_STRING, resString))
            }
            (resCodec, resCodecCopy)
        }
      }

    case D_BSONARRAY =>
      val token = codec.readToken(SonArray(CS_ARRAY))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      token match {
        case SonArray(_, data) => data match {
          case byteArr: Array[Byte] =>
            val resCodec = applyFunction(injFunction, byteArr) match {
              case resByteArr: Array[Byte] => codec.writeToken(codecRes, SonArray(CS_ARRAY, resByteArr))
            }
            (resCodec, resCodecCopy)

          case jsonString: String =>
            val resCodec = applyFunction(injFunction, jsonString) match {
              case resString: String => codec.writeToken(codecRes, SonString(CS_STRING, resString))
            }
            (resCodec, resCodecCopy)
        }
      }

    case D_BOOLEAN =>
      val token = codec.readToken(SonBoolean(CS_BOOLEAN))
      val value0: Boolean = token match {
        case SonBoolean(_, data) => data.asInstanceOf[Boolean]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case tkn: Boolean => codec.writeToken(codecRes, SonBoolean(CS_BOOLEAN, tkn))
      }
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_FLOAT_DOUBLE =>
      val token = codec.readToken(SonNumber(CS_DOUBLE))
      val value0: Double = token match {
        case SonNumber(_, data) => data.asInstanceOf[Double]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case tkn: Double => codec.writeToken(codecRes, SonNumber(CS_DOUBLE, tkn))
      }
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_INT =>
      val token = codec.readToken(SonNumber(CS_INTEGER))
      val value0: Int = token match {
        case SonNumber(_, data) => data.asInstanceOf[Int]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case tkn: Int => codec.writeToken(codecRes, SonNumber(CS_INTEGER, tkn))
      }
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_LONG =>

      val token = codec.readToken(SonNumber(CS_LONG))
      val value0: Long = token match {
        case SonNumber(_, data) => data.asInstanceOf[Long]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case tkn: Long => codec.writeToken(codecRes, SonNumber(CS_LONG, tkn))
      }
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_NULL => throw new Exception() //TODO Implement


  }

  /**
    *
    * @param statementsList  - A list with pairs that contains the key of interest and the type of operation
    * @param seqType         - Type of the value found and processing
    * @param codec           - Structure from which we are reading the values
    * @param currentResCodec - Structure that contains the information already processed and where we write the values
    * @param fieldID         - name of the field we are searching
    * @param injFunction     - Function given by the user with the new value
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def processTypesAll[T](statementsList: StatementsList, seqType: Int, codec: Codec, currentResCodec: Codec, fieldID: String, injFunction: T => T): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        codec.writeToken(currentResCodec, codec.readToken(SonString(CS_STRING)))
      case D_BSONOBJECT =>
        val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT)) match {
          case SonObject(_, result) => result match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        currentResCodec + codecAux
      case D_BSONARRAY =>
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY)) match {
          case SonArray(_, result) => result match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        currentResCodec + codecAux
      case D_NULL => throw new Exception
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
      case D_BOOLEAN =>
        codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
    }
  }

  /**
    * Verifies if Key given by user is HalfWord and if it matches with the one extracted.
    *
    * @param fieldID   - Key given by User.
    * @param extracted - Key extracted.
    * @return A boolean that is true if it's a HalWord or false or if it's not
    */
  private def isHalfword(fieldID: String, extracted: String): Boolean = {
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

  /**
    * Method that tries to apply the given injector function to a given value
    *
    * @param injFunction - The injector function to be applied
    * @param value       - The value to apply the injector function to
    * @tparam T - The type of the value
    * @return A modified value in which the injector function was applied
    */
  private def applyFunction[T](injFunction: T => T, value: Any): T = {

    Try(injFunction(value.asInstanceOf[T])) match {
      case Success(modifiedValue) =>
        modifiedValue

      case Failure(_) => value match {
        case double: Double =>
          Try(injFunction(double.toFloat.asInstanceOf[T])) match { //try with the value being a Double
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

            case Failure(_) => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
          }

        case byteArr: Array[Byte] =>
          Try(injFunction(new String(byteArr).asInstanceOf[T])) match { //try with the value being a Array[Byte]
            case Success(modifiedValue) => //TODO should we always try to transform the byte arr to a string ? our injFunction could receive a Array[Byte] as input/output?
              modifiedValue.asInstanceOf[T]

            case Failure(_) =>
              Try(injFunction(Instant.parse(new String(byteArr)).asInstanceOf[T])) match { //try with the value being an Instant
                case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

                case Failure(_) => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
              }
          }
      }

      case _ => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
    }
  }

  /**
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the values
    * @param currentCodec   - Structure that contains the information already processed and where we write the values
    * @param injFunction    - Function given by the user to alter specific values
    * @param key            - Name of value to be used in search (can be empty)
    * @param left           - Left argument of the array conditions
    * @param mid            - Middle argument of the array conditions
    * @param right          - Right argument of the array conditions
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def arrayInjection[T](statementsList: StatementsList, codec: Codec, currentCodec: Codec, injFunction: T => T, key: String, left: Int, mid: String, right: Any): Codec = {
    val arrayTokenCodec = codec.readToken(SonArray(CS_ARRAY)) match {
      case SonArray(_, data) => data match {
        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
        case jsonString: String => CodecObject.toCodec(jsonString)
      }
    }
    val codecArrayEnd: Codec = (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, to.toString)
      case (EMPTY_KEY, from, expr, _) =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString)
      case (nonEmptyKey, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString, to.toString)
      case (nonEmptyKey, from, expr, _) =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString)
    }
    currentCodec + codecArrayEnd
  }

  /**
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the values
    * @param injFunction    - Function given by the user to alter specific values
    * @param condition      - Represents a type of injection, it can me END, ALL, FIRST, # TO #, # UNTIL #
    * @param from           - Represent the inferior limit of a given range
    * @param to             - Represent the superior limit of a given range
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifyArrayEnd[T](statementsList: StatementsList, codec: Codec, injFunction: T => T, condition: String, from: String, to: String = C_END): Codec = {
    val startReaderIndex = codec.getReaderIndex
    val originalSize = codec.readSize

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec     - The codec to be modified
      * @param currentCodecCopy - An Auxiliary codec to where we write the values in case the previous cycle was the last one
      * @param exceptions       - An Int that represents how many exceptions have occurred
      * @return a modified codec pair and the amount of axceptions
      */
    def iterateDataStructure(currentCodec: Codec, currentCodecCopy: Codec, exceptions: Int): (Codec, Codec, Int) = {
      if ((codec.getReaderIndex - startReaderIndex) >= originalSize && exceptions < 2)
        (currentCodec, currentCodecCopy, exceptions)
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte)) //Verify
        val codecWithDataTypeCopy = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte))
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType, codecWithDataTypeCopy, exceptions)
          case _ =>
            val (isArray, key, byte): (Boolean, String, Int) = {
              val key: String = codec.readToken(SonString(CS_NAME)) match {
                case SonString(_, keyString) => keyString.asInstanceOf[String]
              }
              val byte: Byte = codec.readToken(SonBoolean(C_ZERO)) match {
                case SonBoolean(_, result) => result.asInstanceOf[Byte]
              }
              (key.forall(b => b.isDigit), key, byte)
            }
            val codecWithKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key))
            val codecWithKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key))

            val ((codecResult, codecResultCopy), exceptionsResult): ((Codec, Codec), Int) = (new String(key), condition, to) match {
              case (x, C_END, _) if isArray =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val partialData: Either[ByteBuf, String] = codec.readToken(SonArray(CS_ARRAY)) match {
                          case SonArray(_, value) => value match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case jsonString: String => Right(jsonString)
                          }
                        }

                        val partialCodec = {
                          if (statementsList.head._1.isInstanceOf[ArrExpr])
                            inject(partialData, statementsList, injFunction)
                          else {
                            partialData match {
                              case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                              case Right(jsonString) => CodecObject.toCodec(jsonString)
                            }
                          }
                        }

                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(partialCodec, dataType, injFunction, codecWithKey, codecWithKeyCopy)) match {
                            case Success(tuple) =>
                              (tuple, 0)
                            case Failure(e) =>
                              ((codecWithKey + partialCodec, codecWithKeyCopy + partialCodec), 1)
                          }
                        (codecTuple, exceptionsReturn)

                      case _ =>
                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKeyCopy)) match {
                            case Success(tuple) => (tuple, 0)
                            case Failure(e) => ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        (codecTuple, exceptionsReturn)
                    }
                  } else {
                    val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKeyCopy)) match {
                        case Success(tuple) => (tuple, 0)
                        case Failure(e) => ((codecWithKey, codecWithKeyCopy), 1)
                      }
                    (codecTuple, exceptionsReturn)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialData: Either[ByteBuf, String] = codec.readToken(SonArray(CS_ARRAY)) match {
                          case SonArray(_, value) => value.asInstanceOf[Either[ByteBuf, String]]
                        }

                        val codecData =
                          if (statementsList.head._1.isInstanceOf[ArrExpr])
                            inject(partialData, statementsList, injFunction).getCodecData
                          else
                            partialData

                        val partialCodec = partialData match {
                          case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                          case Right(jsonString) => CodecObject.toCodec(jsonString)
                        }

                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) = Try(inject(codecData, statementsList.drop(1), injFunction)) match {
                          case Success(successCodec) => ((currentCodec + successCodec, currentCodec + partialCodec), exceptions)
                          case Failure(e) => ((currentCodec + partialCodec, currentCodec + partialCodec), exceptions + 1)
                        }
                        (codecTuple, exceptionsReturn)
                      case _ =>
                        val codecTuple = processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy)
                        (codecTuple, exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val newCodec: Codec = codecWithKeyCopy.duplicate
                        Try(inject(codec.getCodecData, statementsList.drop(1), injFunction)) match {
                          case Success(c) => ((c, processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                          case Failure(e) => ((processTypesArray(dataType, codec.duplicate, newCodec), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                        }
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                    }
                  }
                }
              case (x, _, C_END) if isArray && from.toInt <= key.toInt =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val emptyCodec: Codec = createEmptyCodec(codec)
                          val newCodecCopy = codecWithKey.duplicate
                          val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                          val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                            Try(modifierEnd(modifiedPartialCodec, dataType, injFunction, emptyCodec, emptyCodec.duplicate)) match {
                              case Success(tuple) =>
                                ((codecWithKey + tuple._1, newCodecCopy + tuple._2), exceptions)
                              case Failure(e) =>
                                ((codecWithKey + partialCodec, newCodecCopy + partialCodec), exceptions + 1)
                            }
                          (codecTuple, exceptionsReturn)
                        } else
                          ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, newCodecCopy)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(e) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      val newCodecCopy = codecWithKey.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKey, newCodecCopy)) match {
                        case Success(tuple) =>
                          (tuple, exceptions)
                        case Failure(e) =>
                          ((codecWithKey, newCodecCopy), exceptions + 1)
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                          Try(inject(modifiedPartialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((codecWithKey + c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(e) =>
                              ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, newCodecCopy)), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        val codecTuple = processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy)
                        (codecTuple, exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(inject(codec.duplicate.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(e) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }
              case (x, _, C_END) if isArray && from.toInt > key.toInt =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                      val newCodecResult = codecWithKey + modifiedPartialCodec
                      val newCodecResultCopy = codecWithKeyCopy + modifiedPartialCodec
                      ((newCodecResult, newCodecResultCopy), exceptions)
                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else {
                  ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                }

              case (x, _, l) if isArray && (from.toInt <= x.toInt && l.toInt >= x.toInt) =>
                if (statementsList.lengthCompare(1) == 0) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val emptyCodec: Codec = createEmptyCodec(codec)
                          val newCodecCopy = codecWithKey.duplicate
                          val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                          Try(modifierEnd(modifiedPartialCodec, dataType, injFunction, emptyCodec, emptyCodec.duplicate)) match {
                            case Success(tuple) =>
                              ((codecWithKey + tuple._1, newCodecCopy + tuple._2), exceptions)
                            case Failure(e) =>
                              ((codecWithKey + partialCodec, newCodecCopy + partialCodec), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, newCodecCopy)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(e) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      val newCodecCopy = codecWithKey.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKey, newCodecCopy)) match {
                        case Success(tuple) =>
                          (tuple, exceptions)
                        case Failure(e) =>
                          ((codecWithKey, newCodecCopy), exceptions + 1)
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                          Try(inject(modifiedPartialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((codecWithKey + c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(e) =>
                              ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, newCodecCopy)), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        val codecTuple = processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy)
                        (codecTuple, exceptions)

                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(inject(codec.duplicate.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(e) =>
                              ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, newCodecCopy)), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }
              case (x, _, l) if isArray && (from.toInt > x.toInt || l.toInt < x.toInt) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                      ((codecWithKey + modifiedPartialCodec, codecWithKeyCopy + modifiedPartialCodec), exceptions)

                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                  }
                } else {
                  ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                }
              case (x, _, l) if !isArray =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = inject(partialCodec.getCodecData, statementsList, injFunction)
                      ((codecWithKey + modifiedPartialCodec, codecWithKeyCopy + modifiedPartialCodec), exceptions)
                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                  }
                } else
                  throw new Exception
            }
            iterateDataStructure(codecResult, codecResultCopy, exceptionsResult)
        }
      }
    }

    val emptyCodec: Codec = createEmptyCodec(codec)

    val (codecWithoutSize, codecWithoutSizeCopy, exceptions): (Codec, Codec, Int) = iterateDataStructure(emptyCodec, emptyCodec.duplicate, 0)

    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSize.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace

    val codecFinal = codecWithoutSize + codecWithoutSize

    val finalSizeCopy = codecWithoutSizeCopy.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSizeCopy.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSizeCopy.removeEmptySpace

    val codecFinalCopy = codecWithSizeCopy + codecWithoutSizeCopy

    condition match {
      case TO_RANGE =>
        if (exceptions == 0)
          codecFinal
        else
          throw new Exception
      case UNTIL_RANGE =>
        if (exceptions <= 1)
          codecFinalCopy
        else
          throw new Exception
      case _ =>
        if (exceptions == 0)
          codecFinal
        else
          throw new Exception
    }
  }

  /**
    *
    * @param statementList   - A list with pairs that contains the key of interest and the type of operation
    * @param fieldID         - Name of the field of interest
    * @param dataType        - Type of the value found and processing
    * @param codec           - Structure from which we are reading the values
    * @param injFunction     - Function given by the user with the new value
    * @param condition       - Represents a type of injection, it can me END, ALL, FIRST, # TO #, # UNTIL #
    * @param from            - Represent the inferior limit when a range is given
    * @param to              - Represent the superior limit when a range is given
    * @param resultCodec     - Structure that contains the information already processed and where we write the values
    * @param resultCodecCopy - Auxiliary structure to where we write the values in case the previous cycle was the last one
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made and an Auxiliary Codec
    */
  private def processTypesArrayEnd[T](statementList: List[(Statement, String)], fieldID: String, dataType: Int, codec: Codec, injFunction: T => T, condition: String, from: String = C_ZERO, to: String = C_END, resultCodec: Codec, resultCodecCopy: Codec): (Codec, Codec) = {
    dataType match {

      case D_FLOAT_DOUBLE =>
        val token = codec.readToken(SonNumber(CS_DOUBLE))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val token = codec.readToken(SonString(CS_STRING))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_BSONOBJECT =>
        val codecObj = codec.readToken(SonObject(CS_OBJECT)) match {
          case SonObject(_, data) => data match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val auxCodec = modifyArrayEndWithKey(statementList, codecObj, fieldID, injFunction, condition, from, to)
        (resultCodec + auxCodec, resultCodecCopy + auxCodec)
      case D_BSONARRAY =>
        //        val res: BosonImpl = modifyArrayEndWithKey(list, buf, fieldID, f, condition, limitInf, limitSup)
        val codecArr = codec.readToken(SonArray(CS_ARRAY)) match {
          case SonArray(_, data) => data match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val auxCodec = modifyArrayEndWithKey(statementList, codecArr, fieldID, injFunction, condition, from, to)
        (resultCodec + auxCodec, resultCodecCopy + auxCodec)

      case D_NULL => (resultCodec, resultCodecCopy)

      case D_INT =>
        val token = codec.readToken(SonNumber(CS_INTEGER))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_LONG =>
        val token = codec.readToken(SonNumber(CS_LONG))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_BOOLEAN =>
        val token = codec.readToken(SonBoolean(CS_BOOLEAN))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))
    }
  }

  /**
    * Function used to search for the last element of an array that corresponds to field with name fieldID
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the values
    * @param fieldID        - Name of the field of interest
    * @param injFunction    - Function given by the user with the new value
    * @param condition      - Represents a type of injection, it can me END, ALL, FIRST, # TO #, # UNTIL #
    * @param from           - Represent the inferior limit when a range is given
    * @param to             - Represent the superior limit when a range is given
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifyArrayEndWithKey[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T, condition: String, from: String, to: String = C_END): Codec = {
    val startReaderIndex = codec.getReaderIndex
    val originalSize = codec.readSize

    val emptyDataStructure: Codec = createEmptyCodec(codec)

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec     - The codec to be modified
      * @param currentCodecCopy - An Auxiliary codec to where we write the values in case the previous cycle was the last one
      * @return a modified codec
      */
    //TODO - Check to see if it's necessary to pass statementsList
    def iterateDataStructure(currentCodec: Codec, currentCodecCopy: Codec /*, currentStatementList: StatementsList*/): (Codec, Codec) = {
      if ((codec.getReaderIndex - startReaderIndex) >= originalSize)
        (currentCodec, currentCodecCopy)
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType: Codec = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte))
        val codecWithDataTypeCopy: Codec = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte))
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType, codecWithDataTypeCopy)
          case _ =>
            val (key, byte): (String, Byte) = {
              val key: String = codec.readToken(SonString(CS_NAME)) match {
                case SonString(_, keyString) => keyString.asInstanceOf[String]
              }
              val token = codec.readToken(SonBoolean(C_ZERO))
              val byte: Byte = token match {
                case SonBoolean(_, byteBooelan) => byteBooelan.asInstanceOf[Byte]
              }
              (key, byte)
            }
            val modResultCodec = codec.writeToken(codecWithDataType, SonString(CS_STRING, key))
            val modResultCodecCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key))

            val resCodec = codec.writeToken(modResultCodec, SonString(CS_STRING, byte))
            val resCodecCopy = codec.writeToken(modResultCodecCopy, SonString(CS_STRING, byte))
            key match {
              //In case we the extracted elem name is the same as the one we're looking for (or they're halfwords) and the
              //datatype is a BsonArray
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType == D_BSONARRAY =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                      case SonArray(_, result) => result match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case jsonString: String => CodecObject.toCodec(jsonString)
                      }
                    }
                    val newCodec = modifyArrayEnd(statementsList, partialCodec, injFunction, condition, from, to)
                    iterateDataStructure(resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  } else {
                    val newCodec: Codec = modifyArrayEnd(statementsList, codec, injFunction, condition, from, to)
                    iterateDataStructure(resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                      case SonArray(_, result) => result match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case jsonString: String => CodecObject.toCodec(jsonString)
                      }
                    }
                    //TODO - Check
                    val newCodec = modifyArrayEnd(statementsList.drop(1), partialCodec, injFunction, condition, from, to)
                    iterateDataStructure(resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  } else {
                    //TODO - Check
                    val newCodec = modifyArrayEnd(statementsList.drop(1), codec, injFunction, condition, from, to)
                    iterateDataStructure(resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  }

                }
              //If we found the desired elem but the dataType is not an array, or if we didn't find the desired elem
              case _ =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  val (processedCodec, processedCodecCopy): (Codec, Codec) = processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                  iterateDataStructure(processedCodec, processedCodecCopy)
                } else {
                  val codecIterate = processTypesArray(dataType, codec.duplicate, resCodec)
                  val codecIterateCopy = processTypesArray(dataType, codec, resCodecCopy)
                  iterateDataStructure(codecIterate, codecIterateCopy)
                }
            }
        }
      }
      iterateDataStructure(emptyDataStructure, emptyDataStructure.duplicate)
    }

    val emptyCodec = createEmptyCodec(codec)

    val (codecWithoutSize, codecWithoutSizeCopy): (Codec, Codec) = iterateDataStructure(emptyCodec, emptyCodec.duplicate)

    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val finalSizeCopy = codecWithoutSizeCopy.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSize.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSizeCopy.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSizeCopy.removeEmptySpace

    val finalCodec = codecWithSize + codecWithoutSize
    val finalCodecCopy = codecWithSizeCopy + codecWithoutSizeCopy

    condition match {
      case TO_RANGE =>
        finalCodec
      case UNTIL_RANGE =>
        finalCodecCopy
      case _ =>
        finalCodec
    }
  }

  /**
    * Helper function to retrieve a codec with the key information written in it , and the key that was written
    *
    * @param codec         - Structure from which we are reading the values
    * @param writableCodec - Structure that contains the information already processed and where we write the values
    * @return
    */
  private def writeKeyAndByte(codec: Codec, writableCodec: Codec): (Codec, String) = {
    val key: String = codec.readToken(SonString(CS_NAME)) match {
      case SonString(_, keyString) => keyString.asInstanceOf[String]
    }
    val b: Byte = codec.readToken(SonBoolean(C_ZERO)) match { //TODO FOR CodecJSON we cant read a boolean, we need to read an empty string
      case SonBoolean(_, result) => result.asInstanceOf[Byte]
    }

    val codecWithKey = codec.writeToken(writableCodec, SonString(CS_STRING, key))
    (codec.writeToken(codecWithKey, SonNumber(CS_BYTE, b)), key)
  }

  /**
    * Method that creates a Codec with an empty data structure inside it.
    *
    * For CodecBson it creates a ByteBuf with capacity 256.
    * For CodecJson it creates an empty String
    *
    * @param inputCodec - a codec in order to determine which codec to create
    * @return a Codec with an empty data structure inside it
    */
  private def createEmptyCodec(inputCodec: Codec): Codec = {
    val emptyCodec = inputCodec.getCodecData match {
      case Left(_) => CodecObject.toCodec(Unpooled.buffer()) //Creates a CodecBson with an empty ByteBuf with capacity 256
      case Right(_) => CodecObject.toCodec("") //Creates a CodecJson with an empty String
    }
    emptyCodec.setWriterIndex(0) //Sets the writerIndex of the newly created codec to 0 (Initialy it starts at 256 for CodecBson, so we need o reset it)
    emptyCodec
  }
}
