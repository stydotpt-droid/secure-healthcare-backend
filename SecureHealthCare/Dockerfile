# Use official Java 17 image
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy everything into /app
COPY . .

# Build the Spring Boot application
RUN ./mvnw -q -DskipTests package

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the Spring Boot JAR
CMD ["java", "-jar", "target/SecureHealthCare-0.0.1-SNAPSHOT.jar"]
