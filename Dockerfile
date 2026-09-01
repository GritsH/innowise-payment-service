FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8086

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]