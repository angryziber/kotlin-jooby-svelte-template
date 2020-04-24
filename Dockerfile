# Build client side
FROM node:12-alpine as ui-build
RUN apk add openssl && rm -rf /var/cache/apk

WORKDIR /app

COPY *.json *.js .browserslistrc ./
RUN npm ci
COPY ui ui
COPY i18n i18n
COPY public public
RUN npm run build

# This image builds server side, but also is used for E2E tests in Chromium, see Jenkinsfile
FROM ubuntu:bionic as server-build
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

# The final image - Bellsoft alpine OpenJDK images are the smallest
FROM bellsoft/liberica-openjdk-alpine:11 as final
RUN adduser -S user

COPY --from=server-build /app/build/install /
WORKDIR /app

RUN mkdir logs tmp; chown -R user logs tmp
# Run under non-privileged user with minimal write permissions
USER user

# Fit into Heroku's 512m total limit
ENV JAVA_OPTS="-Xmx330m -Xss512k"
CMD bin/app

# Heroku redefines exposed port
ENV PORT=8080
EXPOSE $PORT
