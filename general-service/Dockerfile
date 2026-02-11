FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY build/libs/gateway-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]