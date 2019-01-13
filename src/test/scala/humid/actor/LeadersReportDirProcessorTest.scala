package humid.actor

import java.io.File

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import humid._
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class LeadersReportDirProcessorTest extends TestKit(ActorSystem()) with ImplicitSender with WordSpecLike with BeforeAndAfterAll {
  "LeadersReportDirProcessor" when {

    "received ProcessReportDir message" must {
      "print final statistics" in {

        val printer = mock(classOf[StatisticsPrinter])

        val dirProcessor = system.actorOf(Props(classOf[LeadersReportDirProcessor], printer))

        val reportDir = new File(getClass.getResource("/input").getFile)

        dirProcessor ! ProcessReportDir(reportDir)

        val expectedStatistics = List(
          SensorReport("s2", 3, 0, Some(SensorMeasurementsSnapshot(78, 88, 82, 3))),
          SensorReport("s1", 3, 1, Some(SensorMeasurementsSnapshot(10, 98, 54, 2))),
          SensorReport("s3", 1, 1, None))

        Await.ready(system.whenTerminated, 10.seconds)

        verify(printer).printStats(expectedStatistics, 2)

      }
    }
  }
}
