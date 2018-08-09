package benchmark

object  IncrementValue extends Metered {

  val WarmUpRuns = 100
  val MeasuredRuns = 1000

  override def startUp: Unit = {}
  override def shutDown: Unit = {}


  val stats = measure {
    Thread.sleep(10)
  }

  println(s"Avg :${stats.mean}")
}
