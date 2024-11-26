FROM openjdk:21-jdk as builder
WORKDIR /app
COPY *.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:21-jdk
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
