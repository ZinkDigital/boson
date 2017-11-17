package io.boson.injectors

import java.nio.ByteBuffer
import java.time.Instant
import java.util.NoSuchElementException

import com.sun.corba.se.impl.transport.ByteBufferPoolImpl
import io.boson.bson.{BsonArray, BsonObject}
import io.boson.injectors
import io.boson.injectors.CheckStruture.{bP, netty1}
import io.boson.injectors.Injector.obj
import io.boson.nettybson.NettyBson
import io.netty.buffer.{ByteBuf, Unpooled}
import io.boson.nettybson.Constants._
import io.boson.scalaInterface.ScalaInterface
import io.netty.util.ByteProcessor
import io.vertx.core.buffer.Buffer
import io.vertx.core.buffer.impl.BufferImpl

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.{Failure, Success, Try}

/**
  * Created by Ricardo Martins on 07/11/2017.
  */



object e extends Enumeration{
  val A = Value("Asdghrt")
  val B = Value("Bdysrtyry")
}
object CheckStruture extends App{
  val newbObj: BsonObject = new BsonObject().put("newbsonObj", "newbsonObj")
  val bsonArray: BsonArray = new BsonArray().add(new BsonObject().put("ewfw", new BsonObject().put("fjjl", "99")).put("wf", 3)).add(new BsonObject().put("key", "valu0e").put("anotherKey", 34))

  val bP: ByteProcessor = new ByteProcessor {
    override def process(value: Byte): Boolean = {
      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value.toByte)
      true
    }
  }

  val netty: Option[NettyBson] = Some(new NettyBson(vertxBuff = Option(newbObj.encode())))
  val netty1: Option[NettyBson] = Some(new NettyBson(vertxBuff = Option(bsonArray.encode())))
 // netty.get.getByteBuf.forEachByte(bP)
  println("-------------------------------------")
  netty1.get.getByteBuf.forEachByte(bP)
  val inj: Injector = new Injector
  val indexesOfInterest: List[(Int, Int)] = inj.findBsonObjectWithinBsonArray(netty1.get.getByteBuf.duplicate())
println(indexesOfInterest)

  val bufferOriginalSize: ByteBuf = netty1.get.getByteBuf.slice(0, indexesOfInterest.head._1)
  val resultBuf: ByteBuf = netty1.get.getByteBuf.slice(indexesOfInterest.head._1, indexesOfInterest.head._2 - indexesOfInterest.head._1)
  val bufferRemainder: ByteBuf = netty1.get.getByteBuf.slice(indexesOfInterest.head._2, netty1.get.getByteBuf.getIntLE(0) - indexesOfInterest.head._2)

  inj.start(resultBuf, "fjjl", x=>x)
 /* println("++++++++++++++++")
  bufferOriginalSize.forEachByte(bP)
  println("++++++++++++++++")
  resultBuf.forEachByte(bP)
  println("++++++++++++++++")
  bufferRemainder.forEachByte(bP)
  println("++++++++++++++++")*/


}
object Injector extends App{

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
  val bsonArray: BsonArray = new BsonArray().add(new BsonObject().put("field", "1")).add(new BsonObject().put("hg", 2)).add(new BsonObject().put("field", "2"))//.add(1).add(2).add("Hi")
  val newbsonArray: BsonArray = new BsonArray().add(3).add(4).add("Bye")
  val enumJava = io.boson.injectors.EnumerationTest.A
  val newEnumJava = io.boson.injectors.EnumerationTest.B
  val charseq: CharSequence = "charSequence"
  val anotherCharseq: CharSequence = "AnothercharSequence"
  val inj: Injector = new Injector
  val ext = new ScalaInterface
  val ins: Instant = Instant.now()
  val ins1: Instant = Instant.now()
  val obj1: BsonObject = new BsonObject().put("bsonArray", bsonArray).putNull("null").put("enum", e.A.toString)//.put("field", 0).put("bool", bool).put("long", long).put("no", "ok").put("float", float).put("bObj",bObj).put("charS", charseq).put("array", bytearray1).put("inst", ins)
  val obj: BsonArray = bsonArray
  val netty: Option[NettyBson] = Some(new NettyBson(vertxBuff = Option(obj.encode())))

  println( obj.encode())
  val b1: Try[NettyBson] = Try(inj.modify(netty, "field", x => "100").get)



  b1 match {
    case Success(v)  =>
      val s: Any = ext.parse(v, "field", "all")
      //println(s.getClass.getSimpleName)
      println(s.asInstanceOf[List[Any]].head)
    case Failure(e) => println(e.getStackTrace.foreach(p => println(p.toString)))
  }
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
      val buff: ByteBuf = Unpooled.buffer(4)
      buffer.getBytes(0, buff, 4)
      val bufferSize: Int = buff.readIntLE() // buffer.readIntLE()
      println(s"buffer size = $bufferSize ")

      val seqType: Int = buffer.getByte(4).toInt
      seqType match {
        case 0 => None // end of obj
        case _ =>
          buffer.getByte(5).toInt match {
            case 48 => // root obj is BsonArray, call extractFromBsonArray
              println("Root is BsonArray")

              val bP: ByteProcessor = new ByteProcessor {
                    override def process(value: Byte): Boolean = {
                      println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value.toByte)
                      true
                    }
                  }
              buffer.forEachByte(bP)

              //apply the given function to all bsonarray elements
              if(fieldID.isEmpty){
                // Operate on Root
                //updateBsonArrayValue()
                Option(new NettyBson())
              }else{

                println("Input capacity = " + buffer.capacity())
                val indexesOfInterest: List[(Int, Int)] = findBsonObjectWithinBsonArray(buffer.duplicate())
                println(indexesOfInterest)

                val result: ByteBuf = arrayTreatment(buffer.duplicate(), fieldID, f, indexesOfInterest.head)
                result.forEachByte(bP)
                println("Input capacity = " + result.capacity())
                Option(new NettyBson(byteBuf = Option(result)))
              }
            case _ => // root obj isn't BsonArray, call extractFromBsonObj

                /*
                TODO
                use
                findBsonObjectWithinBsonArray
                findBsonObjectWithinBsonObject
                before finding the index for the specified fieldID
                 */
              val bP: ByteProcessor = new ByteProcessor {
                override def process(value: Byte): Boolean = {
                  println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value.toByte)
                  true
                }
              }
              println("+++++++++++++++++++++")
buffer.forEachByte(bP)
              println("+++++++++++++++++++++")
                val result: ByteBuf = start(buffer.duplicate(), fieldID, f)
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
                //println("real result capacity = " + res.getByteBuf.capacity())
                Some(res)

          }
      }
    }
  }

  def findFieldID(buffer: ByteBuf, fieldID: String): (Int,Int) = {

    val bufferSize: Int = buffer.getIntLE(0)//readIntLE()
    val startReaderIndex: Int = buffer.readerIndex()
    if(startReaderIndex < (bufferSize-1)) {
      //buffer.readIntLE()// consume the size of the buffer
      val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
      val seqType: Int = buffer.readByte().toInt
      while (buffer.getByte(buffer.readerIndex()) != 0) {
        fieldBytes.append(buffer.readByte())
      }
      buffer.readByte()
      println(new String(fieldBytes.toArray))
      if (fieldID.toCharArray.deep == new String(fieldBytes.toArray).toCharArray.deep) {
        println("FOUND NAME")
        consume(seqType, buffer)
        (startReaderIndex, buffer.readerIndex())
      } else {
        println("DIDNT FOUND NAME")
        // keep looking
        consume(seqType, buffer)
        findFieldID(buffer, fieldID)
      }
    }else{
      println("No FieldID found!!")
      //throw new NoSuchElementException(s" Field $fieldID doesn´t exist.")
      (0,0)
    }
  }

  def updateValues(buffer: ByteBuf, f: (Any) => Any): ByteBuf = {
    // new buffer to return
    val newBuffer: ByteBuf = Unpooled.buffer()
    //gget the seqType
    val seqType: Int = buffer.readByte().toInt
    // get the name of the field
    val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
    while (buffer.getByte(buffer.readerIndex()) != 0) {
      fieldBytes.append(buffer.readByte())
    }
    fieldBytes.append(buffer.readByte())
    // write the seqType in returning buffer
    newBuffer.writeByte(seqType.toByte)
    // write the field name in returning buffer
    fieldBytes.foreach( b => newBuffer.writeByte(b))
    // updated the new value in returning buffer
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        // get the present value
        val value: Double = buffer.readDoubleLE()
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(value)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        // get the size of the value
        val valueLength: Int = buffer.readIntLE()
        // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
        val value: ByteBuf = Unpooled.copiedBuffer(buffer.readBytes(valueLength))
        // get the value in Array[Byte] with the array() function
        val array: Array[Byte] = value.array()
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(new String(array))
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue, seqType, valueLength)
      case D_BSONOBJECT =>
        // get the size of the value
        val valueTotalLength: Int = buffer.readIntLE() //TODO
        // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
        val bsonObject: ByteBuf = Unpooled.copiedBuffer(buffer.readBytes(valueTotalLength-4))
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(bsonObject)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case D_BSONARRAY =>
        // get the size of the value
        val valueLength: Int = buffer.readIntLE() // TODO
        // get the section of the buffer and copy it to a new buffer so we can use the array() function from ByteBuf Lib
        val bsonArray: ByteBuf = buffer.readBytes(valueLength-4)
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(bsonArray)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case D_BOOLEAN =>
        // get the value Boolean
        val value: Int = buffer.readByte()
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(value)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case D_NULL =>

      case D_INT =>
        // get the value Int
        val value: Int = buffer.readIntLE()
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(value)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case D_LONG =>
        // get the value Long
        val value: Long = buffer.readLongLE()
        // apply the f function to the present value and obtain the new value
        val newValue: Any = f(value)
        // write the new value in the returning buffer
        writeNewValue(newBuffer, newValue)
      case _ =>
    }
    //return a ByteBuf with the the values updated

    Unpooled.buffer(newBuffer.writerIndex()).writeBytes(newBuffer)
  }

  def consume(seqType: Int, buffer: ByteBuf) : Unit = {
    seqType match {
      case D_ZERO_BYTE => None
      case D_FLOAT_DOUBLE =>
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        val value: ByteBuf = buffer.readBytes(valueLength)
      case D_BSONOBJECT =>
        println("BSONOBJECT ")
        val valueTotalLength: Int = buffer.readIntLE()
        //println(valueTotalLength)
        val bsonObject: ByteBuf = buffer.readBytes(valueTotalLength-4)
      case D_BSONARRAY =>
        println("D_BSONARRAY")
        val valueLength: Int = buffer.readIntLE()
        val bsonArray: ByteBuf = buffer.readBytes(valueLength-4)
      case D_BOOLEAN =>
        println("D_BOOLEAN")
        val value: Int = buffer.readByte()
      case D_NULL =>
        println("D_NULL")
      case D_INT =>
        println("D_INT")
        val value: Int = buffer.readIntLE()
      case D_LONG =>
    println("D_LONG")
        val value: Long = buffer.readLongLE()
      case _ =>
    }
  }

  def writeNewValue(newBuffer: ByteBuf, newValue: Any, seqType: Int = 0, valueLength: Int = 0): Unit = {
    val returningType: String = newValue.getClass.getSimpleName
   // println("returning type = "+returningType)
    // superclass to try to find a solution for enumerations
    //val superclass: String = newValue.getClass.getGenericSuperclass.toString
    //println("superclass type = "+superclass)

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
      case "Enumerations" => //TODO

      case "Float" =>
        val aux: Float = newValue.asInstanceOf[Float]
        newBuffer.writeDoubleLE(aux)
      case "Double" =>
        val aux: Double = newValue.asInstanceOf[Double]
        newBuffer.writeDoubleLE(aux)
      case "BsonObject" =>
        val buf: Buffer = newValue.asInstanceOf[BsonObject].encode()
        newBuffer.writeBytes(buf.getByteBuf)
      case "Boolean" =>
        newBuffer.writeBoolean(newValue.asInstanceOf[Boolean])
      case "Long" =>
        newBuffer.writeLongLE(newValue.asInstanceOf[Long])
      case "BsonArray" =>
        val buf: Buffer = newValue.asInstanceOf[BsonArray].encode()
        newBuffer.writeBytes(buf.getByteBuf)

      case _ if seqType == D_ARRAYB_INST_STR_ENUM_CHRSEQ => //enumerations
        writeNewValue(newBuffer, newValue.toString)
    }
  }

  def findBsonObjectWithinBsonArray(buffer: ByteBuf):List[(Int, Int)] = {
    //list result to keep the pairs for the bsonObjects positions
    val listResult: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]
    //the size of the received buffer
    val buffsize: Int = buffer.readIntLE()
    // while we dont reach the end of the buffer
    while (buffer.readerIndex() < buffer.writerIndex()-1) {
      // get the type of the following value
      val seqType: Int = buffer.readByte()
      // get the index position of the array
      val arrayIndex: Int = buffer.readByte() //48 == 0
      val zero: Int = buffer.readByte() //byte 0
      // match and treat each type
      processTypes(buffer, seqType).foreach(U => listResult.+=((U._1, U._2)))
    }
    // last zero of a BsonArray
    buffer.readByte()
    // return the list of pairs of positions of BsonObjects
    listResult.toList
  }

  def findBsonObjectWithinBsonObject(buffer: ByteBuf): List[(Int, Int)] = {
    //list result to keep the pairs for the bsonObjects positions
    val listResult: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]
    //the size of the received buffer
    val buffsize: Int = buffer.readIntLE()
    // while we dont reach the end of the buffer
    while (buffer.readerIndex() < buffer.writerIndex()-1) {
      // get the type of the following value
      val seqType: Int = buffer.readByte()
      // fieldBytes will keep the name of the field
      val fieldBytes: ListBuffer[Byte] = new ListBuffer[Byte]
      // consume bytes until the name is complete, i.e, when we find a 0 byte
      while (buffer.getByte(buffer.readerIndex()) != 0) {
        fieldBytes.append(buffer.readByte())
      }
      //consume the 0 byte
      buffer.readByte()
      // match and treat each type
      processTypes(buffer, seqType).foreach(U => listResult.+=((U._1, U._2)))
    }
    // last zero of a BsonArray
    buffer.readByte()
    // return the list of pairs of positions of BsonObjects
    listResult.toList
  }

  def arrayTreatment(buffer: ByteBuf,fieldID: String,f: Any => Any, indexesOfInterest: (Int, Int), indexesProcessed: Int = 0):ByteBuf = {

    /////////////////////////
    val bP: ByteProcessor = new ByteProcessor {
      override def process(value: Byte): Boolean = {
        println("char= " + value.toChar + " int= " + value.toInt + " byte= " + value.toByte)
        true
      }
    }
    //result.forEachByte(bP)
    //println(result.capacity())
    /////////////////////////


    // get the buffer's Start Region Index and Finish Region Index
    val (fieldStartIndex, fieldFinishIndex): (Int, Int) = (indexesOfInterest._1, indexesOfInterest._2)
    // Slice of the buffer corresponding to the section before the region of interest
    val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
    println("++++++++++++++++++++++++")
    bufferOriginalSize.forEachByte(bP)
    // Slice of the buffer's region of interest
    val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex)
    println("++++++++++++++++++++++++")
    bufferOfInterst.forEachByte(bP)
    // Slice of the buffer corresponding to the section after the region of interest
    val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, buffer.getIntLE(0) - fieldFinishIndex)
    println("++++++++++++++++++++++++")
    bufferRemainder.forEachByte(bP)
    println("++++++++++++++++++++++++")
    // Execute the updated of the field FieldID with function f
    // The function findFieldID throws an Exception case no field with FieldID exists.
    // Use the Try the catch the exception and keep processing the rest of the pairs
    println("bufferOfInterst inside START = " + bufferOfInterst.capacity() )
    val newBuffer:ByteBuf = start(bufferOfInterst, fieldID, f)
    // Update the number of pairs processed
    println("AFTER START")
    newBuffer.forEachByte(bP)
    val newListIndexesProcessed: Int = indexesProcessed+1
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
      if(newList.isEmpty){
        result
      }else {
        arrayTreatment(result.duplicate(), fieldID, f, newList.head, newListIndexesProcessed)
      }
    res
  }




  def start(buffer: ByteBuf, fieldID: String, f: Any => Any): ByteBuf = {
   // val failureBuffer: ByteBuf = buffer.duplicate()
    // get the size of the buffer without consumes the bytes
   // val bufferSize: Int = buffer.readIntLE()
    val bufaux: ByteBuf = Unpooled.buffer(buffer.getIntLE(0)).writeBytes(buffer.duplicate())

    // compute the region of interest of the buffer corresponding to the fieldID
    val (fieldStartIndex: Int, fieldFinishIndex: Int) = findFieldID(buffer, fieldID)
    //println(s"$fieldStartIndex -> $fieldFinishIndex")
    // Slice of the buffer corresponding to the section before the region of interest

    (fieldStartIndex,fieldFinishIndex ) match{
      case (0,0) =>
       bufaux
      case (x,y) =>
        val bufferOriginalSize: ByteBuf = buffer.slice(0, fieldStartIndex)
        // Slice of the buffer's region of interest
        val bufferOfInterst: ByteBuf = buffer.slice(fieldStartIndex, fieldFinishIndex - fieldStartIndex)
        // Slice of the buffer corresponding to the section after the region of interest
        val bufferRemainder: ByteBuf = buffer.slice(fieldFinishIndex, buffer.getIntLE(0) - fieldFinishIndex)


        // Compute the new buffer with the value of FieldID updated with function f
        val newBuffer: ByteBuf = updateValues(bufferOfInterst, f)
        // Compute a new Buffer corresponding to the section of the size with the size of the buffer
        val byteBufOriginalSize: ByteBuf = Unpooled.buffer(4).writeIntLE(bufferOriginalSize.capacity() + newBuffer.capacity() + bufferRemainder.capacity())
        // Slice of the region after the ByteBuf size and before the region of interest
        val remainingOfSizeBuffer: ByteBuf = bufferOriginalSize.slice(4, bufferOriginalSize.capacity() - 4)
        // Create a new ByteBuf with all the ByteBuf that were sliced and updated
        val result: ByteBuf = Unpooled.wrappedBuffer(byteBufOriginalSize, remainingOfSizeBuffer, newBuffer, bufferRemainder)
        // return the new Buffer
        println("Size of failure buffer = " + buffer.capacity() + " result buffer = " + result.capacity() )
        result
    }
  }

  def processTypes(buffer: ByteBuf, seqType: Int): List[(Int, Int)] = {
    val listResult: ListBuffer[(Int, Int)] = new ListBuffer[(Int, Int)]
    seqType match {
      case D_ZERO_BYTE =>
        // process Zero Byte
        None
      case D_FLOAT_DOUBLE =>
        // process Float or Double
        println("D_FLOAT_DOUBLE")
        buffer.readDoubleLE()
      case D_ARRAYB_INST_STR_ENUM_CHRSEQ =>
        // process Array[Byte], Instants, Strings, Enumerations, Char Sequences
        println("D_ARRAYB_INST_STR_ENUM_CHRSEQ")
        val valueLength: Int = buffer.readIntLE()
        val value: ByteBuf = buffer.readBytes(valueLength)
      case D_BSONOBJECT =>
        // process BsonObjects
        println("BSONOBJECT ")
        val startRegion: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonObject: ByteBuf = buffer.readBytes(valueTotalLength)
        val finishRegion: Int = buffer.readerIndex()
        findBsonObjectWithinBsonObject(bsonObject).foreach(U => listResult.+=((U._1+startRegion, U._2+startRegion)))
        listResult.+=((startRegion, finishRegion))
      case D_BSONARRAY =>
        // process BsonArrays
        println("D_BSONARRAY")
        val startRegion: Int = buffer.readerIndex()
        val valueTotalLength: Int = buffer.getIntLE(buffer.readerIndex())
        val bsonarray: ByteBuf = buffer.readBytes(valueTotalLength)
        val finishRegion: Int = buffer.readerIndex()
        findBsonObjectWithinBsonArray(bsonarray).foreach(U => listResult.+=((U._1+startRegion, U._2+startRegion)))
      case D_BOOLEAN =>
        // process Booleans
        println("D_BOOLEAN")
        val value: Int = buffer.readByte()
      case D_NULL =>
        // process Null
        println("D_NULL")
      case D_INT =>
        // process Ints
        println("D_INT")
        val value: Int = buffer.readIntLE()
      case D_LONG =>
        // process Longs
        println("D_LONG")
        val value: Long = buffer.readLongLE()
      case _ =>
    }
    // return the list of pairs of positions of BsonObjects
    listResult.toList
  }
}
