FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY infrastructure/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
