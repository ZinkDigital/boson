package io.zink.boson.bson.value

import java.time.Instant

import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.impl.{ValueInt, ValueString}
import shapeless._

trait Value {
  def write(codec: Codec): Codec
}

sealed trait ValueFacade {
  def toValue[T](a: T)(implicit c: Values[T]): Value
}

object ValueObject extends ValueFacade {
  override def toValue[T](a: T)(implicit c: Values[T]): Value = c.applyFunc(a)
}

sealed trait Values[T] {
  def applyFunc(value: T): Value
}

object Values extends DefaultValues {
  def apply[T](f: T => Value, a: T): Value = f(a)
}

sealed trait DefaultValues {
  implicit object IntValue extends Values[Int] {
    override def applyFunc(value: Int): Value = new ValueInt(value)
  }

  implicit object StringValue extends Values[String] {
    override def applyFunc(value: String): Value = new ValueString(value)
  }

  implicit object LongValue extends Values[Long] {
    override def applyFunc(value: Long): Value = ???
  }

  implicit object FloatValue extends Values[Float] {
    override def applyFunc(value: Float): Value = ???
  }

  implicit object DoubleValue extends Values[Double] {
    override def applyFunc(value: Double): Value = ???
  }

  implicit object BooleanValue extends Values[Boolean] {
    override def applyFunc(value: Boolean): Value = ???
  }

  implicit object NullValue extends Values[Null] {
    override def applyFunc(value: Null): Value = ???
  }

  implicit object BarrayValue extends Values[Array[Byte]] {
    override def applyFunc(value: Array[Byte]): Value = ???
  }

  implicit object SeqBarryValue extends Values[Seq[Array[Byte]]] {
    override def applyFunc(value: Seq[Array[Byte]]): Value = ???
  }

  implicit object InstantValue extends Values[Instant] {
    override def applyFunc(value: Instant): Value = ???
  }

  implicit object AnyCodec extends Values[Any] {
    override def applyFunc(value: Any): Value = value match {
      case string: String => ???
      case int: Int => ???
      case _ => ???
    }
  }
}
