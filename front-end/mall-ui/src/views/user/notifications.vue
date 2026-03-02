<template>
  <NavBar />

  <div class="notifications-container">
    <div class="page-header">
      <h1 class="page-title">我的通知</h1>
      <el-button
        v-if="stats.unreadCount > 0"
        type="primary"
        size="small"
        @click="markAllAsRead"
        :loading="markAllLoading"
      >
        全部标为已读
      </el-button>
    </div>

    <!-- 状态筛选 -->
    <div class="status-tabs">
      <el-button :class="{ active: activeStatus === 'all' }" @click="changeStatus('all')">
        全部({{ stats.totalCount }})
      </el-button>
      <el-button :class="{ active: activeStatus === 'unread' }" @click="changeStatus('unread')">
        未读({{ stats.unreadCount }})
      </el-button>
      <el-button :class="{ active: activeStatus === 'read' }" @click="changeStatus('read')">
        已读({{ stats.readCount }})
      </el-button>
    </div>

    <!-- 通知列表 -->
    <div v-if="notifications.length > 0" class="notifications-list">
      <div
        v-for="notification in notifications"
        :key="notification.notificationId"
        :class="['notification-card', { unread: !notification.readStatus }]"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-header">
          <span class="notification-type" :class="getTypeClass(notification.type)">
            {{ getTypeText(notification.type) }}
          </span>
          <span class="notification-time">{{ formatDate(notification.createTime) }}</span>
        </div>
        <div class="notification-body">
          <h3 class="notification-title">{{ notification.title }}</h3>
          <p class="notification-content">{{ notification.content }}</p>
        </div>
        <div v-if="!notification.readStatus" class="notification-unread-indicator"></div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty :description="getEmptyDescription()" />
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    getNotificationsApi,
    getNotificationStatsApi,
    markAsReadApi,
    markAllAsReadApi,
  } from '@/api/notification'
  import type { Notification, NotificationQuery, NotificationStats } from '@/api/notification/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()

  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const activeStatus = ref('all') // all, unread, read
  const notifications = ref<Notification[]>([])
  const stats = ref<NotificationStats>({
    totalCount: 0,
    unreadCount: 0,
    readCount: 0,
  })
  const markAllLoading = ref(false)

  const fetchStats = async () => {
    try {
      const res = await getNotificationStatsApi()
      stats.value = res || { totalCount: 0, unreadCount: 0, readCount: 0 }
    } catch (error) {
      console.error('获取通知统计失败:', error)
    }
  }

  const fetchNotifications = async (page = currentPage.value, size = pageSize.value) => {
    try {
      const params: NotificationQuery = {
        pageNum: page,
        pageSize: size,
      }

      if (activeStatus.value === 'unread') {
        params.readStatus = 0
      } else if (activeStatus.value === 'read') {
        params.readStatus = 1
      }

      const res = await getNotificationsApi(params)
      notifications.value = res.records || []
      total.value = res.total || 0
    } catch (error) {
      console.error('获取通知列表失败:', error)
      ElMessage.error('加载失败')
    }
  }

  const changeStatus = (status: string) => {
    activeStatus.value = status
    currentPage.value = 1
    fetchNotifications(1, pageSize.value)
  }

  const handleNotificationClick = async (notification: Notification) => {
    if (!notification.readStatus) {
      try {
        await markAsReadApi(notification.notificationId)

        notification.readStatus = 1

        stats.value.unreadCount--
        stats.value.readCount++

        // 如果在未读列表中，需要重新加载
        if (activeStatus.value === 'unread') {
          fetchNotifications(currentPage.value, pageSize.value)
        }
      } catch (error) {
        console.error('标记通知为已读失败:', error)
        ElMessage.error('操作失败')
        return
      }
    }

    // 跳转到目标页面
    if (notification.targetUrl) {
      router.push(notification.targetUrl)
    }
  }

  // 标记全部已读
  const markAllAsRead = async () => {
    ElMessageBox.confirm('确定要将所有通知标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      markAllLoading.value = true
      try {
        await markAllAsReadApi()
        ElMessage.success('已全部标为已读')

        // 更新统计
        stats.value.readCount += stats.value.unreadCount
        stats.value.unreadCount = 0

        // 重新加载当前列表
        fetchNotifications(currentPage.value, pageSize.value)
      } catch (error) {
        ElMessage.error('操作失败')
        console.error('标记全部通知为已读失败:', error)
      } finally {
        markAllLoading.value = false
      }
    })
  }

  // 分页处理
  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchNotifications(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchNotifications(page, pageSize.value)
  }

  // 工具方法
  const getTypeText = (type: number) => {
    switch (type) {
      case 1:
        return '系统通知'
      case 2:
        return '营销通知'
      case 3:
        return '互动通知'
      default:
        return '通知'
    }
  }

  const getTypeClass = (type: number) => {
    switch (type) {
      case 1:
        return 'system'
      case 2:
        return 'marketing'
      case 3:
        return 'interactive'
      default:
        return ''
    }
  }

  const formatDate = (time: string) => {
    if (!time) return ''
    return time.replace('T', ' ').replace(/\.\d+/, '').substring(0, 16)
  }

  const getEmptyDescription = () => {
    if (activeStatus.value === 'unread') return '暂无未读通知'
    if (activeStatus.value === 'read') return '暂无已读通知'
    return '暂无通知'
  }

  // 初始化
  onMounted(() => {
    fetchStats()
    fetchNotifications()
  })
</script>

<style scoped>
  .notifications-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  .status-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    border-bottom: 1px solid #e5e5e5;
    padding-bottom: 15px;
  }

  .status-tabs .el-button {
    padding: 8px 16px;
    background: none;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    color: #333;
  }

  .status-tabs .el-button.active {
    border-color: #e40000;
    color: #e40000;
    font-weight: bold;
  }

  .notifications-list {
    margin-bottom: 20px;
  }

  .notification-card {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 15px;
    cursor: pointer;
    transition: box-shadow 0.2s;
    position: relative;
  }

  .notification-card:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .notification-card.unread {
    border-left: 4px solid #e40000;
    background-color: #fef0f0;
  }

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .notification-type {
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: bold;
  }

  .notification-type.system {
    background: #f0f9ff;
    color: #409eff;
  }

  .notification-type.marketing {
    background: #f5f0ff;
    color: #7b42f5;
  }

  .notification-type.interactive {
    background: #f0faff;
    color: #4285f5;
  }

  .notification-time {
    color: #999;
    font-size: 12px;
  }

  .notification-body {
    margin-bottom: 8px;
  }

  .notification-title {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
    line-height: 1.4;
  }

  .notification-content {
    color: #666;
    font-size: 14px;
    line-height: 1.5;
    margin: 0;
    word-break: break-word;
  }

  .notification-unread-indicator {
    position: absolute;
    top: 12px;
    right: 12px;
    width: 8px;
    height: 8px;
    background: #e40000;
    border-radius: 50%;
  }

  .empty-state {
    text-align: center;
    padding: 60px 0;
  }

  .pagination {
    text-align: right;
  }
</style>
