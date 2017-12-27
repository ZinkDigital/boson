package io.boson.bson.bsonImpl.injectors

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
  * Created by Ricardo Martins on 14/12/2017.
  */
sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class ArrExpr(leftArg: Int, midArg: String, rightArg: Any) extends Statement
//case class ScndGrammar(selectType: String)
//case class Exists(term: String) extends Statement
//case class ArraySelectStatement(grammar: Grammar, arrEx: ArrExpr) extends Statement
//case class SizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfArray(arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement

class ProgramInj(val statement: List[Statement])

/*
arraySelectStatement
      ||| sizeOfArrayStatement
      ||| exists
      ||| arrEx
      |||
      ||| sizeOfSelected
      ||| sizeOfArray

*
 */
class TinyLanguageInj extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r

  def program: Parser[ProgramInj] =
    (arrEx
      ||| grammar

      ) ^^ { s => new ProgramInj(List(s))}

  private def grammar: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ ("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end") <~ "]" ^^ {
    case l ~ m ~ r => ArrExpr(l, m, r)
  }
}
