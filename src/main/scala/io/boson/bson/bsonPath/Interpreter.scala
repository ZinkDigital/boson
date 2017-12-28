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
        //        case ArraySelectStatement(grammar, arrEx) => // "(all|first|last) [# .. #]"
        //          bsonValue.BsObject.toBson(executeArraySelectStatement(grammar, arrEx))
        case KeyWithGrammar(key, grammar) =>
          executeSelect(key, grammar.selectType) //key.grammar
        case KeyWithArrExpr(key, arrEx, secondKey) =>
          println("KeyWithArrExpr case")
          secondKey.isDefined match {
            case true if arrEx.midArg.isDefined && arrEx.rightArg.isDefined =>  //key.[#..#].secondKey
              println("inside case key.[#..#].secondKey")
              executeArraySelectWithTwoKeys(key,arrEx.leftArg,arrEx.midArg.get,arrEx.rightArg.get,secondKey.get)
            case true =>   //key.[#].secondKey
              println("inside case key.[#].secondKey")
              executePosSelect(key,arrEx.leftArg,secondKey)
            case false if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#]
              println("inside case key.[#..#]")
              executeArraySelect(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get) match {
                case Seq() => bsonValue.BsObject.toBson(Seq.empty)
                case v =>
                  bsonValue.BsObject.toBson {

                    for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield Compose.composer(elem)
                  }
              }
            case false =>  //key.[#]
              executePosSelect(key,arrEx.leftArg,secondKey)
          }
        case ArrExpr(left, mid, right,secondKey) =>
          secondKey.isDefined match {
            case true if mid.isDefined && right.isDefined => //[#..#].2ndKey
              println("interpreter case [#..#].2ndKey")
              executeArraySelectWithTwoKeys("",left,mid.get,right.get,secondKey.get)
            case true => //[#].2ndKey
              executePosSelect("",left,secondKey)
            case false if mid.isDefined && right.isDefined => //[#..#]
              executeArraySelect("", left, mid.get, right.get) match {
                case Seq() => bsonValue.BsObject.toBson(Seq.empty)
                case v =>
                  bsonValue.BsObject.toBson {
                    //for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
                    Compose.composer(v.head)
                  }
              }
            case false => //[#]
              println("case [#]")
              executePosSelect("",left,None)
          }
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect("", selectType)
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def executePosSelect(key: String, left: Int, secondKey: Option[String]): bsonValue.BsValue = {
    println("executePosSelect")
    val keyList =
    if(secondKey.isDefined) {
      List((key,"onePos"),(secondKey.get,"onePos2nd"))
    } else {
      println("no second key")
      List((key,"onePos"))
    }
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, keyList, Some(left), None) map { v =>
        println(s"v: $v")
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

  private def executeArraySelect(key: String, left: Int, mid: String, right: Any): Seq[Array[Any]] = {
    (left, mid, right) match {
      case (a, ("until" | "Until"), "end") =>
        val midResult = boson.extract(boson.getByteBuf, List((key,"limit")), Some(a), None)
        midResult.map(v => {
          for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
            elem.take(elem.length - 1)
          }
        }).getOrElse(Seq.empty)
      case (a, _, "end") => // "[# .. end]"
        val midResult = boson.extract(boson.getByteBuf, List((key,"limit")), Some(a), None)
        midResult.map { v =>
          v.asInstanceOf[Seq[Array[Any]]]
        }.getOrElse(Seq.empty)
      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case ("to" | "To") =>
            boson.extract(
              boson.getByteBuf, List((key,"limit")), Some(a), Some(b.asInstanceOf[Int])
            ).map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            }.getOrElse(Seq.empty)
          case ("until" | "Until") =>
            boson.extract(
              boson.getByteBuf, List((key,"limit")), Some(a), Some(b.asInstanceOf[Int] - 1)
            ).map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            }.getOrElse(Seq.empty)
        }
    }
  }

  private def executeArraySelectWithTwoKeys(key: String, left: Int,
                                             mid: String, right: Any,
                                             secondKey: String): bsonValue.BsValue = {
    println("executeArraySelectWithTwoKeys")
    val result =
    (left,mid,right) match {
      case (a, ("until" | "Until"), "end") =>
        boson.extract(
          boson.getByteBuf, List((key,"limit"),(secondKey,"all")), Some(a), None
        ) map {v => {
          for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
            elem.take(elem.length - 1)
          }
        }} getOrElse Seq.empty[Array[Any]]
      case (a, _, "end") =>
        boson.extract(
          boson.getByteBuf, List((key,"limit"),(secondKey,"all")), Some(a), None
        ) map { v =>
          v.asInstanceOf[Seq[Array[Any]]]
        } getOrElse Seq.empty[Array[Any]]
      case (a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case ("to" | "To") =>
            boson.extract(
              boson.getByteBuf, List((key,"limit"),(secondKey,"all")), Some(a), Some(b.asInstanceOf[Int])
            ) map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            } getOrElse Seq.empty[Array[Any]]
          case ("until" | "Until") =>
            boson.extract(
              boson.getByteBuf, List((key,"limit"),(secondKey,"all")), Some(a), Some(b.asInstanceOf[Int]-1)
            ) map { v =>
              v.asInstanceOf[Seq[Array[Any]]]
            } getOrElse Seq.empty[Array[Any]]
        }
    }
    result match {
      case Vector() =>bsonValue.BsObject.toBson(Seq.empty)
      case v =>
        println(s"almost ended: $v")
        println(s"his head: ${v.head}")
        bsonValue.BsObject.toBson {
//          for {
//            elem <- v.asInstanceOf[Seq[Array[Any]]]
//          } yield elem.toList
          //transformer(v)
          Compose.composer(v.head)
        }
    }
  }

  private def executeSelect(key: String, selectType: String): bsonValue.BsValue = {
    println(s"executeSelect(key.grammar) with key: $key")
    val result = boson.extract(boson.getByteBuf, List((key,selectType)))
    println(s"result of grammar with key: $key, selectType: $selectType -> $result")
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map{v =>
            Seq(Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).head)}.getOrElse(Seq.empty[Any]))
        } else {
          result.map { v =>
            //println(s"result of using first with key: v${composer(v.asInstanceOf[Seq[Any]].head.asInstanceOf[Array[Any]])}")
            v.asInstanceOf[Seq[Any]].head match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson(Compose.composer(p.asInstanceOf[Array[Any]]))
              case p =>
                bsonValue.BsObject.toBson(Seq(p)) //  TODO:Check if composer is required
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
                bsonValue.BsObject.toBson(p.asInstanceOf[Array[Any]].toList)
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
              case array: Array[Any] => array.toList
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
      case x :: Nil  if x.isInstanceOf[Array[Any]] =>
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
    for(elem <- value) {
      elem match {
        case e: Array[Any] =>
          println(s"elem is arr: $e")
          help.append(composer(e))
        case e: List[Any] =>
          println(s"elem is list: $e")
          for (elem <- e) {
            elem match {
              case v: Array[Any] =>
                println(s" is arr: $v")
                help.append(composer(v))
              case v =>
                println(s" is elem: $v")
                help.append(v)
            }
          }
        case e =>
          println(s"elem is: $e")
          help.append(e)
      }
      println(s"helpSeq: $help")
    }
    help.toList
  }

}
