import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfoApi, loginApi, logoutApi } from '@/api/user'
import type { LoginRequest, UserInfo } from '@/api/user/type'
import router from '@/router'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const userInfo = ref<UserInfo | null>(null)

    const login = async (data: LoginRequest) => {
      try {
        const SaTokenInfo: any = await loginApi(data)
        token.value = SaTokenInfo.tokenValue

        await fetchUserInfo()
        return true
      } catch (error) {
        console.warn(error)
      }
    }

    const fetchUserInfo = async () => {
      try {
        const res = await getUserInfoApi()
        userInfo.value = res
      } catch (error) {
        console.warn('获取用户信息失败:', error)
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
      localStorage.removeItem('member-user')
      sessionStorage.clear()

      if (router.currentRoute.value.path !== '/login') {
        router.push('/login')
      }
    }

    const clearUserData = () => {
      token.value = ''
      userInfo.value = null
    }

    return {
      token,
      userInfo,
      login,
      logout,
      logoutAndClear,
      fetchUserInfo,
    }
  },
  {
    persist: {
      key: 'member-user',
      storage: localStorage,
      pick: ['token', 'userInfo'],
    },
  }
)
