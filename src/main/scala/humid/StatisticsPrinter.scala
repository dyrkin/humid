package humid

class StatisticsPrinter {
  def printStats(sensorStatistics: List[SensorReport], filesCount: Int): Unit = {
    val (processedMeasurements, failedMeasurements) = collectTotals(sensorStatistics)


    val completeReport =
      s"""
         |Num of processed files: $filesCount
         |Num of processed measurements: $processedMeasurements
         |Num of failed measurements: $failedMeasurements

         |Sensors with highest avg humidity:
         |
         |sensor-id,min,avg,max
         |${sensorStatistics.map(_.asString).mkString("\n")}
       """.stripMargin

    print(completeReport)
  }

  private def collectTotals(sensorStatistics: List[SensorReport]) = {
    sensorStatistics.foldRight(0L, 0L) { case (sensorReport, (processed, failed)) =>
      val p = processed + sensorReport.processed
      val f = failed + sensorReport.failed
      p -> f
    }
  }
}
