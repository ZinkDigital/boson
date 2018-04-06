package io.zink.boson.bson.bsonPath

import org.parboiled2.CharPredicate.Digit
import org.parboiled2._
import shapeless.HNil

import scala.collection.immutable
import scala.util.{Failure, Success}

sealed abstract class ProgStatement

case class KeyWithArrExpr1(key: String, arrEx: ArrExpr1) extends ProgStatement
case class ArrExpr1(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) extends ProgStatement
case class HalfName1(half: String) extends ProgStatement  { def getValue: String = half}
case class HasElem1(key: String, elem: String) extends ProgStatement
case class Key1(key: String) extends ProgStatement  { def getValue: String = key}
object ROOT1 extends ProgStatement

case class MoreKeys1(statementList: Seq[ProgStatement], dotsList: Seq[String]) extends ProgStatement

case class RangeCondition(value: String) extends ProgStatement { def getValue: String = value}


class DSLParser(val input: ParserInput) extends Parser with StringBuilding {

  //  the root rule

  def Final = rule {
    optional(capture(".." | ".")) ~ Exp ~> {
      (dots, expr: MoreKeys1) => {
          if (dots.isDefined) MoreKeys1(expr.statementList, expr.dotsList.+:(dots.get))
          else MoreKeys1(expr.statementList, expr.dotsList.+:(".."))
        }
    }
  }



  def Exp: Rule1[MoreKeys1] = rule {
    Statements ~ oneOrMore(
      (capture(".." | ".") ~ Statements ~> ((first: ProgStatement, dots: String, second) => {
        first match {
          case MoreKeys1(listS,listD) => (listS.:+(second),listD.:+(dots))
          case _ => (Seq(first).:+(second),Seq(dots))
        }
      })) ~> (lists =>MoreKeys1(lists._1,lists._2)))
  }

  def Statements: Rule1[ProgStatement] = rule { _HasElem | _HalfName | _KeyWithArrExpr1 | _ArrExpr | _Key/*| _ROOT*/}

  def _KeyWithArrExpr1: Rule1[KeyWithArrExpr1] = rule { _Key ~ _ArrExpr ~> ((key: Key1,arr: ArrExpr1) => KeyWithArrExpr1(key.key,arr)) }

  def _ArrExpr: Rule1[ArrExpr1] = rule { ws('[') ~ Numbers ~ WhiteSpace ~ optional(_RangeCondition) ~ WhiteSpace ~ optional(Numbers) ~ ws(']') ~>
    ((leftInt: Int,mid,rightInt) => ArrExpr1(leftInt, mid,rightInt)) }

  def _HasElem: Rule1[HasElem1] = rule {
    (HalfNameUnwrapped | Word) ~ ws('[') ~ ws('@') ~ (HalfNameUnwrapped | Word) ~ ws(']') ~> {
      (first: String,second: String) => HasElem1(first,second)
    }
  }

  //def Value = rule { (_Key | _HalfName) ~> (Program1(List(_)))}

  def _Key: Rule1[Key1] = rule { Word ~> Key1 }

  def _HalfName: Rule1[HalfName1] = rule { HalfNameUnwrapped ~> HalfName1 }

  def HalfNameUnwrapped = rule {
    (capture(ws('*')) ~ zeroOrMore(
      Word ~ ws('*') ~> ((first: String,second) => first.concat(second).concat("*")) |
        Word ~> ((first: String,second) => first.concat(second))
    )) |
      (Word ~ oneOrMore(
        ws('*') ~ Word ~> ((first: String, second) => first.concat("*").concat(second)) |
          ws('*') ~> ((first: String) => first.concat("*"))
      ))
  }

  def _ROOT = rule { OneDot ~ push(ROOT1) }

  def Word = rule{ capture(oneOrMore("a"-"z" | "A"-"Z" | """\u00C0""" - """\u017F""")) }

  def _RangeCondition: Rule[HNil, shapeless.::[RangeCondition, HNil]] = rule { RangeConditionUnwrapped ~> RangeCondition}

  def RangeConditionUnwrapped: Rule[HNil, shapeless.::[String, HNil]] = rule { clearSB() ~ ("to" ~ appendSB("to") | "until" ~ appendSB("until")) ~ push(sb.toString) }

  def Numbers = rule { capture(Digits) ~> (_.toInt) }

  def Digits = rule { oneOrMore(CharPredicate.Digit) }

  def WhiteSpace = rule { zeroOrMore(WhiteSpaceChar) }

  def ws(c: Char) = rule { c }

  //def Dots = rule { OneDot | TwoDots }

  val WhiteSpaceChar = CharPredicate(" \n\r\t\f")

  val OneDot = CharPredicate(".")

  //val TwoDots = CharPredicate("..")

}

object Main extends App {

//  parse("Àñçónío")  //Key
//  parse("Ze[0 until 9]")  //KeyWithArr
//  parse("[3 to 11]")  //ArrExpr
//  println()
//  parse("lol*") //HalfName
//  parse("lol*l")  //HalfName
//  parse("lol*l*l*l*l*l*")  //HalfName
//  parse("*werty") //HalfName
//  parse("*wert*") //HalfName
//  parse("*nf*nit*") //HalfName
//  parse("*") //HalfName
//
//  parse(".")  //ROOT
//
//  parse("qwerty[@min]") //HasElem
//  parse("qwe*rty[@m*n]") //HasElem
//  parse("qwe*t*[@min]") //HasElem
//  parse("qwerty[@*i*]") //HasElem

  parse(".Store.Book[0]..SpecialEditions[@Price].Title")


  def parse(expression: String)/*: ProgStatement*/ = {
    val parser = new DSLParser(expression)
    parser.Final.run() match {
      case Success(result)  =>  println(result)//;result
      case Failure(e)  => throw e
    }
  }

}
