# Etapa 1: compilar con Maven
FROM maven:3.9-eclipse-temurin-17 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV TZ=America/Bogota
RUN addgroup -S filacero && adduser -S filacero -G filacero
COPY --from=compilacion /app/target/filacero.jar app.jar
USER filacero
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
