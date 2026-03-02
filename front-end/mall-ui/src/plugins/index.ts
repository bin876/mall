import { type App } from 'vue'
import { setupElementPlus } from './element-plus'
import { setupRouter } from './router'
import { setupPinia } from './pinia'

export function registerPlugins(app: App) {
  setupPinia(app)
  setupElementPlus(app)
  setupRouter(app)
}
