<template>
  <NavBar />

  <div class="orders-container">
    <h1 class="page-title">我的订单</h1>

    <div class="status-tabs">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        :class="{ active: activeStatus === tab.value }"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="badge">{{ tab.count }}</span>
      </button>
    </div>

    <div v-if="orderList.length > 0" class="orders-list">
      <div v-for="order in orderList" :key="order.orderId" class="order-item">
        <div class="order-header">
          <div class="order-info">
            <span class="order-sn">订单编号：{{ order.orderSn }}</span>
            <span class="order-time">{{ formatTime(order.createTime) }}</span>
          </div>
          <div class="order-status">
            <el-tag :type="getStatusType(order.status)">
              {{ order.statusDesc }}
            </el-tag>
          </div>
        </div>

        <div class="order-products">
          <div v-for="(item, index) in getOrderProducts(order)" :key="index" class="product-item">
            <img :src="item.skuPic" :alt="item.skuName" />
            <div class="product-info">
              <h4>{{ item.skuName }}</h4>
              <div class="product-price">
                <span>¥{{ formatPrice(item.skuPrice) }}</span>
                <span>× {{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="order-footer">
          <div class="order-total">
            共 {{ order.productCount }} 件商品，实付款：
            <span class="total-amount">¥{{ formatPrice(order.payAmount) }}</span>
          </div>
          <div class="order-actions">
            <el-button size="small" @click="viewOrder(order.orderSn)">查看详情</el-button>

            <template v-if="order.status === 0">
              <!-- <el-button size="small" type="primary" @click="goToPay(order.orderSn)">
                去支付
              </el-button> -->
              <el-button size="small" type="danger" @click="cancelOrder(order.orderSn)">
                取消订单
              </el-button>
            </template>

            <template v-else-if="order.status === 1">
              <el-button size="small" @click="viewLogistics(order.orderSn)">查看物流</el-button>
            </template>

            <template v-else-if="order.status === 2">
              <el-button size="small" type="primary" @click="confirmReceipt(order.orderSn)">
                确认收货
              </el-button>
            </template>

            <template v-else>
              <el-button size="small" @click="deleteOrder(order.orderSn)">删除订单</el-button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无订单" />
      <el-button type="primary" style="margin-top: 20px" @click="goToHome">去逛逛</el-button>
    </div>

    <div v-if="total > 0" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[5, 10, 20]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { getOrderListApi, cancelOrderApi, confirmReceiptApi, deleteOrderApi } from '@/api/product'
  import type { OrderSimple } from '@/api/product/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()

  const statusTabs = ref([
    { value: null, label: '全部订单', count: 0 },
    { value: 0, label: '待付款', count: 0 },
    { value: 1, label: '待发货', count: 0 },
    { value: 2, label: '待收货', count: 0 },
    { value: 3, label: '已完成', count: 0 },
  ])

  const activeStatus = ref<number | null>(null)
  const orderList = ref<OrderSimple[]>([])
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  const formatTime = (time: string) => {
    return time.replace('T', ' ').replace(/\.\d+/, '')
  }

  const formatPrice = (price: number) => {
    return price.toFixed(2)
  }

  const getOrderProducts = (order: OrderSimple) => {
    return [
      {
        skuPic: order.firstProductImg,
        skuName: '商品名称',
        skuPrice: order.payAmount,
        quantity: order.productCount,
      },
    ]
  }

  const getStatusType = (status: number) => {
    if (status === 0) return 'warning'
    if (status === 1) return 'info'
    if (status === 2) return 'success'
    return 'default'
  }

  const changeStatus = (status: number | null) => {
    activeStatus.value = status
    currentPage.value = 1
    fetchOrders()
  }

  const fetchOrders = async () => {
    try {
      const params = {
        status: activeStatus.value,
        pageNum: currentPage.value,
        pageSize: pageSize.value,
      }
      const res = await getOrderListApi(params)
      orderList.value = res.records || []
      total.value = res.total || 0

      if (activeStatus.value === null) {
        statusTabs.value[0].count = total.value
      }
    } catch (error) {
      console.error('获取订单列表失败:', error)
    }
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchOrders()
  }
  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchOrders()
  }

  const viewOrder = (orderSn: string) => {
    router.push(`/user/order/${orderSn}`)
  }

  const goToPay = (orderSn: string) => {
    router.push(`/order/pay?orderSn=${orderSn}`)
  }

  const viewLogistics = (orderSn: string) => {
    ElMessage.info('物流信息暂未实现')
  }

  const cancelOrder = async (orderSn: string) => {
    ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await cancelOrderApi(orderSn)
        ElMessage.success('订单已取消')
        fetchOrders()
      } catch (error: any) {
        ElMessage.error(error.msg || '取消失败')
      }
    })
  }

  const confirmReceipt = async (orderSn: string) => {
    ElMessageBox.confirm('确认已收到商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    }).then(async () => {
      try {
        await confirmReceiptApi(orderSn)
        ElMessage.success('已确认收货')
        fetchOrders()
      } catch (error: any) {
        ElMessage.error(error.msg || '确认失败')
      }
    })
  }

  const deleteOrder = async (orderSn: string) => {
    ElMessageBox.confirm('确定要删除此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteOrderApi(orderSn)
        ElMessage.success('订单已删除')
        fetchOrders()
      } catch (error: any) {
        ElMessage.error(error.msg || '删除失败')
      }
    })
  }

  const goToHome = () => {
    router.push('/')
  }

  onMounted(() => {
    fetchOrders()
  })
</script>

<style scoped>
  .orders-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px 0;
  }

  .status-tabs {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    border-bottom: 1px solid #e5e5e5;
    padding-bottom: 15px;
  }

  .status-tabs button {
    padding: 8px 16px;
    background: none;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    cursor: pointer;
    font-size: 14px;
    color: #333;
    position: relative;
  }

  .status-tabs button:hover {
    border-color: #999;
  }

  .status-tabs button.active {
    border-color: #e40000;
    color: #e40000;
    font-weight: bold;
  }

  .badge {
    background: #e40000;
    color: white;
    border-radius: 10px;
    padding: 0 6px;
    font-size: 12px;
    margin-left: 5px;
  }

  .order-item {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    margin-bottom: 15px;
    padding: 20px;
  }

  .order-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #eee;
  }

  .order-info .order-sn {
    font-weight: bold;
    color: #333;
  }

  .order-info .order-time {
    color: #666;
    font-size: 12px;
    margin-top: 5px;
    display: block;
  }

  .order-products {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    padding: 10px 0;
  }

  .product-item {
    display: flex;
    gap: 15px;
    width: 300px;
  }

  .product-item img {
    width: 80px;
    height: 80px;
    object-fit: cover;
    border: 1px solid #eee;
  }

  .product-info {
    flex: 1;
  }

  .product-info h4 {
    font-size: 14px;
    font-weight: normal;
    color: #333;
    margin: 0 0 8px 0;
    line-height: 1.4;
  }

  .product-price {
    display: flex;
    justify-content: space-between;
    color: #666;
    font-size: 14px;
  }

  .product-price span:first-child {
    color: #e40000;
    font-weight: bold;
  }

  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 15px;
    border-top: 1px solid #eee;
  }

  .order-total {
    color: #666;
    font-size: 14px;
  }

  .total-amount {
    color: #e40000;
    font-weight: bold;
    font-size: 16px;
  }

  .order-actions {
    display: flex;
    gap: 10px;
  }

  .empty-state {
    text-align: center;
    padding: 60px 0;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
</style>
