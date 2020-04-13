class Router {
  interceptHrefs() {
    document.addEventListener('click', this.handleGlobalClick)
  }

  handleGlobalClick = (e: MouseEvent) => {
    let href, element = e.target as HTMLAnchorElement
    while (element && !(href = element.href)) element = element.parentElement as HTMLAnchorElement
    if (element && href && href.length && href.startsWith('app:')) {
      e.preventDefault()
      this.navigateTo(href.substring(4))
    }
  }

  currentPage(path: string): string {
    return path.replace(/^.*?\/app\//, '')
  }

  matches(pattern: string, page: string): object|false {
    if (page === pattern) return {}
    const patternParts = pattern.split('/')
    const pageParts = page.split('/')
    const params = {}
    if (patternParts.length !== pageParts.length) return false
    for (const i in patternParts) {
      const patternPart = patternParts[i], pagePart = pageParts[i]
      if (patternPart.startsWith(':') && pagePart)
        params[patternPart.substring(1)] = pagePart
      else if (pagePart !== patternPart)
        return false
    }
    return params
  }

  fullUrl(page: string, href = location.href): string {
    return href.replace(/\/app\/.*$/, '/app/') + page
  }

  navigateTo(page: string, options = {replaceHistory: false}) {
    const fullPath = this.fullUrl(page, location.pathname)
    if (options.replaceHistory) history.replaceState(null, '', fullPath)
    else history.pushState(null, '', fullPath)
    window.dispatchEvent(new Event('popstate'))
  }

  navigateWithReload(url: string) {
    location.href = url
  }
}

export default new Router()
