package io.boson.nettyboson

import java.nio.charset.Charset

object Constants {
  val EMPTY_CONSTRUCTOR: String = "EmptyConstructor"
  val SCALA_ARRAYBUF: String = "ArrayBuffer"
  val JAVA_BYTEBUFFER: String = "HeapByteBuffer"
  val ARRAY_BYTE: String = "byte[]"

  val D_ZERO_BYTE: Int = 0
  val D_FLOAT_DOUBLE: Int = 1
  val D_ARRAYB_INST_STR_ENUM_CHRSEQ: Int = 2
  val D_BSONOBJECT: Int = 3
  val D_BSONARRAY: Int = 4
  val D_BOOLEAN: Int = 8
  val D_NULL: Int = 10
  val D_INT: Int = 16
  val D_LONG: Int = 18

  // Our Own Netty Buffer Implementation
  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")
}
