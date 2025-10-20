# Dockerfile

# jdk17 Image Start
FROM openjdk:17

ARG JAR_FILE=build/libs/payment-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} payment_Backend.jar
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","payment_Backend.jar"]