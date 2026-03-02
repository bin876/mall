import { useUserStore } from '@/stores/user'

let isLoggingOut = false

export const handleAuthError = () => {
  if (isLoggingOut) return

  isLoggingOut = true

  ElMessage.warning('登录已失效，请重新登录')

  setTimeout(() => {
    const userStore = useUserStore()
    userStore.logoutAndClear()
    isLoggingOut = false
  }, 1500)
}

export const isAuthError = (error: any): boolean => {
  if (error?.code === 401) {
    return true
  }

  if (error?.response?.status === 401) {
    return true
  }

  if (error?.response?.data?.code === 401) {
    return true
  }

  return false
}
