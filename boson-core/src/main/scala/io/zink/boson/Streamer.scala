package io.zink.boson


sealed trait SonNamedType

case class SonStart(name : String = "Start") extends SonNamedType

case class SonString(name : String) extends SonNamedType
case class SonNumber(name : String) extends SonNamedType
case class SonObject(name : String) extends SonNamedType
case class SonArray(name : String)  extends SonNamedType
case class SonTrue(name : String)   extends SonNamedType
case class SonFalse(name : String)  extends SonNamedType
case class SonNull(name : String)   extends SonNamedType

case class SonEnd(name : String = "End") extends SonNamedType


/**
  * For current design a Streamer can have T as a
  * String => Json
  * or
  * Byte[] => Bson
  *
  * @tparam T : The underlying data to stream over
  */

trait Streamer[T] {

  /**
    * Move the pointer in the stream to where we can capture the type of the next SonValue
    * and return it;
    */
  def readNamedType(inside : SonNamedType) : SonNamedType


  /**
    * Return a slice of the stream source up to this point
    * @return
    */
  def used() : T


  /**
    * return the remainder of the stream up to this point
    */
  def remaining() : T


}
