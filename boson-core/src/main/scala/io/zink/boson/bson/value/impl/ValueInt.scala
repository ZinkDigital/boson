package io.zink.boson.bson.value.impl

import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.Value

class ValueInt(value: Int) extends Value {

  def write(codec: Codec): Codec ={
    codec.writeToken(SonNumber(CS_INTEGER, value))
  }
}
