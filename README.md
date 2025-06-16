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