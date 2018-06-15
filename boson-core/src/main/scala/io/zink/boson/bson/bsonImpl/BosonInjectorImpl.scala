package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import BosonImpl.{DataStructure, StatementsList}
import scala.util.{Failure, Success, Try}

private[bsonImpl] object BosonInjectorImpl {

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
  def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T): Codec = {

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
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte))
        val newCodec = dataType match {
          case 0 => writeCodec(codecWithDataType, startReader, originalSize) // This is the end
          case _ =>
            val (codecWithKey, key) = writeKeyAndByte(codec, codecWithDataType)

            key match {
              case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep || isHalfword(fieldID, extracted) =>
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
                        val partialCodec = BosonImpl.inject(partialData, statementsList, injFunction)
                        val mergedCodecs = codecWithKey + partialCodec
                        codec.setReaderIndex(codec.getReaderIndex + partialCodec.getSize + 1) //Skip the bytes that the subcodec already read
                        modifierAll(codec, mergedCodecs, dataType, injFunction)
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
                        codecWithKey + subCodec //merge the two subcodes

                      case _ =>
                        processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                    }
                  } else {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        val subCodec = BosonImpl.inject(codec.getCodecData, statementsList.drop(1), injFunction)
                        val mergedCodecs = codecWithKey + subCodec
                        codec.setReaderIndex(codec.getReaderIndex + subCodec.getSize + 1) //Skip the bytes that the subcodec already read
                        mergedCodecs
                      case _ => processTypesArray(dataType, codec, codecWithKey)
                    }
                  }
                }
              case x if fieldID.toCharArray.deep != x.toCharArray.deep && !isHalfword(fieldID, x) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT))
                  processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
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
    val codecWithLastByte = if (codec.getReaderIndex == originalSize && (codecWithoutSize.getWriterIndex == codec.getReaderIndex - 5)) { //If there's only one last byte to read (the closing 0 byte)
      codecWithoutSize + emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
    } else codecWithoutSize

    val finalSize = codecWithLastByte.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithLastByte.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace
    codecWithSize + codecWithLastByte
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
  def modifyHasElem[T](statementsList: StatementsList, codec: Codec, fieldID: String, elem: String, injFunction: T => T): Codec = {

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

    emptyDataStructure.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, size)) + codecWithoutSize
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
  def searchAndModify[T](statementsList: StatementsList, codec: Codec, elem: String, injFunction: T => T, writableCodec: Codec): Codec = {
    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to write the information into
      * @return A codec containing the alterations made
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) >= originalSize) writableCodec
      else {
        val dataType = codec.readDataType
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType.toByte)) //write the read byte to a Codec
        dataType match {
          case 0 => iterateDataStructure(codecWithDataType)
          case _ =>
            val (codecWithKey, _) = writeKeyAndByte(codec, codecWithDataType)
            dataType match {
              case D_BSONOBJECT =>
                val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT_WITH_SIZE)) match {
                  case SonObject(_, result) => result match {
                    case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                    case jsonString: String => CodecObject.toCodec(jsonString)
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
                      iterateDataStructure(codecWithKey + modifiedSubCodec)
                    } else iterateDataStructure(codecWithKey + modifiedCodec)

                  } else {
                    val modifiedCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)

                    if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                      val modifiedSubCodec = BosonImpl.inject(modifiedCodec.getCodecData, statementsList, injFunction)
                      iterateDataStructure(codecWithKey + modifiedSubCodec)
                    } else iterateDataStructure(codecWithKey + modifiedCodec)
                  }

                } else iterateDataStructure(codecWithKey + partialCodec)

              case _ => processTypesArray(dataType, codec, writableCodec) //If its not an object then it will not have the elemnt we're looking for inside it
            }
        }
      }
    }

    val modifiedSubCodec = iterateDataStructure(createEmptyCodec(codec)) //Search for the element of interest it and try to apply the injection function to it
    val modifiedSubSize = modifiedSubCodec.getCodecData match {
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }
    val codecWithSize = codec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, modifiedSubSize))
    val modCodecWithSize = codecWithSize + modifiedSubCodec //Add the size of the resulting sub-codec (plus 4 bytes) with the actual sub-codec
    writableCodec + modCodecWithSize //merge the modified sub-codec with the rest of the codec passed to this fuction as an argument
  }

  /**
    * Method used to see if an object contains a certain element inside it
    *
    * @param codec - The structure in which to look for the element
    * @param elem  - The name of the element to look for
    * @return A boolean value saying if the given element is present in that object
    */
  def hasElem(codec: Codec, elem: String): Boolean = {
    val size: Int = codec.readSize
    var key: String = ""
    //Iterate through all of the keys from the dataStructure in order to see if it contains the elem
    while (codec.getReaderIndex < size && (!elem.equals(key) && !isHalfword(elem, key))) {
      key = "" //clear the key
      val dataType = codec.readDataType
      dataType match {
        case 0 => //TODO what do we do in this case
        case _ =>
          key = codec.readToken(SonString(CS_NAME)) match {
            case SonString(_, keyString) => keyString.asInstanceOf[String]
          }
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
  def rootInjection[T](codec: Codec, injFunction: T => T): Codec = {
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
  def processTypesHasElem[T](statementsList: StatementsList, dataType: Int, fieldID: String, elem: String, codec: Codec, resultCodec: Codec, injFunction: T => T): Codec = dataType match {
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
  def processTypesArray(dataType: Int, codec: Codec, currentResCodec: Codec): Codec = {
    dataType match {
      case D_ZERO_BYTE =>
        currentResCodec
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1))
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
      case D_BSONOBJECT =>
        val value0 = codec.readToken(SonObject(CS_OBJECT_INJ)) match { //TODO What about JSON case ???
          case SonObject(_, byteBuf) => byteBuf.asInstanceOf[ByteBuf]
        }
        codec.writeToken(currentResCodec, SonObject(CS_OBJECT, value0.array))
      case D_BSONARRAY =>
        val value0 = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
          case SonArray(_, byteBuf) => byteBuf.asInstanceOf[ByteBuf]
        }
        codec.writeToken(currentResCodec, SonArray(CS_ARRAY, value0.array))
      case D_NULL =>
        currentResCodec
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
      case D_BOOLEAN =>
        codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
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
  def modifierAll[T](codec: Codec, currentResCodec: Codec, seqType: Int, injFunction: T => T): Codec = {
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
            val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value.length + 1))
            codec.writeToken(strSizeCodec, SonString(CS_STRING, value)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
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
  def modifierEnd[T](codec: Codec, dataType: Int, injFunction: T => T, codecRes: Codec, codecResCopy: Codec): (Codec, Codec) = dataType match {

    case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
      val value0 = codec.readToken(SonString(CS_STRING)) match {
        case SonString(_, data) => data.asInstanceOf[String]
      }
      val resCodec = applyFunction(injFunction, value0) match {
        case str: String =>
          val strSizeCodec = codec.writeToken(codecRes, SonNumber(CS_INTEGER, str.length + 1))
          codec.writeToken(strSizeCodec, SonString(CS_STRING, str)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
      }
      val strSizeCodec = codec.writeToken(codecResCopy, SonNumber(CS_INTEGER, value0.length + 1))
      val resCodecCopy = codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + codec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
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
  def processTypesAll[T](statementsList: StatementsList, seqType: Int, codec: Codec, currentResCodec: Codec, fieldID: String, injFunction: T => T): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(currentResCodec, SonNumber(CS_INTEGER, value0.length + 1))
        codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))

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
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY)) match {
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

      case D_BOOLEAN => codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
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
  def applyFunction[T](injFunction: T => T, value: Any, fromRoot: Boolean = false): T = {

    Try(injFunction(value.asInstanceOf[T])) match {
      case Success(modifiedValue) =>
        modifiedValue

      case Failure(_) => value match {
        case double: Double =>
          Try(injFunction(double.toFloat.asInstanceOf[T])) match { //try with the value being a Double
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

            case Failure(_) => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
          }

        case byteArr: Array[Byte] if fromRoot => //In case the User passed in the root
          Try(injFunction(byteArr.asInstanceOf[T])) match {
            case Success(modifiedValue) =>
              modifiedValue.asInstanceOf[T]

            case Failure(_) => throw CustomException(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
          }

        case byteArr: Array[Byte] => //TODO PREVENT USER FROM PASSING THE ROOT AND REPLACING IT WITH A STRING OR AN INSTANT ??
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
  def arrayInjection[T](statementsList: StatementsList, codec: Codec, currentCodec: Codec, injFunction: T => T, key: String, left: Int, mid: String, right: Any): Codec = {

    val arrayTokenCodec = codec.readToken(SonArray(CS_ARRAY_INJ)) match {
      case SonArray(_, data) => data match {
        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
        case jsonString: String => CodecObject.toCodec(jsonString)
      }
    }
    val codecArrayEnd: Codec = (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, from, expr, to) if to.isInstanceOf[Int] =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, to.toString, fullStatementsList = statementsList)
      case (EMPTY_KEY, from, expr, _) =>
        modifyArrayEnd(statementsList, arrayTokenCodec, injFunction, expr, from.toString, fullStatementsList = statementsList)
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
  def modifyArrayEnd[T](statementsList: StatementsList, codec: Codec, injFunction: T => T, condition: String, from: String, to: String = C_END, fullStatementsList: StatementsList): Codec = {
    val startReaderIndex = codec.getReaderIndex
    val originalSize = codec.readSize

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param currentCodec     - The codec where we right the values of the processed data
      * @param currentCodecCopy - An Auxiliary codec to where we write the values in case the previous cycle was the last one
      * @param exceptions       - An Int that represents how many exceptions have occurred (This value is used to detirmine if the range inserted is a TO_RANGE or UNTIL_RANGE)
      * @return a codec pair with the modifications made and the amount of exceptions that occured
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

            val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
              case SonString(_, keyString) => keyString.asInstanceOf[String]
            }
            val b: Byte = codec.readToken(SonBoolean(C_ZERO)) match { //TODO FOR CodecJSON we cant read a boolean, we need to read an empty string
              case SonBoolean(_, result) => result.asInstanceOf[Byte]
            }

            val codecWithoutKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key))
            val codecWithKey = codec.writeToken(codecWithoutKey, SonNumber(CS_BYTE, b))

            val codecWithoutKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key))
            val codecWithKeyCopy = codec.writeToken(codecWithoutKeyCopy, SonNumber(CS_BYTE, b))

            val isArray = key.forall(b => b.isDigit) //TODO - isArray true if dataType == 4

            val ((codecResult, codecResultCopy), exceptionsResult): ((Codec, Codec), Int) = (new String(key), condition, to) match {
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

                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(partialCodec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                            case Success(tuple) =>
                              (tuple, 0)
                            case Failure(_) =>
                              ((codecWithKey + partialCodec, codecWithKeyCopy + partialCodec), 1)
                          }
                        (codecTuple, exceptionsReturn)

                      case _ =>
                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                          Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                            case Success(tuple) => (tuple, 0)
                            case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                          }
                        (codecTuple, exceptionsReturn)
                    }
                  } else {
                    val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKeyCopy, codecWithKeyCopy)) match {
                        case Success(tuple) => (tuple, 0)
                        case Failure(_) => ((codecWithKey, codecWithKeyCopy), 1)
                      }
                    (codecTuple, exceptionsReturn)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[ArrExpr]) {
                    dataType match {
                      case D_BSONARRAY | D_BSONOBJECT =>
                        val partialData: DataStructure = codec.readToken(SonArray(CS_ARRAY)) match {
                          case SonArray(_, value) => value.asInstanceOf[DataStructure]
                        }

                        val codecData =
                          if (statementsList.head._1.isInstanceOf[ArrExpr])
                            BosonImpl.inject(partialData, statementsList, injFunction).getCodecData
                          else
                            partialData

                        val partialCodec = partialData match {
                          case Left(byteBuf) => CodecObject.toCodec(byteBuf)
                          case Right(jsonString) => CodecObject.toCodec(jsonString)
                        }

                        val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) = Try(BosonImpl.inject(codecData, statementsList.drop(1), injFunction)) match {
                          case Success(successCodec) => ((currentCodec + successCodec, currentCodec + partialCodec), exceptions)
                          case Failure(_) => ((currentCodec + partialCodec, currentCodec + partialCodec), exceptions + 1)
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
                          val newCodecCopy = codecWithKey.duplicate
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          val subCodec = BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                          if (condition equals UNTIL_RANGE) {
                            val (codecTuple, exceptionsReturn): ((Codec, Codec), Int) =
                              Try(modifierEnd(modifiedPartialCodec, dataType, injFunction, emptyCodec, emptyCodec.duplicate)) match {
                                case Success(tuple) =>
                                  ((codecWithKey + tuple._1, newCodecCopy + tuple._2), exceptions)
                                case Failure(_) =>
                                  ((codecWithKey + partialCodec, newCodecCopy + partialCodec), exceptions + 1)
                              }
                            (codecTuple, exceptionsReturn)
                          } else ((codecWithKey + subCodec, newCodecCopy + subCodec), exceptions)
                        } else
                          ((codecWithKey, codecWithKeyCopy), exceptions + 1)
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
                      //                      val newCodecCopy = codecWithKey.duplicate // Here!
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                        case Success(tuple) =>
                          (tuple, exceptions)
                        case Failure(_) =>
                          ((codecWithKey, codecWithKey), exceptions + 1)
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
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList.drop(1), injFunction)
                          Try(BosonImpl.inject(modifiedPartialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((codecWithKey + c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(_) =>
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
                if (statementsList.head._2.contains(C_DOUBLEDOT) && !statementsList.head._1.isInstanceOf[KeyWithArrExpr]) { //TODO why can't it be keyWithArrExpr ?
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
                      ((processTypesArray(dataType, codec, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else
                  ((processTypesArray(dataType, codec, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)


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
                          val newCodecCopy = codecWithKey.duplicate
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)

                          //Look inside the curent object for cases that match the user given expression
                          val mergedCodec =
                            if (!statementsList.equals(fullStatementsList)) //we only want to investigate inside this object if it has the property we're looking for
                              BosonImpl.inject(modifiedPartialCodec.getCodecData, fullStatementsList, injFunction)
                            else
                              modifiedPartialCodec

                          //TODO HERE this wont corectly tell the until range, does not know the last element
                          Try(modifierEnd(mergedCodec, dataType, injFunction, emptyCodec, createEmptyCodec(codec))) match { //emptyCodec.duplicate
                            case Success(_) => ((codecWithKey, codecWithKeyCopy), exceptions)
                            //((codecWithKey + tuple._1, newCodecCopy + tuple._2), exceptions)
                            case Failure(_) =>
                              ((codecWithKey + mergedCodec, newCodecCopy + mergedCodec), if (condition equals UNTIL_RANGE) exceptions + 1 else exceptions)
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
                      val newCodecCopy = codecWithKey.duplicate
                      Try(modifierEnd(codec, dataType, injFunction, codecWithKey, codecWithKey)) match {
                        case Success(tuple) =>
                          (tuple, exceptions)
                        case Failure(_) =>
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
                          val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                          Try(BosonImpl.inject(modifiedPartialCodec.getCodecData, statementsList.drop(1), injFunction)) match {
                            case Success(c) =>
                              ((codecWithKey + c, processTypesArray(dataType, codec, newCodecCopy)), exceptions)
                            case Failure(_) =>
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
                      val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                        case SonArray(_, value) => value match {
                          case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                          case string: String => CodecObject.toCodec(string)
                        }
                      }
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, fullStatementsList, injFunction)
                      ((codecWithKey + modifiedPartialCodec, codecWithKeyCopy + modifiedPartialCodec), exceptions)

                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions)
                  }
                } else {
                  ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions) // Exceptions
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
                      val modifiedPartialCodec = BosonImpl.inject(partialCodec.getCodecData, statementsList, injFunction)
                      ((codecWithKey + modifiedPartialCodec, codecWithKeyCopy + modifiedPartialCodec), exceptions)
                    case _ =>
                      ((processTypesArray(dataType, codec.duplicate, codecWithKey), processTypesArray(dataType, codec, codecWithKeyCopy)), exceptions + 1)
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

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))

    val codecFinal = codecWithSize + codecWithoutSize

    val finalSizeCopy = codecWithoutSizeCopy.getCodecData match { //TODO this might be wrong, size is giving 330
      case Left(byteBuf) => byteBuf.writerIndex + 4
      case Right(string) => string.length + 4
    }

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSizeCopy))
    codecWithoutSizeCopy.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSizeCopy.removeEmptySpace

    val codecFinalCopy = codecWithSizeCopy + codecWithoutSizeCopy

    condition match {
      case TO_RANGE =>
        if (exceptions == 0)
          codecFinal
        else
          throw new Exception //TODO throwing exception here
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
  def processTypesArrayEnd[T](statementList: StatementsList, fieldID: String, dataType: Int, codec: Codec, injFunction: T => T, condition: String, from: String = C_ZERO, to: String = C_END, resultCodec: Codec, resultCodecCopy: Codec): (Codec, Codec) = {
    dataType match {

      case D_FLOAT_DOUBLE =>
        val token = codec.readToken(SonNumber(CS_DOUBLE))
        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        //        val token = codec.readToken(SonString(CS_STRING))
        //
        //        (codec.writeToken(resultCodec, token), codec.writeToken(resultCodecCopy, token))

        val value0 = codec.readToken(SonString(CS_STRING)) match {
          case SonString(_, data) => data.asInstanceOf[String]
        }
        val strSizeCodec = codec.writeToken(resultCodec, SonNumber(CS_INTEGER, value0.length + 1))
        val strSizeCodecCopy = codec.writeToken(resultCodecCopy, SonNumber(CS_INTEGER, value0.length + 1))
        val codecWithLastByte = codec.writeToken(strSizeCodec, SonString(CS_STRING, value0)) + strSizeCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
        val codecWithLastByteCopy = codec.writeToken(strSizeCodecCopy, SonString(CS_STRING, value0)) + strSizeCodecCopy.writeToken(createEmptyCodec(codec), SonNumber(CS_BYTE, 0.toByte))
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
  def modifyArrayEndWithKey[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T, condition: String, from: String, to: String = C_END): Codec = {
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
        val dataType: Int = codec.readDataType
        val codecWithDataType: Codec = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType.toByte))
        val codecWithDataTypeCopy: Codec = codec.writeToken(currentCodecCopy, SonNumber(CS_BYTE, dataType.toByte))
        val (codecTo, codecUntil): (Codec, Codec) = dataType match {
          case 0 => (codecWithDataType, codecWithDataTypeCopy)
          case _ =>
            val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
              case SonString(_, keyString) => keyString.asInstanceOf[String]
            }
            val b: Byte = codec.readToken(SonBoolean(C_ZERO)) match { //TODO FOR CodecJSON we cant read a boolean, we need to read an empty string
              case SonBoolean(_, result) => result.asInstanceOf[Byte]
            }

            val codecWithKey = codec.writeToken(codecWithDataType, SonString(CS_STRING, key))
            val resCodec = codec.writeToken(codecWithKey, SonNumber(CS_BYTE, b))

            val codecWithKeyCopy = codec.writeToken(codecWithDataTypeCopy, SonString(CS_STRING, key))
            val resCodecCopy = codec.writeToken(codecWithKeyCopy, SonNumber(CS_BYTE, b))

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
                    val newCodec = modifyArrayEnd(statementsList, partialCodec, injFunction, condition, from, to, statementsList)
                    val newInjectCodec = BosonImpl.inject(newCodec.getCodecData, statementsList, injFunction)
                    (resCodec + newInjectCodec, resCodecCopy + newInjectCodec.duplicate)
                  } else {
                    val newCodec: Codec = modifyArrayEnd(statementsList, codec, injFunction, condition, from, to, statementsList)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  }
                } else {
                  if (statementsList.head._2.contains(C_DOUBLEDOT)) {
                    val partialCodec = codec.readToken(SonArray(CS_ARRAY_WITH_SIZE)) match {
                      case SonArray(_, result) => result match {
                        case byteBuf: ByteBuf => CodecObject.toCodec(byteBuf)
                        case jsonString: String => CodecObject.toCodec(jsonString)
                      }
                    }
                    val newCodec = modifyArrayEnd(statementsList.drop(1), partialCodec, injFunction, condition, from, to, statementsList)
                    (resCodec + newCodec, resCodecCopy + newCodec.duplicate)
                  } else {
                    val newCodec = modifyArrayEnd(statementsList.drop(1), codec, injFunction, condition, from, to, statementsList)
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

              case extracted if fieldID.toCharArray.deep != extracted.toCharArray.deep && !isHalfword(fieldID, extracted) =>
                if (statementsList.head._2.contains(C_DOUBLEDOT) && statementsList.head._1.isInstanceOf[KeyWithArrExpr]) {
                  val (processedCodec, processedCodecCopy): (Codec, Codec) = processTypesArrayEnd(statementsList, fieldID, dataType, codec, injFunction, condition, from, to, resCodec, resCodecCopy)
                  (processedCodec, processedCodecCopy)
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

    val codecWithSize: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSize))
    codecWithoutSize.removeEmptySpace //TODO MAYBE MAKE THIS IMMUTABLE ??
    codecWithSize.removeEmptySpace

    val codecWithSizeCopy: Codec = emptyCodec.writeToken(createEmptyCodec(codec), SonNumber(CS_INTEGER, finalSizeCopy))
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
  def writeKeyAndByte(codec: Codec, writableCodec: Codec): (Codec, String) = {
    val key: String = codec.readToken(SonString(CS_NAME_NO_LAST_BYTE)) match {
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
  def createEmptyCodec(inputCodec: Codec): Codec = {
    val emptyCodec = inputCodec.getCodecData match {
      case Left(_) => CodecObject.toCodec(Unpooled.buffer()) //Creates a CodecBson with an empty ByteBuf with capacity 256
      case Right(_) => CodecObject.toCodec("") //Creates a CodecJson with an empty String
    }
    emptyCodec.setWriterIndex(0) //Sets the writerIndex of the newly created codec to 0 (Initialy it starts at 256 for CodecBson, so we need o reset it)
    emptyCodec
  }

}
