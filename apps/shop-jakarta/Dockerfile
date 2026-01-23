FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

FROM payara/micro:6.2024.4-jdk17
COPY --from=build /build/target/shopfront.war /opt/payara/deployments/shopfront.war
ENV DB_HOST=db \
    DB_PORT=5432 \
    DB_NAME=aspcrm \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    DB_URL=jdbc:postgresql://db:5432/aspcrm
EXPOSE 8080
CMD ["--nocluster","--contextRoot","/","--deploymentDir","/opt/payara/deployments"]
