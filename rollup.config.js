import svelte from 'rollup-plugin-svelte'
import resolve from '@rollup/plugin-node-resolve'
import commonjs from '@rollup/plugin-commonjs'
import json from '@rollup/plugin-json'
import livereload from 'rollup-plugin-livereload'
import {terser} from 'rollup-plugin-terser'
import scss from 'rollup-plugin-scss'
import autoprefixer from 'autoprefixer'
import postcss from 'postcss'
import typescript from 'rollup-plugin-typescript2'

const svelteOptions = require('./svelte.config')

const production = !process.env.ROLLUP_WATCH

// TODO: a hack to enable watching of scss changes until master of plugin is released: https://github.com/thgh/rollup-plugin-scss/issues/43
const scssPluginWithWatch = scss({
  output: 'public/build/bundle.css',
  outputStyle: production && 'compressed',
  processor: css => postcss(autoprefixer).process(css, {from: undefined}).then(result => result.css)
})
const scssTransform = scssPluginWithWatch.transform
scssPluginWithWatch.transform = function(...args) {
  this.addWatchFile('ui/assets/scss')
  this.addWatchFile('ui/assets/scss/components')
  this.addWatchFile('ui/assets/scss/components/utilities')
  return scssTransform(...args)
}

export default {
  input: 'ui/main.ts',
  output: {
    sourcemap: true,
    format: 'iife',
    name: 'app',
    dir: 'public/build/'
  },
  onwarn: function(warning, handler) {
    if (warning.code === 'THIS_IS_UNDEFINED' && warning.loc.file.includes('/node_modules/')) return
    handler(warning)
  },
  plugins: [
    json(),
    scssPluginWithWatch,
    svelte({
      ...svelteOptions,
      // enable run-time checks when not in production
      dev: !production,
      // we'll extract any component CSS out into
      // a separate file — better for performance
      css: css => {
        css.write('public/build/components.css')
      },
      onwarn: (warning, handler) => {
        if (warning.code === 'a11y-autofocus') return
        if (warning.code === 'a11y-missing-attribute' && warning.message.includes('<a>') && warning.message.includes('href')) return
        handler(warning)
      }
    }),

    // If you have external dependencies installed from
    // npm, you'll most likely need these plugins. In
    // some cases you'll need additional configuration —
    // consult the documentation for details:
    // https://github.com/rollup/plugins/tree/master/packages/commonjs
    resolve({
      browser: true,
      dedupe: importee => importee === 'svelte' || importee.startsWith('svelte/')
    }),
    commonjs(),
    typescript(),

    // Watch the `public` directory and refresh the
    // browser on changes when not in production
    !production && livereload('public'),

    // If we're building for production (npm run build
    // instead of npm run dev), minify
    production && terser()
  ],
  watch: {
    clearScreen: false
  }
}
