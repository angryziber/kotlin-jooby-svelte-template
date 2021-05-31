import './assets/scss/main.scss'
import router from './routing/Router'
import App from './App.svelte'
import {initErrorHandlers} from './errorHandlers'
import './shared/ArrayExtensions'

initErrorHandlers()
router.interceptHrefs()

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
