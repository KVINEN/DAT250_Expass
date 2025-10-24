# DAT250 - Expass6

## Problems:

### 1. Initial Test Failure
* NullPointerException in VoteController when calling getPoll().getId() on a VoteOption received in the request body.
* JsonParseException in tests because the controller returned a plain text error message instead of JSON due to the NullPointerException.
* NOT_FOUND - no exchange 'poll.*' errors from RabbitMQ during application startup in tests.

#### Solution:
* Refactored VoteController: Modified the castVote method to explicitly fetch the Poll and VoteOption entities from the PollManager based on IDs before constructing the Vote object to be saved. Error responses where changed to return JSON Map.of("error", "message").
* Explicit Exchange Declaration: Added a @Bean method pollWildcardExchange in RabbitMQConfig.java to explicitly declare the poll.* topic exchange required for the queue binding.

### 2. Spring configuration error
* Application context failed to load during tests with the error: @Configuration class 'RabbitMQConfig' contains overloaded @Bean methods with name 'topicBinding'.
* Adding the explicit exchange bean resulted in two @Bean methods with the same name (topicBinding).

#### Solution:
* Renamed the @Bean method responsible for creating the Declarables (queue binding) to voteQueueBinding in RabbitMQConfig.java.

### 3. CI/CD Pipeline - Docker Create Error
* The docker create command for the RabbitMQ service failed with invalid reference format.
* An inline comment (# Increase retries...) within the multi-line options string for the RabbitMQ service in .github/workflows/main.yml was being interpreted as part of the command arguments.

#### Solution:
* Removed the comment from the options block in the workflow file.

### 4. CI/CD Pipeline - RabbitMQ Connection failure
* The nc command in the workflow failed with Temporary failure in name resolution when trying to check the readiness of the rabbitmq service hostname.
* Tests failed with Connection refused errors when the Spring application tried to connect to RabbitMQ, indicating it was likely still trying localhost or the service wasn't fully ready/accessible via hostname when the tests started.

#### Solution:
* Modified Wait Logic: Changed the nc command in the workflow's wait loop to check localhost:5672 instead of rabbitmq:5672. This checks the port mapping on the runner itself, bypassing potential DNS issues within the initial script execution.
* Explicit Host Configuration: Added -Dspring.rabbitmq.host=rabbitmq as a system property to the gradlew command in the workflow. This explicitly tells the Spring application running the tests to connect to the RabbitMQ service using its correct service name within the Docker network created by GitHub Actions.