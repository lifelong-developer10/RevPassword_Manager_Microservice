FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY microservices/vault-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
