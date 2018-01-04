package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue

import scala.collection.mutable.ListBuffer

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
        case KeyWithGrammar(key, grammar) =>
          executeSelect(key, grammar.selectType) //key.grammar
        case KeyWithArrExpr(key, arrEx, secondKey) =>
          secondKey.isDefined match {
            case true if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#].secondKey
              executeArraySelectWithTwoKeys(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get, secondKey.get)
            case true => //key.[#].secondKey
              executeArraySelectWithTwoKeys(key,arrEx.leftArg,"to",arrEx.leftArg,secondKey.get)
            case false if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#]
              executeArraySelect(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get)
            case false => //key.[#]
              executeArraySelect(key,arrEx.leftArg,"to", arrEx.leftArg)
          }
        case ArrExpr(left, mid, right, secondKey) =>
          secondKey.isDefined match {
            case true if mid.isDefined && right.isDefined => //[#..#].2ndKey
              executeArraySelectWithTwoKeys("", left, mid.get, right.get, secondKey.get)
            case true => //[#].2ndKey
              executeArraySelectWithTwoKeys("",left, "to", left,secondKey.get)
            case false if mid.isDefined && right.isDefined => //[#..#]
              executeArraySelect("", left, mid.get, right.get)
            case false => //[#]
              executeArraySelect("",left, "to", left)
          }
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect("", selectType)
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

//  private def executePosSelect(key: String, left: Int, secondKey: Option[String]): bsonValue.BsValue = {
//    val keyList =
//      if (secondKey.isDefined) {
//        List((key, "onePos"), (secondKey.get, "onePos2nd"))
//      } else {
//        List((key, "onePos"))
//      }
//    val result: Seq[Any] =
//      boson.extract(boson.getByteBuf, keyList, Some(left), None) map { v =>
//        println(s"after extraction of -> $v")
//        v.asInstanceOf[Seq[Any]]
//      } getOrElse Seq.empty
//    result match {
//      case Seq() => bsonValue.BsObject.toBson(Seq.empty)
//      case v =>
//          bsonValue.BsObject.toBson {
//            for (elem <- v) yield {
//              elem match {
//                case e: Array[Any] => Compose.composer(e)
//                case e => e
//              }
//            }
//          }
//    }
//  }

  private def executeArraySelect(key: String, left: Int, mid: String, right: Any): bsonValue.BsValue = {
    val result =
      (left, mid, right) match {
        case (a, ("until" | "Until"), "end") =>
          val midResult = boson.extract(boson.getByteBuf, List((key, "limit")), Some(a), None)
          midResult.map(v => {
            (for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
              elem.take(elem.length - 1)
            }).filter(p => p.nonEmpty)
          }).getOrElse(Seq.empty)
        case (a, _, "end") => // "[# .. end]"
          val midResult = boson.extract(boson.getByteBuf, List((key, "limit")), Some(a), None)
          midResult.map { v =>
            v.asInstanceOf[Seq[Any]]
          }.getOrElse(Seq.empty)
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr match {
            case ("to" | "To") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit")), Some(a), Some(b.asInstanceOf[Int])
              ).map { v =>
                v.asInstanceOf[Seq[Any]]
              }.getOrElse(Seq.empty)
            case ("until" | "Until") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit")), Some(a), Some(b.asInstanceOf[Int] - 1)
              ).map { v =>
                v.asInstanceOf[Seq[Any]]
              }.getOrElse(Seq.empty)
          }
      }
    result match {
      case Seq() => bsonValue.BsObject.toBson(Seq.empty)
      case v =>
        bsonValue.BsObject.toBson {
          //for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield Compose.composer(elem)
          for (elem <- v) yield {
            elem match {
              case e: Array[Any] => Compose.composer(e)
              case e => e
            }
          }
      }
    }
  }

  private def executeArraySelectWithTwoKeys(key: String, left: Int,
                                            mid: String, right: Any,
                                            secondKey: String): bsonValue.BsValue = {
    val result =
      (left, mid, right) match {
        case (a, ("until" | "Until"), "end") =>
          boson.extract(
            boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), None
          ) map { v => {
            for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
              elem.take(elem.length - 1)
            }
          }
          } getOrElse Seq.empty[Array[Any]]
        case (a, _, "end") =>
          boson.extract(
            boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), None
          ) map { v =>
            v.asInstanceOf[Seq[Array[Any]]]
          } getOrElse Seq.empty[Array[Any]]
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr match {
            case ("to" | "To") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), Some(b.asInstanceOf[Int])
              ) map { v =>
                v.asInstanceOf[Seq[Array[Any]]]
              } getOrElse Seq.empty[Array[Any]]
            case ("until" | "Until") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), Some(b.asInstanceOf[Int] - 1)
              ) map { v =>
                v.asInstanceOf[Seq[Array[Any]]]
              } getOrElse Seq.empty[Array[Any]]
          }
      }
    result match {
      case Seq() => bsonValue.BsObject.toBson(Seq.empty)
      case v =>
        bsonValue.BsObject.toBson {
//          val res =
//            for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield Compose.composer(elem)
//          if (res.size>1) res else res.head //  TODO: rethink this situation, using traverBsonArray several times outputs lists inside lists
          for (elem <- v) yield {
            elem match {
              case e: Array[Any] => Compose.composer(e)
              case e => e
            }
          }
        }
    }
  }

  private def executeSelect(key: String, selectType: String): bsonValue.BsValue = {
    val result = boson.extract(boson.getByteBuf, List((key, selectType)))
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map { v =>
            Seq(Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).head)
          }.getOrElse(Seq.empty[Any]))
        } else {
          result.map { v =>
            v.asInstanceOf[Seq[Any]].head match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson{
                  Compose.composer(p.asInstanceOf[Array[Any]])
                }
              case p =>
                bsonValue.BsObject.toBson(Seq(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Seq.empty[Any])
        }
      case "last" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Seq(Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).last)).getOrElse(Seq.empty))
        } else {
          result.map { elem =>
            elem.asInstanceOf[Seq[Any]].last match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson(Compose.composer(p.asInstanceOf[Array[Any]]))
              case p =>
                bsonValue.BsObject.toBson(Seq(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Seq.empty[Any])
        }
      case "all" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head)).getOrElse(Seq.empty))
        } else {
          bsonValue.BsObject.toBson(result.map { v =>
            v.asInstanceOf[Seq[Any]].map {
              case array: Array[Any] => Compose.composer(array)
              case elem => elem
            }
          }.getOrElse(Seq.empty))
        }
    }
  }

  @Deprecated
  private def transformer(value: Seq[Any]): Seq[Any] = {
    value match {
      case Seq() => Seq.empty
      case x :: Nil if x.isInstanceOf[Seq[Any]] =>
        println("case x :: Nil -> x is list")
        transformer(x.asInstanceOf[Seq[Any]])
      case x :: Nil if x.isInstanceOf[Array[Any]] =>
        println(s"case x :: Nil -> x is arr: ${x.asInstanceOf[Array[Any]].toList}")
        transformer(x.asInstanceOf[Array[Any]].toList)
      //x.asInstanceOf[Array[Any]].toList
      case x :: Nil =>
        println(s"case x :: Nil -> x is element: $x")
        x +: Seq()
      case x :: xs if x.isInstanceOf[Seq[Any]] =>
        println("case x :: xs -> x is list")
        transformer(x.asInstanceOf[Seq[Any]]) +: transformer(xs) +: Seq()
      case x :: xs if x.isInstanceOf[Array[Any]] =>
        println("case x :: xs -> x is arr")
        x.asInstanceOf[Array[Any]].toList +: transformer(xs) +: Seq()
      case x :: xs =>
        println("case x :: xs -> x is element")
        Seq(x) +: transformer(xs) +: Seq()
    }
  }

}

object Compose {

  def composer(value: Array[Any]): Seq[Any] = {
    val help: ListBuffer[Any] = new ListBuffer[Any]
    for (elem <- value) {
      elem match {
        case e: Array[Any] =>
          help.append(composer(e))
        case e: List[Any] =>
          for (elem <- e) {
            elem match {
              case v: Array[Any] =>
                help.append(composer(v))
              case v =>
                help.append(v)
            }
          }
        case e =>
          help.append(e)
      }
    }
    help.toList
  }

}
