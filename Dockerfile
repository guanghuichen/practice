FROM openjdk:17
MAINTAINER "Guanghui@Chen"
VOLUME /data/tmp


WORKDIR /app

COPY ./build/libs/practice-0.0.2-SNAPSHOT.jar /app/practice-0.0.2.jar

ENTRYPOINT java -jar /app/practice-0.0.2.jar