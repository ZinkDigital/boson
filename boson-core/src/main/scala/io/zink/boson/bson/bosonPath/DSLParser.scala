package io.zink.boson.bson.bosonPath

import org.parboiled2.ParserInput.StringBasedParserInput
import org.parboiled2._
import scala.util.Try
import io.zink.boson.bson.bosonImpl.Dictionary._

sealed trait Statement

case class KeyWithArrExpr(key: String, arrEx: ArrExpr) extends Statement
case class RangeCondition(value: String) extends Statement
case class ArrExpr(leftArg: Int, midArg: Option[RangeCondition], rightArg: Option[Any]) extends Statement
case class HalfName(half: String) extends Statement
case class HasElem(key: String, elem: String) extends Statement
case class Key(key: String) extends Statement
object ROOT extends Statement
case class ProgStatement(statementList: List[Statement], dotsList: List[String]) extends Statement



class DSLParser(val input: ParserInput/*_expression: String*/) extends Parser with StringBuilding {

  //var expression: String = _expression

  //  can't be on class constructor because of java api
  //def input: ParserInput = new StringBasedParserInput(expression)

  //  the method to call to initiate the parser
  def Parse(): Try[ProgStatement] = Final.run()

  //  the root rule
  def Final = rule {
    (optional(capture(C_DOUBLEDOT | C_DOT)) ~ Exp ~ EOI ~> {
      (dots, expr: Statement) => {
        expr match {
          case ProgStatement(listS, listD) if dots.isDefined => ProgStatement(listS, listD.+:(dots.get))
          case ProgStatement(listS, listD) => ProgStatement(listS, listD.+:(C_DOUBLEDOT))
          case x if dots.isDefined => ProgStatement(List(x), List(dots.get))
          case x => ProgStatement(List(x), List(C_DOUBLEDOT))
        }
      }
    }) | _ROOT ~ EOI ~> ((statement: Statement) => ProgStatement(List(statement), List(C_DOT)))
  }

  def Exp: Rule1[Statement] = rule {
    Statements ~ zeroOrMore(
      (capture(C_DOUBLEDOT | C_DOT) ~ Statements ~> ((first: Statement, dots: String, second) => {
        first match {
          case ProgStatement(listS,listD) => (listS.:+(second),listD.:+(dots))
          case _ => (List(first).:+(second),List(dots))
        }
      })) ~> (lists =>ProgStatement(lists._1,lists._2)))
  }

  def Statements: Rule1[Statement] = rule { _KeyWithArrExpr1 | _ArrExpr | _HasElem | _HalfName | _Key }

  def _KeyWithArrExpr1: Rule1[KeyWithArrExpr] = rule {
    (_Key | _HalfName) ~ _ArrExpr ~> (
      (key: Statement, arr: ArrExpr) => {
        key match {
          case Key(value) => KeyWithArrExpr(value, arr)
          case HalfName(value) => KeyWithArrExpr(value, arr)
        }
      })
  }

  def _ArrExpr: Rule1[ArrExpr] = rule { ws(P_OPEN_BRACKET) ~ (_ArrExprWithConditions | _ArrExprWithRange)  ~ ws(P_CLOSE_BRACKET) }

  def _ArrExprWithRange: Rule1[ArrExpr] = rule {
    Numbers ~ WhiteSpace ~ optional(_RangeCondition) ~ WhiteSpace ~ optional(Numbers| capture(C_END)) ~> ((leftInt: Int,mid,right) => ArrExpr(leftInt, mid,right))
  }

  def _ArrExprWithConditions: Rule1[ArrExpr] = rule {
    (capture(C_FIRST) | capture(C_END) | capture(C_ALL)) ~> ((cond: String) => ArrExpr(0,Some(RangeCondition(cond)),None))
  }

  def _HasElem: Rule1[HasElem] = rule {
    (HalfNameUnwrapped | Word) ~ ws(P_OPEN_BRACKET) ~ ws(P_HASELEM_AT) ~ (HalfNameUnwrapped | Word) ~ ws(P_CLOSE_BRACKET) ~> {
      (first: String,second: String) => HasElem(first,second)
    }
  }

  def _Key: Rule1[Key] = rule { Word ~> Key }

  def _HalfName: Rule1[HalfName] = rule { HalfNameUnwrapped ~> HalfName }

  def HalfNameUnwrapped = rule {
    (capture(ws(P_STAR)) ~ zeroOrMore(
      Word ~ ws(P_STAR) ~> ((first: String,second) => first.concat(second).concat(STAR)) |
        Word ~> ((first: String,second) => first.concat(second))
    )) |
      (Word ~ oneOrMore(
        ws(P_STAR) ~ Word ~> ((first: String, second) => first.concat(STAR).concat(second)) |
          ws(P_STAR) ~> ((first: String) => first.concat(STAR))
      ))
  }

  def _ROOT = rule { OneDot ~ push(ROOT) }

  def Word = rule{ capture(oneOrMore("a"-"z" | "A"-"Z" | """\u00C0""" - """\u017F""")) }

  def _RangeCondition = rule { RangeConditionUnwrapped ~> RangeCondition}

  def RangeConditionUnwrapped = rule { clearSB() ~ (TO_RANGE ~ appendSB(TO_RANGE) | UNTIL_RANGE ~ appendSB(UNTIL_RANGE)) ~ push(sb.toString) }

  def Numbers = rule { capture(Digits) ~> (_.toInt) }

  def Digits = rule { oneOrMore(CharPredicate.Digit) }

  def WhiteSpace = rule { zeroOrMore(WhiteSpaceChar) }

  def ws(c: Char) = rule { c }

  val WhiteSpaceChar = CharPredicate(" \n\r\t\f")

  val OneDot = CharPredicate(C_DOT)

}
