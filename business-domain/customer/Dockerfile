# Usar la imagen base de Maven construida localmente
ARG MAVEN_BASE_IMAGE=springneobank-maven
FROM ${MAVEN_BASE_IMAGE} AS build

WORKDIR /app

# Copiar únicamente el microservicio auth
COPY pom.xml ./pom.xml
COPY src ./src

# Compilar el microservicio
RUN mvn clean package spring-boot:repackage -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:21-jdk-alpine

# Crear usuario y grupo para ejecutar la app
RUN addgroup -g 1028 devopsc \
    && adduser -D -G devopsc admin \
    && apk add --no-cache curl

VOLUME /tmp

# Copiar el JAR y el script desde la etapa de build
COPY --from=build /app/target/customer-*-exec.jar /app.jar

USER admin
ENTRYPOINT ["java","-jar","/app.jar"]
