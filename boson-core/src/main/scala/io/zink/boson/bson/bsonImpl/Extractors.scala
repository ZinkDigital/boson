package io.boson.bson.bsonImpl



sealed trait Extractor[T] {
  def applyFunc(f: (T => Unit), value: T): Unit
}

object Extractor extends DefaultExtractor {
  def apply[T](f: T => Extractor[T], v: T): Extractor[T] = {
    f(v)
  }
}

trait DefaultExtractor {

  implicit object StringExtractor extends Extractor[String] {
    def applyFunc(f: String => Unit, value: String): Unit =
      f(value)
  }

  implicit object IntExtractor extends Extractor[Int] {
    def applyFunc(f: Int => Unit, value: Int): Unit =
      f(value)
  }

  implicit object BooleanExtractor extends Extractor[Boolean] {
    def applyFunc(f: Boolean => Unit, value: Boolean): Unit =
      f(value)
  }

  implicit object DoubleExtractor extends Extractor[Double] {
    def applyFunc(f: Double => Unit, value: Double): Unit =
      f(value)
  }

  implicit object LongExtractor extends Extractor[Long] {
    def applyFunc(f: Long => Unit, value: Long): Unit =
      f(value)
  }
}
