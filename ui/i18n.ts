import {derived, get, writable} from 'svelte/store'
import dictionary from '../i18n/langs'
import {defaultLang, translate} from '../i18n/i18n'

const LANG_COOKIE = 'LANG'
export const PATH_LANG_REGEX = /^\/([a-z]{2})\//

export const lang = writable(location.pathname.match(PATH_LANG_REGEX)?.[1] ?? defaultLang)
lang.subscribe(lang => window['ga']?.('set', 'language', lang))

export const langs = writable(dictionary)

export const _ = derived([lang, langs], lang => translate.bind(this, lang[0]))

type Date_ = string|number|Date|undefined
const toDate = (d: Date_) => d instanceof Date ? d : d ? new Date(d) : undefined
export const formatDate = (d: Date_, _lang = get(lang)) => toDate(d)?.toLocaleDateString(_lang) ?? ''
export const formatDateTime = (d: Date_, _lang = get(lang)) => toDate(d)?.toLocaleString(_lang) ?? ''

export function rememberLang(lang, history = window.history, location = window.location) {
  document.cookie = `${LANG_COOKIE}=${lang};path=/`
  history.pushState(null, '', location.pathname.replace(PATH_LANG_REGEX, `/${lang}/`) + location.search + location.hash)
}

export function choices(prefix: string, locale?: string): object {
  let values = dictionary[locale || get(lang)]
  return findByPrefix(prefix, values)
}

export function tryTranslate(message: string): string {
  message = message || '';
  if (!message.startsWith('i18n:')) return message
  return findByPrefix(message.substring(5), dictionary[get(lang)])
}

function findByPrefix(prefix: string, values) {
  for (let p of prefix.split('.')) {
    values = values[p]
    if (!values) break
  }
  return values
}

export function use(a, f: (a) => any, other) {
  return a && f(a) || other
}
