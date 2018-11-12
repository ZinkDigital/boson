package io.zink.boson.bson.value.impl

import java.time.Instant

import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.Value
import shapeless._

case class ValueString(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeString(value)

}

case class ValueClassJson(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeObject(value)

}

case class ValueInt(value: Int) extends Value {
  override def write(codec: Codec): Codec = codec.writeInt(value)

}

case class ValueLong(value: Long) extends Value {
  override def write(codec: Codec): Codec = codec.writeLong(value)

}

case class ValueFloat(value: Float) extends Value {
  override def write(codec: Codec): Codec = codec.writeFloat(value)

}

case class ValueDouble(value: Double) extends Value {
  override def write(codec: Codec): Codec = codec.writeDouble(value)

}

case class ValueBoolean(value: Boolean) extends Value {
  override def write(codec: Codec): Codec = codec.writeBoolean(value)

}

case class ValueNull(value: Null) extends Value {
  override def write(codec: Codec): Codec = codec.writeNull(value)

}

case class ValueBarray(value: Array[Byte]) extends Value {
  override def write(codec: Codec): Codec = codec.writeBarray(value)

}

//TODO - the two bellow might not be necessary
case class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  override def write(codec: Codec): Codec = ???

}

case class ValueInstant(value: Instant) extends Value {
  override def write(codec: Codec): Codec = ???

}