package io.boson.bson.bsonValue

/**
  * Created by Tiago Filipe on 08/11/2017.
  */
sealed trait BsonFacade {

  /**
    * Converts any writeable value to a [[BsValue]].
    *
    * A value is writeable if a [[Writes]] implicit is available for its type.
    *
    * @tparam T the type of the value to be written as BSON
    * @param o the value to convert as BSON
    */
  def toBson[T](o: T)(implicit tjs: Writes[T]): BsValue

}

object BsObject extends BsonFacade {

  def toBson[T](o: T)(implicit tjs: Writes[T]): BsValue =tjs.writes(o)

}
