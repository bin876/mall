import './styles/tailwind.css'
import './styles/index.scss'

import { createApp } from 'vue'
import App from './App.vue'

import { registerPlugins } from './plugins'

import { vHas } from '@/directives/has'

const app = createApp(App)

registerPlugins(app)

app.directive('has', vHas)

app.mount('#app')
