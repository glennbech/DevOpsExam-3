# First stage: build the application
FROM maven:3.8.3-jdk-11 AS build
COPY . /app/
WORKDIR /app
RUN mvn package -DskipTests

# Second stage: create a slim image
FROM openjdk:11-jre-slim
ENV AWS_REGION eu-west-1
COPY --from=build /app/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]