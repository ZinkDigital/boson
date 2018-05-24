package io.zink.boson.bson.bsonImpl

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary._

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
    *
    * @param dataStructure -
    * @param statements    -
    * @param injFunction   -
    * @tparam T            -
    * @return
    */
  def inject[T](dataStructure: DataStructure, statements: StatementsList, injFunction: T => T ): Codec = {
    val codec: Codec = dataStructure match{
      case Right(jsonString) => CodecObject.toCodec(jsonString)
      case Left(byteBuf) => CodecObject.toCodec(byteBuf)
    }

    statements.head._1 match {
      case ROOT => rootInjection(codec, injFunction) //execRootInjection(codec, f)

      case Key(key: String) => ??? //modifyAll(statements, codec, key, injFunction) (key = fieldID)

      case HalfName(half: String) => ??? //modifyAll(statements, codec, half, injFunction)

      case HasElem(key: String, elem: String) => ??? //modifyHasElem(statements, codec, key, elem, f)

      case _ => ???
    }
  }

  /**
    *
    * @param statementsList -
    * @param codec          -
    * @param fieldID        -
    * @param injFunction    -
    * @tparam T             -
    * @return
    */
  def modifyAll[T](statementsList: StatementsList, codec: Codec, fieldID: String, injFunction: T => T): Codec = {
    def writeCodec(currentCodec: Codec, startReader: Int, originalSize: Int): Codec = {
      if((codec.getReaderIndex - startReader) < originalSize) {
        currentCodec
      } else {
        val dataType: Int = codec.readDataType
        val newCodec = dataType match{
          case 0 => // This is the end
            codec.writeToken(currentCodec, SonNumber(CS_BYTE, dataType))
          case _ =>
            val codecWithDataType = codec.writeToken(currentCodec,SonNumber(CS_BYTE,dataType))
            val (key, b): (String, Int) = {
              val key: String = codec.readToken(SonString(CS_NAME)) match {
                case SonString(_, keyString) => keyString.asInstanceOf[String]
              }
              val b: Byte = codec.readToken(SonBoolean(CS_BYTE)) match {
                case SonBoolean(_, bByte) => bByte.asInstanceOf[Byte]
              }
              (key,b)
            }
            val codecWithKey1 = codec.writeToken(codecWithDataType,SonString(CS_STRING,key))
            val codecWithKey = codec.writeToken(codecWithKey1, SonNumber(CS_BYTE,b))

            key match{
              case extracted if fieldID.toCharArray.deep == extracted.toCharArray.deep => //add isHalWord Later
                if(statementsList.lengthCompare(1) == 0) {
                  if(statementsList.head._2.contains(C_DOUBLEDOT)) {
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
                if(statementsList.head._2.contains(C_DOUBLEDOT))
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

    emptyCodec.writeToken(emptyCodec, SonNumber(CS_INTEGER,finalSize)) + codecWithoutSize
  }

  def processTypesArray(dataType: Int, codec: Codec, currentResCodec: Codec): Codec = {
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

  def processTypesAll[T](statementsList: StatementsList, seqType: Int, codec: Codec, currentResCodec: Codec, fieldID: String, injFunction: T => T): Codec = {
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
      val modifiedBytes: Array[Byte] = applyFunction(injFunction, bsonBytes).asInstanceOf[Array[Byte]] //apply the injector function to the extracted bytes
      val newBuf = Unpooled.buffer(modifiedBytes.length).writeBytes(modifiedBytes) //create a new ByteBuf from those bytes
        CodecObject.toCodec(newBuf)

      case Right(jsonString) => //TODO not sure if this is correct for CodecJson
        val modifiedString: String = applyFunction(injFunction, jsonString).asInstanceOf[String]
        CodecObject.toCodec(modifiedString)
    }
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

}
