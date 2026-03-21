FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY microservices/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
