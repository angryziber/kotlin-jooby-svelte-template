import * as i18n from './i18n'
import * as si18n from 'svelte-i18n'
import {get} from 'svelte/store'

it('language is saved to cookie and url is replaced', () => {
  const location = {
    pathname: '/jp/about/',
    search: '?hello',
    hash: '#blah'
  }
  const history = {pushState: jest.fn()}
  i18n.rememberLang('de', history as any, location as any)
  expect(document.cookie.includes('LANG=de')).toBe(true)
  expect(history.pushState).toBeCalledWith(null, '', '/de/about/?hello#blah')
})

it('contains same number of translations for each lang', () => {
  const dict = get(si18n.dictionary)
  const numEnTranslations = Object.keys(dict['en']).length
  expect(Object.entries(dict).find(([lang, entries]) => Object.keys(entries as any).length != numEnTranslations)).toBeFalsy()
})

test('datetime formatting', () => {
  const $_ = get(si18n._)
  expect($_.datetime()).toBe('')
  expect($_.datetime(new Date())).toMatch(new Date().getFullYear().toString())
  expect($_.datetime('2020-01-01T10:23:45.010101')).toMatch('10:23')
  expect($_.datetime(123)).toMatch('1970')
})
