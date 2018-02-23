package benchmark


import bsonLib.BsonObject
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import io.zink.boson.bson.bsonImpl.BosonImpl
import io.zink.boson.bson.bsonPath.{Interpreter, Program, TinyLanguage}
import io.zink.boson.bson.bsonValue.{BsObject, BsValue}
import org.scalameter._

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
object PerformanceTest extends App {

  def callParse(boson: BosonImpl, expression: String): BsValue = {
    val parser = new TinyLanguage
    try {
      parser.parseAll(parser.program, expression) match {
        case parser.Success(r, _) =>
          val interpreter = new Interpreter(boson, r.asInstanceOf[Program])
          interpreter.run()
        case parser.Error(_, _) => BsObject.toBson("Error parsing!")
        case parser.Failure(_, _) => BsObject.toBson("Failure parsing!")
      }
    } catch {
      case e: RuntimeException => BsObject.toBson(e.getMessage)
    }
  }

  def run() = {
    val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
    val finale: String = bufferedSource.getLines.toSeq.head
    bufferedSource.close

    val json: JsonObject = new JsonObject(finale)
    val bson: BsonObject = new BsonObject(json)

    //val boson: BosonImpl = sI.createBoson(bson.encode().getBytes)
    val boson: BosonImpl = new BosonImpl(byteArray = Option(bson.encodeToBarray()))

    def bestTimeMeasure[R](block: => R): Quantity[Double] = {
      val time = config(
        Key.exec.benchRuns -> 50
      ) withWarmer {
        new Warmer.Default
      } measure {
        block
      }
      time
    }

    /**
      * Testing performance of extracting a top value of a BsonObject
      */
    val result1 = bestTimeMeasure {
      callParse(boson.duplicate, "Epoch.first")
    }
    println()
    //println("result1: " + callParse(boson.duplicate, "Epoch.first").asInstanceOf[BsSeq].getValue.head)
    println(s"Benchmark for this test: $result1")
    //println()


    /**
      * Testing performance of extracting a bottom value of a BsonObject
      */
    val result2 = bestTimeMeasure {
      callParse(boson.duplicate, "SSLNLastName.last")
    }
    println()
//    println("result2: " + new String(callParse(boson.duplicate, "SSLNLastName.last")
//      .asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]))
    println(s"Benchmark for this test: $result2")
    //println()

    /**
      * Testing performance of extracting all 'Tags' values
      */
    val result3 = bestTimeMeasure {
      callParse(boson.duplicate, "Tags.all")
    }
    println()
//    println("result3: " + callParse(boson.duplicate, "Tags.all").asInstanceOf[BsSeq].getValue)
    println(s"Benchmark for this test: $result3")
    //println()

    /**
      * Testing performance of extracting values of some positions of a BsonArray
      */
    val result4 = bestTimeMeasure {
      callParse(boson.duplicate, "Markets.[3 to 5]")
    }
    println()
//    callParse(boson.duplicate, "Markets.[3 to 5]").asInstanceOf[BsSeq]
//      .getValue.head.asInstanceOf[Seq[Any]].foreach(elem => println(s"result4: $elem"))
    println(s"Benchmark for this test: $result4")
    println()

    /**
      *  Testing performance of extracting with two keys
      * */
    val result5 = bestTimeMeasure {
      callParse(boson.duplicate, "Markets.[10].selectiongroupid")
    }
    println()
    //println("result5: " + callParse(boson.duplicate, "Markets.[50 to 55]")
      //.asInstanceOf[BsSeq].getValue.head)
    println(s"Benchmark for this test: $result5")
    //println()
  }
}

object tester extends App {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  try {
    for {_ <- 0 until 1} {
      //println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
      PerformanceTest.run()
    }
  } catch {
    case e: Exception =>
      println(e)
  }
}


