package benchmark

object  IncrementValue extends Metered {

  val WarmUpRuns = 5
  val MeasuredRuns = 10

  override def startUp: Unit = {}
  override def shutDown: Unit = {}


  val stats = measure {
    // TODO Increment a value in a long JSON string
  }

  println(s"Avg :${stats.mean}  StandDev :${stats.standardDev}")

}
