import {render} from '@testing-library/svelte'
import Modal from './Modal.svelte'

test('Modal is shown', (done) => {
  const {container, component} = render(Modal, {title: 'Title', show: false, flyParams: {duration: 0}})
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

test('body.modal-open is added on show and removed on destroy', () => {
  const {component} = render(Modal, {title: 'Title', show: true, flyParams: {duration: 0}})
  expect(document.body).toHaveClass('modal-open')
  component.$destroy()
  expect(document.body).not.toHaveClass('modal-open')
})
