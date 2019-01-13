package humid

import java.io.File

import scala.io.Source

class LeadersReportFileIterator(csv: File) extends Iterator[Measurement] {
  private val source = Source.fromFile(csv, 4 * 1024 * 1024)

  //get lines and drop header
  private val lines = source.getLines().drop(1)

  override def hasNext: Boolean = lines.hasNext

  override def next(): Measurement = lineToMeasurement(lines.next())

  private def lineToMeasurement(line: String): Measurement = {
    val Array(sensorId, humidity) = line.split(",").map(_.trim)
    Measurement(sensorId, humidityRecordToOption(humidity))
  }

  private def humidityRecordToOption(humidity: String): Option[Int] =
    if (humidity.toLowerCase != "nan") Some(humidity.toInt) else None

  def close(): Unit = source.close()

}
