import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;

public class ADOTLambdaHandlerTest {
    @Test
    public void testHandleRequest() {
        // Placeholder text - input is not currently used in any way, shape, or form (by the handler)
        var inputMessage = """
                {
                    "key1": "value1",
                    "key2": "value2"
                }
                """;
        var inputStream = new ByteArrayInputStream(inputMessage.getBytes(StandardCharsets.UTF_8));
        var outputStream = new ByteArrayOutputStream();
        var context = mock(Context.class);
        var handler = new ADOTLambdaHandler();
        handler.handleRequest(inputStream, outputStream, context);
    }
}
