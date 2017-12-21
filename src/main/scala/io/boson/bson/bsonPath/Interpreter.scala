package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter(boson: BosonImpl, program: Program) {

  def run(): bsonValue.BsValue = {
    start(program.statement)
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        //        case ArraySelectStatement(grammar, arrEx) => // "(all|first|last) [# .. #]"
        //          bsonValue.BsObject.toBson(executeArraySelectStatement(grammar, arrEx))
        case KeyWithGrammar(key, grammar, secondKey) => //key.grammar.option(secondKey)
          if (secondKey.isDefined) executeSelectWithTwoKeys(key, grammar.selectType, secondKey.get) //key.grammar.secondKey
          else executeSelect(key, grammar.selectType) //key.grammar
        case KeyWithArrExpr(key, arrEx, secondKey) => //key.[#..#].option(secondKey)
          secondKey.isDefined match {
            case true if arrEx.midArg.isDefined && arrEx.rightArg.isDefined =>  //key.[#..#].secondKey
              executeArraySelectWithTwoKeys(key,arrEx.leftArg,arrEx.midArg.get,arrEx.rightArg.get,secondKey.get)
            case true =>   //key.[#].secondKey
              executePosSelect(key,arrEx.leftArg,secondKey)
            case false if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#]
              executeArraySelect(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get) match {
                case Seq() => bsonValue.BsObject.toBson(Seq.empty)
                case v =>
                  bsonValue.BsObject.toBson {
                    for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
                  }
              }
            case false =>  //key.[#]
              executePosSelect(key,arrEx.leftArg,secondKey)
          }
        case ArrExpr(left, mid, right) => // "[# .. #]"
          if(mid.isDefined && right.isDefined) {
            executeArraySelect("", left, mid.get, right.get) match {
              case Seq() => bsonValue.BsObject.toBson(Seq.empty)
              case v =>
                bsonValue.BsObject.toBson {
                  for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
                }
            }
          } else {  //  [#]
            executePosSelect("",left,None)
          }
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect("", selectType)
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  //  private def executeSizeOfArrayStatement(grammar: Grammar, arrEx: ArrExpr, scndGrammar: ScndGrammar): bsonValue.BsValue = {
  //    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
  //    val result =
  //      midResult match {
  //        case Seq() => Seq.empty
  //        case value =>
  //          if (key.isEmpty) {
  //            grammar.selectType match {
  //              case "first" =>
  //                Seq(value.head.asInstanceOf[Array[Any]].head)
  //              case "last" =>
  //                Seq(value.head.asInstanceOf[Array[Any]].last)
  //              case "all" =>
  //                Seq(value.head.asInstanceOf[Array[Any]])
  //            }
  //          } else {
  //            grammar.selectType match {
  //              case "first" => Seq(value.asInstanceOf[Seq[Array[Any]]].head)
  //              case "last" =>
  //                Seq(value.asInstanceOf[Seq[Array[Any]]].last)
  //              case "all" => value.asInstanceOf[Seq[Array[Any]]]
  //            }
  //          }
  //      }
  //    if (result.isEmpty) {
  //      scndGrammar.selectType match {
  //        case "size" => bsonValue.BsObject.toBson(0)
  //        case "isEmpty" => bsonValue.BsObject.toBson(true)
  //      }
  //    } else {
  //      scndGrammar.selectType match {
  //        case "size" =>
  //          if (result.size == 1 && key.isEmpty) {
  //            result.head match {
  //              case i: Array[Any] =>
  //                bsonValue.BsObject.toBson(i.length)
  //              case _ =>
  //                bsonValue.BsObject.toBson(1)
  //            }
  //          } else { //("all")
  //            bsonValue.BsObject.toBson(result.head.asInstanceOf[Array[Any]].length)
  //          }
  //        case "isEmpty" => bsonValue.BsObject.toBson(false)
  //      }
  //    }
  //  }

  //  private def executeArraySelectStatement(grammar: Grammar, arrEx: ArrExpr): Seq[Any] = {
  //    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
  //    midResult match {
  //      case Seq() => Seq.empty
  //      case value =>
  //        if (key.isEmpty) {
  //          grammar.selectType match {
  //            case "first" =>
  //              Seq(value.asInstanceOf[Seq[Array[Any]]].head.head)
  //            case "last" =>
  //              Seq(value.asInstanceOf[Seq[Array[Any]]].head.last)
  //            case "all" =>
  //              value.asInstanceOf[Seq[Array[Any]]].head
  //          }
  //        } else {
  //          grammar.selectType match {
  //            case "first" =>
  //              value.asInstanceOf[Seq[Array[Any]]].size match {
  //                case 1 =>
  //                  Seq(value.asInstanceOf[Seq[Array[Any]]].head.head)
  //                case _ =>
  //                  Seq(value.asInstanceOf[Seq[Array[Any]]].head.toList)
  //              }
  //            case "last" =>
  //              value.asInstanceOf[Seq[Array[Any]]].size match {
  //                case 1 =>
  //                  Seq(value.asInstanceOf[Seq[Array[Any]]].head.last)
  //                case _ =>
  //                  Seq(value.asInstanceOf[Seq[Array[Any]]].last.toList)
  //              }
  //            case "all" =>
  //              for(elem <- value.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
  //          }
  //        }
  //    }
  //  } //  (all|first|last) [#..#]

  private def executePosSelect(key: String, left: Int, secondKey: Option[String]): bsonValue.BsValue = {
    if(secondKey.isDefined) {
      //println(s"executePosSelect -> without secondKey -> midresult: ${midResult.get.asInstanceOf[Seq[Array[Any]]].head.toSeq}")
      bsonValue.BsObject.toBson("Not Done Yet")
    } else {
      val result: Seq[Any] =
      boson.extract(boson.getByteBuf, key, "onePos", Some(left), None) map { v =>
        v.asInstanceOf[Seq[Array[Any]]]
      } getOrElse Seq.empty
      result match {
        case Seq() => bsonValue.BsObject.toBson(Seq.empty)
        case v =>
          bsonValue.BsObject.toBson {
            for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
          }
      }
    }
  }

  private def executeArraySelect(key: String, left: Int, mid: String, right: Any): Seq[Any] = {
    (left, mid, right) match {
      case (a, ("until" | "Until"), "end") =>
        val midResult = boson.extract(boson.getByteBuf, key, "limit", Some(a), None)
        midResult.map(v => {
          for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
            elem.take(elem.length - 1)
          }
        }).getOrElse(Seq.empty)
      case (a, _, "end") => // "[# .. end]"
        val midResult = boson.extract(boson.getByteBuf, key, "limit", Some(a), None)
        midResult.map { v =>
          v.asInstanceOf[Seq[Array[Any]]]
        }.getOrElse(Seq.empty)
      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case ("to" | "To") =>
            boson.extract(
              boson.getByteBuf, key, "limit", Some(a), Some(b.asInstanceOf[Int])
            ).map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            }.getOrElse(Seq.empty)
          case ("until" | "Until") =>
            boson.extract(
              boson.getByteBuf, key, "limit", Some(a), Some(b.asInstanceOf[Int] - 1)
            ).map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            }.getOrElse(Seq.empty)
        }
    }
  }

  private def executeArraySelectWithTwoKeys(key: String, left: Int,
                                             mid: String, right: Any,
                                             secondKey: String): bsonValue.BsValue = {
    Seq(key,secondKey)
    boson.extract(
      boson.getByteBuf, key, "limit", Some(left), Some(right.asInstanceOf[Int])
    )
    bsonValue.BsObject.toBson("Not Done Yet")
  }

  // "[# .. #]"

  //  private def executeSizeOfArraySelect(arrEx: ArrExpr, scndGrammar: ScndGrammar): bsonValue.BsValue = {
  //    val midResult = executeArraySelect(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)
  //    midResult match {
  //      case Seq() =>
  //        scndGrammar.selectType match {
  //          case "size" => bsonValue.BsObject.toBson(0)
  //          case "isEmpty" => bsonValue.BsObject.toBson(true)
  //        }
  //      case v =>
  //        scndGrammar.selectType match {
  //          case "size" =>
  //            val arrList = for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.length
  //            bsonValue.BsObject.toBson(arrList)
  //          case "isEmpty" => bsonValue.BsObject.toBson(false)
  //        }
  //    }
  //  }

  private def executeSelect(key: String, selectType: String): bsonValue.BsValue = {
    val result = boson.extract(boson.getByteBuf, key, selectType)
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Seq(v.asInstanceOf[Seq[Array[Any]]].head.head)).getOrElse(Seq.empty))
        } else {
          result.map { elem =>
            elem.asInstanceOf[Seq[Any]].head match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson(p.asInstanceOf[Array[Any]].toList)
              case p =>
                bsonValue.BsObject.toBson(Seq(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Seq.empty[Any])
        }
      case "last" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Seq(v.asInstanceOf[Seq[Array[Any]]].head.last)).getOrElse(Seq.empty))
        } else {
          result.map { elem =>
            elem.asInstanceOf[Seq[Any]].last match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson(p.asInstanceOf[Array[Any]].toList)
              case p =>
                bsonValue.BsObject.toBson(Seq(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Seq.empty[Any])
        }
      case "all" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => v.asInstanceOf[Seq[Array[Any]]].head.toList).getOrElse(Seq.empty))
        } else {
          bsonValue.BsObject.toBson(result.map { v =>
            v.asInstanceOf[Seq[Any]].map {
              case array: Array[Any] => array.toList
              case elem => elem
            }
          }.getOrElse(Seq.empty))
        }
    }
  }

  private def executeSelectWithTwoKeys(key: String, selectType: String, secondKey: String): bsonValue.BsValue = {
    bsonValue.BsObject.toBson("Not Done Yet")
  }

  //  (all|first|last)

  //  private def executeSizeOfSelected(grammar: Grammar, scndGrammar: ScndGrammar): bsonValue.BsValue = {
  //    val result = boson.extract(boson.getByteBuf, key, grammar.selectType)
  //    grammar.selectType match {
  //      case "first" | "last" =>
  //        result match {
  //          case Some(_) =>
  //            scndGrammar.selectType match {
  //              case "size" => bsonValue.BsObject.toBson(1)
  //              case "isEmpty" => bsonValue.BsObject.toBson(false)
  //            }
  //          case None =>
  //            scndGrammar.selectType match {
  //              case "size" => bsonValue.BsObject.toBson(0)
  //              case "isEmpty" => bsonValue.BsObject.toBson(true)
  //            }
  //        }
  //      case "all" =>
  //        if (key.nonEmpty) {
  //          result match {
  //            case Some(v) =>
  //              scndGrammar.selectType match {
  //                case "size" => bsonValue.BsObject.toBson(v.asInstanceOf[Seq[Any]].size) //TODO:review this case
  //                case "isEmpty" => bsonValue.BsObject.toBson(false)
  //              }
  //            case None =>
  //              scndGrammar.selectType match {
  //                case "size" => bsonValue.BsObject.toBson(0)
  //                case "isEmpty" => bsonValue.BsObject.toBson(true)
  //              }
  //          }
  //        } else {
  //          result match {
  //            case Some(v) =>
  //              scndGrammar.selectType match {
  //                case "size" => bsonValue.BsObject.toBson(v.asInstanceOf[Seq[Array[Any]]].head.length)
  //                case "isEmpty" =>
  //                  v.asInstanceOf[Seq[Array[Any]]].head.length match {
  //                    case 0 => bsonValue.BsObject.toBson(true)
  //                    case _ => bsonValue.BsObject.toBson(false)
  //                  }
  //              }
  //            case None =>
  //              scndGrammar.selectType match {
  //                case "size" => bsonValue.BsObject.toBson(0)
  //                case "isEmpty" => bsonValue.BsObject.toBson(true)
  //              }
  //          }
  //        }
  //    }
  //  }

  //  private def executeExists(term: String): Boolean = {
  //    if (key.isEmpty) {
  //      throw new RuntimeException("Expressions in/Nin aren't available with Empty Key")
  //    } else {
  //      term match {
  //        case "in" =>
  //          val result = boson.extract(boson.getByteBuf, key, "first")
  //          result match {
  //            case Some(_) => true
  //            case None => false
  //          }
  //        case "Nin" =>
  //          val result = boson.extract(boson.getByteBuf, key, "first")
  //          result match {
  //            case Some(_) => false
  //            case None => true
  //          }
  //      }
  //    }
  //  } // in|Nin

}
