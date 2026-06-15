ARG DOCKER_REGISTRY=

FROM ${DOCKER_REGISTRY}maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=60"

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml ./
RUN for i in 1 2 3; do \
      mvn -s /root/.m2/settings.xml dependency:resolve -B && break; \
      if [ "$i" = "3" ]; then exit 1; fi; \
      sleep 5; \
    done

COPY src ./src
RUN for i in 1 2 3; do \
      mvn -s /root/.m2/settings.xml package -DskipTests -B && break; \
      if [ "$i" = "3" ]; then exit 1; fi; \
      sleep 5; \
    done

FROM ${DOCKER_REGISTRY}eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/tea-tracker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
