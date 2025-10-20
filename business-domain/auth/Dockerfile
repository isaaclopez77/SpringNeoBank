FROM eclipse-temurin:21-jdk-alpine

RUN addgroup -g 1028 devopsc \
    && adduser -D -G devopsc admin \
    && apk add --no-cache curl

VOLUME /tmp

COPY target/auth-*-exec.jar app.jar

COPY wait-for-config.sh /wait-for-config.sh
RUN chmod +x /wait-for-config.sh

RUN chown -R admin:devopsc /app.jar /tmp /wait-for-config.sh

USER admin

ENTRYPOINT ["/wait-for-config.sh", "java","-jar","/app.jar"]