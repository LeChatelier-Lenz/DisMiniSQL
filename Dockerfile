# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy the source code
COPY . /app/src

# Download dependencies
RUN apt-get update && \
    apt-get install -y wget && \
    wget https://repo1.maven.org/maven2/org/apache/curator/curator-framework/5.5.0/curator-framework-5.5.0.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/org/apache/curator/curator-recipes/5.5.0/curator-recipes-5.5.0.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/org/apache/zookeeper/zookeeper/3.8.3/zookeeper-3.8.3.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/org/apache/commons/commons-net/3.9.0/commons-net-3.9.0.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/ch/qos/logback/logback-classic/1.4.11/logback-classic-1.4.11.jar -P /app/lib && \
    wget https://repo1.maven.org/maven2/ch/qos/logback/logback-core/1.4.11/logback-core-1.4.11.jar -P /app/lib

# Compile the Java code, excluding test files
RUN find /app/src -name "*.java" | grep -v "test" > sources.txt && \
    javac -cp "/app/src:/app/lib/*" -d /app/bin @sources.txt && \
    rm sources.txt

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the compiled classes and dependencies
COPY --from=builder /app/bin /app/bin
COPY --from=builder /app/lib /app/lib

# Expose the port the RegionServer listens on
EXPOSE 22222

# Set the command to run the application
CMD ["java", "-cp", "/app/bin:/app/lib/*", "RegionServer"] 