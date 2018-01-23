package io.boson.bson.bsonPath

import io.boson.bson.bsonImpl.{BosonImpl, CustomException}
import io.boson.bson.bsonValue

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */
class Interpreter[T](boson: BosonImpl, program: Program, f: Option[Function[T,T]] = None) {

  def run(): bsonValue.BsValue = {
    f match {
      case Some(_) => //func /*Inejctor*/
        startInjector(program.statement)
      case None => /*Extractor*/
        start(program.statement)
    }
  }

  private def start(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case MoreKeys(first, list, dots) =>
          println(s"statements: ${List(first) ++ list}")
          println(s"dotList: $dots")
          executeMoreKeys(first, list, dots)
        case MoreKeysRoot(first, list, dots) =>
          println("Interpreter.start() -> MoreKeysRoot case")
          println(s"statements: ${List(first) ++ list}")
          executeMoreKeysRoot(first,list, dots)
        case _ => throw new RuntimeException("Something went wrong!!!")
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def executeMoreKeysRoot(first: Statement, list: List[Statement], dots: List[String]): bsonValue.BsValue = {
    val keyLimitList: (List[(String, String)], List[(Option[Int], Option[Int], String)]) = buildKeyListRoot(first, list, dots)
    println("after build keylist -> " + keyLimitList._1)
    println("after build limitlist -> " + keyLimitList._2)
      val result: Seq[Any] =
        boson.extract(boson.getByteBuf, keyLimitList._1, keyLimitList._2) map { v =>
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

  private def buildKeyListRoot(first: Statement, statementList: List[Statement], dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
      val (firstList,limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
        first match {
          case KeyWithArrExpr(key,arrEx) => (List((key, "limitLevel")), defineLimits(arrEx.leftArg,arrEx.midArg,arrEx.rightArg))  //taken care of
          case ArrExpr(l,m,r) => (List(("", "limitLevel")), defineLimits(l,m,r))                                                  //taken care of
          case HalfName(halfName) => if(halfName.equals("*")) (List((halfName, "level")), List((None,None,""))) else (List((halfName, "level")), List((None,None,"")))  //TODO: take care of '*'
          case HasElem(key, elem) => (List((key, "limitLevel"), (elem, "filter")),List((None,None,""),(None,None,"")) )           //taken care of
          case Key(key) =>                                                                                                        //taken care of
            if(statementList.nonEmpty) (List((key, "next")),List((None,None,""))) else (List((key, "level")),List((None,None,"")))
          case _ => throw CustomException("Error building key list")
        }

      if(statementList.nonEmpty) {
        val forList: List[(List[(String,String)], List[(Option[Int], Option[Int], String)])] =
          for (statement <- statementList) yield {
            statement match {
              case KeyWithArrExpr(key, arrEx) => (List((key, "limitLevel")), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg)) //taken care of
              case ArrExpr(l, m, r) => (List(("", "limitLevel")), defineLimits(l, m, r))                                                //TODO:missing, probably never gets inside this case
              case HalfName(halfName) => (List((halfName, "level")), List((None, None, "")))                                            //taken care of
              case HasElem(key, elem) => (List((key, "limitLevel"), (elem, "filter")), List((None, None, ""), (None, None, "")))        //taken care of
              case Key(key) => (List((key, "next")), List((None, None, "")))
              case _ => throw CustomException("Error building key list")//taken care of
            }
          }
        val secondList: List[(String, String)] = firstList ++ forList.flatMap(p => p._1)
        val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(p => p._2)
        println(s"secondList -> $secondList")
        println(s"dotList -> $dotsList")

        val thirdList: List[(String, String)] = secondList.zipWithIndex map {elem =>
          elem._1._2 match {
            case "limitLevel" => if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"limit") else elem._1
            case "level" => if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"all") else elem._1
            case "filter" => elem._1
            case "next" => if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"all") else elem._1
            case _ => throw CustomException("Error building key list with dots")
          }
        }
        println(s"thirdlist -> $thirdList")
        dotsList.last match {
          case "." =>
            statementList.last match {
              case HalfName(halfName) => if(halfName.equals("*")) (thirdList.take(thirdList.size-1)++List((halfName,"all")),limitList2) else (thirdList, limitList2) //TODO: take care of '*'
              case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, "level")), limitList2)
              case _ => (thirdList, limitList2)
            }
          case ".." => (thirdList, limitList2)
        }
      } else (firstList,limitList1)
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

  private def buildKeyList(firstStatement: Statement, middleStatementList: List[Statement], dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList,limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      firstStatement match {
        //case KeyWithGrammar(key, grammar) => (List((key, grammar.selectType)),List((None,None,"")))
        case KeyWithArrExpr(key,arrEx) => (List((key, "limit")), defineLimits(arrEx.leftArg,arrEx.midArg,arrEx.rightArg))
        case ArrExpr(l,m,r) => (List(("", "limit")), defineLimits(l,m,r))
        case HalfName(halfName) => if(halfName.equals("*")) (List((halfName, "level")), List((None,None,""))) else (List((halfName, "all")), List((None,None,"")))  //TODO: treat '*'
        case HasElem(key, elem) => (List((key, "limit"), (elem, "filter")),List((None,None,""),(None,None,"")) )
        case Key(key) =>
          if(middleStatementList.nonEmpty) (List((key, "next")),List((None,None,""))) else (List((key, "all")),List((None,None,"")))
        case _ => throw CustomException("Error building key list")
      }
    if (middleStatementList.nonEmpty) { // edited the limit to limitLevel in the traverse statementList, needs further aproval
      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for (statement <- middleStatementList) yield {
          statement match {
            //case KeyWithGrammar(key, grammar) => (List((key, grammar.selectType)), List((None, None, "")))
            case KeyWithArrExpr(key, arrEx) => (List((key, "limitLevel")), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case ArrExpr(l, m, r) => (List(("", "limitLevel")), defineLimits(l, m, r))
            case HalfName(halfName) => (List((halfName, "level")), List((None, None, "")))                                                                          //TODO: treat '*'
            case HasElem(key, elem) =>
              (List((key, "limitLevel"), (elem, "filter")), List((None, None, ""), (None, None, "")))
            case Key(key) => (List((key, "next")), List((None, None, "")))
            case _ => throw CustomException("Error building key list")
          }
        }
      val secondList: List[(String, String)] = firstList ++ forList.flatMap(p => p._1)
      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(p => p._2)

      val thirdList: List[(String, String)] = secondList.zipWithIndex map {elem =>
        elem._1._2 match {
          case "limitLevel" => if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"limit") else elem._1
          case "level" => if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"all") else elem._1
          case "filter" => elem._1
          case "next" => elem._1//if(dotsList.take(elem._2+1).last.equals("..")) (elem._1._1,"all") else elem._1
          case "limit" => if(dotsList.take(elem._2+1).last.equals(".."))elem._1 else(elem._1._1,"limitLevel")
          case _ => throw CustomException("Error building key list with dots")
        }
      }
      println(s"thirdlist -> $thirdList")
      dotsList.last match {
        case "." =>
          middleStatementList.last match {
            case HalfName(halfName) => if (halfName.equals("*")) (thirdList.take(thirdList.size - 1) ++ List((halfName, "all")), limitList2) else (thirdList, limitList2) //TODO: treat '*'
            case KeyWithArrExpr(key, _) => (thirdList.take(thirdList.size - 1) ++ List((key, "limitLevel")), limitList2)
            case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, "level")), limitList2)
            case _ => (thirdList, limitList2)
          }
        case ".." => (thirdList, limitList2)
      }
    } else (firstList,limitList1)
//    val (thirdList,limitList3): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
//      if (lastStatement.isDefined) {
//        lastStatement.get match {
//          case KeyWithGrammar(key, grammar) =>
//            println("!!!!!!-----------------!!!!!!!!!!-------buildKeyList lastStatement is KeyWithGrammar-------NOT EXPECTED!!!!!!!!")
//            (secondList ++ List((key, grammar.selectType)),limitList2 ++ List((None,None,"")))
//          case Grammar(selectType) =>
//            middleStatementList.last match {
//              case Key(k) => (secondList.take(secondList.size - 1) ++ List((k, selectType)), limitList2)
//              case _ => throw new RuntimeException("Terms as \"last\", \"first\" and \"all\" are only applicable to a Key")
//            }
//        }
//      } else {
//        Try(middleStatementList.last)match{
//          case Success(v) =>
//
//            v match {
//              case Key(k) => (secondList.take(secondList.size - 1) ++ List((k,"level")), limitList2)
//              case _ => (secondList,limitList2)
//            }
//          case Failure(e)=> (secondList,limitList2)
//        }
//
//        //println("last statement nonDefined")
////        middleStatementList.last match {
////          case Key(k) => (secondList.take(secondList.size - 1) ++ List((k,"level")), limitList2)
////          case _ => (secondList,limitList2)
////        }
//      }
//    (thirdList,limitList3)
  }

  private def executeMoreKeys(first: Statement, list: List[Statement], dotsList: List[String]): bsonValue.BsValue = {
    println("executeMoreKeys before build list")
    val keyList: (List[(String, String)], List[(Option[Int], Option[Int], String)]) = buildKeyList(first, list, dotsList)
    println("after build keylist -> " + keyList._1)
    println("after build limitlist -> " + keyList._2)
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, keyList._1, keyList._2) map { v =>
        v.asInstanceOf[Seq[Any]]
      } getOrElse Seq.empty[Any]

//    val finalResult: Seq[Any] =   //  TODO:test more cases and rethink a better structure
//      if (last.isDefined) {
//        last.get match {
//          case Grammar(grammar) =>
//            grammar match {
//              case "last" =>
//                (for (elem <- result) yield {
//                  elem match {
//                    case e: Array[Any] => Compose.composer(e)
//                    case e => e
//                  }
//                }).toVector map { elem => elem.asInstanceOf[Seq[Any]].last }
//              case _ => result
//            }
//          case _ => throw new RuntimeException("default case match inside executeMoreKeys")
//        }
//      } else result

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

  private def startInjector(statement: List[Statement]): bsonValue.BsValue = {
    if (statement.nonEmpty) {
      statement.head match {
        case MoreKeys(first, list, dots) => //  key
          val statements: ListBuffer[Statement] = new ListBuffer[Statement]
          statements.append(first)
          list.foreach(statement => statements.append(statement))
          executeMultipleKeysInjector(statements)
        case _ => throw new RuntimeException("Something went wrong!!!")
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def executeMultipleKeysInjector(statements: ListBuffer[Statement]): bsonValue.BsValue = {
    val result:bsonValue.BsValue=
      Try(boson.execStatementPatternMatch(boson.getByteBuf, Some(statements), f.get ))match{
        case Success(v)=> bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
        case Failure(e)=>bsonValue.BsException(e.getMessage)      }

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
result
    //bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(result.array())))
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
