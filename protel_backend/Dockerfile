# Stage 1: Build aplikasi menggunakan Gradle
FROM gradle:7.6-jdk17 AS build
COPY --from=chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

# Stage 2: Jalankan aplikasi menggunakan OpenJDK
FROM openjdk:17-jdk-slim
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]