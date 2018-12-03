package io.zink.boson.bson.value.impl

import java.time.Instant
import io.zink.boson.bson.bsonImpl.BosonInjectorImpl
import io.zink.boson.bson.codec._
import io.zink.boson.bson.value.{Value, ValueObject}
import scala.util.{Failure, Success, Try}

case class ValueString(value: String) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeString(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = Try(injFunction(value.asInstanceOf[T])) match {
    case Success(modValue) =>
      ValueObject.toValue(modValue.asInstanceOf[String])
    case Failure(_) => Try(injFunction(Instant.parse(value).asInstanceOf[T])) match {
      case Success(modValue) =>
        ValueObject.toValue(modValue.asInstanceOf[Instant])
      case Failure(exception) =>
        if(convertFunction.isDefined){
          val extractedTuples: TupleList = BosonInjectorImpl.extractTupleList(Right(value)) //TuppleList Function
          val convertFunct = convertFunction.get
          val convertedValue = convertFunct(extractedTuples)

          Try(injFunction(convertedValue)) match {
            case Success(modValue) =>
              val modifiedTupleList = BosonInjectorImpl.toTupleList(modValue)
              BosonInjectorImpl.encodeTupleList(modifiedTupleList, value) match {
                case Right(str) => ValueObject.toValue(str.asInstanceOf[String])
              }
            case Failure(exception) => throw exception
          }
        } else throw exception
    }
  }
}

case class ValueClassJson(value: String) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeObject(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[String])
  }
}

case class ValueInt(value: Int) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeInt(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Int])
  }
}

case class ValueLong(value: Long) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeLong(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Long])
  }
}

case class ValueFloat(value: Float) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeFloat(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Float])
  }
}

case class ValueDouble(value: Double) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeDouble(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Double])
  }
}

case class ValueBoolean(value: Boolean) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeBoolean(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Boolean])
  }
}

case class ValueNull(value: Null) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeNull(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Null])
  }
}

case class ValueBarray(value: Array[Byte]) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeBarray(value)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    Try(injFunction(value.asInstanceOf[T])) match {
      case Success(modValue) =>
        ValueObject.toValue(modValue.asInstanceOf[Array[Byte]])
      case Failure(_) =>
        if (convertFunction.isDefined) {
          val extractedTuples: TupleList = BosonInjectorImpl.extractTupleList(Left(value)) //TuppleList Function
          val convertFunct = convertFunction.get
          val convertedValue = convertFunct(extractedTuples)

          Try(injFunction(convertedValue)) match {
            case Success(modValue) =>
              val modifiedTupleList = BosonInjectorImpl.toTupleList(modValue)
              BosonInjectorImpl.encodeTupleList(modifiedTupleList, value) match {
                case Left(bb) => ValueObject.toValue(bb.asInstanceOf[Array[Byte]])
              }
            case Failure(exception) => throw exception
          }

        } else {
          Try(injFunction(new String(value).asInstanceOf[T])) match {
            case Success(modValue) =>
              ValueObject.toValue(modValue.asInstanceOf[Array[Byte]])
            case Failure(_) =>
              Try(injFunction(Instant.parse(new String(value)).asInstanceOf[T])) match {
                case Success(modValue) =>
                  ValueObject.toValue(modValue.asInstanceOf[Array[Byte]])
                case Failure(exception) => throw exception
              }
          }
          ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Array[Byte]])
        }
    }
  }
}

case class ValueInstant(value: Instant) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = codec.writeString(value.toString)

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Instant])
  }
}

//TODO - the ONE bellow might not be necessary
case class ValueSeqBarray(value: Seq[Array[Byte]]) extends Value {
  private type TupleList = List[(String, Any)]

  override def write(codec: Codec): Codec = ???

  override def applyFunction[T](injFunction: T => T, convertFunction: Option[TupleList => T] = None): Value = {
    ValueObject.toValue(injFunction(value.asInstanceOf[T]).asInstanceOf[Seq[Array[Byte]]])
  }
}
