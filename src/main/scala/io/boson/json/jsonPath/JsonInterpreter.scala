package io.boson.json.jsonPath


import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.bson.bsonImpl.Constants._
import io.boson.bson.bsonImpl.CustomException

import scala.collection.immutable.List
import scala.collection.{immutable, mutable}
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 19/01/2018.
  */
class JsonInterpreter() {
  def runJsonEncoder(program: JsonProgram): ByteBuf = {
    val buf:ByteBuf = encodeJson(program.statement)
    buf
  }

  def encodeJson(statement: Statement, buffer:ByteBuf=Unpooled.buffer()): ByteBuf = {
    statement match {
      case expr:JsonParser =>
        expr.root match {
          case value:JsonObj =>
            val list: List[Statement] = value.list
            list.foreach(stat => {
              val result:ByteBuf = encodeJson(stat)
              result.capacity(result.writerIndex())
              buffer.writeBytes(result)
            })
            buffer.writeZero(1)
            buffer.capacity(buffer.writerIndex())
            Unpooled.buffer(buffer.capacity()+4).writeIntLE(buffer.capacity()+4).writeBytes(buffer)
          case value: JsonArray =>
            val list: List[Statement] = value.list
            list.foreach(stat => {
              val result:ByteBuf = encodeJson(stat)
              result.capacity(result.writerIndex())
              buffer.writeBytes(result)
            })
            buffer.writeZero(1)
            buffer.capacity(buffer.writerIndex())
            Unpooled.buffer(buffer.capacity()+4).writeIntLE(buffer.capacity()+4).writeBytes(buffer)
          case _ =>
            throw CustomException("Error encoding Json (JsonParser).")
        }
      case expr:JsonObj =>
        val list: List[Statement] = expr.list
        list.foreach(stat => {
          val result:ByteBuf = encodeJson(stat)
          result.capacity(result.writerIndex())
          buffer.writeBytes(result)
        })
        buffer.writeZero(1)
        buffer.capacity(buffer.writerIndex())
        Unpooled.buffer(buffer.capacity()+4).writeIntLE(buffer.capacity()+4).writeBytes(buffer)
      case expr:JsonArray =>
        val list: List[JsonValue] = expr.list
        val indexes: List[Int] = list.indices.toList
        list.zip(indexes).foreach(stat => {
          stat._1.value match{
            case value: JsonKey=>
              buffer.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ)
              buffer.writeBytes(stat._2.toString.getBytes).writeZero(1)
              val result: ByteBuf = encodeJson(value)
              result.capacity(result.writerIndex())
              buffer.writeIntLE(result.capacity()).writeBytes(result)
            case value: JsonNumber=>
              val result: ByteBuf = encodeJson(value)
              result.capacity(result.writerIndex())
              buffer.writeByte(result.readByte())
              buffer.writeBytes(stat._2.toString.getBytes).writeZero(1)
              buffer.writeBytes( result.readBytes(result.capacity()-1))
            case value: JsonArray=>
              buffer.writeByte(D_BSONARRAY)
              buffer.writeBytes(stat._2.toString.getBytes).writeZero(1)
              val result: ByteBuf = encodeJson(value)
              result.capacity(result.writerIndex())
              buffer.writeBytes(result)
            case value: JsonObj=>
              buffer.writeByte(D_BSONOBJECT)
              buffer.writeBytes(stat._2.toString.getBytes).writeZero(1)
              val result: ByteBuf = encodeJson(value)
              result.capacity(result.writerIndex())
              buffer.writeBytes(result)
            case _ =>
              throw CustomException("Error encoding Json (JsonArray).")
          }
        })
        buffer.writeZero(1)
        buffer.capacity(buffer.writerIndex())
        Unpooled.buffer(buffer.capacity()+4).writeIntLE(buffer.capacity()+4).writeBytes(buffer)
      case expr: JsonEntry =>
        val key: JsonKey = expr.key
        val value: JsonValue = expr.value
        value.value match{
          case k: JsonKey=>
            buffer.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ)
            val result: ByteBuf = encodeJson(key)
            result.capacity(result.writerIndex())
            buffer.writeBytes(result)
            val result1: ByteBuf = encodeJson(k)
            result1.capacity(result1.writerIndex())
            buffer.writeIntLE(result1.capacity()).writeBytes(result1)
            buffer
          case value: JsonNumber=>
            val result: ByteBuf = encodeJson(value)
            result.capacity(result.writerIndex())
            buffer.writeByte(result.readByte())
            val resultk: ByteBuf = encodeJson(key)
            resultk.capacity(resultk.writerIndex())
            buffer.writeBytes(resultk)
            buffer.writeBytes( result.readBytes(result.capacity()-1))
          case value: JsonArray=>
            buffer.writeByte(D_BSONARRAY)
            val result: ByteBuf = encodeJson(key)
            buffer.writeBytes(result)
            val result1: ByteBuf = encodeJson(value)
            result1.capacity(result1.writerIndex())
            buffer.writeBytes(result1)
            buffer
          case value: JsonObj=>
            buffer.writeByte(D_BSONOBJECT)
            val result: ByteBuf = encodeJson(key)
            result.capacity(result.writerIndex())
            buffer.writeBytes(result)
            val result1: ByteBuf = encodeJson(value)
            result1.capacity(result1.writerIndex())
            buffer.writeBytes(result1)
            buffer
          case _ =>
            throw CustomException("Error encoding Json (JsonEntry).")
        }
      case expr: JsonValue =>
        expr.value match{
          case expr:JsonKey=>
            val result: ByteBuf = encodeJson(expr)
            result.capacity(result.writerIndex())
            buffer.writeIntLE(result.capacity()).writeBytes(result)
          case expr:JsonNumber=>
            val result: ByteBuf = encodeJson(expr)
            result.capacity(result.writerIndex())
            buffer.writeBytes(result)
          case expr:JsonObj=>
            val result: ByteBuf = encodeJson(expr)
            result.capacity(result.writerIndex())
            buffer.writeBytes(result)
          case expr:JsonArray=>
            val result: ByteBuf = encodeJson(expr)
            result.capacity(result.writerIndex())
            buffer.writeBytes(result)
          case _ =>
            throw CustomException("Error encoding Json (JsonValue)")
        }
      case expr: JsonKey =>
        buffer.writeBytes(expr.key.getBytes).writeZero(1)
        buffer.capacity(buffer.writerIndex())
        buffer
      case expr: JsonNumber =>
        val number: String = expr.number
        if(number.contains(".")){
          buffer.writeByte(D_FLOAT_DOUBLE).writeDoubleLE(number.toDouble)
        }else {
          Try(number.toInt) match {
            case Success(i) => buffer.writeByte(D_INT).writeIntLE(i)
            case Failure(_) => buffer.writeByte(D_LONG).writeLongLE(number.toLong)
          }
        }
        buffer.capacity(buffer.writerIndex())
        buffer
    }
  }

  def decodeJson(buffer: ByteBuf): String = {
    val array:Boolean = buffer.getByte(5) == 48 && buffer.getByte(6) == 0

    val res: immutable.Iterable[Any] with PartialFunction[Int with String, Any] = if(array) decodeBsonArray(buffer) else decodeBsonObject(buffer)

    //println(res)
    toJson(res)

  }

  def decodeBsonArray(buf: ByteBuf): List[Any] = {
    val startIndex: Int = buf.readerIndex()
    val list: ListBuffer[Any] = new ListBuffer[Any]
    val bufSize: Int = buf.readIntLE()
    while((buf.readerIndex()-startIndex)<bufSize) {
      val dataType: Int = buf.readByte()
      dataType match{
        case 0 =>
        case _ =>
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
            val b: Byte = buf.readByte()
            key.append(b)
          }
          val b: Byte = buf.readByte()
          key.append(b)

          dataType match {
            case D_FLOAT_DOUBLE =>
              //println("D_FLOAT_DOUBLE")
              val number: Double = buf.readDoubleLE()
              list.append(number)
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              //println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
              val size: Int = buf.readIntLE()
              val str: Array[Byte] = Unpooled.copiedBuffer(buf.readBytes(size)).array()
              list.append(new String(str))
            case D_BSONOBJECT =>
              //println("D_BSONOBJECT")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: Map[String, _] = decodeBsonObject(bson)
              list.append(res)
            case D_BSONARRAY =>
              //println("D_BSONARRAY")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: List[Any] = decodeBsonArray(bson)
              list.append(res)
            case D_INT =>
              //println("D_INT")
              val int: Int = buf.readIntLE()
              list.append(int)
            case D_NULL =>
              //println("D_NULL")
            case D_LONG =>
              //println("D_LONG")
              val long: Long = buf.readLongLE()
              list.append(long)
            case D_BOOLEAN =>
              //println("D_BOOLEAN")
              val bool: Boolean = buf.readBoolean()
              list.append(bool)
            case _ =>
              //println("Something happened")
          }
      }
    }
    list.toList
  }

  def decodeBsonObject(buf: ByteBuf): Map[String, Any] = {
    val startIndex: Int = buf.readerIndex()
    val map =  new mutable.TreeMap[String, Any]()
    val bufSize: Int = buf.readIntLE()
    while((buf.readerIndex()-startIndex)<bufSize) {
      val dataType: Int = buf.readByte()
      dataType match {
        case 0 =>
        case _ =>
          val key: ListBuffer[Byte] = new ListBuffer[Byte]
          while (buf.getByte(buf.readerIndex()) != 0 || key.length < 1) {
            val b: Byte = buf.readByte()
            key.append(b)
          }
          val b: Byte = buf.readByte()
          val strKey: String = new String(key.toArray)
          //println(strKey)
          dataType match {
            case D_FLOAT_DOUBLE =>
              //println("D_FLOAT_DOUBLE")
              val number: Double = buf.readDoubleLE()
              map.put(strKey, number)
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              //println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
              val size: Int = buf.readIntLE()
              val str: Array[Byte] = Unpooled.copiedBuffer(buf.readBytes(size-1)).array()
              map.put(strKey, new String(str))
            case D_BSONOBJECT =>
              //println("D_BSONOBJECT")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: Map[String, Any] = decodeBsonObject(bson)
              map.put(strKey, res)
            case D_BSONARRAY =>
              //println("D_BSONARRAY")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: List[Any] = decodeBsonArray(bson)
              map.put(strKey, res)
            case D_INT =>
              //println("D_INT")
              val int: Int = buf.readIntLE()
              map.put(strKey, int)
            case D_NULL =>
              //println("D_NULL")
              map.put(strKey, null)
            case D_LONG =>
              //println("D_LONG")
              val long: Long = buf.readLongLE()
              map.put(strKey, long)
            case D_BOOLEAN =>
              //println("D_BOOLEAN")
              val bool: Boolean = buf.readBoolean()
              map.put(strKey, bool)
            case _ =>
              //println("Something happened")
          }
      }
    }

    map.toMap
  }

  def toJson(o: Any) : String = {
    val json = new ListBuffer[String]()
    o match {
      case m: Map[_,_] =>
        for ( (k,v) <- m ) {
          val key: String = escape(k.asInstanceOf[String])
          v match {
            case a: Map[_,_] => json += "\"" + key + "\":" + toJson(a)
            case a: List[_] => json += "\"" + key + "\":" + toJson(a)
            case a: Int => json += "\"" + key + "\":" + a
            case a: Double => json += "\"" + key + "\":" + a
            case a: Boolean => json += "\"" + key + "\":" + a
            case a: String => json += "\"" + key + "\":\"" + escape(a) + "\""
            case _ => ;
          }
        }
        "{" + json.mkString(",") + "}"
      case m: List[_] =>
        val list = new ListBuffer[String]()
        for ( el <- m ) {
          el match {
            case a: Map[_,_] => list += toJson(a)
            case a: List[_] => list += toJson(a)
            case a: Int => list += a.toString
            case a: Double => list += a.toString
            case a: Boolean => list += a.toString
            case a: String => list += "\"" + escape(a) + "\""
            case _ =>
          }
        }
        "[" + list.mkString(",") + "]"
    }
  }

  private def escape(s: String) : String = {
     s.replaceAll("\"" , "\\\\\"")
  }
}
