import { ElMessage } from 'element-plus'
import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { isAuthError, handleAuthError } from '@/utils/auth'
import type { Result } from '@/api/type'
import { useUserStore } from '@/stores/user'

const service: AxiosInstance = axios.create({
  // baseURL: import.meta.env.VITE_API_BASE_URL,
  baseURL: '/proxy',
  timeout: 5000,
  headers: { 'Content-Type': 'application/json' },
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.token

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res: Result = response.data

    if (res.code === 200) {
      return res.data
    }

    const error = {
      code: res.code,
      msg: res.msg,
      data: res.data,
    }

    if (isAuthError(error)) {
      handleAuthError()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }

    return Promise.reject(error)
  },
  (error) => {
    if (!error.response) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }

    const { status, data } = error.response

    const statusMessages: Record<number, string> = {
      400: '请求参数错误',
      401: '登录已失效，请重新登录',
      403: '无权限访问',
      404: '请求资源不存在',
      429: '请求过于频繁',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务暂时不可用',
      504: '网关超时',
    }

    const message = statusMessages[status] || `请求失败（${status}）`

    if (status === 401) {
      handleAuthError()
      return Promise.reject(error)
    }

    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export function request<T = unknown>(config: AxiosRequestConfig): Promise<T> {
  return service.request<any, T>(config)
}

export function get<T = unknown>(
  url: string,
  params?: Record<string, any>,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({ method: 'GET', url, params, ...config })
}

export function post<T = unknown>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({ method: 'POST', url, data, ...config })
}

export function put<T = unknown>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'PUT', url, data, ...config })
}

export function del<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return request<T>({ method: 'DELETE', url, ...config })
}

export default { get, post, put, del }
