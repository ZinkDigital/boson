package io.zink.boson.bson.bsonImpl

import java.nio.charset.Charset


object Dictionary {
  // BUFFERS CONSTANTS
  val EMPTY_CONSTRUCTOR: String = "EmptyConstructor"
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
  val C_ALLDOTS: String = "allDots"
  val C_NEXT: String = "next"
  val C_ALLNEXT: String = "allNext"
  val C_FIRST: String = "first"
  val C_END: String = "end"
  val C_DOT: String = "."
  val C_DOUBLEDOT: String = ".."
  val C_LAST: String = "last"  // "end" should be used to maintain consistency
  val C_RANDOM: String = "random"
  val C_BUILD: String = "build"
  val C_ZERO: String = "0"

  val V_NULL: String = "Null"
  val C_MATCH: String = "Matched"

  // PARSER CONSTANTS
  val P_CLOSE_BRACKET: Char = ']'
  val P_OPEN_BRACKET: Char = '['
  val P_HASELEM_AT: Char = '@'
  val P_STAR: Char = '*'

  // ERROR MESSAGES
  val E_HALFNAME: String = "Error Parsing HalfName!"
  val E_ProgStatement: String = "Failure parsing!"

  // TYPES CONSTANTES
  val STRING: String = "String"
  val DOUBLE: String = "Double"
  val INTEGER: String = "Integer"
  val LONG: String = "Long"
  val BOOLEAN: String = "Boolean"
  val ARRBYTE: String = "byte[]"
  val ANY: String = "Any"

  //CODECS CONSTANTS
  val CS_OBJECT: String = "Object"
  val CS_ARRAY: String = "Array"
  val CS_NAME: String = "Name"
  val CS_STRING: String = STRING
  val CS_INTEGER: String = INTEGER
  val CS_DOUBLE: String = DOUBLE
  val CS_LONG: String = LONG
  val CS_BOOLEAN: String = BOOLEAN
  val CS_NULL: String = V_NULL
  val CS_TRUE: String = "true"
  val CS_FALSE: String = "false"
  val CS_T: Char = 't'
  val CS_F: Char = 'f'
  val CS_N: Char = 'n'
  val CS_QUOTES: Char = '\"'
  val CS_COMMA: Char = ','
  val CS_OPEN_BRACKET: Char = '{'
  val CS_CLOSE_BRACKET: Char = '}'
  val CS_OPEN_RECT_BRACKET: Char = '['
  val CS_CLOSE_RECT_BRACKET: Char = ']'
  val CS_DOT: Char = '.'
  val CS_2DOT: Char = ':'
  val CS_ZERO: Char = '0'

  val charset: Charset = java.nio.charset.Charset.availableCharsets().get("UTF-8")
}

