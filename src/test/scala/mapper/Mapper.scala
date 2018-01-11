package mapper

import java.util

import bsonLib.{BsonArray, BsonObject}


import scala.collection.immutable.List
import scala.collection.mutable

/**
  * Created by Ricardo Martins on 08/01/2018.
  */
case class CustomException(smth:String) extends Exception {
  override def getMessage: String = smth
}

object Mapper {
  import scala.collection.JavaConverters._

  def convert(smth: Any): Any = {
    smth match {
      case bsonObject: BsonObject => convertBsonObject(bsonObject)
      case bsonArray: BsonArray => convertBsonArray(bsonArray)
      case _ => throw CustomException("No Type to Convert")
    }
  }

  def convertBsonObject(bsonObject: BsonObject): Map[String, Any] = {
    //val map: mutable.Set[(String, Any)] = mutable.Set.empty
    val map =  new mutable.TreeMap[String, Any]()


    val bsonMap: Map[String, AnyRef] = bsonObject.getMap.asScala.toMap//.asInstanceOf[Map[String, Any]]
    //val entries: util.Set[util.Map.Entry[String, AnyRef]] =bsonObject.getMap.entrySet()// bsonMap.toSet
    val entries: Set[(String, AnyRef)] = bsonMap.toSet
    entries.foreach(entry => {
      val key: String = entry._1// getKey//_1
      val value: Any = entry._2//getValue//_2

      value match {
        case bsonObject: BsonObject =>
          map.put(key, convertBsonObject(bsonObject))
        case bsonArray: BsonArray =>
          map.put(key, convertBsonArray(bsonArray))
        case _ =>
          map.put(key, value)
      }

    })
    map.toMap
  }

  def convertBsonArray(array: BsonArray): List[Any] = {
    //val list: util.List[Any] = new util.LinkedList[Any]()
    val list: mutable.ListBuffer[Any] = new mutable.ListBuffer[Any]
    val bsonList: List[Any] = array.getList.asScala.asInstanceOf[List[Any]]

    bsonList.foreach {
      case bsonObject: BsonObject =>
        list.append(convertBsonObject(bsonObject))
      case bsonArray: BsonArray =>
        list.append(convertBsonArray(bsonArray))
      case entry =>
        list.append(entry)
    }
    list.toList
  }

  def getAllTypes(any: Any, tabs: Int = 0): Any = {
    var tab: String = ""
    var t: Int = tabs
    while (t != 0){
      tab = tab.concat("\t")
      t=t-1
    }
    any match {
      case map: util.Map[_, _] =>
        map.entrySet().forEach( e => {
          val key: String =  e.getKey.asInstanceOf[String]
          val value: Any = e.getValue
          value match {
            case map1: util.Map[_, _] =>
              println(s"$tab$key -> ${map1.getClass.getSimpleName}")
              getAllTypes(map1, tabs + 1)
            case list1: util.List[_] =>
              println(s"$tab$key -> ${list1.getClass.getSimpleName}")
              getAllTypes(list1, tabs + 1)
            case null =>
              println(s"$tab$key -> ${"Null"}")
            case _ =>
              println(s"$tab$key -> ${value.getClass.getSimpleName}")
          }} )
      case list: util.List[_] =>
        var i = 0
        list.forEach {
          case map1: util.Map[_, _] =>
            println(s"$tab$i -> ${map1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(map1, tabs + 1)
          case list1: util.List[_] =>
            println(s"$tab$i -> ${list1.getClass.getSimpleName}")
            i = i + 1
            getAllTypes(list1, tabs + 1)
          case e: Any =>
            println(s"$tab$i -> ${e.getClass.getSimpleName}")
            i = i + 1
        }
      case _ => println("Wrong input type")
    }
  }
}
