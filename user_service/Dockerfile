FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build
COPY ../.. .
RUN ./mvnw -pl user_service -am clean package -DskipTests=true

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/user_service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]