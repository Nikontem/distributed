# Use the official maven/Java 17 image to create a build artifact.
FROM maven:3.8.3-openjdk-17 as builder

# Copy local code to the container image.
WORKDIR /app
COPY . .

# Build a release artifact.
RUN mvn package -DskipTests

# Use OpenJDK for final image
FROM openjdk:17

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/distributed-1.0-SNAPSHOT-jar-with-dependencies.jar /distributed-joins.jar

# Run the web service on container startup.
CMD ["java", "-jar", "/distributed-joins.jar"]