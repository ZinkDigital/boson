package io.zink.boson.bson.bsonImpl

//import scala.reflect.runtime.universe._

sealed trait Extractor[T] {
  def applyFunc(f: (T => Unit), value: T): T
}

object Extractor extends DefaultExtractor {
  def apply[T](f: T => Extractor[T], v: T): Extractor[T] = {
    f(v)
  }
}

trait DefaultExtractor {

  implicit def SeqExtractor[T: Extractor]: Extractor[Seq[T]] = new Extractor[Seq[T]] {
      def applyFunc(f: Seq[T] => Unit, value: Seq[T]): Seq[T] = {
        f(value.map(elem => implicitly[Extractor[T]].applyFunc(_=>{}, elem)))
        value
      }
    }

//  implicit def ObjExtractor[T: TypeTag]: Extractor[T] = new Extractor[T] {
//    def applyFunc(f: T => Unit, value: T): T = {
//      f(value)
//      value
//    }
//  }

  implicit object ArrByteExtractor extends Extractor[Array[Byte]] {
    def applyFunc(f: Array[Byte] => Unit, value: Array[Byte]): Array[Byte] = {
      f(value)
      value
    }
  }

  implicit object StringExtractor extends Extractor[String] {
    def applyFunc(f: String => Unit, value: String): String = {
      f(value)
      value
    }
  }

  implicit object IntExtractor extends Extractor[Int] {
    def applyFunc(f: Int => Unit, value: Int): Int = {
      f(value)
      value
    }
  }

  implicit object BooleanExtractor extends Extractor[Boolean] {
    def applyFunc(f: Boolean => Unit, value: Boolean): Boolean ={
      f(value)
      value
    }
  }

  implicit object DoubleExtractor extends Extractor[Double] {
    def applyFunc(f: Double => Unit, value: Double): Double ={
      f(value)
      value
    }
  }

  implicit object LongExtractor extends Extractor[Long] {
    def applyFunc(f: Long => Unit, value: Long): Long ={
      f(value)
      value
    }
  }

}

//import shapeless._
//import labelled.{FieldType, field}
//
//sealed trait FromList[L <: HList] {
//  def apply(m: List[(String,Any)]): Option[L]
//}
//
//trait LowPriorityFromList {
//  implicit def hconsFromList1[K <: Symbol, V, T <: HList](implicit
//                                                          witness: Witness.Aux[K],
//                                                          typeable: Typeable[V],
//                                                          fromListT: Lazy[FromList[T]]
//                                                         ): FromList[FieldType[K, V] :: T] = new FromList[FieldType[K, V] :: T] {
//    def apply(m: List[(String,Any)]): Option[FieldType[K, V] :: T] = {
//      val resFromFor = for {
//        v <- m.find(elem => elem._1.equals(witness.value.name)).map( v => v._2)
//        h <- typeable.cast(v)
//        t <- fromListT.value(m)
//      } yield field[K](h) :: t
//      resFromFor
//    }
//  }
//}
//
//object FromList extends LowPriorityFromList {
//  implicit val hnilFromMap: FromList[HNil] = new FromList[HNil] {
//    def apply(m: List[(String,Any)]): Option[HNil] = Some(HNil)
//  }
//
//  implicit def hconsFromList0[K <: Symbol, V, R <: HList, T <: HList](implicit
//                                                                      witness: Witness.Aux[K],
//                                                                      gen: LabelledGeneric.Aux[V, R],
//                                                                      fromListH: FromList[R],
//                                                                      fromListT: FromList[T]
//                                                                     ): FromList[FieldType[K, V] :: T] = new FromList[FieldType[K, V] :: T] {
//    def apply(m: List[(String,Any)]): Option[FieldType[K, V] :: T] = {
//      val resFromFor = for {
//        v <- m.find(elem => elem._1.equals(witness.value.name)).map( v => v._2)
//        r <- Typeable[List[(String,Any)]].cast(v)
//        h <- fromListH(r)
//        t <- fromListT(m)
//      } yield field[K](gen.from(h)) :: t
//      resFromFor
//    }
//  }
//
//  def to[A]/*(implicit A: LabelledGeneric[A])*/: Convert[A] = new Convert[A]//(value)
//}
//
//class Convert[A]/*(value: LabelledGeneric[A])*/ {
//  def from[R <: HList](m: List[(String,Any)])(implicit
//                                              gen: LabelledGeneric.Aux[A, R],
//                                              fromList: FromList[R]
//  ): Option[A] = {
//    fromList(m).map(gen.from)
//  }
//}
