import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import io.opentelemetry.api.trace.Tracer
import org.apache.logging.log4j.{LogManager, Logger}

object TestOpenTelemetry extends App {

  class PoisonPillExample extends Actor {

    def receive: Receive = {
      case "start" =>
        log.info("Actor started")
      case "process" =>
        val currentSpan = tracer.spanBuilder("MainSpan").startSpan()
        currentSpan.setAttribute("myKey", "string1")
        log.error(s"Error Message Log [dt.trace_id=${currentSpan.getSpanContext.getTraceId},dt.span_id=${currentSpan.getSpanContext.getSpanId}]")
        currentSpan.end()
      case "stop" =>
        self ! PoisonPill
    }
  }

  val tracer: Tracer = OpentelemetrySetup.setup().getTracer("TraceSpanInstrumentation")
  val log: Logger = LogManager.getLogger(getClass)

  // Your application code here...
  val system = ActorSystem("MySystem")
  val actor = system.actorOf(Props[PoisonPillExample], "PoisonPillExample")

  actor ! "start"
  for (_ <- 1 to 100) {
    actor ! "process"
    Thread.sleep(10000)
  }
  actor ! "stop"

}
