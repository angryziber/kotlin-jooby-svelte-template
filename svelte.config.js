// Note: this file cannot be named .mjs because snowpack-plugin svelte looks for file with .js extension
const preprocess = require('svelte-preprocess')

module.exports = {
  dev: process.env.NODE_ENV === 'development',
  preprocess: preprocess({typescript: {compilerOptions: {target: 'es2020'}}})
}
