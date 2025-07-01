import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.slf4j.MDC;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

@SuppressWarnings("unused")
public class ADOTLambdaHandler implements RequestStreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(ADOTLambdaHandler.class);

    @Override
    public void handleRequest(final InputStream inputStream, final OutputStream outputStream, final Context context) {
        try {
            MDC.put("requestId", context.getAwsRequestId());
            logger.trace("Lambda function received request");
            int delay = new Random().nextInt(1000, 6000);
            logger.info("Sleeping for {} seconds to simulate doing things", delay / 1000.0);
            Thread.sleep(delay);
            logger.info("Finished doing things and stuff");
            logger.trace("Lambda function completed successfully");
        } catch (InterruptedException e) {
            logger.warn("Lambda function was interrupted");
            logger.error("Exception: ", e);
            throw new RuntimeException(e);
        } finally {
            MDC.clear();
        }
    }
}