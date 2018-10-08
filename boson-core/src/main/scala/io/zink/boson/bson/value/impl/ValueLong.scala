package io.zink.boson.bson.value.impl

import io.zink.boson.bson.bsonImpl.Dictionary.CS_LONG
import io.zink.boson.bson.codec.{Codec, SonNumber}
import io.zink.boson.bson.value.Value

class ValueLong(value: Long) extends Value {

  def write(codec: Codec): Codec = codec.writeToken(SonNumber(CS_LONG, value))

}
