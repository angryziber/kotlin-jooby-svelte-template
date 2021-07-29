import proxy from 'http2-proxy'

const svelteIgnore = [
  'a11y-autofocus',
  'a11y-missing-attribute',
  'a11y-label-has-associated-control'
]

const isTest = process.env.NODE_ENV === 'test'

const plugins = [
  '@snowpack/plugin-typescript',
  '@snowpack/plugin-svelte',
  ['@snowpack/plugin-run-script',
    {cmd: 'svelte-check --output human --compiler-warnings ' + svelteIgnore.map(i => i + ':ignore').join(','), watch: '$1 --watch', output: 'stream'}
  ]
]

if (!isTest) {
  const sassCmd = 'sass -I node_modules ui/assets/scss:public/css'
  plugins.push(['@snowpack/plugin-run-script', {
    cmd: `${sassCmd} --no-source-map --style=compressed`, watch: `${sassCmd} --embed-source-map --watch`
  }])
}

const proxyOptions = {
  hostname: 'localhost', port: 8080,
  onReq: (req, {headers}) => {headers['x-forwarded-host'] = req.headers['host']}
}

/** @type {import("snowpack").SnowpackUserConfig } */
export default {
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
  plugins,
  packageOptions: {
    knownEntrypoints: isTest ? ['sinon', 'chai', 'sinon-chai', '@testing-library/svelte'] : []
  },
  buildOptions: {
    out: 'build/public',
    sourcemap: true
  },
  optimize: {
    sourcemap: false,
    minify: true,
    target: 'es2020'
  },
  routes: [
    {src: '/api/.*', dest: (req, res) => proxy.web(req, res, proxyOptions).catch(() => res.end())},
    {match: 'routes', src: '.*', dest: '/index.html'}
  ],
  devOptions: {
    port: isTest ? 8678 : 8088,
    open: 'none'
  }
}
