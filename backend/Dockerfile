# Etapa 1: Build da aplicação
FROM maven:3.9.4-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia o pom.xml e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime (imagem enxuta só com JRE)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=builder /app/target/summai-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
