const preprocess = {
  script(input) {
    input.content = `import {_, formatDate, formatDateTime} from '@ui/i18n'\n` + input.content;
    return {code: input.content}
  }
};

module.exports = {
  dev: process.env.NODE_ENV === 'development',
  preprocess,
}
