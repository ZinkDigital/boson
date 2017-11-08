package io.boson.bsonValue

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
sealed trait BsValue

/**
  * Represents a Json boolean value.
  */
case class BsBoolean(value: Boolean) extends BsValue

/**
* Represent a Json number value.
*/
case class BsNumber(value: BigDecimal) extends BsValue

/**
  * Represent a Json number value.
  */
case class BsSeq(value: Seq[Any]) extends BsValue
