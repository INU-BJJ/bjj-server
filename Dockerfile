FROM openjdk:17-jdk-slim
LABEL maintainer="bjj"
COPY ./build/libs/BJJ-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]