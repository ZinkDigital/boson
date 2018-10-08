package io.zink.boson.bson.value

import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.impl.{ValueInt, ValueString}

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
  /**
    * implicit object StringCodec extends Codecs[String] {
    * override def applyFunc(arg: String): CodecJson = new CodecJson(arg)
    * }
    */
  implicit object IntValue extends Values[Int] {
    override def applyFunc(value: Int): Value = new ValueInt(value)
  }

  implicit object StringValue extends Values[String] {
    override def applyFunc(value: String): Value = new ValueString(value)
  }
}
