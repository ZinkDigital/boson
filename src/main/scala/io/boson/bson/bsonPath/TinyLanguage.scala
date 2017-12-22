package io.boson.bson.bsonPath

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers



/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class KeyWithGrammar(key: String, grammar: Grammar) extends Statement
case class KeyWithArrExpr(key: String,arrEx: ArrExpr, scndKey: Option[String]) extends Statement
case class ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any], scndKey: Option[String]) extends Statement
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

  private def keyWithGrammar: Parser[KeyWithGrammar] = word ~ ("." ~> grammar) ^^ {
    case k ~ g => KeyWithGrammar(k,g)
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ^^ {
    case k ~ a if a.scndKey.isDefined => KeyWithArrExpr(k,a,a.scndKey)  //Key.[#..].2ndKey
    case k ~ a => KeyWithArrExpr(k,a,None)  //Key.[#..]
  }

  private def grammar: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ opt(("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end")) ~ "]" ~ opt("." ~> word) ^^ {
    case l ~ Some(m ~ r) ~ _ ~ None => ArrExpr(l, Some(m), Some(r), None)  //[#..#]
    case l ~ Some(m ~ r) ~ _ ~ Some(sK) => ArrExpr(l, Some(m), Some(r), Some(sK))  //[#..#].2ndKey
    case l ~ None ~ _ ~ None => ArrExpr(l, None, None, None) //[#]
    case l ~ None ~ _ ~ Some(sK) => ArrExpr(l, None, None, Some(sK)) //[#].2ndKey
  }

}
