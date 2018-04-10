package io.zink.boson.bson.bsonImpl

sealed trait ExtractorFacade {

  def toPrimitive[T](f: T => Unit, o: T)(implicit tc: Extractor[T]): Unit


}

object Transform extends ExtractorFacade {

  def toPrimitive[T](f: T => Unit, o: T)(implicit tc: Extractor[T]): Unit = tc.applyFunc(f,o)



}
