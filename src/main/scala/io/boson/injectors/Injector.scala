package io.boson.injectors

import java.nio.ByteBuffer
import java.time.Instant
import java.util.NoSuchElementException

import com.sun.corba.se.impl.transport.ByteBufferPoolImpl
import io.boson.bson.{BsonArray, BsonObject}
import io.boson.injectors
import io.boson.nettybson.NettyBson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._
import io.boson.scalaInterface.ScalaInterface
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by Ricardo Martins on 07/11/2017.
  */



/*object e extends Enumeration{
  val A = ""
  val B = 54
}*/

object Injector extends App{

  val bytearray1: Array[Byte] = "AlguresPorAi".getBytes()
  val bytearray2: Array[Byte] = "4".getBytes()
  //val enum = io.boson.injectors.EnumerationTest.A
  //val enum1 =  io.boson.injectors.EnumerationTest.A
  //val enum = e.A
  //val enum1 =  e.B
  val inj: Injector = new Injector
  val ext = new ScalaInterface
  val ins: Instant = Instant.now()
  val ins1: Instant = Instant.now()
  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok").put("array", bytearray1).put("inst", ins)
  val netty: Option[NettyBson] = Some(new NettyBson(vertxBuff = Option(obj.encode())))

  val b1: NettyBson = inj.modify(netty, "x", x => x).get
  //println(new String(ext.parse(b1, "inst", "first").asInstanceOf[List[Array[Byte]]].head))
  val s: String = new String(ext.parse(b1, "enum", "first").asInstanceOf[List[Array[Byte]]].head).replaceAll("\\p{C}", "")
  println(s)

 /*
  println(s"b1 capacity = ${b1.getByteBuf.capacity()}")
  println(new String(ext.parse(b1, "no", "first").asInstanceOf[List[Array[Byte]]].head))
  val b2= inj.modify(b1, "no", x => x.asInstanceOf[String].concat("Whatever"))
  println(ext.parse(b2, "no", "first"))
  val b3= inj.modify(b2, "no", x =>  "Whatever")
  println(new String(ext.parse(b3, "no", "first").asInstanceOf[List[Array[Byte]]].head))
  println(ext.parse(b3, "field", "first"))
*/
}
class Injector {

  def modify(nettyOpt: Option[NettyBson], fieldID: String, f: (Any)=>Any): Option[NettyBson] = {
    if (nettyOpt.isEmpty) {
      println(s" Input NettyBson is Empty. ")
      None
    }else {
      val netty: NettyBson = nettyOpt.get
      println(s"input buf size = ${netty.getByteBuf.capacity()} ")
      val buffer: ByteBuf = netty.getByteBuf.duplicate()
      val bufferSize: Int = buffer.readIntLE()
      println(s"buffer size = $bufferSize ")
      try {
        //val (fieldStartIndex: Int, fieldFinishIndex: Int) =
        val (fieldStartIndex: Int, fieldFinishIndex: Int) = findFieldID(buffer, fieldID, bufferSize)

        println(s"$fieldStartIndex -> $fieldFinishIndex")

        //buffer or another duplicate???
        val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
        val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex)
        val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, bufferSize - fieldFinishIndex)
        /*
      * TODO
      * Do the work on the buffer of interest
      * break the bufferOriginalSize into 2 buffer : One with the entire buf size and other with the remaining
      * */
        //Verify the seqType with the type to update
        //call the updateValue function to update a specific field on the bufferOfInterest
        val seqType: Int = bufferOfInterst.readByte().toInt
        val newBuffer: ByteBuf = updateValues(bufferOfInterst, seqType, f)
        println(new String(newBuffer.array()))

        //Split bufferOriginalSize into 2 bytebuf
        println(s"bufferOriginalSize buf size = ${bufferOriginalSize.capacity()} ")
        println(s"newBuffer buf size = ${newBuffer.capacity()} ")
        println(s"bufferRemainder buf size = ${bufferRemainder.capacity()} ")

        //create the section that indicates the size of the entire buf
        val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBuffer.capacity() + bufferRemainder.capacity())
        val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4)

        //Putting the pieces together
        val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBuffer, bufferRemainder)


        println("result capacity = " + result.capacity())

        /* vertx buffer
      val vertxBuffer: BufferImpl = {
        val b: BufferImpl = new BufferImpl()
        val array: Array[Byte] = new Array[Byte](test.capacity())
        test.getBytes(0, array)
        b.setBytes(0,array )
        b
      }
      */
        /* java buffer nio
      val java: ByteBuffer = test.nioBuffer()
      */
        /* arrayByte => NEED TO CONVERT TO NIO AND THEN TO ARRAY[BYTE]
      val array: Array[Byte] = test.nioBuffer().array()
      */
        /* scala buffer ArrayBuffer
      val scala : ArrayBuffer[Byte] = new ArrayBuffer[Byte](test.capacity())
      test.nioBuffer().array().foreach(b => scala.+=(b))
      */

        //returning the resulting NettyBson
        val res = new NettyBson(byteBuf = Option(result))
        println("real result capacity = " + res.getByteBuf.capacity())
        Some(res)
        //Unpooled.buffer(bufferOriginalSize.capacity()+newBuffer.capacity()+bufferRemainder.capacity()).writeBytes(bufferOriginalSize).writeBytes(newBuffer).writeBytes(bufferRemainder))

      } catch {
        case e: NoSuchElementException => println(s"Exception Caught => ${e.getMessage}")
          nettyOpt
      }
    }
  }

  def findFieldID(buffer: ByteBuf, fieldID: String, bufferSize: Int): (Int,Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    if(startReaderIndex < (bufferSize-1)) {
      val seqType: Int = buffer.readByte().toInt
      val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]

      while (buffer.getByte(buffer.readerIndex()) != 0) {
        fieldBytes.append(buffer.readByte())
      }
      //fieldBytes.append(buffer.readByte())
      buffer.readByte()
      println(new String(fieldBytes.toArray))
      if (fieldID.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep) {
        consume(seqType, buffer)
        (startReaderIndex, buffer.readerIndex())
      } else {
        // keep looking
        consume(seqType, buffer)
        findFieldID(buffer, fieldID, bufferSize)
      }
    }else{
      throw new NoSuchElementException(s" Field $fieldID doesn´t exist.")
    }
  }

  def updateValues(buffer: ByteBuf, seqType: Int, f: (Any) => Any): ByteBuf = {
    val newBuffer: ByteBuf = Unpooled.buffer()
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    fieldBytes.append(buffer.readByte())

    newBuffer.writeByte(seqType.toByte)
    //println("add seqType " + new String(newBuffer.array()))
    fieldBytes.foreach( b => newBuffer.writeByte(b))
    //println("add field name "+new String(newBuffer.array()))
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        buffer.readDoubleLE()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        val value: ByteBuf = Unpooled.copiedBuffer(buffer.readBytes(valueLength))
        //println("NEW VALUE SIZE "+value.capacity())
        val array: Array[Byte] = value.array()
        //println(newBuffer.readerIndex())
        val newValue: Any = f(new String(array))//.toString
        writeNewValue(newBuffer, newValue, valueLength)
        //println("add new value "+new String(newBuffer.array()))
      case D_BSONOBJECT =>
        val bsonStartReaderIndex: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.readIntLE()
        val bsonObject: ByteBuf = buffer.readBytes(valueTotalLength)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength)
      case D_BOOLEAN =>
        val value: Int = buffer.readByte()
      case D_NULL =>
      case D_INT =>
        println("Int")
        val value: Int = buffer.readIntLE()
        val newValue: Any = f(value)
        writeNewValue(newBuffer, newValue)
        println("add new value "+new String(newBuffer.array()))
      case D_LONG =>
        val value: Long = buffer.readLongLE()
      case _ =>
    }
    Unpooled.buffer(newBuffer.writerIndex()).writeBytes(newBuffer)
  }

  def consume(seqType: Int, buffer: ByteBuf) : Unit = {
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        buffer.readDoubleLE()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        val value: ByteBuf = buffer.readBytes(valueLength)
      case D_BSONOBJECT =>
        val bsonStartReaderIndex: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.readIntLE()
        val bsonObject: ByteBuf = buffer.readBytes(valueTotalLength)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength)
      case D_BOOLEAN =>
        val value: Int = buffer.readByte()
      case D_NULL =>

      case D_INT =>
        val value: Int = buffer.readIntLE()
      case D_LONG =>
        val value: Long = buffer.readLongLE()
      case _ =>
    }
  }

  def writeNewValue(newBuffer: ByteBuf, newValue: Any, valueLength: Int = 0): Unit = {
    val returningType: String = newValue.getClass.getSimpleName
    println("returning type = "+returningType)
    val superclass: String = newValue.getClass.getGenericSuperclass.toString
    println("superclass type = "+superclass)
    returningType match {
      case "Integer" =>
        newBuffer.writeIntLE(newValue.asInstanceOf[Int])
      case "byte[]" =>
        val aux: Array[Byte] = newValue.asInstanceOf[Array[Byte]]
        newBuffer.writeIntLE(aux.length+1).writeBytes(aux).writeByte(0)
      case "String" =>
        val aux: Array[Byte] = newValue.asInstanceOf[String].getBytes()
        newBuffer.writeIntLE(aux.length+1).writeBytes(aux).writeByte(0)
      case "Instant" =>
        val aux: Array[Byte] = newValue.asInstanceOf[Instant].toString.getBytes()
        newBuffer.writeIntLE(aux.length +1).writeBytes(aux).writeByte(0)
      case "Enumerations" =>




    }
  }
}
