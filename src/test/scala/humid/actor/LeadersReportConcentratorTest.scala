package humid.actor

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import humid._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class LeadersReportConcentratorTest extends TestKit(ActorSystem()) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  "LeadersReportConcentrator" when {

    "ProvideStatistics received" must {
      "respond with complete statistics" in {
        val dirProcessor = TestProbe()

        val concentrator = system.actorOf(Props[LeadersReportConcentrator])

        concentrator ! dirProcessor.ref

        val reportFile = new File("")
        val sensorReport = createSensorReport(1, 1, 1, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))
        val leadersReport = LeadersReport(reportFile, List(sensorReport))
        val expectedStatistics = Statistics(leadersReport.sensorReports)

        concentrator ! Concentrate(leadersReport)

        dirProcessor.expectMsg(ReportFileProcessed(reportFile))

        concentrator ! ProvideStatistics

        dirProcessor.expectMsg(expectedStatistics)
      }
    }

    "2 Concentrate messages received" must {
      "aggregate their values" in {
        val dirProcessor = TestProbe()

        val concentrator = system.actorOf(Props[LeadersReportConcentrator])

        concentrator ! dirProcessor.ref

        val reportFile = new File("")
        val sensorReport1 = createSensorReport(1, 1, 0, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))
        val sensorReport2 = createSensorReport(1, 2, 0, Some(SensorMeasurementsSnapshot(3, 5, 4, 2)))
        val leadersReport1 = LeadersReport(reportFile, List(sensorReport1))
        val leadersReport2 = LeadersReport(reportFile, List(sensorReport2))
        val expectedStatistics = Statistics(List(sensorReport1.mergeWith(sensorReport2)))

        concentrator ! Concentrate(leadersReport1)

        dirProcessor.expectMsg(ReportFileProcessed(reportFile))

        concentrator ! Concentrate(leadersReport2)

        dirProcessor.expectMsg(ReportFileProcessed(reportFile))

        concentrator ! ProvideStatistics

        dirProcessor.expectMsg(expectedStatistics)
      }
    }
  }

  private def createSensorReport(id: Int, processed: Long, failed: Long, snapshot: Option[SensorMeasurementsSnapshot]) = {
    SensorReport(s"s$id", processed, failed, snapshot)
  }
}
