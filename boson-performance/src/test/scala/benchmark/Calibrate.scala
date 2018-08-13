package benchmark

object  Calibrate extends Metered {

  val WarmUpRuns = 5
  val MeasuredRuns = 10

  override def startUp: Unit = {}
  override def shutDown: Unit = {}

  val stats = measure {
    Thread.sleep(10)
  }

  println(s"Avg :${stats.mean} StandDev :${stats.standardDev}")
}
