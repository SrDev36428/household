FROM maven:3.8.3-openjdk-17 AS builder

# Set working directory
WORKDIR /app

# Copy the project files to the container
COPY . /app

# Set working directory to the root directory of the project
WORKDIR /app

# Build the app using Maven
RUN mvn package

# Build the final Docker image
FROM openjdk:17

# Copy the app files from the previous stage
COPY --from=builder /app/target/Household-0.0.1-SNAPSHOT.jar /app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "/app.jar"]

