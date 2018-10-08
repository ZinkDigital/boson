package io.zink.boson.bson.value.impl

import java.time.Instant

import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.Value
import shapeless._

class ValueInt(value: Int) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_INTEGER, value))
}

class ValueLong(value: Long) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_LONG, value))
}

class ValueString(value: String) extends Value {
  def write(codec:Codec): Codec = {
    codec.writeToken(SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
    codec.writeToken(SonString(CS_STRING, value))
    codec.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
  }
}

class ValueFloat(value:Float) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueDouble(value: Double) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueBoolean(value: Boolean) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueNull(value: Null) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueBarray(value: Array[Byte]) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueInstant(value: Instant) extends Value {
  def write(codec: Codec): Codec = ???
}

