import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw, Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layouts/Layout.vue'
import { isAuthError } from '@/utils/auth'

const views = import.meta.glob('@/views/**/*.vue')

const loginRoute: RouteRecordRaw = {
  path: '/login',
  name: 'Login',
  component: () => import('@/views/login/index.vue'),
}

export const rootRoute: RouteRecordRaw = {
  path: '/',
  name: 'Root',
  component: Layout,
  redirect: '/dashboard',
  children: [],
}

const router = createRouter({
  history: createWebHistory(),
  routes: [loginRoute, rootRoute],
})

const getComponent = (componentPath: string) => {
  const absolutePath = `/src/views/${componentPath}.vue`
  const component = views[absolutePath]

  if (!component) {
    console.warn(`Component not found: ${absolutePath}`)
    console.warn('Available components:', Object.keys(views))
    return () => import('@/views/404.vue')
  }

  return component
}

const buildDynamicRoutes = (menus: any[], parentPath = ''): RouteRecordRaw[] => {
  return menus.map((menu) => {
    const fullPath = menu.path.startsWith('/') ? menu.path : `${parentPath}/${menu.path}`

    const route: RouteRecordRaw = {
      path: fullPath,
      name: menu.name,
      meta: {
        title: menu.title,
        icon: menu.icon,
        hidden: menu.hidden === 1,
      },
      children: [],
    }

    if (menu.type === 1) route.component = getComponent(menu.component)
    if (menu.redirect) route.redirect = menu.redirect
    if (menu.children?.length) route.children = buildDynamicRoutes(menu.children, fullPath)
    return route
  })
}

const lazyRegisteredPaths = new Set<string>()

const findMenuByPath = (menus: any[], targetPath: string): any => {
  for (const menu of menus) {
    if (menu.type === 1 && menu.path === targetPath) {
      return menu
    }
    if (menu.children?.length) {
      const found = findMenuByPath(menu.children, targetPath)
      if (found) return found
    }
  }
  return null
}

let isDynamicAdded = false

export const setupDynamicRoutes = (router: Router, menus: any[]) => {
  if (isDynamicAdded) return

  const dynamicRoutes = buildDynamicRoutes(menus)
  dynamicRoutes.forEach((route) => router.addRoute('Root', route))
  isDynamicAdded = true
}

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    next()
    return
  }

  if (!userStore.token) {
    next('/login')
    return
  }

  if (userStore.menus.length === 0) {
    try {
      const success = await userStore.getMenu()
      if (!success) {
        next('/login')
        return
      }
    } catch (error) {
      if (isAuthError(error)) {
        next('/login')
        return
      }
    }
  }

  if (userStore.token && !isDynamicAdded) {
    setupDynamicRoutes(router, userStore.menus)
    return next({ ...to, replace: true })
  }

  if (to.matched.length === 0) {
    const menu = findMenuByPath(userStore.menus, to.path)
    if (menu && !lazyRegisteredPaths.has(to.path)) {
      const route = {
        path: menu.path,
        name: menu.name || menu.path,
        component: getComponent(menu.component),
        meta: {
          title: menu.title,
          icon: menu.icon,
          hidden: menu.hidden === 1,
        },
      }
      router.addRoute('Root', route)
      lazyRegisteredPaths.add(to.path)
      return next({ ...to, replace: true })
    }
  }

  next()
})

export default router
