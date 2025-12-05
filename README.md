# 4156-TeamProject-2025 team The Java Beans
This is the shared repo for 4156 SWE Team Project Fall 2025. This README will provide documentation on the development 
and deployment of the Log Processing Service. This service involves a Spring Boot application that processes Apache log files, stores 
them in a database, and provides analytics and security endpoints.

## Building and Running a Local Instance
Make sure to run and install the following:
- Maven 3.9.5: https://maven.apache.org/download.cgi
    - Download and follow the installation instructions (preferably download the bin zip file or bin tar gz file)
    - Set the bin as described in Maven's README as a new path variable by editing the system variables on Windows
- JDK 17: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
    - Download JDK 17 to have the best compatibility with the code developed in the repo
- IntelliJ IDE: https://www.jetbrains.com/idea/download/?section=windows
    - You should use IntelliJ for optimal performance, but other IDEs like Visual Studio Code should be alright
- SQLite Database (required for JPA/Hibernate):
    - Already included in the `application.properties`. Database is created upon running service.
- Download the PMD Source Code Analyzer: https://pmd.github.io/
- Clone the repo using the command "git clone https://github.com/pauldavidarevalo/4156-TeamProject-2025.git"
- Go into the root folder and run the command "mvn compile" to compile the project with Maven
- You should then run the command "mvn spring-boot:run" to actually run the application
- You will then be able to make requests at 127.0.0.1:8080 or localhost:8080
- If you have issues making POST multipart requests, create a free Postman account here: https://www.postman.com/
- Download the Desktop Agent for Postman so that you can make requests locally
- Create a new workspace so that you can invoke different requests
- In order to populate the database, call /logs/upload with the sampleApacheSimple.log and suspiciousIps.log, both found in the sampleLogs folder
- You can then paste the URL of the endpoint you are invoking in the search bar and select the type of request made
- If you wish to run the style checker, you can run the command "mvn checkstyle:check" to see the code style results on the command terminal or the command "mvn checkstyle:checkstyle" if you wish to generate the report.
- Note: This project was run on a Windows computer, so the instructions might be a little different for Mac computers.

## Running Tests
The unit tests are located in the "src/test/java/dev/coms4156/project/logprocessor" directory and
cover many different branches for the LogEntry model, LogService, LogEntryRepository, controller classes. You should first
compile the project by going into the root directory and running the command "mvn compile" here. You
should then run the command "mvn clean test" to run the updated suite of tests. After running these tests, you should
check that all tests are successfully working (no assertion errors, missing imports, etc.). You can then look at the new
JaCoCo report by running the command "mvn jacoco:report" and going to the "src/target/site/jacoco" folder. You can then
look at the overall branch coverage by clicking index.html and opening it in a browser (Google Chrome works best).

## Testing endpoints from first iteration
To test in postman, open a workspace in postman and select Import -> Select File -> select "Log Analytics Service.postman_collection.json" in the top level of this repo.

## Client Program
The client program is located in src/main/java/dev/coms4155/client/LogProcessorClient.java. It is a standalone program that can be run anytime the service is deployed to the cloud. This client allows the user to upload any amount of new log files and then uses the service to compute analytics on these uploaded logs. Three main plots are generated using these analytics:
### HTTP Status Codes Count with Health Score
This plot displays a histogram of the frequency of different status codes from the uploaded logs. It also takes the statusCode response from the service and uses it to compute a health score -- a percentage of status codes that were 1xx-3xx out of the total status codes. This metrics is added to the plot
### Requests per Hour (highlight suspicious hours)
This plot combines the response from the timeseries/requests and security/suspicious-ips endpoints. It plots the frequency of requests binned by hour and highlights any hours in which a suspicious ip address -- one that results in more than 5 401 or 403 requests in an hour window -- in red. It also displays the suspicious ip address about the hour bar in which it was found. 
### Hourly Requests vs Errors
The last plot combines responses from timeseries/requests and timeseries/error-counts to generate two line plots of the number of requests within an hour in blue and the number of error requests (4xx or 5xx) within an hour. This plot uses both of the timeseries endpoints to help illustrate trends in the types of requests (successful vs error) in the logs.

## Endpoints
This summarizes the endpoints from LogController, AnalyticsController, and SecurityController classes, 
covering the inputs, outputs, and overall functionality for their methods. Any request that does not follow the correct 
endpoint structure will cause an HTTP 400 Bad Request response. If the service is not running (the commands "mvn compile" 
and "mvn spring-boot:run" are not used), there will be generic error message saying "This site can't be reached 127.0.0.1 
refused to connect." on the page. As a side note, there is an option to check the "Pretty-print" box so that the JSON response looks well-formatted.

### GET /analytics/top-endpoints
- Expected Input Parameters: N/A
- Expected Output: A JSON array of `[endpoint, count]` arrays sorted by frequency descending (most to least)
- Returns the most frequently accessed endpoints across all clients
- Upon Success: HTTP 200 Status Code is returned with the top endpoints array in the response body
- Upon Failure: N/A (There are no exceptions thrown and no error responses are generated)
- Endpoint Link: http://127.0.0.1:8080/analytics/top-endpoints


### POST /logs/upload
- Expected Input Parameters: clientId (String and form-data parameter), file (MultipartFile and form-data parameter)
- Expected Output: A String containing success or error message
- Uploads and processes an Apache combined format log file, storing entries in the database tagged with clientId
- Provides a section to input the clientId
- Provides a section to input the file
- Upon Success: HTTP 200 Status Code is returned with the message "Log file processed successfully." in the response body
- Upon Failure: HTTP 200 Status Code is returned with the message "Error processing log file: {error message}" in the response body
- Endpoint Link: http://127.0.0.1:8080/logs/upload

### GET /logs/statusCodeCounts
- Expected Input Parameters: clientId (String and query parameter)
- Expected Output: A JSON object mapping HTTP status codes to their counts (e.g., `{"200": 150, "404": 5, "500": 2}`)
- Returns the frequency of each HTTP status code for the specified client
- Upon Success: HTTP 200 Status Code is returned with the status code counts object in the response body
- Upon Failure:
    - HTTP 404 Status Code with "Error: clientId not found" in the response body if no log entries exist for the clientId
- Endpoint Link: http://127.0.0.1:8080/logs/statusCodeCounts?clientId={clientId}


### GET /analytics/timeseries/requests/{clientId}
- Expected Input Parameters: clientId (String path variable)
- Expected Output: A JSON object mapping hour strings to request counts
- Returns hourly request volume time series for the specified client
- Upon Success: HTTP 200 Status Code is returned with the time series object in the response body
- Upon Failure: N/A (There are no exceptions thrown and no error responses are generated)
- Endpoint Link: http://127.0.0.1:8080/analytics/timeseries/requests/{clientId}

### GET /analytics/timeseries/error-counts/{clientId}
- Expected Input Parameters: clientId (String and path variable)
- Expected Output: A JSON object mapping hour strings to error count objects
- Returns system-wide hourly 4xx and 5xx error counts for given clientId
- Upon Success: HTTP 200 Status Code is returned with the error time series object in the response body
- Upon Failure: N/A (There are no exceptions thrown and no error responses are generated)
- Endpoint Link: http://127.0.0.1:8080/analytics/timeseries/error-counts

### GET /security/suspicious-ips/{clientId}
- Expected Input Parameters: clientId (String and path variable)
- Expected Output: A JSON array of objects containing suspicious IP activity
- Returns IP addresses with 5 or more authentication errors (401/403) within any single hour window for given clientId
- Upon Success: HTTP 200 Status Code is returned with the suspicious IPs array in the response body
- Upon Failure: N/A (There are no exceptions thrown and no error responses are generated)
- Endpoint Link: http://127.0.0.1:8080/security/suspicious-ips

## API Tests

[API Testing Guide](./API-Testing.md)


## Style Checking Report
This project uses the Maven checkstyle tool to check the style of the code and also have style checking reports that
contain audits of the code style. The command needed to invoke this checkstyle tool is "mvn checkstyle:check" if you
want to see this information in the terminal. If you want to see a report, the command "mvn checkstyle:checkstyle"
should be used.

<img src="/images/Maven_Checkstyle_Command.png" alt="alt text" />

## Branch Coverage Report
This project uses JaCoCo to perform branch coverage analysis by looking at all possible logical branches
in the code and evaluating the total percentage of branches covered by the unit tests. The "src/target/site/jacoco"
folder contains different files associated with the different class branch coverage metrics. You can look at the overall
branch coverage by clicking index.html and opening it in a browser (Google Chrome works best).

<img src="/images/JaCoCo_Report_for_Branch_Coverage.png" alt="alt text" />

Final test coverage and checkstyle reports stored in First Iteration Reports folder.

## Static Code Analysis
### PMD Source Code Analyzer Guides
1. https://pmd.github.io/
2. https://pmd.github.io/pmd/pmd_rules_java.html

### PMD Source Code Analyzer Instructions
1. Download pmd-dist-7.17.0-bin.zip
2. Extract the zip-archive, e.g. to C:\pmd-bin-7.17.0
3. Add folder C:\pmd-bin-7.17.0\bin to PATH
4. Navigate to "4156-TeamProject-2025" directory
5. Execute: `pmd check -d src -R rulesets/java/quickstart.xml -f text -r pmd-report.txt`

### PMD Source Code Analyzer Note
There might be a few minor warnings that might appear for auto-generated JPA annotations.
However, we have considered these warnings to not be that important to the code style and structure.

## Continuous Integration Report
This repository uses GitHub CI/CD Workflows with separate `.yml` files for feature and main branches. Access the results by clicking on the Actions tab:
- Click workflow → latest run → "Unit Test" job
- Feature Branch Workflow: Runs on feature branch pushes
- Main Branch Workflow: Runs on main branch pushes/pull requests

## Tools Used
These are all the tools used for code development and deployment for this application:
- Maven Package Manager
- GitHub Actions (CI/CD Workflows)
    - This is enabled via the "Actions" tab on GitHub.
    - There are two workflows where one workflow focuses on the feature branch while the other workflow focuses on the main branch.
    - The first workflow "Feature Branch" runs a Maven build and all unit tests to make sure the code builds on the feature branch pushing code changes.
    - The second workflow "Main Branch" runs a Maven build and all unit tests to make sure the code builds on the main branch whenever there is a push or pull request.
- Spring Boot Application
  - Spring Boot serves as a web framework that facilitates queries to web endpoints.
  - Spring Boot autoconfigures the Apache Tomcat server, sets up JSON serialization, and manages REST API functionality.
- Spring Data JPA/Hibernate
  - Spring Data JPA converts the LogEntryRepository interface into full database CRUD operations.
  - Spring Data JPA also allows for custom queries through the analytics endpoints.
- SQLite
  - SQLite is used to facilitate data persistence and storage with a database.
- Checkstyle
    - Checkstyle is used for code style reporting and enforcement.
- PMD Source Code Analyzer
    - PMD does static analysis of the Java code before generating a report in a txt file.
- JUnit
    - JUnit tests get run automatically as part of the CI pipeline.
- JaCoCo
    - JaCoCo was used to generate code coverage reports.
- Postman
    - Postman was very useful for API testing, especially when someone needs to make PATCH requests to the service.

## AI Documentation
-   Used to make the sampleApacheLogSimple.log and suspiciousIps.log. 
    - Prompt: generate a sample apache log that contains some ip-addresses with multiple 401/403 calls in an hour window
-   Used to make the query for LogEntryRepository.findIpsWithManyAuthErrors
    - Prompt: Given this LogEntry class, write a JPQL query to get ip-addresses with multiple 401/403 calls in an hour window
-   Used ChatGPT to debug and refine logic for SQLite query integration and JSON-safe output in the following functions in LogService.java:
    - Prompt: Fix ClassCastException and make this method work with both Integer and Long types from SQLite aggregate results
    - Resolve JSON serialization errors (null map keys) and make aggregation results safe for 4xx/5xx counts
