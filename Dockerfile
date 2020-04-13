FROM node:12-alpine as ui-build
RUN apk add openssl && rm -rf /var/cache/apk

WORKDIR /app

COPY *.json *.js .browserslistrc ./
RUN npm ci
COPY ui ui
COPY i18n i18n
COPY public public
ARG BUILD_ID
LABEL ui-build=$BUILD_ID
RUN CI=true npm test
RUN npm run build

FROM bellsoft/liberica-openjdk-alpine:11 as server-build

WORKDIR /app

COPY gradlew ./
COPY gradle gradle/
RUN ./gradlew --version

COPY build.gradle* ./
RUN ./gradlew downloadDeps

COPY . ./
COPY --from=ui-build /app/public public/
ARG BUILD_ID
LABEL server-build=$BUILD_ID
RUN ./gradlew test installDist

FROM ubuntu:latest as e2e-tests
RUN apt-get update && apt-get install -y chromium-browser openjdk-11-jre-headless && apt-get clean
RUN ln -s /usr/bin/chromium-browser /usr/bin/google-chrome

WORKDIR /app
COPY --from=server-build /root/.gradle /root/.gradle
COPY --from=server-build /app ./
ARG BUILD_ID
LABEL e2e-tests=$BUILD_ID
RUN ./gradlew e2eTest --info --no-daemon -Pheadless

FROM bellsoft/liberica-openjdk-alpine:11 as final
RUN apk add goaccess && rm -rf /var/cache/apk
RUN adduser -S user

COPY --from=server-build /app/build/install /
WORKDIR /app
COPY .goaccessrc ./

RUN mkdir logs tmp; chown -R user logs tmp
USER user

ENV JAVA_OPTS="-Xmx512M"

CMD (while :; do sleep 10; goaccess -a -p .goaccessrc -o logs/overview.html logs/request*.log 2>/dev/null || echo "GoAccess failed $?"; done &); bin/app

ENV PORT=8080
EXPOSE $PORT
