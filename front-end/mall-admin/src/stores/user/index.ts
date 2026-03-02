import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getSysUserInfoApi, getUserPermissionsApi, loginApi, logoutApi } from '@/api/user'
import type { LoginRequest, SysUserInfo } from '@/api/user/type'
import { menuTreeApi } from '@/api/menu'
import router from '@/router'
import { isAuthError } from '@/utils/auth'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const menus = ref<any>([])
    const permissions = ref<string[]>([])
    const roles = ref<string[]>([])

    const userInfo = ref<SysUserInfo | null>(null)

    const login = async (data: LoginRequest) => {
      try {
        const SaTokenInfo: any = await loginApi(data)
        token.value = SaTokenInfo.tokenValue

        permissions.value = await getUserPermissionsApi()

        userInfo.value = await getSysUserInfoApi()
        return true
      } catch (error) {
        console.warn(error)
        return false
      }
    }

    const logout = async () => {
      try {
        await logoutApi()
      } catch (error) {
        console.warn('Logout API error:', error)
      } finally {
        clearUserData()
        if (router.currentRoute.value.path !== '/login') {
          router.push('/login')
        }
      }
    }

    const logoutAndClear = () => {
      clearUserData()
      localStorage.removeItem('admin-user')
      sessionStorage.clear()

      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }

    const clearUserData = () => {
      token.value = ''
      menus.value = []
      permissions.value = []
      roles.value = []
    }

    const getMenu = async () => {
      try {
        menus.value = await menuTreeApi()
        return true
      } catch (error) {
        console.warn(error)
        if (isAuthError(error)) {
          logoutAndClear()
        }
        return false
      }
    }

    const hasPermission = (p: string) => permissions.value.includes(p)
    const hasRole = (role: string) => roles.value.includes(role)

    return {
      token,
      menus,
      permissions,
      roles,
      userInfo,
      login,
      logout,
      logoutAndClear,
      getMenu,
      hasPermission,
      hasRole,
    }
  },
  {
    persist: {
      key: 'admin-user',
      storage: localStorage,
      pick: ['token', 'menus', 'permissions', 'roles', 'userInfo'],
    },
  }
)
