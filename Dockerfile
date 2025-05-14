# Stage 1: Build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy the source code
COPY . /app/src

# Install wget and create lib directory
RUN apt-get update && \
    apt-get install -y wget && \
    mkdir -p /app/lib

# Function to download with retry
RUN echo '#!/bin/bash\n\
for i in {1..3}; do\n\
    wget -q --no-check-certificate "$1" -P "$2" && break\n\
    echo "Retry $i..."\n\
    sleep 2\n\
done' > /usr/local/bin/download-with-retry && \
    chmod +x /usr/local/bin/download-with-retry

# Download dependencies one by one with retry
RUN download-with-retry "https://maven.aliyun.com/repository/public/org/apache/curator/curator-recipes/2.12.0/curator-recipes-2.12.0.jar" /app/lib && \
    echo "Downloaded curator-recipes" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/apache/curator/curator-framework/2.12.0/curator-framework-2.12.0.jar" /app/lib && \
    echo "Downloaded curator-framework" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/apache/curator/curator-client/2.12.0/curator-client-2.12.0.jar" /app/lib && \
    echo "Downloaded curator-client" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/apache/zookeeper/zookeeper/3.4.7/zookeeper-3.4.7.jar" /app/lib && \
    echo "Downloaded zookeeper" && \
    download-with-retry "https://maven.aliyun.com/repository/public/commons-net/commons-net/3.9.0/commons-net-3.9.0.jar" /app/lib && \
    echo "Downloaded commons-net" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar" /app/lib && \
    echo "Downloaded lombok" && \
#    download-with-retry "https://maven.aliyun.com/repository/public/log4j/log4j/1.2.12/log4j-1.2.12.jar" /app/lib && \
#    echo "Downloaded log4j" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/apache/thrift/libthrift/0.13.0/libthrift-0.13.0.jar" /app/lib && \
    echo "Downloaded libthrift" && \
    download-with-retry "https://maven.aliyun.com/repository/public/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar" /app/lib && \
    echo "Downloaded slf4j-api" && \
#    download-with-retry "https://maven.aliyun.com/repository/public/org/slf4j/slf4j-log4j12/1.7.36/slf4j-log4j12-1.7.36.jar" /app/lib && \
#    echo "Downloaded slf4j-log4j12" && \
    download-with-retry "https://maven.aliyun.com/repository/public/com/google/guava/guava/16.0.1/guava-16.0.1.jar" /app/lib && \
    echo "Downloaded guava"

# Verify all JARs are downloaded
RUN ls -l /app/lib/*.jar

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

# Add the network connection check script
COPY check_network.sh /app/check_network.sh
RUN chmod +x /app/check_network.sh

# Expose the port the RegionServer listens on
EXPOSE 22222

# Set the command to run the application
CMD ["java", "-cp", "/app/bin:/app/lib/*", "RegionServer"] 