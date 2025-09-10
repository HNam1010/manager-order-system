FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .

COPY . .

# Now copy the entire source code
#COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/cart-service/target/*.jar app.jar
EXPOSE 8083

#run the app
ENTRYPOINT ["java","-jar","/app/app.jar"]
