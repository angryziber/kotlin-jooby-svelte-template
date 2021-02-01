const sveltePreprocess = require('svelte-preprocess')

const preprocess = sveltePreprocess({
  javascript: {
    prependData: `import {_, formatDate, formatDateTime} from '@ui/i18n'\n`
  }
})

module.exports = {
  dev: process.env.NODE_ENV === 'development',
  preprocess
}
