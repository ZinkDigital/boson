package io.boson.bson.bsonPath

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers


/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
case class Grammar(selectType: String) extends Statement
case class KeyWithGrammar(key: String, grammar: Grammar) extends Statement
case class KeyWithArrExpr(key: String,arrEx: ArrExpr/*, scndKey: Option[String]*/) extends Statement
case class ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any]/*, scndKey: Option[String]*/) extends Statement
case class HalfName(half: String) extends Statement
//case class Everything(key: String) extends Statement
case class HasElem(key: String, elem: String) extends Statement
case class MoreKeys(first: Statement, list: List[Statement], last: Option[Statement]) extends Statement
case class Key(key: String) extends Statement
class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r

  private def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r //  symbol "+" is parsed
  //^(?!last|first|all)
  def program: Parser[Program] =
  (moreKeys
    ||| keyWithGrammar
    ||| grammar
    ||| keyWithArrEx
    //||| everything
    ||| halfnameHasHalfelem
    ||| halfnameHasElem
    ||| keyHasHalfelem
    ||| keyHasElem
    ||| halfName
    ||| arrEx
    ||| key) ^^ { s => {
    //println(s)
    new Program(List(s)) }
  }

  private def key: Parser[Key] = word ^^ { w => Key(w) }

  private def halfName: Parser[HalfName] = opt(word) ~ "*" ~ opt(word) ^^ {
    case Some(x) ~ "*" ~ Some(y) => HalfName(x.concat("*").concat(y))
    case None ~ "*" ~ Some(y) => HalfName("*".concat(y))
    case Some(x) ~ "*" ~ None => HalfName(x.concat("*"))
    case None ~ "*" ~ None => HalfName("*")
  }

  private def keyHasElem: Parser[HasElem] = key ~ (".[@" ~> word <~ "]") ^^ {
    case k ~ w => HasElem(k.key, w)
  }

  private def keyHasHalfelem: Parser[HasElem] = key ~ (".[@" ~> halfName <~ "]") ^^ {
    case k ~ w => HasElem(k.key, w.half)
  }

  private def halfnameHasElem: Parser[HasElem] = halfName ~ (".[@" ~> word <~ "]") ^^ {
    case k ~ w => HasElem(k.half, w)
  }

  private def halfnameHasHalfelem: Parser[HasElem] = halfName ~ (".[@" ~> halfName <~ "]") ^^ {
    case k ~ w => HasElem(k.half, w.half)
  }

  /*private def everything: Parser[Everything] = "*" ^^ { k =>
    //println(k)
    Everything(k) }*/

  private def arrEx: Parser[ArrExpr] = "[" ~> (number ^^ {
    _.toInt
  }) ~ opt(("to" | "To" | "until" | "Until") ~ ((number ^^ {
    _.toInt
  }) | "end")) ~ "]"/* ~ opt("." ~> word) */^^ {
    case l ~ Some(m ~ r) ~ _ /*~ None*/ => ArrExpr(l, Some(m), Some(r)/*, None*/) //[#..#]
    //case l ~ Some(m ~ r) ~ _ ~ Some(sK) => ArrExpr(l, Some(m), Some(r), Some(sK)) //[#..#].2ndKey
    case l ~ None ~ _ /*~ None*/ => ArrExpr(l, None, None/*, None*/) //[#]
    //case l ~ None ~ _ ~ Some(sK) => ArrExpr(l, None, None, Some(sK)) //[#].2ndKey
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ^^ {
   // case k ~ a if a.scndKey.isDefined => KeyWithArrExpr(k, a, a.scndKey) //Key.[#..].2ndKey
    case k ~ a => KeyWithArrExpr(k, a/*, None*/) //Key.[#..]
  }

  private def grammar: Parser[Grammar] = "." ~> ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def keyWithGrammar: Parser[KeyWithGrammar] =  word ~ ("." ~> grammar) ^^ {
    case k ~ g => KeyWithGrammar(k, g)
  }


  private def moreKeys: Parser[MoreKeys] = (keyWithArrEx /*| everything */| halfnameHasHalfelem| halfnameHasElem |   keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ~ rep1("." ~> (keyWithArrEx /*| everything*/ | halfnameHasHalfelem| halfnameHasElem |   keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ~ opt("." ~>(keyWithGrammar | grammar | keyWithArrEx /*| everything*/ | halfnameHasHalfelem| halfnameHasElem |   keyHasHalfelem | keyHasElem | halfName |  arrEx | key ) )^^ {
   /* x =>
      println("Tiny Language   " + x)
      MoreKeys()*/
    case first ~ list ~ Some(last) =>
      println(first +"   "+ list + "    " + last)
      MoreKeys(first, list, Some(last))
    case first ~ list ~ None =>
      println(first +"   "+ list + "    " + "None")
      MoreKeys(first, list, None)

  }

}
