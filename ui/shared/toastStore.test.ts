import {hideToast, showToast, toastStore} from './toastStore'
import {get} from 'svelte/store'

beforeAll(() => jest.useFakeTimers())
afterAll(() => jest.useRealTimers())

test('show/hide toast', () => {
  const toast1 = showToast('Created stuff')
  expect(toast1.type).to.equal('success')
  expect(toast1.timeoutSec).to.equal(10)
  const toast2 = showToast('Failed', {type: 'error', timeoutSec: 25})
  expect(toast2.type).to.equal('error')
  expect(toast2.timeoutSec).to.equal(25)

  expect(get(toastStore)).to.deep.equal([toast1, toast2])

  hideToast(toast1)
  expect(get(toastStore)).to.deep.equal([toast2])

  jest.runAllTimers()
  expect(get(toastStore)).to.deep.equal([])
})
