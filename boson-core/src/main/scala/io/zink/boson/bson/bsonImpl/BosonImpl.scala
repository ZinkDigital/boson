package io.zink.boson.bson.bsonImpl

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

import io.netty.buffer.ByteBuf
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonPath._
import io.zink.boson.bson.codec._
import BosonExtractorImpl._
import BosonInjectorImpl._
import io.zink.boson.bson.codec.impl.{CodecBson, CodecJson}

import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 18/09/2017.
  */
case class CustomException(smth: String) extends RuntimeException {
  override def getMessage: String = smth
}

/**
  * Class with all operations to be applied on a Netty ByteBuffer or a Json encoded String
  */
object BosonImpl {

  type DataStructure = Either[ByteBuf, String]
  type StatementsList = List[(Statement, String)]

  /**
    * Public method to trigger extraction.
    *
    * @param netty1    Encoded document.
    * @param keyList   set of keys.
    * @param limitList limits of arrays.
    * @tparam T type to be extracted.
    * @return List with extraction result.
    */
  def extract[T](netty1: DataStructure, keyList: List[(String, String)],
                 limitList: List[(Option[Int], Option[Int], String)]): List[Any] = {

    val nettyC: Codec = netty1 match {
      case Right(x) => CodecObject.toCodec(x)
      case Left(x) => CodecObject.toCodec(x)
    }

    val startReaderIndexCodec: Int = nettyC.getReaderIndex
    Try(nettyC.readSize) match {

      case Success(value) =>
        val size: Int = value
        val seqTypeCodec: SonNamedType = nettyC.rootType //TODO - look into this
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

  //----------------------------------------------------- INJECTORS ----------------------------------------------------

  /**
    * Starter method for the injection process, this method will pattern match the statements in the statements list
    * and delegate to other helper methods
    *
    * @param dataStructure - The data structure in which to perform the injection process (either a ByteBuf or a String)
    * @param statements    - The statements with information regarding where to perform the injection
    * @param injFunction   - The injection function to be applied
    * @tparam T - The type of the input and output of the injection function
    * @return a new codec with the changes applied to it
    */
  def inject[T](dataStructure: DataStructure, statements: StatementsList, injFunction: T => T, readerIndextoUse: Int = 0)(implicit convertFunction: Option[List[(String, Any)] => T] = None): Codec = {
    val codec: Codec = dataStructure match {
      case Right(jsonString: String) =>
        val returnCodec = new CodecJson(jsonString)
        returnCodec.setReaderIndex(readerIndextoUse)
        returnCodec
      case Left(byteBuf: ByteBuf) => new CodecBson(byteBuf)
    }

    statements.head._1 match {
      case ROOT => rootInjection(codec, injFunction)

      case Key(key: String) => modifyAll(statements, codec, key, injFunction)

      case HalfName(half: String) => modifyAll(statements, codec, half, injFunction)

      case HasElem(key: String, elem: String) => modifyHasElem(statements, codec, key, elem, injFunction)

      case ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) =>
        val input: (String, Int, String, Any) =
          (leftArg, midArg, rightArg) match {
            case (i, o1, o2) if o1.isDefined && o2.isDefined =>
              if (o1.get.value.equals(UNTIL_RANGE) && o2.get.isInstanceOf[Int]) {
                val to: Int = o2.get.asInstanceOf[Int]
                (EMPTY_KEY, i, TO_RANGE, to - 1)
              }
              else (EMPTY_KEY, i, o1.get.value, o2.get)

            case (i, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (EMPTY_KEY, i, TO_RANGE, i)

            case (0, str, None) =>
              str.get.value match {
                case C_FIRST => (EMPTY_KEY, 0, TO_RANGE, 0)
                case C_END => (EMPTY_KEY, 0, C_END, None)
                case C_ALL => (EMPTY_KEY, 0, TO_RANGE, C_END)
              }
          }
        arrayInjection(statements, codec, codec.duplicate, injFunction, input._1, input._2, input._3, input._4)

      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
        val input: (String, Int, String, Any) =
          (arrEx.leftArg, arrEx.midArg, arrEx.rightArg) match {

            case (_, o1, o2) if o1.isDefined && o2.isDefined =>
              if (o1.get.value.equals(UNTIL_RANGE) && o2.get.isInstanceOf[Int]) {
                val to: Int = o2.get.asInstanceOf[Int]
                (key, arrEx.leftArg, TO_RANGE, to - 1)
              } else (key, arrEx.leftArg, o1.get.value, o2.get) //User sent, for example, Key[1 TO 3] translate to - Key[1 TO 3]

            case (_, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg) //User sent, for example, Key[1] translates to - Key[1 TO 1]

            case (0, str, None) =>
              str.get.value match {
                case C_FIRST =>
                  (key, 0, TO_RANGE, 0) //User sent Key[first] , translates to - Key[0 TO 0]
                case C_END =>
                  (key, 0, C_END, None) //User sent Key[end], translates to - Key[0 END None]
                case C_ALL =>
                  (key, 0, TO_RANGE, C_END) //user sent Key[all], translates to - Key[0 TO END]
              }
          }
        arrayInjection(statements, codec, codec.duplicate, injFunction, input._1, input._2, input._3, input._4)

      case _ =>
        throw CustomException("Wrong Statements, Bad Expression.")
    }
  }

  def injectValue[T](dataStructure: DataStructure, statements: StatementsList, injValue: T, readerIndextoUse: Int = 0)(implicit convertFunction: Option[List[(String, Any)] => T] = None): Codec = {
    val (auxType, codec): (Int, Codec) = dataStructure match {
      case Right(jsonString: String) =>
        val returnCodec = new CodecJson(jsonString)
        returnCodec.setReaderIndex(readerIndextoUse)

        jsonString.charAt(0) match{
          case '[' => (4, returnCodec.wrapInBrackets())
          case '{' => (3, returnCodec)
          case _ => (returnCodec.getDataType, returnCodec)
        }
      case Left(byteBuf: ByteBuf) => (4, new CodecBson(byteBuf))
    }

    /*
      val (auxType, codecToUse)= codec.getCodecData match {
          case Right(js) => js.charAt(0) match {
            case '[' => (4, codec.wrapInBrackets())
            case '{' => (3, codec)
            case _ => (codec.getDataType, codec) // Maybe
          }
          case Left(bb) => (4, codec)
        }
    * */

    val resCodec: Codec = statements.head._1 match {
      case ROOT =>
        injectRootValue(codec, injValue)

      case Key(key: String) =>
        injectKeyValue(codec, statements, injValue, key)

      case HalfName(half: String) =>
        injectKeyValue(codec, statements, injValue, half)

      case HasElem(key: String, elem: String) => injectHasElemValue(codec, statements, injValue, key, elem)

      case KeyWithArrExpr(key: String, arrEx: ArrExpr) =>
        val input: (String, Int, String, Any) =
          (arrEx.leftArg, arrEx.midArg, arrEx.rightArg) match {
            case (_, o1, o2) if o1.isDefined && o2.isDefined =>
              if (o1.get.value.equals(UNTIL_RANGE) && o2.get.isInstanceOf[Int]) {
                val to: Int = o2.get.asInstanceOf[Int]
                (key, arrEx.leftArg, TO_RANGE, to - 1)
              } else (key, arrEx.leftArg, o1.get.value, o2.get) //User sent, for example, Key[1 TO 3] translate to - Key[1 TO 3]

            case (_, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (key, arrEx.leftArg, TO_RANGE, arrEx.leftArg) //User sent, for example, Key[1] translates to - Key[1 TO 1]

            case (0, str, None) =>
              str.get.value match {
                case C_FIRST =>
                  (key, 0, TO_RANGE, 0) //User sent Key[first] , translates to - Key[0 TO 0]
                case C_END =>
                  (key, 0, C_END, None) //User sent Key[end], translates to - Key[0 END None]
                case C_ALL =>
                  (key, 0, TO_RANGE, C_END) //user sent Key[all], translates to - Key[0 TO END]
              }
          }
        injectArrayValueWithKey(codec, statements, injValue, input._1, input._2.toString, input._3, input._4.toString)

      case ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) =>
        val input: (String, Int, String, Any) =
          (leftArg, midArg, rightArg) match {
            case (i, o1, o2) if o1.isDefined && o2.isDefined =>
              if (o1.get.value.equals(UNTIL_RANGE) && o2.get.isInstanceOf[Int]) {
                val to: Int = o2.get.asInstanceOf[Int]
                (EMPTY_KEY, i, TO_RANGE, to - 1)
              }
              else (EMPTY_KEY, i, o1.get.value, o2.get)

            case (i, o1, o2) if o1.isEmpty && o2.isEmpty =>
              (EMPTY_KEY, i, TO_RANGE, i)

            case (0, str, None) =>
              str.get.value match {
                case C_FIRST => (EMPTY_KEY, 0, TO_RANGE, 0)
                case C_END => (EMPTY_KEY, 0, C_END, None)
                case C_ALL => (EMPTY_KEY, 0, TO_RANGE, C_END)
              }
          } //TODO - might need to change below the formerType
//        val (auxType, codecToUse)= codec.getCodecData match {
//          case Right(js) => js.charAt(0) match {
//            case '[' => (4, codec.wrapInBrackets())
//            case '{' => (3, codec)
//            case _ => (codec.getDataType, codec) // Maybe
//          }
//          case Left(bb) => (4, codec)
//        }
        //        val injectCodec = codec.wrapInBrackets()

        injectArrayValue(codec, statements, injValue, input._3, input._2.toString, input._4.toString, auxType)
    }
    resCodec
  }

}
