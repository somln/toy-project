FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/toyProject-0.0.1-SNAPSHOT.jar /app/toyProject.jar

ENTRYPOINT ["java", "-jar", "/app/toyProject.jar"]