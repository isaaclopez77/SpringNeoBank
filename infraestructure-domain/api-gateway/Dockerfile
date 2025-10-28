# Usar la imagen base de Maven construida localmente
ARG MAVEN_BASE_IMAGE=springneobank-maven
FROM ${MAVEN_BASE_IMAGE} AS build

WORKDIR /app

# Copiar únicamente el microservicio
COPY pom.xml ./pom.xml
COPY src ./src

# Compilar el microservicio
RUN mvn clean package spring-boot:repackage -DskipTests

FROM eclipse-temurin:21-jdk-alpine

RUN addgroup -g 1028 devopsc \
    && adduser -D -G devopsc admin

VOLUME /tmp

COPY --from=build /app/target/api-gateway-*-exec.jar /app.jar

RUN chown -R admin:devopsc /app.jar /tmp

USER admin

ENTRYPOINT ["java","-jar","/app.jar"]