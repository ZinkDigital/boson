package benchmark


case class Stats(val times : Seq[Long]) {

  private val meanNanos = times.sum.toDouble / times.size.toDouble
  private val squaredDiffNanos = times.map( v => (v - meanNanos) * (v - meanNanos) )
  private val varianceNanos : Double = squaredDiffNanos.sum / squaredDiffNanos.size.toDouble


  def millisConversion(nanos : Double) : Double = nanos / 1000000
  def slowest : Double = millisConversion(times max )
  def fastest : Double = millisConversion(times min )
  def mean    : Double = millisConversion(meanNanos )
  def standardDev = millisConversion( Math.sqrt(varianceNanos))


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
    // Deliberately flattened
    val s1 = System.nanoTime
    val s2 = System.nanoTime
    val s3 = System.nanoTime
    val s4 = System.nanoTime
    val s5 = System.nanoTime
    val s6 = System.nanoTime
    val s7 = System.nanoTime
    val s8 = System.nanoTime
    val diffs = List(s1,s2,s3,s4,s5,s6,s7,s8).sliding(2).map( l => l.last - l.head).toList
    val mean = (diffs.sum)  / (diffs.size)
    mean
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
