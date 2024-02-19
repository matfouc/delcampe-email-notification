# 1st Docker build stage: build the project with Maven
FROM maven:3-eclipse-temurin-21-alpine as builder
WORKDIR /project
COPY . /project/
RUN mvn package -DskipTests -B

# 2nd Docker build stage: copy builder output and configure entry point
FROM eclipse-temurin:21-jre-alpine

ENV APP_DIR /application
ENV APP_FILE container-uber-jar.jar 

EXPOSE 8888

WORKDIR $APP_DIR
COPY --from=builder /project/target/*-fat.jar $APP_DIR/$APP_FILE

RUN apk --no-cache add curl
HEALTHCHECK CMD curl --fail https://www.delcampe.net || exit 1

ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $APP_FILE"]

