export const headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}

class Gateway {
  request(path: string, init?: RequestInit | {body: object}, fetch = window.fetch) {
    document.documentElement.classList.add('loading')
    const disabledButtons = this.disableSubmitButtons((init as RequestInit)?.method)

    return fetch(path, {
      ...init, headers,
      body: init?.body && JSON.stringify(init.body)
    })
    .then(this.extractJsonHandlingErrors)
    .catch(this.handleFetchFailure)
    .finally(() => {
      document.documentElement.classList.remove('loading')
      disabledButtons?.forEach(btn => btn.disabled = false)
    })
  }

  private async extractJsonHandlingErrors(response: Response): Promise<any> {
    let data
    try {
      data = response.status == 204 ? undefined : await response.json()
    } catch (e) {
      console.error('Not a JSON', e)
      throw {message: 'errors.notJson'}
    }
    if (response.status < 200 || response.status >= 400) {
      data.message = data.message || data.reason
      throw data
    }
    return data
  }

  private handleFetchFailure(error) {
    if (error.message === 'Failed to fetch') throw {message: 'errors.networkUnavailable'}
    else throw error
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
