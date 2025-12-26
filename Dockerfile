FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="bjj"
COPY ./build/libs/BJJ-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]