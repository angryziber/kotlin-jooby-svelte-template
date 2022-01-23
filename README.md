# Jooby/Kotlin + Svelte/Bootstrap app template

**Note:** I no longer use this template for new projects:
* For Kotlin with coroutines on server-side I have created [Klite](https://github.com/angryziber/klite) framework (it also contains a sample project)
* On the client side I still use Svelte with the same structure as here, but built by Vite instead of Snowpack. It does the same, but lighter and easier to configure. Switching is easy.

---

A relatively lightweight modern app template using Kotlin/JVM.
For people asking me at conferences what would I recommend from technical perspective.

* Server API using Jooby
    * Postgres is used for DB (runnable using docker-compose)
    * Liquibase migrates the DB
    * Server unit tests use Junit5/Mockk
    * Repository integration tests run in the same DB instance (different schema)
* UI is built with Svelte + Snowpack with TypeScript support
    * UI tests use Web Test Runner and TypeScript
* E2E tests use Selenide to drive the actual browser backed by H2 DB

## Noteworthy features

* Efficient Docker usage (cached layers in order of less frequent changes), Gradle downloads dependencies once
* Builds/tests run in Docker in several stages, test results available after build in [Jenkins](Jenkinsfile)
* [Internationalization](i18n) (both client-side and server-side)
* Supports static server-side rendered pages using [Pebble templates](ui/static)
* Selenide/Selenium tests work inside of Docker
* Automatic TypeScript types from Kotlin classes in [UI API](ui/api/types.ts) 
  (configurable in [Gradle build script](build.gradle.kts))

No frameworks needed for this:

* 30-line [dependency injection](src/app/AutoCreatingServiceRegistry.kt)
* 100-line [JDBC extensions](src/db/JdbcExtensions.kt) instead of ORM and [transaction management](src/db/Transaction.kt)  
* Simple client-side [router](ui/routing/Router.ts) used in [root component](ui/App.svelte)

Testing:

* 3 layers of testing: UI (client-side components), server-side (unit + repositories) and E2E using Selenide to drive an actual browser.
* Repository tests rollback to avoid recreation of the DB each time
* E2E tests test login once and then use [fake login](src/auth/FakeLoginForTestingController.kt) to get to needed places quickly

### Why Kotlin & Jooby?

See [conference slides](https://docs.google.com/presentation/d/1iXFKSsvPhBze-3cvd2j0Ri7dp2yH_w_1ULZPMfbdWAQ) from Kotland 2021 conference.

### Why Svelte?

Firstly, websites should use server-side/static rendering. Apps (not caring about SEO) are better off using reactive UI frameworks.

Framework                   |**React**                  |**Vue**                           |**Svelte**
----------------------------|---------------------------|----------------------------------|------------------------------
NPM packages                |18 with router             |2 with router                     |1
Minified runtime size       |6k                         |64k                               |0
Reactivity                  |runtime one-way (complex editing apps usually require flux/redux)|runtime two-way                   |compile-time two-way (better performance, less boilerplate)
Template syntax             |non-standard JSX file format, weird attribute names|Template exports must follow some strict structure, grouping of properties, etc|Just standard js variables - closest to real HTML
Component imports           |Just import & use|Import, declare, then use with a different name|Just import & use
Component syntax            |3 different ways to write: class, function, hooks|1 (+ 1 without the compiler)|1 way

Dependencies, runtime size and simplicity is also the reason why this repository implements
its own simple router and i18n support on top of Svelte.

Also, *fetch*, *Promises/async/await* and many other APIs (including array transformations) are already available in all modern browsers, so
dependencies like *lodash* and *axios* are obsolete.

### Why Snowpack?

Bundler                        |**Webpack**|**Rollup**|**Snowpack**
-------------------------------|-----------|----------|--------------
NPM packages (without plugins) |74         |2         |7
ES6 modules                    |transpiled to es5|native|native & not bundled by default - you run what you write
Watch & reload                 |full rebuild|full rebuild|rebuilds & reloads only changed files (es6 modules)

## Running in Docker

`docker-compose up --build`

or to just start the DB:
`docker-compose up -d db`

This will bind to `127.0.0.1:65432` by default

# Development

After clone:

```
npm install --legacy-peer-deps
```

Then:

```
npm run watch
# or just `npm run build`
./gradlew run
```

To run tests:

* `npm test` - for UI components
* `./gradlew test` - for API
* `./gradlew e2eTest` - for in-browser End-to-End tests

## Running from IDE

Some [IntelliJ IDEA config](.idea) is committed to share code style, run configurations, etc with the team.

* Open the directory as project
* Click "Import gradle project"
* `npm run watch` will run automatically to compile changing UI assets on the fly
* Install "Svelte" plugin for working with UI components
* Choose "LauncherKt" run configuration to start the server (Jooby/Netty)

## Deployment

* [Jenkinsfile](Jenkinsfile) would deploy the app using `docker-compose`
* In addition, deployment to [Heroku](https://heroku.com) is supported using the same Docker container
* Env-specific configuration is provided using env vars (docker-compose.yml files or Heroku), according to [12-factor apps](https://12factor.net)
* All env vars are optional, so that everything would run out of the box in development

## Adding icons

Uses Feather icon set available at https://feather.netlify.com/.

To add an icon:

1. Download any icon from that repository as svg. For custom ones, use existing ones as a basis for consistency.
2. Add the icon to `public/img/icons`.
3. Generate the sprites using the run configuration in IDEA or `npm run gen-icon-sprite`
