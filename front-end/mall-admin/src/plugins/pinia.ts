import type { App } from 'vue'
import piniaPersistedstate from 'pinia-plugin-persistedstate'
import store from '@/stores'

export function setupPinia(app: App) {
  store.use(piniaPersistedstate)
  app.use(store)
}
