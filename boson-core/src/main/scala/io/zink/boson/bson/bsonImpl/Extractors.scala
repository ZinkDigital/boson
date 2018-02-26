package io.zink.boson.bson.bsonImpl

sealed trait Extractor[T] {
  def applyFunc(f: (T => Unit), value: T): Unit
}

object Extractor extends DefaultExtractor {
  def apply[T](f: T => Extractor[T], v: T): Extractor[T] = {
    f(v)
  }
}

trait DefaultExtractor {

  implicit def SeqExtractor[T: Extractor]: Unit = new Extractor[Seq[T]] {
      def applyFunc(f: Seq[T] => Unit, value: Seq[T]): Unit =
        f(value)
    }

  implicit object StringExtractor extends Extractor[String] {
    def applyFunc(f: String => Unit, value: String): Unit =
      f(value)
  }

  implicit object SeqStringExtractor extends Extractor[Seq[String]] {
    def applyFunc(f: Seq[String] => Unit, value: Seq[String]): Unit =
      f(value)
  }

  implicit object IntExtractor extends Extractor[Int] {
    def applyFunc(f: Int => Unit, value: Int): Unit =
      f(value)
  }

  implicit object SeqIntExtractor extends Extractor[Seq[Int]] {
    def applyFunc(f: Seq[Int] => Unit, value: Seq[Int]): Unit =
      f(value)
  }

  implicit object BooleanExtractor extends Extractor[Boolean] {
    def applyFunc(f: Boolean => Unit, value: Boolean): Unit =
      f(value)
  }

  implicit object SeqBooleanExtractor extends Extractor[Seq[Boolean]] {
    def applyFunc(f: Seq[Boolean] => Unit, value: Seq[Boolean]): Unit =
      f(value)
  }

  implicit object DoubleExtractor extends Extractor[Double] {
    def applyFunc(f: Double => Unit, value: Double): Unit =
      f(value)
  }

  implicit object SeqDoubleExtractor extends Extractor[Seq[Double]] {
    def applyFunc(f: Seq[Double] => Unit, value: Seq[Double]): Unit =
      f(value)
  }

  implicit object LongExtractor extends Extractor[Long] {
    def applyFunc(f: Long => Unit, value: Long): Unit =
      f(value)
  }

  implicit object SeqLongExtractor extends Extractor[Seq[Long]] {
    def applyFunc(f: Seq[Long] => Unit, value: Seq[Long]): Unit =
      f(value)
  }

}
