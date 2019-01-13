package humid.actor

import akka.actor.{Actor, ActorRef}
import humid._

import scala.collection.mutable

class LeadersReportConcentrator extends Actor {

  private var dirProcessor: ActorRef = _

  private val sensorReports = mutable.Map.empty[String, SensorReport]

  override def receive: Receive = awaitDirProcessor

  private def awaitDirProcessor: Receive = {
    case dirProcessorRef: ActorRef =>
      dirProcessor = dirProcessorRef
      context.become(active)
  }

  def active: Receive = {
    case Concentrate(leadersReport) =>
      leadersReport.sensorReports.foreach { sensorReport =>
        val currentSensorReport = sensorReports.getOrElse(sensorReport.sensorId, SensorReport(sensorReport.sensorId, 0, 0, None))
        val updatedSensorReport = currentSensorReport.mergeWith(sensorReport)
        sensorReports.put(sensorReport.sensorId, updatedSensorReport)
      }

      dirProcessor ! ReportFileProcessed(leadersReport.reportFile)

    case ProvideStatistics =>
      dirProcessor ! Statistics(sensorReports.values.toList)
  }
}
