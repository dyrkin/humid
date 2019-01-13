package humid.actor

import akka.actor.{Actor, ActorRef, PoisonPill}
import akka.event.Logging
import humid.{LeadersReportFileIterator, _}

import scala.collection.mutable

class LeadersReportFileProcessor extends Actor {

  private val log = Logging(context.system, this)

  private var concentrator: ActorRef = _

  override def receive: Receive = awaitConcentrator

  private def awaitConcentrator: Receive = {
    case concentratorRef: ActorRef =>
      concentrator = concentratorRef
      context.become(awaitTask)
  }

  private def awaitTask: Receive = {
    case ProcessReportFile(reportFile) =>
      log.info("Received request to process report")
      val humidityStatsCsvIterator = new LeadersReportFileIterator(reportFile)
      val sensorReports = mutable.Map.empty[String, SensorReport]

      var records = 0L

      humidityStatsCsvIterator.foreach { measurement =>
        val sensorReport = sensorReports.getOrElse(measurement.sensorId, SensorReport(measurement.sensorId, 0, 0, None))
        val updatedSensorReport = sensorReport.appendMeasurement(measurement.humidity)
        sensorReports.put(updatedSensorReport.sensorId, updatedSensorReport)
        records = records + 1
        if (records % 100000 == 0) {
          log.info(s"$records records processed")
        }
      }

      humidityStatsCsvIterator.close()

      log.info(s"$records records processed")

      concentrator ! Concentrate(LeadersReport(reportFile, sensorReports.values.toList))

      log.info(s"Report has been processed and contains measurements from ${sensorReports.size} sensors")
      //I'm done. Bye
      self ! PoisonPill
  }
}
