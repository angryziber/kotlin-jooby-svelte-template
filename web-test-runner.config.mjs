import snowpackPlugin from '@snowpack/web-test-runner-plugin'

// Needed by "@snowpack/web-test-runner-plugin"
process.env.NODE_ENV = 'test'

export default {
  testsFinishTimeout: 10000,
  // manual: true,
  // open: true,
  plugins: [
    snowpackPlugin()
  ],
  testRunnerHtml: testFramework =>
    `<html>
      <body>
        <script type="module" src="/_dist_/ui/setup-tests.js"></script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>`
}
