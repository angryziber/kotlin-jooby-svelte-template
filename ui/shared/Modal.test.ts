import {render} from '@testing-library/svelte'
import Modal from './Modal.svelte'
import {expect} from 'chai'

it('Modal is shown', (done) => {
  const {container, component} = render(Modal, {title: 'Title', show: false, flyParams: {duration: 0}})
  expect(container.textContent).not.to.contain('Title')

  component.$set({show: true})
  setTimeout(() => {
    expect(container.textContent).to.contain('Title')

    expect(document.body.classList.contains('modal-open')).to.be.true
    expect(document.querySelector('body > .modal-backdrop')).to.be.ok

    component.$set({show: false})
    setTimeout(() => {
      expect(document.body.classList.contains('modal-open')).to.be.false
      expect(document.querySelector('body > .modal-backdrop')).to.be.null
      done()
    }, 100)
  })
})

it('body.modal-open is added on show and removed on destroy', () => {
  const {component} = render(Modal, {title: 'Title', show: true, flyParams: {duration: 0}})
  expect(document.body.classList.contains('modal-open')).to.be.true
  component.$destroy()
  expect(document.body.classList.contains('modal-open')).to.be.false
})
