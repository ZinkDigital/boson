package io.zink.boson.bson.catsValidation

import cats.syntax.either._
import cats.data._
import cats.data.Validated._
import cats.implicits._
import io.zink.boson.bson.bsonPath.{DSLParser, ProgStatement}

import scala.util.{Failure, Success}

sealed trait GlobalValidation {
  def errorMessage: Throwable
}

case class WrongExpression(error: Throwable) extends GlobalValidation {
  override def errorMessage: Throwable = error
}



sealed trait InterpreterValidatorNel {

  type ValidationResult[A] = ValidatedNel[GlobalValidation,A]

  def validateExpression(expression: String): ValidationResult[ProgStatement] = {
    new DSLParser(expression).Parse() match {
      case Success(result) => result.validNel
      case Failure(error) => WrongExpression(error).invalidNel
    }
  }
}

object InterpreterValidator extends InterpreterValidatorNel




