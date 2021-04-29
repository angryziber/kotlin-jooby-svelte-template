import {fireEvent, render} from '@testing-library/svelte'
import Popover from './Popover.svelte'

it('is shown and hidden on hover', async () => {
  const {container} = render(Popover, {props: {title: 'Title', text: 'Text', containerClass: 'container'}})
  expect(container.querySelector('.popover')).toBeFalsy()
  const slot = container.querySelector('span.container')! as HTMLElement
  jest.spyOn(slot, 'getBoundingClientRect').mockReturnValue({x: 20, y: 30} as any)
  await fireEvent.mouseEnter(slot)

  const popover = document.querySelector('body > .popover') as HTMLElement
  expect(popover).toContainHTML('Title')
  expect(popover).toContainHTML('Text')
  expect(popover.style.top).to.equal('30px')

  await fireEvent.mouseLeave(slot)
  expect(document.querySelector('body > .popover')).toBeFalsy()
})
