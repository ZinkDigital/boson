package io.zink.boson.bson.value.impl

import java.time.Instant

import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.Value
import shapeless._

class ValueString(value: String) extends Value {
  def write(codec:Codec): Codec = {
    codec.writeToken(SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
    codec.writeToken(SonString(CS_STRING, value))
    codec.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
  }
}

class ValueClassJson(value: String) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonObject(CS_OBJECT, value))
}

class ValueInt(value: Int) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_INTEGER, value))

  //  def get: Int = value
}

class ValueLong(value: Long) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_LONG, value))
}

class ValueFloat(value:Float) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_FLOAT, value))
}

class ValueDouble(value: Double) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_DOUBLE, value))
}

class ValueBoolean(value: Boolean) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonBoolean(CS_BOOLEAN, value))
}

class ValueNull(value: Null) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonNull(CS_NULL, value))
}

class ValueBarray(value: Array[Byte]) extends Value {
  def write(codec: Codec): Codec = codec.writeToken(SonObject(CS_OBJECT, value))
}

//TODO - the two bellow might not be necessary
class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  def write(codec: Codec): Codec = ???
}

class ValueInstant(value: Instant) extends Value {
  def write(codec: Codec): Codec = ???
}