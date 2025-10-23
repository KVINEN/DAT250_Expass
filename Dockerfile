# Stage 1: Use a full JDK to build the application with Gradle
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace/app

# Copy the Gradle wrapper files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle

# Copy the project definition files
COPY build.gradle.kts settings.gradle.kts ./

# Copy the rest of the source code
COPY src ./src

# Build the application. The --no-daemon flag is best for CI/CD environments.
RUN chmod +x ./gradlew && ./gradlew build -x test --no-daemon

# ---

# Stage 2: Use a smaller JRE image for the final container
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy only the built JAR from the builder stage
# The file will be in 'build/libs/' and will end with '.jar'
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]