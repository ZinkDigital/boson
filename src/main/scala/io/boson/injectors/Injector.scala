package io.boson.injectors

import java.time.Instant
import io.boson.bson.{BsonArray, BsonObject}
import io.boson.nettybson.NettyBson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._
import io.boson.scalaInterface.ScalaInterface
import io.netty.util.ByteProcessor
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 07/11/2017.
  */


object e extends Enumeration {
  val A: e.Value = Value("Asdghrt")
  val B: e.Value = Value("Bdysrtyry")
}

object Injector extends App {

  val bytearray1: Array[Byte] = "AlguresPorAi".getBytes()
  val bytearray2: Array[Byte] = "4".getBytes()
  val float: Float = 11.toFloat
  val newFloat: Float = 15.toFloat
  val bObj: BsonObject = new BsonObject().put("bsonObj", "ola")
  val newbObj: BsonObject = new BsonObject().put("newbsonObj", "newbsonObj")
  val bool: Boolean = true
  val newBool: Boolean = false
  val long: Long = 100000001.toLong
  val newLong: Long = 200000002.toLong
  val bsonArray: BsonArray = new BsonArray().add(new BsonObject().put("field", "1")).add(new BsonObject().put("hg", 2)).add(new BsonObject().put("field", "2"))
  //.add(1).add(2).add("Hi")
  val newbsonArray: BsonArray = new BsonArray().add(3).add(4).add("Bye")
  val enumJava = io.boson.injectors.EnumerationTest.A
  val newEnumJava = io.boson.injectors.EnumerationTest.B
  val charseq: CharSequence = "charSequence"
  val anotherCharseq: CharSequence = "AnothercharSequence"
  val inj: Injector = new Injector
  val ext = new ScalaInterface
  val ins: Instant = Instant.now()
  val ins1: Instant = Instant.now()
  //val obj1: BsonObject = new BsonObject().put("bsonArray", bsonArray).putNull("null").put("enum", e.A.toString)//.put("field", 0).put("bool", bool).put("long", long).put("no", "ok").put("float", float).put("bObj",bObj).put("charS", charseq).put("array", bytearray1).put("inst", ins)

  val obj1: BsonObject = new BsonObject().put("fridgeTemp", 5.2f).put("fanVelocity", 20.5).put("doorOpen", false)
  val obj2: BsonObject = new BsonObject().put("fridgeTemp", 5.0f).put("fanVelocity", 20.6).put("doorOpen", false)
  val obj3: BsonObject = new BsonObject().put("fridgeTemp", 3.854f).put("fanVelocity", 20.5).put("doorOpen", true)
  val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeReadings", arr)

  val obj: BsonArray = bsonArray
  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  val bufferedSource: Source = Source.fromURL(getClass.getResource("/jsonOutput.txt"))
  val finale: String = bufferedSource.getLines.toSeq.head
  bufferedSource.close
  val json: JsonObject = new JsonObject(finale)
  val bson: BsonObject = new BsonObject(json)
  val boson: Option[NettyBson] = Some(ext.createNettyBson(bson.encode().getBytes))

  val b1: Try[NettyBson] = Try(inj.modify(boson, "Epoch", _ => 10).get)

  println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------------")


  b1 match {
    case Success(v) =>
      val s: Any = ext.parse(v, "Epoch", "first")
      //println(s.getClass.getSimpleName)
      println(s"size of result list afeter extraction: ${s.asInstanceOf[List[Any]].size}")
      println("-------------------------------------------------------- " + s.asInstanceOf[List[Any]].foreach(elem => println(elem)))
    case Failure(e) => println(e.getStackTrace.foreach(p => println(p.toString)))
  }
}

object ChangeStrInsideManyObj extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val obj2: BsonObject = new BsonObject().put("Its", "Me!!!")
  val obj1: BsonObject = new BsonObject().put("Hi", obj2)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", obj1)
  //.put("sec", 1)//.put("bool", true)
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  netty.get.getByteBuf.forEachByte(bP)
  val b1: Try[NettyBson] = Try(inj.modify(netty, "Its", _ => "Y").get)

  b1 match {
    case Success(v) =>
      v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting the field injected with value: " + new String(sI.parse(v, "Its", "last").asInstanceOf[List[Array[Byte]]].head))

    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }


}

object changeObj extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val obj1: BsonObject = new BsonObject().put("Hi", "Me!!!")
  val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", obj1)
  //.put("sec", 1)//.put("bool", true)
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  netty.get.getByteBuf.forEachByte(bP)
  val obj2: BsonObject = new BsonObject().put("Hi", "0123456789")
  val b1: Try[NettyBson] = Try(inj.modify(netty, "fridgeTemp", _ => obj2).get)

  b1 match {
    case Success(v) =>
      v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting the field injected with value: " + sI.parse(v, "fridgeTemp", "last").asInstanceOf[List[BsonObject]].head)

    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }
}

object Testing1 extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val obj2: BsonObject = new BsonObject().put("Its", "Me!!!")
  val obj1: BsonObject = new BsonObject().put("Hi", 10)
  val array1: BsonArray = new BsonArray().add(obj2).add("oi").add(2).add(obj2)
  val bsonEvent: BsonObject = new BsonObject().put("fridgeTemp", obj1)
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(array1.encode().getBytes)))

  println(array1)
  println(array1.encode())

  netty.get.getByteBuf.forEachByte(bP)

  val b1: Try[NettyBson] = Try(inj.modify(netty, "Its", _ => "Y").get)

  b1 match {
    case Success(v) =>
      v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting the field injected with value: ")
      sI.parse(v, "Its", "all").asInstanceOf[List[Array[Byte]]].foreach(elem => println("-> " + new String(elem)))

    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }


}

object ObjAsRoot extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", "Hi").put("bool", true)
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  netty.get.getByteBuf.forEachByte(bP)
  val b1: Try[NettyBson] = Try(inj.modify(netty, "fridgeTemp", _ => "12345").get)

  b1 match {
    case Success(v) =>
      v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting the field injected with value: " + new String(sI.parse(v, "fridgeTemp", "all").asInstanceOf[List[Array[Byte]]].head))

    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }


}

object ArrInsideObj extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val array1: BsonArray = new BsonArray().add(1).add(2)
  val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!")
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  netty.get.getByteBuf.forEachByte(bP)
  val b1: Try[NettyBson] = Try(inj.modify(netty, "bool", _ => "true").get)

  b1 match {
    case Success(v) =>
      v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting the field injected with value: " + new String(sI.parse(v, "bool", "all").asInstanceOf[List[Array[Byte]]].head))

    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }


}

object BsObjRootWithDeepMix extends App {

  val bP: ByteProcessor = (value: Byte) => {
    println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
    true
  }
  val obj3: BsonObject = new BsonObject().put("John", "NoBody")
  val obj2: BsonObject = new BsonObject().put("John", "Locke")
  val arr2: BsonArray = new BsonArray().add(obj2)
  val obj1: BsonObject = new BsonObject().put("hey", "me").put("will", arr2)
  val array1: BsonArray = new BsonArray().add(1).add(2).add(obj1)
  val bsonEvent: BsonObject = new BsonObject().put("sec", 1).put("fridgeTemp", array1).put("bool", "false!!!").put("finally", obj3)
  val inj: Injector = new Injector

  val netty: Option[NettyBson] = Some(new NettyBson(byteArray = Option(bsonEvent.encode().getBytes)))

  println(bsonEvent)
  println(bsonEvent.encode())

  //netty.get.getByteBuf.forEachByte(bP)
  val b1: Try[NettyBson] = Try(inj.modify(netty, "John", _ => "SomeBody").get)

  b1 match {
    case Success(v) =>
      //v.getByteBuf.forEachByte(bP)
      val sI: ScalaInterface = new ScalaInterface
      println("Extracting values of key \"John\" : ")
      sI.parse(v, "John", "all").asInstanceOf[List[Array[Byte]]].foreach(elem => println("-> " +new String(elem)))
    case Failure(e) =>
      println(e.getMessage)
      println(e.getStackTrace.foreach(p => println(p.toString)))
  }


}

class Injector {

  def modify(nettyOpt: Option[NettyBson], fieldID: String, f: (Any) => Any): Option[NettyBson] = {
    if (nettyOpt.isEmpty) {
      println(s" Input NettyBson is Empty. ")
      None
    } else {
      val netty: NettyBson = nettyOpt.get
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
                Option(new NettyBson())
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
                  val result: ByteBuf = Unpooled.wrappedBuffer(bufNewTotalSize, buf) //  adding the global size to result buffer
                  Some(new NettyBson(byteBuf = Option(result)))
                } getOrElse {
                  println("DIDN'T FOUND THE FIELD OF CHOICE TO INJECT, bsonarray as root, returning None")
                  None
                }
              }
            case _ => // root obj isn't BsonArray, call extractFromBsonObj
              println("Root is BsonObject")
              if (fieldID.isEmpty) {
                Option(new NettyBson())
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
                  val result: ByteBuf = Unpooled.wrappedBuffer(bufNewTotalSize, buf)
                  val res = new NettyBson(byteBuf = Option(result))
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
        }
      case D_BSONOBJECT =>
        val valueLength: Int = buffer.readIntLE() //  length of current obj
      val bsonObject: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonObject)
        val buf: Buffer = newValue.asInstanceOf[BsonObject].encode()
        (newBuffer.writeBytes(buf.getByteBuf), buf.length() - valueLength)
      case D_BSONARRAY =>
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength - 4)
        val newValue: Any = f(bsonArray)
        val buf: Buffer = newValue.asInstanceOf[BsonArray].encode()
        (newBuffer.writeBytes(buf.getByteBuf), 0) //  ZERO  for now, cant be zero
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

  //  def findFieldID(buffer: ByteBuf, fieldID: String, bufferSize: Int): (Int, Int) = {
  //
  //    //    while(buffer.readerIndex()< buffer.writerIndex()){
  //    //      println(s"*** -> ${buffer.readByte()}")
  //    //    }
  //    //val bufferSize: Int = buffer.readIntLE()  //getIntLE(0)
  //    println(s"findFieldID...........bufferSize: $bufferSize")
  //    val startReaderIndex: Int = buffer.readerIndex()
  //    if (startReaderIndex < (bufferSize - 1)) {
  //      //buffer.readIntLE()// consume the size of the buffer
  //      val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
  //      val seqType: Int = buffer.readByte().toInt
  //      println(s"findFieldID...........seqType: $seqType")
  //      while (buffer.getByte(buffer.readerIndex()) != 0) {
  //        fieldBytes.append(buffer.readByte())
  //      }
  //      buffer.readByte()
  //      println(s"............... $fieldID")
  //      println(s"............... ${new String(fieldBytes.toArray)}")
  //      if (fieldID.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep) {
  //        println("FOUND NAME")
  //        consume(seqType, buffer)
  //        println(s"findFieldID...........startReaderIndex: $startReaderIndex, buffer.readerIndex() -> ${buffer.readerIndex()}")
  //        (startReaderIndex, buffer.readerIndex())
  //      } else {
  //        println("DIDNT FOUND NAME")
  //        // keep looking
  //        consume(seqType, buffer)
  //        findFieldID(buffer, fieldID, bufferSize)
  //      }
  //    } else {
  //      println("No FieldID found!!")
  //      //throw new NoSuchElementException(s" Field $fieldID doesn´t exist.")
  //      (0, 0)
  //    }
  //  }

  //  def updateValues(buffer: ByteBuf, f: (Any) => Any): ByteBuf = {
  //    // new buffer to return
  //    val newBuffer: ByteBuf = Unpooled.buffer()
  //    //gget the seqType
  //    val seqType: Int = buffer.readByte().toInt
  //    println(s"updateValues!!!!!!!!!!!!!!!!!!!!seqType -> $seqType")
  //    // get the name of the field
  //    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
  //    while (buffer.getByte(buffer.readerIndex()) != 0) {
  //      fieldBytes.append(buffer.readByte())
  //    }
  //    println(s"updateValues!!!!!!!!!!!!!!!!!!!!fieldBytes -> ${new String(fieldBytes.toArray)}")
  //    fieldBytes.append(buffer.readByte()) //zero byte
  //    // write the seqType in returning buffer
  //    newBuffer.writeByte(seqType.toByte)
  //    // write the field name in returning buffer
  //    fieldBytes.foreach(b => newBuffer.writeByte(b))
  //    // updated the new value in returning buffer
  //    seqType match {
  //      case D_ZERO_BYTE => None
  //      case D_FLOAT_DOUBLE =>
  //        // get the present value
  //        val value: Double = buffer.readDoubleLE()
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(value)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
  //        // get the size of the value
  //        val valueLength: Int = buffer.readIntLE()
  //        // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
  //        val value: ByteBuf = Unpooled.copiedBuffer(buffer.readBytes(valueLength))
  //        // get the value in Array[Byte] with the array() function
  //        val array: Array[Byte] = value.array()
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(new String(array))
  //        // write the new value in the returning buffer
  //        println(s"updateValues!!!!!!!!!!!!!!!!!!!!valueLength -> $valueLength")
  //        writeNewValue(newBuffer, newValue, seqType, valueLength) //(newbuffer, funcWithArg, 2, 2)
  //      case D_BSONOBJECT =>
  //        // get the size of the value
  //        val valueTotalLength: Int = buffer.readIntLE() //TODO
  //      // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
  //      val bsonObject: ByteBuf = Unpooled.copiedBuffer(buffer.readBytes(valueTotalLength - 4))
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(bsonObject)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case D_BSONARRAY =>
  //        // get the size of the value
  //        val valueLength: Int = buffer.readIntLE() // TODO
  //      // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
  //      val bsonArray: ByteBuf = buffer.readBytes(valueLength - 4)
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(bsonArray)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case D_BOOLEAN =>
  //        // get the value Boolean
  //        val value: Int = buffer.readByte()
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(value)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case D_NULL =>
  //
  //      case D_INT =>
  //        // get the value Int
  //        val value: Int = buffer.readIntLE()
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(value)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case D_LONG =>
  //        // get the value Long
  //        val value: Long = buffer.readLongLE()
  //        // apply the f function to the present value and obtain the new value
  //        val newValue: Any = f(value)
  //        // write the new value in the returning buffer
  //        writeNewValue(newBuffer, newValue)
  //      case _ =>
  //    }
  //    //return a ByteBuf with the the values updated
  //
  //    Unpooled.buffer(newBuffer.writerIndex()).writeBytes(newBuffer)
  //  }

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

  //  def consume(seqType: Int, buffer: ByteBuf): Unit = {
  //    seqType match {
  //      case D_ZERO_BYTE => None
  //      case D_FLOAT_DOUBLE =>
  //        println("D_FLOAT_DOUBLE")
  //        buffer.readDoubleLE()
  //      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
  //        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
  //        val valueLength: Int = buffer.readIntLE()
  //        buffer.readBytes(valueLength)
  //      case D_BSONOBJECT =>
  //        println("BSONOBJECT ")
  //        val valueTotalLength: Int = buffer.readIntLE()
  //        //println(valueTotalLength)
  //        buffer.readBytes(valueTotalLength - 4)
  //      case D_BSONARRAY =>
  //        println("D_BSONARRAY")
  //        val valueLength: Int = buffer.readIntLE()
  //        buffer.readBytes(valueLength - 4)
  //      case D_BOOLEAN =>
  //        println("D_BOOLEAN")
  //        buffer.readByte()
  //      case D_NULL =>
  //        println("D_NULL")
  //      case D_INT =>
  //        println("D_INT")
  //        buffer.readIntLE()
  //      case D_LONG =>
  //        println("D_LONG")
  //        buffer.readLongLE()
  //      case _ =>
  //    }
  //  }

  //  def writeNewValue(newBuffer: ByteBuf, newValue: Any, seqType: Int = 0, valueLength: Int = 0): Unit = {
  //    val returningType: String = newValue.getClass.getSimpleName
  //    // println("returning type = "+returningType)
  //    // superclass to try to find a solution for enumerations
  //    //val superclass: String = newValue.getClass.getGenericSuperclass.toString
  //    //println("superclass type = "+superclass)
  //    println(s"writeNewValue___________________________________returningType -> $returningType")
  //    returningType match {
  //      case "Integer" =>
  //        newBuffer.writeIntLE(newValue.asInstanceOf[Int])
  //      case "byte[]" =>
  //        val aux: Array[Byte] = newValue.asInstanceOf[Array[Byte]]
  //        newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0)
  //      case "String" =>
  //        val aux: Array[Byte] = newValue.asInstanceOf[String].getBytes()
  //        newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0)
  //      case "Instant" =>
  //        val aux: Array[Byte] = newValue.asInstanceOf[Instant].toString.getBytes()
  //        newBuffer.writeIntLE(aux.length + 1).writeBytes(aux).writeByte(0)
  //      case "Enumerations" => //TODO
  //
  //      case "Float" =>
  //        val aux: Float = newValue.asInstanceOf[Float]
  //        newBuffer.writeDoubleLE(aux)
  //      case "Double" =>
  //        val aux: Double = newValue.asInstanceOf[Double]
  //        newBuffer.writeDoubleLE(aux)
  //      case "BsonObject" =>
  //        val buf: Buffer = newValue.asInstanceOf[BsonObject].encode()
  //        newBuffer.writeBytes(buf.getByteBuf)
  //      case "Boolean" =>
  //        newBuffer.writeBoolean(newValue.asInstanceOf[Boolean])
  //      case "Long" =>
  //        newBuffer.writeLongLE(newValue.asInstanceOf[Long])
  //      case "BsonArray" =>
  //        val buf: Buffer = newValue.asInstanceOf[BsonArray].encode()
  //        newBuffer.writeBytes(buf.getByteBuf)
  //
  //      case _ if seqType == D_ARRAYB_INST_STR_ENUM_CHRSEQ => //enumerations
  //        writeNewValue(newBuffer, newValue.toString)
  //    }
  //  }

  private def readArrayPos(netty: ByteBuf): Char = {
    val list: ListBuffer[Byte] = new ListBuffer[Byte]
    var i: Int = netty.readerIndex()
    while (netty.getByte(i) != 0) {
      list.+=(netty.readByte())
      i += 1
    }
    list.+=(netty.readByte()) //  consume the end Pos byte
    //val a: String = ""
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
//      processTypes(buffer, seqType, fieldID, f) map { elem =>
//        println("out of processTypes and got Some")
//        (elem._1,elem._2)
//      } getOrElse {
//        println("Another None AGAIN")
//        findBsonObjectWithinBsonArray(buffer, fieldID, f)
//      }
    }
  }

  /*def findBsonObjectWithinBsonObject(buffer: ByteBuf): List[(Int, Int)] = {
    //list result to keep the pairs for the bsonObjects positions
    val listResult: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]
    //the size of the received buffer
    buffer.readIntLE() //buffSize
    // while we dont reach the end of the buffer
    while (buffer.readerIndex() < buffer.writerIndex() - 1) {
      // get the type of the following value
      val seqType: Int = buffer.readByte()
      // fieldBytes will keep the name of the field
      val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
      // consume bytes until the name is complete, i.e, when we find a 0 byte
      while (buffer.getByte(buffer.readerIndex()) != 0) {
        fieldBytes.append(buffer.readByte())
      }
      println(s"findBsonObjectWithinBsonObject ---> ${new String(fieldBytes.toArray)}")
      //consume the 0 byte
      buffer.readByte()
      // match and treat each type
      processTypes(buffer, seqType).foreach(U => listResult.+=((U._1, U._2)))
    }
    // last zero of a BsonArray
    buffer.readByte()
    // return the list of pairs of positions of BsonObjects
    listResult.toList
  }*/

  /*def arrayTreatment(buffer: ByteBuf, fieldID: String, f: Any => Any, indexesOfInterest: (Int, Int), indexesProcessed: Int = 0): ByteBuf = {

    /////////////////////////
    val bP: ByteProcessor = (value: Byte) => {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value)
      true
    }
    // get the buffer's Start Region Index and Finish Region Index
    val (fieldStartIndex, fieldFinishIndex): (Int, Int) = (indexesOfInterest._1, indexesOfInterest._2)
    println(s"fieldStartIndex -> $fieldStartIndex, fieldFinishIndex -> $fieldFinishIndex")
    // Slice of the buffer corresponding to the section before the region of interest
    val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
    //println("++++++++++++++++++++++++")
    //bufferOriginalSize.forEachByte(bP)
    // Slice of the buffer's region of interest
    val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex)
    //println("++++++++++++++++++++++++")
    //bufferOfInterst.forEachByte(bP)
    // Slice of the buffer corresponding to the section after the region of interest
    val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, buffer.getIntLE(0) - fieldFinishIndex)
    //println("++++++++++++++++++++++++")
    //bufferRemainder.forEachByte(bP)
    //println("++++++++++++++++++++++++")
    // Execute the updated of the field FieldID with function f
    // The function findFieldID throws an Exception case no field with FieldID exists.
    // Use the Try the catch the exception and keep processing the rest of the pairs
    println("bufferOfInterst inside START = " + bufferOfInterst.capacity())
    val newBuffer: ByteBuf = start(bufferOfInterst, fieldID, f)
    // Update the number of pairs processed
    println("AFTER START")
    newBuffer.forEachByte(bP)
    val newListIndexesProcessed: Int = indexesProcessed + 1
    /*
        // Compute a new Buffer corresponding to the section of the size with the size of the buffer
        val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBufferT.get.capacity() + bufferRemainder.capacity())
        // Slice of the region after the ByteBuf size and before the region of interest
        val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4)
        // Create a new ByteBuf with all the ByteBuf that were sliced and updated
        val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBufferT.get, bufferRemainder)
    */

    // Compute a new Buffer corresponding to the section of the size with the size of the buffer
    val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBuffer.capacity() + bufferRemainder.capacity())
    println(s"arrayTreatment::::::bufferOriginalSize.capacity() -> ${bufferOriginalSize.capacity()}::::::newBuffer.capacity() -> ${newBuffer.capacity()}:::::::bufferRemainder.capacity() -> ${bufferRemainder.capacity()}")
    // Slice of the region after the ByteBuf size and before the region of interest
    val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4)
    // Create a new ByteBuf with all the ByteBuf that were sliced and updated
    val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBuffer, bufferRemainder)

    // println("After GLUEING averything together")
    //result.forEachByte(bP)

    val indexesOfInterestAux: List[(Int, Int)] = findBsonObjectWithinBsonArray(result.duplicate())
    val newList: List[(Int, Int)] = indexesOfInterestAux.drop(newListIndexesProcessed)
    println(newList)
    val res: ByteBuf =
      if (newList.isEmpty) {
        println("isEmpty!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1")
        result
      } else {
        arrayTreatment(result.duplicate(), fieldID, f, newList.head, newListIndexesProcessed)
      }
    res
  }*/

  /*def objectTreatment(buffer: ByteBuf, fieldID: String, f: Any => Any, indexesOfInterest: (Int, Int), indexesProcessed: Int = -1): ByteBuf = {
    // get the buffer's Start Region Index and Finish Region Index
    val (fieldStartIndex, fieldFinishIndex): (Int, Int) = (indexesOfInterest._1, indexesOfInterest._2)
    println(s"fieldStartIndex -> $fieldStartIndex, fieldFinishIndex -> $fieldFinishIndex")
    // Slice of the buffer corresponding to the section before the region of interest
    val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
    // Slice of the buffer's region of interest
    val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex)
    // Slice of the buffer corresponding to the section after the region of interest
    val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, buffer.getIntLE(0) - fieldFinishIndex)
    // Execute the updated of the field FieldID with function f
    // The function findFieldID throws an Exception case no field with FieldID exists.
    // Use the Try the catch the exception and keep processing the rest of the pairs
    println("bufferOfInterst inside START = " + bufferOfInterst.capacity())
    val newBuffer: ByteBuf = start(bufferOfInterst, fieldID, f)
    // Update the number of pairs processed
    println("AFTER START")
    val newListIndexesProcessed: Int = indexesProcessed + 1
    // Compute a new Buffer corresponding to the section of the size with the size of the buffer
    val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBuffer.capacity() + bufferRemainder.capacity()) //NEW TOTAL LENGTH
    // Slice of the region after the ByteBuf size and before the region of interest
    val result: ByteBuf =
    if (bufferOriginalSize.capacity() == 0) {
      // Create a new ByteBuf with all the ByteBuf that were sliced and updated
      newBuffer.readIntLE() // consume the length so it wont be wrapped twice
      Unpooled.wrappedBuffer(byteBufOriginalSize, newBuffer)
    } else {
      val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4) //BEFORE WITHOUT THE TOTAL SIZE
      // Create a new ByteBuf with all the ByteBuf that were sliced and updated
      Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBuffer, bufferRemainder)
    }

    val indexesOfInterestAux: List[(Int, Int)] = findBsonObjectWithinBsonObject(result.duplicate())
    val newList: List[(Int, Int)] = indexesOfInterestAux.drop(newListIndexesProcessed)
    println(newList)
    val res: ByteBuf =
      if (newList.isEmpty) {
        result
      } else {
        objectTreatment(result.duplicate(), fieldID, f, newList.head, newListIndexesProcessed)
      }
    res
  }*/

  //  def start(buffer: ByteBuf, fieldID: String, f: Any => Any): ByteBuf = {
  //    // val failureBuffer: ByteBuf = buffer.duplicate()
  //    // get the size of the buffer without consumes the bytes
  //    // val bufferSize: Int = buffer.readIntLE()
  //    val bufaux: ByteBuf = Unpooled.buffer(buffer.getIntLE(0)).writeBytes(buffer.duplicate())
  //
  //    // compute the region of interest of the buffer corresponding to the fieldID
  //    val bufferSize: Int = buffer.readIntLE()
  //    println(s"start--------------> bufferSize: $bufferSize")
  //    val (fieldStartIndex: Int, fieldFinishIndex: Int) = findFieldID(buffer, fieldID, bufferSize) //first time the start is 4 and finish is 17
  //    //println(s"$fieldStartIndex -> $fieldFinishIndex")
  //    // Slice of the buffer corresponding to the section before the region of interest
  //
  //    (fieldStartIndex, fieldFinishIndex) match {
  //      case (0, 0) =>
  //        bufaux
  //      case (_, _) =>
  //        val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex) //(0,4length) = (0,3)
  //      // Slice of the buffer's region of interest
  //      val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex) //(4,13length) = (4,17)
  //      // Slice of the buffer corresponding to the section after the region of interest
  //      val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, buffer.getIntLE(0) - fieldFinishIndex) //(17,1length) = (17,18)
  //
  //
  //        // Compute the new buffer with the value of FieldID updated with function f
  //        val newBuffer: ByteBuf = updateValues(bufferOfInterst, f) //buffer has everything written correctly
  //      // Compute a new Buffer corresponding to the section of the size with the size of the buffer
  //      val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBuffer.capacity() + bufferRemainder.capacity())
  //        println(s"start -------bufferOriginalSize.capacity(): ${bufferOriginalSize.capacity()}-------newBuffer.capacity(): ${newBuffer.capacity()}------bufferRemainder.capacity(): ${bufferRemainder.capacity()}")
  //        // Slice of the region after the ByteBuf size and before the region of interest
  //        val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4)
  //        println(s"start---------remainingOfSizeBuffer.capacity(): ${remainingOfSizeBuffer.capacity()}")
  //        // Create a new ByteBuf with all the ByteBuf that were sliced and updated
  //        val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBuffer, bufferRemainder) //(totalSize, 0, buffer with new value, finishing byte buffer)
  //        // return the new Buffer
  //        println("Size of failure buffer = " + buffer.capacity() + " result buffer = " + result.capacity())
  //        result
  //    }
  //  }

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
