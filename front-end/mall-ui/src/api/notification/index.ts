import request from '@/utils/request'
import type { Notification, NotificationQuery, NotificationStats } from './type'
import type { PageResult } from '../type'

export const getUnreadCountApi = () => {
  return request.get<number>('/api/member/notification/unread-count')
}

export const markAsReadApi = (id: number) => {
  return request.post(`/api/member/notification/read/${id}`)
}

export const markAllAsReadApi = () => {
  return request.post('/api/member/notification/read-all')
}

export const getNotificationsApi = (params: NotificationQuery) => {
  return request.get<PageResult<Notification>>('/api/member/notification/list', {
    pageNum: params.pageNum,
    pageSize: params.pageSize,
    readStatus: params.readStatus,
  })
}

export const getNotificationStatsApi = () => {
  return request.get<NotificationStats>('/api/member/notification/stats')
}
