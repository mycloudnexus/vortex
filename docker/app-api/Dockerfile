###########################
#
# Download dependencies and package our applications
#
###########################
FROM docker.io/library/eclipse-temurin:17-jdk AS builder

COPY pom.xml /tmp/
COPY mvnw /tmp/
COPY .mvn /tmp/.mvn
COPY vortex-app /tmp/vortex-app
COPY vortex-java-sdk /tmp/vortex-java-sdk

WORKDIR /tmp

RUN --mount=type=cache,id=maven_deps,target=/usr/share/maven/ref/repository \
    ./mvnw -B dependency:go-offline -DskipTests \
    -Dmaven.repo.local=/usr/share/maven/ref/repository \
    -f /tmp/pom.xml

RUN --mount=type=cache,id=maven_deps,target=/usr/share/maven/ref/repository \
    ./mvnw -B \
    -Dmaven.repo.local=/usr/share/maven/ref/repository \
    package -DskipTests -Dskip-git=true


###########################
#
# Set  up our new base container
#
###########################
FROM docker.io/library/eclipse-temurin:17-jre AS base

RUN apt-get update && \
    apt-get install -y --no-install-recommends dumb-init && \
    apt-get upgrade -y && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -r -g 11020 app && \
    useradd -r -u 11020 -g 11020 -m -s /bin/false -d /opt/app app

WORKDIR /opt/app

ENTRYPOINT ["/usr/bin/dumb-init", "--"]


###########################
#
# Copy the vortex-app-api JARs to final build
#
###########################
FROM base AS vortex-app-api

COPY --from=builder /tmp/vortex-app/vortex-app-api/target/*.jar /opt/app/app.jar

EXPOSE 8080

USER 11020

CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/app/app.jar"]
