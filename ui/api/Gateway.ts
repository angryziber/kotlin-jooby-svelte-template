export const headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}

class Gateway {
  request(path: string, init?: RequestInit | {body: object}, fetch = window.fetch) {
    document.documentElement.classList.add('loading')
    const disabledButtons = this.disableSubmitButtons(init && (init as RequestInit).method)

    return fetch(path, {
      ...init, headers,
      body: init && init.body && JSON.stringify(init.body)
    })
    .then(this.tryToExtractPayload)
    .then(this.checkStatusCode)
    .catch(this.handleFetchFailure)
    .finally(() => {
      document.documentElement.classList.remove('loading')
      disabledButtons && disabledButtons.forEach(btn => btn.disabled = false)
    })
  }

  private handleFetchFailure(error) {
    if (error.message === 'Failed to fetch') {
      throw {message: 'errors.networkUnavailable'}
    } else {
      throw error
    }
  }

  private async tryToExtractPayload(response: Response): Promise<object|undefined> {
    try {
      return response.status == 204 ? undefined: await response.json()
    } catch (e) {
      console.error('Not a JSON', e)
      throw {message: 'errors.notJson'}
    }
  }

  private checkStatusCode(data) {
    if (data && data.statusCode) throw {...data, message: data.message || data.reason}
    else return data
  }

  async get(path: string) {
    return await this.request(path)
  }

  async post(path: string, body: object = {}) {
    return await this.request(path, {method: 'POST', body})
  }

  async delete(path: string) {
    return await this.request(path, {method: 'DELETE'})
  }

  async patch(path: string, body: object = {}) {
    return await this.request(path, {method: 'PATCH', body})
  }

  private disableSubmitButtons(method?: string) {
    if (method === 'GET') return
    const buttons = document.querySelectorAll<HTMLButtonElement>("form button:not(:disabled)")
    buttons.forEach(btn => btn.disabled = true)
    return buttons
  }
}

export default new Gateway()
