package io.zink.boson.bson.bsonImpl

import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Typeable, Witness}
import shapeless.labelled.{FieldType, field}

sealed trait extractLabels[L <: HList] {
  def apply(m: List[(String,Any)]): Option[L]
}

trait LowPriorityFromList0 {
  implicit def hconsFromList0[K <: Symbol, V, TAIL <: HList](implicit
                                                             witness: Witness.Aux[K],
                                                             typeable: Typeable[V],
                                                             extractLabelsT: Lazy[extractLabels[TAIL]]
                                                            ): extractLabels[FieldType[K, V] :: TAIL] =
    new extractLabels[FieldType[K, V] :: TAIL] {
      override def apply(m: List[(String,Any)]): Option[FieldType[K, V] :: TAIL] = {
        val res =
          for {
            v <- m.find(elem => elem._1.toLowerCase.equals(witness.value.name.toLowerCase)).map(v => v._2)
            h <- typeable.cast(v)
            t <- extractLabelsT.value(m)
          } yield {
            field[K](h) :: t
          }
        res
      }
    }
}

trait LowPriorityFromList1 extends LowPriorityFromList0 {
  implicit def hconsFromList1[K <: Symbol, V, R <: HList, T<: HList](implicit
                                                                     witness: Witness.Aux[K],
                                                                     gen: LabelledGeneric.Aux[V,R],
                                                                     extH: extractLabels[R],
                                                                     extT: Lazy[extractLabels[T]]
                                                                    ): extractLabels[FieldType[K,V]::T] = new extractLabels[FieldType[K, V]:: T] {
    override def apply(m: List[(String, Any)]): Option[FieldType[K, V]:: T] =
      for {
        v <- m.find(elem => elem._1.toLowerCase.equals(witness.value.name.toLowerCase)).map(v => v._2)
        r <- Typeable[List[(String,Any)]].cast(v)
        h <- extH(r)
        t <- extT.value(m)
      } yield field[K](gen.from(h)) :: t
  }
}

object extractLabels extends LowPriorityFromList1 {

  implicit val hconsNil: extractLabels[HNil] = new extractLabels[HNil] {
    def apply(m: List[(String,Any)]): Option[HNil] = Some(HNil)
  }

  implicit def hconsFromList2[K <: Symbol, V, R <: HList, T<: HList](implicit
                                                                     witness: Witness.Aux[K],
                                                                     gen: LabelledGeneric.Aux[V,R],
                                                                     extH: extractLabels[R],
                                                                     extT: Lazy[extractLabels[T]]
                                                                    ): extractLabels[FieldType[K,Seq[V]]::T] = new extractLabels[FieldType[K,Seq[V]]:: T] {
    override def apply(m: List[(String, Any)]): Option[FieldType[K, Seq[V]]:: T] =
      for {
        v <- m.find(elem => elem._1.toLowerCase.equals(witness.value.name.toLowerCase)).map(v => v._2)
        r <- Typeable[Seq[List[(String,Any)]]].cast(v)
        h = {
          val z = r.map(x => extH(x))
          z.collect{case p if p.isDefined => p.get}
        }
        t <- extT.value(m)
      } yield field[K](h.map(p => gen.from(p))) :: t
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


