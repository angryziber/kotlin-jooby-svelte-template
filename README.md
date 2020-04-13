# Jooby/Kotlin + Svelte/Bootstrap app template

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

Rollup + Svelte is used for client-side.

Jooby + Kotlin is used for server-side.

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
