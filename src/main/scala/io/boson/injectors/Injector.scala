package io.boson.injectors

import java.nio.ByteBuffer

import com.sun.corba.se.impl.transport.ByteBufferPoolImpl
import io.boson.bson.BsonObject
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

object Injector extends App{

  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok")
  val netty: NettyBson = new NettyBson(vertxBuff = Option(obj.encode()))
  val inj: Injector = new Injector
  val ext = new ScalaInterface

  val b1= inj.modify(netty, "no", x => "no")

  println(s"b1 capacity = ${b1.getByteBuf.capacity()}")
  println(new String(ext.parse(b1, "no", "first").asInstanceOf[List[Array[Byte]]].head))

  val b2= inj.modify(b1, "no", x => "yes")

  println(ext.parse(b2, "no", "first"))

  val b3= inj.modify(b2, "no", x => "maybe")
  println(new String(ext.parse(b3, "no", "first").asInstanceOf[List[Array[Byte]]].head))
  //println(ext.parse(b3, "field", "first"))

}
class Injector {

  def modify(netty: NettyBson, fieldID: String, f: (Any)=>Any):NettyBson = {
    val buffer: ByteBuf = netty.getByteBuf.duplicate().capacity(netty.writerIndex).readerIndex(0)
    val bufferSize: Int = buffer.readIntLE()
    println(s"$bufferSize " )
    val (fieldStartIndex: Int, fieldFinishIndex: Int) = findFieldID(buffer, fieldID, bufferSize)

    println(s"$fieldStartIndex -> $fieldFinishIndex" )

    //buffer or another duplicate???
    val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
    val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex-fieldStartIndex)
    val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, bufferSize-fieldFinishIndex)
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
    println("bufferOriginalSize "+ bufferOriginalSize.capacity())
    val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity()+newBuffer.capacity()+bufferRemainder.capacity())
    val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4,bufferOriginalSize.capacity() - 4)

    //create the section that indicates the size of the entire buf
    val newSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity()+newBuffer.capacity()+bufferRemainder.capacity())
    println("newSize reder index " + newSize.readerIndex() + " writer index " + newSize.writerIndex())


    //Putting the pieces together
    val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer,newBuffer,bufferRemainder)
    //val test1 = Unpooled.c
    println(result.capacity())

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
    new NettyBson(byteBuf = Option(result))
      //Unpooled.buffer(bufferOriginalSize.capacity()+newBuffer.capacity()+bufferRemainder.capacity()).writeBytes(bufferOriginalSize).writeBytes(newBuffer).writeBytes(bufferRemainder))
  }

  def findFieldID(buffer: ByteBuf, fieldID: String, bufferSize: Int): (Int,Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    val seqType: Int = buffer.readByte().toInt
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buffer.getByte(buffer.readerIndex()) != 0) {
     fieldBytes.append(buffer.readByte())
    }
    //fieldBytes.append(buffer.readByte())
    buffer.readByte()
println(new String(fieldBytes.toArray))
    if(fieldID.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep){
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

      (startReaderIndex, buffer.readerIndex())
    }else{
      // keep looking
      findFieldID(buffer, fieldID, bufferSize)
    }
  }

  def updateValues(buffer: ByteBuf, seqType: Int, f: (Any) => Any): ByteBuf = {
    val newBuffer: ByteBuf = Unpooled.buffer()
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]

    println(s"buffer initial capacity ${buffer.capacity()}")

    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    fieldBytes.append(buffer.readByte())

   // newBuffer.writeBytes(fieldBytes.toArray)
    newBuffer.writeByte(seqType.toByte)
    println("add seqType " + new String(newBuffer.array()))
    fieldBytes.foreach( b => newBuffer.writeByte(b))
    println("add field name "+new String(newBuffer.array()))
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        buffer.readDoubleLE()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        val valueLength: Int = buffer.readIntLE()
        val value: ByteBuf = buffer.readBytes(valueLength)
        val newValue: Any = f(value)

        newBuffer.writeIntLE(newValue.asInstanceOf[String].length+1).writeBytes(newValue.asInstanceOf[String].getBytes).writeByte(0)
        println("add new value "+new String(newBuffer.array()))
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
        val newValue = f(value)
        newBuffer.writeIntLE(newValue.asInstanceOf[Int])
        println("add new value "+new String(newBuffer.array()))
      case D_LONG =>
        val value: Long = buffer.readLongLE()
      case _ =>
    }
    println("size "+newBuffer.capacity())
    val aux = Unpooled.wrappedBuffer(newBuffer)
println(s"updateValue() : ${newBuffer.capacity(newBuffer.writerIndex()).capacity()}")
    Unpooled.buffer(newBuffer.capacity()).writeBytes(newBuffer)
  }
}
