package io.zink.boson.bson.bsonPath

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonImpl._
import shapeless._
import ops.record._
import shapeless.labelled.{FieldType, field}
import shapeless.syntax.typeable
import syntax.singleton._

import scala.collection.mutable.ListBuffer
//import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */


case class Book(title: String, edition: Int, forSale: Boolean)

class Interpreter[T](boson: BosonImpl, program: Program, fInj: Option[T => T] = None, fExt: Option[T => Unit] = None) {

  def run(): Any = {
    fInj.isDefined match {
      case true => startInjector(program.statement)
      case false if fExt.isDefined =>
        start(program.statement)
        //boson.getByteBuf.array
      case false => throw new IllegalArgumentException("Construct Boson object with at least one Function.")
    }
  }

  private def start(statement: List[Statement]): Any = {
    if (statement.nonEmpty) {
      statement.head match {
//        case MoreKeys(first, list, dots) if first.isInstanceOf[ROOT]=>
//          //println(s"statements: ${List(first) ++ list}")
//          //println(s"dotList: $dots")
//          //executeMoreKeys(first, list, dots)
//          ???
        case MoreKeys(first, list, dots) =>
//          println(s"statements: ${List(first) ++ list}")
//          println(s"dotList: $dots")
          extract(boson.getByteBuf,List(first) ++ list)
          //buildExtractors(List(first) ++ list)
          //executeMoreKeys(first, list, dots)
        case _ => throw new RuntimeException("Something went wrong!!!")
      }
    }else throw new RuntimeException("List of statements is empty.")
  }

  private def constructObj(encodedSeqByteArray: Seq[Array[Byte]], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Seq[List[(String,Any)]] = {
//    def help(list: Iterable[Any]): List[(String, Any)] = {
//      list match {
//        case (x: String) :: (y: Any) :: Nil => List((x, y))
//        case (x: String) :: (y: Any) :: xs => List((x, y)) ++ help(xs)
//      }
//    }
    def toTuples(list: Iterable[Any]): List[(String, Any)] = {
      list match {
        case x: List[Any] if x.isEmpty => List()
        case x: List[Any] if x.lengthCompare(2) >= 0 => List((x.head.asInstanceOf[String], x.tail.head)) ++ toTuples(x.drop(2))
      }
    }
    //println("constructObj")
    val constructedObjs: /*Seq[T]*/ Seq[List[(String,Any)]] =
      encodedSeqByteArray.map { encodedByteArray =>
        val res: Iterable[Any] = runExtractors(Unpooled.copiedBuffer(encodedByteArray), keyList, limitList).asInstanceOf[Iterable[Iterable[Any]]].flatten
        // val resToTuples: List[(String, Any)] = help(res).map(elem => (elem._1.toLowerCase, elem._2))
        //case class Book(title: String, price: Double, edition: Int, forsale: Boolean, npages: Long)
        //        val book = Book("",2.2,2,true,10000)
        //        val repr = genObj.get.to(book.asInstanceOf[R])
        //        genObj.get.from(repr)
        //FromList.to(genObj.get).from(resToTuples)
        //println(Generic[Book])
        //GenericObj[Book]
        //println(FromList.to[Book].from(resToTuples))
        //fromMap(resToTuples)
        val l: List[(String,Any)] = toTuples(res).map(elem => (elem._1.toLowerCase,elem._2))
        l
      }
    constructedObjs
        //println(l)
//        def rec(list: Seq[String]): HList = {
//          list.length match{
//            case 1 =>list.head::HNil
//            case _ => list.head::rec(list.drop(1))
//          }
//        }
        //val list: HList = rec(l)
        //println(list)
//        val labl = genObj.get
//        val r: T = labl.from(list.asInstanceOf[labl.Repr])
//        println(r)
        //extractLabels.to[T].from[gen.Repr](l)
      //}.collect{ case v if v.nonEmpty => v.get}
    //constructedObjs.foreach( elem => fExt.get(elem))


    //println(s"constructedObjs: $constructedObjs")
//    constructedObjs.size match {
//      case 1 =>
//        //println(s"constructedObjs: $constructedObjs")
//        Transform.toPrimitive(fExt.get, constructedObjs.head)
//      case _ =>
//        //println(s"constructedObjs: $constructedObjs")
//        Transform.toPrimitive(fExt.get, constructedObjs.asInstanceOf[R])
//    }
//    val res: Iterable[Any] = runExtractors(Unpooled.copiedBuffer(encodedByteArray), keyList, limitList).asInstanceOf[Iterable[Iterable[Any]]].flatten
//    //println(s"res: $res")
//    val seqSorted: Seq[(String, Any)] = help(res).map(elem => (elem._1.toLowerCase, elem._2)).sortWith(_._1 < _._1)
//    //println(s"Extracted seqSorted = $seqSorted")
//    val book = fromMap(seqSorted)
//    Transform.toPrimitive[R](fExt.get, book)
    //println(s"caseClass constructed: $book")
  }

  /*private def fromMap(m: Seq[(String, Any)]) = {
    val tArgs = typeOf[R] match {
      case TypeRef(_, _, args) => args
    }
    val runType =
      tArgs match {
        case x: Seq[Any] if x.isEmpty => typeOf[R]
        case x => x.head
      }
    //println(s"runtype -> $runType")
    val rm = runtimeMirror(getClass.getClassLoader)
    val classTest = runType.typeSymbol.asClass
    val classMirror = rm.reflectClass(classTest)
    val constructor = runType.decl(termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)

    val constructorArgsTuple = constructor.paramLists.flatten.map((param: Symbol) => {
      (param.name.toString, param.typeSignature)
    })
    val constructorArgs =
      constructorArgsTuple.map {
        case (name, _type) =>
          m.find(elem => elem._1.equals(name.toLowerCase)).map { element =>
            _type match {
              case t if t =:= typeOf[Double] && element._2.getClass.getSimpleName.equals(DOUBLE) => element._2
              case t if t =:= typeOf[String] && element._2.getClass.getSimpleName.equals(STRING) => element._2
              case t if t =:= typeOf[Boolean] && element._2.getClass.getSimpleName.equals(BOOLEAN) => element._2
              case t if t =:= typeOf[Int] && element._2.getClass.getSimpleName.equals(INTEGER) => element._2
              case t if t =:= typeOf[Long] && element._2.getClass.getSimpleName.equals(LONG) => element._2
              case _ => throw new IllegalArgumentException(s"Argument $name has different type of key with same name from the extracted object.")
            }
          }.getOrElse(throw new IllegalArgumentException(s"Missing argument $name in the extracted object."))
      }
    constructorMirror(constructorArgs: _*).asInstanceOf[R]
  }
*/
  private def buildExtractors(statementList: List[Statement]): (List[(String,String)],List[(Option[Int], Option[Int], String)]) = {
    //println(s"Generic of Type R: ${Generic[R]}")
    val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
    for( statement <- statementList) yield{
      statement match {
        case Key(key) => (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
        case KeyWithArrExpr(key, arrEx) =>(List((key, C_NEXT),(EMPTY_KEY, C_LIMITLEVEL)),List((None, None, EMPTY_KEY))++defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case ArrExpr(l, m, r) =>  (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
        case HalfName(halfName) =>  (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
        case HasElem(key, elem) =>  (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
      }
    }
    val keyList: List[(String,String)] = forList.flatMap(elem => elem._1)
    val limitList: List[(Option[Int], Option[Int], String)] = forList.flatMap(elem => elem._2)

    val finalKeyList: List[(String,String)] =
    keyList.last._2 match {
      case C_NEXT => keyList.take(keyList.size-1)++ List((keyList.last._1,C_LEVEL))
      case _ => keyList
    }
    (finalKeyList,limitList)
    //extract(boson.getByteBuf, finalKeyList, limitList)
    //boson.getByteBuf.release()
    //runExtractors(boson.getByteBuf, finalKeyList, limitList)
  }

  private def extract(encodedStructure: ByteBuf, statementList: List[Statement]): Any = {
    val (keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]) = buildExtractors(statementList)
    val result: Iterable[Any] = runExtractors(encodedStructure, keyList, limitList)
    //println(s"extracted -> $result")
    val typeClass =
      result.size match {
        case 0 => None
        case 1 => Some(result.head.getClass.getSimpleName)
        case _ =>
          if (result.tail.forall { p => result.head.getClass.equals(p.getClass) }) Some(result.head.getClass.getSimpleName)
          else None
      }
    //println(s"typeClass: $typeClass")
    //println(s"Final result from extraction: $result")
    if(returnInsideSeq(limitList)){
      /*val tArgs = typeOf[R] match {
        case TypeRef(_,_,args) => args
      }
      //println(s"tArgs: $tArgs")*/
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING => Transform.toPrimitive(fExt.get.asInstanceOf[Seq[String] => Unit], result.asInstanceOf[Seq[String]])
          case INTEGER => Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Int] => Unit], result.asInstanceOf[Seq[Int]])
          case LONG => Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Long] => Unit], result.asInstanceOf[Seq[Long]])
          case BOOLEAN => Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Boolean] => Unit], result.asInstanceOf[Seq[Boolean]])
          case DOUBLE => Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Double] => Unit], result.asInstanceOf[Seq[Double]])
//          case ARRAY_BYTE if tArgs.head =:= typeOf[Array[Byte]] =>
//            Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], result.asInstanceOf[Seq[Array[Byte]]])
          case ARRAY_BYTE => constructObj(result.asInstanceOf[Seq[Array[Byte]]],List(("*","build")), List((None,None,"")))
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    } else {
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING => Transform.toPrimitive(fExt.get.asInstanceOf[String => Unit], result.asInstanceOf[Seq[String]].head)
          case INTEGER => Transform.toPrimitive(fExt.get.asInstanceOf[Int => Unit], result.asInstanceOf[Seq[Int]].head)
          case LONG => Transform.toPrimitive(fExt.get.asInstanceOf[Long => Unit], result.asInstanceOf[Seq[Long]].head)
          case BOOLEAN => Transform.toPrimitive(fExt.get.asInstanceOf[Boolean => Unit], result.asInstanceOf[Seq[Boolean]].head)
          case DOUBLE => Transform.toPrimitive(fExt.get.asInstanceOf[Double => Unit], result.asInstanceOf[Seq[Double]].head)
//          case ARRAY_BYTE if typeOf[R] =:= typeOf[Array[Byte]] =>
//            Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], result.asInstanceOf[Seq[Array[Byte]]].head)
          case ARRAY_BYTE => constructObj(result.asInstanceOf[Seq[Array[Byte]]],List(("*","build")), List((None,None,"")))
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    }
  }

  private def returnInsideSeq(limitList: List[(Option[Int], Option[Int], String)]): Boolean = limitList.exists { elem =>
    elem._1.isDefined match {
      case true if elem._2.isEmpty => true
      case true if elem._2.isDefined && elem._2.get != elem._1.get => true
      case true if elem._2.isDefined && elem._2.get == elem._1.get => false
      case false => false
    }
    //TODO: missing implementation to verify ".." and "[@.elem]
  }

  private def runExtractors(encodedStructure: ByteBuf, keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Iterable[Any] = {
//    println(s"KeyList: $keyList")
//    println(s"LimitList: $limitList")
    val value: Iterable[Any] =
    keyList.size match {
      case 1 =>
        val res: Iterable[Any] = boson.extract(encodedStructure, fExt.get, keyList, limitList)
        //println(s"RES from last extractor: $res")
        res
      case _ =>
        val res: Iterable[Any] = boson.extract(encodedStructure, fExt.get, keyList, limitList)
        res.forall(e => e.isInstanceOf[Array[Byte]]) match {
          case true /*if keyList.head._1.equals(EMPTY_KEY)*/ =>
            val result: Iterable[Any] =
              res.asInstanceOf[Iterable[Array[Byte]]].map { elem =>
                val b: ByteBuf = Unpooled.buffer(elem.length).writeBytes(elem)
                runExtractors(b, keyList.drop(1), limitList.drop(1))
                //b.release()
              }.reduce(_++_)
            //println(s"RES from extractor: $result")
            result
          case false => throw CustomException("The given path doesn't correspond with the event structure.")
        }
    }
    value
  }

  /*private def buildKeyList(first: Statement, statementList: List[Statement], dotsList: List[String]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    val (firstList, limitList1): (List[(String, String)], List[(Option[Int], Option[Int], String)]) =
      first match {
        case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
        case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
        case HalfName(halfName) =>
          halfName.equals(STAR) match {
            case true => (List((halfName, C_ALL)), List((None, None, STAR)))
            case false if statementList.nonEmpty => (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
            case false => (List((halfName, C_LEVEL)), List((None, None, EMPTY_KEY)))
          }
        case HasElem(key, elem) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        case Key(key) => if (statementList.nonEmpty) (List((key, C_NEXT)), List((None, None, EMPTY_KEY))) else (List((key, C_LEVEL)), List((None, None, EMPTY_KEY)))
        case ROOT() => (List((C_DOT,C_DOT)), List((None,None,EMPTY_RANGE)))
        case _ => throw CustomException("Error building key list")
      }
    if (statementList.nonEmpty) {
      val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
        for (statement <- statementList) yield {
          statement match {
            case KeyWithArrExpr(key, arrEx) => (List((key, C_LIMITLEVEL)), defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
            case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
            case HalfName(halfName) =>if(halfName.equals(STAR)) (List((halfName, C_ALL)), List((None, None, STAR))) else (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
            case HasElem(key, elem) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
            case Key(key) => (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
            case _ => throw CustomException("Error building key list")
          }
        }
      val secondList: List[(String, String)] = firstList ++ forList.flatMap(p => p._1)
      val limitList2: List[(Option[Int], Option[Int], String)] = limitList1 ++ forList.flatMap(p => p._2)

      val thirdList: List[(String, String)] = secondList.zipWithIndex map {elem =>
        elem._1._2 match {
          case C_LIMITLEVEL => if(dotsList.take(elem._2+1).last.equals(C_DOUBLEDOT)) (elem._1._1,C_LIMIT) else elem._1
          case C_LEVEL => println("----- NOT POSSIBLE----"); elem._1
          case C_FILTER => elem._1
          case C_NEXT => elem._1
          case C_ALL => (elem._1._1,C_NEXT)//elem._1
          case _ => throw CustomException("Error building key list with dots")
        }
      }
      dotsList.last match {
        case C_DOT =>
          statementList.last match {
            case HalfName(halfName) if !halfName.equals(STAR) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_LEVEL)), limitList2)
            case HalfName(halfName) if halfName.equals(STAR) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_ALL)), limitList2)
            case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, C_LEVEL)), limitList2)
            case _ => (thirdList, limitList2)
          }
        case C_DOUBLEDOT =>
          statementList.last match {
            case HalfName(halfName) => (thirdList.take(thirdList.size - 1) ++ List((halfName, C_ALL)), limitList2) //TODO: treat '*'
            case Key(k) => (thirdList.take(thirdList.size - 1) ++ List((k, C_ALL)), limitList2)
            case _ => (thirdList, limitList2)
          }
      }
    } else {
      (firstList.map { elem =>
        elem._2 match {
          case C_LIMITLEVEL => if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_LIMIT) else elem
          case C_LEVEL => if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_ALL) else elem
          case C_FILTER => elem
          case C_NEXT => println("----- NOT POSSIBLE----");if(dotsList.head.equals(C_DOUBLEDOT)) (elem._1,C_ALL) else elem
          case C_ALL => elem
          case C_DOT => elem
        }
      },limitList1)
    }
  }
*/
  private def defineLimits(left: Int, mid: Option[String], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
    mid.isDefined match {
      case true if right.isEmpty=>
        mid.get match {
          case C_FIRST => List((Some(0),Some(0),TO_RANGE))
          case C_ALL => List((Some(0),None,TO_RANGE))
          case C_END => List((Some(0),None,C_END))
        }
      case true if right.isDefined =>
        (left, mid.get.toLowerCase, right.get) match {
          case (a, UNTIL_RANGE, C_END) => List((Some(a),None,UNTIL_RANGE))
          case (a, _, C_END) => List((Some(a),None,TO_RANGE))
          case (a, expr, b) if b.isInstanceOf[Int] =>
            expr.toLowerCase match {
              case TO_RANGE => List((Some(a),Some(b.asInstanceOf[Int]),TO_RANGE))
              case UNTIL_RANGE => List((Some(a),Some(b.asInstanceOf[Int]-1),TO_RANGE))
            }
        }
      case false =>
        List((Some(left),Some(left),TO_RANGE))
    }
//    if(mid.isDefined && right.isDefined) {
//      (left, mid.get.toLowerCase, right.get) match {
//        case (a, UNTIL_RANGE, C_END) => List((Some(a),None,UNTIL_RANGE))
//        case (a, _, C_END) => List((Some(a),None,TO_RANGE))
//        case (a, expr, b) if b.isInstanceOf[Int] =>
//          expr.toLowerCase match {
//            case TO_RANGE => List((Some(a),Some(b.asInstanceOf[Int]),TO_RANGE))
//            case UNTIL_RANGE => List((Some(a),Some(b.asInstanceOf[Int]-1),TO_RANGE))
//          }
//      }
//    } else { //[#]
//      List((Some(left),Some(left),TO_RANGE))
//    }
  }

  /*private def executeMoreKeys(first: Statement, list: List[Statement], dotsList: List[String]): bsonValue.BsValue = {
    val keyList: (List[(String, String)], List[(Option[Int], Option[Int], String)]) = buildKeyList(first, list, dotsList)
    //println("after build keylist -> " + keyList._1)
    //println("after build limitlist -> " + keyList._2)
    val result: Seq[Any] =
      boson.extract(boson.getByteBuf, keyList._1, keyList._2) map { v =>
             v.asInstanceOf[Seq[Any]]
      } getOrElse Seq.empty[Any]
    result match {
      case Seq() => bsonValue.BsObject.toBson(Vector.empty[Any])
      case v => bsonValue.BsObject.toBson {
        (for (elem <- v) yield {
          elem match {
            case e: Array[Any] =>
              Compose.composer(e)
            case e => e
          }
        }).toVector
      }
    }
  }*/

  private def startInjector(statement: List[Statement]): Array[Byte] = {
    val stat: MoreKeys = statement.head.asInstanceOf[MoreKeys]
    val united: List[Statement] = stat.list.+:(stat.first)
    val zipped: List[(Statement, String)] =
      if(stat.first.isInstanceOf[ROOT]){
        united.map(e => (e, C_DOT))
      }else{
       united.zip(stat.dotList)
      }
    println(zipped)
    executeMultipleKeysInjector(zipped)
  }

  private def executeMultipleKeysInjector(statements: List[(Statement, String)]): Array[Byte] = {
    val result: Array[Byte]=
      Try(boson.execStatementPatternMatch(boson.getByteBuf, statements, fInj.get ))match{
        case Success(v)=>
          //val bsResult: bsonValue.BsValue = bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
          //v.release()
          //bsResult
          v.array
        case Failure(e)=>
          throw CustomException(e.getMessage)
          //bsonValue.BsException(e.getMessage)
          }
    boson.getByteBuf.release()
    result
  }
}

object Compose {
  //import org.specs2.mutable.Specification

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
