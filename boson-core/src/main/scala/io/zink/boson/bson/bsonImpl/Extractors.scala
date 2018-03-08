package io.zink.boson.bson.bsonImpl

import scala.reflect.runtime.universe._

sealed trait Extractor[T] {
  def applyFunc(f: (T => Unit), value: T): T
}

object Extractor extends DefaultExtractor {
  def apply[T](f: T => Extractor[T], v: T): Extractor[T] = {
    f(v)
  }
}

trait DefaultExtractor {

  implicit def SeqExtractor[T: Extractor]: Extractor[Seq[T]] = new Extractor[Seq[T]] {
      def applyFunc(f: Seq[T] => Unit, value: Seq[T]): Seq[T] = {
        f(value.map(elem => implicitly[Extractor[T]].applyFunc(_=>{}, elem)))
        value
      }
    }

  implicit def ObjExtractor[T: TypeTag]: Extractor[T] = new Extractor[T] {
    def applyFunc(f: T => Unit, value: T): T = {
      f(value)
      value
    }
  }

  implicit object ArrByteExtractor extends Extractor[Array[Byte]] {
    def applyFunc(f: Array[Byte] => Unit, value: Array[Byte]): Array[Byte] = {
      f(value)
      value
    }
  }

  implicit object StringExtractor extends Extractor[String] {
    def applyFunc(f: String => Unit, value: String): String = {
      f(value)
      value
    }
  }

  implicit object IntExtractor extends Extractor[Int] {
    def applyFunc(f: Int => Unit, value: Int): Int = {
      f(value)
      value
    }
  }

  implicit object BooleanExtractor extends Extractor[Boolean] {
    def applyFunc(f: Boolean => Unit, value: Boolean): Boolean ={
      f(value)
      value
    }
  }

  implicit object DoubleExtractor extends Extractor[Double] {
    def applyFunc(f: Double => Unit, value: Double): Double ={
      f(value)
      value
    }
  }

  implicit object LongExtractor extends Extractor[Long] {
    def applyFunc(f: Long => Unit, value: Long): Long ={
      f(value)
      value
    }
  }

}
