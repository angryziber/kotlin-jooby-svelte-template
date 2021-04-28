// NODE_ENV=test - Needed by "@snowpack/web-test-runner-plugin"
process.env.NODE_ENV = 'test';

module.exports = {
  plugins: [require('@snowpack/web-test-runner-plugin')()],
  testRunnerHtml: testFramework =>
    `<html>
      <body>
        <script src="/_dist_/ui/shared/ArrayExtensions.js"></script>
        <script type="module" src="${testFramework}"></script>
      </body>
    </html>`,
};
