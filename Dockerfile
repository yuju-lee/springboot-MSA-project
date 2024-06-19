#FROM ubuntu:latest
#LABEL authors="iyuju"
#
#ENTRYPOINT ["top", "-b"]

FROM openjdk:17
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "/app.jar"]