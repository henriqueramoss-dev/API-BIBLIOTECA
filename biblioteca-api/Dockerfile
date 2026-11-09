# Imagem da API para publicar (Render): compila com Maven e roda só com o JRE
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# A Render informa a porta na variável PORT; localmente usa 8080
ENTRYPOINT ["sh", "-c", "exec java -XX:MaxRAMPercentage=70 -Dserver.port=${PORT:-8080} -jar app.jar"]
