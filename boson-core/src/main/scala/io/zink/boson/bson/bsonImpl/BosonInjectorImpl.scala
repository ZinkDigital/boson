package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import BosonImpl.{DataStructure, StatementsList}
import io.zink.bsonLib.BsonObject
import org.scalameter.Events

import scala.util.{Failure, Success, Try}

private[bsonImpl] object BosonInjectorImpl {

  private type TupleList = List[(String, Any)]

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

    def anyToEither(result: Any): Either[ByteBuf, String] = result match {
      case byteBuf: ByteBuf => Left(byteBuf)
      case jsonString: String => Right(jsonString)
    }

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec - the codec to write the modified information into
      * @return A Codec containing the alterations made
      */
    def writeCodec(currentCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) currentCodec
      else {
        val (dataType, codecWithDataType) = readWriteDataType(codec, currentCodec)
        val newCodec = dataType match {
          case 0 => writeCodec(codecWithDataType)

          case _ =>
            val (codecWithKey, key) = if (codec.canReadKey) writeKeyAndByte(codec, codecWithDataType) else (codecWithDataType, "")

            key match {
              case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted) =>
                if (statementsList.lengthCompare(1) == 0) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
                        val partialData = codec.readToken(token) match {
                          case SonObject(_, result) => anyToEither(result)
                          case SonArray(_, result) => anyToEither(result)
                        }
                        val subCodec = BosonImpl.inject(partialData, statementsList, injFunction)
                        modifierAll(subCodec, codecWithKey, dataType, injFunction)

                      case _ => modifierAll(codec, codecWithKey, dataType, injFunction)
                    }
                  } else modifierAll(codec, codecWithKey, dataType, injFunction)

                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
                        val partialData = codec.readToken(token) match {
                          case SonObject(_, result) => anyToEither(result)
                          case SonArray(_, result) => anyToEither(result)
                        }
                        val modifiedSubCodec = BosonImpl.inject(partialData, statementsList.drop(1), injFunction)
                        val subCodec = BosonImpl.inject(modifiedSubCodec.getCodecData, statementsList, injFunction)
                        codecWithKey + subCodec

                      case _ => processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                    }
                  } else {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val codecData: DataStructure = codec.getCodecData
                        codecData match {
                          case Left(byteBuf) =>
                            val subCodec = BosonImpl.inject(codecData, statementsList.drop(1), injFunction)
                            val mergedCodecs = codecWithKey + subCodec
                            val codecWithReaderIndex = CodecObject.toCodec(byteBuf)
                            codec.setReaderIndex(codecWithReaderIndex.getReaderIndex)
                            mergedCodecs

                          case Right(_) =>
                            val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
                            val partialData = codec.readToken(token) match {
                              case SonObject(_, result) => Right(result.asInstanceOf[String])
                              case SonArray(_, result) => Right(result.asInstanceOf[String])
                            }
                            val subCodec = BosonImpl.inject(partialData, statementsList.drop(1), injFunction)
                            val mergedCodecs = codecWithKey + subCodec
                            codec.setReaderIndex(codec.getReaderIndex + subCodec.getWriterIndex)
                            mergedCodecs
                        }
                      case _ => processTypesArray(dataType, codec, codecWithKey)
                    }
                  }
                }
              case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  codec.getCodecData match {
                    case Right(jsonString) =>
                      if (jsonString.charAt(0).equals('[')) {
                        if (codec.getReaderIndex != 1) {
                          //codec.setReaderIndex(codec.getReaderIndex - (key.length + 4)) //Go back the size of the key plus a ":", two quotes and the beginning "{"
                          val processedObj = processTypesAll(statementsList, dataType, codec, codecWithDataType, fieldID, injFunction)
                          CodecObject.toCodec(processedObj.getCodecData.asInstanceOf[Right[ByteBuf, String]].value + ",")
                        } else {
                          codec.setReaderIndex(0)
                          processTypesArray(4, codec, codecWithKey)
                        }
                      } else processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)

                    case Left(_) => processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                  }
                } else processTypesArray(dataType, codec, codecWithKey)
            }
        }
        writeCodec(newCodec)
      }
    }

    val codecWithoutSize = writeCodec(createEmptyCodec(codec))
    val mergedCodecs = codecWithoutSize.writeCodecSize + codecWithoutSize
    codec.removeTrailingComma(mergedCodecs, checkOpenRect = true)
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

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to write the modified information into
      * @return A Codec containing the alterations made
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val (dataType, codecWithDataType) = readWriteDataType(codec, writableCodec)
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
                //So we will look for the "elem" of interest inside the current object
                val modifiedCodec: Codec = searchAndModify(statementsList, codec, elem, injFunction, codecWithKeyByte)
                iterateDataStructure(modifiedCodec)

              case _ =>
                val processedCodec: Codec =
                  if (statementsList.head._2.contains(C_DOUBLEDOT))
                    processTypesHasElem(statementsList, dataType, fieldID, elem, codec, codecWithKeyByte, injFunction)
                  else
                    processTypesArray(dataType, codec, codecWithKeyByte)
                iterateDataStructure(processedCodec)
            }
        }
      }
    }

    val codecWithoutSize = iterateDataStructure(createEmptyCodec(codec)) //Initiate recursion with an empty data structure
    val returnCodec = codecWithoutSize.writeCodecSize + codecWithoutSize
    codec.removeTrailingComma(returnCodec)
  }

  /**
    * Function used to search for an element inside an object inside an array after finding the key of interest
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param elem           - Name of the element of interest
    * @param injFunction    - The injection function to be applied
    * @param writableCodec  - Structure to where we write the values
    * @tparam T - The type of input and output of the injection function
    * @return a new Codec with the value injected
    */
  private def searchAndModify[T](statementsList: StatementsList, codec: Codec, elem: String, injFunction: T => T, writableCodec: Codec)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    codec.skipChar() //If it's a CodecJson we need to skip the "[" character

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to write the information into
      * @return A codec containing the alterations made
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val (dataType, codecWithDataType) = readWriteDataType(codec, writableCodec)
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType)

          case _ =>
            dataType match {
              case D_BSONOBJECT =>
                val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)
                val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
                  case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                  case jsonString: String => CodecObject.toCodec("{\"" + key + "\":" + jsonString) //For CodecJson the "{" and the key is already read, we need to add it back
                } //Obtain only the size and the object itself of this BsonObject
                if (hasElem(partialCodec.duplicate, elem)) {
                  if (statementsList.size == 1) {

                    val newStatementList: StatementsList = statementsList.map {
                      case (statement, dots) => statement match {
                        case HasElem(_, element) => (Key(element), dots)
                        case _ => (statement, dots)
                      }
                    }

                    val modifiedCodec = BosonImpl.inject(partialCodec.getCodecData, newStatementList, injFunction)

                    if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                      val modifiedSubCodec = BosonImpl.inject(modifiedCodec.getCodecData, statementsList, injFunction)
                      iterateDataStructure(codec.decideCodec(codecWithKey, writableCodec) + modifiedSubCodec.addComma)
                    } else iterateDataStructure(codec.decideCodec(codecWithKey, writableCodec) + modifiedCodec.addComma)

                  } else {
                    val modifiedCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                    val modifiedSubCodec =
                      if (statementsList.head._2.contains(C_DOUBLEDOT))
                        BosonImpl.inject(modifiedCodec.getCodecData, statementsList, injFunction)
                      else
                        modifiedCodec
                    iterateDataStructure(codec.decideCodec(codecWithKey, writableCodec) + modifiedSubCodec.addComma)
                  }

                } else iterateDataStructure(codec.decideCodec(codecWithKey, writableCodec) + partialCodec.addComma)

              case _ =>
                val codecToWrite = codec.decideCodec(writeKeyAndByte(codec, codecWithDataType)._1, writableCodec)
                iterateDataStructure(processTypesArray(dataType, codec, codecToWrite)) //If its not an object then it will not have the element we're looking for inside it
            }
        }
      }
    }

    val modifiedSubCodec = iterateDataStructure(createEmptyCodec(codec)) //Search for the element of interest it and try to apply the injection function to it
    val modCodecWithSize = modifiedSubCodec.writeCodecSize + modifiedSubCodec //Add the size of the resulting sub-codec (plus 4 bytes) with the actual sub-codec
    writableCodec + codec.removeTrailingComma(modCodecWithSize, rectBrackets = true)
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
      val dataType = codec.readDataType()
      dataType match {
        case 0 =>
        case _ =>
          key = codec.readToken(SonString(CS_NAME)).asInstanceOf[SonString].info.asInstanceOf[String]
          dataType match {

            case D_FLOAT_DOUBLE => codec.readToken(SonNumber(CS_DOUBLE))

            case D_ARRAYB_INST_STR_ENUM_CHRSEQ => codec.readToken(SonString(CS_STRING))

            case D_BSONOBJECT | D_BSONARRAY =>
              codec.skipChar() //Skip the ":" character
              codec.readToken(SonObject(CS_OBJECT_WITH_SIZE))

            case D_BOOLEAN => codec.readToken(SonBoolean(CS_BOOLEAN))

            case D_LONG => codec.readToken(SonNumber(CS_LONG))

            case D_INT => codec.readToken(SonNumber(CS_INTEGER))

            case D_NULL => codec.readToken(SonNull(CS_NULL))
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
  def rootInjection[T](codec: Codec, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec =
    codec.getCodecData match {
      case Left(byteBuf) =>
        val bsonBytes: Array[Byte] = byteBuf.array() //extract the bytes from the bytebuf
      /*
        We first cast the result of applyFunction as String because when we input a byte array we apply the injFunction to it
        as a String. We return that modified String and convert it back to a byte array in order to create a ByteBuf from it
       */
      val modifiedBytes: Array[Byte] = applyFunction(injFunction, bsonBytes).asInstanceOf[Array[Byte]]
        CodecObject.toCodec(Unpooled.copiedBuffer(modifiedBytes))

      case Right(jsonString) =>
        val modifiedString: String = applyFunction(injFunction, jsonString).asInstanceOf[String]
        CodecObject.toCodec(modifiedString)
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
  private def processTypesHasElem[T](statementsList: StatementsList, dataType: Int, fieldID: String, elem: String, codec: Codec, resultCodec: Codec, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = dataType match {
    case D_BSONOBJECT =>
      val objectCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
        case jsonString: String => CodecObject.toCodec(jsonString)
      }
      val modifiedCodec: Codec = modifyHasElem(statementsList, objectCodec, fieldID, elem, injFunction)
      resultCodec + modifiedCodec

    case D_BSONARRAY =>
      val arrayCodec: Codec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
        case jsonString: String => CodecObject.toCodec(jsonString + ",")
      }
      resultCodec + arrayCodec


    case D_FLOAT_DOUBLE => codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_DOUBLE)))

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
      val codecWithValue = codec.writeToken(createEmptyCodec(codec), SonString(CS_STRING, value0))
      val codecWithSize = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
      val codecWithZeroByte = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
      ((resultCodec + codecWithSize) + codecWithValue) + codecWithZeroByte

    case D_INT => codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_INTEGER)))

    case D_LONG => codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_LONG)))

    case D_BOOLEAN =>
      val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
        case byte: Byte => byte == 1
      }
      codec.writeToken(resultCodec, SonBoolean(CS_BOOLEAN, value0))

    case D_NULL => codec.writeToken(resultCodec, codec.readToken(SonNull(CS_NULL)))
  }

  /**
    * Fucntion used to process all the values inside Arrays that are not of interest to the injection and copies them
    * to the current result Codec
    *
    * @param dataType        - Type of the value found and processing
    * @param codec           - Structure from which we are reading the values
    * @param currentResCodec - Structure that contains the information already processed and where we write the values
    * @return A Codec containing the alterations made
    */
  private def processTypesArray[T](dataType: Int, codec: Codec, currentResCodec: Codec)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    dataType match {
      case D_ZERO_BYTE => currentResCodec

      case D_FLOAT_DOUBLE => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

      case D_BSONOBJECT => codec.writeToken(currentResCodec, codec.readToken(SonObject(CS_OBJECT_INJ)))

      case D_BSONARRAY =>
        val value0 = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
          case byteBuff: ByteBuf => byteBuff.array
          case jsonString: String => jsonString
        }
        codec.writeToken(currentResCodec, SonArray(CS_ARRAY, value0))

      case D_NULL => codec.writeToken(currentResCodec, codec.readToken(SonNull(CS_NULL)))

      case D_INT => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))

      case D_LONG => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))

      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
          case byte: Byte => byte == 1
        }
        codec.writeToken(currentResCodec, SonBoolean(CS_BOOLEAN, value0))
    }
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
  private def modifierAll[T](codec: Codec, currentResCodec: Codec, seqType: Int, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0 = codec.readToken(SonNumber(CS_DOUBLE)).asInstanceOf[SonNumber].info.asInstanceOf[Double]
        codec.writeToken(currentResCodec, SonNumber(CS_DOUBLE, applyFunction(injFunction, value0).asInstanceOf[Double]))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        applyFunction(injFunction, value0) match {
          case valueAny: Any =>
            val value = valueAny.toString
            val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
            codec.writeToken(strSizeCodec, SonString(CS_STRING, value)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        }

      case D_BSONOBJECT =>
        codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
          case byteBuf: ByteBuf => codec.writeToken(currentResCodec, SonArray(CS_ARRAY, applyFunction(injFunction, byteBuf.array()).asInstanceOf[Array[Byte]]))
          case str: String => codec.writeToken(currentResCodec, SonString(CS_STRING_NO_QUOTES, applyFunction(injFunction, str).asInstanceOf[String]))
        }

      case D_BSONARRAY =>
        codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
          case byteBuf: ByteBuf => codec.writeToken(currentResCodec, SonArray(CS_ARRAY, applyFunction(injFunction, byteBuf.array()).asInstanceOf[Array[Byte]]))

          case str: String => codec.writeToken(currentResCodec, SonString(CS_STRING_NO_QUOTES, applyFunction(injFunction, str).asInstanceOf[String]))
        }

      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
          case byte: Byte => byte == 1
        }
        codec.writeToken(currentResCodec, SonBoolean(CS_BOOLEAN, applyFunction(injFunction, value0).asInstanceOf[Boolean]))

      case D_NULL => throw CustomException(s"NULL field. Can not be changed")

      case D_INT =>
        val value0 = codec.readToken(SonNumber(CS_INTEGER)).asInstanceOf[SonNumber].info.asInstanceOf[Int]
        codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, applyFunction(injFunction, value0).asInstanceOf[Int]))

      case D_LONG =>
        val value0 = codec.readToken(SonNumber(CS_LONG)).asInstanceOf[SonNumber].info.asInstanceOf[Long]
        codec.writeToken(currentResCodec, SonNumber(CS_LONG, applyFunction(injFunction, value0).asInstanceOf[Long]))
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
    * @return A Codec containing the alterations made and an Auxiliary Codec
    */
  private def modifierEnd[T](codec: Codec, dataType: Int, injFunction: T => T, codecRes: Codec, codecResCopy: Codec)(implicit convertFunction: Option[TupleList => T] = None): (Codec, Codec) = dataType match {

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]

      val resCodec = applyFunction(injFunction, value0) match {
        case value: Any => //This Any is exclusively either String or Instant
          val str = value.toString
          val strSizeCodec = codec.writeToken(codecRes, SonNumber(CS_INTEGER, str.length + 1), ignoreForJson = true)
          codec.writeToken(strSizeCodec, SonString(CS_STRING, str)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
      }
      val strSizeCodec = codec.writeToken(codecResCopy, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
      val resCodecCopy = codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + codec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
      (resCodec, resCodecCopy)

    case D_BSONOBJECT =>
      val token = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      val resCodec: Codec = token.asInstanceOf[SonObject].info match {
        case byteBuf: ByteBuf => codec.writeToken(codecRes, SonArray(CS_ARRAY, applyFunction(injFunction, byteBuf.array()).asInstanceOf[Array[Byte]]))

        case str: String => codec.writeToken(codecRes, SonString(CS_STRING, applyFunction(injFunction, str).asInstanceOf[String]))
      }
      (resCodec, resCodecCopy)

    case D_BSONARRAY =>
      val token = codec.readToken(SonArray(CS_ARRAY))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      val resCodec = token.asInstanceOf[SonArray].info match {
        case byteArr: Array[Byte] => codec.writeToken(codecRes, SonArray(CS_ARRAY, applyFunction(injFunction, byteArr).asInstanceOf[Array[Byte]]))

        case jsonString: String => codec.writeToken(codecRes, SonString(CS_STRING, applyFunction(injFunction, jsonString).asInstanceOf[String]))
      }
      (resCodec, resCodecCopy)

    case D_BOOLEAN =>
      val token = codec.readToken(SonBoolean(CS_BOOLEAN))
      val value0: Boolean = token.asInstanceOf[SonBoolean].info match {
        case byte: Byte => byte == 1
      }
      val resCodec = codec.writeToken(codecRes, SonBoolean(CS_BOOLEAN, applyFunction(injFunction, value0).asInstanceOf[Boolean]))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_FLOAT_DOUBLE =>
      val token = codec.readToken(SonNumber(CS_DOUBLE))
      val value0: Double = token.asInstanceOf[SonNumber].info.asInstanceOf[Double]
      val resCodec = codec.writeToken(codecRes, SonNumber(CS_DOUBLE, applyFunction(injFunction, value0).asInstanceOf[Double]))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_INT =>
      val token = codec.readToken(SonNumber(CS_INTEGER))
      val value0: Int = token.asInstanceOf[SonNumber].asInstanceOf[Int]
      val resCodec = codec.writeToken(codecRes, SonNumber(CS_INTEGER, applyFunction(injFunction, value0).asInstanceOf[Int]))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_LONG =>
      val token = codec.readToken(SonNumber(CS_LONG))
      val value0: Long = token.asInstanceOf[SonNumber].asInstanceOf[Long]
      val resCodec = codec.writeToken(codecRes, SonNumber(CS_LONG, applyFunction(injFunction, value0).asInstanceOf[Long]))
      val resCodecCopy = codec.writeToken(codecResCopy, token)
      (resCodec, resCodecCopy)

    case D_NULL => throw CustomException(s"NULL field. Can not be changed")
  }

  /**
    * Function that processes the types of all the information that is not relevant for the injection and copies it to
    * the current resulting Codec
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
  private def processTypesAll[T](statementsList: StatementsList, seqType: Int, codec: Codec, currentResCodec: Codec, fieldID: String, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    seqType match {

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

      case D_BSONOBJECT =>
        val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
          case jsonString: String => CodecObject.toCodec(jsonString)
        }
        currentResCodec + modifyAll(statementsList, partialCodec, fieldID, injFunction)

      case D_BSONARRAY =>
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
          case jsonString: String => CodecObject.toCodec(jsonString)
        }
        currentResCodec + modifyAll(statementsList, partialCodec, fieldID, injFunction)

      case D_INT => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))

      case D_FLOAT_DOUBLE => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))

      case D_LONG => codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))

      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
          case byte: Byte => byte == 1
        }
        codec.writeToken(currentResCodec, SonBoolean(CS_BOOLEAN, value0))

      case D_NULL => currentResCodec
    }
  }

  /**
    * Verifies if Key given by user is a HalfWord and if it matches with the one extracted.
    *
    * @param fieldID   - Key given by User.
    * @param extracted - Key extracted.
    * @return A boolean that is true if it's a HalWord or false or if it's not
    */
  def isHalfword(fieldID: String, extracted: String): Boolean = {
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
                isHalfword(s.substring(1 + list.apply(1).length), x.substring(list.apply(1).length))
              else
                isHalfword(s, x.substring(1))

            case s if !s.startsWith(STAR) =>
              if (x.startsWith(list.head)) isHalfword(s.substring(list.head.length), extracted.substring(list.head.length))
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
            //try with the value being a Array[Byte]
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
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the values
    * @param injFunction    - Function given by the user to alter specific values
    * @param condition      - Represents a type of injection, it can be END, ALL, FIRST, # TO #, # UNTIL #
    * @param from           - Represent the inferior limit of a given range
    * @param to             - Represent the superior limit of a given range
    * @tparam T - Type of the value being injected
    * @return A Codec containing the alterations made
    */
  private def modifyArrayEnd[T](statementsList: StatementsList, codec: Codec, injFunction: T => T, condition: String, from: String, to: String = C_END, fullStatementsList: StatementsList, formerType: Int)(implicit
                                                                                                                                                                                                             convertFunction: Option[TupleList => T] = None): Codec = {
    val (startReaderIndex, originalSize) = (codec.getReaderIndex, codec.readSize)
    var counter: Int = -1

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec     - The codec where we right the values of the processed data
      * @param currentCodecCopy - An Auxiliary codec to where we write the values in case the previous cycle was the last one
      * @param exceptions       - An Int that represents how many exceptions have occurred (This value is used to determine if the range inserted is a TO_RANGE or UNTIL_RANGE)
      * @return a codec pair with the modifications made and the amount of exceptions that occured
      */
    def iterateDataStructure(currentCodec: Codec, currentCodecCopy: Codec, exceptions: Int): (Codec, Codec, Int) = {
      if ((codec.getReaderIndex - startReaderIndex) >= originalSize && exceptions < 2)
        (currentCodec, currentCodecCopy, exceptions)
      else {
        val (dataType, codecWithDataType) = readWriteDataType(codec, currentCodec, formerType)
        val codecWithDataTypeCopy = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType, codecWithDataTypeCopy, exceptions)

          case _ =>
            val key: String = codec.getCodecData match {
              case Left(_) => codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)).asInstanceOf[SonString].info.asInstanceOf[String]
              case Right(_) =>
                counter += 1
                counter.toString
            }
            val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true).asInstanceOf[SonBoolean].info.asInstanceOf[Byte]

            val codecWithoutKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key), ignoreForJson = true)
            val codecWithKey = codec.writeToken(codecWithoutKey, SonNumber(CS_BYTE, b), ignoreForJson = true)

            val codecWithoutKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key), ignoreForJson = true)
            val codecWithKeyCopy = codec.writeToken(codecWithoutKeyCopy, SonNumber(CS_BYTE, b), ignoreForJson = true)

            val isArray = codec.isArray(formerType, key)

            val ((codecResult, codecResultCopy), exceptionsResult): ((Codec, Codec), Int) = (key, condition, to) match {
              case (_, C_END, _) if isArray =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {

                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val partialData: DataStructure = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                          case byteBuf: ByteBuf => Left(byteBuf)
                          case jsonString: String => Right("{" + jsonString + "}")
                        }

                        val partialCodec =
                          if (statementsList.head._1.isInstanceOf[ArrExpr])
                            BosonImpl.inject(partialData, statementsList, injFunction)
                          else {
                            partialData match {
                              case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                              case Right(jsonString) => CodecObject.toCodec(jsonString)
                            }
                          }

                        val partialCodecModified = BosonImpl.inject(partialData, statementsList, injFunction)
                        val subPartial = BosonImpl.inject(partialCodecModified.getCodecData, fullStatementsList, injFunction)
                        val subPartialToUse = subPartial.addComma

                        val partialCodecToUse = partialCodec.addComma
                        ((codecWithKeyCopy + subPartialToUse, codecWithKeyCopy + partialCodecToUse), 0)
                      case _ =>
                        Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                          case Success(tuple) => (tuple, 0)
                          case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                        }
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec("{" + string + "}")
                        }
                        Try(BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)) match {
                          case Success(c) =>
                            val cToUse = c.addComma
                            val partialToUse = partialCodec.addComma
                            ((codecWithKeyCopy + cToUse, codecWithKeyCopy + partialToUse), 0)
                          case Failure(_) => ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), 1)
                        }
                      case _ =>
                        Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                          case Success(tuple) => (tuple, 0)
                          case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                        }
                    }
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialData: DataStructure = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                          case byteBuff: ByteBuf => Left(byteBuff)
                          case jsonString: String => Right("{" + jsonString + "}")
                        }
                        val codecData =
                          if (!statementsList.equals(fullStatementsList))
                            BosonImpl.inject(partialData, fullStatementsList, injFunction).getCodecData
                          else partialData

                        val partialCodec = partialData match {
                          case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                          case Right(jsonString) => CodecObject.toCodec(jsonString)
                        }
                        val partialToUse = partialCodec.addComma

                        Try(BosonImpl.inject(codecData, statementsList.drop(1), injFunction)) match {
                          case Success(c) =>
                            if (codec.getDataType == 0) {
                              codec.skipChar(back = true)
                              val cToUse = c.addComma
                              ((codecWithKey + cToUse, codecWithKeyCopy + partialToUse), exceptions)
                            } else {
                              codec.skipChar(back = true)
                              ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions)
                            }

                          case Failure(_) =>
                            ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions + 1)
                        }

                      case _ => (processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy), exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec("{" + string + "}")
                        }
                        val partialToUse = partialCodec.addComma
                        Try(BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                          case Success(c) =>
                            val cToUse = c.addComma
                            if (codec.getDataType == 0) {
                              codec.skipChar(back = true)
                              ((codecWithKey + cToUse, processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                            } else {
                              codec.skipChar(back = true)
                              ((codecWithKey + partialToUse, processTypesArray(dataType, codec.duplicate, codecWithKeyCopy)), exceptions)
                            }
                          case Failure(_) => ((processTypesArray(dataType, codec.duplicate, codecWithKeyCopy), processTypesArray(dataType, codec.duplicate, codecWithKeyCopy)), exceptions + 1)
                        }
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec.duplicate, codecWithKeyCopy)), exceptions)
                    }
                  }
                }
              case (x, _, C_END) if isArray && from.toInt <= x.toInt =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec("{" + string + "}")
                        }
                        val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                        val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)

                        val partialToUse = partialCodec.addComma
                        val subCodecToUse = subCodec.addComma

                        if (condition equals UNTIL_RANGE) {
                          if (codec.getDataType == 0) {
                            codec.skipChar(back = true)
                            ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions)
                          } else {
                            codec.skipChar(back = true)
                            ((codecWithKey + subCodecToUse, codecWithKeyCopy + subCodecToUse), exceptions)
                          }
                        } else {
                          val subCodecToUse = subCodec.addComma
                          ((codecWithKey + subCodecToUse, codecWithKeyCopy + subCodecToUse), exceptions)
                        }
                      case _ =>
                        if (exceptions == 0)
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) => (tuple, exceptions)
                            case Failure(_) => ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                        else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      dataType match {
                        case D_BSONARRAY | D_BSONOBJECT =>
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }
                          val interiorObjCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          ((codecWithKey + interiorObjCodec.addComma, codecWithKey + partialCodec.addComma), exceptions)
                        case _ =>
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) => (tuple, exceptions)
                            case Failure(_) => ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }

                          val subCodec =
                            if (!statementsList.equals(fullStatementsList))
                              BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                            else
                              partialCodec

                          val partialToUse = partialCodec.addComma

                          Try(BosonImpl.inject(subCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) => ((codecWithKey + c.addComma, codecWithKey + partialToUse), 0)
                            case Failure(_) => ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)

                      case _ => (processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy), exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }

                          val partialToUse = partialCodec.addComma
                          Try(BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              val cToUse = c.addComma
                              if (condition equals UNTIL_RANGE) {
                                if (codec.getDataType == 0) {
                                  codec.skipChar(back = true)
                                  ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions)
                                } else {
                                  codec.skipChar(back = true)
                                  ((processTypesArray(dataType, codec.duplicate, codecWithKey), codecWithKey + cToUse), exceptions)
                                }
                              } else ((codecWithKey + cToUse, processTypesArray(dataType, codec.duplicate, codecWithKey)), exceptions)

                            case Failure(_) => ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec.duplicate, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }

              case (x, _, C_END) if isArray && from.toInt > x.toInt =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  //this is the case where we haven't yet reached the condition the user sent us
                  //but we still need to check inside this object to see there's a value that matches that condition
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case string: String => CodecObject.toCodec("{" + string + "}")
                      }
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                      val codecComma = modifiedPartialCodec.changeBrackets(dataType, curlyToRect = false)
                      val newCodecResult = codecWithKey + codecComma
                      val newCodecResultCopy = codecWithKeyCopy + codecComma
                      ((newCodecResult, newCodecResultCopy), exceptions)
                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else
                  ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)

              case (x, _, l) if isArray && (from.toInt <= x.toInt && l.toInt >= x.toInt) =>
                if (statementsList.lengthCompare(1) == 0) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String =>
                              if (dataType == D_BSONOBJECT) CodecObject.toCodec("{" + string + "}")
                              else CodecObject.toCodec(string)
                          }
                          val emptyCodec: Codec = createEmptyCodec(codec)
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          //Look inside the current object for cases that match the user given expression
                          val mergedCodec =
                            if (!statementsList.equals(fullStatementsList) && fullStatementsList.head._2.contains(C_DOUBLEDOT)) { //we only want to investigate inside this object if it has the property we're looking for
                              val auxCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                              auxCodec.changeBrackets(dataType)
                            } else modifiedPartialCodec

                          Try(modifierEnd(mergedCodec, dataType, injFunction, emptyCodec, createEmptyCodec(codec))) match {
                            case Success(_) => ((codecWithKey + mergedCodec, codecWithKeyCopy + partialCodec), exceptions)
                            case Failure(_) =>
                              val mergedToUse = mergedCodec.addComma
                              ((codecWithKey + mergedToUse, codecWithKey + mergedToUse), if (condition equals UNTIL_RANGE) exceptions + 1 else exceptions)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        if (exceptions == 0) {
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) => (tuple, exceptions)
                            case Failure(_) => ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      dataType match {
                        case D_BSONARRAY | D_BSONOBJECT =>
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String =>
                              if (dataType == D_BSONOBJECT) CodecObject.toCodec("{" + string + "}")
                              else CodecObject.toCodec(string)
                          }
                          val codecMod = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          ((codecWithKey + codecMod.addComma, codecWithKeyCopy + partialCodec.addComma), exceptions)
                        case _ =>
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) => (tuple, exceptions)
                            case Failure(_) => ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }

                          val mergedCodec =
                            if (!statementsList.equals(fullStatementsList))
                              BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                            else
                              partialCodec
                          Try(BosonImpl.inject(mergedCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) => ((codecWithKey + c.addComma, codecWithKey + partialCodec.addComma), 0)
                            case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), 1)
                      case _ =>
                        (processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy), exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }

                          Try(BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) => ((codecWithKey + c.addComma, processTypesArray(dataType, codec.duplicate, codecWithKey)), exceptions)
                            case Failure(_) => ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec.duplicate, codecWithKey)), exceptions),
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec.duplicate, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }
              case (x, _, l) if isArray && (from.toInt > x.toInt || l.toInt < x.toInt) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)).asInstanceOf[SonArray].info match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case string: String =>
                          if (dataType == D_BSONOBJECT) CodecObject.toCodec("{" + string + "}")
                          else CodecObject.toCodec(string)
                      }
                      val modifiedAuxCodec =
                        if (!statementsList.equals(fullStatementsList) && fullStatementsList.head._2.contains(C_DOUBLEDOT))
                          if (fullStatementsList.head._2.contains(C_DOUBLEDOT)) BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                          else BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                        else partialCodec

                      val modToUse = modifiedAuxCodec.changeBrackets(dataType).addComma
                      ((codecWithKey + modToUse, codecWithKeyCopy + modToUse), exceptions)

                    case _ => ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)

              case (_, _, _) if !isArray =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  codec.getCodecData match {
                    case Left(_) => ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                    case Right(_) =>
                      codec.skipChar(back = true)
                      val key = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)).asInstanceOf[SonString].info.asInstanceOf[String]
                      val codecAux = codec.writeToken(codecWithKey, SonString(CS_STRING, key), isKey = true)
                      val codecAuxCopy = codec.writeToken(codecWithKeyCopy, SonString(CS_STRING, key), isKey = true)

                      val processedCodec = processTypesArray(dataType, codec.duplicate, codecAux)
                      val processedCodecCopy = processTypesArray(dataType, codec, codecAuxCopy)

                      codec.skipChar() // Skip the comma written

                      ((processedCodec, processedCodecCopy), exceptions)
                  }
                } else throw CustomException("*modifyArrayEnd* Not a Array")
            }
            iterateDataStructure(codecResult, codecResultCopy, exceptionsResult)
        }
      }
    }

    val (codecWithoutSize, codecWithoutSizeCopy, exceptions): (Codec, Codec, Int) = iterateDataStructure(createEmptyCodec(codec), createEmptyCodec(codec), 0)

    val codecMerged = codecWithoutSize.writeCodecSize + codecWithoutSize
    val codecFinal = codec.removeTrailingComma(codecMerged, rectBrackets = true)

    val codecMergedCopy = codecWithoutSizeCopy.writeCodecSize + codecWithoutSizeCopy
    val codecFinalCopy = codec.removeTrailingComma(codecMergedCopy, rectBrackets = true)

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
    * @return A Codec containing the alterations made and an Auxiliary Codec
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
                                      resultCodecCopy: Codec)(implicit convertFunction: Option[TupleList => T] = None): (Codec, Codec) = {
    dataType match {

      case D_FLOAT_DOUBLE =>
        val token = codec.readToken(SonNumber(CS_DOUBLE))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)).asInstanceOf[SonString].info.asInstanceOf[String]
        val strSizeCodec = codec.writeToken(resultCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        val strSizeCodecCopy = codec.writeToken(resultCodecCopy, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        val codecWithLastByte = codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        val codecWithLastByteCopy = codec.writeToken(strSizeCodecCopy, SonString(CS_STRING, value0)) + strSizeCodecCopy.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        (codecWithLastByte, codecWithLastByteCopy)

      case D_BSONOBJECT =>
        val codecObj = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
          case jsonString: String => CodecObject.toCodec(jsonString)
        }
        val auxCodec = modifyArrayEndWithKey(statementList, codecObj, fieldID, injFunction, condition, from, to)
        (resultCodec + auxCodec, resultCodecCopy + auxCodec)

      case D_BSONARRAY =>
        val codecArr = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
          case jsonString: String => CodecObject.toCodec(jsonString)
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
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
          case byte: Byte => byte == 1
        }
        (codec.writeToken(resultCodec, SonBoolean(CS_BOOLEAN, value0)), codec.writeToken(resultCodecCopy, SonBoolean(CS_BOOLEAN, value0)))
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

    /**
      * Recursive function to iterate through the given data structure and  return the modified codec
      *
      * @param currentCodec     - a codec to write the modified information into
      * @param currentCodecCopy - An Auxiliary codec to where we write the values in case the previous cycle was the last one
      * @return a codec containig the modifications made
      */
    def iterateDataStructure(currentCodec: Codec, currentCodecCopy: Codec): (Codec, Codec) = {
      if ((codec.getReaderIndex - startReaderIndex) >= originalSize) (currentCodec, currentCodecCopy)
      else {
        val (dataType, codecWithDataType) = readWriteDataType(codec, currentCodec)
        val codecWithDataTypeCopy: Codec = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        val (codecTo, codecUntil): (Codec, Codec) = dataType match {
          case 0 => (codecWithDataType, codecWithDataTypeCopy)
          case _ =>
            val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)).asInstanceOf[SonString].info.asInstanceOf[String]
            val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true).asInstanceOf[SonBoolean].info.asInstanceOf[Byte]

            val codecWithKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key), isKey = true)
            val resCodec = codec.writeToken(codecWithKey, SonNumber(CS_BYTE, b), ignoreForJson = true)

            val codecWithKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key), isKey = true)
            val resCodecCopy = codec.writeToken(codecWithKeyCopy, SonNumber(CS_BYTE, b), ignoreForJson = true)

            key match {
              //In case we the extracted elem name is the same as the one we're looking for (or they're halfwords) and the
              //datatype is a BsonArray
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType == D_BSONARRAY =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY)).asInstanceOf[SonArray].info match {
                      case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                      case jsonString: String => CodecObject.toCodec("{" + jsonString + "}")
                    }
                    val newCodec = modifyArrayEnd(statementsList, partialCodec, injFunction, condition, from, to, statementsList, dataType)

                    val newInjectCodec1 = BosonImpl.inject(newCodec.getCodecData, statementsList, injFunction)
                    val newInjectCodec = newInjectCodec1.getCodecData match {
                      case Left(_) => newInjectCodec1
                      case Right(jsonString) => CodecObject.toCodec("[" + jsonString.substring(1, jsonString.length - 1) + "]")
                    }
                    (resCodec + newInjectCodec, resCodecCopy + newInjectCodec.duplicate)
                  } else {
                    val newCodec: Codec = modifyArrayEnd(statementsList, codec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)).asInstanceOf[SonArray].info match {
                      case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                      case jsonString: String => CodecObject.toCodec("{" + jsonString + "}")
                    }
                    val newCodec = modifyArrayEnd(statementsList.drop(1), partialCodec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  } else {
                    val newCodec = modifyArrayEnd(statementsList.drop(1), codec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  }
                }
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType != D_BSONARRAY =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr])
                  processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                else
                  (processTypesArray(dataType, codec.duplicate, resCodec), processTypesArray(dataType, codec, resCodecCopy))

              case _ =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  codec.getCodecData match {
                    case Left(_) => processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)

                    case Right(jsonString) =>
                      if ((jsonString.charAt(codec.getReaderIndex - 1) equals ',') || (jsonString.charAt(codec.getReaderIndex - 1) equals ']')) {
                        //If this happens key is not key but value
                        codec.setReaderIndex(codec.getReaderIndex - key.length - 3)
                        val keyType = codec.getDataType
                        processTypesArrayEnd(statementsList, fieldID, keyType, codec, injFunction, condition, from, to, codecWithDataType, codecWithDataTypeCopy)
                      } else processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                  }
                } else (processTypesArray(dataType, codec.duplicate, resCodec), processTypesArray(dataType, codec, resCodecCopy))

              //If we found the desired elem but the dataType is not an array, or if we didn't find the desired elem
            }
        }
        iterateDataStructure(codecTo, codecUntil)
      }
    }

    val (codecWithoutSize, codecWithoutSizeCopy): (Codec, Codec) = iterateDataStructure(createEmptyCodec(codec), createEmptyCodec(codec))

    val codecMerged = codecWithoutSize.writeCodecSize + codecWithoutSize
    val codecMergedCopy = codecWithoutSizeCopy.writeCodecSize + codecWithoutSizeCopy

    val finalCodec = codec.removeTrailingComma(codecMerged)
    val finalCodecCopy = codec.removeTrailingComma(codecMergedCopy)

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
    val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)).asInstanceOf[SonString].info.asInstanceOf[String]
    val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true).asInstanceOf[SonBoolean].info.asInstanceOf[Byte]
    val codecWithKey = codec.writeToken(writableCodec, SonString(CS_STRING, key), isKey = true)
    (codec.writeToken(codecWithKey, SonNumber(CS_BYTE, b), ignoreForJson = true), key)
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
    emptyCodec.setWriterIndex(0) //Sets the writerIndex of the newly created codec to 0 (Initially it starts at 256 for CodecBson, so we need o reset it)
    emptyCodec
  }

  /**
    * Method that extracts a list of tuples containing the name of a field of the object the value for that field
    *
    * @param value - Object of type T encoded in a array of bytes
    * @return a List of tuples containing the name of a field of the object the value for that field
    */
  private def extractTupleList(value: Either[Array[Byte], String]): TupleList = {
    val codec: Codec = value match {
      case Left(byteArr) => CodecObject.toCodec(Unpooled.buffer(byteArr.length).writeBytes(byteArr))

      case Right(jsonString) => CodecObject.toCodec(jsonString)
    }
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize

    /**
      * Iterate inside the object passed as parameter in order to extract all of its field names and filed values
      *
      * @param writableList - The list to store the extracted field names and field values
      * @return A TupleList containing the extracted field names and field values
      */
    def iterateObject(writableList: TupleList): TupleList = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableList
      else {
        val dataType: Int = codec.readDataType()
        dataType match {
          case 0 => iterateObject(writableList)
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
            iterateObject(writableList :+ (fieldName, fieldValue))
        }
      }
    }

    iterateObject(List())
  }

  /**
    * Private method that iterates through the fields of a given object and creates a list of tuple from the field names
    * and field values
    *
    * @param modifiedValue - the object to be iterated
    * @tparam T - The type T of the object to be iterated
    * @return A list of tuples consisting in pairs of field names and field values
    */
  private def toTupleList[T](modifiedValue: T): TupleList = {
    val tupleArray = for {
      field <- modifiedValue.getClass.getDeclaredFields //Iterate through this object's fields
      if !field.getName.equals("$outer") //remove shapeless add $outer param
    } yield {
      field.setAccessible(true) //make this object accessible so we can get its value
      val attributeClass = field.get(modifiedValue).getClass.getSimpleName
      if (!SCALA_TYPES_LIST.contains(attributeClass.toLowerCase)) {
        val otherTupleList = toTupleList(field.get(modifiedValue)).asInstanceOf[Any]
        (field.getName, otherTupleList)
      } else
        (field.getName, field.get(modifiedValue).asInstanceOf[Any])

    }
    tupleArray.toList
  }

  /**
    * Private method that receives a list of tuples and encodes them into a byte array or Json String (to be implemented)
    *
    * @param tupleList - List of tuples to be encoded
    * @return An array of bytes representing the encoded list of tuples
    */
  private def encodeTupleList(tupleList: TupleList, value: Any): Either[Array[Byte], String] = {
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
    * @return A Tuple containig both the read data type and the written codec
    */
  private def readWriteDataType(codec: Codec, writeCodec: Codec, formerType: Int = 0): (Int, Codec) = {
    val dataType: Int = codec.readDataType(formerType)
    val codecWithDataType = codec.writeToken(writeCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
    (dataType, codecWithDataType)
  }
}