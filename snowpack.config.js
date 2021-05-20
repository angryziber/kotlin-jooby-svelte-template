const proxy = require('http2-proxy')

const svelteIgnore = [
  'a11y-autofocus',
  'a11y-no-onchange',
  'a11y-missing-attribute',
  'a11y-label-has-associated-control'
]

const isTest = process.env.NODE_ENV === 'test'

const plugins = [
  '@snowpack/plugin-typescript',
  '@snowpack/plugin-svelte',
  [
    '@snowpack/plugin-run-script',
    {cmd: 'svelte-check --output human --compiler-warnings ' + svelteIgnore.map(i => i + ':ignore').join(','), watch: '$1 --watch', output: 'stream'}
  ],
  [
    'snowpack-plugin-rollup-bundle',
    {
      emitHtmlFiles: false,
      preserveSourceFiles: false,
      entrypoints: 'build/public/_dist_/ui/main.js',
      extendConfig: config => {
        config.outputOptions.entryFileNames = '[name].js'
        config.outputOptions.assetFileNames = 'css/[name].[ext]'
        return config
      }
    }
  ]
]

if (!isTest) plugins.push(
  ['@snowpack/plugin-run-script', {
    cmd: 'sass -I node_modules -I stylebook/scss ui/assets/scss:public/css --no-source-map --style=compressed', watch: 'sass -I node_modules -I stylebook/scss ui/assets/scss:public/css --embed-source-map --watch'
  }]
)

module.exports = {
  mount: {
    public: '/',
    ui: '/_dist_/ui',
    i18n: '/_dist_/i18n'
  },
  alias: {
    '@ui': './ui'
  },
  exclude: [
    '**/node_modules/**/*',
    '**/*.test.ts',
    '**/ui/test-utils.ts',
    '**/ui/setup-tests.ts',
    '**/_*.scss'
  ],
  plugins: [
    ['@snowpack/plugin-run-script', {
      cmd: 'sass ui/assets/scss:public/css --no-source-map --style=compressed', watch: 'sass ui/assets/scss:public/css --embed-source-map --watch'
    }],
    '@snowpack/plugin-typescript',
    '@snowpack/plugin-svelte',
    [
      '@snowpack/plugin-run-script',
      {cmd: 'svelte-check --output human --compiler-warnings ' + svelteIgnore.map(i => i + ':ignore').join(','), watch: '$1 --watch', output: 'stream'}
    ],
    [
      'snowpack-plugin-rollup-bundle',
      {
        emitHtmlFiles: false,
        preserveSourceFiles: false,
        entrypoints: 'build/public/_dist_/ui/main.js',
        extendConfig: config => {
          config.outputOptions.entryFileNames = '[name].js'
          config.outputOptions.assetFileNames = 'css/[name].[ext]'
          return config
        }
      }
    ]
  ],
  packageOptions: {
    knownEntrypoints: ['tslib'].concat(isTest ? ['sinon', 'chai', 'sinon-chai', '@testing-library/svelte'] : [])
  },
  buildOptions: {
    out: 'build/public',
    sourcemap: true
  },
  routes: [
    {src: '/api/.*', dest: (req, res) => proxy.web(req, res, {hostname: 'localhost', port: 8080}).catch(() => res.end())},
    {match: 'routes', src: '.*', dest: '/index.html'}
  ],
  devOptions: {
    port: isTest ? 8678 : 8088,
    open: 'none'
  }
}
