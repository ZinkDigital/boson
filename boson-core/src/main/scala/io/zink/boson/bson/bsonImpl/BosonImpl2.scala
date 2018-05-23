package io.zink.boson.bson.bsonImpl

import io.netty.buffer.ByteBuf
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._

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
  def inject[T](dataStructure: DataStructure, statements: StatementsList, injFunction: T => T ): DataStructure = {
    val codec: Codec = dataStructure match{
      case Right(jsonString) => CodecObject.toCodec(jsonString)
      case Left(byteBuf) => CodecObject.toCodec(byteBuf)
    }

    statements.head._1 match {
      case ROOT => ??? //execRootInjection(codec, f)

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
                if(statementsList.head._2.contains(C_DOUBLEDOT)) {
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      val token = if(dataType == D_BSONOBJECT) SonObject(CS_OBJECT) else SonArray(CS_ARRAY)
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
                      val modifiedPartialCodec: DataStructure = inject(partialData, statementsList.drop(1), injFunction)
                      inject(modifiedPartialCodec,statementsList,injFunction) //Maybe, Maybe Not
                      ???
                    case _ =>
                      processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction)  //processTypesAll
                  }
                } else{
                  dataType match {
                    case D_BSONOBJECT | D_BSONARRAY =>
                      inject(codec.getCodecData, statementsList.drop(1), injFunction)
                      ???
                    case _ =>
                      processTypesArray(dataType, codec, codecWithKey) //processTypesArray
                  }
                }
              case x if fieldID.toCharArray.deep != x.toCharArray.deep => // add !isHalfWord
                if(statementsList.head._2.contains(C_DOUBLEDOT))
                  processTypesAll(statementsList, dataType, codec, codecWithKey, fieldID, injFunction) //processTypesAll
                else
                  processTypesArray(dataType, codec, codecWithKey) //processTypesArray
            }
        }
        writeCodec(newCodec, startReader, originalSize)
      }
    }

    val startReader: Int = codec.getReaderIndex
    val originalSize: Int = codec.readSize
    val emptyCodec: Codec = CodecObject.toCodec()

    writeCodec(emptyCodec, startReader, originalSize)

    //TODO - Add size to outputCodec
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
        codec.writeToken(currentResCodec, codec.readToken(SonObject(CS_OBJECT))) // TODO - codec + codecAux ???
      case D_BSONARRAY =>
        codec.writeToken(currentResCodec, codec.readToken(SonArray(CS_ARRAY))) // TODO - codec + codecAux ???
      case D_NULL => throw new Exception
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
      case D_BOOLEAN =>
        codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
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
        // TODO - codec + codecAux ???
        codec.writeToken(currentResCodec, codecAux.readToken(SonObject(CS_OBJECT)))
      case D_BSONARRAY =>
        val partialCodec: Codec = codec.readToken(SonArray(CS_ARRAY)) match {
          case SonArray(_, result) => CodecObject.toCodec(result)
        }
        val codecAux = modifyAll(statementsList, partialCodec, fieldID, injFunction)
        // TODO - codec + codecAux ???
        codec.writeToken(currentResCodec, codecAux.readToken(SonArray(CS_ARRAY)))
      case D_NULL => throw Exception
      case D_INT =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_INTEGER)))
      case D_LONG =>
        codec.writeToken(currentResCodec, codec.readToken(SonNumber(CS_LONG)))
      case D_BOOLEAN =>
        codec.writeToken(currentResCodec, codec.readToken(SonBoolean(CS_BOOLEAN)))
    }
  }

}
