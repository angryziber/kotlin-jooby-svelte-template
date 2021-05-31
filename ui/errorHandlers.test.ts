import {expect} from 'chai'
import {$_} from '@ui/test-utils'
import {handleUnhandledRejection} from '@ui/errorHandlers'
import {toastStore} from '@ui/shared/toastStore'
import {get} from 'svelte/store'

describe('handles unhandled promises', () => {
  const promise = Promise.reject('')
  afterEach(() => toastStore.set([]))

  it('without translation', async () => {
    const e = new PromiseRejectionEvent('unhandledrejection', {reason: {message: 'no translation'}, promise})
    handleUnhandledRejection(e)
    expect(get(toastStore).last().message).to.eq('no translation')
  })

  it('with translation', async () => {
    const e = new PromiseRejectionEvent('unhandledrejection', {reason: {message: 'errors.technical', statusCode: 500}, promise})
    handleUnhandledRejection(e)
    expect(get(toastStore).last().message).to.eq($_('errors.technical'))
  })
})
