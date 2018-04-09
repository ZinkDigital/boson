package io.zink.boson.bson.bsonPath

import org.parboiled2.CharPredicate.Digit
import org.parboiled2.ParserInput.StringBasedParserInput
import org.parboiled2._
import shapeless.HNil

import scala.collection.immutable
import scala.util.{Failure, Success, Try}

sealed abstract class ProgStatement

case class KeyWithArrExpr1(key: String, arrEx: ArrExpr1) extends ProgStatement
case class ArrExpr1(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) extends ProgStatement
case class HalfName1(half: String) extends ProgStatement  { def getValue: String = half}
case class HasElem1(key: String, elem: String) extends ProgStatement
case class Key1(key: String) extends ProgStatement  { def getValue: String = key}
object ROOT1 extends ProgStatement

case class MoreKeys1(statementList: List[ProgStatement], dotsList: List[String]) extends ProgStatement

case class RangeCondition(value: String) extends ProgStatement { def getValue: String = value}


class DSLParser(_expression: String) extends Parser with StringBuilding {

  var expression: String = _expression

  def input: ParserInput = new StringBasedParserInput(expression)

  def finalRun(): Try[MoreKeys1] = Final.run()

  //  the root rule
  def Final = rule {
    (optional(capture(".." | ".")) ~ Exp ~ EOI ~> {
      (dots, expr: ProgStatement) => {
        //println(s"EXPR -> $expr")
        expr match {
          case MoreKeys1(listS, listD) if dots.isDefined => MoreKeys1(listS, listD.+:(dots.get))
          case MoreKeys1(listS, listD) => MoreKeys1(listS, listD.+:(".."))
          case x if dots.isDefined => MoreKeys1(List(x), List(dots.get))
          case x => MoreKeys1(List(x), List(".."))
        }
      }
    }) | _ROOT ~ EOI ~> ((statement: ProgStatement) => MoreKeys1(List(statement), List(".")))
  }



  def Exp: Rule1[ProgStatement] = rule {
    Statements ~ zeroOrMore(
      (capture(".." | ".") ~ Statements ~> ((first: ProgStatement, dots: String, second) => {
        first match {
          case MoreKeys1(listS,listD) => (listS.:+(second),listD.:+(dots))
          case _ => (List(first).:+(second),List(dots))
        }
      })) ~> (lists =>MoreKeys1(lists._1,lists._2)))
  }

  def Statements: Rule1[ProgStatement] = rule { _KeyWithArrExpr1 | _ArrExpr | _HasElem | _HalfName | _Key }

  def _KeyWithArrExpr1: Rule1[KeyWithArrExpr1] = rule {
    (_Key | _HalfName) ~ _ArrExpr ~> (
      (key: ProgStatement, arr: ArrExpr1) => {
        key match {
          case Key1(value) => KeyWithArrExpr1(value, arr)
          case HalfName1(value) => KeyWithArrExpr1(value, arr)
        }
      })
  }

  def _ArrExpr: Rule1[ArrExpr1] = rule { ws('[') ~ (_ArrExprWithConditions | _ArrExprWithRange)  ~ ws(']') }

  def _ArrExprWithRange: Rule1[ArrExpr1] = rule {
    Numbers ~ WhiteSpace ~ optional(_RangeCondition) ~ WhiteSpace ~ optional(Numbers| capture("end")) ~> ((leftInt: Int,mid,right) => ArrExpr1(leftInt, mid,right))
  }

  def _ArrExprWithConditions: Rule1[ArrExpr1] = rule {
    (capture("first") | capture("end") | capture("all")) ~> ((cond: String) => ArrExpr1(0,Some(RangeCondition(cond)),None))
  }

  def _HasElem: Rule1[HasElem1] = rule {
    (HalfNameUnwrapped | Word) ~ ws('[') ~ ws('@') ~ (HalfNameUnwrapped | Word) ~ ws(']') ~> {
      (first: String,second: String) => HasElem1(first,second)
    }
  }

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

  def Word = rule{ capture(oneOrMore("a"-"z" | "A"-"Z" | """\u00C0""" - """\u017F""") ~ !anyOf(" ")) }

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

  //parse(".Store.Book[0]..SpecialEditions[@Price].Title")
  parse("..*k[all]..[0]")


  def parse(expression: String)/*: ProgStatement*/ = {
    val parser = new DSLParser(expression)
    parser.Final.run() match {
      case Success(result)  =>  println(result)//;result
      case Failure(e)  => throw e
    }
  }

}
