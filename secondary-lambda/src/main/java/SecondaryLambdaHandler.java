import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.MDC;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("unused")
public class SecondaryLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(SecondaryLambdaHandler.class);

    @Override
    public void handleRequest(final InputStream inputStream, final OutputStream outputStream, final Context context) {
        var span = Span.current();
        try {
            // add open telemetry span context to MDC, assuming said context is valid
            var spanCtx = span.getSpanContext();
            if (spanCtx.isValid()) {
                MDC.put("traceId", spanCtx.getTraceId());
                MDC.put("spanId", spanCtx.getSpanId());
            }
            // Setting request id in MDC like this does not help correlate logs w/ traces as hoped, but we'll keep it
            // here for the sake of completeness. It may come in handy in the future.
            // MDC.put("requestId", context.getAwsRequestId());

            logger.trace("Lambda function received request");
            int delay = new Random().nextInt(1000, 6000);
            logger.info("Sleeping for {} seconds to simulate doing things", Optional.of(delay / 1000.0));
            Thread.sleep(delay);
            logger.info("Finished doing things and stuff");
            logger.trace("Lambda function completed successfully");
        } catch (InterruptedException e) {
            logger.warn("Lambda function was interrupted");
            logger.error("InterruptedException", e);
            throw new RuntimeException(e);
        } finally {
            span.end();
            MDC.clear();
        }
    }
}