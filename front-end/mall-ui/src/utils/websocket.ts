class NotificationWebSocket {
  private socket: WebSocket | null = null
  private reconnectTimer: NodeJS.Timeout | null = null
  private reconnectAttempts = 0
  private maxReconnectAttempts = 5

  constructor(
    private memberId: string,
    private onMessage: (data: any) => void
  ) {}

  connect() {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      return
    }

    const wsUrl = `ws://${window.location.host}/ws/notifications/${this.memberId}`
    this.socket = new WebSocket(wsUrl)

    this.socket.onopen = () => {
      console.log('通知WebSocket连接成功')
      this.reconnectAttempts = 0
    }

    this.socket.onmessage = (event) => {
      const data = JSON.parse(event.data)
      this.onMessage(data)
    }

    this.socket.onclose = () => {
      console.log('通知WebSocket连接关闭')
      this.reconnect()
    }

    this.socket.onerror = (error) => {
      console.error('通知WebSocket错误:', error)
    }
  }

  private reconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      const delay = Math.min(1000 * this.reconnectAttempts, 10000)
      this.reconnectTimer = setTimeout(() => {
        this.connect()
      }, delay)
    }
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
    }
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
  }
}

export default NotificationWebSocket
