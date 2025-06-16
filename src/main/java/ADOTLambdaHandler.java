import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

@SuppressWarnings("unused") // AWS Lambda uses this handler
public class ADOTLambdaHandler implements RequestStreamHandler {

    // Using SLF4J for logging (imported as a transitive dependency from logback-classic)
    Logger logger = LoggerFactory.getLogger(ADOTLambdaHandler.class);

    @Override
    public void handleRequest(final InputStream inputStream, final OutputStream outputStream, final Context context) {
        try {
            logger.trace("Lambda function received request");

            // Simulate doing <stuff> with a random delay
            var delay = new Random().nextInt(1000, 6000);
            logger.info("Sleeping for {} seconds to simulate doing things and stuff", delay / 1000.0);
            Thread.sleep(delay);
            logger.info("Finished doing things and stuff");

            logger.trace("Lambda function completed successfully");
        }
        catch (InterruptedException e) {
            logger.warn("Lambda function was interrupted");
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
