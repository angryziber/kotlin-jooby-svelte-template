import './assets/scss/main.scss'
import * as i18n from './i18n'
import router from './routing/Router'
import App from './App.svelte'
import jsErrorHandler from './jsErrorHandler'

i18n.init()
router.interceptHrefs()
window.onerror = jsErrorHandler

const app = new App({
	target: document.getElementById('app')!,
	props: {
		initialUser: window['initialUser']
	}
})

export default app
