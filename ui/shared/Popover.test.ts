import {fireEvent, render} from '@testing-library/svelte'
import Popover from './Popover.svelte'
import {expect} from 'chai'
import {stub} from 'sinon'

it('is shown and hidden on hover', async () => {
  const {container} = render(Popover, {props: {title: 'Title', text: 'Text', containerClass: 'container'}})
  expect(container.querySelector('.popover')).to.be.null
  const slot = container.querySelector('.container') as HTMLElement
  stub(slot, 'getBoundingClientRect').returns({x: 20, y: 30} as any)
  await fireEvent.mouseEnter(slot)

  const popover = document.querySelector('body > .popover') as HTMLElement
  expect(popover.textContent).to.contain('Title')
  expect(popover.textContent).to.contain('Text')
  // TODO expect(popover.style.top).to.equal('30px')

  await fireEvent.mouseLeave(slot)
  expect(document.querySelector('body > .popover')).to.be.null
})
