package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter[T](boson: BosonImpl, program: Program, f: Option[Function[T,T]] = None) {

  def run(): bsonValue.BsValue = {
    f match {
      case Some(func) => //func /*Inejctor*/
        startInjector(program.statement)
      case None => /*Extractor*/
        start(program.statement)
    }
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
        case HalfName(halfName) =>  //  "*halfname"
          executeSelect("*"+halfName,"all")
        case Everything(key) => //  *
          executeSelect(key,"all")

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
//          } else { //("all")
//            bsonValue.BsObject.toBson(result.head.asInstanceOf[Array[Any]].length)
//          }
//        case "isEmpty" => bsonValue.BsObject.toBson(false)
//      }
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
          } getOrElse Seq.empty[Any]
        case (a, _, "end") =>
          boson.extract(
            boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), None
          ) map { v =>
            v.asInstanceOf[Seq[Any]]
          } getOrElse Seq.empty[Any]
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr match {
            case ("to" | "To") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), Some(b.asInstanceOf[Int])
              ) map { v =>
                v.asInstanceOf[Seq[Any]]
              } getOrElse Seq.empty[Any]
            case ("until" | "Until") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), Some(a), Some(b.asInstanceOf[Int] - 1)
              ) map { v =>
                v.asInstanceOf[Seq[Any]]
              } getOrElse Seq.empty[Any]
          }
      }
    result match {
      case Seq() => bsonValue.BsObject.toBson(Seq.empty)
      case v =>
        bsonValue.BsObject.toBson {
//          val res =
//            for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield Compose.composer(elem)
//          if (res.size>1) res else res.head //
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
  //  (all|first|last)

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



  private def startInjector(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case KeyWithGrammar(k,grammar) => //key.grammar
          executeSelectInjector(k,grammar.selectType)
        case Grammar(selectType) => // "(all|first|last)"
          executeSelectInjector("",selectType)

        case ArrExpr(left: Int, mid: Option[String], right: Option[Any], None) => // "[# .. #]"
          println("ArrExpr")
          executeArraySelectInjector("", left, mid.get, right.get)
        case KeyWithArrExpr(k,arrEx, None) =>    //key.[#..#]

          executeArraySelectInjector(k,arrEx.leftArg,arrEx.midArg.get,arrEx.rightArg.get)

        /*executeArraySelect(k,arrEx.leftArg,arrEx.midArg,arrEx.rightArg) match {
          case Seq() => bsonValue.BsObject.toBson(Seq.empty)
          case v =>
            bsonValue.BsObject.toBson {
              for(elem <- v.asInstanceOf[Seq[Array[Any]]]) yield elem.toList
            }
        }*/
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  def executeSelectInjector(key: String, selectType: String):  bsonValue.BsValue = {
    selectType match {
      case "first" =>
        val result: Try[BosonImpl] = Try(boson.modify(Option(boson), key, f.get).get)
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
      case "all" =>
        val result: Try[BosonImpl] = Try(new BosonImpl(byteArray = Option(boson.modifyAll(boson.getByteBuf, key, f.get)._1.array())))
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
      case "last"=>
        // val ocorrencias: Option[Int] = Option(boson.findOcorrences(boson.getByteBuf.duplicate(), key).size-1)
        val result: Try[BosonImpl] = Try(boson.modifyEnd(boson.getByteBuf.duplicate(), key, f.get)._1)
        result match {
          case Success(v) =>
            bsonValue.BsObject.toBson(v)
          case Failure(e) =>
            bsonValue.BsException.apply(e.getMessage)
        }
    }
  }

  def executeArraySelectInjector(key: String,left: Int, mid: String, right: Any): bsonValue.BsValue = {
println((key, left, mid.toLowerCase(), right))
    (key, left, mid.toLowerCase(), right) match {
      case ("", a, "until", "end") =>

        val midResult = Try(boson.modifyArrayEnd(boson.getByteBuf.duplicate(), f.get, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)

        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case ("", a, "to", "end") => // "[# .. end]"

        val midResult = Try(boson.modifyArrayEnd(boson.getByteBuf.duplicate(), f.get, a.toString))

        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._1)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case ("", a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val range: Range = a to b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEnd(boson.getByteBuf.duplicate(), f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEnd(boson.getByteBuf.duplicate(), f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      case (k, a, "until", "end") =>


        val midResult = Try(boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(),k, f.get, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }
      case (k, a, "to", "end") =>


        val midResult = Try(boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(),k, f.get, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._1)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }
      case (k, a, expr, b) if b.isInstanceOf[Int] =>
        expr match {
          case "to" =>
            val range: Range = a to b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(),k, f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEndWithKey(boson.getByteBuf.duplicate(),k,f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      /* val some: Try[Option[Any]] = Try(boson.extract(boson.getByteBuf, k, "until", Option(a)))
       val firstResult: Equals = some match {
         case Success(v) =>  v.map { v =>
       v.asInstanceOf[Seq[Array[Any]]]
       }.getOrElse (Seq.empty)
           //bsonValue.BsObject.toBson(v)
         case Failure(e) => bsonValue.BsException.apply(e.getMessage)
       }


       val midResult = Try(boson.modifyArrayEnd(f, a.toString))


       //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)

       midResult match {
         case Success(v) => bsonValue.BsObject.toBson(v._2)
         case Failure(e) => bsonValue.BsException.apply(e.getMessage)
       }*/
      case _ => bsonValue.BsException.apply("Invalid Expression.")
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
