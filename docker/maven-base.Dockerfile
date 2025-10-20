# Stage 0: Maven base
FROM maven:3.9.1-eclipse-temurin-17 AS maven-base

WORKDIR /app

# Copiar POM padre
COPY pom.xml ./pom.xml

# Copiar módulos hijos completos
COPY business-domain ./business-domain
COPY infraestructure-domain ./infraestructure-domain

# Instalar POM padre y todos los módulos hijos
RUN mvn -f ./pom.xml install -DskipTests
