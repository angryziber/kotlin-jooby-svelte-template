import preprocess from 'svelte-preprocess'

export default {
  dev: process.env.NODE_ENV === 'development',
  preprocess: preprocess({typescript: {compilerOptions: {target: 'esnext'}}})
}
