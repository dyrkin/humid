package humid

import java.io.File

import akka.actor.{ActorSystem, Props}
import humid.actor.LeadersReportDirProcessor

object Boot extends App {

  args.headOption match {
    case Some(path) =>
      val file = new File(path)

      if (file.isDirectory) {
        processDirectory(file)
      } else {
        println(s"$file is not a directory. Please provide correct path")
      }

    case None =>
      println("Please provide path to directory")
  }

  private def processDirectory(directory: File): Unit = {
    val system = ActorSystem()
    val statisticsPrinter = new StatisticsPrinter
    val dirProcessor = system.
      actorOf(Props(classOf[LeadersReportDirProcessor], statisticsPrinter), "dir-processor")

    dirProcessor ! ProcessReportDir(directory)
  }
}
