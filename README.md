# PoC - Java Lambda w/ ADOT Auto-Instrumentation
## ADOT (AWS Distribution for OpenTelemetry)

This is a proof of concept for using AWS Distro for OpenTelemetry (ADOT) with Java Lambda functions. The goal is to 
provide a minimal example, which demonstrates how to set up auto-instrumentation for Java AWS Lambdas.

AWS ADOT can be enabled for Lambda functions using a layer, which can be added to the function either manually or via 
CloudFormation. This example uses CloudFormation.

## Prerequisites
- AWS CLI (v2) must be installed and at least one SSO profile configured.

## Running locally

To run this example locally, you will first need to do one (possibly two) steps:

- Run `aws sso login` with whichever AWS account you intend to use. 
  - For non-default profiles: You will need to specify `--profile <profile_name>` as arg(s) in the CLI command.
  - For non-default profiles: Set the `AWS_PROFILE` environment variable to the profile you want to use.
- Run `mvn clean verify`

On running the above command, the following will happen:
- A S3 bucket will be created with the name (and region) specified by the corresponding properties in the pom file.
- The Lambda's packaged JAR file will be uploaded to this bucket.
- A CloudFormation stack will be created using this project's artifact id (also in the pom file) as the stack name.
  - The created stack will contain two resources — the LambdaFunction and its LambdaFunctionRole.
  - The LambdaFunction should contain an `aws-otel-java-agent-xyz` ADOT layer. 
    - `xyz` being the architecture (e.g. `amd64`) and version (e.g. `ver-1-32-0`) of the agent.

# What this PoC does

This project creates 2 Java Lambda functions in AWS - both of which are using an ADOT (AWS Distro for Open Telemetry) layer to enable sending telemetry directly to CloudWatch (without having to set up an Otel Collector). 
Both lambdas are created using CloudFormation, and deployed using Maven.

The primary lambda internally calls the secondary one in order to test whether or not logs from both lambdas are correctly being traced over multiple lambda calls.

> [!Note]
> It usually takes several minutes for CloudWatch to finish processing fresh telemetry information - do not expect traces to show up complete with all logs, etc. immediately upon function invokation!

### Example Trace:

<img width="1831" height="430" alt="image" src="https://github.com/user-attachments/assets/b6c03e03-c1d2-440a-aa9d-35e9d6bbc1c9" />

<img width="1875" height="738" alt="image" src="https://github.com/user-attachments/assets/2fa16c2a-9a10-4084-a40d-07a57c41bd37" />

<img width="1877" height="348" alt="image" src="https://github.com/user-attachments/assets/0e8a5397-62e9-4d78-88e6-266f4136fb47" />

## Notable Information - How to add this to your own Lambda(s)

The two most notable points which need attention in order to implement this own your own lambdas are located in the CloudFormation Template and the Handler class for a given lambda. In both cases the changes necessary are relatively minimal.

### CloudFormation Template

Within the CloudFormation, you will want to add one of the AWS-provided ADOT layers to the Resource.Properties block, as well as set Tracing to Active. It may also be necessary to set the `AWS_LAMBDA_EXEC_WRAPPER` environment variable.

<img width="971" height="725" alt="image" src="https://github.com/user-attachments/assets/abe361f9-745c-4459-aee4-2619e206fc6e" />

      Layers:
        # This is the Amazon Distribution of OpenTelemetry (ADOT) Layer for Java
        - arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-java-wrapper-amd64-ver-1-32-0:6

      Environment:
        Variables:
          AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-stream-handler # Used by the ADOT Layer to wrap the handler (wrapper layer)

      Tracing: Active # Active tracing must be enabled for the ADOT layer to work properly


### Lambda Handler

At the beginning of the code implementing the handler class - simply get your trace and span id's from the current span, and add those to your logger MDC:

Make sure you end the span and clear your MDC context at the end of the handler..? `// I'm not sure how necessary this step is`

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
    
                // Do stuff
            }
            finally {
                span.end();
                MDC.clear();
            }
        }
    }

If you aren't able to access `Span.current()` - you may need to import Open Telemetry into your pom file, in which case you should add the Open Telemetry bom to your DependencyManagement section, and the Open Telemetry API to your dependencies:

        <dependencyManagement>
          <dependencies>
            <dependency>
                <groupId>io.opentelemetry.instrumentation</groupId>
                <artifactId>opentelemetry-instrumentation-bom</artifactId>
                <version>2.16.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
          </dependencies>
        </dependencyManagement>

        <dependencies>
          <dependency>
              <groupId>io.opentelemetry</groupId>
              <artifactId>opentelemetry-api</artifactId>
          </dependency>
        </dependencies>



