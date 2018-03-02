package io.zink.boson

import io.netty.buffer.{ByteBuf, Unpooled}


case class BsonStreamer(source : Array[Byte]) extends Streamer[Array[Byte]] {

  // use netty byteBuf to help with decoding
  val bbuf = Unpooled.wrappedBuffer(source)

  var size = 0

  /**
    * Move the pointer in the stream to where we can capture the type of the next SonValue
    * and return it;
    */
  override def readNamedType(inside : SonNamedType): SonNamedType = {
     if (bbuf.arrayOffset() == 0) {
       size = bbuf.readIntLE
       return SonObject("")
     }
    return SonEnd()
  }

  /**
    * Return a slice of the stream source up to this point
    *
    * @return
    */
  override def used(): Array[Byte] = ???

  /**
    * return the remainder of the stream up to this point
    */
  override def remaining(): Array[Byte] = ???
}
