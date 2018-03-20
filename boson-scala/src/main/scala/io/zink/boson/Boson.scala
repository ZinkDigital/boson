package io.zink.boson

import java.nio.ByteBuffer

import io.zink.boson.bson.bsonImpl.extractLabels
import io.zink.boson.impl.{BosonExtractor, BosonExtractorObj, BosonInjector, BosonValidate}
import shapeless.{HList, LabelledGeneric, Lazy, the}
//import java.util.function.Consumer
import scala.tools.reflect.ToolBox
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.concurrent.Future

object Extractor {

  def apply[A](expression: String, extractFunction: A => Unit)(implicit ext: Extractor[A]): Boson = ext.extract(expression,extractFunction)

  implicit def caseClass[A, L <:HList](implicit
                                       f: LabelledGeneric.Aux[A, L],
                                       ext: extractLabels[L]): Extractor[A] =
    new Extractor[A] {
      def extract(expression: String, extractFunction: A => Unit): Boson =
        new BosonExtractorObj[A,L](expression,extractFunction)
    }

  implicit def seqLiterals[A: Extractor]: Extractor[Seq[A]] =
    new Extractor[Seq[A]] {
      def extract(expression: String, extractFunction: Seq[A] => Unit): Boson =
        new BosonExtractor[Seq[A]](expression,extractFunction)
    }

  implicit val double: Extractor[Double] =
    new Extractor[Double] {
      override def extract(expression: String, extractFunction: Double => Unit): Boson =
        new BosonExtractor[Double](expression,extractFunction)
    }

  implicit val long: Extractor[Long] =
    new Extractor[Long] {
      override def extract(expression: String, extractFunction: Long => Unit): Boson =
        new BosonExtractor[Long](expression,extractFunction)
    }

  implicit val int: Extractor[Int] =
    new Extractor[Int] {
      override def extract(expression: String, extractFunction: Int => Unit): Boson =
        new BosonExtractor[Int](expression,extractFunction)
    }

  implicit val string: Extractor[String] =
    new Extractor[String] {
      override def extract(expression: String, extractFunction: String => Unit): Boson =
        new BosonExtractor[String](expression,extractFunction)
    }

  implicit val boolean: Extractor[Boolean] =
    new Extractor[Boolean] {
      override def extract(expression: String, extractFunction: Boolean => Unit): Boson =
        new BosonExtractor[Boolean](expression,extractFunction)
    }
}

object Boson {

  def validate[T, R <: HList](expression: String, validateFunction: T => Unit)(implicit
                                                                               f: LabelledGeneric.Aux[T, R],
                                                                               extract: extractLabels[R]) = new BosonValidate[T, R](expression, validateFunction)

  /**
    * Make an Extractor that will call the extract function (Consumer) according to
    * the given expression.
    *
    * @param expression
    * @param extractFunction
    * @param < T>
    * @return a BosonImpl that is a BosonExtractor
    */

//  def extractor[T, R <: HList](expression: String, extractFunction: T => Unit)(implicit
//                                                                               f: LabelledGeneric.Aux[T, R],
//                                                                               extract: extractLabels[R]) = {
//    //val bool = checkIfCaseClass[T]
//    //    if(bool) instExtractorObj[T,R](expression,extractFunction)
//    //    else instExtractor[T](expression,extractFunction)
//  //}
//
//  //implicit val gen = the[LabelledGeneric[T]]
//  new BosonExtractorObj[T, R](expression, extractFunction)
//}

  //  def extractor[T](expression: String, extractFunction: T => Unit) = {
  //    //implicit val gen = the[LabelledGeneric[T]]
  //    println("Should be any other type than case class")
  //    new BosonExtractor[T](expression, extractFunction)
  //  }

  /**
    * Make an Injector that will call the inject function (of T -> T) according to
    * the given expression.
    *
    * @param expression
    * @param injectFunction
    * @param < T>
    * @return
    */
  def injector[T, R <: HList](expression: String, injectFunction: T => T)(implicit
                                                                          f: LabelledGeneric.Aux[T, R],
                                                                          extract: extractLabels[R]) =
    new BosonInjector[T, R](expression, injectFunction)

//  def impl[T: c.WeakTypeTag](c: Context) = {
//    import c.universe._
//    val symbol = c.symbolOf[T]
//    symbol.asClass.isCaseClass match {
//      case true => c.Expr(Literal(Constant(true)))
//      case false => c.Expr(Literal(Constant(false)))
//    }
//  }
//
//  def checkIfCaseClass[T]: Boolean = macro impl[T]

//  private def extractor_impl[T, R <: HList](c: Context)(implicit T: c.WeakTypeTag[T]): Boson = {
//    import c.universe._
//    val symbol = c.weakTypeTag[T].tpe.typeSymbol
//    if (symbol.isClass || symbol.asClass.isCaseClass) {
//      instExtractorObj[T,R](expression,extractFunction)
//    } else {
//      instExtractor[T](expression,extractFunction)
//    }
//
//  }

}

trait Extractor[A] {
  def extract(expression: String, extractFunction: A => Unit): Boson
}

trait Boson {

  //def extractor[T](expression: String, extractFunction: Function[T, Unit])

  //  def apply[A](implicit f: Lazy[Generic[A]]): Generic[A]
  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor this will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteEncoding
    * @return
    */
  def go(bsonByteEncoding: Array[Byte]): Future[Array[Byte]]

  /**
    * Apply this BosonImpl to the byte array that arrives and at some point in the future complete
    * the future with the resulting byte array. In the case of an Extractor tis will result in
    * the immutable byte array being returned unmodified.
    *
    * @param bsonByteBufferEncoding
    * @return
    */
  def go(bsonByteBufferEncoding: ByteBuffer): Future[ByteBuffer]


  /**
    * Fuse one BosonImpl to another. The boson that is this should be executed first before the
    * boson that is the parameter in teh case of update/read conflicts.
    * the immutable byte array being returned unmodified.
    *
    * @param the BosonImpl to fuse to.
    * @return the fused BosonImpl
    */
  def fuse(boson: Boson): Boson

}
