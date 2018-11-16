package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import BosonImpl.{DataStructure, StatementsList}
import com.sun.beans.decoder.ValueObject
import io.zink.boson.bson.value.{Value, ValueObject}
import io.zink.bsonLib.{BsonArray, BsonObject}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/*private[bsonImpl]*/ object BosonInjectorImpl {

  private type TupleList = List[(String, Any)]
  implicit lazy val emptyBuff: ByteBuf = Unpooled.buffer()
  emptyBuff.writerIndex(0)


  /**
    * Function that recursively searches for the keys that are of interest to the injection
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param fieldID        - Name of the field of interest
    * @param injFunction    - The injection function to be applied
    * @tparam T - The type of input and output of the injection function
    * @return A Codec containing the alterations made
    */
  def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {

    val (startReader: Int, originalSize: Int) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReader) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)
      dataType match {
        case 0 =>

        case _ =>
          val rk = codec.canReadKey()
          val (_, key) = if (rk) writeKeyAndByte(codec, currentCodec) else (currentCodec, "")

          key match {
            case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(fieldID, extracted) =>
              if (statementsList.lengthCompare(1) == 0) {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.getPartialCodec(3)
                      val subCodec = modifyAll(statementsList, partialCodec, key, injFunction)
                      modifierAll(subCodec, currentCodec, dataType, injFunction)

                    case _ => modifierAll(codec, currentCodec, dataType, injFunction)
                  }
                } else modifierAll(codec, currentCodec, dataType, injFunction)

              } else {
                dataType match {
                  case D_BSONOBJECT | D_BSONARRAY =>
                    val partialCodec = codec.getPartialCodec(3)
                    val modCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                    val resCodec = if (statementsList.head._2.contains(C_DOUBLEDOT))
                      BosonImpl.inject(modCodec.getCodecData, statementsList, injFunction)
                    else modCodec
                    currentCodec + resCodec

                  case _ =>
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                }
              }
            case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfWord(fieldID, x) =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) /*&& (dataType == D_BSONARRAY || dataType == D_BSONOBJECT)*/ ) {
                codec.getCodecData match {
                  case Right(jsonString) =>
                    if (jsonString.charAt(0).equals('[')) {
                      if (codec.getReaderIndex != 1) {
                        val partialCodec = codec.getPartialCodec(dataType)
                        val subCodec = modifyAll(statementsList, partialCodec, fieldID, injFunction)
                        currentCodec + subCodec
                      } else {
                        codec.setReaderIndex(0)
                        val processValue = codec.readToken2(4)
                        processValue.write(currentCodec)
                      }
                    } else {
                      if (dataType == D_BSONARRAY || dataType == D_BSONOBJECT) {
                        val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(4).removeBrackets else codec.getPartialCodec(3)
                        currentCodec + modifyAll(statementsList, partialCodec, fieldID, injFunction)
                      } else {
                        val value0 = codec.readToken2(dataType)
                        value0.write(currentCodec)
                      }
                    }
                  case Left(_) =>
                    if (dataType == D_BSONARRAY || dataType == D_BSONOBJECT) {
                      val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(4).removeBrackets else codec.getPartialCodec(3)
                      currentCodec + modifyAll(statementsList, partialCodec, fieldID, injFunction)
                    } else {
                      val value0 = codec.readToken2(dataType)
                      value0.write(currentCodec)
                    }
                }
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }
          }
      }
    }
    currentCodec.writeCodecSize.removeTrailingComma(codec, checkOpenRect = true)
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
  def modifyHasElem[T](statementsList: StatementsList, codec: Codec, fieldID: String, elem: String, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val (startReader: Int, originalSize: Int) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReader) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)
      dataType match {
        case 0 => //Nothing
        case _ =>
          val (_, key) = if (codec.canReadKey()) writeKeyAndByte(codec, currentCodec) else (currentCodec, "")

          //We only want to modify if the dataType is an Array and if the extractedKey matches with the fieldID
          //or they're halfword's
          //in all other cases we just want to copy the data from one codec to the other (using "process" like functions)
          key match {
            case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(fieldID, extracted)) && dataType == D_BSONARRAY =>
              //the key is a halfword and matches with the extracted key, dataType is an array
              //So we will look for the "elem" of interest inside the current object

              dataType match {
                case D_BSONARRAY =>
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newStatementList: StatementsList = (Key(elem), ".") :: statementsList.tail
                  val modified = modifyArrayEnd(newStatementList, partialCodec, injFunction, TO_RANGE, "0", C_END, statementsList, dataType)
                  currentCodec + modified

                case _ =>
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
              }

            case _ =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && dataType == 3) {
                val partialCodec = codec.getPartialCodec(dataType)
                val y = if (partialCodec.wrappable) partialCodec.wrapInBrackets(key = fieldID) else partialCodec
                val modifiedCodec: Codec = modifyHasElem(statementsList, y, fieldID, elem, injFunction)
                currentCodec + modifiedCodec
              }
              else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }
          }
      }
    }

    currentCodec.writeCodecSize.removeTrailingComma(codec, checkOpenRect = true)
  }

  /**
    * Method that will perform the injection in the root of the data structure
    *
    * @param codec       - Codec encapsulating the data structure to inject in
    * @param injFunction - The injection function to be applied
    * @tparam T - The type of elements the injection function receives
    * @return - A new codec with the injFunction applied to it
    */
  def rootInjection[T](codec: Codec, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec =
    codec.getCodecData match {
      case Left(byteBuf) =>

        val modifiedBytes: Array[Byte] = applyFunction(injFunction, byteBuf.array).asInstanceOf[Array[Byte]]
        CodecObject.toCodec(Unpooled.copiedBuffer(modifiedBytes))

      case Right(jsonString) =>
        val modifiedString: String = applyFunction(injFunction, jsonString).asInstanceOf[String]
        CodecObject.toCodec(modifiedString)
    }

  /**
    * Function used to perform the injection of the new values
    *
    * @param codec           - Structure from which we are reading the values
    * @param currentResCodec - Structure that contains the information already processed and where we write the values
    * @param seqType         - Type of the value found and processing
    * @param injFunction     - Function given by the user with the new value
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifierAll[T](codec: Codec, currentResCodec: Codec, seqType: Int, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Unit = {
    //    val processValue = codec.readToken2(seqType)
    //    val modValue = processValue.applyFunction(injFunction)
    //    modValue.write(currentResCodec)

    seqType match {
      case D_FLOAT_DOUBLE =>
        val processValue = codec.readToken2(seqType)
        val modValue = processValue.applyFunction(injFunction)
        modValue.write(currentResCodec)
//        val value0 = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info.asInstanceOf[Double]
//        currentResCodec.writeToken(SonNumber(CS_DOUBLE, applyFunction(injFunction, value0).asInstanceOf[Double]))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ => //TODO - Java Instant
        //        val processValue = codec.readToken2(seqType)
        //        val modValue = processValue.applyFunction(injFunction)
        //        modValue.write(currentResCodec)
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        applyFunction(injFunction, value0) match {
          case valueAny: Any =>
            val value = valueAny.toString
            currentResCodec.writeToken(SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
            currentResCodec.writeToken(SonString(CS_STRING, value))
            currentResCodec.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        }

      case D_BSONOBJECT =>
        codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
          case byteBuf: ByteBuf => currentResCodec.writeToken(SonArray(CS_ARRAY, applyFunction(injFunction, byteBuf.array()).asInstanceOf[Array[Byte]]))
          case str: String => currentResCodec.writeToken(SonString(CS_STRING_NO_QUOTES, applyFunction(injFunction, str).asInstanceOf[String]))
        }

      case D_BSONARRAY =>
        codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
          case byteBuf: ByteBuf => currentResCodec.writeToken(SonArray(CS_ARRAY, applyFunction(injFunction, byteBuf.array()).asInstanceOf[Array[Byte]]))

          case str: String => currentResCodec.writeToken(SonString(CS_STRING_NO_QUOTES, applyFunction(injFunction, str).asInstanceOf[String]))
        }

      case D_BOOLEAN =>
        val processValue = codec.readToken2(seqType)
        val modValue = processValue.applyFunction(injFunction)
        modValue.write(currentResCodec)
      //        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
      //          case byte: Byte => byte == 1
      //        }
      //        currentResCodec.writeToken(SonBoolean(CS_BOOLEAN, applyFunction(injFunction, value0).asInstanceOf[Boolean]))

      case D_NULL => throw CustomException(s"NULL field. Can not be changed")

      case D_INT =>
        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info.asInstanceOf[Int]
        currentResCodec.writeToken(SonNumber(CS_INTEGER, applyFunction(injFunction, value0).asInstanceOf[Int]))

      case D_LONG =>
        val value0 = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info.asInstanceOf[Long]
        currentResCodec.writeToken(SonNumber(CS_LONG, applyFunction(injFunction, value0).asInstanceOf[Long]))
    }
  }

  /**
    * Function used to perform the injection on the last ocurrence of a field
    *
    * @param codec        - Structure from which we are reading the values
    * @param dataType     - Type of the value found and processing
    * @param injFunction  - Function given by the user with the new value
    * @param codecRes     - Structure that contains the information already processed and where we write the values
    * @param codecResCopy - Auxiliary structure to where we write the values in case the previous cycle was the last one
    * @tparam T - Type of the value being injected
    * @return A Codec tuple containing the alterations made and an Auxiliary Codec
    */
  private def modifierEnd[T](codec: Codec, dataType: Int, injFunction: T => T, codecRes: Codec, codecResCopy: Codec)(implicit convertFunction: Option[TupleList => T] = None): Unit = {
    val processValue = codec.readToken2(dataType)
    val modValue = processValue.applyFunction(injFunction)
    modValue.write(codecRes)
    processValue.write(codecResCopy)
  }

  /**
    * Verifies if Key given by user is a HalfWord and if it matches with the one extracted.
    *
    * @param fieldID   - Key given by User.
    * @param extracted - Key extracted.
    * @return A boolean that is true if it's a HalWord or false or if it's not
    */
  def isHalfWord(fieldID: String, extracted: String): Boolean = {
    if (fieldID.contains(STAR) & extracted.nonEmpty) {
      val list: Array[String] = fieldID.split(STAR_CHAR)
      (extracted, list.length) match {
        case (_, 0) => true

        case (x, 1) if x.startsWith(list.head) => true

        case (x, 2) if x.startsWith(list.head) & x.endsWith(list.last) => true

        case (x, i) if i > 2 =>
          fieldID match {
            case s if s.startsWith(STAR) =>
              if (x.startsWith(list.apply(1)))
                isHalfWord(s.substring(1 + list.apply(1).length), x.substring(list.apply(1).length))
              else
                isHalfWord(s, x.substring(1))

            case s if !s.startsWith(STAR) =>
              if (x.startsWith(list.head)) isHalfWord(s.substring(list.head.length), extracted.substring(list.head.length))
              else false
          }
        case _ => false
      }
    } else false
  }


  /**
    * Method that tries to apply the given injector function to a given value
    *
    * @param injFunction - The injector function to be applied
    * @param value       - The value to apply the injector function to
    * @tparam T - The type of the value
    * @return A modified value in which the injector function was applied
    */
  private def applyFunction[T](injFunction: T => T, value: Any)(implicit convertFunction: Option[TupleList => T] = None): T = {

    def throwException(className: String): T = throw CustomException(s"Type Error. Cannot Cast $className inside the Injector Function.")

    Try(injFunction(value.asInstanceOf[T])) match {
      case Success(modifiedValue) =>
        modifiedValue

      case Failure(_) => value match {
        case double: Double =>
          Try(injFunction(double.toFloat.asInstanceOf[T])) match {
            case Success(modValue) => modValue
            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }

        case byteArrOrJson if convertFunction.isDefined => //In case T is a case class and value is a byte array encoding that object of type T

          val extractedTuples: TupleList = byteArrOrJson match {
            case byteArray: Array[Byte] => extractTupleList(Left(byteArray))
            case jsonString: String => extractTupleList(Right(jsonString))
          }

          val convertFunct = convertFunction.get
          val convertedValue = convertFunct(extractedTuples)
          Try(injFunction(convertedValue)) match {
            case Success(modValue) =>
              val modifiedTupleList = toTupleList(modValue)
              encodeTupleList(modifiedTupleList, byteArrOrJson) match {
                case Left(modByteArr) => modByteArr.asInstanceOf[T]
                case Right(modJsonString) => modJsonString.asInstanceOf[T]
              }
            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }

        case byteArr: Array[Byte] =>
          Try(injFunction(new String(byteArr).asInstanceOf[T])) match {
            case Success(modifiedValue) =>
              modifiedValue.asInstanceOf[T]

            case Failure(_) =>
              Try(injFunction(Instant.parse(new String(byteArr)).asInstanceOf[T])) match {
                case Success(modValue) => modValue
                case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
              }
          }

        case str: String =>
          Try(injFunction(Instant.parse(str).asInstanceOf[T])) match {
            case Success(modValue) => modValue
            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }
      }

      case _ => throwException(value.getClass.getSimpleName.toLowerCase)
    }
  }

  /**
    * Function that handles the type of injection into an Array and calls the modifiers accordingly
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
  def arrayInjection[T](statementsList: StatementsList, codec: Codec, currentCodec: Codec, injFunction: T => T, key: String, left: Int, mid: String, right: Any)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val (arrayTokenCodec, formerType): (Codec, Int) = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
      case SonArray(_, data) => data match {
        case byteBuf: ByteBuf => (CodecObject.toCodec(byteBuf), 0)
        case jsonString: String =>
          val auxType = codec.getCodecData.asInstanceOf[Right[ByteBuf, String]].value.charAt(0) match {
            case '[' => 4
            case '{' => 3
            case _ => codec.getDataType
          }
          (CodecObject.toCodec("{" + jsonString + "}"), auxType)
      }
    }
    (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, to.toString, fullStatementsList = statementsList, formerType = formerType)

      case (EMPTY_KEY, from, expr, _) =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, fullStatementsList = statementsList, formerType = formerType)

      case (_, from, expr, to) if to.isInstanceOf[Int] && formerType == 4 =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, to.toString, fullStatementsList = statementsList, formerType = formerType)

      case (_, from, expr, _) if formerType == 4 =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, fullStatementsList = statementsList, formerType = formerType)

      case (nonEmptyKey, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString, to.toString)

      case (nonEmptyKey, from, expr, _) =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString)
    }
  }

  /**
    * This function iterates through the all the positions of an array to find the relevant elements to be changed
    * in the injection
    *
    * @param statementsList     - A list with pairs that contains the key of interest and the type of operation
    * @param codec              - Structure from which we are reading the values
    * @param injFunction        - Function given by the user to alter specific values
    * @param condition          - Represents a type of injection, it can be END, ALL, FIRST, # TO #, # UNTIL #
    * @param from               - Represent the inferior limit of a given range
    * @param to                 - Represent the superior limit of a given range
    * @param fullStatementsList - The original statementsList passed in the first injection
    * @param formerType         - The former type of the data read before
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifyArrayEnd[T](statementsList: StatementsList, codec: Codec, injFunction: T => T, condition: String, from: String, to: String = C_END, fullStatementsList: StatementsList, formerType: Int)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val (startReaderIndex, originalSize) = (codec.getReaderIndex, codec.readSize)
    var counter: Int = -1

    val currentCodec = codec.createEmptyCodec
    val currentCodecCopy = codec.createEmptyCodec

    while ((codec.getReaderIndex - startReaderIndex) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec, formerType)
      currentCodecCopy.writeToken(SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
      dataType match {
        case 0 =>
        case _ =>

          val (key, b): (String, Byte) = codec.getCodecData match {
            case Left(_) => codec.readKey
            case Right(_) =>
              counter += 1
              (counter.toString, codec.readByte)
          }

          currentCodec.writeArrayKey(key, b)
          currentCodecCopy.writeArrayKey(key, b)

          val isArray = codec.isArray(formerType, key)

          (key, condition, to) match {
            case (_, C_END, _) if isArray =>
              if (statementsList.size == 1) {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {

                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.getPartialCodec(4)
                      val partialCodecModified = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      val subPartial = BosonImpl.inject(partialCodecModified.getCodecData, fullStatementsList, injFunction)

                      if (codec.getDataType == 0) {
                        codec.skipChar(back = true)
                        currentCodec + subPartial.addComma
                        currentCodecCopy + partialCodec
                      } else {
                        codec.skipChar(back = true)
                        currentCodec + partialCodec.addComma
                        currentCodecCopy + partialCodec
                      }
                    case _ =>
                      val newCodecCopy = currentCodecCopy.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, newCodecCopy, currentCodecCopy)) match {
                        case Success(_) => currentCodec.clear + newCodecCopy
                        case Failure(_) =>
                      }
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.getPartialCodec(4)
                      Try(BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)) match {
                        case Success(c) =>
                          partialCodec.addComma
                          if (codec.getDataType == 0) {
                            codec.skipChar(back = true)
                            currentCodec + c.addComma
                            currentCodecCopy + partialCodec
                          } else {
                            codec.skipChar(back = true)
                            currentCodec + partialCodec
                            currentCodecCopy + partialCodec
                          }
                        case Failure(_) =>
                          val processValue = codec.readToken2(dataType)
                          processValue.write(currentCodec)
                          processValue.write(currentCodecCopy)
                      }
                    case _ =>
                      val newCodecCopy = currentCodecCopy.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, newCodecCopy, currentCodecCopy)) match {
                        case Success(_) => currentCodec.clear + newCodecCopy
                        case Failure(_) =>
                      }
                  }
                }
              } else {
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.getPartialCodec(4)
                      val codecData =
                        if (!statementsList.equals(fullStatementsList))
                          BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction).getCodecData
                        else partialCodec.getCodecData
                      val partialToUse = partialCodec.addComma

                      Try(BosonImpl.inject(codecData, statementsList.drop(1), injFunction)) match {
                        case Success(c) =>
                          if (codec.getDataType == 0) {
                            codec.skipChar(back = true)
                            currentCodec + c.addComma
                            currentCodecCopy + partialToUse
                          } else {
                            codec.skipChar(back = true)
                            currentCodec + partialToUse
                            currentCodecCopy + partialToUse
                          }

                        case Failure(_) =>
                          currentCodec + partialToUse
                          currentCodecCopy + partialToUse
                      }

                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.getPartialCodec(4)
                      Try(BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                        case Success(c) =>
                          if (codec.getDataType == 0) {
                            codec.skipChar(back = true)
                            currentCodec + c.addComma
                            currentCodecCopy + partialCodec
                          } else {
                            codec.skipChar(back = true)
                            currentCodec + partialCodec.addComma
                            currentCodecCopy + partialCodec
                          }
                        case Failure(_) =>
                          currentCodecCopy + partialCodec.addComma
                          currentCodec + partialCodec
                      }
                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                }
              }
            case (x, _, C_END) if isArray && from.toInt <= x.toInt =>
              if (statementsList.size == 1) {
                if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.getPartialCodec(dataType)
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)

                      if (condition equals UNTIL_RANGE) {
                        if (codec.getDataType == 0) {
                          codec.skipChar(back = true)
                          currentCodec + partialCodec.addComma
                          currentCodecCopy + partialCodec
                        } else {
                          codec.skipChar(back = true)
                          currentCodec + subCodec.addComma
                          currentCodecCopy + subCodec
                        }
                      } else {
                        currentCodec + subCodec.addComma
                        currentCodecCopy + subCodec
                      }
                    case _ =>
                      if (fullStatementsList.head._1.isInstanceOf[HasElem]) {
                        codec.skipChar(back = true)
                        val processValue = codec.readToken2(dataType)
                        processValue.write(currentCodec)
                        currentCodecCopy.clear + currentCodec
                      } else {
                        val newCodecCopy = currentCodec.duplicate
                        Try(modifierEnd(codec, dataType, injFunction, currentCodec, newCodecCopy)) match {
                          case Success(_) => currentCodecCopy.clear + newCodecCopy
                          case Failure(_) =>
                        }
                      }
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.getPartialCodec(dataType)
                      val interiorObjCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      val changedInsideCodec =
                        if (statementsList.head._2.contains(C_DOUBLEDOT))
                          BosonImpl.inject(interiorObjCodec.getCodecData, fullStatementsList, injFunction)
                        else
                          interiorObjCodec

                      if (condition.equals(UNTIL_RANGE)) {
                        if (codec.getDataType == 0) {
                          codec.skipChar(back = true)
                          currentCodec + partialCodec.addComma
                          currentCodecCopy + partialCodec
                        }
                        else {
                          codec.skipChar(back = true)
                          currentCodec + partialCodec.addComma
                          currentCodecCopy + changedInsideCodec.addComma
                        }
                      } else {
                        currentCodec + changedInsideCodec.addComma
                        currentCodecCopy + partialCodec.addComma
                      }
                    case _ =>
                      if (fullStatementsList.head._1.isInstanceOf[HasElem]) {
                        codec.skipChar(back = true)
                        val processValue = codec.readToken2(dataType)
                        processValue.write(currentCodec)
                        currentCodecCopy.clear + currentCodec
                      } else {
                        val newCodecCopy = currentCodec.duplicate
                        Try(modifierEnd(codec, dataType, injFunction, currentCodec, newCodecCopy)) match {
                          case Success(_) => currentCodecCopy.clear + newCodecCopy
                          case Failure(_) =>
                        }
                      }
                  }
                }
              } else {
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.getPartialCodec(4)
                      val subCodec =
                        if (!statementsList.equals(fullStatementsList))
                          BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                        else
                          partialCodec

                      Try(BosonImpl.inject(subCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                        case Success(c) =>
                          if (condition equals UNTIL_RANGE) {
                            if (codec.getDataType == 0) {
                              codec.skipChar(back = true)
                              currentCodec + partialCodec.addComma
                              currentCodecCopy + partialCodec
                            } else {
                              codec.skipChar(back = true)
                              currentCodec + partialCodec.addComma
                              currentCodecCopy + c.addComma
                            }
                          } else {
                            currentCodec + c.addComma
                            currentCodecCopy + partialCodec.addComma
                          }
                        case Failure(_) =>
                          currentCodec + partialCodec.addComma
                          currentCodecCopy + partialCodec
                      }

                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(dataType).removeBrackets else codec.getPartialCodec(dataType)
                      val statementsToUse = if (statementsList.head._2.contains(C_DOUBLEDOT) || fullStatementsList.head._1.isInstanceOf[HasElem]) statementsList else statementsList.drop(1)
                      Try(BosonImpl.inject(partialCodec.getCodecData, statementsToUse, injFunction)) match {
                        case Success(c) =>
                          if (condition equals UNTIL_RANGE) {
                            if (codec.getDataType == 0) {
                              codec.skipChar(back = true)
                              currentCodec + partialCodec.addComma
                              currentCodecCopy + partialCodec
                            } else {
                              codec.skipChar(back = true)
                              currentCodec + partialCodec.addComma
                              currentCodecCopy + c.addComma
                            }
                          } else {
                            currentCodec + c.addComma
                            currentCodecCopy + partialCodec.addComma
                          }

                        case Failure(_) =>
                          currentCodec + partialCodec.addComma
                          currentCodecCopy + partialCodec
                      }
                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                }
              }

            case (x, _, C_END) if isArray && from.toInt > x.toInt =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                //this is the case where we haven't yet reached the condition the user sent us
                //but we still need to check inside this object to see there's a value that matches that condition
                dataType match {
                  case D_BSONOBJECT | D_BSONARRAY =>
                    val partialCodec = codec.getPartialCodec(4)
                    val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                    modifiedPartialCodec.changeBrackets(dataType, curlyToRect = false)
                    currentCodec + modifiedPartialCodec
                    currentCodecCopy + modifiedPartialCodec
                  case _ =>
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                    processValue.write(currentCodecCopy)
                }
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
                processValue.write(currentCodecCopy)
              }

            case (x, _, l) if isArray && (from.toInt <= x.toInt && l.toInt >= x.toInt) =>
              if (statementsList.lengthCompare(1) == 0) {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(dataType).removeBrackets else codec.getPartialCodec(dataType)
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      //Look inside the current object for cases that match the user given expression
                      val mergedCodec =
                        if (!statementsList.equals(fullStatementsList) && fullStatementsList.head._2.contains(C_DOUBLEDOT)) { //we only want to investigate inside this object if it has the property we're looking for
                          val auxCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                          auxCodec.changeBrackets(dataType)
                        } else modifiedPartialCodec

                      Try(modifierEnd(mergedCodec, dataType, injFunction, codec.createEmptyCodec, codec.createEmptyCodec)) match {
                        case Success(_) =>
                          currentCodec + mergedCodec
                          currentCodecCopy + partialCodec
                        case Failure(_) =>
                          currentCodec + mergedCodec.addComma
                          currentCodecCopy + partialCodec
                      }
                    case _ =>
                      val newCodecCopy = currentCodec.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, currentCodec, newCodecCopy)) match {
                        case Success(_) => currentCodecCopy.clear + newCodecCopy
                        case Failure(_) =>
                      }
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(dataType).removeBrackets else codec.getPartialCodec(dataType)
                      val codecMod = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      currentCodec + codecMod.addComma
                      currentCodecCopy + partialCodec.addComma
                    case _ =>
                      modifierEnd(codec, dataType, injFunction, currentCodec, currentCodecCopy)
                  }
                }
              } else {
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(dataType).removeBrackets else codec.getPartialCodec(dataType)
                      val mergedCodec =
                        if (!statementsList.equals(fullStatementsList))
                          BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                        else
                          partialCodec
                      Try(BosonImpl.inject(mergedCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                        case Success(c) =>
                          currentCodecCopy + partialCodec.addComma
                          currentCodec + c.addComma
                        case Failure(_) =>
                      }
                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                } else {
                  dataType match {
                    case D_BSONARRAY | D_BSONOBJECT =>
                      val partialCodec = codec.duplicate.getPartialCodec(4)
                      val processValue = codec.readToken2(dataType)
                      Try(BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                        case Success(c) =>
                          currentCodec + c.addComma
                          processValue.write(currentCodecCopy)
                        case Failure(_) =>
                          processValue.write(currentCodec)
                          processValue.write(currentCodecCopy)
                      }
                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                      processValue.write(currentCodecCopy)
                  }
                }
              }
            case (x, _, l) if isArray && (from.toInt > x.toInt || l.toInt < x.toInt) =>
              if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                dataType match {
                  case D_BSONOBJECT | D_BSONARRAY =>
                    val partialCodec = if (dataType == D_BSONARRAY) codec.getPartialCodec(dataType).removeBrackets else codec.getPartialCodec(dataType)
                    val modifiedAuxCodec =
                      if (!statementsList.equals(fullStatementsList) && fullStatementsList.head._2.contains(C_DOUBLEDOT))
                        if (fullStatementsList.head._2.contains(C_DOUBLEDOT))
                          BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                        else
                          BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      else partialCodec

                    modifiedAuxCodec.changeBrackets(dataType).addComma
                    currentCodec + modifiedAuxCodec
                    currentCodecCopy + modifiedAuxCodec

                  case _ =>
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                    processValue.write(currentCodecCopy)
                }
              } else {
                if (l.toInt < x.toInt) {
                  currentCodec.writeRest(codec.duplicate, dataType)
                  currentCodecCopy.writeRest(codec, dataType)
                } else {
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
                  processValue.write(currentCodecCopy)
                }
              }

            case (_, _, _) if !isArray =>
              if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                codec.getCodecData match {
                  case Left(_) =>
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                    processValue.write(currentCodecCopy)
                  case Right(_) =>
                    codec.skipChar(back = true)
                    val (key, b1) = codec.readKey
                    currentCodec.writeKey(key, b1)
                    currentCodecCopy.writeKey(key, b1)
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                    processValue.write(currentCodecCopy)
                    codec.skipChar() // Skip the comma written
                }
              } else throw CustomException("*modifyArrayEnd* Not a Array")
          }
      }
    }

    val codecFinal = currentCodec.writeCodecSize.removeTrailingComma(codec, rectBrackets = true)
    val codecFinalCopy = currentCodecCopy.writeCodecSize.removeTrailingComma(codec, rectBrackets = true)

    condition match {
      case UNTIL_RANGE => codecFinalCopy
      case _ => codecFinal
    }
  }

  /**
    * This function processes the types not relevant to the injection of an Array and copies them to the resulting
    * codec with the processed information up until this point
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
    * @return Unit
    */
  private def processTypesArrayEnd[T](statementList: StatementsList,
                                      fieldID: String,
                                      dataType: Int,
                                      codec: Codec,
                                      injFunction: T => T,
                                      condition: String,
                                      from: String = C_ZERO,
                                      to: String = C_END,
                                      resultCodec: Codec,
                                      resultCodecCopy: Codec)(implicit convertFunction: Option[TupleList => T] = None): Unit = {
    dataType match {

      case D_FLOAT_DOUBLE =>
        val token = codec.readToken(SonNumber(CS_DOUBLE))
        resultCodec.writeToken(token)
        resultCodecCopy.writeToken(token)

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        resultCodec.writeToken(SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        resultCodec.writeToken(SonString(CS_STRING, value0))
        resultCodec.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

        resultCodecCopy.writeToken(SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        resultCodecCopy.writeToken(SonString(CS_STRING, value0))
        resultCodecCopy.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

      case D_BSONOBJECT =>
        val codecObj = CodecObject.toCodec(codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info)
        val auxCodec = BosonImpl.inject(codecObj.getCodecData, statementList, injFunction)
        resultCodec + auxCodec.addComma
        resultCodecCopy + auxCodec

      case D_BSONARRAY =>
        val codecArr = CodecObject.toCodec(codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info)
        val auxCodec = BosonImpl.inject(codecArr.getCodecData, statementList, injFunction)
        resultCodec + auxCodec
        resultCodecCopy + auxCodec

      case D_NULL =>
        val token = codec.readToken(SonNull(CS_NULL))
        resultCodec.writeToken(token)
        resultCodecCopy.writeToken(token)

      case D_INT =>
        val token = codec.readToken(SonNumber(CS_INTEGER))
        resultCodec.writeToken(token)
        resultCodecCopy.writeToken(token)

      case D_LONG =>
        val token = codec.readToken(SonNumber(CS_LONG))
        resultCodec.writeToken(token)
        resultCodecCopy.writeToken(token)

      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
          case byte: Byte => byte == 1
        }
        resultCodec.writeToken(SonBoolean(CS_BOOLEAN, value0))
        resultCodecCopy.writeToken(SonBoolean(CS_BOOLEAN, value0))
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
  private def modifyArrayEndWithKey[T](statementsList: StatementsList,
                                       codec: Codec,
                                       fieldID: String,
                                       injFunction: T => T,
                                       condition: String,
                                       from: String,
                                       to: String = C_END)(implicit convertFunction: Option[TupleList => T] = None): Codec = {

    val (startReaderIndex, originalSize) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    val currentCodecCopy = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReaderIndex) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)
      currentCodecCopy.writeToken(SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)

      dataType match {
        case 0 =>
        case _ =>
          val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)).asInstanceOf[SonString].info.asInstanceOf[String]
          val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true).asInstanceOf[SonBoolean].info.asInstanceOf[Byte]

          currentCodec.writeToken(SonString(CS_STRING, key), isKey = true)
          currentCodec.writeToken(SonNumber(CS_BYTE, b), ignoreForJson = true)

          currentCodecCopy.writeToken(SonString(CS_STRING, key), isKey = true)
          currentCodecCopy.writeToken(SonNumber(CS_BYTE, b), ignoreForJson = true)

          key match {
            //In case we the extracted elem name is the same as the one we're looking for (or they're halfwords) and the
            //dataType is a BsonArray
            case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(fieldID, extracted)) && dataType == D_BSONARRAY =>
              if (statementsList.size == 1) {
                val partialCodec = codec.getPartialCodec(dataType)
                val newCodec = modifyArrayEnd(statementsList, partialCodec, injFunction, condition, from, to, statementsList, dataType)
                val newInjectCodec = if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  BosonImpl.inject(newCodec.getCodecData, statementsList, injFunction)
                } else newCodec
                currentCodec + newInjectCodec
                currentCodecCopy + newInjectCodec
              } else {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newCodec = modifyArrayEnd(statementsList.drop(1), partialCodec, injFunction, condition, from, to, statementsList, dataType)
                  currentCodec + newCodec
                  currentCodecCopy + newCodec
                } else {
                  val newCodec = modifyArrayEnd(statementsList.drop(1), codec, injFunction, condition, from, to, statementsList, dataType)
                  currentCodec + newCodec
                  currentCodecCopy + newCodec
                }
              }
            case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(fieldID, extracted)) && dataType != D_BSONARRAY =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr] && dataType == (D_BSONOBJECT | D_BSONARRAY)) {
                val partialCodec = codec.getPartialCodec(dataType)
                val modCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                currentCodec + modCodec
                currentCodecCopy + modCodec
              }
              else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
                processValue.write(currentCodecCopy)
              }
            case _ =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr] /*&& dataType == (D_BSONOBJECT | D_BSONARRAY)*/ ) {
                codec.getCodecData match {
                  case Left(_) =>
                    processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, currentCodec, currentCodecCopy)

                  case Right(jsonString) =>
                    if ((jsonString.charAt(codec.getReaderIndex - 1) equals ',') || (jsonString.charAt(codec.getReaderIndex - 1) equals ']')) {
                      //This happens if key is not a key but a value
                      codec.setReaderIndex(codec.getReaderIndex - key.length - 3)
                      val keyType = codec.getDataType
                      processTypesArrayEnd(statementsList, fieldID, keyType, codec, injFunction, condition, from, to, currentCodec, currentCodecCopy)
                    } else {
                      if (dataType == 4) {
                        val codecArr = CodecObject.toCodec(codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info)
                        val resCodec = arrayInjection(statementsList, codecArr, currentCodec, injFunction, key, left = 0, TO_RANGE, C_END)
                        currentCodec + resCodec
                        currentCodecCopy + resCodec
                      } else processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, currentCodec, currentCodecCopy)
                    }
                }
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
                processValue.write(currentCodecCopy)
              }

          }
      }
    }

    val finalCodec = currentCodec.writeCodecSize.removeTrailingComma(codec)
    val finalCodecCopy = currentCodecCopy.writeCodecSize.removeTrailingComma(codec)

    condition match {
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
    * @return the resulting codec and a string containing the key extracted
    */
  private def writeKeyAndByte(codec: Codec, writableCodec: Codec): (Codec, String) = {
    val (key, b1): (String, Byte) = codec.readKey
    writableCodec.writeKey(key, b1)
    (writableCodec, key)
  }

  /**
    * Method that extracts a list of tuples containing the name of a field of the object the value for that field
    *
    * @param value - Object of type T encoded in a array of bytes
    * @return a List of tuples containing the name of a field of the object the value for that field
    */
  private def extractTupleList(value: Either[Array[Byte], String]): TupleList = {
    val codec: Codec = value match {
      case Left(byteArr) => CodecObject.toCodec(Unpooled.copiedBuffer(byteArr))

      case Right(jsonString) => CodecObject.toCodec(jsonString)
    }
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val extractedTuples: ListBuffer[(String, Any)] = ListBuffer.empty[(String, Any)]

    while (codec.getReaderIndex - startReader < originalSize) {
      val dataType: Int = codec.readDataType()
      dataType match {
        case 0 =>
        case _ =>
          val fieldName: String = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].info.asInstanceOf[String]
          val fieldValue = dataType match {
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ => codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]

            case D_BSONOBJECT =>
              codec.skipChar() //skip the ":" character
              codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
                case byteBuff: ByteBuf => extractTupleList(Left(byteBuff.array))
                case jsonString: String => extractTupleList(Right(jsonString))
              }

            case D_BSONARRAY =>
              //                codec.skipChar() //skip the ":" character
              codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
                case byteBuff: ByteBuf => extractTupleList(Left(byteBuff.array))
                case jsonString: String => extractTupleList(Right(jsonString))
              }

            case D_FLOAT_DOUBLE => codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info.asInstanceOf[Double]

            case D_INT => codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info.asInstanceOf[Int]

            case D_LONG => codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info.asInstanceOf[Long]

            case D_BOOLEAN => codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
              case byte: Byte => byte == 1
            }

            case D_NULL => codec.readToken(SonNull(CS_NULL)); null;
          }
          extractedTuples += ((fieldName, fieldValue))
      }
    }
    extractedTuples.toList
  }

  /**
    * Private method that iterates through the fields of a given object and creates a list of tuple from the field names
    * and field values
    *
    * @param modifiedValue - the object to be iterated
    * @tparam T - The type T of the object to be iterated
    * @return A list of tuples consisting in pairs of field names and field values
    */
  def toTupleList[T](modifiedValue: T): TupleList = {
    val tupleArray = for {
      field <- modifiedValue.getClass.getDeclaredFields //Iterate through this object's fields
      if !field.getName.equals("$outer") //remove shapeless added $outer param
    } yield {
      field.setAccessible(true) //make this object accessible so we can get its value
      val attributeClass = field.get(modifiedValue).getClass.getSimpleName
      if (SCALA_TYPES_LIST.contains(attributeClass.toLowerCase))
        (field.getName, field.get(modifiedValue).asInstanceOf[Any])
      else
        (field.getName, toTupleList(field.get(modifiedValue)).asInstanceOf[Any])
    }
    tupleArray.toList
  }

  /**
    * Private method that receives a list of tuples and encodes them into a byte array or Json String (to be implemented)
    *
    * @param tupleList - List of tuples to be encoded
    * @return An array of bytes representing the encoded list of tuples
    */
  def encodeTupleList(tupleList: TupleList, value: Any): Either[Array[Byte], String] = {
    val encodedObject = new BsonObject()
    tupleList.foreach {
      case (fieldName: String, fieldValue: Any) =>
        fieldValue match {
          case nestedTupleList: TupleList =>
            val nestedObject = new BsonObject()
            nestedTupleList.foreach {
              case (name: String, value: Any) =>
                nestedObject.put(name, value)
            }
            encodedObject.put(fieldName, nestedObject)

          case _ => encodedObject.put(fieldName, fieldValue)
        }
    }
    value match {
      case _: Array[Byte] => Left(encodedObject.encodeToBarray)
      case _: String => Right(encodedObject.encodeToString)
    }
  }

  /**
    * Method that reads the next data type, writes it to the writeCodec passed as an argument and
    * returns the data type and the written codec
    *
    * @param codec      - The codec from which to read the data type
    * @param writeCodec - The codec in which to write the data type
    * @return A Tuple containing both the read data type and the written codec
    */
  private def readWriteDataType(codec: Codec, writeCodec: Codec, formerType: Int = 0): (Int, Codec) = {
    val dataType: Int = codec.readDataType(formerType)
    writeCodec.writeDataType(dataType)
    (dataType, writeCodec)
  }


  //********** Inject Value Functions Go Here **********//

  def injectRootValue(codec: Codec, value: Value): Codec = {
    val currentCodec = codec.createEmptyCodec
    val (mod, _) = writeValue(codec, currentCodec, value, 0)
    mod.wrapInBrackets()
  }

  /**
    *
    * @param codec
    * @param statementsList
    * @param value
    * @param key
    * @tparam T
    * @return
    */
  def injectKeyValue(codec: Codec, statementsList: StatementsList, value: Value, key: String): Codec = {

    val (startReader: Int, originalSize: Int) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReader) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)
      dataType match {
        case 0 =>

        case _ =>
          val (_, found) = if (codec.canReadKey()) writeKeyAndByte(codec, currentCodec) else (currentCodec, "")

          found match {
            case extracted if key.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(key, extracted) =>
              if (statementsList.lengthCompare(1) == 0) {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.getPartialCodec(dataType)
                      val subCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList, value)
                      writeValue(subCodec, currentCodec, value, dataType)

                    case _ => writeValue(codec, currentCodec, value, dataType)
                  }
                } else {
                  writeValue(codec, currentCodec, value, dataType)
                }
              } else {
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.getPartialCodec(dataType)

                      val modifiedSubCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                      if (dataType == D_BSONARRAY) modifiedSubCodec.changeBrackets(4)
                      val subCodec = BosonImpl.injectValue(modifiedSubCodec.getCodecData, statementsList, value)
                      currentCodec + subCodec

                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                  }
                } else {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val codecData: DataStructure = codec.getCodecData
                      codecData match {
                        case Left(bb) =>
                          val subCodec = BosonImpl.injectValue(codecData, statementsList.drop(1), value)
                          currentCodec + subCodec
                          codec.setReaderIndex(bb.readerIndex)
                        case Right(_) =>
                          val partialCodec = codec.getPartialCodec(dataType)
                          val subCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                          currentCodec + subCodec
                          codec.setReaderIndex(codec.getReaderIndex + subCodec.getWriterIndex)
                      }
                    case _ =>
                      val processValue = codec.readToken2(dataType)
                      processValue.write(currentCodec)
                  }
                }
              }
            case x if key.toCharArray.deep != x.toCharArray.deep && !isHalfWord(key, x) =>
              if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                dataType match {
                  case D_BSONOBJECT | D_BSONARRAY =>
                    val partialCodec = codec.getPartialCodec(dataType)
                    val subCodec = injectKeyValue(partialCodec, statementsList, value, key)
                    currentCodec + subCodec
                  case _ =>
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                }
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }
          }
      }
    }
    currentCodec.writeCodecSize.removeTrailingComma(codec, checkOpenRect = true)
  }

  /**
    *
    * @param codec
    * @param statementsList
    * @param value
    * @param key
    * @param condition
    * @param from
    * @param to
    * @param convertFunction
    * @tparam T
    * @return
    */
  def injectArrayValueWithKey[T](codec: Codec, statementsList: StatementsList, value: Value, key: String, condition: String, from: String, to: String = C_END)(implicit convertFunction: Option[TupleList => T] = None): Codec = {

    val (startReaderIndex, originalSize) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReaderIndex) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)

      dataType match {
        case 0 =>
        case _ =>
          val (found, b): (String, Byte) = codec.readKey

          currentCodec.writeKey(found, b)

          found match {
            case extracted if (key.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(key, extracted)) && dataType == D_BSONARRAY =>
              val partialCodec = codec.getPartialCodec(dataType)
              val newCodec = injectArrayValue(partialCodec, statementsList, value, from, condition, to, dataType)
              currentCodec + newCodec

            case extracted if (key.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(key, extracted)) && dataType != D_BSONARRAY =>
              val processValue = codec.readToken2(dataType)
              processValue.write(currentCodec)

            case _ =>
              if ((dataType == D_BSONARRAY || dataType == D_BSONOBJECT) && statementsList.head._2.equals(C_DOUBLEDOT)) {
                val partialCodec = codec.getPartialCodec(dataType)
                val modified = if (dataType == D_BSONARRAY) {
                  injectArrayValue(partialCodec, statementsList, value, from, condition, to, 4)
                } else {
                  BosonImpl.injectValue(partialCodec.getCodecData, statementsList, value)
                }
                currentCodec + modified
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }
          }

      }
    }
    currentCodec.writeCodecSize.removeTrailingComma(codec, checkOpenRect = true)
  }

  /**
    *
    * @param codec
    * @param statementsList
    * @param value
    * @param condition
    * @param from
    * @param to
    * @param formerType
    * @param convertFunction
    * @tparam T
    * @return
    */
  def injectArrayValue[T](codec: Codec, statementsList: StatementsList, value: Value, condition: String, from: String, to: String, formerType: Int)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val (startReaderIndex, originalSize) = (codec.getReaderIndex, codec.readSize)
    var counter: Int = -1

    val currentCodec = codec.createEmptyCodec
    val currentCodecCopy = codec.createEmptyCodec

    while ((codec.getReaderIndex - startReaderIndex) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec, formerType)
      currentCodecCopy.writeToken(SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
      dataType match {
        case 0 =>
        case _ =>
          val (key, b): (String, Byte) = codec.getCodecData match {
            case Left(_) => codec.readKey
            case Right(_) =>
              counter += 1
              (counter.toString, codec.readByte)
          }

          currentCodec.writeArrayKey(key, b)
          currentCodecCopy.writeArrayKey(key, b)

          val isArray = codec.isArray(formerType, key)

          (key, condition, to) match {
            case (_, C_END, _) if isArray =>
              if (statementsList.lengthCompare(1) == 0) {
                val newCurrentCodec = currentCodecCopy.duplicate
                Try(writeValue(codec.duplicate, newCurrentCodec, value, dataType)) match {
                  case Success(_) =>
                    currentCodec.clear + newCurrentCodec
                    val value = codec.readToken2(dataType)
                    value.write(currentCodecCopy)
                  case Failure(_) =>
                }
              } else {
                if (dataType == D_BSONOBJECT) {
                  val partialCodec = codec.getPartialCodec(dataType)
                  val double = if (!statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                    BosonImpl.injectValue(newCodec.getCodecData, statementsList, value)
                  } else {
                    BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                  }
                  currentCodec.clear + currentCodecCopy.duplicate + double.addComma
                  currentCodecCopy + partialCodec.addComma
                } else {
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
                }
              }

            case (x, _, C_END) if isArray && from.toInt <= x.toInt =>
              if (statementsList.lengthCompare(1) == 0) {
                if (condition.equals(UNTIL_RANGE)) {
                  if (codec.getDataType == 0) {
                    codec.skipChar(back = true)
                    val processValue = codec.readToken2(dataType)
                    processValue.write(currentCodec)
                    processValue.write(currentCodecCopy)
                  } else {
                    currentCodecCopy.clear + currentCodec.duplicate
                    val before = writeValue(codec, currentCodec, value, dataType)
                    before._2.write(currentCodecCopy)
                  }
                } else {
                  if (dataType == D_BSONOBJECT && statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.getPartialCodec(dataType)
                    val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList, value)
                    currentCodec + newCodec.addComma
                  } else {
                    writeValue(codec, currentCodec, value, dataType)
                  }
                }
              } else {
                if (dataType == D_BSONOBJECT) {
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                  currentCodec + newCodec.addComma
                } else {
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
                }
              }
            case (x, _, C_END) if isArray && from.toInt > x.toInt =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && dataType == D_BSONOBJECT) {
                val partialCodec = codec.getPartialCodec(dataType)
                val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                val double = BosonImpl.injectValue(newCodec.getCodecData, statementsList, value)
                currentCodec + double.addComma
              } else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }

            case (x, _, l) if isArray && (from.toInt <= x.toInt && l.toInt >= x.toInt) =>
              if (statementsList.lengthCompare(1) == 0) {
                if (dataType == D_BSONOBJECT) {
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList, value)
                  currentCodec + newCodec.addComma
                } else {
                  writeValue(codec, currentCodec, value, dataType)
                }
              } else {
                if (dataType == D_BSONOBJECT) {
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList.drop(1), value)
                  val double = if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    BosonImpl.injectValue(newCodec.getCodecData, statementsList, value)
                  } else newCodec

                  currentCodec + double.addComma
                } else {
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
                }
              }

            case (x, _, l) if isArray && (from.toInt > x.toInt || l.toInt < x.toInt) =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && dataType == D_BSONOBJECT) {
                val partialCodec = codec.getPartialCodec(dataType)
                val newCodec = BosonImpl.injectValue(partialCodec.getCodecData, statementsList, value)
                currentCodec + newCodec.addComma
              } else {
                if (l.toInt < x.toInt) {
                  currentCodec.writeRest(codec, dataType)
                } else {
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
                }
              }
            case _ =>
              val processValue = codec.readToken2(dataType)
              processValue.write(currentCodec)
          }
      }
    }

    if (formerType != 4) {
      currentCodec.writeCodecSize.removeTrailingComma(codec, rectBrackets = true, noBrackets = true)
    } else {
      if (condition.equals(UNTIL_RANGE)) currentCodecCopy.writeCodecSize.removeTrailingComma(codec, rectBrackets = true)
      else currentCodec.writeCodecSize.removeTrailingComma(codec, rectBrackets = true)
    }
  }


  /**
    *
    * @param codec
    * @param statementsList
    * @param value
    * @param key
    * @param elem
    * @tparam T
    * @return
    */
  def injectHasElemValue[T](codec: Codec, statementsList: StatementsList, value: Value, key: String, elem: String)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val (startReader: Int, originalSize: Int) = (codec.getReaderIndex, codec.readSize)

    val currentCodec = codec.createEmptyCodec
    while ((codec.getReaderIndex - startReader) < originalSize) {
      val (dataType, _) = readWriteDataType(codec, currentCodec)
      dataType match {
        case 0 => //Nothing
        case _ =>
          val (_, found) = if (codec.canReadKey()) writeKeyAndByte(codec, currentCodec) else (currentCodec, "")
          found match {
            case extracted if (key.toCharArray.deep == extracted.toCharArray.deep || isHalfWord(key, extracted)) && dataType == D_BSONARRAY =>
              //the key is a halfword and matches with the extracted key, dataType is an array
              //So we will look for the "elem" of interest inside the current object
              dataType match {
                case D_BSONARRAY =>
                  /*
                  * HasElem only works with arrays of objects, if those objects contain the key to be modified
                  * */
                  val partialCodec = codec.getPartialCodec(dataType)
                  val newStatementList: StatementsList = statementsList.head :: (Key(elem), ".") :: statementsList.tail
                  val modified = injectArrayValue(partialCodec, newStatementList, value, TO_RANGE, "0", C_END, dataType)
                  currentCodec + modified

                case _ =>
                  val processValue = codec.readToken2(dataType)
                  processValue.write(currentCodec)
              }


            case _ =>
              if (statementsList.head._2.contains(C_DOUBLEDOT) && dataType == 3) {
                val partialCodec = codec.getPartialCodec(dataType)
                val y = if (partialCodec.wrappable) partialCodec.wrapInBrackets(key = key) else partialCodec
                val modifiedCodec: Codec = injectHasElemValue(y, statementsList, value, key, elem)
                currentCodec + modifiedCodec
              }
              else {
                val processValue = codec.readToken2(dataType)
                processValue.write(currentCodec)
              }
          }
      }
    }
    currentCodec.writeCodecSize.removeTrailingComma(codec, checkOpenRect = true)
  }

  /**
    *
    * @param codec
    * @param currentCodec
    * @param value
    * @param dataType
    * @tparam T
    * @return
    */
  private def writeValue(codec: Codec, currentCodec: Codec, value: Value, dataType: Int): (Codec, Value) = {
    value.write(currentCodec)
    val before = codec.readToken2(dataType)
    (currentCodec, before)
  }
}