import './assets/scss/main.scss'
import router from './routing/Router'
import App from './App.svelte'
import jsErrorHandler from './jsErrorHandler'
import './shared/ArrayExtensions'

router.interceptHrefs()
window.onerror = jsErrorHandler

const app = new App({target: document.getElementById('app')!})

export default app

// Hot Module Replacement (HMR) - Remove this snippet to remove HMR.
// Learn more: https://www.snowpack.dev/#hot-module-replacement
if (import.meta['hot']) {
	import.meta['hot'].accept()
	import.meta['hot'].dispose(() => {
		app.$destroy()
	})
}
