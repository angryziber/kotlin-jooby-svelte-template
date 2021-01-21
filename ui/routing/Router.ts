import {get} from 'svelte/store'
import {lang} from '@ui/i18n'
class Router {
  interceptHrefs() {
    document.addEventListener('click', this.handleGlobalClick)
  }

  handleGlobalClick = (e: MouseEvent) => {
    let href, element = e.target as HTMLAnchorElement
    while (element && !(href = element.href)) element = element.parentElement as HTMLAnchorElement
    if (element && href?.startsWith('app:')) {
      e.preventDefault()
      this.navigateTo(href.substring(4))
    }
  }

  currentPage(path: string): string {
    return path.replace(/^\/(.*?\/app\/)?/, '')
  }

  matches(pattern: string, page: string): object|false {
    if (page === pattern) return {}
    const patternParts = pattern.split('/')
    const pageParts = page.split('/')
    const params = {}
    if (patternParts.length !== pageParts.length) return false
    let i = 0
    for (let patternPart of patternParts) {
      const pagePart = pageParts[i++]
      if (patternPart.startsWith(':') && pagePart)
        params[patternPart.substring(1)] = pagePart
      else if (pagePart !== patternPart)
        return false
    }
    return params
  }

  fullUrl(page: string, origin = location.origin): string {
    return origin + this.fullPath(page)
  }

  fullPath(page: string): string {
    return `/${get(lang)}/app/${page}`
  }

  navigateTo(page: string, options = {replaceHistory: false}) {
    const fullPath = this.fullPath(page)
    if (options.replaceHistory) history.replaceState(null, '', fullPath)
    else history.pushState(null, '', fullPath)
    window.dispatchEvent(new Event('popstate'))
  }

  navigateWithReload(url: string) {
    location.href = url
  }
}

export default new Router()
