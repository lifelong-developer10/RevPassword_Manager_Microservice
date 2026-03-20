FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY infrastructure/eureka-server/pom.xml infrastructure/eureka-server/pom.xml
COPY infrastructure/config-server/pom.xml infrastructure/config-server/pom.xml
COPY infrastructure/api-gateway/pom.xml infrastructure/api-gateway/pom.xml
COPY microservices/user-service/pom.xml microservices/user-service/pom.xml
COPY microservices/vault-service/pom.xml microservices/vault-service/pom.xml
COPY microservices/generator-service/pom.xml microservices/generator-service/pom.xml
COPY microservices/security-service/pom.xml microservices/security-service/pom.xml
COPY microservices/notification-service/pom.xml microservices/notification-service/pom.xml
RUN mvn dependency:go-offline -pl microservices/user-service -am -q
COPY microservices/user-service/src microservices/user-service/src
RUN mvn clean package -pl microservices/user-service -am -DskipTests -q

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/microservices/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
