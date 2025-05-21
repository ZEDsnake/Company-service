FROM eclipse-temurin:17-jdk-jammy AS builder
ARG MODULE
WORKDIR /build
COPY . .
RUN ./mvnw -pl ${MODULE} -am clean package -DskipTests=true

FROM eclipse-temurin:17-jre-jammy
ARG MODULE
WORKDIR /app
COPY --from=builder /build/${MODULE}/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
