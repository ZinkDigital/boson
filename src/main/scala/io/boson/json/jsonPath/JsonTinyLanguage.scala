package io.boson.json.jsonPath

/**
  * Created by Ricardo Martins on 19/01/2018.
  */

import io.boson.bson.bsonImpl.CustomException

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers


/**
  * Created by Tiago Filipe on 13/10/2017.
  */

sealed trait Statement
case class JsonKey(key: String) extends Statement
case class JsonValue(value:Statement) extends Statement
case class JsonEntry(key:JsonKey, value:JsonValue) extends Statement
case class JsonArray(list: List[JsonValue]) extends Statement
case class JsonObj(list: List[JsonEntry]) extends Statement
case class JsonParser(root: Statement) extends Statement
case class JsonNumber(number: String) extends Statement



class JsonProgram(val statement: Statement)

class JsonTinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r

  private def word: Parser[String] = """[/^[a-zA-Z\u00C0-\u017F]+\d_-]+""".r //  symbol "+" is parsed
  //^(?!last|first|all)

  def programJson: Parser[JsonProgram] = jsonParser ^^ {s=>new JsonProgram(s)}

  private def jsonKey: Parser[JsonKey] = "\"" ~> word <~ "\"" ^^ {
    x => JsonKey(x)
  }
  private def jsonNumber: Parser[JsonNumber] = number ^^ {
    x => JsonNumber(x)
  }
  private def jsonValue: Parser[JsonValue] = (jsonKey | jsonNumber | jsonObject | jsonArray) ^^ {
    x => JsonValue(x)
  }
  private def jsonEntry: Parser[JsonEntry] = (jsonKey ~ ":" ~ jsonValue ) ^^ {

    case (x:JsonKey)~":"~(y:JsonValue) => JsonEntry(x, y)
  }
  private def jsonObject: Parser[JsonObj] = "{"~> rep(jsonEntry ~ opt(",")) <~ "}" ^^ {
    entries => JsonObj(entries.map(entry => entry._1))
  }
  private def jsonArray: Parser[JsonArray] = "["~> rep(jsonValue ~ opt(",")) <~ "]" ^^ {
    entries => JsonArray(entries.map(entry => entry._1))
  }
  private def jsonParser: Parser[JsonParser] = (jsonObject | jsonArray) ^^ {
    case x:JsonObj =>   JsonParser(x)
    case x:JsonArray =>   JsonParser(x)
    case x =>
      println(x)
      throw CustomException(s"Catching $x")

  }

}
