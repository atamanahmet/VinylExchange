FROM maven:3.9.6-amazoncorretto-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM amazoncorretto:17
WORKDIR /app
RUN yum install -y curl && yum clean all
COPY --from=build /app/target/vinyl-exchange-0.0.1.jar app.jar
VOLUME /app/uploads
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]