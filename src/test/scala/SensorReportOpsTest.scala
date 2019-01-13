import humid.SensorReport._
import humid.{SensorMeasurementsSnapshot, SensorReport}
import org.scalatest.WordSpecLike

class SensorReportOpsTest extends WordSpecLike {

  "mergeWith" when {
    "proper data received" must {
      "aggregate values" in {
        val sensorReport1 = SensorReport("s1", 1, 0, Some(SensorMeasurementsSnapshot(7, 7, 7, 1)))

        val sensorReport2 = SensorReport("s1", 2, 0, Some(SensorMeasurementsSnapshot(3, 5, 4, 2)))

        val expectedSensorReport = SensorReport("s1", 3, 0, Some(SensorMeasurementsSnapshot(3, 7, 5, 3)))

        val actualSensorReport = sensorReport1.mergeWith(sensorReport2)

        assertResult(expectedSensorReport)(actualSensorReport)
      }
    }

    "incorrect sensorId received" must {
      "produce error" in {
        val sensorReport1 = SensorReport("s1", 1, 0, Some(SensorMeasurementsSnapshot(7, 7, 7, 1)))

        val sensorReport2 = SensorReport("s2", 2, 0, Some(SensorMeasurementsSnapshot(3, 5, 4, 2)))

        assertThrows[RuntimeException](
          sensorReport1.mergeWith(sensorReport2)
        )
      }
    }
  }

  "appendMeasurement" when {
    "snapshot non empty" when {
      "non empty measurement received" must {
        "append value to snapshot and increment processed counter" in {
          val sensorReport1 = SensorReport("s1", 1, 0, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))

          val expectedSensorReport = SensorReport("s1", 2, 0, Some(SensorMeasurementsSnapshot(1, 3, 2, 2)))

          val actualSensorReport = sensorReport1.appendMeasurement(Some(3))

          assertResult(expectedSensorReport)(actualSensorReport)
        }
      }

      "empty measurement received" must {
        "increment processed and failed counters" in {
          val sensorReport1 = SensorReport("s1", 1, 0, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))

          val expectedSensorReport = SensorReport("s1", 2, 1, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))

          val actualSensorReport = sensorReport1.appendMeasurement(None)

          assertResult(expectedSensorReport)(actualSensorReport)
        }
      }
    }

    "snapshot is empty" when {
      "non empty measurement received" must {
        "initialize snapshot and increment processed counter" in {
          val sensorReport1 = SensorReport("s1", 0, 0, None)

          val expectedSensorReport = SensorReport("s1", 1, 0, Some(SensorMeasurementsSnapshot(1, 1, 1, 1)))

          val actualSensorReport = sensorReport1.appendMeasurement(Some(1))

          assertResult(expectedSensorReport)(actualSensorReport)
        }
      }

      "empty measurement received" must {
        "increment processed and failed counters" in {
          val sensorReport1 = SensorReport("s1", 0, 0, None)

          val expectedSensorReport = SensorReport("s1", 1, 1, None)

          val actualSensorReport = sensorReport1.appendMeasurement(None)

          assertResult(expectedSensorReport)(actualSensorReport)
        }
      }
    }
  }
}
