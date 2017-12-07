package io.boson.bsonPath

import io.boson.nettyboson.Boson
import io.boson.bson.BsonArray
import io.boson.bsonValue

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter(boson: Boson, key: String, program: Program) {

  def run(): bsonValue.BsValue = {
    start(program.statement)
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case ArraySelectStatement(grammar, arrEx) =>    // "(all|first|last) [# .. #]"
          bsonValue.BsObject.toBson(executeArraySelectStatement(grammar, arrEx))
        case SizeOfArrayStatement(grammar, arrEx, scndGrammar) =>   // "(all|first|last) [# .. #] (size|isEmpty)"
          executeSizeOfArrayStatement(grammar, arrEx, scndGrammar)
        case Exists(term) =>    // "(in|Nin)"
          bsonValue.BsObject.toBson(executeExists(term))
        case ArrExpr(left: Int, mid: String, right: Any) =>   // "[# .. #]"
          executeArraySelect(left, mid, right) match {
            case Seq() => bsonValue.BsObject.toBson(Seq.empty)
            case v =>     bsonValue.BsObject.toBson(v)
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

  private def executeSizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar): bsonValue.BsValue = {
    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
    val result =
    midResult match {
      case Seq() => Seq.empty
      case value =>
        if(key.isEmpty) {
          grammar.selectType match {
            case "first" =>
              Seq(new BsonArray().add(value.asInstanceOf[Seq[BsonArray]].head.getValue(0)))
            case "last" =>
              Seq(new BsonArray().add(value.asInstanceOf[Seq[BsonArray]].head.getValue(value.asInstanceOf[Seq[BsonArray]].head.size()-1)))
            case "all" => value.asInstanceOf[Seq[BsonArray]]
          }
        } else {
          grammar.selectType match {
            case "first" => Seq(value.asInstanceOf[Seq[BsonArray]].head)
            case "last" => Seq(value.asInstanceOf[Seq[BsonArray]].last)
            case "all" => value.asInstanceOf[Seq[BsonArray]]
          }
        }
    }
    if (result.isEmpty) {
      scndGrammar.selectType match {
        case "size" =>  bsonValue.BsObject.toBson(0)
        case "isEmpty" => bsonValue.BsObject.toBson(true)
      }
    } else {
      scndGrammar.selectType match {
        case "size" =>
          if(result.size == 1 && key.isEmpty){
            result.head match {
              case i: BsonArray =>
                bsonValue.BsObject.toBson(i.size())
              case _ =>
                bsonValue.BsObject.toBson(result.size)
            }
          } else {    //("all")
            val list =
              for (elem <- result.asInstanceOf[Seq[BsonArray]]) yield elem.size()
            bsonValue.BsObject.toBson(list)
          }
        case "isEmpty" => bsonValue.BsObject.toBson(false)
      }
    }
  }

  private def executeArraySelectStatement(grammar: Grammar, arrEx: ArrExpr): Seq[Any] = {
    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
    midResult match {
      case Seq() => Seq.empty
      case value =>
        if(key.isEmpty) {
          grammar.selectType match {
            case "first" =>
              Seq(value.asInstanceOf[Seq[BsonArray]].head.getValue(0))
            case "last" =>
              Seq(value.asInstanceOf[Seq[BsonArray]].head.getValue(value.asInstanceOf[Seq[BsonArray]].head.size()-1))
            case "all" => value.asInstanceOf[Seq[BsonArray]]
          }
        } else {
          grammar.selectType match {
            case "first" if value.asInstanceOf[Seq[BsonArray]].size == 1 =>
              Seq(value.asInstanceOf[Seq[BsonArray]].head.getValue(0))
            case "first" => Seq(value.asInstanceOf[Seq[BsonArray]].head)
            case "last" if value.asInstanceOf[Seq[BsonArray]].size == 1 =>
              Seq(value.asInstanceOf[Seq[BsonArray]].head.getValue(value.asInstanceOf[Seq[BsonArray]].head.size()-1))
            case "last" => Seq(value.asInstanceOf[Seq[BsonArray]].last)
            case "all" => value.asInstanceOf[Seq[BsonArray]]
          }
        }
    }
  }

  private def executeArraySelect(left: Int, mid: String, right: Any): Seq[Any] = {
    (left, mid, right) match {
      case (a, ("until" | "Until"), "end") =>
        val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
          midResult.map(v => {
            v.asInstanceOf[Seq[BsonArray]].foreach(elem => elem.remove(elem.size()-1))
            v.asInstanceOf[Seq[BsonArray]]
          }
          ).getOrElse(Seq.empty)
      case (a, _, "end") => // "[# .. end]"
        boson.extract(boson.getByteBuf, key, "limit", Option(a), None).map( v =>
        v.asInstanceOf[Seq[BsonArray]]).getOrElse(Seq.empty)
      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case ("to" | "To") =>
            boson.extract(
              boson.getByteBuf, key, "limit", Option(a), Option(b.asInstanceOf[Int])
            ).map(v =>v.asInstanceOf[Seq[BsonArray]]).getOrElse(Seq.empty)
          case ("until" | "Until") =>
            boson.extract(
              boson.getByteBuf, key, "limit", Option(a), Option(b.asInstanceOf[Int] - 1)
            ).map(v => v.asInstanceOf[Seq[BsonArray]]).getOrElse(Seq.empty)
        }
    }
  }

  private def executeSizeOfArraySelect(arrEx: ArrExpr, scndGrammar: ScndGrammar): bsonValue.BsValue = {
    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
    midResult match {
      case Seq() =>
        scndGrammar.selectType match {
          case "size" => bsonValue.BsObject.toBson(0)
          case "isEmpty" => bsonValue.BsObject.toBson(true)
        }
      case v =>
        scndGrammar.selectType match {
          case "size" =>
            val list =
              for (elem <- v.asInstanceOf[Seq[BsonArray]]) yield elem.size()
            bsonValue.BsObject.toBson(list)
            //bsonValue.BsObject.toBson(v.asInstanceOf[Seq[BsonArray]].size)
          case "isEmpty" => bsonValue.BsObject.toBson(false)
        }
    }
  }

  private def executeSelect(selectType: String): bsonValue.BsValue = {
    val result = boson.extract(boson.getByteBuf, key, selectType)
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Seq(v.asInstanceOf[Seq[BsonArray]].head.getValue(0))).getOrElse(Seq.empty))
        } else {
          bsonValue.BsObject.toBson(result.map(v => Seq(v.asInstanceOf[Seq[Any]].head)).getOrElse(Seq.empty))
        }
      case "last" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => {
            val list: Seq[BsonArray] = v.asInstanceOf[Seq[BsonArray]]
            Seq(list.head.getValue(list.head.size - 1))
          }).getOrElse(Seq.empty))
        } else {
          bsonValue.BsObject.toBson(result.map(v => List(v.asInstanceOf[Seq[Any]].last)).getOrElse(Seq.empty))
        }
      case "all" =>
        bsonValue.BsObject.toBson(result.map(v => v.asInstanceOf[Seq[Any]]).getOrElse(Seq.empty))
    }
  }

  private def executeSizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar): bsonValue.BsValue = {
    val result = boson.extract(boson.getByteBuf, key, grammar.selectType)
    grammar.selectType match {
      case "first" | "last" =>
        result match {
          case Some(_) =>
            scndGrammar.selectType match {
              case "size" => bsonValue.BsObject.toBson(1)
              case "isEmpty" => bsonValue.BsObject.toBson(false)
            }
          case None =>
            scndGrammar.selectType match {
              case "size" => bsonValue.BsObject.toBson(0)
              case "isEmpty" => bsonValue.BsObject.toBson(true)
            }
        }
      case "all" =>
        if (key.nonEmpty) {
          result match {
            case Some(v) =>
              scndGrammar.selectType match {
                case "size" => bsonValue.BsObject.toBson(v.asInstanceOf[Seq[Any]].size)
                case "isEmpty" => bsonValue.BsObject.toBson(false)
              }
            case None =>
              scndGrammar.selectType match {
                case "size" => bsonValue.BsObject.toBson(0)
                case "isEmpty" => bsonValue.BsObject.toBson(true)
              }
          }
        } else {
          result match {
            case Some(v) =>
              scndGrammar.selectType match {
                case "size" => bsonValue.BsObject.toBson(v.asInstanceOf[Seq[BsonArray]].head.size())
                case "isEmpty" =>
                  v.asInstanceOf[Seq[BsonArray]].head.size() match {
                    case 0 => bsonValue.BsObject.toBson(true)
                    case _ => bsonValue.BsObject.toBson(false)
                  }
              }
            case None =>
              scndGrammar.selectType match {
                case "size" => bsonValue.BsObject.toBson(0)
                case "isEmpty" => bsonValue.BsObject.toBson(true)
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
          val result = boson.extract(boson.getByteBuf, key, "first")
          result match {
            case Some(_) => true
            case None => false
          }
        case "Nin" =>
          val result = boson.extract(boson.getByteBuf, key, "first")
          result match {
            case Some(_) => false
            case None => true
          }
      }
    }
  }

}
