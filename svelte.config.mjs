import sveltePreprocess from 'svelte-preprocess'

export default {
  dev: process.env.NODE_ENV === 'development',
  preprocess: sveltePreprocess()
}
