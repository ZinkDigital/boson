package benchmark

import bsonLib.BsonObject
import io.boson.bson.bsonImpl.BosonImpl
import io.boson.bson.bsonValue.{BsNumber, BsSeq}
import io.boson.scalaInterface.ScalaInterface
import io.netty.util.ResourceLeakDetector
import io.vertx.core.json.JsonObject
import org.scalameter._

import scala.io.Source

/**
  * Created by Tiago Filipe on 20/11/2017.
  */
object PerformanceTest extends App {
  def run() = {
    val sI: ScalaInterface = new ScalaInterface

    val bufferedSource: Source = Source.fromURL(getClass.getResource("/longJsonString.txt"))
    val finale: String = bufferedSource.getLines.toSeq.head
    bufferedSource.close

    val json: JsonObject = new JsonObject(finale)
    val bson: BsonObject = new BsonObject(json)

    val boson: BosonImpl = sI.createBoson(bson.encode().getBytes)

    def bestTimeMeasure[R](block: => R): Quantity[Double] = {
      val time = withWarmer(new Warmer.Default) measure {
        block
      }
      time
    }

    /**
      * Testing performance of extracting a top value of a BsonObject
      */
    val result1 = bestTimeMeasure {
      sI.parse(boson.duplicate, "Epoch.first")
    }
    println()
    println("result1: " + sI.parse(boson.duplicate, "Epoch.first").asInstanceOf[BsSeq].getValue.head)
    println(s"Benchmark for this test: $result1")
    println()


    /**
      * Testing performance of extracting a bottom value of a BsonObject
      */
    val result2 = bestTimeMeasure {
      sI.parse(boson.duplicate, "SSLNLastName.last")
    }
    println()
    println("result2: " + new String(sI.parse(boson.duplicate, "SSLNLastName.last")
      .asInstanceOf[BsSeq].value.head.asInstanceOf[Array[Byte]]))
    println(s"Benchmark for this test: $result2")
    println()

    /**
      * Testing performance of extracting all 'Tags' values
      */
    val result3 = bestTimeMeasure {
      sI.parse(boson.duplicate, "Tags.all")
    }
    println()
    println("result3: " + sI.parse(boson.duplicate, "Tags.all").asInstanceOf[BsSeq].getValue)
    println(s"Benchmark for this test: $result3")
    println()

    /**
      * Testing performance of extracting values of some positions of a BsonArray
      */
    val result4 = bestTimeMeasure {
      sI.parse(boson.duplicate, "Markets.[3 to 5]")
    }
    println()
    sI.parse(boson.duplicate, "Markets.[3 to 5]").asInstanceOf[BsSeq]
      .getValue.head.asInstanceOf[Seq[Any]].foreach(elem => println(s"result4: $elem"))
    println(s"Benchmark for this test: $result4")
    println()

    /**
      * Testing performance of extracting values of some positions of a BsonArray and selecting one
      */  // TODO:redo both tests
//    val result5 = bestTimeMeasure {
//      sI.parse(boson.duplicate, "Markets", "last [50 to 55]")
//    }
//    println()
//    println("result5: " + sI.parse(boson.duplicate, "Markets", "last [50 to 55]")
//      .asInstanceOf[BsSeq].getValue.head.asInstanceOf[Map[_, _]])
//    println(s"Benchmark for this test: $result5")
//    println()

    /**
      * Testing performance of getting size of all occurrences of a key
      */
//    val result6 = bestTimeMeasure {
//      sI.parse(boson.duplicate, "Price", "all size")
//    }
//    println()
//    println("result6: " + sI.parse(boson.duplicate, "Price", "all size").asInstanceOf[BsNumber].value)
//    println(s"Benchmark for this test: $result6")
//    println()
  }
}

object tester extends App {
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED)
  try {
    for {_ <- 0 until 100} {
      //println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
      PerformanceTest.run()
    }
  } catch {
    case e: Exception =>
      println(e)
  }
}


