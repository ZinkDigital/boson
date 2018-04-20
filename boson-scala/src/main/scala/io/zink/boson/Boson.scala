package io.zink.boson

import java.nio.ByteBuffer
import java.time.Instant

import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl.{BosonExtractor, BosonExtractorObj, BosonInjector, BosonValidate}
import shapeless.{HList, LabelledGeneric, TypeCase, Typeable}

import scala.concurrent.Future

/**
  * Companion for [[Boson]]
  */
object Boson {

  /**
    * Returns a Boson instance depending on the given type.
    *
    * @tparam A Type of Value to be extracted.
    */
  trait extractor[A] {
    //implicit val typeCase: Option[TypeCase[A]]
    def extract(expression: String, extractFunction: A => Unit): Boson
  }

  /**
    * Companion fo [[extractor]]
    */
  object extractor {

    /**
      * Apply method of extractor object.
      *
      * @param expression String given by the User designated as BsonPath.
      * @param extractFunction  Extract function given by the User to be applied after extraction.
      * @param ext  Implicit Extractor.
      * @tparam A Type of Value to be extracted.
      * @return Instance of a Boson
      */
    def apply[A](expression: String, extractFunction: A => Unit)(implicit ext: extractor[A]): Boson = ext.extract(expression, extractFunction)


    /**
      * Method used when User wants an Object extraction and a case class is given as a type.
      * LabelledGeneric allows to keep track of case class arguments and their types.
      * ExtractLabels performs the construction of the HList's case class and case class instantiation.
      *
      * @param f  LabelledGeneric of type A and L.
      * @param ext  Object extractLabels of type L.
      * @tparam A Represents a case class.
      * @tparam L Represents the HList of the case class.
      * @return Instance of a Boson.
      */
    implicit def caseClass[A, L <: HList](implicit
                                          f: LabelledGeneric.Aux[A, L],
                                          ext: extractLabels[L],
                                          tp: Typeable[A]): extractor[A] =
      new extractor[A] {

        implicit val typeCase: Option[TypeCase[A]] = Some(TypeCase[A])
        //println("implicit caseClass")
        def extract(expression: String, extractFunction: A => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractFunction = Option(extractFunction))(f,ext,typeCase)
      }

    /**
      * Like previous method handles the same situation but for sequences of case classes.
      *
      * @param f  LabelledGeneric of type A and L.
      * @param ext  Object extractLabels of type L.
      * @tparam A Represents a case class.
      * @tparam L Represents the HList of the case class.
      * @return Instance of a Boson.
      */
    implicit def seqCaseClass[A, L <: HList](implicit
                                             f: LabelledGeneric.Aux[A, L],
                                             ext: extractLabels[L],
                                             tp: Typeable[A]): extractor[Seq[A]] = {
      new extractor[Seq[A]] {
        implicit val typeCase: Option[TypeCase[A]] = Some(TypeCase[A])
        //println("implicit seqCaseClass")
        def extract(expression: String, extractFunction: Seq[A] => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractSeqFunction = Option(extractFunction))(f, ext,typeCase)

      }
    }

    /**
      * This method is chosen whenever User specifies the type of extraction as a Sequence of a primitive type.
      *
      * @tparam A Primitive type.
      * @tparam Coll  Collection.
      * @return Instance of a Boson
      */
    implicit def seqLiterals[A, Coll[_]](implicit tp1: Typeable[Coll[A]]): extractor[Coll[A]] =
      new extractor[Coll[A]] {

        implicit val typeCase: Option[TypeCase[Coll[A]]] = Some(TypeCase[Coll[A]])
        //println("implicit seqLiterals")
        def extract(expression: String, extractFunction: Coll[A] => Unit): Boson =
          new BosonExtractor[Coll[A]](expression, extractFunction)(typeCase)
      }

    implicit val seqArrByte: extractor[Seq[Array[Byte]]] =
      new extractor[Seq[Array[Byte]]] {
        implicit val typeCase: Option[TypeCase[Seq[Array[Byte]]]] = None
        //println(s"implicit of seqArrByte")
        override def extract(expression: String, extractFunction: Seq[Array[Byte]] => Unit): Boson =
          new BosonExtractor[Seq[Array[Byte]]](expression,extractFunction)
      }

    /**
      * Special case to handle Array[Byte].
      */
    implicit val arrByte: extractor[Array[Byte]] =
      new extractor[Array[Byte]] {
        //println("implicit arrByte")
        implicit val typeCase: Option[TypeCase[Array[Byte]]] = None
        override def extract(expression: String, extractFunction: Array[Byte] => Unit): Boson =
          new BosonExtractor[Array[Byte]](expression,extractFunction)
      }

    /**
      * Primitive type Double.
      */
    implicit val double: extractor[Double] =
      new extractor[Double] {
        //println("implicit Double")
        implicit val typeCase: Option[TypeCase[Double]] = Some(TypeCase[Double])
        override def extract(expression: String, extractFunction: Double => Unit): Boson =
          new BosonExtractor[Double](expression, extractFunction)
      }

    /**
      * Primitive type Float.
      */
    implicit val float: extractor[Float] =
      new extractor[Float] {
        //println("float")
        implicit val typeCase: Option[TypeCase[Float]] = Some(TypeCase[Float])
        override def extract(expression: String, extractFunction: Float => Unit): Boson =
          new BosonExtractor[Float](expression, extractFunction)
      }

    /**
      * Special case to handle java.time.Instant.
      */
    implicit val instant: extractor[Instant] =
      new extractor[Instant] {
        //println("instant")
        implicit val typeCase: Option[TypeCase[Instant]] = Some(TypeCase[Instant])
        override def extract(expression: String, extractFunction: Instant => Unit): Boson =
          new BosonExtractor[Instant](expression, extractFunction)
      }

    /**
      * Primitive type Long.
      */
    implicit val long: extractor[Long] =
      new extractor[Long] {
        //println("implicit Long")
        implicit val typeCase: Option[TypeCase[Long]] = Some(TypeCase[Long])
        override def extract(expression: String, extractFunction: Long => Unit): Boson =
          new BosonExtractor[Long](expression, extractFunction)
      }

    /**
      * Primitive type Int.
      */
    implicit val int: extractor[Int] =
      new extractor[Int] {
        //println("int")
        implicit val typeCase: Option[TypeCase[Int]] = Some(TypeCase[Int])
        override def extract(expression: String, extractFunction: Int => Unit): Boson =
          new BosonExtractor[Int](expression, extractFunction)
      }

    /**
      * Primitive type String.
      */
    implicit val string: extractor[String] =
      new extractor[String] {
        //println("string")
        implicit val typeCase: Option[TypeCase[String]] = Some(TypeCase[String])
        override def extract(expression: String, extractFunction: String => Unit): Boson =
          new BosonExtractor[String](expression, extractFunction)
      }

    /**
      * Primitive type Boolean.
      */
    implicit val boolean: extractor[Boolean] =
      new extractor[Boolean] {
        //println("boolean")
        implicit val typeCase: Option[TypeCase[Boolean]] = Some(TypeCase[Boolean])
        override def extract(expression: String, extractFunction: Boolean => Unit): Boson =
          new BosonExtractor[Boolean](expression, extractFunction)
      }
  }


  def validate[T](expression: String, validateFunction: T => Unit) = new BosonValidate[T](expression, validateFunction)

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression String given by the User designated as BsonPath.
    * @param injectFunction Inject function given by the User to be applied after injection.
    * @tparam T Type of Value to be injected.
    * @return Instance of Boson.
    */
  def injector[T](expression: String, injectFunction: T => T) = new BosonInjector[T](expression, injectFunction)

}


trait Boson {
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding Array[Byte] encoded
    * @return Future with original or a modified Array[Byte].
    */
  def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]]
  def go(bsonByteEncoding: String): Future[String]
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding Array[Byte] encoded wrapped in a ByteBuffer.
    * @return Future with original or a modified ByteBuffer.
    */
  def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer]


  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in the case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param boson BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  def fuse(boson: Boson): Boson

}
