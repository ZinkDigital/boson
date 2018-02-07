package io.boson.bson.bsonPath

import java.io.Serializable

import io.boson.bson.bsonImpl.CustomException

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import io.boson.bson.bsonImpl.Dictionary._
import io.restassured.internal.support.ParameterUpdater.Serializer


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
case class ROOT() extends Statement
case class MoreKeys(first: Statement, list: List[Statement], dotList: List[String]) extends Statement
//case class MoreKeysRoot(first: Statement, list: List[Statement], dotList: List[String]) extends Statement
//case class MoreKeysFinal(dots: Option[String], first: Statement, list: List[Statement]) extends Statement

class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = P_NUMBER.r

  private def word: Parser[String] =P_WORD.r //  symbol "+" is parsed

  def program: Parser[Program] =
  moreKeysFinal ^^ { s => {
    new Program(List(s)) }
  }

  private def key: Parser[Key] = word ^^ { w => Key(w) }

  private def halfName: Parser[HalfName] = opt(word) ~ rep1(STAR ~ opt(word)) ^^ {
    case Some(x) ~ list =>
      val s: String = list.foldRight("")((a,b) => {a._1.concat(a._2.getOrElse("")).concat(b)})
      HalfName(x.concat(s))
    case None ~ list =>
      val s: String = list.foldRight("")((a,b) => {a._1.concat(a._2.getOrElse("")).concat(b)})
      HalfName(s)

    case _ => throw CustomException(E_HALFNAME)
  }

  private def keyHasElem: Parser[HasElem] = key ~ (P_HAS_ELEM ~> word <~ P_CLOSE_BRACKET) ^^ {
    case k ~ w =>
      HasElem(k.key, w)
  }

  private def keyHasHalfelem: Parser[HasElem] = key ~ (P_HAS_ELEM ~> halfName <~ P_CLOSE_BRACKET) ^^ {
    case k ~ w =>
      HasElem(k.key, w.half)
  }

  private def halfnameHasElem: Parser[HasElem] = halfName ~ (P_HAS_ELEM ~> word <~ P_CLOSE_BRACKET) ^^ {
    case k ~ w => HasElem(k.half, w)
  }

  private def halfnameHasHalfelem: Parser[HasElem] = halfName ~ (P_HAS_ELEM ~> halfName <~ P_CLOSE_BRACKET) ^^ {
    case k ~ w => HasElem(k.half, w.half)
  }

  private def arrEx: Parser[ArrExpr] = P_OPEN_BRACKET ~>
    (C_FIRST | C_ALL | C_END | ((number ^^ {_.toInt}) ~ opt((TO_RANGE | TO_RANGE | UNTIL_RANGE | UNTIL_RANGE) ~ ((number ^^ {_.toInt}) | C_END))))~ P_CLOSE_BRACKET ^^ {
    //case (str) => ArrExpr(-1,Some(str._2), Some(-1) )
    case (l:Int) ~ Some(m ~ r) ~ _ => ArrExpr(l, Some(m.asInstanceOf[String]), Some(r)) //[#..#]
    case (l:Int) ~ None ~ _  => ArrExpr(l, None, None) //[#]
    case (str:String) ~ _ => ArrExpr(0,Some(str), None )
    case _ => throw new RuntimeException(E_MOREKEYS)
  }

  private def keyWithArrEx: Parser[KeyWithArrExpr] = word ~ arrEx ^^ {
    case k ~ a => KeyWithArrExpr(k, a) //Key[#..]
  }

  private def halfKeyWithArrEx: Parser[KeyWithArrExpr] = halfName ~ arrEx ^^ {
    case k ~ a => KeyWithArrExpr(k.half, a) //Key[#..]
  }

  private def root: Parser[ROOT] = "." ^^ (k => ROOT())

  private def moreKeysFinal: Parser[MoreKeys] =  opt(C_DOUBLEDOT | C_DOT) ~ opt(halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem| halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ~ rep((C_DOUBLEDOT | C_DOT) ~ (halfKeyWithArrEx | keyWithArrEx | halfnameHasHalfelem | halfnameHasElem | keyHasHalfelem | keyHasElem | halfName |  arrEx | key) ) ^^ {
      case Some(dots) ~ None ~ list if dots.equals(".") =>
        MoreKeys(ROOT(), list.map(elem => elem._2), list.map(elem => elem._1))
      case None ~ first ~ list if first.isDefined=>
        MoreKeys(first.get, list.map(elem => elem._2), List(C_DOUBLEDOT) ++ list.map(elem => elem._1)) //this is replacing the original/working moreKeys
      case Some(dots) ~ first ~ list if dots.equals(C_DOUBLEDOT) & first.isDefined =>
        MoreKeys(first.get, list.map(elem => elem._2), List(dots) ++ list.map(elem => elem._1)) //option of starting with .., same as the case before
      case Some(dots) ~ first ~ list if dots.equals(C_DOT) =>
        MoreKeys(first.get, list.map(elem => elem._2), List(dots) ++ list.map(elem => elem._1))
      case _ => throw new RuntimeException(E_MOREKEYS)

  }
}
