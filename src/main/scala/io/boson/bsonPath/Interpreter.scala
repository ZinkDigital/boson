package io.boson.bsonPath

import io.boson.nettybson.NettyBson
import io.boson.bson.BsonArray

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter(netty: NettyBson, key: String, program: Program) {

  def run(): Any = {
    start(program.statement)
  }

  private def start(statement: List[Statement]): Any = {
    if (statement.nonEmpty) {
      statement.head match {
        case ArraySelectStatement(grammar, arrEx) =>    // "(all|first|last) [# .. #]"
          executeArraySelectStatement(grammar, arrEx)
        case SizeOfArrayStatement(grammar, arrEx, scndGrammar) =>   // "(all|first|last) [# .. #] (size|isEmpty)"
          executeSizeOfArrayStatement(grammar, arrEx, scndGrammar)
        case Exists(term) =>    // "(in|Nin)"
          executeExists(term)
        case ArrExpr(left: Int, mid: String, right: Any) =>   // "[# .. #]"
          executeArraySelect(left, mid, right) match {
            case None => List()
            case v => v
          }
        case Grammar(selectType) =>   // "(all|first|last)"
          executeSelect(selectType)
        case SizeOfSelected(grammar, scndGrammar) =>    // "(all|first|last) (size|isEmpty)"
          executeSizeOfSelected(grammar, scndGrammar)
        case SizeOfArray(arrEx, scndGrammar) =>   // "[# .. #] (size|isEmpty)"
          executeSizeOfArraySelect(arrEx, scndGrammar)
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def executeSizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar): Any = {
    val midResult: List[Any] = executeArraySelectStatement(grammar, arrEx)
    if (midResult.isEmpty) {
      scndGrammar.selectType match {
        case "size" => 0
        case "isEmpty" => true
      }
    } else {
      scndGrammar.selectType match {
        case "size" => for (elem <- midResult.asInstanceOf[List[BsonArray]]) yield elem.size()
        case "isEmpty" => false
      }
    }
  }

  private def executeArraySelectStatement(grammar: Grammar, arrEx: ArrExpr): List[Any] = {
    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
    midResult match {
      case None => List()
      case value =>
        grammar.selectType match {
          case "first" => List(value.asInstanceOf[List[BsonArray]].head)
          case "last" => List(value.asInstanceOf[List[BsonArray]].last)
          case "all" => value.asInstanceOf[List[BsonArray]]
        }
    }
  }

  private def executeArraySelect(left: Int, mid: String, right: Any): Any = {
    (left, mid, right) match {
      case (a, _, "end") => // "[# .. end]"
        netty.extract(netty.getByteBuf, key, "limit", Option(a), None).getOrElse(None)
      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case ("to" | "To") =>
            netty.extract(
              netty.getByteBuf, key, "limit", Option(a), Option(b.asInstanceOf[Int])
            ).getOrElse(None)
          case ("until" | "Until") =>
            netty.extract(
              netty.getByteBuf, key, "limit", Option(a), Option(b.asInstanceOf[Int] - 1)
            ).getOrElse(None)
        }
    }
  }

  private def executeSizeOfArraySelect(arrEx: ArrExpr, scndGrammar: ScndGrammar): Any = {
    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
    midResult match {
      case None =>
        scndGrammar.selectType match {
          case "size" => 0
          case "isEmpty" => true
        }
      case v =>
        scndGrammar.selectType match {
          case "size" => v.asInstanceOf[List[BsonArray]].size
          case "isEmpty" => false
        }
    }
  }

  private def executeSelect(selectType: String): Any = {
    val result = netty.extract(netty.getByteBuf, key, selectType)
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          result.map(v => List(v.asInstanceOf[List[BsonArray]].head.getValue(0))).getOrElse(List.empty)
        } else {
          result.map(v => List(v.asInstanceOf[List[Any]].head)).getOrElse(List.empty)
        }
      case "last" =>
        if (key.isEmpty) {
          result.map(v => {
            val list: List[BsonArray] = v.asInstanceOf[List[BsonArray]]
            List(list.head.getValue(list.head.size - 1))
          }).getOrElse(List.empty)
        } else {
          result.map(v => List(v.asInstanceOf[List[Any]].last)).getOrElse(List.empty)
        }
      case "all" =>
        result.getOrElse(List.empty)
    }
  }

  private def executeSizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar): Any = {
    val result = netty.extract(netty.getByteBuf, key, grammar.selectType)
    grammar.selectType match {
      case "first" | "last" =>
        result match {
          case Some(_) =>
            scndGrammar.selectType match {
              case "size" => 1
              case "isEmpty" => false
            }
          case None =>
            scndGrammar.selectType match {
              case "size" => 0
              case "isEmpty" => true
            }
        }
      case "all" =>
        if (key.nonEmpty) {
          result match {
            case Some(v) =>
              scndGrammar.selectType match {
                case "size" => v.asInstanceOf[List[Any]].size
                case "isEmpty" => false
              }
            case None =>
              scndGrammar.selectType match {
                case "size" => 0
                case "isEmpty" => true
              }
          }
        } else {
          result match {
            case Some(v) =>
              scndGrammar.selectType match {
                case "size" => v.asInstanceOf[List[BsonArray]].head.size()
                case "isEmpty" =>
                  v.asInstanceOf[List[BsonArray]].head.size() match {
                    case 0 => true
                    case _ => false
                  }
              }
            case None =>
              scndGrammar.selectType match {
                case "size" => 0
                case "isEmpty" => true
              }
          }
        }
    }
  }

  private def executeExists(term: String): Boolean = {
    if (key.isEmpty) {
      throw new RuntimeException("Expressions in/Nin aren't available with Empty Key")
    } else {
      term match {
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
      }
    }
  }

}
