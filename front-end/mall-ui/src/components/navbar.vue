<template>
  <header class="top-nav">
    <div class="nav-container">
      <div class="nav-left">
        <router-link to="/" class="nav-link">首页</router-link>
      </div>

      <div class="nav-right">
        <template v-if="userStore.token">
          <span class="ml-1.5">你好, {{ userStore.userInfo?.username }}</span>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <span class="nav-divider">|</span>
          <router-link to="/register" class="nav-link">注册</router-link>
        </template>

        <div v-if="userStore.token" class="notification-container flex items-center">
          <el-badge
            :value="unreadCount"
            :hidden="unreadCount === 0"
            :max="99"
            class="notification-badge"
          >
            <el-icon @click="toggleDropdown" class="cursor-pointer">
              <Bell />
            </el-icon>
          </el-badge>

          <div v-if="showDropdown" class="notification-dropdown">
            <div class="notification-header">
              <span>通知</span>
              <el-button
                size="small"
                @click="markAllAsRead"
                v-if="unreadCount > 0"
                :loading="markAllLoading"
              >
                全部标为已读
              </el-button>
            </div>

            <div v-if="recentNotifications.length === 0" class="empty-notifications">暂无通知</div>

            <div v-else class="notifications-list">
              <div
                v-for="notification in recentNotifications"
                :key="notification.notificationId"
                :class="['notification-item', { unread: !notification.readStatus }]"
                @click="handleNotificationClick(notification)"
              >
                <div class="notification-content">
                  <div class="notification-title">{{ notification.title }}</div>
                  <div class="notification-content-text">{{ notification.content }}</div>
                  <div class="notification-time">{{ formatTime(notification.createTime) }}</div>
                </div>
              </div>
            </div>

            <div class="view-all" @click="goToNotifications">查看全部通知</div>
          </div>
        </div>

        <span class="nav-divider">|</span>
        <router-link to="/user/coupon" class="nav-link">优惠券</router-link>
        <span class="nav-divider">|</span>
        <router-link to="/user/collect" class="nav-link">我的收藏</router-link>
        <span class="nav-divider">|</span>
        <router-link to="/user/orders" class="nav-link">我的订单</router-link>
        <span class="nav-divider">|</span>
        <router-link to="/cart" class="nav-link">购物车</router-link>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
  import { useUserStore } from '@/stores/user'
  import { ref, onMounted, onUnmounted, nextTick } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { Bell } from '@element-plus/icons-vue'
  import NotificationWebSocket from '@/utils/websocket'
  import {
    getUnreadCountApi,
    getNotificationsApi,
    markAsReadApi,
    markAllAsReadApi,
  } from '@/api/notification'
  import type { Notification } from '@/api/notification/type'

  const userStore = useUserStore()

  const router = useRouter()
  const unreadCount = ref(0)
  const recentNotifications = ref<Notification[]>([])
  const showDropdown = ref(false)
  const markAllLoading = ref(false)

  let ws: NotificationWebSocket | null = null
  let clickOutsideHandler: ((event: MouseEvent) => void) | null = null

  // 防抖函数
  const debounce = (func: Function, wait: number) => {
    let timeout: NodeJS.Timeout
    return (...args: any[]) => {
      clearTimeout(timeout)
      timeout = setTimeout(() => func.apply(this, args), wait)
    }
  }

  // 获取未读数量（带防抖）
  const fetchUnreadCount = debounce(async () => {
    try {
      const res = await getUnreadCountApi()
      unreadCount.value = res || 0
    } catch (error) {
      console.error('获取未读通知数量失败:', error)
    }
  }, 300)

  // 获取最近通知
  const fetchRecentNotifications = async () => {
    const params: NotificationQuery = {
      pageNum: 1,
      pageSize: 10,
    }

    try {
      const res = await getNotificationsApi(params)

      recentNotifications.value = res.records || []
    } catch (error) {
      console.error('获取最近通知失败:', error)
    }
  }

  // 处理新通知
  const handleNewNotification = (notification: Notification) => {
    // 更新未读数量
    unreadCount.value++

    // 添加到通知列表顶部
    recentNotifications.value.unshift(notification)

    // 限制只显示最近10条
    if (recentNotifications.value.length > 10) {
      recentNotifications.value.pop()
    }

    // 显示桌面通知（仅当页面不在前台时）
    if (document.hidden) {
      ElMessage.success({
        message: `新通知: ${notification.title}`,
        duration: 3000,
      })
    }
  }

  // 标记为已读
  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.readStatus) {
      try {
        await markAsReadApi(notification.notificationId)
        notification.readStatus = 1
        unreadCount.value--
      } catch (error) {
        ElMessage.error('标记通知失败')
        console.error('标记通知为已读失败:', error)
        return
      }
    }

    // 跳转到目标页面
    if (notification.targetUrl) {
      router.push(notification.targetUrl)
    }
    showDropdown.value = false
  }

  // 标记全部已读
  const markAllAsRead = async () => {
    markAllLoading.value = true
    try {
      await markAllAsReadApi()
      unreadCount.value = 0
      recentNotifications.value.forEach((n) => (n.readStatus = 1))
      ElMessage.success('已全部标为已读')
    } catch (error) {
      ElMessage.error('操作失败')
      console.error('标记全部通知为已读失败:', error)
    } finally {
      markAllLoading.value = false
    }
  }

  // 跳转到通知页面
  const goToNotifications = () => {
    router.push('/user/notifications')
    showDropdown.value = false
  }

  // 格式化时间
  const formatTime = (time: string) => {
    if (!time) return ''
    return time.replace('T', ' ').replace(/\.\d+/, '').substring(5, 16)
  }

  // 切换下拉菜单
  const toggleDropdown = async () => {
    showDropdown.value = !showDropdown.value
    if (showDropdown.value) {
      await fetchRecentNotifications()
      // 确保点击事件处理器已绑定
      await nextTick()
    }
  }

  // 点击外部关闭下拉菜单
  const handleClickOutside = (event: MouseEvent) => {
    const container = document.querySelector('.notification-container')
    if (container && !container.contains(event.target as Node)) {
      showDropdown.value = false
    }
  }

  // 安全获取会员ID
  const getMemberId = (): string | null => {
    // 优先从用户 store 获取
    if (userStore.userInfo?.memberId) {
      return userStore.userInfo.memberId.toString()
    }
    // 回退到 localStorage
    return localStorage.getItem('memberId')
  }

  // 初始化
  onMounted(async () => {
    if (!userStore.token) {
      return // 未登录用户不初始化通知
    }

    await fetchUnreadCount()
    await fetchRecentNotifications()

    // 连接 WebSocket
    const memberId = getMemberId()
    if (memberId) {
      ws = new NotificationWebSocket(memberId, handleNewNotification)
      ws.connect()
    }

    // 绑定点击外部事件
    clickOutsideHandler = handleClickOutside
    document.addEventListener('click', clickOutsideHandler)

    // 页面可见性变化时刷新未读数
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        fetchUnreadCount()
      }
    }
    document.addEventListener('visibilitychange', handleVisibilityChange)

    // 组件卸载时清理
    onUnmounted(() => {
      if (ws) {
        ws.disconnect()
      }
      if (clickOutsideHandler) {
        document.removeEventListener('click', clickOutsideHandler)
      }
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    })
  })
</script>

<style scoped>
  /* 第1行：导航栏 (36px) */
  .top-nav {
    height: 36px;
    background: #f5f5f5;
    border-bottom: 1px solid #e5e5e5;
    font-size: 12px;
  }

  .nav-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 100%;
  }

  .nav-right {
    display: flex;
    gap: 10px;
  }

  .nav-link {
    color: #333;
    text-decoration: none;
  }

  .nav-divider {
    color: #999;
  }

  /* 通知相关样式 */
  .notification-container {
    position: relative;
    height: 100%;
    display: flex;
    align-items: center;
  }

  .notification-badge :deep(.el-badge__content) {
    background: #e40000;
    border: none;
    font-size: 10px;
    height: 16px;
    min-width: 16px;
    line-height: 16px;
    padding: 0 4px;
  }

  .notification-dropdown {
    position: absolute;
    top: 100%;
    right: 0;
    width: 320px;
    background: white;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    z-index: 1000;
    margin-top: 5px;
  }

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #eee;
    font-weight: bold;
    font-size: 12px;
  }

  .empty-notifications {
    text-align: center;
    padding: 20px;
    color: #999;
    font-size: 12px;
  }

  .notifications-list {
    max-height: 300px;
    overflow-y: auto;
  }

  .notification-item {
    padding: 12px 16px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .notification-item:hover {
    background-color: #f9f9f9;
  }

  .notification-item.unread {
    background-color: #f0f9ff;
    border-left: 3px solid #409eff;
  }

  .notification-title {
    font-weight: bold;
    margin-bottom: 4px;
    color: #333;
    font-size: 12px;
  }

  .notification-content-text {
    color: #666;
    font-size: 11px;
    margin-bottom: 4px;
    line-height: 1.4;
    word-break: break-word;
  }

  .notification-time {
    color: #999;
    font-size: 10px;
  }

  .view-all {
    padding: 12px 16px;
    text-align: center;
    color: #409eff;
    cursor: pointer;
    border-top: 1px solid #eee;
    font-size: 12px;
  }

  .view-all:hover {
    background-color: #f9f9f9;
  }

  .cursor-pointer {
    cursor: pointer;
  }

  .el-icon {
    vertical-align: middle;
    display: inline-flex;
    align-items: center;
  }
</style>
