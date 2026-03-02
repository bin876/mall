import type { Directive } from 'vue'
import { useUserStore } from '@/stores/user'

export const vHas: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    applyPermission(el, binding)
  },

  updated(el: HTMLElement, binding: DirectiveBinding) {
    applyPermission(el, binding)
  },
}

const applyPermission = (el: HTMLElement, binding: DirectiveBinding) => {
  const { value: requiredPermission, modifiers } = binding
  const userStore = useUserStore()

  if (!requiredPermission || typeof requiredPermission !== 'string') {
    console.error('[v-has] 无效的权限参数:', requiredPermission)
    if (!modifiers.hide) {
      el.parentNode?.removeChild(el)
    }
    return
  }

  if (!userStore.token) {
    handleNoPermission(el, modifiers)
    return
  }

  const userPermissions = userStore.permissions || []

  const hasPermission = checkPermission(userPermissions, requiredPermission)

  if (!hasPermission) {
    handleNoPermission(el, modifiers)
    if (import.meta.env.DEV) {
      console.debug(`[权限控制] 无权限访问: ${requiredPermission}`, {
        userPermissions: userPermissions,
      })
    }
  }
}

const checkPermission = (userPermissions: string[], requiredPermission: string): boolean => {
  if (userPermissions.includes(requiredPermission)) {
    return true
  }
  console.log(userPermissions)

  if (requiredPermission.endsWith(':*')) {
    const basePermission = requiredPermission.slice(0, -2)

    return userPermissions.some(
      (perm) =>
        perm === basePermission ||
        (perm.startsWith(basePermission + ':') && perm.length > basePermission.length + 1)
    )
  }

  if (requiredPermission.includes(':*')) {
    const modulePrefix = requiredPermission.split(':')[0] + ':'
    return userPermissions.some((perm) => perm.startsWith(modulePrefix))
  }

  return false
}

const handleNoPermission = (el: HTMLElement, modifiers: DirectiveBinding['modifiers']) => {
  if (modifiers.hide || modifiers.invisible) {
    el.style.display = 'none'
  } else {
    el.parentNode?.removeChild(el)
  }
}
