# Use OpenJDK 21 as the base image
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar into the container
COPY /target/flights-api.jar /app/flights-api.jar

# Expose the port for the application
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "flights-api.jar"]
