package io.boson.bson.bsonImpl.injectors

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

/**
  * Created by Ricardo Martins on 14/12/2017.
  */

sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class KeyWithGrammar(key: String, grammar: Grammar) extends Statement
case class KeyWithArrExpr(key: String,arrEx: ArrExpr) extends Statement
//case class ScndGrammar(selectType: String)
//case class Exists(term: String) extends Statement
case class ArrExpr(leftArg: Int, midArg: String, rightArg: Any) extends Statement
//case class ArraySelectStatement(grammar: Grammar, arrEx: ArrExpr) extends Statement
//case class KeyStatement(key: String) extends Statement
//case class SizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfArray(arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement

class ProgramInj(val statement: List[Statement])

class TinyLanguageInj extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r
  //private val word: Regex =  """[a-z]+""".r
  def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r  //  TODO:further tests needed to prove this is the best regular-expression to use

  def program: Parser[ProgramInj] =
    ( keyWithGrammar
      ||| arrEx
      ||| grammar
      ||| keyWithArrEx) ^^ { s => new ProgramInj(List(s))}
  //     ||| sizeOfArrayStatement
  //     ||| exists
//      ||| sizeOfSelected
//      ||| sizeOfArray
  // |||arraySelectStatement)

//  private def keyStatement: Parser[KeyStatement] = word ^^ {
//    w => KeyStatement(w)
//  }

  private def keyWithGrammar: Parser[KeyWithGrammar] = word ~ ("." ~> grammar) ^^ {
    case k ~ g  => KeyWithGrammar(k,g)
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ^^ {
    case k ~ a => KeyWithArrExpr(k,a)
  }

  private def grammar: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

//  private def exists: Parser[Exists] = ("in" | "Nin") ^^ { d => Exists(d)}

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ ("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end") <~ "]" ^^ {
    case l ~ m ~ r => ArrExpr(l, m, r)
  }

}