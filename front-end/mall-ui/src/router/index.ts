import { useUserStore } from '@/stores/user'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/search/index.vue'),
    meta: { title: '商品搜索' },
    props: (route: { query: { categoryId: string; keyword: any; sort: any } }) => ({
      categoryId: parseInt(route.query.categoryId) || null,
      keyword: route.query.keyword || '',
      sort: route.query.sort || 'default',
    }),
  },
  {
    path: '/product/:spuId(\\d+)',
    name: 'ProductDetail',
    component: () => import('@/views/product/detail.vue'),
    meta: { title: '商品详情' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' },
  },

  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/index.vue'),
    meta: { title: '购物车', requiresAuth: true },
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/order/confirm.vue'),
    meta: { title: '确认订单', requiresAuth: true },
  },

  {
    path: '/user/profile',
    name: 'Profile',
    component: () => import('@/views/user/profile.vue'),
    meta: { title: '个人资料', requiresAuth: true },
  },
  {
    path: '/user/notifications',
    name: 'Notifications',
    component: () => import('@/views/user/notifications.vue'),
    meta: { title: '我的优惠券', requiresAuth: true },
  },
  {
    path: '/user/orders',
    name: 'MyOrders',
    component: () => import('@/views/user/orders.vue'),
    meta: { title: '我的订单', requiresAuth: true },
  },
  {
    path: '/user/order/:orderSn',
    name: 'OrderDetail',
    component: () => import('@/views/user/order-detail.vue'),
    meta: { title: '订单详情', requiresAuth: true },
    props: true,
  },
  {
    path: '/user/collect',
    name: 'Collect',
    component: () => import('@/views/user/collect-list.vue'),
    meta: { title: '我的收藏', requiresAuth: true },
  },
  {
    path: '/user/coupon',
    name: 'Coupon',
    component: () => import('@/views/user/coupon.vue'),
    meta: { title: '我的优惠券', requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (to.path === '/login' && userStore.token) {
    next('/')
    return
  }

  next()
})

router.afterEach((to) => {
  document.title = (to.meta.title as string) || '电商平台'
})

export default router
