package benchmark


case class Stats(val times : Seq[Long]) {
  def millisConversion(nanos : Double) : Double = nanos / 1000000
  def slowest : Double = millisConversion(times max )
  def fastest : Double = millisConversion(times min )
  def mean    : Double = millisConversion(times.sum.toDouble / times.size.toDouble )
}

abstract class Metered extends App {

  def WarmUpRuns : Int
  def MeasuredRuns : Int

  def startUp
  def shutDown


  private[Metered] def warmup[R](block : => R) : Unit = {
    var testRuns: Long = 0
    while (testRuns < WarmUpRuns) {
      block
      testRuns += 1
    }
    Thread.sleep(100);
  }


  private[Metered] def measureOverhead(): Long = {
    val start = System.nanoTime()
    val end = System.nanoTime()
    end - start
  }


  private[Metered] def measureTimes[R](block : => R, overhead : Long = 0) : Seq[Long] = {

    import scala.collection.mutable.ArrayBuffer
    val times = new ArrayBuffer[Long](MeasuredRuns)

    var testRuns: Long = 0
    while (testRuns < MeasuredRuns) {
      val start = System.nanoTime()
      block
      val end = System.nanoTime()
      times += (end - start - overhead)
      testRuns += 1
    }
    times
  }


  def measure [R](block : => R) : Stats = {
    startUp
    warmup(block)
    val times = measureTimes( block , measureOverhead)
    shutDown
    Stats(times)
  }

}
