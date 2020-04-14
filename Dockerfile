FROM node:12-alpine as ui-build
RUN apk add openssl && rm -rf /var/cache/apk

WORKDIR /app

COPY *.json *.js .browserslistrc ./
RUN npm ci
COPY ui ui
COPY i18n i18n
COPY public public
RUN npm run build

# -----------------------------------------------
FROM ubuntu:latest as server-build
RUN apt-get update && apt-get install -y chromium-browser openjdk-11-jre-headless && apt-get clean
RUN ln -s /usr/bin/chromium-browser /usr/bin/google-chrome

WORKDIR /app

COPY gradlew ./
COPY gradle gradle/
RUN ./gradlew --version

COPY build.gradle* ./
RUN ./gradlew downloadDeps

COPY . ./
COPY --from=ui-build /app/public public/
RUN ./gradlew installDist

# -----------------------------------------------
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
