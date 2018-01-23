package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.CustomException

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers


/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
//case class Grammar(selectType: String) extends Statement
//case class KeyWithGrammar(key: String, grammar: Grammar) extends Statement
case class KeyWithArrExpr(key: String,arrEx: ArrExpr) extends Statement
case class ArrExpr(leftArg: Int, midArg: Option[String], rightArg: Option[Any]) extends Statement
case class HalfName(half: String) extends Statement
case class HasElem(key: String, elem: String) extends Statement
case class Key(key: String) extends Statement

case class MoreKeys(first: Statement, list: List[Statement], dotList: List[String]) extends Statement
case class MoreKeysRoot(first: Statement, list: List[Statement], dotList: List[String]) extends Statement
//case class MoreKeysFinal(dots: Option[String], first: Statement, list: List[Statement]) extends Statement

class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r

  private def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r //  symbol "+" is parsed

  def program: Parser[Program] =
  moreKeysFinal ^^ { s => {
    new Program(List(s)) }
  }

  private def key: Parser[Key] = word ^^ { w => Key(w) }

  private def halfName: Parser[HalfName] = opt(word) ~ "*" ~ opt(word) ^^ {
    case Some(x) ~ "*" ~ Some(y) => HalfName(x.concat("*").concat(y))
    case None ~ "*" ~ Some(y) => HalfName("*".concat(y))
    case Some(x) ~ "*" ~ None => HalfName(x.concat("*"))
    case None ~ "*" ~ None => HalfName("*")
    case _ => throw CustomException("Error Parsing HalfName!")
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
  }) ~ opt(("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end")) ~ "]" ^^ {
    case l ~ Some(m ~ r) ~ _ => ArrExpr(l, Some(m), Some(r)) //[#..#]
    case l ~ None ~ _  => ArrExpr(l, None, None) //[#]
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ ("." ~> arrEx) ^^ {
    case k ~ a => KeyWithArrExpr(k, a) //Key.[#..]
  }

  private def halfKeyWithArrEx: Parser[KeyWithArrExpr] = halfName ~ ("." ~> arrEx) ^^ {
    case k ~ a => KeyWithArrExpr(k.half, a) //Key.[#..]
  }

//  private def grammar: Parser[Grammar] = "." ~> ("first" | "last" | "all") ^^ {
//    g => Grammar(g)
//  }

//  private def keyWithGrammar: Parser[KeyWithGrammar] =  word ~ ("." ~> grammar) ^^ {
//    case k ~ g => KeyWithGrammar(k, g)
//  }


//  private def moreKeys: Parser[MoreKeys] = (halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem| halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ~ rep("." ~> (halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem | halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ^^ {
////    case first ~ list ~ Some(last) =>
////      println(first +"   "+ list + "    " + last)
////      MoreKeys(first, list, Some(last))
//    case first ~ list =>
//      println(first +"   "+ list)
//      MoreKeys(first, list)
//
//  }

//  private def moreKeysDOT: Parser[MoreKeys] = "." ~>(keyWithArrEx | halfnameHasHalfelem| halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ~ rep("." ~> (keyWithArrEx | halfnameHasHalfelem | halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ^^ {
////    case first ~ list ~ Some(last) =>
////      println(first +"   "+ list + "    " + last)
////      MoreKeys(first, list, Some(last))
//    case first ~ list =>
//      println(first +"   "+ list)
//      MoreKeys(first, list)
//
//  }

//  private def moreKeysFinal: Parser[MoreKeysFinal] =
//    rep1( opt(".." | ".") ~ (halfKeyWithArrEx |keyWithArrEx | halfnameHasHalfelem| halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ^^ {
//
//      case list:List[Statement] =>
//        println(list)
//        MoreKeysFinal(list)
//    }

  private def moreKeysFinal: Parser[Statement] = opt(".." | ".") ~ (halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem| halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ~ rep((".." | ".") ~ (halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem | halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ^^ {
    case None ~ first ~ list =>
      MoreKeys(first,list.map(elem => elem._2),List("..") ++ list.map(elem => elem._1))  //this is replacing the original/working moreKeys
    case Some(dots) ~ first ~ list if dots.equals("..")=>
      MoreKeys(first, list.map(elem => elem._2), List(dots) ++ list.map(elem => elem._1))  //option of starting with .., same as the case before
    case Some(dots) ~ first ~ list if dots.equals(".")=>
      MoreKeysRoot(first, list.map(elem => elem._2), List(dots) ++ list.map(elem => elem._1))  //first key has to match on Root

//    case Some(dots) ~ first ~ list => //  this case doesn't exist, we ain't implementing the middle dots yet
//      println(s"Dots: '$dots', first: $first, list.map(elem => elem._1): ${list.map(elem => elem._1)}, list.map(elem => elem._2): ${list.map(elem => elem._2)}")
//      MoreKeysFinal(Some(dots),first,list.map(elem => elem._2))
    case _ => throw new RuntimeException("not desired case yet")

  }

}
