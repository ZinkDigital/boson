package io.boson.bson.bsonPath

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers



/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class KeyWithGrammar(key: String, grammar: Grammar, scndKey: Option[String]) extends Statement
case class KeyWithArrExpr(key: String,arrEx: ArrExpr, scndKey: Option[String]) extends Statement
case class ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any]) extends Statement
//case class ScndGrammar(selectType: String)
//case class Exists(term: String) extends Statement
//case class ArraySelectStatement(grammar: Grammar, arrEx: ArrExpr) extends Statement
//case class KeyStatement(key: String) extends Statement
//case class SizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar) extends Statement
//case class SizeOfArray(arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement

class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r
  //private val word: Regex =  """[a-z]+""".r
  def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r  //  TODO:further tests needed to prove this is the best regular-expression to use

  def program: Parser[Program] =
    ( keyWithGrammar
      ||| arrEx
      ||| grammar
      ||| keyWithArrEx) ^^ { s => new Program(List(s))}
  //     ||| sizeOfArrayStatement
  //     ||| exists
//      ||| sizeOfSelected
//      ||| sizeOfArray
  // |||arraySelectStatement)

//  private def keyStatement: Parser[KeyStatement] = word ^^ {
//    w => KeyStatement(w)
//  }

  private def keyWithGrammar: Parser[KeyWithGrammar] = word ~ ("." ~> grammar) ~ opt("." ~> word) ^^ {
    case k ~ g ~ None  => KeyWithGrammar(k,g, None)
    case k ~ g ~ Some(sK)  => KeyWithGrammar(k,g,Some(sK))
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ~ opt("." ~> word) ^^ {
    case k ~ a  ~ None => KeyWithArrExpr(k,a,None)
    case k ~ a  ~ Some(sK) => KeyWithArrExpr(k,a,Some(sK))
  }

  private def grammar: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

//  private def exists: Parser[Exists] = ("in" | "Nin") ^^ { d => Exists(d)}

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ opt(("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end")) <~ "]" ^^ {
    case l ~ Some(m ~ r) => ArrExpr(l, Some(m), Some(r))
    case l ~ None => ArrExpr(l, None, None)
  }

//  private def scndGrammar: Parser[ScndGrammar] = ("size" | "isEmpty") ^^ {
//    g => ScndGrammar(g)
//  }

//  private def arraySelectStatement: Parser[ArraySelectStatement] = grammar ~ arrEx ^^ {
//    case e ~ a => ArraySelectStatement(e, a)
//  }

//  private def sizeOfArrayStatement: Parser[SizeOfArrayStatement] = arraySelectStatement ~ scndGrammar ^^ {
//    case a ~ s => SizeOfArrayStatement(a.grammar, a.arrEx, s)
//  }

//  private def sizeOfSelected: Parser[SizeOfSelected] = grammar ~ scndGrammar ^^ {
//    case g ~ s => SizeOfSelected(g, s)
//  }

//  private def sizeOfArray: Parser[SizeOfArray] = arrEx ~ scndGrammar ^^ {
//    case a ~ s => SizeOfArray(a, s)
//  }

}
