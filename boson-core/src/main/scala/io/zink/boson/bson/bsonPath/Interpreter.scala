package io.zink.boson.bson.bsonPath

import java.time.Instant

import io.netty.buffer.{ByteBuf, Unpooled}
import io.zink.boson.bson.bsonImpl.Dictionary._
import io.zink.boson.bson.bsonImpl._
import shapeless._

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

/**
  * Created by Tiago Filipe on 02/11/2017.
  */


case class Book(title: String, edition: Int, forSale: Boolean)

class Interpreter[T](boson: BosonImpl, program: Program, fInj: Option[T => T] = None, fExt: Option[T => Unit] = None) {

  def run(): Any = {
    if (fInj.isDefined) startInjector(program.statement)
    else start(program.statement)
  }

  private def start(statement: List[Statement]): Any = {
    if (statement.nonEmpty) {
      statement.head match {
        case MoreKeys(first, list, dots) => extract(boson.getByteBuf, List(first) ++ list)
        case _ => throw new RuntimeException("Something went wrong!!!")
      }
    } else throw new RuntimeException("List of statements is empty.")
  }

  private def constructObj(encodedSeqByteArray: Seq[Array[Byte]], keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]): Seq[List[(String, Any)]] = {

    def toTuples(list: Iterable[Any]): List[(String, Any)] = {
      list match {
        case x: List[Any] if x.isEmpty => List()
        case x: List[Any] if x.lengthCompare(2) >= 0 => List((x.head.asInstanceOf[String], x.tail.head)) ++ toTuples(x.drop(2))
      }
    }

    encodedSeqByteArray.par.map { encodedByteArray =>
      val res: Iterable[Any] = runExtractors(Unpooled.copiedBuffer(encodedByteArray), keyList, limitList).asInstanceOf[Iterable[Iterable[Any]]].flatten
      val l: List[(String, Any)] = toTuples(res).map(elem => (elem._1.toLowerCase, elem._2))
      l
    }.seq
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
  private def buildExtractors(statementList: List[Statement]): (List[(String, String)], List[(Option[Int], Option[Int], String)]) = {
    //println(s"Generic of Type R: ${Generic[R]}")
    val forList: List[(List[(String, String)], List[(Option[Int], Option[Int], String)])] =
      for (statement <- statementList) yield {
        statement match {
          case Key(key) => (List((key, C_NEXT)), List((None, None, EMPTY_KEY)))
          case KeyWithArrExpr(key, arrEx) => (List((key, C_NEXT), (EMPTY_KEY, C_LIMITLEVEL)), List((None, None, EMPTY_KEY)) ++ defineLimits(arrEx.leftArg, arrEx.midArg, arrEx.rightArg))
          case ArrExpr(l, m, r) => (List((EMPTY_KEY, C_LIMITLEVEL)), defineLimits(l, m, r))
          case HalfName(halfName) => (List((halfName, C_NEXT)), List((None, None, EMPTY_KEY)))
          case HasElem(key, elem) => (List((key, C_LIMITLEVEL), (elem, C_FILTER)), List((None, None, EMPTY_KEY), (None, None, EMPTY_KEY)))
        }
      }
    val keyList: List[(String, String)] = forList.flatMap(elem => elem._1)
    val limitList: List[(Option[Int], Option[Int], String)] = forList.flatMap(elem => elem._2)

    val finalKeyList: List[(String, String)] =
      keyList.last._2 match {
        case C_NEXT => keyList.take(keyList.size - 1) ++ List((keyList.last._1, C_LEVEL))
        case _ => keyList
      }
    (finalKeyList, limitList)
    //extract(boson.getByteBuf, finalKeyList, limitList)
    //boson.getByteBuf.release()
    //runExtractors(boson.getByteBuf, finalKeyList, limitList)
  }

  private def extract(encodedStructure: ByteBuf, statementList: List[Statement]): Any = {
    val (keyList: List[(String, String)], limitList: List[(Option[Int], Option[Int], String)]) = buildExtractors(statementList)
    val result: Iterable[Any] = runExtractors(encodedStructure, keyList, limitList)
    println(s"extracted -> $result")
    val typeClass: Option[String] =
      result.size match {
        case 0 => None
        case 1 => Some(result.head.getClass.getSimpleName)
        case _ =>
          if (result.tail.forall { p => result.head.getClass.equals(p.getClass) }) Some(result.head.getClass.getSimpleName)
          else None
      }
    println(s"typeClass: $typeClass")
    applyFunction(result,limitList,typeClass)
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
        val res: Iterable[Any] = boson.extract(encodedStructure, keyList, limitList)
        //println(s"RES from last extractor: $res")
        res
      case _ =>
        val res: Iterable[Any] = boson.extract(encodedStructure, keyList, limitList)
        res.forall(e => e.isInstanceOf[Array[Byte]]) match {
          case true /*if keyList.head._1.equals(EMPTY_KEY)*/ =>
            val result: Iterable[Any] =
              res.asInstanceOf[Iterable[Array[Byte]]].par.map { elem =>
                val b: ByteBuf = Unpooled.buffer(elem.length).writeBytes(elem)
                runExtractors(b, keyList.drop(1), limitList.drop(1))
                //b.release()
              }.seq.reduce(_ ++ _)
            //println(s"RES from extractor: $result")
            result
          case false => throw CustomException("The given path doesn't correspond with the event structure.")
        }
    }
    value
  }

  //TODO: rethink a better strategy to veryfy if T and type of extracted are the same
  private def applyFunction(result: Iterable[Any],limitList: List[(Option[Int], Option[Int], String)], typeClass: Option[String]): Any = {
    if (returnInsideSeq(limitList)) {
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING if fExt.isDefined =>
            val res: Seq[String] = result.asInstanceOf[Iterable[String]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[String] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Instant] => Unit], res.map(elem => Instant.parse(elem)))) match {
                case Success(_) =>
                case Failure(_) =>
                  val extracted: Seq[Array[Byte]] = res.map(elem =>java.util.Base64.getDecoder.decode(elem))
                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], extracted)) match {
                    case Success(_) =>
                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
                  }
              }
            }
          case INTEGER if fExt.isDefined =>
            val res: Seq[Int] = result.asInstanceOf[Iterable[Int]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Int] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case LONG if fExt.isDefined =>
            val res: Seq[Long] = result.asInstanceOf[Iterable[Long]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Long] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case BOOLEAN if fExt.isDefined =>
            val res: Seq[Boolean] = result.asInstanceOf[Iterable[Boolean]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Boolean] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case DOUBLE if fExt.isDefined =>
            val res: Seq[Double] = result.asInstanceOf[Iterable[Double]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Double] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Float] => Unit], res.map(_.toFloat))) match {
                case Success(_) =>
                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
              }
            }
          case ARRAY_BYTE if fExt.isDefined =>
            val res = result.asInstanceOf[Iterable[Array[Byte]]].toSeq
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Seq[Array[Byte]] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: Seq[${typeClass.get}]")
            }
          case ARRAY_BYTE => constructObj(result.asInstanceOf[Seq[Array[Byte]]], List(("*", "build")), List((None, None, "")))
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    } else {
      if (typeClass.isDefined) {
        typeClass.get match {
          case STRING if fExt.isDefined =>
            val res: String = result.head.asInstanceOf[String]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[String => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Instant => Unit], Instant.parse(res))) match {
                case Success(_) =>
                case Failure(_) =>
                  val extracted: Array[Byte] = java.util.Base64.getDecoder.decode(res)
                  Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], extracted)) match {
                    case Success(_) =>
                    case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
                  }
              }
            }
          case INTEGER if fExt.isDefined =>
            val res: Int = result.head.asInstanceOf[Int]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Int => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case LONG if fExt.isDefined =>
            val res: Long = result.head.asInstanceOf[Long]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Long => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case BOOLEAN if fExt.isDefined =>
            val res: Boolean = result.head.asInstanceOf[Boolean]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Boolean => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case DOUBLE if fExt.isDefined =>
            val res: Double = result.head.asInstanceOf[Double]
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Double => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => Try(Transform.toPrimitive(fExt.get.asInstanceOf[Float => Unit], res.toFloat)) match {
                case Success(_) =>
                case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
              }
            }
          case ARRAY_BYTE if fExt.isDefined =>
            val res = result.asInstanceOf[Seq[Array[Byte]]].head
            Try(Transform.toPrimitive(fExt.get.asInstanceOf[Array[Byte] => Unit], res)) match {
              case Success(_) =>
              case Failure(_) => throw CustomException(s"Type designated doens't correspond with extracted type: ${typeClass.get}")
            }
          case ARRAY_BYTE => constructObj(result.asInstanceOf[Seq[Array[Byte]]], List(("*", "build")), List((None, None, "")))
        }
      } else fExt.get.apply(result.asInstanceOf[T]) //TODO: implement this case, when there aren't results
    }
  }

  private def defineLimits(left: Int, mid: Option[String], right: Option[Any]): List[(Option[Int], Option[Int], String)] = {
    mid.isDefined match {
      case true if right.isEmpty =>
        mid.get match {
          case C_FIRST => List((Some(0), Some(0), TO_RANGE))
          case C_ALL => List((Some(0), None, TO_RANGE))
          case C_END => List((Some(0), None, C_END))
        }
      case true if right.isDefined =>
        (left, mid.get.toLowerCase, right.get) match {
          case (a, UNTIL_RANGE, C_END) => List((Some(a), None, UNTIL_RANGE))
          case (a, _, C_END) => List((Some(a), None, TO_RANGE))
          case (a, expr, b) if b.isInstanceOf[Int] =>
            expr.toLowerCase match {
              case TO_RANGE => List((Some(a), Some(b.asInstanceOf[Int]), TO_RANGE))
              case UNTIL_RANGE => List((Some(a), Some(b.asInstanceOf[Int] - 1), TO_RANGE))
            }
        }
      case false =>
        List((Some(left), Some(left), TO_RANGE))
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

  private def startInjector(statement: List[Statement]): Array[Byte] = {
    val stat: MoreKeys = statement.head.asInstanceOf[MoreKeys]
    val united: List[Statement] = stat.list.+:(stat.first)
    val zipped: List[(Statement, String)] =
      if (stat.first.isInstanceOf[ROOT]) {
        united.map(e => (e, C_DOT))
      } else {
        united.zip(stat.dotList)
      }
    println(zipped)
    executeMultipleKeysInjector(zipped)
  }

  private def executeMultipleKeysInjector(statements: List[(Statement, String)]): Array[Byte] = {
    val result: Array[Byte] =
      Try(boson.execStatementPatternMatch(boson.getByteBuf, statements, fInj.get)) match {
        case Success(v) =>
          //val bsResult: bsonValue.BsValue = bsonValue.BsObject.toBson( new BosonImpl(byteArray = Option(v.array())))
          //v.release()
          //bsResult
          v.array
        case Failure(e) =>
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
