# Use Java 17
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the entire project
COPY . .

# Give execute permission to mvnw (important!)
RUN chmod +x mvnw

# Build the application using Maven Wrapper
RUN ./mvnw -q -DskipTests package

# Expose Spring Boot default port
EXPOSE 8080

# Run the generated jar file
CMD ["java", "-jar", "target/SecureHealthCare-0.0.1-SNAPSHOT.jar"]
