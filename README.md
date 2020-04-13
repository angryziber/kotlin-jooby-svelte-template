# Jooby/Kotlin + Svelte/Bootstrap app template

* Server API is built with Jooby in Kotlin
    * Postgres is used for DB (runnable using docker-compose)
    * Liquibase migrates the DB
    * Server unit tests use Junit5/Mockk
    * Repository integration tests run in in-memory H2 to avoid any dependencies
* UI is built with Svelte
    * UI tests use Jest and TypeScript (there is no IDE support for Svelte+TS yet)
* E2E tests use Selenide to drive the actual browser backed by H2
* The whole 4-stage build runs using `docker build`, see below 

## Running in Docker

`docker-compose up --build`

or to just start the DB:
`docker-compose up -d db`

This will bind to `127.0.0.1:55432` by default

# Development

After clone:

```
npm install
```

Then:

```
npm run watch
# or just npm run build
./gradlew run
```

To run tests:

* `npm test` - for UI components
* `./gradlew test` - for API
* `./gradlew e2eTest` - for in-browser End-to-End tests

## Running from IDE

# UI
## Static/landing pages

Are generated using Pebble templates in `ui/static`

## Dynamic pages

Pages after login use Svelte framework with components in `ui`

## Adding icons

The design uses Feather icon set available at https://feather.netlify.com/.

To add an icon:

1. Download any icon from that repository as svg. For custom ones, use existing ones as a basis for consistency.
2. Add the icon to `public/img/icons`.
3. Generate the sprites using the run configuration in IDEA or `npm run-script generate-sprite`
