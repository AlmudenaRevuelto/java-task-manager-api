# Stage 1: Build
FROM maven:3.9.3-eclipse-temurin-17 AS build

# Directorio de la app
WORKDIR /app

# Copiar pom.xml y src
COPY pom.xml .
COPY src ./src

# Construir proyecto sin tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Directorio de la app
WORKDIR /app

# Copiar solo el jar compilado del stage anterior
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]