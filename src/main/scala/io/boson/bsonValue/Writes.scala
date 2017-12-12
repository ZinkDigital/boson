package io.boson.bsonValue

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
trait Writes[-A] {
  /**
    * Convert the object into a BsValue
    */
  def writes(o: A): BsValue

}

object Writes extends DefaultWrites {

  def apply[A](f: A => BsValue): Writes[A] = (a: A) => f(a)

}

trait DefaultWrites {

  implicit object IntWrites extends Writes[Int] {
    def writes(o: Int) = BsNumber(o)
  }

  implicit object BigDecimalWrites extends Writes[BigDecimal] {
    def writes(o: BigDecimal) = BsNumber(o)
  }

  implicit object BooleanWrites extends Writes[Boolean] {
    def writes(o: Boolean) = BsBoolean(o)
  }

  implicit object BsSeqWrites extends Writes[Seq[Any]] {
    def writes(o: Seq[Any]) = BsSeq(o)
  }

  implicit object BsExceptionWrites extends Writes[String] {
    def writes(o: String) = BsException(o)
  }

  implicit object BsValueWrites extends Writes[BsValue] {
    def writes(o: BsValue) = o
  }

}