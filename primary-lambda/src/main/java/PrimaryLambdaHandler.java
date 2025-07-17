import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class PrimaryLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(PrimaryLambdaHandler.class);

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

            logger.trace("Primary Lambda function received request");
            try (var lambdaClient = LambdaClient.create()) {
                var secondaryLambdaVersion = System.getProperty("aws.secondary.lambda.version", System.getenv("AWS_SECONDARY_LAMBDA_VERSION"));
                var invokeRequest = InvokeRequest
                        .builder()
                        .functionName("secondary-lambda-" + secondaryLambdaVersion)
                        .invocationType(InvocationType.REQUEST_RESPONSE)
                        .payload(SdkBytes.fromInputStream(inputStream))
                        .build();

                logger.info("Invoking Secondary Lambda");

                var invokeResponse = lambdaClient.invoke(invokeRequest);

                logger.info("Response Status from Secondary Lambda: {}", invokeResponse.statusCode());

                var responseJson = invokeResponse.payload().asUtf8String();

                logger.info("Response JSON: {}", responseJson);

                outputStream.write(responseJson.getBytes(StandardCharsets.UTF_8));
            }
            catch (IOException e) {
                logger.error("IO Exception", e);
                throw new RuntimeException(e);
            }
        }
        finally {
            span.end();
            MDC.clear();
        }
    }
}