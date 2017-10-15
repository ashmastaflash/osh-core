FROM openjdk:8u131-jdk-alpine
MAINTAINER ashmastaflash

RUN apk add -U \
    bash \
    git

RUN mkdir /app
COPY ./ /app/
ENV OSH_RUNNER=/app/run_osh.sh

RUN cd /app/ && \
    ./gradlew

EXPOSE 8181

ENTRYPOINT /bin/bash
CMD /app/launch.sh
