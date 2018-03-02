package io.zink.boson.bson

class StringScanner(val source : String) {

  var offset = 0
  val limit = source.length

  def getCharIgnoring(ignoring: Array[Char]) : Char  = {
    while (offset < limit && ignoring.contains(source.charAt(offset))) { offset += 1 }
    return if (offset == limit) '\u0000' else source.charAt(offset)
  }



}
