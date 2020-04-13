const preprocess = {
  script(input) {
    input.content = `import { _, locale, format } from 'svelte-i18n'\n` + input.content;
    return {code: input.content}
  }
};

module.exports = {
  dev: process.env.NODE_ENV !== 'development',
  preprocess,
}
