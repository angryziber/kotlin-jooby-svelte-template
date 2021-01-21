import langs from './langs'

export const defaultLang = 'en'

export function ensureSupportedLang(lang: string) {
  return lang in langs ? lang : defaultLang
}

export function translate(lang: string, key: string, options?: { values: object }): string {
  const keys = key.split('\.')
  let result = langs[lang] || langs[defaultLang]

  for (let k of keys) {
    result = result[k]
    if (!result) return lang === defaultLang ? key : translate(defaultLang, key, options)
  }

  if (result && options?.values) {
    const pluralRules = new Intl.PluralRules(lang)
    result = replaceValues(pluralRules, result, options.values)
  }

  return result || key
}

function replaceValues(pluralRules, text, values) {
  Object.entries(values)
    .forEach(
      ([k, v]) => {
        const pluralMatch = text.match(
          new RegExp(`\{${k}\\|.*?${v != 0 ? pluralRules.select(v) : 'null'}:([^|]*).*?}`))

        text = text.replace(new RegExp(`\{${k}}`, 'g'), v)

        if (pluralMatch !== null) {

          // fallback solution as not all browsers support lookbehind operations to implement escape sequences
          let replaceString = pluralMatch[1]
          let i = replaceString.indexOf('#')
          while (i >= 0) {
            if (i > 0) {
              if (replaceString.codePointAt(i) === '#' && replaceString.codePointAt(i - 1) !== '\\')
                replaceString = replaceString.substring(0, i - 1) + v + replaceString.substring(i + 1, replaceString.length)
            } else
              replaceString = v + replaceString.substring(1, replaceString.length)
            i = replaceString.indexOf('#', i + 1)
          }
          text = text.replace(
            new RegExp(`\{${k}\\|.*?}`, 'g'),
            replaceString
          )
        }
      }
    )
  return text
}
