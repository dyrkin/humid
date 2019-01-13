import java.io.File

package object humid {

  case class ProcessReportFile(reportFile: File)

  case class ReportFileProcessed(reportFile: File)

  case class ProcessReportDir(reportDir: File)

  case class ReportDirProcessed(reportDir: File)

  case class SensorMeasurementsSnapshot(min: Int, max: Int, avg: BigDecimal, count: Long)

  case class Concentrate(leadersReport: LeadersReport)

  case class LeadersReport(reportFile: File, sensorReports: List[SensorReport])

  case object ProvideStatistics

  case class Statistics(sensorStatistics: List[SensorReport])

  case class Measurement(sensorId: String, humidity: Option[Int])

}
