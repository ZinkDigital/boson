package benchmark

object  Calibrate extends Metered {

  val WarmUpRuns = 1000
  val MeasuredRuns = 10000

  override def startUp: Unit = {}
  override def shutDown: Unit = {}


  val stats = measure {
    Thread.sleep(10)
  }

  println(s"Avg :${stats.mean}")
}
