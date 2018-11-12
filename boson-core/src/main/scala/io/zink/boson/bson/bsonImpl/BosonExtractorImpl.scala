package io.zink.boson.bson.bsonImpl

import io.zink.boson.bson.bsonImpl.BosonInjectorImpl.isHalfWord
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._

private[bsonImpl] object BosonExtractorImpl {


  /**
    * Set of conditions to extract primitive types while traversing an Object.
    */
  val eObjPrimitiveConditions: List[(String, String)] => Boolean =
    keyList => {
      lazy val headLimit = keyList.head._2
      !headLimit.equals(C_LIMIT) && !headLimit.equals(C_NEXT) && !headLimit.equals(C_ALLNEXT) && !headLimit.equals(C_LIMITLEVEL)
    }

  /**
    * Set of conditions to extract an Object while traversing an Object.
    */
  val eObjConditions: List[(String, String)] => Boolean =
    keyList => {
      !keyList.head._2.equals(C_LIMIT) && !keyList.head._2.equals(C_LIMITLEVEL)
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
  def extractFromBsonObj[T](codec: Codec, keyList: List[(String, String)], bsonFinishReaderIndex: Int, limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    val seqTypeCodec: Int = codec.readDataType()
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
                //Trick: This function does nothing in CodecBson but in CodecJson advances the readerIndex by a unit
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
  def extractFromBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {
    keyList.head._1 match {
      case EMPTY_KEY | STAR =>
        traverseBsonArray(codec, length, arrayFRIdx, keyList, limitList)
      case _ if keyList.head._2.equals(C_LEVEL) || keyList.head._2.equals(C_NEXT) => Nil
      case _ if keyList.head._2.equals(C_LIMITLEVEL) && !keyList.head._1.equals(EMPTY_KEY) => Nil

      case _ =>
        val seqType2: Int = codec.readDataType()
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
    * Function that compares 2 keys to check if they match
    *
    * @param codec Abstraction of Encoded Document.
    * @param key   given by user.
    * @return Tuple of Boolean and String, Boolean representing if keys match, String is the key.
    */
  def compareKeys(codec: Codec, key: String): (Boolean, String) = {
    val key0 = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].info.asInstanceOf[String]
    (key.toCharArray.deep == key0.toCharArray.deep | isHalfWord(key, key0), key0)
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
  def traverseBsonArray[T](codec: Codec, length: Int, arrayFRIdx: Int, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

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

      val seqTypeCodec: Int = codec.readDataType()
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
  def findElements(codec: Codec, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)], start: Int, finish: Int): List[Any] = {
    val seqType: Int = codec.readDataType()
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
}
