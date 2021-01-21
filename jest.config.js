const svelteOptions = require('./svelte.config')

module.exports = {
  testPathIgnorePatterns: ['/node_modules/'],
  globals: {
    svelte: svelteOptions
  },
  preset: 'ts-jest',
  testMatch: [
    '**/{ui,i18n}/**/*.test.ts'
  ],
  transform: {
    '\\.svelte$': ['svelte-jester', {preprocess: true}]
  },
  moduleNameMapper: {
    "^@ui(.*)$": "<rootDir>/ui$1",
  },
  moduleFileExtensions: ['js', 'ts', 'd.ts', 'svelte'],
  setupFilesAfterEnv: [
    '@testing-library/jest-dom/extend-expect',
    './ui/setup-tests.ts'
  ],
  restoreMocks: true,
  reporters: [ 'default', ['jest-junit', {
    suiteNameTemplate: "{filepath}",
    titleTemplate: "{title}",
    outputDirectory: 'build/test-results/ui'
  }]],
}
