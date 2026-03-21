FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY infrastructure/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
