# Imagen base con Java 17 y Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Crear directorio
WORKDIR /app

# Copiar proyecto completo
COPY . .

# Construir el JAR (esto genera target/emailservice-1.0.0.jar en Railway)
RUN mvn -q -e -DskipTests package


# ----- Etapa final -----
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=build /app/target/emailservice-1.0.0.jar /app/emailservice.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/emailservice.jar"]
