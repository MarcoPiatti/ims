# -------- Stage 1: Build --------
FROM sbtscala/scala-sbt:eclipse-temurin-24.0.1_9_1.11.6_3.7.3 AS builder
WORKDIR /app
COPY . .
RUN sbt clean assembly

# -------- Stage 2: Run --------
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=builder /app/target/scala-3.7.3/*-assembly-*.jar /app/app.jar
COPY --from=builder /app/src/main/resources /app/resources

ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
