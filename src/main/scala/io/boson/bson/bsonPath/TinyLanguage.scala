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
case class HalfName(half: Option[String]) extends Statement
case class Everything(key: String) extends Statement
case class HasElem(key: Key, elem: String) extends Statement
case class Key(key: String) extends Statement
class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r
  def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r  //  symbol "+" is parsed
  //^(?!last|first|all)
  def program: Parser[Program] =
    ( keyWithGrammar
      ||| arrEx
      ||| grammar
      ||| keyWithArrEx
      ||| halfName
      ||| everything
      ||| key
      ||| hasElem) ^^ { s => new Program(List(s))}

  private def hasElem: Parser[HasElem] = key ~ (".[@" ~> word <~ "]") ^^ {
    case k ~ w => HasElem(k,w)
  }

  private def key: Parser[Key] = word ^^ { w => Key(w) }

  private def everything: Parser[Everything] = "*" ^^ { k => Everything(k) }

  private def halfName: Parser[HalfName] =  opt(word) ~ "*" ~ opt(word) ^^ {
    case Some(x)~ "*" ~ Some(y) =>  HalfName(Option(x.concat("*").concat(y)))
    case None~ "*" ~ Some(y) => HalfName(Option("*".concat(y)))
    case Some(x)~ "*" ~ None => HalfName(Option(x.concat("*")))
    case _ => HalfName(Option("wdwd"))


     }

  private def keyWithGrammar: Parser[KeyWithGrammar] = word ~ ("." ~> grammar) ^^ {
    case k ~ g => KeyWithGrammar(k,g)
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ^^ {
    case k ~ a if a.scndKey.isDefined => KeyWithArrExpr(k,a,a.scndKey)  //Key.[#..].2ndKey
    case k ~ a => KeyWithArrExpr(k,a,None)  //Key.[#..]
  }

  private def grammar: Parser[Grammar] = "." ~> ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ opt(("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end")) ~ "]" ~ opt("." ~> word) ^^ {
    case l ~ Some(m ~ r) ~ _ ~ None => ArrExpr(l, Some(m), Some(r), None)  //[#..#]
    case l ~ Some(m ~ r) ~ _ ~ Some(sK) => ArrExpr(l, Some(m), Some(r), Some(sK))  //[#..#].2ndKey
    case l ~ None ~ _ ~ None => ArrExpr(l, None, None, None) //[#]
    case l ~ None ~ _ ~ Some(sK) => ArrExpr(l, None, None, Some(sK)) //[#].2ndKey
  }

}
