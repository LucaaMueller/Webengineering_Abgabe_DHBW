# Schritt 1: Build-Phase
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /webeng_2

# Maven Wrapper und Quellcode kopieren
COPY mvnw mvnw.cmd ./
COPY .mvn/ .mvn/
COPY pom.xml .
COPY src/ ./src

# Maven Wrapper ausführbar machen
RUN chmod +x mvnw

# Maven-Build ausführen
RUN ./mvnw clean package -DskipTests

# Schritt 2: Laufzeit-Phase
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Das erstellte JAR-File aus der Build-Phase kopieren
COPY --from=build /webeng_2/target/Web_eng2-0.0.1-SNAPSHOT.jar app.jar

# Port definieren
EXPOSE 8080

# Anwendung starten
CMD ["java", "-jar", "app.jar"]
