import {act, render} from '@testing-library/svelte'
import Toasts from './Toasts.svelte'
import {showToast} from './toastStore'

it('renders regular toast', async () => {
  const {container} = render(Toasts)
  await act(() => showToast('Great success', {type: 'success'}))
  expect(container.querySelector('.toasts-container .toast-success')).toContainHTML('Great success')
})

it('renders modal toast', async () => {
  const {container} = render(Toasts)
  await act(() => showToast('Great warning', {type: 'warning', modal: true}))
  expect(container.querySelector('.modal .alert-warning')).toContainHTML('Great warning')
  expect(container.querySelector('.toasts-container .modal')).not.toBeInTheDocument()
})
