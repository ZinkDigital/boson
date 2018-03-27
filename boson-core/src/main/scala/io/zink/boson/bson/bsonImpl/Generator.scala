package io.zink.boson.bson.bsonImpl

import shapeless.{::, HList, HNil, LabelledGeneric, Typeable, Witness}
import shapeless.labelled.{FieldType, field}

sealed trait extractLabels[L <: HList] {
  def apply(m: List[(String,Any)]): Option[L]
}

object extractLabels {

  implicit val hconsNil: extractLabels[HNil] = new extractLabels[HNil] {
    def apply(m: List[(String,Any)]): Option[HNil] = Some(HNil)
  }



    implicit def hconsFromList[K <: Symbol, V, TAIL <: HList](implicit
                                                            witness: Witness.Aux[K],
                                                            typeable: Typeable[V],
                                                            extractLabelsT: extractLabels[TAIL]
                                                           ): extractLabels[FieldType[K, V] :: TAIL] =
    new extractLabels[FieldType[K, V] :: TAIL] {
      override def apply(m: List[(String,Any)]): Option[FieldType[K, V] :: TAIL] = {
//        println(s" -> ${witness.value.name}")
//        val v = m.find(elem => elem._1.equals(witness.value.name.toLowerCase)).map(v => v._2).getOrElse(None)
//        println(s"return: $v")
//        println(s"after cast -> ${typeable.cast(v)}")
        val res =
          for {
            v <- m.find(elem => elem._1.equals(witness.value.name.toLowerCase)).map(v => v._2)
            h <- typeable.cast(v)
            t <- extractLabelsT(m)
          } yield {
            field[K](h) :: t
          }
        res
      }
    }

  def to[A]: Convert[A] = new Convert[A]

}

class Convert[A] {
  def from[R <: HList](m: List[(String,Any)])(implicit
                                              gen: LabelledGeneric.Aux[A,R],
                                              extract: extractLabels[R]): Option[A] = {
    extract(m).map(gen.from)
  }
}


