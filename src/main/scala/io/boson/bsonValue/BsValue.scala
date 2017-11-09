package io.boson.bsonValue

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
sealed trait BsValue

/**
  * Represents a Bson boolean value.
  */
case class BsBoolean(value: Boolean) extends BsValue

/**
* Represent a Bson number value.
*/
case class BsNumber(value: BigDecimal) extends BsValue

/**
  * Represent a Bson number value.
  */
case class BsSeq(value: Seq[Any]) extends BsValue

/**
  * Represent a Bson throwable value.
  */
case class BsException(value: String) extends BsValue
