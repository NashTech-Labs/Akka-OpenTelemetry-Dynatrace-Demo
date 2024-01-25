import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes.SERVICE_NAME

import java.util.concurrent.TimeUnit

object OpentelemetrySetup {
  def setup(): OpenTelemetrySdk = {
    val resource = Resource.getDefault.merge(Resource.builder.put(SERVICE_NAME, "OtlpExporterExample").build)
    val openTelemetrySdk = OpenTelemetrySdk.builder
      .setTracerProvider(
        SdkTracerProvider.builder
          .setResource(resource)
          .addSpanProcessor(
            BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder.setTimeout(2, TimeUnit.SECONDS).build)
              .setScheduleDelay(100, TimeUnit.MILLISECONDS)
              .build
          )
          .build
      )
      .buildAndRegisterGlobal()

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      openTelemetrySdk.getSdkTracerProvider.shutdown()
    }))

    openTelemetrySdk
  }
}

