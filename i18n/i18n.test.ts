import {translate, ensureSupportedLang} from './i18n'
import langs from './langs'

function createEntry(lang: string, key: string, text: string) {
  let keyWalker = langs[lang]
  let splitKey = key.split('.')
  let lastKey = splitKey.pop()
  splitKey.forEach(k => {
    keyWalker[k] = keyWalker[k] || {}
    keyWalker = keyWalker[k]
  })
  keyWalker[lastKey] = text
}

test("test entry creation utility", () => {
  createEntry('en', 'enkeytesting.testing.text', 'works!')
  expect(langs['en']['enkeytesting']['testing']['text']).toEqual('works!')
})

test('backend translate', () => {
  expect(translate('en', 'login.submit')).toBe('Login')
  expect(translate('en', 'login.otp.message', {values: {otp: '000111'}})).toBe('Your one-time password is 000111')
})

test('backend translate with unsupported lang', () => {
  expect(translate('??', 'login.submit')).toBe('Login')
})

test('translation should fall back to en', () => {
  createEntry('en', 'key.that.exists.only.in.en', 'Tere {name}!')
  expect(translate('et', 'key.that.exists.only.in.en', {values: {name: 'Jaan'}})).toBe('Tere Jaan!')
})

test('if translation fails it should return translation key', () => {
  const nonExistingKey = 'some.key.that.does.not.exist'
  expect(translate('et', nonExistingKey)).toBe(nonExistingKey)
})

test('ensureSupportedLang', () => {
  expect(ensureSupportedLang('en')).toBe('en')
  expect(ensureSupportedLang('??')).toBe('en')
})

test('translate strings with plurals', () => {
  const key = 'bup.bap.plural.test'
  createEntry('en', key,
    'Testing {count|zero:nothing|one:a single translation|other:# translations!}')
  createEntry('en', key + '2',
    'Testing {count|zero:nothing|one:a single translation|other:# translations but don\'t \#change this!}')
  expect(translate('en', key, {values: {count: 0}})).toBe("Testing nothing")
  expect(translate('en', key, {values: {count: 1}})).toBe("Testing a single translation")
  expect(translate('en', key, {values: {count: 5}})).toBe("Testing 5 translations!")

  expect(translate('en', key + '2', {values: {count: 12}})).toBe("Testing 12 translations but don't #change this!")
})

test('translate template with plurals and regular substitution', () => {
  const key = 'messages.since'
  createEntry('en', key, 'Tere {username}! You have {n|one:# message|other:# messages} since {date}')
  expect(translate('en', key, {values: {n: 5, date: '2020/01/27', username: 'Piret'}})).toEqual('Tere Piret! You have 5 messages since 2020/01/27')
})

test('empty token', () => {
  const key = 'messages.since'
  createEntry('en', key, '{n|zero:|one:one|other:# messages}')
  expect(translate('en', key, {values: {n: 0}})).toEqual('')
})
