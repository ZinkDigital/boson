package io.boson.injectors

import io.boson.bson.BsonObject
import io.boson.nettybson.NettyBson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._

import scala.collection.mutable.ListBuffer

/**
  * Created by Ricardo Martins on 07/11/2017.
  */

object Injector extends App{

  val obj: BsonObject = new BsonObject().put("field", 0).put("no", "ok")
  val netty: NettyBson = new NettyBson(vertxBuff = Option(obj.encode()))
  val inj: Injector = new Injector
  inj.modify(netty, "no", x => x)

}
class Injector {

  def modify(netty: NettyBson, fieldID: String, f: (Int)=>Int):NettyBson = {
    val buffer: ByteBuf = netty.getByteBuf.duplicate()
    val bufferSize: Int = buffer.readIntLE()
    val (fieldStartIndex: Int, fieldFinishIndex: Int) = findFieldID(buffer, fieldID, bufferSize)
    println(s"$bufferSize " )
    println(s"$fieldStartIndex -> $fieldFinishIndex" )

    //buffer or another duplicate???
    val bufferOriginalSize: ByteBuf = buffer.slice(0, 3)
    val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex-1)
    val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, bufferSize)
    /*
    * TODO
    * Do the work on the buffer of interest
    * */



    val v: ByteBuf = Unpooled.wrappedBuffer(bufferOriginalSize, bufferOfInterst, bufferRemainder)

    new NettyBson(byteBuf = Option(v))
  }

  def findFieldID(buffer: ByteBuf, fieldID: String, bufferSize: Int): (Int,Int) = {
    val startReaderIndex: Int = buffer.readerIndex()
    val seqType: Int = buffer.readByte().toInt
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buffer.getByte(buffer.readerIndex()) != 0) {
     fieldBytes.append(buffer.readByte())
    }
    buffer.readByte()

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
}
