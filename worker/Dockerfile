FROM openjdk:11

ARG JAR_FILE=/build/libs/*.jar

RUN echo ${JAR_FILE}

COPY ${JAR_FILE} app.jar

EXPOSE 5000

ENTRYPOINT ["java","-jar","/app.jar"]