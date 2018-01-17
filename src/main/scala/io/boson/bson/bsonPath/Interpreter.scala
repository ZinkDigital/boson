package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.{BosonImpl, CustomException}
import io.boson.bson.bsonValue
import io.netty.buffer.ByteBuf

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
        case MoreKeys(x, y, z) =>
          executeMoreKeys(x, y, z)
        case KeyWithGrammar(key, grammar) =>
          executeSelect(key, grammar.selectType) //key.grammar
        case KeyWithArrExpr(key, arrEx /*, secondKey*/) =>
          /* secondKey.isDefined match {
             case true if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#].secondKey
               executeArraySelectWithTwoKeys(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get, secondKey.get)
             case true => //key.[#].secondKey
               executeArraySelectWithTwoKeys(key,arrEx.leftArg,"to",arrEx.leftArg,secondKey.get)
             case false if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#]
               executeArraySelect(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get)
             case false => //key.[#]
               executeArraySelect(key,arrEx.leftArg,"to", arrEx.leftArg)
           }*/
          if (arrEx.midArg.isDefined && arrEx.rightArg.isDefined) {
            //key.[#..#]
            executeArraySelect(key, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get)
          } else {
            //[#]
            executeArraySelect(key, arrEx.leftArg, "to", arrEx.leftArg)
          }
        case ArrExpr(left, mid, right /*, secondKey*/) =>
          /*secondKey.isDefined match {
            case true if mid.isDefined && right.isDefined => //[#..#].2ndKey
              executeArraySelectWithTwoKeys("", left, mid.get, right.get, secondKey.get)
            case true => //[#].2ndKey
              executeArraySelectWithTwoKeys("",left, "to", left,secondKey.get)
            case false if mid.isDefined && right.isDefined => //[#..#]
              executeArraySelect("", left, mid.get, right.get)
            case false => //[#]
              executeArraySelect("",left, "to", left)
          }*/
          if (mid.isDefined && right.isDefined) { //[#..#]
            executeArraySelect("", left, mid.get, right.get)
          } else {
            //[#]
            executeArraySelect("", left, "to", left)
          }
        case Grammar(selectType) => // "(all|first|last)"
          executeSelect("", selectType)
        case HalfName(halfName) => //  "*halfName"
          println("                                                                                                 " + halfName)
          executeSelect(halfName, "all")
        /*case Everything(key) => //  *
          println("key "+key)
          executeSelect(key,"all")*/
        case HasElem(key, elem) => //  key.[@elem]
          executeHasElem(key, elem)
        case Key(key) => //  key
          println("all elements of Key")
          executeSelect(key, "all")
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def defineLimits(left: Int, mid: Option[String], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
    if(mid.isDefined && right.isDefined) {
      (left, mid.get.toLowerCase, right.get) match {
        case (a, "until", "end") => List((Some(a),None,"until"))
        case (a, _, "end") => List((Some(a),None,"to"))
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr.toLowerCase match {
            case "to" => List((Some(a),Some(b.asInstanceOf[Int]),"to"))
            case "until" => List((Some(a),Some(b.asInstanceOf[Int]-1),"to"))
          }
      }
    } else { //[#]
      List((Some(left),Some(left),"to"))
    }
  }

  private def buildKeyList(firstStatement: Statement, middleStatementList: List[Statement], lastStatement: Option[Statement]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList,limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      firstStatement match {
        case KeyWithGrammar(key, grammar) => (List((key, grammar.selectType)),List((None,None,"")))
        case KeyWithArrExpr(key,arrEx) => (List((key, "limit")), defineLimits(arrEx.leftArg,arrEx.midArg,arrEx.rightArg))
        case ArrExpr(l,m,r) => (List(("", "limit")), defineLimits(l,m,r))
        case HalfName(halfName) => (List((halfName, "all")), List((None,None,"")))
        case HasElem(key, elem) => (List((key, "limit"), (elem, "filter")),List((None,None,""),(None,None,"")) )
        case Key(key) =>
          if(middleStatementList.nonEmpty) (List((key, "next")),List((None,None,""))) else (List((key, "all")),List((None,None,"")))
      }

    val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for (statement <- middleStatementList) yield {
          statement match {
            case KeyWithGrammar(key, grammar) => (List((key, grammar.selectType)),List((None,None,"")))
            case KeyWithArrExpr(key, arrEx) => (List((key, "limit")), defineLimits(arrEx.leftArg,arrEx.midArg,arrEx.rightArg))
            case ArrExpr(l,m,r) => (List(("", "limit")), defineLimits(l,m,r))
            case HalfName(halfName) => (List((halfName, "all")), List((None,None,"")))
            case HasElem(key, elem) =>
              (List((key, "limit"), (elem, "filter")),List((None,None,""),(None,None,"")) )
            case Key(key) => (List((key, "next")),List((None,None,"")))
          }
        }
    val secondList: List[(String, String)] = firstList ++ forList.flatMap(p => p._1)
    val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(p => p._2)


    val (thirdList,limitList3): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      if (lastStatement.isDefined) {
        lastStatement.get match {
          case KeyWithGrammar(key, grammar) =>
            println("!!!!!!-----------------!!!!!!!!!!-------buildKeyList lastStatement is KeyWithGrammar-------NOT EXPECTED!!!!!!!!")
            (secondList ++ List((key, grammar.selectType)),limitList2 ++ List((None,None,"")))
          case Grammar(selectType) =>
            middleStatementList.last match {
              case Key(k) => (secondList.take(secondList.size - 1) ++ List((k, selectType)), limitList2)
              case _ => throw new RuntimeException("Terms as \"last\", \"first\" and \"all\" are only applicable to a Key")
            }
        }
      } else {
        println("last statement nonDefined")
        middleStatementList.last match {
          case Key(k) => (secondList.take(secondList.size - 1) ++ List((k,"level")), limitList2)
          case _ => (secondList,limitList2)
        }
      }
//    scndList.zipWithIndex.map( elem =>
//    if(elem._1._3.equals("HasElem") && elem._2 != scndList.size-1 && elem._1._2.equals("filter")){
//      (elem._1._1,"next")
//    } else (elem._1._1, elem._1._2)
//    )
    (thirdList,limitList3)
  }

  private def executeMoreKeys(first: Statement, list: List[Statement], last: Option[Statement]): bsonValue.BsValue = {
    println("executeMoreKeys before build list")
    val keyList: (List[(String, String)], List[(Option[Int], Option[Int], String)]) = buildKeyList(first, list, last)
    println("after build keylist -> " + keyList._1)
    println("after build limitlist -> " + keyList._2)
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, keyList._1, keyList._2) map { v =>
        v.asInstanceOf[Seq[Any]]
      } getOrElse Seq.empty[Any]

    val finalResult: Seq[Any] =   //  TODO:test more cases and rethink a better structure
      if (last.isDefined) {
        last.get match {
          case Grammar(grammar) =>
            grammar match {
              case "last" =>
                (for (elem <- result) yield {
                  elem match {
                    case e: Array[Any] => Compose.composer(e)
                    case e => e
                  }
                }).toVector map { elem => elem.asInstanceOf[Seq[Any]].last }
              case _ => result
            }
          case _ => throw new RuntimeException("default case match inside executeMoreKeys")
        }
      } else result

    finalResult match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty[Any])
      case v => bsonValue.BsObject.toBson {
        (for (elem <- v) yield {
          elem match {
            case e: Array[Any] => Compose.composer(e)
            case e => e
          }
        }).toVector
      }
    }
  }

  private def startInjector(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case KeyWithGrammar(k, grammar) =>
          println("KeyWithGrammar") //key.grammar
          executeSelectInjector(k, grammar.selectType)
        case Grammar(selectType) => // "(all|first|last)"
          println("Grammar")
          executeSelectInjector("", selectType)
        case ArrExpr(left: Int, mid: Option[String], right: Option[Any] /*, secondKey*/) => // "[# .. #]"
          println("ArrExpr")
          //executeArraySelectInjector("", left, mid.get, right.get)

          /* secondKey.isDefined match {
             case true if mid.isDefined && right.isDefined => //[#..#].2ndKey
               ???
               //executeArraySelectWithTwoKeys("", left, mid.get, right.get, secondKey.get)
             case true => //[#].2ndKey
               ???
               //executeArraySelectWithTwoKeys("",left, "to", left,secondKey.get)
             case false if mid.isDefined && right.isDefined => //[#..#]
               executeArraySelectInjector("", left, mid.get, right.get)
             case false => //[#]
               executeArraySelectInjector("", left, "to", left)
           }*/
          if (mid.isDefined && right.isDefined) {
            //[#..#]
            executeArraySelectInjector("", left, mid.get, right.get)
          } else {
            //[#]
            executeArraySelectInjector("", left, "to", left)
          }
        case KeyWithArrExpr(k, arrEx /*, secondKey*/) => //key.[#..#]
          //executeArraySelectInjector(k,arrEx.leftArg,arrEx.midArg.get,arrEx.rightArg.get)
          println("KeyWithArrExpr")
          /* secondKey.isDefined match {
             case true if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#].secondKey
               ???
               //executeArraySelectWithTwoKeys(k, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get, secondKey.get)
             case true => //key.[#].secondKey
               ???
               //executeArraySelectWithTwoKeys(k,arrEx.leftArg,"to",arrEx.leftArg,secondKey.get)
             case false if arrEx.midArg.isDefined && arrEx.rightArg.isDefined => //key.[#..#]
               executeArraySelectInjector(k,arrEx.leftArg,arrEx.midArg.get,arrEx.rightArg.get)
             case false => //key.[#]
               executeArraySelectInjector(k,arrEx.leftArg,"to",arrEx.leftArg)
           }*/

          if (arrEx.midArg.isDefined && arrEx.rightArg.isDefined) {
            //key.[#..#]
            executeArraySelectInjector(k, arrEx.leftArg, arrEx.midArg.get, arrEx.rightArg.get)
          } else {
            //key.[#]
            executeArraySelectInjector(k, arrEx.leftArg, "to", arrEx.leftArg)
          }
        case HalfName(halfName) => //  "*halfName"
          println("HalfName")
          executeSelectInjector(halfName, "all")
        /* case Everything(key) => //  *
           println("Everything")
           executeSelectInjector(key,"all")*/
        case HasElem(key, elem) => //  key.(@elem)
          println("HasElem")
          executeHasElemInjector(key, elem)
        case Key(key) => //  key
          println("key ")
          executeSelectInjector(key, "all")
        case MoreKeys(first, list, last) => //  key
          println("MoreKeys ")
          val statements: ListBuffer[Statement] = new ListBuffer[Statement]
          statements.append(first)
          list.foreach(statement => statements.append(statement))
          if (last.isDefined) {
            statements.append(last.get)
          }
          executeMultipleKeysInjector(statements)
        //  executeSelectInjector("*","all")
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

  /*
  * Extractor functions
  * */

  private def executeHasElem(key: String, elem: String): bsonValue.BsValue = {
    println(s"hasElem with key: $key and elem: $elem")
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, List((key, "limit"), (elem, "filter")), List((None,None,""))) map { v =>
        v.asInstanceOf[Seq[Any]]
      } getOrElse Seq.empty[Any]
    result match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty[Any])
      case v => bsonValue.BsObject.toBson {
        (for (elem <- v) yield {
          elem match {
            case e: Array[Any] => Compose.composer(e)
            case e => e
          }
        }).toVector
      }
    }
  }

  private def executeArraySelect(key: String, left: Int, mid: String, right: Any): bsonValue.BsValue = {
    println("executeArraySelect")
    val result: Seq[Any] =
      (left, mid, right) match {
        case (a, ("until" | "Until"), "end") =>
          val midResult: Option[Any] = boson.extract(boson.getByteBuf, List((key, "limit")), List((Some(a),None,"")))
          midResult.map(v => {
            (for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
              elem.take(elem.length - 1)
            }).filter(p => p.nonEmpty)
          }).getOrElse(Seq.empty)
        case (a, _, "end") => // "[# .. end]"
          val midResult: Option[Any] = boson.extract(boson.getByteBuf, List((key, "limit")), List((Some(a),None,"")))
          midResult.map { v =>
            v.asInstanceOf[Seq[Any]]
          }.getOrElse(Seq.empty)
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr match {
            case ("to" | "To") =>
              println("case # TO #")
              boson.extract(
                boson.getByteBuf, List((key, "limit")), List((Some(a), Some(b.asInstanceOf[Int]),""))
              ).map { v =>
                v.asInstanceOf[Seq[Any]]
              }.getOrElse(Seq.empty)
            case ("until" | "Until") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit")), List((Some(a), Some(b.asInstanceOf[Int] - 1),""))
              ).map { v =>
                v.asInstanceOf[Seq[Any]]
              }.getOrElse(Seq.empty)
          }
      }
    result match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty)
      case v =>
        bsonValue.BsObject.toBson {
          val bson: Vector[Any] =
          (for (elem <- v) yield {
            elem match {
              case e: Array[Any] => Compose.composer(e)
              case e => e
            }
          }).toVector
          println(bson)
          bson
        }
    }
  }

  private def executeArraySelectWithTwoKeys(key: String, left: Int,
                                            mid: String, right: Any,
                                            secondKey: String): bsonValue.BsValue = {
    val result: Seq[Any] =
      (left, mid, right) match {
        case (a, ("until" | "Until"), "end") =>
          boson.extract(
            boson.getByteBuf, List((key, "limit"), (secondKey, "all")), List((Some(a),None,""))
          ) map { v => {
            for (elem <- v.asInstanceOf[Seq[Array[Any]]]) yield {
              elem.take(elem.length - 1)
            }
          }
          } getOrElse Seq.empty[Any]
        case (a, _, "end") =>
          boson.extract(
            boson.getByteBuf, List((key, "limit"), (secondKey, "all")), List((Some(a),None,""))
          ) map { v =>
            v.asInstanceOf[Seq[Any]]
          } getOrElse Seq.empty[Any]
        case (a, expr, b) if b.isInstanceOf[Int] =>
          expr match {
            case ("to" | "To") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), List((Some(a), Some(b.asInstanceOf[Int]),""))
              ) map { v =>
                println(s"result before composer: $v")
                v.asInstanceOf[Seq[Any]]
              } getOrElse Seq.empty[Any]
            case ("until" | "Until") =>
              boson.extract(
                boson.getByteBuf, List((key, "limit"), (secondKey, "all")), List((Some(a), Some(b.asInstanceOf[Int] - 1),""))
              ) map { v =>
                v.asInstanceOf[Seq[Any]]
              } getOrElse Seq.empty[Any]
          }
      }
    result match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty)
      case v =>
        bsonValue.BsObject.toBson {
          val res: Vector[Any] =
            (for (elem <- v) yield {
              elem match {
                case e: Array[Any] => Compose.composer(e)
                case e => e
              }
            }).toVector
          println(s"result after compose: $res")
          res
        }
    }
  }

  private def executeSelect(key: String, selectType: String): bsonValue.BsValue = {
    println(s"executeSelect with key: $key and selectType: $selectType")
    val result: Option[Any] = boson.extract(boson.getByteBuf, List((key, selectType)),List((None,None,"")))
    println("after")
    selectType match {
      case "first" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map { v =>
            Vector(Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).head)
          }.getOrElse(Vector.empty[Any]))
        } else {
          result.map { v =>
            v.asInstanceOf[Seq[Any]].head match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson {
                  Compose.composer(p.asInstanceOf[Array[Any]]).toVector
                }
              case p =>
                bsonValue.BsObject.toBson(Vector(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Vector.empty[Any])
        }
      case "last" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Vector(Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).last)).getOrElse(Vector.empty))
        } else {
          result.map { elem =>
            elem.asInstanceOf[Seq[Any]].last match {
              case p if p.isInstanceOf[Array[Any]] =>
                bsonValue.BsObject.toBson(Compose.composer(p.asInstanceOf[Array[Any]]).toVector)
              case p =>
                bsonValue.BsObject.toBson(Vector(p))
            }
          } getOrElse bsonValue.BsObject.toBson(Vector.empty[Any])
        }
      case "all" =>
        if (key.isEmpty) {
          bsonValue.BsObject.toBson(result.map(v => Compose.composer(v.asInstanceOf[Seq[Array[Any]]].head).toVector).getOrElse(Vector.empty))
        } else {
          println("executeSelect -> inside Key nonEmpty")
          bsonValue.BsObject.toBson(result.map { v =>
            println(s"result: $v")
            v.asInstanceOf[Seq[Any]].map {
              case array: Array[Any] => Compose.composer(array)
              case elem => elem
            }.toVector
          }.getOrElse(Vector.empty))
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

  /*
  * Injector Functions
  * */
  private def executeSelectInjector(key: String, selectType: String): bsonValue.BsValue = {
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
        val result: Try[BosonImpl] = Try(new BosonImpl(byteArray = Option(boson.modifyAll(None,boson.getByteBuf, key, f.get).array())))
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

  private def executeArraySelectInjector(key: String,left: Int, mid: String, right: Any): bsonValue.BsValue = {
println((key, left, mid.toLowerCase(), right))
    (key, left, mid.toLowerCase(), right) match {
      case ("", a, "until", "end") =>
        val midResult = Try(boson.modifyArrayEnd(None, boson.getByteBuf.duplicate(), f.get, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }

      case ("", a, "to", "end") => // "[# .. end]"
        val midResult = Try(boson.modifyArrayEnd(None, boson.getByteBuf.duplicate(), f.get, a.toString))
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
            val midResult = Try(boson.modifyArrayEnd(None, boson.getByteBuf.duplicate(), f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEnd(None, boson.getByteBuf.duplicate(), f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      case (k, a, "until", "end") =>
        val midResult = Try(boson.modifyArrayEndWithKey(None, boson.getByteBuf.duplicate(),k, f.get, a.toString))
        //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
        midResult match {
          case Success(v) => bsonValue.BsObject.toBson(v._2)
          case Failure(e) => bsonValue.BsException.apply(e.getMessage)
        }
      case (k, a, "to", "end") =>
        val midResult = Try(boson.modifyArrayEndWithKey(None, boson.getByteBuf.duplicate(),k, f.get, a.toString))
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
            val midResult = Try(boson.modifyArrayEndWithKey(None, boson.getByteBuf.duplicate(),k, f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._1)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
          case "until" =>
            val range: Range = a until b.asInstanceOf[Int]
            val list: ListBuffer[String] = new ListBuffer[String]
            range.foreach( c => list.append(c.toString))
            val midResult = Try(boson.modifyArrayEndWithKey(None, boson.getByteBuf.duplicate(),k,f.get, a.toString, b.toString))
            //val midResult = boson.extract(boson.getByteBuf, key, "limit", Option(a), None)
            midResult match {
              case Success(v) => bsonValue.BsObject.toBson(v._2)
              case Failure(e) => bsonValue.BsException.apply(e.getMessage)
            }
        }
      case _ => bsonValue.BsException.apply("Invalid Expression.")
    }
  }

  private def executeHasElemInjector(key: String, elem: String): bsonValue.BsValue = {
    val midResult: Try[ByteBuf] = Try(boson.modifyHasElem(None,boson.getByteBuf.duplicate(), key, elem, f.get))
    midResult match {
      case Success(v) => bsonValue.BsObject.toBson(new BosonImpl(byteArray = Option(v.array())))
      case Failure(e) => bsonValue.BsException.apply(e.getMessage)
    }
  }


  private def executeMultipleKeysInjector(statements: ListBuffer[Statement]): bsonValue.BsValue = {
    val result:ByteBuf= boson.execStatementPatternMatch(boson.getByteBuf, Some(statements), f.get )

    // if Statements size is equal to 1 then cal start Injector
    // else keep filter the buffer
    /*if(statements.size==1){
      //execute statement
      startInjector(List(statements.head))
    }else {
      // filter buffer
      execStatementPatternMatch(statements)
    }*/
    // return BsValue

    bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(result.array())))
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
