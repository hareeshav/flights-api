# Use OpenJDK with explicit platform specification
FROM --platform=linux/amd64 openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Install curl
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy application jar
COPY target/flights-api.jar flights-api.jar

# Copy wait script
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh

# Expose the application port
EXPOSE 8080

# Command to run the wait-for-it script and then start the app
CMD ["/app/wait-for-it.sh", "elasticsearch", "9200", "--", "java", "-jar", "flights-api.jar"]
