package io.zink.boson.bson.bsonValue

import io.zink.boson.bson.bsonImpl.BosonImpl

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
sealed trait BsValue {
  def getValue: Any
}

///**
//  * Represents a Bson boolean value.
//  */
//case class BsBoolean(value: Boolean) extends BsValue { override def getValue: Boolean = value}

///**
//* Represent a Bson number value.
//*/
//case class BsNumber(value: BigDecimal) extends BsValue { override def getValue: BigDecimal = value}

/**
  * Represent a Bson number value.
  */
case class BsSeq(value: Vector[Any]) extends BsValue { override def getValue: Vector[Any] = value}

/**
  * Represent a Bson throwable value.
  */
case class BsException(value: String) extends BsValue { override def getValue: String = value}


/**
  * Represent a Bson Boson value.
  */
case class BsBoson(value: BosonImpl) extends BsValue { override def getValue: BosonImpl = value}