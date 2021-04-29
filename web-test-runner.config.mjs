import snowpackPlugin from '@snowpack/web-test-runner-plugin'

// NODE_ENV=test - Needed by "@snowpack/web-test-runner-plugin"
process.env.NODE_ENV = 'test'

export default {
  plugins: [
    snowpackPlugin()
  ],
  testRunnerHtml: testFramework =>
    `<html>
      <body>
        <script type="module" src="/_dist_/ui/setup-tests-new.js"></script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>`
}
