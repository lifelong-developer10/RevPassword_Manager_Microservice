FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY microservices/notification-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
