package io.zink.boson.bson.value.impl

import java.time.Instant

import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.{Value, ValueObject}

case class ValueString(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeString(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[String])
  }
}

case class ValueClassJson(value: String) extends Value {
  override def write(codec: Codec): Codec = codec.writeObject(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[String])
  }
}

case class ValueInt(value: Int) extends Value {
  override def write(codec: Codec): Codec = codec.writeInt(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Int])
  }
}

case class ValueLong(value: Long) extends Value {
  override def write(codec: Codec): Codec = codec.writeLong(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Long])
  }
}

case class ValueFloat(value: Float) extends Value {
  override def write(codec: Codec): Codec = codec.writeFloat(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Float])
  }
}

case class ValueDouble(value: Double) extends Value {
  override def write(codec: Codec): Codec = codec.writeDouble(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Double])
  }
}

case class ValueBoolean(value: Boolean) extends Value {
  override def write(codec: Codec): Codec = codec.writeBoolean(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Boolean])
  }
}

case class ValueNull(value: Null) extends Value {
  override def write(codec: Codec): Codec = codec.writeNull(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Null])
  }
}

case class ValueBarray(value: Array[Byte]) extends Value {
  override def write(codec: Codec): Codec = codec.writeBarray(value)

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Array[Byte]])
  }
}

//TODO - the two bellow might not be necessary
case class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  override def write(codec: Codec): Codec = ???

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Seq[Array[Byte]]])
  }
}

case class ValueInstant(value: Instant) extends Value {
  override def write(codec: Codec): Codec = ???

  override def applyFunction[T](injFunction: T => T):Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Instant])
  }
}