import java.io.FileWriter
import java.nio.file.Paths

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Random

object GenTestData extends App {

  def file(id: Int) = {
    Paths.get("/Users/{username}/Downloads/report2", s"leader-$id.csv").toFile
  }

  val maxFileSize: Long = 1024L * 1024 * 1024 * 4 //4 GiB, 4.29 GB

  val maxStringSize: Long = 1024L * 1024 * 4 //4 MiB

  def randId = Random.nextInt(9)

  def nanRange = (40 to 46).toSet

  def randValue = {
    val rand = Random.nextInt(99)
    if (nanRange.contains(rand)) "NaN"
    else rand.toString
  }

  val futures = (0 to 1).map { id =>
    Future {
      val f = file(id)
      var fileSize = 0L

      val w = new FileWriter(f)

      val header = "sensor-id,humidity\n"

      fileSize = fileSize + header.length

      w.write(header)

      while (fileSize < maxFileSize) {
        val builder = new StringBuilder()
        while (builder.length < maxStringSize) {
          val humidity = s"s$randId,$randValue\n"
          builder.append(humidity)
        }
        fileSize = fileSize + builder.length
        w.write(builder.toString())
      }

      w.close()
    }
  }

  Await.result(Future.sequence(futures), Duration.Inf)
}
