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
        <script src="/_dist_/ui/shared/ArrayExtensions.js"></script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>`
}
