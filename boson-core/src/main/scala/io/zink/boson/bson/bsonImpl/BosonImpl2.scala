package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._

import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
case class CustomException2(errorMessage: String) extends RuntimeException {
  override def getMessage: String = errorMessage
}

class BosonImpl2 {
  type DataStructure = Either[ByteBuf, String]
  type StatementsList = List[(Statement, String)]

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
    val codec: Codec = dataStructure match {
      case Right(jsonString) => CodecObject.toCodec(jsonString)
      case Left(byteBuf) => CodecObject.toCodec(byteBuf)
    }

    statements.head._1 match {
      case ROOT => rootInjection(codec, injFunction) //execRootInjection(codec, f)

      case Key(key: String) => modifyAll(statements, codec, key, injFunction) // (key = fieldID)

      case HalfName(half: String) => modifyAll(statements, codec, half, injFunction)

      case HasElem(key: String, elem: String) => modifyHasElem(statements, codec, key, elem, injFunction)

      case ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) => ???

      case _ => ???
    }
  }

  /**
    *
    * @param statementsList -
    * @param codec          -
    * @param fieldID        -
    * @param injFunction    -
    * @tparam T -
    * @return
    */
  private def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T): Codec = {
    def writeCodec(currentCodec: Codec, startReader: Int, originalSize: Int): Codec = {
      if ((codec.getReaderIndex - startReader) < originalSize) currentCodec
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType))
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
                        val partialCodec = CodecObject.toCodec(inject(partialData, statementsList, injFunction))
                        modifierAll(codec, codecWithKey + partialCodec, dataType, injFunction)
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
                        val modifiedPartialCodec = inject(partialData, statementsList.drop(1), injFunction)
                        inject((codecWithKey + modifiedPartialCodec).getCodecData, statementsList, injFunction)

                      case _ =>
                        processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)
                    }
                  } else {
                    dataType match {
                      case D_BSONOBJECT | D_BSONARRAY =>
                        inject(codec.getCodecData, statementsList.drop(1), injFunction)
                      case _ =>
                        processTypesArray(dataType, codec, codecWithKey)
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
    val emptyCodec: Codec = CodecObject.toCodec()

    val codecWithoutSize = writeCodec(emptyCodec, startReader, originalSize)
    val finalSize = codecWithoutSize.getCodecData match {
      case Left(byteBuf) => byteBuf.capacity
      case Right(string) => string.length
    }

    emptyCodec.writeToken(emptyCodec, SonNumber(CS_INTEGER, finalSize)) + codecWithoutSize
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
    //TODO DONT FORGET TO WRITE THE SIZE TO THE RESULT CODEC
    val emptyDataStructure: Codec = codec.getCodecData match {
      case Left(_) => CodecObject.toCodec(Unpooled.EMPTY_BUFFER)
      case Right(_) => CodecObject.toCodec("")
    }

    /**
      * Recursive function to iterate through the given data structure and return the modified codec
      *
      * @param writableCodec - the codec to be modified
      * @return A modified codec
      */
    def iterateDataStructure(writableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) < originalSize) writableCodec
      else {
        val dataType: Int = codec.readDataType
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType)) //write the read byte to a Codec
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

    iterateDataStructure(emptyDataStructure) //Initiate recursion with an empty data structure
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

    def iterateDataStructure(writtableCodec: Codec): Codec = {
      if ((codec.getReaderIndex - startReader) < originalSize) writableCodec
      else {
        val dataType = codec.readDataType
        val codecWithDataType = codec.writeToken(writableCodec, SonNumber(CS_BYTE, dataType)) //write the read byte to a Codec
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
                          case byteBuf: ByteBuf => Left(ByteBuf)
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
      val modifiedBytes: Array[Byte] = applyFunction(injFunction, bsonBytes).asInstanceOf[Array[Byte]] //apply the injector function to the extracted bytes
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
    * @param dataType
    * @param codec
    * @param currentResCodec
    * @return
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
    * @param codec
    * @param curentResCodec
    * @param seqType
    * @param f
    * @tparam T
    * @return
    */
  private def modifierAll[T](codec: Codec, curentResCodec: Codec, seqType: Int, f: T => T): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value0 = codec.readToken(SonNumber(CS_DOUBLE))
        applyFunction(f, value0) match {
          case value: SonNumber => codec.writeToken(curentResCodec, value)
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val value0 = codec.readToken(SonString(CS_STRING))
        applyFunction(f, value0) match {
          case value: SonString => codec.writeToken(curentResCodec, value)
        }
      case D_BSONOBJECT =>
        val value0 = codec.readToken(SonObject(CS_OBJECT))
        applyFunction(f, value0) match {
          case value: SonObject => codec.writeToken(curentResCodec, value)
        }
      case D_BSONARRAY =>
        val value0 = codec.readToken(SonArray(CS_ARRAY))
        applyFunction(f, value0) match {
          case value: SonArray => codec.writeToken(curentResCodec, value)
        }
      case D_BOOLEAN =>
        val value0 = codec.readToken(SonBoolean(CS_BOOLEAN))
        applyFunction(f, value0) match {
          case value: SonBoolean => codec.writeToken(curentResCodec, value)
        }
      case D_NULL =>
        throw new Exception //TODO
      case D_INT =>
        val value0 = codec.readToken(SonNumber(CS_INTEGER))
        applyFunction(f, value0) match {
          case value: SonNumber => codec.writeToken(curentResCodec, value)
        }
      case D_LONG =>
        val value0 = codec.readToken(SonNumber(CS_LONG))
        applyFunction(f, value0) match {
          case value: SonNumber => codec.writeToken(curentResCodec, value)
        }
    }
  }

  /**
    *
    * @param statementsList
    * @param seqType
    * @param codec
    * @param currentResCodec
    * @param fieldID
    * @param injFunction
    * @tparam T
    * @return
    */
  private def processTypesAll[T](statementsList: StatementsList, seqType: Int, codec: Codec, currentResCodec: Codec, fieldID: String, injFunction: T => T): Codec = {
    seqType match {
      case D_FLOAT_DOUBLE =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_DOUBLE)))
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        codec.writeToken(currentResCodec, codec.readToken(SonString(CS_STRING)))
      case D_BSONOBJECT =>
        val partialCodec: Codec = codec.readToken(SonObject(CS_OBJECT)) match {
          case SonObject(_, result) => CodecObject.toCodec(result)
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        currentResCodec + codecAux
      case D_BSONARRAY =>
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY)) match {
          case SonArray(_, result) => CodecObject.toCodec(result)
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        currentResCodec + codecAux
      case D_NULL => throw Exception
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
    * @param fieldID   Key given by User.
    * @param extracted Key extracted.
    * @return
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
      case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

      case Failure(_) => value match {
        case double: Double =>
          Try(injFunction(double.toFloat.asInstanceOf[T])) match { //try with the value being a Double
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

            case Failure(_) => throw CustomException2(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
          }

        case byteArr: Array[Byte] =>
          Try(injFunction(new String(byteArr).asInstanceOf[T])) match { //try with the value being a Array[Byte]
            case Success(modifiedValue) => modifiedValue.asInstanceOf[T]
            case Failure(_) =>
              Try(injFunction(Instant.parse(new String(byteArr)).asInstanceOf[T])) match { //try with the value being an Instant
                case Success(modifiedValue) => modifiedValue.asInstanceOf[T]

                case Failure(_) => throw CustomException2(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
              }
          }
      }

      case _ => throw CustomException2(s"Type Error. Cannot Cast ${value.getClass.getSimpleName.toLowerCase} inside the Injector Function.")
    }
  }

  /**
    *
    * @param statementsList
    * @param codec
    * @param currentCodec
    * @param injFunction
    * @param key
    * @param left
    * @param mid
    * @param right
    * @tparam T
    * @return
    */
  private def arrayInjection[T](statementsList: StatementsList, codec: Codec, currentCodec: Codec, injFunction: T => T, key: String, left: Int, mid: String, right: Any): Codec = {
    (key, left, mid.toLowerCase(), right) match {
      case (EMPTY_KEY, 0, C_END, None) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEnd(statementsList, CodecObject.toCodec(arrayToken), injFunction, C_END, 0.toString)
        currentCodec + codecArrayEnd
      case (EMPTY_KEY, from, UNTIL_RANGE, C_END) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEnd
        currentCodec + codecArrayEnd
      case (EMPTY_KEY, from, TO_RANGE, C_END) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEnd
        currentCodec + codecArrayEnd
      case (EMPTY_KEY, from, expression, to) if to.isInstanceOf[Int] =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        ???
      case (nonEmptyKey, 0, C_END, None) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEndWithKey
        currentCodec + codecArrayEnd
      case (nonEmptyKey, from, UNTIL_RANGE, C_END) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEndWithKey
        currentCodec + codecArrayEnd
      case (k, a, TO_RANGE, C_END) =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        val codecArrayEnd = ??? //modifyArrayEndWithKey
        currentCodec + codecArrayEnd
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        val arrayToken = codec.readToken(SonArray(CS_ARRAY))
        ???
    }
    ???
  }

  /**
    *
    * @param statementsList
    * @param codec
    * @param injFunction
    * @param to
    * @param from
    * @tparam T
    * @return
    */
  private def modifyArrayEnd[T](statementsList: StatementsList, codec: Codec, injFunction: T => T, to: String, from: String): Codec = {
    ???
  }

  /**
    *
    * @tparam T
    * @return
    */
  private def modifyArrayEndWithKey[T](): Codec = {
    ???
  }

  /**
    * Helper function to retrieve a Key and its closing byte
    *
    * @param codec
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

}
