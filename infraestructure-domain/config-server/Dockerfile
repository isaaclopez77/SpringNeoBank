FROM eclipse-temurin:21-jdk-alpine

RUN addgroup -g 1028 devopsc \
    && adduser -D -G devopsc admin

VOLUME /tmp

COPY target/config-server-*-exec.jar app.jar

RUN chown -R admin:devopsc /app.jar /tmp

USER admin

ENTRYPOINT ["java","-jar","/app.jar"]