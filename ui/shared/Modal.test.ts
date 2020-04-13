import {render} from '@testing-library/svelte'
import Modal from './Modal.svelte'

test('Modal is shown', (done) => {
  const {container, component} = render(Modal, {props: {title: 'Title', show: false, flyParams: {duration: 0}}})
  expect(container).not.toContainHTML('Title')

  component.$set({show: true})
  setTimeout(() => {
    expect(container).toContainHTML('Title')

    expect(document.body).toHaveClass('modal-open')
    expect(document.querySelector('body > .modal-backdrop')).toBeInTheDocument()

    component.$set({show: false})
    setTimeout(() => {
      expect(document.body).not.toHaveClass('modal-open')
      expect(document.querySelector('body > .modal-backdrop')).not.toBeInTheDocument()
      done()
    }, 100)
  })
})
