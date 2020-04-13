import {act, render} from '@testing-library/svelte'
import Toasts from './Toasts.svelte'
import {showToast} from './toastStore'

it('renders', async () => {
  const {container} = render(Toasts)
  await act(() => showToast('Great success'))
  expect(container.querySelector('.toast-success')).toContainHTML('Great success')
})
