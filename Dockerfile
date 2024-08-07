#
# Build stage
#
FROM maven:3.8.3-openjdk-17 AS build
WORKDIR /app
COPY . /app/
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/DeloySpring-0.0.1-SNAPSHOT.war deloyspring.war
EXPOSE 8080
ENTRYPOINT ["java","-jar","deloyspring.war"]