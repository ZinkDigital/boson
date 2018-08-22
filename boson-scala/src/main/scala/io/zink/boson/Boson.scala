package io.zink.boson

import java.nio.ByteBuffer
import java.time.Instant

import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl._
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
      * @param expression      String given by the User designated as BsonPath.
      * @param extractFunction Extract function given by the User to be applied after extraction.
      * @param ext             Implicit Extractor.
      * @tparam A Type of Value to be extracted.
      * @return Instance of a Boson
      */
    def apply[A](expression: String, extractFunction: A => Unit)(implicit ext: extractor[A]): Boson = ext.extract(expression, extractFunction)


    /**
      * Method used when User wants an Object extraction and a case class is given as a type.
      * LabelledGeneric allows to keep track of case class arguments and their types.
      * ExtractLabels performs the construction of the HList's case class and case class instantiation.
      *
      * @param f   LabelledGeneric of type A and L.
      * @param ext Object extractLabels of type L.
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

        def extract(expression: String, extractFunction: A => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractFunction = Some(extractFunction))(f, ext, typeCase)
      }

    /**
      * Like previous method handles the same situation but for sequences of case classes.
      *
      * @param f   LabelledGeneric of type A and L.
      * @param ext Object extractLabels of type L.
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

        def extract(expression: String, extractFunction: Seq[A] => Unit): Boson =
          new BosonExtractorObj[A, L](expression, extractSeqFunction = Some(extractFunction))(f, ext, typeCase)

      }
    }

    /**
      * This method is chosen whenever User specifies the type of extraction as a Sequence of a primitive type.
      *
      * @tparam A    Primitive type.
      * @tparam Coll Collection.
      * @return Instance of a Boson
      */
    implicit def seqLiterals[A, Coll[_]](implicit tp1: Typeable[Coll[A]]): extractor[Coll[A]] =
      new extractor[Coll[A]] {

        implicit val typeCase: Option[TypeCase[Coll[A]]] = Some(TypeCase[Coll[A]])

        def extract(expression: String, extractFunction: Coll[A] => Unit): Boson =
          new BosonExtractor[Coll[A]](expression, extractFunction)(typeCase)
      }

    implicit val seqArrByte: extractor[Seq[Array[Byte]]] =
      new extractor[Seq[Array[Byte]]] {
        implicit val typeCase: Option[TypeCase[Seq[Array[Byte]]]] = None

        override def extract(expression: String, extractFunction: Seq[Array[Byte]] => Unit): Boson =
          new BosonExtractor[Seq[Array[Byte]]](expression, extractFunction)
      }

    /**
      * Special case to handle Array[Byte].
      */
    implicit val arrByte: extractor[Array[Byte]] =
      new extractor[Array[Byte]] {
        implicit val typeCase: Option[TypeCase[Array[Byte]]] = None

        override def extract(expression: String, extractFunction: Array[Byte] => Unit): Boson =
          new BosonExtractor[Array[Byte]](expression, extractFunction)
      }

    /**
      * Primitive type Double.
      */
    implicit val double: extractor[Double] =
      new extractor[Double] {
        implicit val typeCase: Option[TypeCase[Double]] = Some(TypeCase[Double])

        override def extract(expression: String, extractFunction: Double => Unit): Boson =
          new BosonExtractor[Double](expression, extractFunction)
      }

    /**
      * Primitive type Float.
      */
    implicit val float: extractor[Float] =
      new extractor[Float] {
        implicit val typeCase: Option[TypeCase[Float]] = Some(TypeCase[Float])

        override def extract(expression: String, extractFunction: Float => Unit): Boson =
          new BosonExtractor[Float](expression, extractFunction)
      }

    /**
      * Special case to handle java.time.Instant.
      */
    implicit val instant: extractor[Instant] =
      new extractor[Instant] {
        implicit val typeCase: Option[TypeCase[Instant]] = Some(TypeCase[Instant])

        override def extract(expression: String, extractFunction: Instant => Unit): Boson =
          new BosonExtractor[Instant](expression, extractFunction)
      }

    /**
      * Primitive type Long.
      */
    implicit val long: extractor[Long] =
      new extractor[Long] {
        implicit val typeCase: Option[TypeCase[Long]] = Some(TypeCase[Long])

        override def extract(expression: String, extractFunction: Long => Unit): Boson =
          new BosonExtractor[Long](expression, extractFunction)
      }

    /**
      * Primitive type Int.
      */
    implicit val int: extractor[Int] =
      new extractor[Int] {
        implicit val typeCase: Option[TypeCase[Int]] = Some(TypeCase[Int])

        override def extract(expression: String, extractFunction: Int => Unit): Boson =
          new BosonExtractor[Int](expression, extractFunction)
      }

    /**
      * Primitive type String.
      */
    implicit val string: extractor[String] =
      new extractor[String] {
        implicit val typeCase: Option[TypeCase[String]] = Some(TypeCase[String])

        override def extract(expression: String, extractFunction: String => Unit): Boson =
          new BosonExtractor[String](expression, extractFunction)
      }

    /**
      * Primitive type Boolean.
      */
    implicit val boolean: extractor[Boolean] =
      new extractor[Boolean] {
        implicit val typeCase: Option[TypeCase[Boolean]] = Some(TypeCase[Boolean])

        override def extract(expression: String, extractFunction: Boolean => Unit): Boson =
          new BosonExtractor[Boolean](expression, extractFunction)
      }
  }


  //  def validate[T](expression: String, validateFunction: T => Unit) = new BosonValidate[T](expression, validateFunction)

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * expression String given by the User designated as BsonPath.
    * injectFunction Inject function given by the User to be applied after injection.
    * T Type of Value to be injected.
    * Instance of Boson.
    */

  //  def injector[T](expression: String, injectFunction: T => T) = new BosonInjector[T](expression, injectFunction)

  trait injector[A] {
    def inject(expression: String, injectFunction: A => A): Boson

    def inject(expression: String, injectValue: A): Boson
  }

  object injector {

    def apply[A](expression: String, injectFunction: A => A)(implicit inj: injector[A]): Boson = inj.inject(expression, injectFunction)

    def apply[A](expression: String, injectValue: A)(implicit inj: injector[A]): Boson = inj.inject(expression, injectValue)

    implicit def caseClass[A, L <: HList](implicit
                                          f: LabelledGeneric.Aux[A, L],
                                          ext: extractLabels[L],
                                          tp: Typeable[A]): injector[A] = {
      new injector[A] {
        implicit val typeCase: Option[TypeCase[A]] = Some(TypeCase[A])

        def inject(expression: String, injectFunction: A => A): Boson =
          new BosonInjectorObj[A, L](expression, injectFunction = injectFunction)(typeCase, f, ext)

        override def inject(expression: String, injectValue: A): Boson =
          new BosonInjectorValue[A](expression, injectValue)
      }
    }

    implicit val seqArrByte: injector[Seq[Array[Byte]]] =
      new injector[Seq[Array[Byte]]] {
        implicit val typeCase: Option[TypeCase[Seq[Array[Byte]]]] = None

        override def inject(expression: String, injectFunction: Seq[Array[Byte]] => Seq[Array[Byte]]): Boson =
          new BosonInjector[Seq[Array[Byte]]](expression, injectFunction)

        override def inject(expression: String, injectValue: Seq[Array[Byte]]): Boson =
          new BosonInjectorValue[Seq[Array[Byte]]](expression, injectValue)
      }

    implicit val byteArr: injector[Array[Byte]] =
      new injector[Array[Byte]] {
        implicit val typeCase: Option[TypeCase[Array[Byte]]] = None

        override def inject(expression: String, injectFunction: Array[Byte] => Array[Byte]): Boson =
          new BosonInjector[Array[Byte]](expression, injectFunction)

        override def inject(expression: String, injectValue: Array[Byte]): Boson =
          new BosonInjectorValue[Array[Byte]](expression, injectValue)
      }

    implicit val double: injector[Double] =
      new injector[Double] {
        implicit val typeCase: Option[TypeCase[Double]] = Some(TypeCase[Double])

        override def inject(expression: String, injectFunction: Double => Double): Boson =
          new BosonInjector[Double](expression, injectFunction)

        override def inject(expression: String, injectValue: Double): Boson =
          new BosonInjectorValue[Double](expression, injectValue)
      }

    implicit val float: injector[Float] =
      new injector[Float] {
        implicit val typeCase: Option[TypeCase[Float]] = Some(TypeCase[Float])

        override def inject(expression: String, injectFunction: Float => Float): Boson =
          new BosonInjector[Float](expression, injectFunction)

        override def inject(expression: String, injectValue: Float): Boson =
          new BosonInjectorValue[Float](expression, injectValue)
      }

    implicit val instant: injector[Instant] =
      new injector[Instant] {
        implicit val typeCase: Option[TypeCase[Instant]] = Some(TypeCase[Instant])

        override def inject(expression: String, injectFunction: Instant => Instant): Boson =
          new BosonInjector[Instant](expression, injectFunction)

        override def inject(expression: String, injectValue: Instant): Boson =
          new BosonInjectorValue[Instant](expression, injectValue)
      }

    implicit val long: injector[Long] =
      new injector[Long] {
        implicit val typeCase: Option[TypeCase[Long]] = Some(TypeCase[Long])

        override def inject(expression: String, injectFunction: Long => Long): Boson =
          new BosonInjector[Long](expression, injectFunction)

        override def inject(expression: String, injectValue: Long): Boson =
          new BosonInjectorValue[Long](expression, injectValue)
      }

    implicit val int: injector[Int] =
      new injector[Int] {
        implicit val typeCase: Option[TypeCase[Int]] = Some(TypeCase[Int])

        override def inject(expression: String, injectFunction: Int => Int): Boson =
          new BosonInjector[Int](expression, injectFunction)

        override def inject(expression: String, injectValue: Int): Boson =
          new BosonInjectorValue[Int](expression, injectValue)
      }

    implicit val string: injector[String] =
      new injector[String] {
        implicit val typeCase: Option[TypeCase[String]] = Some(TypeCase[String])

        override def inject(expression: String, injectFunction: String => String): Boson =
          new BosonInjector[String](expression, injectFunction)

        override def inject(expression: String, injectValue: String): Boson =
          new BosonInjectorValue[String](expression, injectValue)
      }

    implicit val boolean: injector[Boolean] =
      new injector[Boolean] {
        implicit val typeCase: Option[TypeCase[Boolean]] = Some(TypeCase[Boolean])

        override def inject(expression: String, injectFunction: Boolean => Boolean): Boson =
          new BosonInjector[Boolean](expression, injectFunction)

        override def inject(expression: String, injectValue: Boolean): Boson =
          new BosonInjectorValue[Boolean](expression, injectValue)
      }
  }

}


trait Boson {

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding bson encoded into a byte array
    * @return Future with original or a modified Array[Byte].
    */
  def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]]

  /**
    * Apply this BosonImpl to the String and at some point in the future complete
    * the future with the resulting String. In the case of an Extractor this will result in
    * the immutable String being returned unmodified.
    *
    * @param bsonByteEncoding bson encoded into a String
    * @return Future with original or a modified String.
    */
  def go(bsonByteEncoding: String): Future[String]


  //  /**
  //    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
  //    * boson that is the parameter in the case of update/read conflicts.
  //    * the immutable byte array being returned unmodified.
  //    *
  //    * @param boson BosonImpl to fuse to.
  //    * @return the fused BosonImpl
  //    */
  //  def fuse(boson: Boson): Boson

}
