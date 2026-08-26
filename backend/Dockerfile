# ==========================================
# Etapa 1: build do projeto com Maven
# ==========================================
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

# Copia primeiro o pom e o wrapper pra aproveitar cache do Docker
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Garante permissao de execucao e baixa as dependencias
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copia o codigo fonte e compila, pulando os testes (testes rodam no CI)
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Etapa 2: imagem final, menor e mais segura
# ==========================================
FROM eclipse-temurin:26-jre

WORKDIR /app

# Copia so o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Porta que o Spring Boot usa
EXPOSE 8080

# Roda no perfil de producao, que usa PostgreSQL via variaveis de ambiente
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
