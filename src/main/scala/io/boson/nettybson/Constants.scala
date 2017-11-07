package io.boson.nettybson

import java.nio.charset.Charset


object Constants {
  val ERR_MANUAL_ACK: String = "stan: cannot manually ack in auto-ack mode"
  val STRING_VALUE: String = "String"
  val BOOLEAN_VALUE: String = "Boolean"
  val BOOLEAN_SIZE: Int = 1
  val BYTE_VALUE: String = "Byte"
  val BYTE_SIZE: Int = 1
  val BYTES_VALUE: String = "Bytes"
  val CHAR_VALUE: String = "Char"
  val CHAR_SIZE: Int = 2
  val CHARSEQUENCE_VALUE: String = "CharSequence"
  val DOUBLE_VALUE: String = "Double"
  val DOUBLE_SIZE: Int = 8
  val FLOAT_VALUE: String = "Float"
  val FLOAT_SIZE: Int = 4
  val INT_VALUE: String = "Int"
  val INT_SIZE: Int = 4
  val INT_LE_VALUE: String = "IntLE"
  val INT_LE_SIZE: Int = 4
  val LONG_VALUE: String = "Long"
  val LONG_SIZE: Int = 8
  val LONG_LE_VALUE: String = "LongLE"
  val LONG_LE_SIZE: Int = 8
  val MEDIUM_VALUE: String = "Medium"
  val MEDIUM_SIZE: Int = 3
  val MEDIUM_LE_VALUE: String = "MediumLE"
  val MEDIUM_LE_SIZE: Int = 3
  val SHORT_VALUE: String = "Short"
  val SHORT_SIZE: Int = 2
  val SHORT_LE_VALUE: String = "ShortLE"
  val SHORT_LE_SIZE: Int = 2
  val UNSIGNED_BYTE_VALUE: String = "UnsignedByte"
  val UNSIGNED_BYTE_SIZE: Int = 1
  val UNSIGNED_INT_VALUE: String = "UnsignedInt"
  val UNSIGNED_INT_SIZE: Int = 4
  val UNSIGNED_INT_LE_VALUE: String = "UnsignedIntLE"
  val UNSIGNED_INT_LE_SIZE: Int = 4
  val UNSIGNED_MEDIUM_VALUE: String = "UnsignedMedium"
  val UNSIGNED_MEDIUM_SIZE: Int = 3
  val UNSIGNED_MEDIUM_LE_VALUE: String = "UnsignedMediumLE"
  val UNSIGNED_MEDIUM_LE_SIZE: Int = 3
  val UNSIGNED_SHORT_VALUE: String = "UnsignedShort"
  val UNSIGNED_SHORT_SIZE: Int = 2
  val UNSIGNED_SHORT_LE_VALUE: String = "UnsignedShortLE"
  val UNSIGNED_SHORT_LE_SIZE: Int = 2
  val EMPTY_CONSTRUCTOR: String = "EmptyConstructor"
  val NETTY_DUPLICATED_BUF: String = "UnpooledDuplicatedByteBuf"
  val SCALA_ARRAYBUF: String = "ArrayBuffer"
  val VERTX_BUF: String = "BufferImpl"
  val JAVA_BYTEBUFFER: String = "HeapByteBuffer"
  val ARRAY_BYTE: String = "byte[]"
  val NETTY_DEFAULT_BUF: String = "InstrumentedUnpooledUnsafeHeapByteBuf" //"UnpooledUnsafeHeapByteBuf" on netty 4.1.8Final this was the name
  val NETTY_READONLY_BUF: String = "ReadOnlyByteBuf"

  val D_ZERO_BYTE: Int = 0
  val D_FLOAT_DOUBLE: Int = 1
  val D_ARRAYB_INST_STR_ENUM_CHRSEQ: Int = 2
  val D_BSONOBJECT: Int = 3
  val D_BSONARRAY: Int = 4
  val D_BOOLEAN: Int = 8
  val D_NULL: Int = 10
  val D_INT: Int = 16
  val D_LONG: Int = 18


  val SEPARATOR: Char = '\\'
  val SEPARATOR_SIZE: Int = 1
  // Our Own Netty Buffer Implementation
  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")
}
