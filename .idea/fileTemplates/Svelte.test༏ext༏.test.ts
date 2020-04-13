import {render} from '@testing-library/svelte'
import ${NAME} from './${NAME}.svelte'

it('renders', async () => {
  const {container} = render(${NAME})
  expect(container).toContainHTML('TODO')
})
