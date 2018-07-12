package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import BosonImpl.{DataStructure, StatementsList}
import io.zink.boson.bson.bsonImpl.bsonLib.BsonObject
import io.zink.boson.bson.codec.impl.{CodecBson, CodecJson}

import scala.util.{Failure, Success, Try}

private[bsonImpl] object BosonInjectorImpl {

  private type TupleList = List[(String, Any)]

  /**
    * Function that recursively searches for the keys that are of interest to the injection
    *
    * @param statementsList - A list with pairs that contains the key of interest and the type of operation
    * @param codec          - Structure from which we are reading the old values
    * @param fieldID        - Name of the field of interest
    * @param injFunction    - The injectino function to be applied
    * @tparam T - The type of input and output of the injection function
    * @return A Codec containing the alterations made
    */
  def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec - the codec to write the modified information into
      * @param startReader  - the initial reader index of the codec passed in modifyAll
      * @param originalSize - the original size of the codec passed in modifyAll
      * @return A Codec containing the alterations made
      */
    def writeCodec(currentCodec: Codec, startReader: Int, originalSize: Int): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) currentCodec
      else {
        val dataType: Int = codec.readDataType()
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        val newCodec = dataType match {
          case 0 =>
            writeCodec(codecWithDataType, startReader, originalSize)
          case _ =>
            val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)

            key match {
              case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted) =>
                if (statementsList.lengthCompare(1) == 0) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
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

                        val subCodec = BosonImpl.inject(partialData, statementsList, injFunction)
                        val modifiedCodec = modifierAll(subCodec, codecWithKey, dataType, injFunction)
                        modifiedCodec.removeEmptySpace
                        modifiedCodec
                      case _ =>
                        modifierAll(codec, codecWithKey, dataType, injFunction)
                    }
                  } else {
                    modifierAll(codec, codecWithKey, dataType, injFunction)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
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
                        val modifiedSubCodec = BosonImpl.inject(partialData, statementsList.drop(1), injFunction)
                        val subCodec = BosonImpl.inject(modifiedSubCodec.getCodecData, statementsList, injFunction)
                        codecWithKey + subCodec
                      case _ =>
                        processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
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

                          case Right(jsonString) =>
                            val token = if (dataType == D_BSONOBJECT) SonObject(CS_OBJECT_WITH_SIZE) else SonArray(CS_ARRAY_WITH_SIZE)
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
                  if (isCodecJson(codec)) {
                    codec.getCodecData match {
                      case Right(jsonString) =>
                        if (jsonString.charAt(0).equals('[')) {
                          codec.setReaderIndex(codec.getReaderIndex - (key.length + 4)) //Go back the size of the key plus a ":", two quotes and the beginning "{"
                          val processedObj = processTypesAll(statementsList, dataType, codec, codecWithDataType, fieldID, injFunction)
                          processedObj.getCodecData match {
                            case Right(js) => CodecObject.toCodec(js + ",")
                          }

                        } else processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                    }
                  } else processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                }
                else processTypesArray(dataType, codec, codecWithKey)
            }
        }
        writeCodec(newCodec, startReader, originalSize)
      }
    }

    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val emptyCodec: Codec = createEmptyCodec(codec)

    val codecWithoutSize = writeCodec(emptyCodec, startReader, originalSize)
    val codecWithLastByte = if (codec.getReaderIndex == originalSize && (codecWithoutSize.getWriterIndex == codec.getReaderIndex - 5)) {
      codecWithoutSize + emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
    } else codecWithoutSize

    val finalSize = codecWithLastByte.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize), ignoreForJson = true)
    val mergedCodecs = codecWithSize + codecWithLastByte
    mergedCodecs.getCodecData match {
      case Left(_) => mergedCodecs
      case Right(jsonString) =>
        if (jsonString.charAt(jsonString.length - 1).equals(',')) {
          codec.getCodecData match {
            case Right(codecString) =>
              if (codecString.charAt(0).equals('[')) CodecObject.toCodec(s"[${jsonString.dropRight(1)}]") //Remove trailing comma
              else CodecObject.toCodec(s"{${jsonString.dropRight(1)}}") //Remove trailing comma
          }
        } else CodecObject.toCodec(s"{$jsonString}")
    }
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

    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val emptyDataStructure: Codec = createEmptyCodec(codec)

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to write the modified information into
      * @return A Codec containing the alterations made
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val dataType: Int = codec.readDataType()
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true) //write the read byte to a Codec
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

    val returnCodec = emptyDataStructure.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, size), ignoreForJson = true) + codecWithoutSize

    if (isCodecJson(returnCodec)) {
      returnCodec.getCodecData match {
        case Right(jsonString) =>
          if (jsonString.charAt(jsonString.length - 1).equals(','))
            CodecObject.toCodec(s"{${jsonString.dropRight(1)}}")
          else CodecObject.toCodec(s"{$jsonString}")
      }
    } else returnCodec
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
    //If it's a CodecJson we need to skip the "[" character
    if (isCodecJson(codec))
      codec.setReaderIndex(codec.getReaderIndex + 1)


    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to write the information into
      * @return A codec containing the alterations made
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val dataType = codec.readDataType()
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true) //write the read byte to a Codec
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType)
          case _ =>
            dataType match {
              case D_BSONOBJECT =>
                val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)
                val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
                  case SonObject(_, result) => result match {
                    case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                    case jsonString: String => CodecObject.toCodec("{\"" + key + "\":" + jsonString) //For CodecJson the "{" and the key is already read, we need to add it back
                  }
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
                      if (isCodecJson(codec)) {
                        val modCodecAux: Codec = modifiedSubCodec.getCodecData match {
                          case Right(jsonString) => CodecObject.toCodec(jsonString + ",")
                        }
                        iterateDataStructure(writableCodec + modCodecAux)
                      }
                      else iterateDataStructure(codecWithKey + modifiedSubCodec)

                    } else {
                      if (isCodecJson(codec)) {
                        val modCodecAux: Codec = modifiedCodec.getCodecData match {
                          case Right(jsonString) => CodecObject.toCodec(jsonString + ",")
                        }
                        iterateDataStructure(writableCodec + modCodecAux)
                      }
                      else iterateDataStructure(codecWithKey + modifiedCodec)
                    }

                  } else {
                    val modifiedCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)

                    if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                      val modifiedSubCodec = BosonImpl.inject(modifiedCodec.getCodecData, statementsList, injFunction)
                      if (isCodecJson(codec)) {
                        val modCodecAux: Codec = modifiedSubCodec.getCodecData match {
                          case Right(jsonString) => CodecObject.toCodec(jsonString + ",")
                        }
                        iterateDataStructure(writableCodec + modCodecAux)
                      }
                      else iterateDataStructure(codecWithKey + modifiedSubCodec)

                    } else iterateDataStructure(codecWithKey + modifiedCodec)
                  }

                } else {
                  if (isCodecJson(codec)) {
                    val modCodecAux: Codec = partialCodec.getCodecData match {
                      case Right(jsonString) => CodecObject.toCodec(jsonString + ",")
                    }
                    iterateDataStructure(writableCodec + modCodecAux)
                  }
                  else iterateDataStructure(codecWithKey + partialCodec)
                }

              case _ =>
                val codecToWrite = if (!isCodecJson(codec)) {
                  writeKeyAndByte(codec, codecWithDataType)._1
                } else writableCodec
                iterateDataStructure(processTypesArray(dataType, codec, codecToWrite)) //If its not an object then it will not have the element we're looking for inside it
            }
        }
      }
    }

    val modifiedSubCodec = iterateDataStructure(createEmptyCodec(codec)) //Search for the element of interest it and try to apply the injection function to it
    val modifiedSubSize = modifiedSubCodec.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val codecWithSize = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, modifiedSubSize), ignoreForJson = true)
    val modCodecWithSize = codecWithSize + modifiedSubCodec //Add the size of the resulting sub-codec (plus 4 bytes) with the actual sub-codec
    val updatedCodec: Codec =
      if (isCodecJson(modCodecWithSize)) {
        modCodecWithSize.getCodecData match {
          case Right(jsonString) =>
            if (jsonString.charAt(jsonString.length - 1).equals(','))
              CodecObject.toCodec(s"[${jsonString.dropRight(1)}]")
            else CodecObject.toCodec(s"[$jsonString]")
        }
      } else modCodecWithSize
    writableCodec + updatedCodec //merge the modified sub-codec with the rest of the codec passed to this function as an argument
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
        case 0 => //TODO what do we do in this case
        case _ =>
          key = codec.readToken(SonString(CS_NAME)) match {
            case SonString(_, keyString) => keyString.asInstanceOf[String]
          }
          dataType match {
            case D_ZERO_BYTE => //TODO what do we do in this case

            case D_FLOAT_DOUBLE => codec.readToken(SonNumber(CS_DOUBLE))

            case D_ARRAYB_INST_STR_ENUM_CHRSEQ => codec.readToken(SonString(CS_STRING))

            case D_BSONOBJECT | D_BSONARRAY =>
              if (isCodecJson(codec)) {
                codec.setReaderIndex(codec.getReaderIndex + 1) //Skip the ":" character
              }
              codec.readToken(SonObject(CS_OBJECT_WITH_SIZE))

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
  def rootInjection[T](codec: Codec, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = {
    codec.getCodecData match {
      case Left(byteBuf) =>
        val bsonBytes: Array[Byte] = byteBuf.array() //extract the bytes from the bytebuf
      /*
        We first cast the result of applyFunction as String because when we input a byte array we apply the injFunction to it
        as a String. We return that modified String and convert it back to a byte array in order to create a ByteBuf from it
       */
      val modifiedBytes: Array[Byte] = applyFunction(injFunction, bsonBytes, fromRoot = true).asInstanceOf[Array[Byte]]
        val newBuf = Unpooled.buffer(modifiedBytes.length).writeBytes(modifiedBytes) //create a new ByteBuf from those bytes
        CodecObject.toCodec(newBuf)

      case Right(jsonString) =>
        val modifiedString: String = applyFunction(injFunction, jsonString, fromRoot = true).asInstanceOf[String]
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
  private def processTypesHasElem[T](statementsList: StatementsList, dataType: Int, fieldID: String, elem: String, codec: Codec, resultCodec: Codec, injFunction: T => T)(implicit convertFunction: Option[TupleList => T] = None): Codec = dataType match {
    case D_BSONOBJECT =>
      val bsonObjectCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
        case SonObject(_, data) => data match {
          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
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

    case D_FLOAT_DOUBLE =>
      val codecToReturn = codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      codecToReturn.removeEmptySpace
      codecToReturn

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      val value0 = codec.readToken(SonString(CS_STRING)) match {
        case SonString(_, data) => data.asInstanceOf[String]
      }
      val codecWithValue = codec.writeToken(createEmptyCodec(codec), SonString(CS_STRING, value0))
      val codecWithSize = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
      val codecWithZeroByte = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

      ((resultCodec + codecWithSize) + codecWithValue) + codecWithZeroByte

    case D_INT =>
      val codecToReturn = codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_INTEGER)))
      codecToReturn.removeEmptySpace
      codecToReturn

    case D_LONG =>
      val codecToReturn = codec.writeToken(resultCodec, codec.readToken(SonNumber(CS_LONG)))
      codecToReturn.removeEmptySpace
      codecToReturn

    case D_BOOLEAN =>
      val value0 = codec.readToken(SonBoolean(CS_BOOLEAN)).asInstanceOf[SonBoolean].info match {
        case byte: Byte => byte == 1
      }
      val codecToReturn = codec.writeToken(resultCodec, SonBoolean(CS_BOOLEAN, value0))
      codecToReturn.removeEmptySpace
      codecToReturn

    case D_NULL => resultCodec //TODO what do we do in this case
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
      case D_ZERO_BYTE =>
        currentResCodec
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
      case D_BSONOBJECT =>
        val value0 = codec.readToken(SonObject(CS_OBJECT_INJ)) match {
          case SonObject(_, content) =>
            if (isCodecJson(codec)) {
              val str = content.asInstanceOf[String]
              if (!str.charAt(0).equals('{')) "{" + str else str
            } else content.asInstanceOf[ByteBuf].array
        }
        codec.writeToken(currentResCodec, SonObject(CS_OBJECT, value0))
      case D_BSONARRAY =>
        val value0 = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
          case SonArray(_, content) => if (isCodecJson(codec)) content.asInstanceOf[String] else content.asInstanceOf[ByteBuf].array
        }
        codec.writeToken(currentResCodec, SonArray(CS_ARRAY, value0))
      case D_NULL =>
        currentResCodec
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
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
          case value: String =>
            val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
            codec.writeToken(strSizeCodec, SonString(CS_STRING, value)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

          case valueInstant: Instant =>
            val value = valueInstant.toString
            val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
            codec.writeToken(strSizeCodec, SonString(CS_STRING, value)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        }

      case D_BSONOBJECT =>
        codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
          case SonObject(_, data) => data match {
            case byteBuf: ByteBuf =>
              applyFunction(injFunction, byteBuf.array()) match {
                case arr: Array[Byte] => codec.writeToken(currentResCodec, SonArray(CS_ARRAY, arr))
              }
            case str: String =>
              applyFunction(injFunction, str) match {
                case resString: String => codec.writeToken(currentResCodec, SonString(CS_STRING_NO_QUOTES, resString))
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
          case SonBoolean(_, data) => data match {
            case byte: Byte => byte == 1
          }
        }
        applyFunction(injFunction, value0) match {
          case value: Boolean => codec.writeToken(currentResCodec, SonBoolean(CS_BOOLEAN, value))
        }

      case D_NULL =>
        throw CustomException(s"NULL field. Can not be changed")

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
      val value0 = codec.readToken(SonString(CS_STRING)) match {
        case SonString(_, data) => data.asInstanceOf[String]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case str: String =>
          val strSizeCodec = codec.writeToken(codecRes, SonNumber(CS_INTEGER, str.length + 1), ignoreForJson = true)
          codec.writeToken(strSizeCodec, SonString(CS_STRING, str)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

        case value: Instant =>
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
        case SonBoolean(_, data) => data match {
          case byte: Byte => byte == 1
        }
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

    case D_NULL =>
      throw CustomException(s"NULL field. Can not be changed")
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
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)

      case D_BSONOBJECT =>
        val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
          case SonObject(_, result) => result match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        currentResCodec + codecAux

      case D_BSONARRAY =>
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
          case SonArray(_, result) => result match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)

        currentResCodec + codecAux

      case D_NULL => currentResCodec

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
  private def applyFunction[T](injFunction: T => T, value: Any, fromRoot: Boolean = false)(implicit convertFunction: Option[TupleList => T] = None): T = {

    def throwException(className: String): T = throw CustomException(s"Type Error. Cannot Cast ${className} inside the Injector Function.")

    Try(injFunction(value.asInstanceOf[T])) match {
      case Success(modifiedValue) =>
        modifiedValue

      case Failure(_) => value match {
        case double: Double =>
          Try(injFunction(double.toFloat.asInstanceOf[T])) match { //try with the value being a Double
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }

        case jsonString: String if fromRoot => //In case the User passed a JsonString in the root
          Try(injFunction(jsonString.asInstanceOf[T])) match {
            case Success(modifiedValue) =>
              modifiedValue.asInstanceOf[T]

            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }

        case byteArr: Array[Byte] if fromRoot => //In case the User passed a BsonObject/BsonArray in the root
          Try(injFunction(byteArr.asInstanceOf[T])) match {
            case Success(modifiedValue) =>
              modifiedValue.asInstanceOf[T]

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
            case Success(modifiedValue) =>
              val modifiedTupleList = toTupleList(modifiedValue)
              encodeTupleList(modifiedTupleList, byteArrOrJson) match {
                case Left(modByteArr) => modByteArr.asInstanceOf[T]
                case Right(modJsonString) => modJsonString.asInstanceOf[T]
              }

            case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
          }


        case byteArr: Array[Byte] =>
          Try(injFunction(new String(byteArr).asInstanceOf[T])) match { //try with the value being a Array[Byte]
            case Success(modifiedValue) =>
              modifiedValue.asInstanceOf[T]

            case Failure(_) =>
              Try(injFunction(Instant.parse(new String(byteArr)).asInstanceOf[T])) match { //try with the value being an Instant
                case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

                case Failure(_) => throwException(value.getClass.getSimpleName.toLowerCase)
              }
          }

        case str: String =>
          Try(injFunction(Instant.parse(str).asInstanceOf[T])) match {
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

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
        case jsonString: String => (CodecObject.toCodec("{" + jsonString + "}"), 4)
      }
    }
    val codecArrayEnd: Codec = (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, to.toString, fullStatementsList = statementsList, formerType = formerType)
      case (EMPTY_KEY, from, expr, _) =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, fullStatementsList = statementsList, formerType = formerType)
      case (nonEmptyKey, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString, to.toString)
      case (nonEmptyKey, from, expr, _) =>
        modifyArrayEndWithKey(statementsList, arrayTokenCodec, nonEmptyKey, injFunction, expr, from.toString)
    }
    codecArrayEnd
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
    val startReaderIndex = codec.getReaderIndex
    val originalSize = codec.readSize
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
        val dataType: Int = codec.readDataType(formerType)
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        val codecWithDataTypeCopy = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType, codecWithDataTypeCopy, exceptions)
          case _ =>
            val key: String = codec.getCodecData match {
              case Left(_) => codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
                case SonString(_, keyString) => keyString.asInstanceOf[String]
              }
              case Right(_) =>
                counter += 1
                counter.toString
            }
            val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true) match {
              case SonBoolean(_, result) => result.asInstanceOf[Byte]
            }

            val codecWithoutKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key), ignoreForJson = true)
            val codecWithKey = codec.writeToken(codecWithoutKey, SonNumber(CS_BYTE, b), ignoreForJson = true)

            val codecWithoutKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key), ignoreForJson = true)
            val codecWithKeyCopy = codec.writeToken(codecWithoutKeyCopy, SonNumber(CS_BYTE, b), ignoreForJson = true)

            val isArray = formerType == 4 || key.forall(b => b.isDigit)

            val ((codecResult, codecResultCopy), exceptionsResult): ((Codec, Codec), Int) = (key, condition, to) match {
              case (x, C_END, _) if isArray =>
                //expections.clear()
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {

                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val partialData: DataStructure = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                          case SonArray(_, value) => value match {
                            case byteBuf: ByteBuf => Left(byteBuf)
                            case jsonString: String => Right(jsonString)
                          }
                        }

                        val partialCodec = {
                          if (statementsList.head._1.isInstanceOf[ArrExpr])
                            BosonImpl.inject(partialData, statementsList, injFunction)
                          else {
                            partialData match {
                              case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                              case Right(jsonString) => CodecObject.toCodec(jsonString)
                            }
                          }
                        }

                        val partialCodecModified = BosonImpl.inject(partialData, statementsList, injFunction)
                        val subPartial = BosonImpl.inject(partialCodecModified.getCodecData, fullStatementsList, injFunction)
                        ((codecWithKeyCopy + subPartial, codecWithKeyCopy + partialCodec), 0)
                      case _ =>
                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                            case Success(tuple) => (tuple, 0)
                            case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        (codecTuple, exceptionsReturn)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                          case SonArray(_, value) => value match {
                            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                            case string: String => CodecObject.toCodec("{" + string + "}")
                          }
                        }
                        Try(BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)) match {
                          case Success(c) =>
                            if (isCodecJson(codec)) {
                              val cWithComma = c.getCodecData match {
                                case Right(str) => CodecObject.toCodec(str + ",")
                              }

                              val partialWithComma = partialCodec.getCodecData match {
                                case Right(str) => CodecObject.toCodec(str + ",")
                              }

                              ((codecWithKeyCopy + cWithComma, codecWithKeyCopy + partialWithComma), 0)
                            } else ((codecWithKeyCopy + c, codecWithKeyCopy + partialCodec), 0)
                          case Failure(_) => ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), 1)
                        }
                      case _ =>
                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                            case Success(tuple) => (tuple, 0)
                            case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        (codecTuple, exceptionsReturn)
                    }
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialData: DataStructure = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                          case SonArray(_, value) => value match {
                            case byteBuff: ByteBuf => Left(byteBuff)
                            case jsonString: String => Right("{" + jsonString + "}")
                          }
                        }
                        //                        val searchInsidePartial = BosonImpl.inject(partialData, statementsList.drop(1), injFunction)

                        val codecData =
                          if (!statementsList.equals(fullStatementsList))
                            BosonImpl.inject(partialData, fullStatementsList, injFunction).getCodecData
                          else
                            partialData

                        val partialCodec = partialData match {
                          case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                          case Right(jsonString) => CodecObject.toCodec(jsonString)
                        }

                        //                        val dataCodec = codecData match {
                        //                          case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                        //                          case Right(jsonString) => CodecObject.toCodec(jsonString)
                        //                        }
                        //                        val dataCodecToUse =
                        //                          if (isCodecJson(codec)) {
                        //                            dataCodec.getCodecData match {
                        //                              case Right(str) => CodecObject.toCodec(str + ",")
                        //                            }
                        //                          } else dataCodec

                        val partialToUse =
                          if (isCodecJson(codec)) {
                            partialCodec.getCodecData match {
                              case Right(str) => CodecObject.toCodec(str + ",")
                            }
                          } else partialCodec

                        Try(BosonImpl.inject(codecData, statementsList.drop(1), injFunction)) match {
                          case Success(c) =>
                            if (codec.getDataType == 0) {
                              if (isCodecJson(codec)) {
                                codec.setReaderIndex(codec.getReaderIndex - 1) //getDataType on CodecJson still consumes a byte
                                val cWithComma: Codec = c.getCodecData match {
                                  case Right(str) => CodecObject.toCodec(str + ",")
                                }
                                ((codecWithKey + cWithComma, codecWithKeyCopy + partialToUse), exceptions)
                              } else ((codecWithKey + c, codecWithKeyCopy + partialToUse), exceptions)
                            } else {
                              if (isCodecJson(codec))
                                codec.setReaderIndex(codec.getReaderIndex - 1) //getDataType on CodecJson still consumes a byte
                              ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions)
                            }

                          case Failure(_) =>
                            ((codecWithKey + partialToUse, codecWithKeyCopy + partialToUse), exceptions + 1)
                        }

                      case _ =>
                        val codecTuple = processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy)
                        (codecTuple, exceptions)
                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val newCodec: Codec = codecWithKeyCopy.duplicate
                        Try(BosonImpl.inject(codec.getCodecData, statementsList.drop(1), injFunction)) match {
                          case Success(c) => ((c, processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                          case Failure(_) => ((processTypesArray(dataType, codec.duplicate, newCodec), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                        }
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                    }
                  }
                }
              case (x, _, C_END) if isArray && from.toInt <= x.toInt =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val emptyCodec: Codec = createEmptyCodec(codec)
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)

                          if (condition equals UNTIL_RANGE) {
                            val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                              Try(modifierEnd(modifiedPartialCodec, dataType, injFunction, emptyCodec, emptyCodec.duplicate)) match {
                                case Success(tuple) =>
                                  ((codecWithKey + tuple._1, codecWithKeyCopy + tuple._2), exceptions)
                                case Failure(_) =>
                                  ((codecWithKey + partialCodec, codecWithKeyCopy + partialCodec), exceptions + 1)
                              }
                            (codecTuple, exceptionsReturn)
                          } else ((codecWithKey + subCodec, codecWithKeyCopy + subCodec), exceptions)

                        } else
                          ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        if (exceptions == 0) {
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(_) =>
                              ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      dataType match {
                        case D_BSONARRAY | D_BSONOBJECT =>
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec("{" + string + "}")
                            }
                          }
                          val interiorObjCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          if (isCodecJson(codec)) {
                            val interiorWithComma = interiorObjCodec.getCodecData match {
                              case Right(str) => CodecObject.toCodec(str + ",")
                            }
                            val partialWithComma = partialCodec.getCodecData match {
                              case Right(str) => CodecObject.toCodec(str + ",")
                            }
                            ((codecWithKey + interiorWithComma, codecWithKey + partialWithComma), exceptions)
                          } else ((codecWithKey + interiorObjCodec, codecWithKey + partialCodec), exceptions)
                        case _ =>
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(_) =>
                              ((codecWithKey, codecWithKey), exceptions + 1)
                          }
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                          val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                          Try(BosonImpl.inject(subCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((codecWithKey + subCodec, codecWithKey + partialCodec), 0)
                            case Failure(_) =>
                              ((codecWithKey + partialCodec, codecWithKeyCopy + partialCodec), 1)
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
                          Try(BosonImpl.inject(codec.duplicate.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(_) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }

              case (x, _, C_END) if isArray && from.toInt > x.toInt =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  //this is the case where we haven't yet reached the condition the user sent us
                  //but we still need to check inside this object to see there's a value that matches that condition
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)

                      val newCodecResult = codecWithKey + modifiedPartialCodec
                      val newCodecResultCopy = codecWithKeyCopy + modifiedPartialCodec
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
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec(string)
                            }
                          }
                          val emptyCodec: Codec = createEmptyCodec(codec)
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)

                          //Look inside the curent object for cases that match the user given expression
                          val mergedCodec =
                            if (!statementsList.equals(fullStatementsList)) //we only want to investigate inside this object if it has the property we're looking for
                              BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                            else
                              modifiedPartialCodec

                          Try(modifierEnd(mergedCodec, dataType, injFunction, emptyCodec, createEmptyCodec(codec))) match { //emptyCodec.duplicate
                            case Success(_) =>
                              ((codecWithKey, codecWithKeyCopy), exceptions)
                            case Failure(_) =>
                              ((codecWithKey + mergedCodec, codecWithKey + mergedCodec), if (condition equals UNTIL_RANGE) exceptions + 1 else exceptions)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, newCodecCopy)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(_) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                    }
                  } else {
                    if (exceptions == 0) {
                      dataType match {
                        case D_BSONARRAY | D_BSONOBJECT =>
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec("{" + string + "}")
                            }
                          }
                          val codecMod = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          if (isCodecJson(codec)) {
                            val codecModWithComma = codecMod.getCodecData match {
                              case Right(str) => CodecObject.toCodec(str + ",")
                            }

                            val partialCodecWithComma = partialCodec.getCodecData match {
                              case Right(str) => CodecObject.toCodec(str + ",")
                            }
                            ((codecWithKey + codecModWithComma, codecWithKeyCopy + partialCodecWithComma), exceptions)
                          } else ((codecWithKey + codecMod, codecWithKeyCopy + partialCodec), exceptions)
                        case _ =>
                          val newCodecCopy = codecWithKey.duplicate
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                            case Success(tuple) =>
                              (tuple, exceptions)
                            case Failure(_) =>
                              ((codecWithKey, newCodecCopy), exceptions + 1)
                          }
                      }
                    } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                            case SonArray(_, value) => value match {
                              case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                              case string: String => CodecObject.toCodec("{" + string + "}")
                            }
                          }

                          val mergedCodec =
                            if (!statementsList.equals(fullStatementsList))
                              BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                            else
                              partialCodec

                          Try(BosonImpl.inject(mergedCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              if (isCodecJson(codec)) {
                                val cWithComma = c.getCodecData match {
                                  case Right(str) => CodecObject.toCodec(str + ",")
                                }
                                val partialWithComma = partialCodec.getCodecData match {
                                  case Right(str) => CodecObject.toCodec(str + ",")
                                }

                                ((codecWithKey + cWithComma, codecWithKey + partialWithComma), 0)
                              } else
                                ((codecWithKey + c, codecWithKey + partialCodec), 0)
                            case Failure(_) =>
                              ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), 1)
                      case _ =>
                        val codecTuple = processTypesArrayEnd(statementsList, EMPTY_KEY, dataType, codec, injFunction, condition, from, to, codecWithKey, codecWithKeyCopy)
                        (codecTuple, exceptions)

                    }
                  } else {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        if (exceptions == 0) {
                          val newCodecCopy = codecWithKey.duplicate
                          Try(BosonImpl.inject(codec.duplicate.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(_) =>
                              ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, newCodecCopy)), exceptions + 1)
                          }
                        } else ((codecWithKey, codecWithKeyCopy), exceptions + 1)
                      case _ =>
                        ((processTypesArray(dataType, codec, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
                    }
                  }
                }
              case (x, _, l) if isArray && (from.toInt > x.toInt || l.toInt < x.toInt) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec("{" + string + "}")
                        }
                      }
                      val modifiedPartialCodec =
                        if (!statementsList.equals(fullStatementsList))
                          BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                        else
                          partialCodec
                      if (isCodecJson(codec)) {
                        val modPartWithComma = modifiedPartialCodec.getCodecData match {
                          case Right(str) => CodecObject.toCodec(str + ",")
                        }
                        ((codecWithKey + modPartWithComma, codecWithKeyCopy + modPartWithComma), exceptions)
                      } else ((codecWithKey + modifiedPartialCodec, codecWithKeyCopy + modifiedPartialCodec), exceptions)

                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions) // Exceptions

              case (x, _, l) if !isArray =>
                if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                      val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                      ((codecWithKey + subCodec, codecWithKeyCopy + subCodec), exceptions)
                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else
                  throw CustomException("*modifyArrayEnd* Not a Array")
            }
            iterateDataStructure(codecResult, codecResultCopy, exceptionsResult)
        }
      }
    }

    val emptyCodec: Codec = createEmptyCodec(codec)

    val (codecWithoutSize, codecWithoutSizeCopy, exceptions): (Codec, Codec, Int) = iterateDataStructure(emptyCodec, createEmptyCodec(codec), 0)

    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize), ignoreForJson = true)

    val codecMerged = codecWithSize + codecWithoutSize

    val codecFinal = codecMerged.getCodecData match {
      case Left(_) => codecMerged
      case Right(jsonString) =>
        if (jsonString.charAt(jsonString.length - 1).equals(','))
          CodecObject.toCodec(s"[${jsonString.dropRight(1)}]") //Remove trailing comma
        else
          CodecObject.toCodec(s"[$jsonString]")
    }

    val finalSizeCopy = codecWithoutSizeCopy.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSizeCopy), ignoreForJson = true)
    codecWithoutSizeCopy.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSizeCopy.removeEmptySpace

    val codecMergedCopy = codecWithSizeCopy + codecWithoutSizeCopy

    val codecFinalCopy = codecMergedCopy.getCodecData match {
      case Left(_) => codecMergedCopy
      case Right(jsonString) =>
        if (jsonString.charAt(jsonString.length - 1).equals(','))
          CodecObject.toCodec(s"[${jsonString.dropRight(1)}]") //Remove trailing comma
        else
          CodecObject.toCodec(s"[$jsonString]")
    }

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
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(resultCodec, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        val strSizeCodecCopy = codec.writeToken(resultCodecCopy, SonNumber(CS_INTEGER, value0.length + 1), ignoreForJson = true)
        val codecWithLastByte = codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        val codecWithLastByteCopy = codec.writeToken(strSizeCodecCopy, SonString(CS_STRING, value0)) + strSizeCodecCopy.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
        (codecWithLastByte, codecWithLastByteCopy)

      case D_BSONOBJECT =>
        val codecObj = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
          case SonObject(_, data) => data match {
            case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
            case jsonString: String => CodecObject.toCodec(jsonString)
          }
        }
        val auxCodec = modifyArrayEndWithKey(statementList, codecObj, fieldID, injFunction, condition, from, to)
        (resultCodec + auxCodec, resultCodecCopy + auxCodec)

      case D_BSONARRAY =>
        val codecArr = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
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

    val startReaderIndex = codec.getReaderIndex
    val originalSize = codec.readSize

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
        val dataType: Int = codec.readDataType()
        val codecWithDataType: Codec = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        val codecWithDataTypeCopy: Codec = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte), ignoreForJson = true)
        val (codecTo, codecUntil): (Codec, Codec) = dataType match {
          case 0 => (codecWithDataType, codecWithDataTypeCopy)
          case _ =>
            val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
              case SonString(_, keyString) => keyString.asInstanceOf[String]
            }
            val b: Byte = codec.readToken(SonBoolean(C_ZERO), true) match {
              case SonBoolean(_, result) => result.asInstanceOf[Byte]
            }

            val codecWithKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key), isKey = true)
            val resCodec = codec.writeToken(codecWithKey, SonNumber(CS_BYTE, b), true)

            val codecWithKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key), isKey = true)
            val resCodecCopy = codec.writeToken(codecWithKeyCopy, SonNumber(CS_BYTE, b), true)

            key match {
              //In case we the extracted elem name is the same as the one we're looking for (or they're halfwords) and the
              //datatype is a BsonArray
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType == D_BSONARRAY =>
                if (statementsList.size == 1) {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY)) match {
                      case SonArray(_, result) => result match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case jsonString: String => CodecObject.toCodec("{" + jsonString + "}") //TODO - Add {}
                      }
                    }
                    val newCodec = modifyArrayEnd(statementsList, partialCodec, injFunction, condition, from, to, statementsList, dataType).getCodecData match {
                      case Left(byteBuf) => new CodecBson(byteBuf)
                      case Right(jsonString) => new CodecJson(jsonString)
                    }
                    val newInjectCodec = BosonImpl.inject(newCodec.getCodecData, statementsList, injFunction) //TODO - Here??? Maybe... add {}
                    (resCodec + newInjectCodec, resCodecCopy + newInjectCodec.duplicate)
                  } else {
                    val newCodec: Codec = modifyArrayEnd(statementsList, codec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                      case SonArray(_, result) => result match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case jsonString: String => CodecObject.toCodec(jsonString)
                      }
                    }
                    val newCodec = modifyArrayEnd(statementsList.drop(1), partialCodec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate) //TODO- This duplicate might need to go...
                  } else {
                    val newCodec = modifyArrayEnd(statementsList.drop(1), codec, injFunction, condition, from, to, statementsList, dataType)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  }

                }
              case extracted if (fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted)) && dataType != D_BSONARRAY =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  val (processedCodec, processedCodecCopy): (Codec, Codec) = processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                  (processedCodec, processedCodecCopy)
                } else {
                  val codecIterate = processTypesArray(dataType, codec.duplicate, resCodec)
                  val codecIterateCopy = processTypesArray(dataType, codec, resCodecCopy)
                  (codecIterate, codecIterateCopy)
                }

              case _ =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  //                  if(isCodecJson(codec)){
                  //                    val (processedCodec, processedCodecCopy): (Codec, Codec) = processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, currentCodec, currentCodecCopy)
                  //                    (processedCodec, processedCodecCopy)
                  ////                    (codec.writeToken(currentCodec, SonString(CS_STRING,key)), codec.writeToken(currentCodecCopy, SonString(CS_STRING,key)))
                  //                  } else {
                  val (processedCodec, processedCodecCopy): (Codec, Codec) = processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                  (processedCodec, processedCodecCopy)
                  //                  }
                } else {
                  val codecIterate = processTypesArray(dataType, codec.duplicate, resCodec)
                  val codecIterateCopy = processTypesArray(dataType, codec, resCodecCopy)
                  (codecIterate, codecIterateCopy)
                }
              //If we found the desired elem but the dataType is not an array, or if we didn't find the desired elem
            }
        }
        iterateDataStructure(codecTo, codecUntil)
      }
    }

    val emptyCodec = createEmptyCodec(codec)

    val (codecWithoutSize, codecWithoutSizeCopy): (Codec, Codec) = iterateDataStructure(emptyCodec, createEmptyCodec(codec))

    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val finalSizeCopy = codecWithoutSizeCopy.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize), ignoreForJson = true)
    codecWithoutSize.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSizeCopy), ignoreForJson = true)
    codecWithoutSizeCopy.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSizeCopy.removeEmptySpace

    val codecMerged = codecWithSize + codecWithoutSize

    val finalCodec = codecMerged.getCodecData match {
      case Left(_) => codecMerged
      case Right(jsonString) =>
        if (jsonString.charAt(jsonString.length - 1).equals(','))
          CodecObject.toCodec(s"{${jsonString.dropRight(1)}}") //Remove trailing comma
        else
          CodecObject.toCodec(s"{$jsonString}")
    }

    val codecMergedCopy = codecWithSizeCopy + codecWithoutSizeCopy

    val finalCodecCopy = codecMergedCopy.getCodecData match {
      case Left(_) => codecMergedCopy
      case Right(jsonString) =>
        if (jsonString.charAt(jsonString.length - 1).equals(','))
          CodecObject.toCodec(s"{${jsonString.dropRight(1)}}") //Remove trailing comma
        else
          CodecObject.toCodec(s"{$jsonString}")
    }

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
    val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
      case SonString(_, keyString) => keyString.asInstanceOf[String]
    }
    val b: Byte = codec.readToken(SonBoolean(C_ZERO), ignore = true) match {
      case SonBoolean(_, result) => result.asInstanceOf[Byte]
    }

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
                if (isCodecJson(codec)) codec.setReaderIndex(codec.getReaderIndex + 1) //skip the ":" character
                codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)).asInstanceOf[SonObject].info match {
                  case byteBuff: ByteBuf => extractTupleList(Left(byteBuff.array))
                  case jsonString: String => extractTupleList(Right(jsonString))
                }

              case D_BSONARRAY =>
                //                if (isCodecJson(codec)) codec.setReaderIndex(codec.getReaderIndex + 1) //skip the ":" character
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

              case D_NULL => ??? //TODO should we return a null here ?
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
        otherTupleList
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
    tupleList.foreach { case (fieldName: String, fieldValue: Any) =>
      fieldValue match {
        case nestedTupleList: TupleList =>
          val nestedObject = new BsonObject()
          nestedTupleList.foreach { case (name: String, value: Any) =>
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
    * Private function used to determine if a given codec is a CodecJson or not
    *
    * @param codec - Codec that we want to know the type of
    * @return - a Boolean saying if the given codec is a CodecJson or not
    */
  private def isCodecJson(codec: Codec): Boolean = codec.getCodecData match {
    case Left(_) => false
    case Right(_) => true
  }
}
