# Build client side
FROM satantime/puppeteer-node:16-slim as ui-build

WORKDIR /app

COPY *.json *js ./
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
RUN ./gradlew deps

COPY . ./
COPY --from=ui-build /app/build/public public/
RUN ./gradlew jar


# The final image - Bellsoft alpine OpenJDK images are the smallest
FROM bellsoft/liberica-openjdk-alpine:11 as final
RUN adduser -S user

WORKDIR /app
COPY --from=server-build /app/build/libs ./
COPY --from=ui-build /app/build/public public/

# Pebble requires a writable tmp
RUN mkdir tmp; chown -R user tmp

# Run under non-privileged user with minimal write permissions
USER user

ENV API_VERSION=1
ENV ENV=https

ARG VERSION=dev
ENV VERSION=$VERSION

# Fit into Heroku's 512m total limit
ENV JAVA_OPTS="-Xmx330m -Xss512k"
CMD java $JAVA_OPTS -jar app.jar

# Heroku redefines exposed port
ENV PORT=8080
EXPOSE $PORT

HEALTHCHECK CMD wget --no-verbose --tries=1 --spider http://localhost:$PORT/api/health || exit 1
