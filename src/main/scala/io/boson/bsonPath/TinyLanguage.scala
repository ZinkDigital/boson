package io.boson.bsonPath


import io.boson.bson.{BsonArray, BsonObject}
import io.boson.nettybson.NettyBson

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import scala.util.parsing.input.Positional



/**
  * Created by Tiago Filipe on 13/10/2017.
  */
sealed trait Statement extends Positional
case class Grammar(selectType: String) extends Statement
case class ScndGrammar(selectType: String)
case class Exists(term: String) extends Statement
case class ArrExpr(leftArg: Int, midArg: String, rightArg: Any) extends Statement
case class ArraySelectStatement(grammar: Grammar, arrEx: ArrExpr) extends Statement
case class SizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar) extends Statement
case class SizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar) extends Statement

class Program(val statement: List[Statement])

class TinyLanguage extends RegexParsers {

  private val number: Regex = """\d+(\.\d*)?""".r
  private val text: Regex = """([a-zA-Z]+)""".r

  private def grammar: Parser[String] = "first" | "last" | "all" //(| "max" | "min" | "in" | "Nin" | "size" | "isEmpty" | "type") PROBABLY BELONG TO OTHER PARSER    // "average" | "stddev"

  private def scndGrammar: Parser[String] = "size" | "isEmpty"

  private def exists: Parser[String] = "in" | "Nin"

  private def expr: Parser[String] = grammar ^^ {_.toString()}
  /* ^^ {
    case Some("first") =>
      netty.extract(netty.getByteBuf, key, "first").get.asInstanceOf[List[Any]].head
    case Some("last") =>
      val result = netty.extract(netty.getByteBuf, key, "last")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          value.asInstanceOf[List[Any]].last
        case Some(value) => value
        case None => None
      }
    case Some("all") =>
      val result = netty.extract(netty.getByteBuf, key, "all")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          value.asInstanceOf[List[Any]]
        case Some(value) => value
        case None => None
      }
    /*case Some("average") =>
      val result = netty.extract(netty.getByteBuf, key, "average")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          val totalAmount =
            value.asInstanceOf[List[Any]].foldLeft(0.0)((x, elem) => x + (elem match {
              case number: Double => number
              case number: Float => number
              case number: Int => number
              case number: Long => number
              case _ => 0
            }))
          totalAmount / value.asInstanceOf[List[Any]].size
        case Some(value) => value
        case None => None
      }*/
    case Some("max") =>
      val result = netty.extract(netty.getByteBuf, key, "max")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          val maxValue =
            value.asInstanceOf[List[Any]].map(elem => elem match {
              case number: Double => number
              case number: Float => number
              case number: Int => number
              case number: Long => number
              case _ => Double.NegativeInfinity
            }).max
          maxValue
        case Some(value) => value
        case None => None
      }
    case Some("min") =>
      val result = netty.extract(netty.getByteBuf, key, "min")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          val minValue =
            value.asInstanceOf[List[Any]].map(elem => elem match {
              case number: Double => number
              case number: Float => number
              case number: Int => number
              case number: Long => number
              case _ => Double.PositiveInfinity
            }).min
          minValue
        case Some(value) => value
        case None => None
      }
    /*case Some("stddev") =>
      val result = netty.extract(netty.getByteBuf, key, "stddev")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          val totalAmount =
            value.asInstanceOf[List[Any]].foldLeft(0.0)((x, elem) => x + (elem match {
              case number: Double => number
              case number: Float => number
              case number: Int => number
              case number: Long => number
              case _ => 0
            }))
          val mean = totalAmount / value.asInstanceOf[List[Any]].size
          val sum = value.asInstanceOf[List[Any]].foldLeft(0.0)((x, elem) => x + (elem match {
            case number: Double => (number - mean) * (number - mean)
            case number: Float => (number - mean) * (number - mean)
            case number: Int => (number - mean) * (number - mean)
            case number: Long => (number - mean) * (number - mean)
            case _ => 0
          }))
          math.sqrt(sum/value.asInstanceOf[List[Any]].size)
        case None => None
      }*/
    case Some("in") =>
      val result = netty.extract(netty.getByteBuf, key, "first")
      result match {
        case Some(_) => true
        case None => false
      }
    case Some("Nin") =>
      val result = netty.extract(netty.getByteBuf, key, "first")
      result match {
        case Some(_) => false
        case None => true
      }
    case Some("type") =>
      val result = netty.extract(netty.getByteBuf, key, "first")
      result match {
        case Some(value) => value.asInstanceOf[List[Any]].head.getClass.getSimpleName
        case None => None
      }
    case Some("size") =>
      val result = netty.extract(netty.getByteBuf, key, "first")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          value.asInstanceOf[List[BsonArray]].head.size
        case None => None
      }
    case Some("isEmpty") =>
      val result = netty.extract(netty.getByteBuf, key, "first")
      result match {
        case Some(value) if value.isInstanceOf[List[Any]] =>
          value.asInstanceOf[List[BsonArray]].head.size match {
            case 0 => true
            case _ => false
          }
        case None => None
      }
    case Some(_) =>
      throw new IllegalArgumentException("Condition doesnt exist in grammar")
    case None =>
      throw new IllegalArgumentException("Condition is empty")
  }*/

  private def arrEx: Parser[Any] = "[" ~ (number ^^ {_.toInt}) ~ text ~ ((number ^^ {_.toInt}) | "end") ~ "]"
  /*^^ {
    case _ ~ a ~ _ ~ "end" ~ _ =>
      netty.extract(netty.getByteBuf, key, "limit", Option(a), None) match {
        case Some(value) => value
        case None => None
      }
    case _ ~ a ~ ("to" | "To") ~ b ~ _ =>
      netty.extract(netty.getByteBuf, key, "limit", Option(a), Option(b.toInt))match {
        case Some(value) => value
        case None => None
      }
    case _ ~ a ~ ("until" | "Until") ~ b ~ _ =>
      netty.extract(netty.getByteBuf, key, "limit", Option(a), Option(b.toInt - 1))match {
        case Some(value) => value
        case None => None
      }
    case _ => "Language not defined"

  }
*/

  private def fltResult: Parser[Any] = ("<" | ">" | "\\>=" | "\\<=" | "==" | "!=") ~ (number ^^ {_.toDouble})
  /*^^ {
    case "<" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0.0
        }) < x.toDouble)
    case ">" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0.0
        }) > x.toDouble)
    case "\\<=" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0.0
        }) <= x.toDouble)
    case "\\>=" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0.0
        }) >= x.toDouble)
    case "==" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0.0
        }) == x.toDouble)
    case "!=" ~ x =>
      getListOfElements(netty, key).filter(elem => (
        elem match {
          case number: Double => number
          case number: Float => number
          case number: Int => number
          case number: Long => number
          case _ => 0
        }) != x.toDouble)
    case _ => "Lost case"
  }
*/

  private val arr: ListBuffer[Any] = new ListBuffer[Any]()

  def finalExpr(netty: NettyBson, key: String): Parser[Any] = opt(expr) ~ opt(arrEx) ~ opt(fltResult) ~ opt(scndGrammar) ~ opt(exists) ^^ {
    case None ~ Some(value) ~ None ~ None ~ None=>
      println("Inside case None Some None None")
      arrayExtractor(netty, key, value) match {
        case None => List.empty
        case v => v
      }
    case None ~ Some(value) ~ None ~ Some(info) ~ None=>
      println("Inside case None Some None Some")
      val result = arrayExtractor(netty, key, value)
      result match {
        case None =>
          info match {
            case "size" => 0
            case "isEmpty" => true
            case _ => throw new Exception("Expression size/isEmpty is Invalid")
          }
        case v =>
          info match {
            case "size" => v.asInstanceOf[List[BsonArray]].size
            case "isEmpty" => false
            case _ => throw new Exception("Expression size/isEmpty is Invalid")
          }
      }
    case Some(condition) ~ Some(value) ~ None ~ None ~ None=>
      println("Inside case Some Some None")
      val result = arrayExtractor(netty, key, value)
      result match {
        case None => List.empty
        case v =>
          condition match {
            case "first" =>
              List(v.asInstanceOf[List[BsonArray]].head)
            case "last" =>
              List(v.asInstanceOf[List[BsonArray]].last)
            case "all" =>
              v.asInstanceOf[List[BsonArray]]
          }
      }
    case Some(condition) ~ Some(value) ~ None ~ Some(info) ~ None=>
      println("Inside case Some Some None")
      arrayExtractor(netty, key, value) match {
        case None =>
          info match {
            case "size" => 0
            case "isEmpty" => true
            case _ => throw new Exception("Expression size/isEmpty is Invalid")
          }
        case v =>
          condition match {
            case "first" =>
              info match {
                case "size" => List(v.asInstanceOf[List[BsonArray]].head.size())
                case "isEmpty" => v.asInstanceOf[List[BsonArray]].head.size() < 0
                case _ => throw new Exception("Expression size/isEmpty is Invalid")
              }
            case "last" =>
              info match {
                case "size" => List(v.asInstanceOf[List[BsonArray]].last.size())
                case "isEmpty" => v.asInstanceOf[List[BsonArray]].last.size() < 0
                case _ => throw new Exception("Expression size/isEmpty is Invalid")
              }
            case "all" =>
              info match {
                case "size" => for(elem <- v.asInstanceOf[List[BsonArray]]) yield elem.size()
                case "isEmpty" => v.asInstanceOf[List[BsonArray]].size < 0
                case _ => throw new Exception("Expression size/isEmpty is Invalid")
              }
          }
      }
    case Some(condition) ~ None ~ Some(fltr) ~ None ~ None=>
      println("Inside case Some None Some None None")
      val result: Iterable[Any] = netty.extract(netty.getByteBuf, key, "all")
      println(s"Some ~ None ~ Some -> result: $result")
      val finalResult =
        if(result.isEmpty){
          result
        } else {
          fltr match {
            case "<" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) < x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) < x)
              }
            case ">" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) > x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) > x)
              }
            case "\\<=" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) <= x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) <= x)
              }
            case "\\>=" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) >= x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) >= x)
              }
            case "==" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) == x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) == x)
              }
            case "!=" ~ (x: Double) =>
              if (key.isEmpty) {
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) != x)
              } else {
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) != x)
              }
            case _ => throw new Exception("Last Expression is Invalid")
          }
        }
          condition match {
            case "first" if finalResult.nonEmpty => finalResult.head
            case "last" if finalResult.nonEmpty => finalResult.last
            case "all" => finalResult
            case "first" | "last" if finalResult.isEmpty => finalResult
            case _ =>  throw new Exception("First Expression is Invalid ")
          }
    case Some(condition) ~ None ~ Some(fltr) ~ Some(info) ~ None=>
      println("Inside case Some None Some Some")
      val result: Iterable[Any] = netty.extract(netty.getByteBuf, key, "all")
      println(s"Some ~ None ~ Some ~ Some -> result: $result")
      val midResult: Iterable[Any] =
        fltr match {
          case "<" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) < x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) < x)
              case _ => result
            }
          case ">" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) > x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) > x)
              case _ => result
            }
          case "\\<=" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) <= x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) <= x)
              case _ => result
            }
          case "\\>=" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) >= x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) >= x)
              case _ => result
            }
          case "==" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) == x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) == x)
              case _ => result
            }
          case "!=" ~ (x: Double) =>
            key.isEmpty match {
              case true if result.nonEmpty =>
                arr.clear()
                result.head.asInstanceOf[List[BsonArray]].head.forEach(elem => arr.append(elem))
                arr.toList.filter(elem => anyToDouble(elem) != x)
              case false if result.nonEmpty =>
                result.head.asInstanceOf[List[Any]].filter(elem => anyToDouble(elem) != x)
              case _ => result
            }
          case _ => throw new Exception("Last Expression is Invalid")
        }
      val finalResult =
        condition match {
          case "first" if midResult.nonEmpty => midResult.head
          case "last" if midResult.nonEmpty => midResult.last
          case "all" => midResult
          case "first"|"last" if midResult.isEmpty => midResult
          case _ => throw new Exception("First Expression is Invalid")
        }
      info match {
        case "size" if finalResult.isInstanceOf[List[Any]]=> finalResult.asInstanceOf[List[Any]].size
        case "size" => 1
        case "isEmpty" if finalResult.isInstanceOf[List[Any]]=>
          finalResult.asInstanceOf[List[Any]].size match {
            case 0 => true
            case _ => false
          }
        case "isEmpty" => false
        case _ => throw new Exception("Expression size/isEmpty is Invalid")
      }
    case None ~ None ~ None ~ None ~ Some(exts) =>
      if(key.isEmpty){
        throw new Exception("Expressions in/Nin aren't available with Empty Key")
      } else {
        exts match {
          case "in" =>
            val result = netty.extract(netty.getByteBuf, key, "first")
            result match {
              case Some(_) => true
              case None => false
            }
          case "Nin" =>
            val result = netty.extract(netty.getByteBuf, key, "first")
            result match {
              case Some(_) => false
              case None => true
            }
          case _ => throw new Exception("Expression is Invalid")
        }
      }
    case Some(condition) ~ None ~ None ~ None ~ None=>
      println("Inside case Some None None None")
      val result = netty.extract(netty.getByteBuf, key, condition)
      condition match {
        case "first" =>
          if(key.isEmpty){
            result.map(v => List(v.asInstanceOf[List[BsonArray]].head.getValue(0))).getOrElse(List.empty)
          } else {
            result.map(v => List(v.asInstanceOf[List[Any]].head)).getOrElse(List.empty)
          }
        case "last" =>
          if(key.isEmpty){
            result.map(v => {
              val list: List[BsonArray] = v.asInstanceOf[List[BsonArray]]
              List(list.head.getValue(list.head.size - 1))
            }).getOrElse(List.empty)
          } else {
            result.map(v => List(v.asInstanceOf[List[Any]].last)).getOrElse(List.empty)
          }
        case "all" =>
          result.getOrElse(List.empty)
        case _ => throw new Exception("First Expression is Invalid")
      }
    case Some(condition) ~ None ~ None ~ Some(info) ~ None=>
      println("Inside case Some None None Some")
      val result = netty.extract(netty.getByteBuf, key, condition)
      condition match {
        case "first" | "last" =>
          result match {
            case Some(_) =>
              info match {
                case "size" => 1
                case "isEmpty" => false
                case _ => throw new Exception("Expression size/isEmpty is Invalid")
              }
            case None =>
              info match {
                case "size" => 0
                case "isEmpty" => true
                case _ => throw new Exception("Expression size/isEmpty is Invalid")
              }
          }
        case "all" =>
          if(key.nonEmpty) {
            result match {
              case Some(v) =>
                info match {
                  case "size" => v.asInstanceOf[List[Any]].size
                  case "isEmpty" => false
                  case _ => throw new Exception("Expression size/isEmpty is Invalid")
                }
              case None =>
                info match {
                  case "size" => 0
                  case "isEmpty" => true
                  case _ => throw new Exception("Expression size/isEmpty is Invalid")
                }
            }
          } else {
            result match {
              case Some(v) =>
                info match {
                  case "size" => v.asInstanceOf[List[BsonArray]].head.size()
                  case "isEmpty" =>
                    v.asInstanceOf[List[BsonArray]].head.size() match {
                      case 0 => true
                      case _ => false
                    }
                  case _ => throw new Exception("Expression size/isEmpty is Invalid")
                }
              case None =>
                info match {
                  case "size" => 0
                  case "isEmpty" => true
                  case _ => throw new Exception("Expression size/isEmpty is Invalid")
                }
            }
          }
        case _ => throw new Exception("First Expression is Invalid")
      }
    case _ => throw new Exception("Global Expression is Invalid")   // When the expressions starts the wrong way
  }

  private def arrayExtractor(netty: NettyBson, key: String, value: Any): Any = {
    arr.clear()
    value match {
      case _ ~ (a: Int) ~ _ ~ "end" ~ _ => // "[# .. end]"
        netty.extract(netty.getByteBuf, key, "limit", Option(a), None).getOrElse(None)
      case _ ~ (a: Int) ~ ("to" | "To") ~ (b: Int) ~ _ => // "[# to #]"
        println("Inside case TO")
        netty.extract(netty.getByteBuf, key, "limit", Option(a), Option(b)).getOrElse(None)
      case _ ~ (a: Int) ~ ("until" | "Until") ~ (b: Int) ~ _ => // "[# until #]"
        netty.extract(netty.getByteBuf, key, "limit", Option(a), Option(b - 1)).getOrElse(None)
      case _ => throw new Exception("Array Expression is Invalid")
    }
  }

  //val < = (xs: List[Any], x: Int) => xs.filter(elem => elem.asInstanceOf[Int] < x)

  /*def getListOfElements(netty: NettyBson, key: String): List[Any] = {
    val result = netty.extract(netty.getByteBuf, key, "all").get.asInstanceOf[List[Any]]
    println(s"result -> $result")
    result
  }*/

   private def anyToDouble(elem: Any): Double = {
    elem match {
      case number: Double => number
      case number: Float => number
      case number: Int => number
      case number: Long => number
      case _ => 0.0
    }
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  def program: Parser[Program] =
    (arraySelectStatement
      ||| sizeOfArrayStatement
      ||| exists2
      ||| arrEx2
      ||| grammar2
      ||| sizeOfSelected) ^^ { s => new Program(List(s))}

  private def grammar2: Parser[Grammar] = ("first" | "last" | "all") ^^ {
    g => Grammar(g)
  }

  private def exists2: Parser[Exists] = ("in" | "Nin") ^^ { d => Exists(d)}

  private def arrEx2: Parser[ArrExpr] = "[" ~> (number ^^ {_.toInt}) ~ ("to" | "To" | "until" | "Until") ~ ((number ^^ {_.toInt}) | "end") <~ "]" ^^ {
    case l ~ m ~ r => ArrExpr(l, m, r)
  }

  private def scndGrammar2: Parser[ScndGrammar] = ("size" | "isEmpty") ^^ {
    g => ScndGrammar(g)
  }

  private def arraySelectStatement: Parser[ArraySelectStatement] = grammar2 ~ arrEx2 ^^ {
    case e ~ a => ArraySelectStatement(e, a)
  }

  private def sizeOfArrayStatement: Parser[SizeOfArrayStatement] = arraySelectStatement ~ scndGrammar2 ^^ {
    case a ~ s => SizeOfArrayStatement(a.grammar, a.arrEx, s)
  }

  private def sizeOfSelected: Parser[SizeOfSelected] = grammar2 ~ scndGrammar2 ^^ {
    case g ~ s => SizeOfSelected(g, s)
  }

}

object TestTinyLanguage extends TinyLanguage {
  def main(args: Array[String]) = {
    val br4: BsonArray = new BsonArray().add("Insecticida")
    val br1: BsonArray = new BsonArray().add("Tarantula").add("Aracnídius").add(br4)
    val obj1: BsonObject = new BsonObject().put("José", br1)

    val br2: BsonArray = new BsonArray().add("Spider")
    val obj2: BsonObject = new BsonObject().put("José", br2)//.put("Alves", false)

    val br3: BsonArray = new BsonArray().add("Fly")
    val obj3: BsonObject = new BsonObject().put("José", br3)//.put("Alves", "we dem boys".getBytes)


    //val arr2: BsonArray = new BsonArray().add("Jesus").add(obj3)
    //val obj4: BsonObject = new BsonObject().put("José", arr2)

    val arr: BsonArray = new BsonArray().add(obj1).add(obj2).add(obj3).add(br4)
    val bsonEvent: BsonObject = new BsonObject().put("StartUp", arr)

    val arrTest: BsonArray = new BsonArray().add(2.2).add(2.4).add(2.6)

    val netty: NettyBson = new NettyBson(vertxBuff = Option(bsonEvent.encode()))
    val key: String = "José"

    val language: String = "all isEmpty"
    val parser = new TinyLanguage
    parser.parseAll(parser.program, language) match {
      case parser.Success(r, _) =>
        val interpreter = new Interpreter(netty, key, r.asInstanceOf[Program])
        try {
          println("SUCCESS: " + interpreter.run())
        } catch {
          case e: RuntimeException => println("Error inside run() " + e.getMessage)
        }
      case parser.Error(msg, _) => println("Error parsing: " + msg)
      case parser.Failure(msg, _) => println("Failure parsing: " + msg)
    }

    println("Life goes on :D")

  }
}
