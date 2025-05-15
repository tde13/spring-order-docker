FROM openjdk:17
ADD target/spring-order-docker.jar spring-order-docker.jar
ENTRYPOINT ["java","-jar","/spring-order-docker.jar"]
