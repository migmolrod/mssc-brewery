FROM openjdk:11-jre-slim as builder
WORKDIR application
ADD maven/${project.build.finalName}.jar ${project.build.finalName}.jar
RUN java -Djarmode=layertools -jar ${project.build.finalName}.jar extract

FROM openjdk:11-jre-slim
LABEL PROJECT_NAME=${project.artifactId} \
    PROJECT=${project.id}

RUN apt update \
    && apt install -y curl

EXPOSE 8080

WORKDIR application
COPY --from=builder \
    application/dependencies/ \
    application/spring-boot-loader/ \
    application/snapshot-dependencies/ \
    application/application/ \
    ./
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]
