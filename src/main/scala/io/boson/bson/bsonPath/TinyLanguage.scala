package io.boson.bson.bsonPath

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers



/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class ScndGrammar(selectType: String)
case class Exists(term: String) extends Statement
case class ArrExpr(leftArg: Int, midArg: String, rightArg: Any) extends Statement
case class ArraySelectStatement(grammar: Grammar, arrEx: ArrExpr) extends Statement
case class SizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement
case class SizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar) extends Statement
case class SizeOfArray(arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement

class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r

  def program: Parser[Program] =
    (arraySelectStatement
      ||| sizeOfArrayStatement
      ||| exists
      ||| arrEx
      ||| grammar
      ||| sizeOfSelected
      ||| sizeOfArray) ^^ { s => new Program(List(s))}

  private def grammar: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def exists: Parser[Exists] = ("in" | "Nin") ^^ { d => Exists(d)}

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ ("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end") <~ "]" ^^ {
    case l ~ m ~ r => ArrExpr(l, m, r)
  }

  private def scndGrammar: Parser[ScndGrammar] = ("size" | "isEmpty") ^^ {
    g => ScndGrammar(g)
  }

  private def arraySelectStatement: Parser[ArraySelectStatement] = grammar ~ arrEx ^^ {
    case e ~ a => ArraySelectStatement(e, a)
  }

  private def sizeOfArrayStatement: Parser[SizeOfArrayStatement] = arraySelectStatement ~ scndGrammar ^^ {
    case a ~ s => SizeOfArrayStatement(a.grammar, a.arrEx, s)
  }

  private def sizeOfSelected: Parser[SizeOfSelected] = grammar ~ scndGrammar ^^ {
    case g ~ s => SizeOfSelected(g, s)
  }

  private def sizeOfArray: Parser[SizeOfArray] = arrEx ~ scndGrammar ^^ {
    case a ~ s => SizeOfArray(a, s)
  }

}
