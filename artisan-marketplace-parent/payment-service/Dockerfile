# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the fat JAR file from the target directory to the container
# The JAR file name might vary depending on the build configuration (e.g., version)
# This assumes a common naming convention for Spring Boot executable JARs
ARG JAR_FILE=target/payment-service-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the JAR file 
# Default Java options can be overridden using JAVA_OPTS environment variable
ENTRYPOINT ["java", "-jar", "app.jar"]
