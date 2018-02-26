package io.zink.boson.bson.bsonImpl
import scala.collection.generic.CanBuildFrom


sealed trait ExtractorFacade {

  def toPrimitive[T](f: T => Unit, o: T)(implicit tc: Extractor[T]): Unit

  //def toExtract[T, Coll[_]](f: Coll[T] => Unit, o: Coll[T])(implicit cbf: scala.collection.generic.CanBuildFrom[Nothing,T, Coll[T]], tc: Extractor[T]): Unit

}

object Transform extends ExtractorFacade {

  def toPrimitive[T](f: T => Unit, o: T)(implicit tc: Extractor[T]): Unit = tc.applyFunc(f,o)

  //def toExtract[T, Coll[_]](f: Coll[T] => Unit, o: Coll[T])(implicit tc: Extractor[Coll[T]]): Unit = tc.applyFunc(f,o)

  //override def toExtract[T, Coll[_]](f: Coll[T] => Unit, o: Coll[T])(implicit cbf: CanBuildFrom[Nothing, T, Coll[T]], tc: Extractor[T]): Unit = tc.applyFunc(f,o)
}
