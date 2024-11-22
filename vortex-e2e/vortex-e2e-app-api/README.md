# vortex-e2e-app-api

# Getting Started
This module contains end-2-end test cases for vortex API server.

The project uses:
1. KarateLab (https://www.karatelabs.io/)

## Run the test
#### Locally
If you want to run it locally, please replace some variables in karate-config.js.

```bash 
./mvnw test
# or run `mvn test`
# can specify environment and tags, like 
# ./mvnw test -Dkarate.env=dev -Dkarate.options="--tags @Debug"

```

If you are using IntelliJ Idea, you can invoke tests from the IDE, which is convenient when in development mode.
Run 'com.consoleconnect.vortex.e2e.E2ETest.java' to run all tests or run specific runner to run specific tests.

## Check the test result

#### Locally
HTML reports are built-in in Karate, you can view the HTML report after testing completed.

Path: target/karate-reports/karate-summary.html