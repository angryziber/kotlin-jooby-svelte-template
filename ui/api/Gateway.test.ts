import gateway, {headers} from './Gateway'

it('extracts json', async () => {
  const fetch = jest.fn().mockResolvedValue({json: () => 'data'})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(document.documentElement.classList.contains('loading')).toBe(true)
  expect(await promise).toBe('data')
  expect(fetch).toBeCalledWith('/path', {headers, body: '{"data":"data"}'})
  expect(document.documentElement.classList.contains('loading')).toBe(false)
})

it('supports null json response', async () => {
  const fetch = jest.fn().mockResolvedValue({json: () => null})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(await promise).toBe(null)
})

it('supports No Content response', async () => {
  const fetch = jest.fn().mockResolvedValue({status: 204})
  const promise = gateway.request('/path', {body: {data: 'data'}}, fetch)
  expect(await promise).toBe(undefined)
})

it('handles http error', () => {
  const fetch = jest.fn().mockResolvedValue({json: () => Promise.resolve({statusCode: 403, message: '', reason: 'Forbidden'})})
  return gateway.request('/path', {headers}, fetch).then(() => {throw 'should be rejected'}, e => {
    expect(e).toEqual({statusCode: 403, message: 'Forbidden', reason: 'Forbidden'})
    expect(document.documentElement.classList.contains('loading')).toBe(false)
  })
})

it('sends get', () => {
  jest.spyOn(gateway, 'request')
  gateway.get('/path')
  expect(gateway.request).toBeCalledWith('/path')
})

it('sends post', () => {
  jest.spyOn(gateway, 'request')
  gateway.post('/path', {data: 'data'})
  expect(gateway.request).toBeCalledWith('/path', {method: 'POST', body: {data: 'data'}})
})

it('sends delete', () => {
  jest.spyOn(gateway, 'request')
  gateway.delete('/path')
  expect(gateway.request).toBeCalledWith('/path', {method: 'DELETE'})
})

it('sends patch', () => {
  jest.spyOn(gateway, 'request')
  gateway.patch('/path', {data: 'data'})
  expect(gateway.request).toBeCalledWith('/path', {method: 'PATCH', body: {data: 'data'}})
})

it('gives a specific error when failed to parse JSON', () => {
  const fetch = jest.fn().mockResolvedValue({json: () => { throw 'Invalid json'}})
  const message = 'errors.notJson'

  expect.assertions(1)
  return expect(gateway.request('/path', {headers: {'Accept': 'application/json'}}, fetch)).rejects.toEqual({message})
})

it('gives a network error', () => {
  const fetch = jest.fn().mockRejectedValue({message: 'Failed to fetch'})
  const message = 'errors.networkUnavailable'

  expect.assertions(1)
  return expect(gateway.request('/path', {headers: {'Accept': 'application/json'}}, fetch)).rejects.toEqual({message})
})

describe('disabling form buttons on submit', () => {
  let form, button
  let fetch

  beforeEach(() => {
    fetch = jest.fn().mockResolvedValue({json: () => 'data'})

    form = document.createElement('form')
    button = document.createElement('button')
    form.appendChild(button)
    document.body.appendChild(form)
  })

  it('disable any form button', async () => {
    const promise = gateway.request('/path', {method: 'POST'}, fetch)
    expect(button).toBeDisabled()
    await promise
    expect(button).not.toBeDisabled()
  })

  it('does not disable anything for GET', async () => {
    const promise = gateway.request('/path', {method: 'GET'}, fetch)
    expect(button).not.toBeDisabled()
    await promise
    expect(button).not.toBeDisabled()
  })

  it('does not enable disabled buttons', async () => {
    const disabledButton = document.createElement('button')
    disabledButton.disabled = true
    form.appendChild(disabledButton)

    const promise = gateway.request('/path', {method: 'PATCH'}, fetch)
    expect(disabledButton).toBeDisabled()
    await promise
    expect(disabledButton).toBeDisabled()
  })

  it('enable button even if it is removed from DOM', async () => {
    const promise = gateway.request('/path', {method: 'DELETE'}, fetch)
    expect(button).toBeDisabled()
    form.removeChild(button)
    await promise
    expect(button).not.toBeDisabled()
  })
})