package io.zink.boson.bson.codec.impl

import io.netty.buffer.{ByteBuf, PooledByteBufAllocator}
import io.zink.boson.bson.codec._
import io.zink.boson.bson.bsonImpl.Dictionary._

import scala.collection.mutable.ListBuffer

class CodecBson(arg: Array[Byte], opt: Option[ByteBuf] = None) extends Codec{
  val alloc: PooledByteBufAllocator = PooledByteBufAllocator.DEFAULT
  val buff: ByteBuf =opt match {
    case None => alloc.directBuffer(arg.length).writeBytes(arg)
    case Some(x) => x
  }

  override def getToken(tkn: SonNamedType): SonNamedType = tkn match {
    case SonBoolean(x, y) =>
      SonBoolean(x, buff.getByte(buff.readerIndex())==1)
    case SonArray(x,y) =>
      x match {
        case C_DOT =>
          val array: Array[Byte] = new Array[Byte](buff.capacity())
          buff.getBytes(0, array)
          SonArray(x, array)
        case "array" =>
          val size = buff.getIntLE(buff.readerIndex()-4)
          val arr: Array[Byte] = new Array[Byte](size)
          buff.getBytes(buff.readerIndex()-4, arr)
          SonArray(x, arr)
      }
    case SonObject(x,y) =>
      x match {
        case C_DOT =>
          val array: Array[Byte] = new Array[Byte](buff.capacity())
          buff.getBytes(0, array)
          SonObject(x, array)
        case "object" =>
          val size = buff.getIntLE(buff.readerIndex()-4)
          val arr: Array[Byte] = new Array[Byte](size)
          buff.getBytes(buff.readerIndex()-4, arr)
          SonObject(x, arr)
      }
    case SonString(x, y) =>
      x match {
        case "name" =>
          val buf0 = buff.duplicate()
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buf0.getByte(buf0.readerIndex()) != 0 || key.length<1) {
            val b: Byte = buf0.readByte()
            key.append(b)
          }
          SonString(x,  new String(key.toArray))
        case "string" =>
          val valueLength: Int = buff.getIntLE(buff.readerIndex())
          val arr: Array[Byte] = new Array[Byte](valueLength)
          buff.getBytes(buff.readerIndex()+4, arr)
          SonString(x, arr)
      }
    case SonNumber(x, y) =>
      x match {
        case "double" =>
          SonNumber(x, buff.getDoubleLE(buff.readerIndex()))
        case "int"=>
          SonNumber(x, buff.getIntLE(buff.readerIndex()))
        case "long"=>
          SonNumber(x, buff.getLongLE(buff.readerIndex()))
        case "arrayPosition"=>
          val buf = buff.duplicate()
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buf.getByte(buf.readerIndex()) != 0 || key.length<1) {
            val b: Byte = buf.readByte()
            key.append(b)
          }
          SonNumber(x, new String(key.toArray).toInt)
      }
  }

  override def readToken(tkn: SonNamedType): SonNamedType = tkn match {
    case SonBoolean(x, y) =>
      SonBoolean(x, buff.readByte() )
    case SonArray(x,y) =>
      x match {
        case C_DOT =>
          val array: Array[Byte] = new Array[Byte](buff.capacity())
          buff.readBytes(array)
          SonArray(x, array)
        case "array" =>
          val size = buff.getIntLE(buff.readerIndex()-4)
          val arr: Array[Byte] = new Array[Byte](size)
          buff.readerIndex(buff.readerIndex()-4)
          buff.readBytes(arr)
         SonArray(x, arr)
      }
    case SonObject(x,y) =>
      x match {
        case C_DOT =>
          val array: Array[Byte] = new Array[Byte](buff.capacity())
          buff.readBytes(array)
          SonObject(x, array)
        case "object" =>
          val size = buff.getIntLE(buff.readerIndex()-4)
          val arr: Array[Byte] = new Array[Byte](size)
          buff.readerIndex(buff.readerIndex()-4)
          buff.readBytes(arr)
          SonObject(x, arr)
      }
    case SonString(x, y) =>
      x match {
        case "name" =>
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buff.getByte(buff.readerIndex()) != 0 || key.length<1) {
            val b: Byte = buff.readByte()
            key.append(b)
          }
          val b: Byte = buff.readByte()
          SonString(x, new String(key.toArray.filter(p => p!=0)))
        case "string" =>
          val valueLength: Int = buff.readIntLE()
          val arr: Array[Byte] = new Array[Byte](valueLength)
          buff.readBytes(arr)
          SonString(x, new String(arr.filter(b => b !=0)))
      }
    case SonNumber(x, y) =>
      x match {
        case "double" =>
          val d = buff.readDoubleLE()
          SonNumber(x, d)
        case "int"=>
          SonNumber(x, buff.readIntLE())
        case "long"=>
          SonNumber(x, buff.readLongLE())
      }
    case SonNull(x, y)=>
      x match {
        case "null" =>
          SonNull(x, V_NULL)
      }
  }

  override def getValueAt(i: Int): Int = buff.getByte(i)

  override def getDataType: Int = buff.getByte(buff.readerIndex())

  override def readDataType: Int = buff.readByte()

  override def getReaderIndex: Int = buff.readerIndex()

  override def setReaderIndex(value: Int): Unit = if(value>=0)buff.readerIndex(value) else{ buff.readerIndex(buff.readerIndex()+value)}

  override def getWriterIndex: Int = buff.writerIndex()

  override def setWriterIndex(value: Int): Unit = buff.writerIndex(value)

  override def getSize: Int = buff.getIntLE(buff.readerIndex())

  override def readSize: Int = buff.readIntLE

  override def duplicate: Codec = {
    val newB = alloc.directBuffer(buff.capacity())
    buff.getBytes(0, newB)
    newB.readerIndex(buff.readerIndex())
    val c = new CodecBson(arg, Some(newB))
    c

  }

  override def rootType: SonNamedType = {
    val buf = buff.duplicate()
    if(buf.capacity()>5){
    buf.readerIndex(5)
    val key: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buf.getByte(buf.readerIndex()) != 0 || key.lengthCompare(1) < 0) {
      val b: Byte = buf.readByte()
      key.append(b)
    }
    val _: Byte = buf.readByte()
    if(key.forall(p => p.toChar.isDigit))
      SonArray(C_DOT)
    else
      SonObject(C_DOT)
  }else{
    SonZero
    }
  }

//  def printCodec() = {
//    val arrr = new Array[Byte](buff.capacity()-buff.readerIndex())
//    buff.getBytes(buff.readerIndex(), arrr)
//    arrr.foreach(b => print(b+" "))
//  }

  override def release(): Unit = buff.release()

  override def getArrayPosition: Int = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i = 0
    while (buff.getByte(buff.readerIndex()+i) != 0) {
      list.+=(buff.getByte(buff.readerIndex()+i))
      i+=1
    }
    new String(list.toArray.filter((b:Byte) => b != 0)).toInt
  }

  override def readArrayPosition: Int = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]

    while (buff.getByte(buff.readerIndex()) != 0) {
      list.+=(buff.readByte())
    }
    if(list.nonEmpty)list.+=(buff.readByte())
    new String(list.toArray.filter((b:Byte) => b != 0)).toInt
  }

  override def downOneLevel: Unit = {}
}
