# Use OpenJDK 21 image as base
FROM eclipse-temurin:21-jdk

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy the project files into the container
COPY . /app

# Build the app using Maven
RUN mvn clean package

# Expose port 8080
EXPOSE 8080

# Command to run the Spring Boot app
CMD ["java", "-jar", "/app/target/Query-Application-0.0.1-SNAPSHOT.jar"]

