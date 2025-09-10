# Stage 1: Build Maven project
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom trước để cache dependencies
COPY pom.xml .
COPY commons/pom.xml commons/
COPY config/pom.xml config/
COPY discovery/pom.xml discovery/
COPY gateway/pom.xml gateway/
COPY auth/pom.xml auth/
COPY cart-service/pom.xml cart-service/
COPY order-service/pom.xml order-service/
COPY product-service/pom.xml product-service/

# Tải dependencies
RUN mvn dependency:go-offline

# Copy toàn bộ source
COPY . .

# Build service
RUN mvn -pl product-service -am clean package -DskipTests
