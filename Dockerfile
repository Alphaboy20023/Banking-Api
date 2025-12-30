# Step 1: Use Maven to build the application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# skip tests during the build to speed up deployment on Render's free tier
RUN mvn clean package -DskipTests

# Step 2: Use a lightweight Java runtime to run the app
FROM openjdk:17.0.1-jdk-slim
WORKDIR /app

COPY --from=build /app/target/BankingApi-0.0.1-SNAPSHOT.jar app.jar

# Render uses the PORT environment variable
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]