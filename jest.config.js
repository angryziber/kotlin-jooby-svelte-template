const svelteOptions = require('./svelte.config')

module.exports = {
  testPathIgnorePatterns: ['/node_modules/'],
  globals: {
    svelte: svelteOptions
  },
  preset: 'ts-jest',
  testMatch: [
    '**/ui/**/*.test.ts'
  ],
  transform: {
    '\\.svelte$': ['svelte-test/transform']
  },
  setupFilesAfterEnv: [
    '@testing-library/jest-dom/extend-expect',
    './ui/setup-tests.ts'
  ],
  restoreMocks: true,
  reporters: [ 'default', ['jest-junit', {
    suiteName: 'UI tests',
    suiteNameTemplate: '{filename}',
    classNameTemplate: '{classname}',
    titleTemplate: '{title}',
    outputDirectory: 'build/test-results/ui',
  }]],
}
