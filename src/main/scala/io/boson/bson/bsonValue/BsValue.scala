package io.boson.bson.bsonValue

import io.boson.bson.bsonImpl.Boson

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
sealed trait BsValue

/**
  * Represents a Bson boolean value.
  */
case class BsBoolean(value: Boolean) extends BsValue { def getValue: Boolean = value}

/**
* Represent a Bson number value.
*/
case class BsNumber(value: BigDecimal) extends BsValue { def getValue: BigDecimal = value}

/**
  * Represent a Bson number value.
  */
case class BsSeq(value: Seq[Any]) extends BsValue { def getValue: Seq[Any] = value}

/**
  * Represent a Bson throwable value.
  */
case class BsException(value: String) extends BsValue { def getValue: String = value}


/**
  * Represent a Bson Boson value.
  */
case class BsBoson(value: Boson) extends BsValue { def getValue: Boson = value}