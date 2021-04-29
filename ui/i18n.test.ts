import * as i18n from './i18n'
import {tryTranslate} from './i18n'
import {get} from 'svelte/store'
import {$_} from './test-utils'
import {expect} from 'chai'
import {fake} from 'sinon'

it('language is saved to cookie and url is replaced', () => {
  const location = {
    pathname: '/ja/about/',
    search: '?hello',
    hash: '#blah'
  }
  const history = {pushState: fake()}
  i18n.rememberLang('de', history as any, location as any)
  expect(document.cookie.includes('LANG=de')).to.equal(true)
  expect(history.pushState).calledWith(null, '', '/de/about/?hello#blah')
})

it('contains same number of translations for each lang', () => {
  const langs = get(i18n.langs)
  const numEnTranslations = Object.keys(langs['en']).length
  expect(Object.entries(langs).find(([lang, entries]) => Object.keys(entries as any).length != numEnTranslations)).to.be.undefined
})

it('datetime formatting', () => {
  expect(i18n.formatDateTime(undefined)).to.equal('')
  expect(i18n.formatDateTime(new Date())).to.contain(new Date().getFullYear().toString())
  expect(i18n.formatDateTime('2020-01-01T10:23:45.010101')).to.contain('10:23')
  expect(i18n.formatDateTime(123)).to.contain('1970')
})

it('translate keys with special prefix', () => {
  const key = 'i18n:title'
  expect(tryTranslate(key)).to.equal($_('App Template'))
})
