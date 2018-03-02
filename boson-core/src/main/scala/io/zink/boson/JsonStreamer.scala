package io.zink.boson


import io.zink.boson.bson.StringScanner


case class JsonStreamer( source : String) extends Streamer[String] {

  // push the source into the tokenising class
  val scanner = new StringScanner(source)

  val whiteSpace = " \n\r\t\f".toCharArray

  /**
    * Move the pointer in the stream to where we can capture the type of the next
    * and return it
    */
  override def readNamedType(inside : SonNamedType): SonNamedType = {
    inside match {
        // Starting so looking for an onject or array
      case nt : SonStart => {
        scanner.getCharIgnoring(whiteSpace) match {
          case '{' =>  SonObject("")
          case '[' =>  SonArray("")
          case 0   =>  SonEnd()
        }
      }
      case _  => SonEnd("Not implemented yet")
    }
  }

  /**
    * Return a slice of the stream source up to this point
    *
    * @return
    */
  override def used(): String = ???

  /**
    * return the remainder of the stream up to this point
    */
  override def remaining(): String = ???
}
