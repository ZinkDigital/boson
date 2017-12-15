package io.boson.bson.bsonImpl.injectors

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.bson.bsonImpl.Constants._
import java.io.ByteArrayOutputStream

import io.netty.util.ByteProcessor

import scala.collection.mutable.ListBuffer
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import io.boson.bson.bsonImpl.BosonImpl

import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 07/11/2017.
  */

object Mapper {

  val mapper: ObjectMapper = new ObjectMapper(new BsonFactory())

  def encode(obj: Any): Array[Byte] = {
    val os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Try(mapper.writeValue(os, obj)) match {
      case Success(_) =>
        os.flush()
        os.toByteArray
      case Failure(e) =>
        throw new RuntimeException(e)
    }
  }
}

class Injector {

  def modify(nettyOpt: Option[BosonImpl], fieldID: String, f: (Any) => Any): Option[BosonImpl] = {
    val bP: ByteProcessor = (value: Byte) => {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
      true
    }
    if (nettyOpt.isEmpty) {
      println(s" Input BosonImpl is Empty. ")
      None
    } else {
      val netty: BosonImpl = nettyOpt.get
      println(s"input buf size = ${netty.getByteBuf.capacity()} ")
      val buffer: ByteBuf = netty.getByteBuf.duplicate()
      val buff: ByteBuf = Unpooled.buffer(4)
      buffer.getBytes(0, buff, 4)
      val bufferSize: Int = buff.readIntLE() // buffer.readIntLE()
      println(s"buffer size = $bufferSize ")

      val seqType: Int = buffer.getByte(4).toInt
      println(s"seqType: $seqType")
      seqType match {
        case 0 => None // end of obj
        case _ =>
          buffer.getByte(5).toInt match {
            case 48 => // root obj is BsonArray, call extractFromBsonArray
              println("Root is BsonArray")
              if (fieldID.isEmpty) {
                Option(new BosonImpl())
              } else {
                println("Input capacity = " + buffer.capacity())
                val startRegionArray: Int = buffer.readerIndex()
                println(s"startRegionArray -> $startRegionArray")
                val valueTotalLength: Int = buffer.readIntLE()
                println(s"valueTotalLength -> $valueTotalLength")
                val indexOfFinishArray: Int = startRegionArray + valueTotalLength
                println(s"indexOfFinish -> $indexOfFinishArray")
                val (midResult, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer.duplicate(), fieldID, f) //buffer is intact so far
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                  val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf) //  adding the global size to result buffer
                  Some(new BosonImpl(byteArray = Option(result.array())))
                } getOrElse {
                  println("DIDN'T FOUND THE FIELD OF CHOICE TO INJECT, bsonarray as root, returning None")
                  None
                }
              }
            case _ => // root obj isn't BsonArray, call extractFromBsonObj
              println("Root is BsonObject")
              if (fieldID.isEmpty) {
                Option(new BosonImpl())
              } else {
                println("Input capacity = " + buffer.capacity())
                val startRegion: Int = buffer.readerIndex()
                println(s"startRegion -> $startRegion")
                val valueTotalLength: Int = buffer.readIntLE()
                println(s"valueTotalLength -> $valueTotalLength")
                val indexOfFinish: Int = startRegion + valueTotalLength
                println(s"indexOfFinish -> $indexOfFinish")
                val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
                midResult map { buf =>
                  val bufNewTotalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  calculates total size
                  val result: ByteBuf = Unpooled.copiedBuffer(bufNewTotalSize, buf)
                  val res = new BosonImpl(byteArray = Option(result.array()))
                  Some(res)
                } getOrElse {
                  println("DIDN'T FOUND THE FIELD OF CHOICE TO INJECT, bsonobject as root, returning None")
                  None
                }
              }
          }
      }
    }
  }

  private def compareKeys(buffer: ByteBuf, key: String): Boolean = {
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    buffer.readByte() // consume the end String byte

    println(s"............... $key")
    println(s"............... ${new String(fieldBytes.toArray)}")

    key.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep
  }

  def matcher(buffer: ByteBuf, fieldID: String, indexOfFinish: Int, f: Any => Any): (Option[ByteBuf], Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    //    val totalSize = indexOfFinish - startReaderIndex
    println(s"matcher..............startReaderIndex: $startReaderIndex")
    if (startReaderIndex < (indexOfFinish - 1)) { //  goes through entire object
      val seqType: Int = buffer.readByte().toInt
      println(s"matcher...........seqType: $seqType")
      if (compareKeys(buffer, fieldID)) { //  changes value if keys match
        println("FOUND FIELD")
        val indexTillInterest: Int = buffer.readerIndex()
        println(s"indexTillInterest -> $indexTillInterest")
        val bufTillInterest: ByteBuf = buffer.slice(4, indexTillInterest - 4)
        val (bufWithNewValue, diff): (ByteBuf, Int) = modifier(buffer, seqType, f) //  change the value
        val indexAfterInterest: Int = buffer.readerIndex()
        println(s"indexAfterInterest -> $indexAfterInterest")
        val bufRemainder: ByteBuf = buffer.slice(indexAfterInterest, buffer.capacity() - indexAfterInterest)
        val midResult: ByteBuf = Unpooled.wrappedBuffer(bufTillInterest, bufWithNewValue, bufRemainder)
        (Some(midResult), diff)
      } else {
        println("DIDNT FOUND FIELD")
        consume(seqType, buffer, fieldID, f) match { //  consume the bytes of value, NEED to check for bsobj and bsarray before consume
          case Some((buf, diff)) => (Some(buf), diff)
          case None => matcher(buffer, fieldID, indexOfFinish, f)
        }
      }
    } else {
      println("OBJECT FINISHED")
      buffer.readByte()
      (None, 0)
    }
  }

  def modifier(buffer: ByteBuf, seqType: Int, f: Any => Any): (ByteBuf, Int) = {
    val newBuffer: ByteBuf = Unpooled.buffer() //  corresponds only to the new value
    seqType match {
      case D_FLOAT_DOUBLE =>
        val value = f(buffer.readDoubleLE())
        value.getClass.getSimpleName match {
          case "Float" =>
            val aux: Float = value.asInstanceOf[Float]
            (newBuffer.writeDoubleLE(aux), 0)
          case "Double" =>
            val aux: Double = value.asInstanceOf[Double]
            (newBuffer.writeDoubleLE(aux), 0)
          case _ => ??? //  [IT,OT] => IT != OT
        }
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val length: Int = buffer.readIntLE()
        val value: Any = f(new String(Unpooled.copiedBuffer(buffer.readBytes(length)).array()))
        println("returning type = " + value.getClass.getSimpleName)
        value.getClass.getSimpleName match {
          case "byte[]" =>
            val aux: Array[Byte] = value.asInstanceOf[Array[Byte]]
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0), (aux.length + 1) - length)
          case "String" =>
            val aux: Array[Byte] = value.asInstanceOf[String].getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0), (aux.length + 1) - length)
          case "Instant" =>
            val aux: Array[Byte] = value.asInstanceOf[Instant].toString.getBytes()
            (newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0), (aux.length + 1) - length)
          //case "Enumerations" => //TODO
          case _ => ??? //  [IT,OT] => IT != OT
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.readIntLE() //  length of current obj
        val bsonObject: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonObject)
        //val buf: Buffer = newValue.asInstanceOf[BsonObject].encode()
        val arr: Array[Byte] = Mapper.encode(newValue.asInstanceOf[java.util.Map[String,_]])//Map[String,_]
        (newBuffer.writeBytes(arr), arr.size - valueLength)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonArray)
        val arr: Array[Byte] = Mapper.encode(newValue.asInstanceOf[java.util.List[_]])//Array[Any]
        (newBuffer.writeBytes(arr), arr.size - valueLength)
      case D_BOOLEAN =>
        val value: Any = f(buffer.readBoolean())
        val finalValue: Boolean = value.asInstanceOf[Boolean]
        (newBuffer.writeBoolean(finalValue), 0)
      case D_NULL => (newBuffer, 0) //  returns empty buffer
      case D_INT =>
        val value: Any = f(buffer.readIntLE())
        (newBuffer.writeIntLE(value.asInstanceOf[Int]), 0)
      case D_LONG =>
        val value: Any = f(buffer.readLongLE())
        (newBuffer.writeLongLE(value.asInstanceOf[Long]), 0)
    }
  }

  def consume(seqType: Int, buffer: ByteBuf, fieldID: String, f: Any => Any): Option[(ByteBuf, Int)] = {
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
        None
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        buffer.readBytes(valueLength)
        None
      case D_BSONOBJECT =>
        println("BSONOBJECT ")
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE() //  length of next BsonObject
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength //  where the next BsonObject ends
        val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
        midResult map { buf =>
          val oneBuf: ByteBuf = buf.slice(0, startRegion - 4) //previous till next object size
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size
          val threeBuf: ByteBuf = buf.slice(startRegion, buf.capacity() - startRegion) //  from size till end
          val fourBuf: ByteBuf = Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf) //  previous buffs together
          (fourBuf, diff)
        }
      case D_BSONARRAY =>
        println("D_BSONARRAY")
        println("Input capacity = " + buffer.capacity())
        val startRegionArray: Int = buffer.readerIndex()
        println(s"startRegionArray -> $startRegionArray")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinishArray: Int = startRegionArray + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinishArray")
        val (midResult, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer, fieldID, f) //buffer is intact so far, with buffer.duplicate doesnt work
        midResult map { buf =>
          val oneBuf: ByteBuf = buf.slice(0, startRegionArray - 4) //previous till next object size
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size
          val threeBuf: ByteBuf = buf.slice(startRegionArray, buf.capacity() - startRegionArray) //  from size till end
          val fourBuf: ByteBuf = Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf) //  previous buffs together
          (fourBuf, diff)
        }
      case D_BOOLEAN =>
        println("D_BOOLEAN")
        buffer.readByte()
        None
      case D_NULL =>
        println("D_NULL")
        None
      case D_INT =>
        println("D_INT")
        buffer.readIntLE()
        None
      case D_LONG =>
        println("D_LONG")
        buffer.readLongLE()
        None
      case _ =>
        println("Something happened")
        None
    }
  }

  private def readArrayPos(netty: ByteBuf): Char = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      list.+=(netty.readByte())
      i += 1
    }
    list.+=(netty.readByte()) //  consume the end Pos byte
    val stringList = list.map(b => b.toInt.toChar)
    println(list)
    stringList.head
  }

  def findBsonObjectWithinBsonArray(buffer: ByteBuf, fieldID: String, f: Any => Any): (Option[ByteBuf], Int) = {
    val seqType: Int = buffer.readByte()
    println(s"findBsonObjectWithinBsonArray____________________________seqType: $seqType")
    if (seqType == 0) {
      println("inside seqType == 0")
      //buffer.readerIndex(0) //  returns all buffer, including global size
      (None,0)
    } else { // get the index position of the array
      val index: Char = readArrayPos(buffer)
      println(s"findBsonObjectWithinBsonArray____________________________Index: $index")
      // match and treat each type
      processTypes(buffer, seqType, fieldID, f) match {
        case Some(elem) =>
          println("out of processTypes and got Some")
          (Some(elem._1),elem._2)
        case None =>
          println("Another None AGAIN")
          findBsonObjectWithinBsonArray(buffer, fieldID, f)
      }
    }
  }

  def processTypes(buffer: ByteBuf, seqType: Int, fieldID: String, f: Any => Any): Option[(ByteBuf, Int)] = {
    seqType match {
      case D_ZERO_BYTE =>
        println("case zero_byte")
        //buffer
        None
      case D_FLOAT_DOUBLE =>
        // process Float or Double
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        buffer.readBytes(valueLength)
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_BSONOBJECT =>
        // process BsonObjects
        println("BSONOBJECT ")
        println("Input capacity = " + buffer.capacity())
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinish")
        val (midResult, diff): (Option[ByteBuf], Int) = matcher(buffer, fieldID, indexOfFinish, f)
        midResult map { b =>
          val oneBuf: ByteBuf = b.slice(0, startRegion - 4)
          val twoBuf: ByteBuf = Unpooled.buffer(4).writeIntLE(valueTotalLength + diff) //  new size//previous till next object size
        val threeBuf: ByteBuf = b.slice(startRegion, b.capacity() - startRegion) //  from size till end
          (Unpooled.wrappedBuffer(oneBuf, twoBuf, threeBuf), diff) //  previous buffs together
        }
      case D_BSONARRAY =>
        // process BsonArrays
        println("D_BSONARRAY")
        val startRegion: Int = buffer.readerIndex()
        println(s"startRegion -> $startRegion")
        val valueTotalLength: Int = buffer.readIntLE()
        println(s"valueTotalLength -> $valueTotalLength")
        val indexOfFinish: Int = startRegion + valueTotalLength
        println(s"indexOfFinish -> $indexOfFinish")
        val (result, diff): (Option[ByteBuf], Int) = findBsonObjectWithinBsonArray(buffer, fieldID, f)
        result map { buf => (buf,diff) }
      case D_BOOLEAN =>
        // process Booleans
        println("D_BOOLEAN")
        buffer.readByte()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_NULL =>
        // process Null
        println("D_NULL")
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_INT =>
        // process Ints
        println("D_INT")
        buffer.readIntLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case D_LONG =>
        // process Longs
        println("D_LONG")
        buffer.readLongLE()
        //findBsonObjectWithinBsonArray(buffer, fieldID, f)
        None
      case _ =>
        //buffer
        println("Something happened")
        None
    }
  }
}
