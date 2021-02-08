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

  if (result && options?.values) result = replaceValues(lang, result, options.values)
  return result ?? key
}

function replaceValues(lang: string, text: string, values: object) {
  let lastPos = 0, bracePos = 0, result = ''
  while ((bracePos = text.indexOf('{', lastPos)) >= 0) {
    result += text.substring(lastPos, bracePos)
    let closingPos = text.indexOf('}', bracePos)
    const textToReplace = text.substring(bracePos + 1, closingPos)
    result += replacePlaceholder(textToReplace, values, lang)
    lastPos = closingPos + 1
  }
  result += text.substring(lastPos)
  return result
}

function replacePlaceholder(text: string, values: object, lang: string) {
  const pluralTokens = text.split('|')
  const field = pluralTokens.first()
  if (pluralTokens.length == 1) return values[field] ?? field

  const pluralRules = new Intl.PluralRules(lang)
  const key = values[field] === 0 ? 'zero' : pluralRules.select(values[field])

  for (let i = 1; i < pluralTokens.length; i++) {
    const [candidateKey, candidateText] = pluralTokens[i].split(':', 2)
    if (candidateKey === key) return candidateText.replace('#', values[field])
  }
  return field
}
