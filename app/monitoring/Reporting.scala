package monitoring

import play.api.libs.concurrent.Promise
import java.util.concurrent.TimeUnit
import scala.concurrent.stm._
import java.lang.management.ManagementFactory
import play.api.libs.iteratee.{Enumeratee, Concurrent, Enumerator}
import play.api.libs.json._

object Reporting {

    var loadAvg = 0f
    var nbThreads = 0
    var memory = 0l
    var cpu = 0

    val osStats = ManagementFactory.getOperatingSystemMXBean
    val threadStats = ManagementFactory.getThreadMXBean
    val memoryStats = ManagementFactory.getMemoryMXBean
    val cpuStats = new CPU()

    val toEventSource = Enumeratee.map[String] { msg => "data: " + msg + "\n\n" }

    val monitoringEnumerator = Enumerator.fromCallback{ () =>
        Promise.timeout( Some( data() ), 1000, TimeUnit.MILLISECONDS )
    }

    def update() = {
        loadAvg = osStats.getSystemLoadAverage.toFloat
        nbThreads = threadStats.getThreadCount
        memory = memoryStats.getHeapMemoryUsage.getUsed / 1024 / 1024
        cpu = ((cpuStats.getCpuUsage() * 1000).round / 10.0).toInt
    }

    def data() = {
        update()
        Json.stringify(
            JsObject(
                List(
                    "thread" -> JsNumber( nbThreads ),
                    "load" -> JsString( loadAvg.toString.replace("0.", ".") ),
                    "mem" -> JsNumber( memory ),
                    "cpu" -> JsNumber( cpu )
                )
            )
        )
    }
}
