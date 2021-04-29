import {act, render} from '@testing-library/svelte'
import Toasts from './Toasts.svelte'
import {showToast} from './toastStore'
import {expect} from 'chai'

it('renders regular toast', async () => {
  const {container} = render(Toasts)
  await act(() => showToast('Great success', {type: 'success'}))
  expect(container.querySelector('.toasts-container .toast-success')!.innerHTML).to.contain('Great success')
})

it('renders modal toast', async () => {
  const {container} = render(Toasts)
  await act(() => showToast('Great warning', {type: 'warning', modal: true}))
  expect(container.querySelector('.modal .alert-warning')!.innerHTML).to.contain('Great warning')
  expect(container.querySelector('.toasts-container .modal')).to.be.null
})
