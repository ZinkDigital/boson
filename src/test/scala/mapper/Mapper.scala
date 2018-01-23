package mapper

import java.time.Instant
import java.util

import bsonLib.{BsonArray, BsonObject}
import io.boson.bson.bsonImpl.Constants._
import io.boson.bson.bsonImpl.CustomException
import io.netty.buffer.{ByteBuf, Unpooled}

import scala.collection.immutable.List
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by Ricardo Martins on 08/01/2018.
  */
case class CustomException(smth:String) extends Exception {
  override def getMessage: String = smth
}

object Mapper {
  import scala.collection.JavaConverters._

  def convert(smth: Any): Any = {
    smth match {
      case bsonObject: BsonObject => convertBsonObject(bsonObject)
      case bsonArray: BsonArray => convertBsonArray(bsonArray)
      case _ => throw CustomException("No Type to Convert")
    }
  }

  def convertBsonObject(bsonObject: BsonObject): Map[String, Any] = {
    //val map: mutable.Set[(String, Any)] = mutable.Set.empty
    val map =  new mutable.TreeMap[String, Any]()


    val bsonMap: Map[String, AnyRef] = bsonObject.getMap.asScala.toMap//.asInstanceOf[Map[String, Any]]
    //val entries: util.Set[util.Map.Entry[String, AnyRef]] =bsonObject.getMap.entrySet()// bsonMap.toSet
    val entries: Set[(String, AnyRef)] = bsonMap.toSet
    entries.foreach(entry => {
      val key: String = entry._1// getKey//_1
      val value: Any = entry._2//getValue//_2

      value match {
        case bsonObject: BsonObject =>
          map.put(key, convertBsonObject(bsonObject))
        case bsonArray: BsonArray =>
          map.put(key, convertBsonArray(bsonArray))
        case _ =>
          map.put(key, value)
      }

    })
    map.toMap
  }

  def convertBsonArray(array: BsonArray): List[Any] = {
    //val list: util.List[Any] = new util.LinkedList[Any]()
    val list: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]
    val bsonList: List[Any] = array.getList.asScala.asInstanceOf[List[Any]]

    bsonList.foreach {
      case bsonObject: BsonObject =>
        list.append(convertBsonObject(bsonObject))
      case bsonArray: BsonArray =>
        list.append(convertBsonArray(bsonArray))
      case entry =>
        list.append(entry)
    }
    list.toList
  }

  def getAllTypes(any: Any, tabs: Int = 0): Any = {
    var tab: String = ""
    var t: Int = tabs
    while (t != 0){
      tab = tab.concat("\t")
      t=t-1
    }
    any match {
      case map: util.Map[_, _] =>
        map.entrySet().forEach( e => {
          val key: String =  e.getKey.asInstanceOf[String]
          val value: Any = e.getValue
          value match {
            case map1: util.Map[_, _] =>
              println(s"$tab$key -> ${map1.getClass.getSimpleName}")
              getAllTypes(map1, tabs + 1)
            case list1: util.List[_] =>
              println(s"$tab$key -> ${list1.getClass.getSimpleName}")
              getAllTypes(list1, tabs + 1)
            case null =>
              println(s"$tab$key -> ${"Null"}")
            case _ =>
              println(s"$tab$key -> ${value.getClass.getSimpleName}")
          }} )
      case list: util.List[_] =>
        var i = 0
        list.forEach {
          case map1: util.Map[_, _] =>
            println(s"$tab$i -> ${map1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(map1, tabs + 1)
          case list1: util.List[_] =>
            println(s"$tab$i -> ${list1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(list1, tabs + 1)
          case e: Any =>
            println(s"$tab$i -> ${e.getClass.getSimpleName}")
            i = i + 1
        }
      case _ => println("Wrong input type")
    }
  }





  def encode(bson: Any): ByteBuf = {

    val res: ByteBuf =  bson match {
      case list: util.List[_] => encodeBsonArray(list.asScala.toList)
      case list: List[Any] => encodeBsonArray(list)
      case map : util.Map[String@unchecked, _] => encodeBsonObject(map.asScala.toMap)
      case map : Map[String@unchecked, _] => encodeBsonObject(map)
      case array: Array[Byte] => encodeBsonArray(array.toList)
      //case map : mutable.Map[String, _] => encodeBsonObject(map)
      case _ => throw CustomException("Wrong input type.")
    }
    println("Has Array? " + res.hasArray)
    if(res.hasArray) {
      res.array()
    }else{
      res.duplicate().array()
    }

    res
  }

  private def encodeBsonArray(list: List[Any]): ByteBuf = {
    val bufSize: ByteBuf = Unpooled.buffer(4)
    val buf: ByteBuf = Unpooled.buffer()
    val numElems: Int = list.size

    for( num <- 0 until numElems){
      val elem: Any = list(num)
      elem match {
        case x: Float =>
          println("D_FLOAT_DOUBLE")
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(num.toString.getBytes).writeZero(1).writeDoubleLE(x.toDouble)
        case x: Double =>
          println("D_FLOAT_DOUBLE")
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(num.toString.getBytes).writeZero(1).writeDoubleLE(x)
        case x: Array[Byte] =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x).writeZero(1)
        case x: Instant =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.toString.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: String =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x.getBytes).writeZero(1)
        case x: CharSequence =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: Map[String@unchecked, _] =>
          println("D_BSONOBJECT")
          buf.writeByte(D_BSONOBJECT).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonObject(x))
        /*case x: mutable.Map[String, _] =>
          println("D_BSONOBJECT")
          buf.writeByte(D_BSONOBJECT).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonObject(x))*/
        case x: List[Any] =>
          println("D_BSONARRAY")
          buf.writeByte(D_BSONARRAY).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonArray(x))
        /*case x: mutable.Buffer[_] =>
          println("D_BSONARRAY")
          buf.writeByte(D_BSONARRAY).writeBytes(num.toString.getBytes).writeZero(1).writeBytes(encodeBsonArray(x))*/
        case x if Option(x).isEmpty  =>
          buf.writeByte(D_NULL).writeBytes(num.toString.getBytes).writeZero(1)
        case x: Int =>
          println("D_INT")
          buf.writeByte(D_INT).writeBytes(num.toString.getBytes).writeZero(1).writeIntLE(x)
        case x: Long =>
          println("D_LONG")
          buf.writeByte(D_LONG).writeBytes(num.toString.getBytes).writeZero(1).writeLongLE(x)
        case x: Boolean =>
          println("D_BOOLEAN")
          buf.writeByte(D_BOOLEAN).writeBytes(num.toString.getBytes).writeZero(1).writeBoolean(x)
        case _ =>
          println("Something happened")
      }
    }
    buf.writeZero(1)
    buf.capacity(buf.writerIndex())
    bufSize.writeIntLE(buf.capacity()+4)
    Unpooled.copiedBuffer(bufSize, buf)
  }

  private def encodeBsonObject(map: Map[String, Any]): ByteBuf = {
    val bufSize: ByteBuf = Unpooled.buffer(4)
    val buf: ByteBuf = Unpooled.buffer()
    val numElems: List[(String, Any)] = map.toList

    for( num <- numElems){
      val elem: (String, Any) = num
      elem._2 match {
        case x: Float =>
          println("D_FLOAT_DOUBLE")
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(elem._1.getBytes()).writeZero(1).writeDoubleLE(x.toDouble)
        case x: Double =>
          println("D_FLOAT_DOUBLE")
          buf.writeByte(D_FLOAT_DOUBLE).writeBytes(elem._1.getBytes()).writeZero(1).writeDoubleLE(x)
        case x: Array[Byte] =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x).writeZero(1)
        case x: Instant =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.toString.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        case x: String =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x.getBytes).writeZero(1)
        case x: CharSequence =>
          println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
          buf.writeByte(D_ARRAYB_INST_STR_ENUM_CHRSEQ).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x.length+1).writeBytes(x.toString.getBytes()).writeZero(1)
        /*case x: mutable.Map[String, _] =>
          println("D_BSONOBJECT")
          buf.writeByte(D_BSONOBJECT).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonObject(x))*/
        case x: Map[String@unchecked, _] =>
          println("D_BSONOBJECT")
          buf.writeByte(D_BSONOBJECT).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonObject(x))
        case x: List[Any] =>
          println("D_BSONARRAY")
          buf.writeByte(D_BSONARRAY).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonArray(x))
        case x: List[Any] =>
          println("D_BSONARRAY")
          buf.writeByte(D_BSONARRAY).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonArray(x))
        /*case x: mutable.Buffer[_] =>
          println("D_BSONARRAY")
          buf.writeByte(D_BSONARRAY).writeBytes(elem._1.getBytes()).writeZero(1).writeBytes(encodeBsonArray(x))*/
        case x if Option(x).isEmpty  =>
          buf.writeByte(D_NULL).writeBytes(elem._1.getBytes()).writeZero(1)
        case x: Int =>
          println("D_INT")
          buf.writeByte(D_INT).writeBytes(elem._1.getBytes()).writeZero(1).writeIntLE(x)
        case x: Long =>
          println("D_LONG")
          buf.writeByte(D_LONG).writeBytes(elem._1.getBytes()).writeZero(1).writeLongLE(x)
        case x: Boolean =>
          println("D_BOOLEAN")
          buf.writeByte(D_BOOLEAN).writeBytes(elem._1.getBytes()).writeZero(1).writeBoolean(x)
        case _ =>
          println("Something happened")
      }
    }
    buf.writeZero(1)
    buf.capacity(buf.writerIndex())
    bufSize.writeIntLE(buf.capacity()+4)
    Unpooled.copiedBuffer(bufSize, buf)
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
              println("D_FLOAT_DOUBLE")
              val number: Double = buf.readDoubleLE()
              list.append(number)
            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
              val size: Int = buf.readIntLE()
              val str: Array[Byte] = Unpooled.copiedBuffer(buf.readBytes(size)).array()

              list.append(new String(str))
            case D_BSONOBJECT =>
              println("D_BSONOBJECT")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: Map[String, _] = decodeBsonObject(bson)
              list.append(res)
            case D_BSONARRAY =>
              println("D_BSONARRAY")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: List[Any] = decodeBsonArray(bson)
              list.append(res)
            case D_INT =>
              println("D_INT")
              val int: Int = buf.readIntLE()
              list.append(int)
            case D_NULL =>
              println("D_NULL")
            case D_LONG =>
              println("D_LONG")
              val long: Long = buf.readLongLE()
              list.append(long)
            case D_BOOLEAN =>
              println("D_BOOLEAN")
              val bool: Boolean = buf.readBoolean()
              list.append(bool)
            case _ =>
              println("Something happened")
          }
      }
    }
    list.toList
  }

  def decodeBsonObject(buf: ByteBuf): Map[String, Any] = {
    val startIndex: Int = buf.readerIndex()
    //val map: mutable.Set[(String, Any)] = mutable.Set.empty[(String, Any)]
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
          //key.append(b)
          val strKey: String = new String(key.toArray)
          println(strKey)
          dataType match {
            case D_FLOAT_DOUBLE =>
              println("D_FLOAT_DOUBLE")
              val number: Double = buf.readDoubleLE()
              map.put(strKey, number)


            case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
              println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
              val size: Int = buf.readIntLE()
              val str: Array[Byte] = Unpooled.copiedBuffer(buf.readBytes(size-1)).array()
              map.put(strKey, new String(str))
            case D_BSONOBJECT =>
              println("D_BSONOBJECT")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: Map[String, Any] = decodeBsonObject(bson)
              map.put(strKey, res)
            case D_BSONARRAY =>
              println("D_BSONARRAY")
              val bsonSize: Int = buf.getIntLE(buf.readerIndex())
              val bson: ByteBuf = buf.readBytes(bsonSize)
              val res: List[Any] = decodeBsonArray(bson)
              map.put(strKey, res)
            case D_INT =>
              println("D_INT")
              val int: Int = buf.readIntLE()
              map.put(strKey, int)
            case D_NULL =>
              println("D_NULL")
              map.put(strKey, null)
            case D_LONG =>
              println("D_LONG")
              val long: Long = buf.readLongLE()
              map.put(strKey, long)
            case D_BOOLEAN =>
              println("D_BOOLEAN")
              val bool: Boolean = buf.readBoolean()
              map.put(strKey, bool)
            case _ =>
              println("Something happened")
          }
      }
    }

    map.toMap
  }



}
