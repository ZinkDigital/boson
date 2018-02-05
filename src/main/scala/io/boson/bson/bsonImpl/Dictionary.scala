package io.boson.bson.bsonImpl

import java.nio.charset.Charset

import io.vertx.core.json.{JsonArray, JsonObject}


object Dictionary {
  // BUFFERS CONSTANTS
  val EMPTY_CONSTRUCTOR: String = "EmptyConstructor"
  val SCALA_ARRAYBUF: String = "ArrayBuffer"
  val JAVA_BYTEBUFFER: String = "HeapByteBuffer"
  val ARRAY_BYTE: String = "byte[]"

  // ENCODING CONSTANTS
  val D_ZERO_BYTE: Int = 0
  val D_FLOAT_DOUBLE: Int = 1
  val D_ARRAYB_INST_STR_ENUM_CHRSEQ: Int = 2
  val D_BSONOBJECT: Int = 3
  val D_BSONARRAY: Int = 4
  val D_BOOLEAN: Int = 8
  val D_NULL: Int = 10
  val D_INT: Int = 16
  val D_LONG: Int = 18


  // EXPRESSIONS CONSTANTS
  val EMPTY_KEY: String = ""
  val EMPTY_RANGE: String = ""
  val UNTIL_RANGE: String = "until"
  val TO_RANGE: String = "to"
  val WARNING_CHAR: Char = '!'
  val STAR: String = "*"
  val C_LEVEL: String = "level"
  val C_LIMITLEVEL: String = "limitLevel"
  val C_LIMIT: String = "limit"
  val C_FILTER: String = "filter"
  val C_ALL: String = "all"
  val C_NEXT: String = "next"
  val C_FIRST: String = "first"
  val C_END: String = "end"
  val C_DOT: String = "."
  val C_DOUBLEDOT: String = ".."
  val C_LAST: String = "last" // "end" should be used to maintain consistency
  val C_RANDOM: String = "random"

  val V_NULL: String = "Null"

  // PARSER CONSTANTS
  val P_NUMBER: String =
    """\d+(\.\d*)?"""
  val P_WORD: String = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+"""
  val P_CLOSE_BRACKET: String = "]"
  val P_OPEN_BRACKET: String = "["
  val P_HAS_ELEM: String = "[@"

  // ERROR MESSAGES
  val E_HALFNAME: String = "Error Parsing HalfName!"
  val E_MOREKEYS: String = "Failure parsing!"


  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")
}
