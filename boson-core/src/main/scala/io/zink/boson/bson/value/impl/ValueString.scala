package io.zink.boson.bson.value.impl

import io.zink.boson.bson.codec.{Codec, SonNumber, SonString}
import io.zink.boson.bson.value.Value
import io.zink.boson.bson.bsonImpl.Dictionary._

class ValueString(value: String) extends Value{

  def write(codec:Codec): Codec = {
    codec.writeToken(SonNumber(CS_INTEGER, value.length + 1), ignoreForJson = true)
    codec.writeToken(SonString(CS_STRING, value))
    codec.writeToken(SonNumber(CS_BYTE, 0.toByte), ignoreForJson = true)
  }

}
