package io.zink.boson.bson.value.impl

import java.time.Instant

import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.Value
import shapeless._

class ValueString(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeString(value)


}

class ValueClassJson(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeObject(value)

}

class ValueInt(value: Int) extends Value {
  override def write(codec: Codec): Codec = codec.writeInt(value)

}

class ValueLong(value: Long) extends Value {
  override def write(codec: Codec): Codec = codec.writeLong(value)

}

class ValueFloat(value: Float) extends Value {
  override def write(codec: Codec): Codec = codec.writeFloat(value)

}

class ValueDouble(value: Double) extends Value {
  override def write(codec: Codec): Codec = codec.writeDouble(value)

}

class ValueBoolean(value: Boolean) extends Value {
  override def write(codec: Codec): Codec = codec.writeBoolean(value)

}

class ValueNull(value: Null) extends Value {
  override def write(codec: Codec): Codec = codec.writeNull(value)

}

class ValueBarray(value: Array[Byte]) extends Value {
  override def write(codec: Codec): Codec = codec.writeBarray(value)

}

//TODO - the two bellow might not be necessary
class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  override def write(codec: Codec): Codec = ???

}

class ValueInstant(value: Instant) extends Value {
  override def write(codec: Codec): Codec = ???

}