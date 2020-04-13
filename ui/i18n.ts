import * as i18n from 'svelte-i18n'
import {get} from 'svelte/store'

import en from '../i18n/en.json'

const dictionary = {en}

const LANG_COOKIE = 'LANG'
export const PATH_LANG_REGEX = /^\/([a-z]{2})\//

export function init() {
  Object.entries(dictionary).forEach(([key, value]) => i18n.addMessages(key, value))

  i18n.init({
    fallbackLocale: 'en',
    initialLocale: {
      pathname: PATH_LANG_REGEX
    },
    formats: {
      date: {
        short: {month: 'numeric', day: 'numeric', year: 'numeric'},
        full: {year: 'numeric', month: 'short', day: 'numeric'}
      }
    }
  })

  get(i18n._).datetime = function(v) {
    if (!v) return ''
    if (!(v instanceof Date)) v = new Date(v)
    return this.date(v) + ' ' + this.time(v)
  }
}

export function rememberLang(lang, history = window.history, location = window.location) {
  document.cookie = `${LANG_COOKIE}=${lang};path=/`
  history.pushState(null, '', location.pathname.replace(PATH_LANG_REGEX, `/${lang}/`) + location.search + location.hash)
}
