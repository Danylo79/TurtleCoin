FROM openjdk:16-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} TurtleCoin-1.0.jar
ENTRYPOINT ["java","-jar","/TurtleCoin-1.0.jar"]