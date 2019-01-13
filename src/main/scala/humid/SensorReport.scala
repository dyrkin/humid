package humid

case class SensorReport(sensorId: String,
                        processed: Long,
                        failed: Long,
                        measurementsSnapshot: Option[SensorMeasurementsSnapshot]
                       ) extends Ordered[SensorReport] {

  override def compare(that: SensorReport): Int = {
    measurementsSnapshot.map { thisSnapshot =>
      that.measurementsSnapshot.map(thatSnapshot => thatSnapshot.avg.compare(thisSnapshot.avg)).getOrElse(-1)
    }.getOrElse(1)
  }
}

object SensorReport {

  implicit class Ops(report: SensorReport) {
    def mergeWith(anotherReport: SensorReport): SensorReport = {
      if (report.sensorId != anotherReport.sensorId) {
        sys.error("Can't merge stats with different sensor ids")
      }
      val merged = report.measurementsSnapshot.flatMap { valuesLeft =>
        anotherReport.measurementsSnapshot.map { valuesRight =>
          val min = Math.min(valuesLeft.min, valuesRight.min)
          val max = Math.max(valuesLeft.max, valuesRight.max)
          val count = valuesLeft.count + valuesRight.count
          val avg = (valuesLeft.avg * valuesLeft.count + valuesRight.avg * valuesRight.count) / count
          report.copy(measurementsSnapshot = Some(SensorMeasurementsSnapshot(min, max, avg, count)))
        }
      }.getOrElse(anotherReport)

      merged.copy(processed = report.processed + anotherReport.processed, failed = report.failed + anotherReport.failed)
    }

    def appendMeasurement(humidityMaybe: Option[Int]): SensorReport = {
      val appended = humidityMaybe match {
        case Some(humidity) =>
          report.measurementsSnapshot.map { snapshot =>
            val min = Math.min(humidity, snapshot.min)
            val max = Math.max(humidity, snapshot.max)
            val count = snapshot.count + 1
            val avg = (snapshot.avg * snapshot.count + humidity) / count
            report.copy(measurementsSnapshot = Some(SensorMeasurementsSnapshot(min, max, avg, count)))
          }.getOrElse {
            report.copy(measurementsSnapshot = Some(SensorMeasurementsSnapshot(humidity, humidity, humidity, 1)))
          }
        case None =>
          report.copy(failed = report.failed + 1)
      }
      appended.copy(processed = appended.processed + 1)
    }

    def asString: String = {
      report.measurementsSnapshot match {
        case Some(snapshot) =>
          s"${report.sensorId},${snapshot.min},${snapshot.avg.toInt},${snapshot.max}"
        case None =>
          s"${report.sensorId},NaN,NaN,NaN"
      }
    }
  }

}
