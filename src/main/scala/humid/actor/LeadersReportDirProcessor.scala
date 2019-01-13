package humid.actor

import java.io.File

import akka.actor.{Actor, PoisonPill, Props}
import akka.event.Logging
import humid._

import scala.collection.mutable

class LeadersReportDirProcessor(statisticsPrinter: StatisticsPrinter) extends Actor {

  private val log = Logging(context.system, this)

  private val reportConcentrator = context.actorOf(Props[LeadersReportConcentrator], "concentrator")

  private val pendingReports = mutable.Set.empty[File]

  private var filesCount: Int = _

  override def preStart(): Unit = {
    reportConcentrator ! self
  }

  override def receive: Receive = {
    case ProcessReportDir(reportDir) =>
      val reportFiles = csvFiles(reportDir)
      filesCount = reportFiles.length
      reportFiles.foreach { reportFile =>
        pendingReports.add(reportFile)
        val reportFileProcessor = context.actorOf(Props[LeadersReportFileProcessor], reportFile.getName)
        reportFileProcessor ! reportConcentrator
        reportFileProcessor ! ProcessReportFile(reportFile)
      }

    case ReportFileProcessed(reportFile) =>
      pendingReports.remove(reportFile)
      log.info(s"Removed report file ${reportFile.getName} from pending list. Pending reports: ${pendingReports.size}")
      if (pendingReports.isEmpty) {
        log.info(s"Processing is done")
        reportConcentrator ! ProvideStatistics
      }

    case Statistics(sensorStatistics) =>
      log.info(s"Printing report")
      statisticsPrinter.printStats(sensorStatistics.sorted, filesCount)

      //Done. Bye bye
      reportConcentrator ! PoisonPill
      self ! PoisonPill
      context.system.terminate()
  }


  private def csvFiles(dir: File) = {
    dir.listFiles().filter(_.getName.endsWith(".csv"))
  }
}
