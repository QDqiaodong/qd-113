ARG DOCKER_REGISTRY=

FROM ${DOCKER_REGISTRY}maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml ./
RUN mvn dependency:resolve -B

COPY src ./src
RUN mvn package -DskipTests -B

FROM ${DOCKER_REGISTRY}eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/tea-tracker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
