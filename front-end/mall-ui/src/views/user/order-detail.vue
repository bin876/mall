<template>
  <NavBar />

  <div class="order-detail-container">
    <h1 class="page-title">订单详情</h1>

    <!-- 订单基本信息 -->
    <div class="order-section">
      <div class="section-header">
        <h2>订单信息</h2>
        <div class="order-status">
          <el-tag :type="getStatusType(orderDetail.status)">
            {{ orderDetail.statusDesc }}
          </el-tag>
        </div>
      </div>

      <div class="order-info-grid">
        <div class="info-item">
          <span class="label">订单编号：</span>
          <span>{{ orderDetail.orderSn }}</span>
        </div>
        <div class="info-item">
          <span class="label">下单时间：</span>
          <span>{{ formatTime(orderDetail.createTime) }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付时间：</span>
          <span>{{ orderDetail.payTime ? formatTime(orderDetail.payTime) : '未支付' }}</span>
        </div>
        <div class="info-item">
          <span class="label">支付方式：</span>
          <span>{{ orderDetail.payTypeDesc || '未支付' }}</span>
        </div>
      </div>
    </div>

    <!-- 收货信息 -->
    <div class="order-section">
      <h2>收货信息</h2>
      <div class="address-info">
        <p>
          <strong>{{ orderDetail.receiverName }}</strong>
          {{ orderDetail.receiverPhone }}
        </p>
        <p>
          {{ orderDetail.receiverProvince }}
          {{ orderDetail.receiverCity }}
          {{ orderDetail.receiverRegion }}
          {{ orderDetail.receiverDetailAddress }}
        </p>
        <p v-if="orderDetail.receiverPostCode">邮编：{{ orderDetail.receiverPostCode }}</p>
      </div>
    </div>

    <!-- 商品清单 -->
    <div class="order-section">
      <h2>商品清单</h2>
      <div class="product-list">
        <div v-for="item in orderDetail.orderItems" :key="item.itemId" class="product-item">
          <div class="product-image">
            <img :src="item.skuPic" :alt="item.skuName" />
          </div>
          <div class="product-info">
            <h3>{{ item.skuName }}</h3>
            <div class="product-price-quantity">
              <span class="price">¥{{ formatPrice(item.skuPrice) }}</span>
              <span class="quantity">× {{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 金额信息 -->
    <div class="order-section">
      <h2>金额信息</h2>
      <div class="amount-summary">
        <div class="amount-row">
          <span>商品总额：</span>
          <span>¥{{ formatPrice(orderDetail.totalAmount) }}</span>
        </div>
        <div class="amount-row">
          <span>运费：</span>
          <span>¥{{ formatPrice(orderDetail.freightAmount) }}</span>
        </div>
        <div class="amount-row">
          <span>优惠金额：</span>
          <span>-¥{{ formatPrice(orderDetail.promotionAmount) }}</span>
        </div>
        <div class="amount-row total">
          <span>应付金额：</span>
          <span class="total-amount">¥{{ formatPrice(orderDetail.payAmount) }}</span>
        </div>
      </div>
    </div>

    <!-- 支付区域（仅待付款订单） -->
    <div v-if="orderDetail.status === 0" class="payment-section">
      <h2>支付订单</h2>

      <!-- 支付方式 -->
      <div class="payment-methods">
        <div
          v-for="method in paymentMethods"
          :key="method.value"
          class="payment-option"
          :class="{ selected: selectedPayType === method.value }"
          @click="selectedPayType = method.value"
        >
          <span class="payment-icon">{{ method.icon }}</span>
          <span>{{ method.name }}</span>
        </div>
      </div>

      <!-- 支付按钮 -->
      <el-button
        type="primary"
        size="large"
        class="pay-btn"
        @click="handlePay"
        :loading="payLoading"
      >
        立即支付 ¥{{ formatPrice(orderDetail.payAmount) }}
      </el-button>
    </div>

    <!-- 操作区域 -->
    <div class="action-section">
      <el-button @click="goBack">返回订单列表</el-button>

      <!-- 待付款 -->
      <template v-if="orderDetail.status === 0">
        <el-button type="danger" @click="cancelOrder">取消订单</el-button>
      </template>

      <!-- 待收货 -->
      <template v-else-if="orderDetail.status === 2">
        <el-button type="primary" @click="confirmReceipt">确认收货</el-button>
      </template>

      <!-- 已完成/已取消/已关闭 -->
      <template v-else-if="[3, 4, 5].includes(orderDetail.status)">
        <el-button type="danger" @click="deleteOrder">删除订单</el-button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import {
    getOrderDetailApi,
    cancelOrderApi,
    confirmReceiptApi,
    deleteOrderApi,
    submitPaymentApi,
  } from '@/api/product'
  import type { OrderDetail } from '@/api/product/type'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()
  const route = useRoute()

  // 订单详情
  const orderDetail = ref<OrderDetail>({
    orderId: 0,
    orderSn: '',
    totalAmount: 0,
    freightAmount: 0,
    promotionAmount: 0,
    payAmount: 0,
    status: 0,
    statusDesc: '',
    payType: 0,
    payTypeDesc: '',
    createTime: '',
    payTime: '',
    deliveryTime: '',
    receiveTime: '',
    closeTime: '',
    receiverName: '',
    receiverPhone: '',
    receiverPostCode: '',
    receiverProvince: '',
    receiverCity: '',
    receiverRegion: '',
    receiverDetailAddress: '',
    autoConfirmDay: 7,
    note: '',
    orderItems: [],
  })

  // 支付相关
  const paymentMethods = [
    { value: 1, name: '支付宝', icon: '📱' },
    { value: 2, name: '微信支付', icon: '💬' },
    { value: 3, name: '银行卡', icon: '💳' },
  ]
  const selectedPayType = ref(1)
  const payLoading = ref(false)

  // 格式化时间
  const formatTime = (time: string) => {
    return time.replace('T', ' ').replace(/\.\d+/, '')
  }

  // 格式化价格
  const formatPrice = (price: number) => {
    return price.toFixed(2)
  }

  // 状态标签类型
  const getStatusType = (status: number) => {
    if (status === 0) return 'warning'
    if (status === 1) return 'info'
    if (status === 2) return 'success'
    return 'default'
  }

  // 加载订单详情
  const loadOrderDetail = async () => {
    try {
      const orderSn = route.params.orderSn as string
      const detail = await getOrderDetailApi(orderSn)
      orderDetail.value = detail
    } catch (error: any) {
      ElMessage.error(error.msg || '加载订单详情失败')
      router.push('/user/orders')
    }
  }

  // 操作订单
  const cancelOrder = async () => {
    ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await cancelOrderApi(orderDetail.value.orderSn)
        ElMessage.success('订单已取消')
        loadOrderDetail()
      } catch (error: any) {
        ElMessage.error(error.msg || '取消失败')
      }
    })
  }

  const confirmReceipt = async () => {
    ElMessageBox.confirm('确认已收到商品？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'success',
    }).then(async () => {
      try {
        await confirmReceiptApi(orderDetail.value.orderSn)
        ElMessage.success('已确认收货')
        loadOrderDetail()
      } catch (error: any) {
        ElMessage.error(error.msg || '确认失败')
      }
    })
  }

  const deleteOrder = async () => {
    ElMessageBox.confirm('确定要删除此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteOrderApi(orderDetail.value.orderSn)
        ElMessage.success('订单已删除')
        router.push('/user/orders')
      } catch (error: any) {
        ElMessage.error(error.msg || '删除失败')
      }
    })
  }

  // 支付订单
  const handlePay = async () => {
    if (!orderDetail.value.orderSn) return

    payLoading.value = true
    try {
      await submitPaymentApi({
        orderSn: orderDetail.value.orderSn,
        payType: selectedPayType.value,
      })
      ElMessage.success('支付成功！')
      router.push('/user/orders')
    } catch (error: any) {
      ElMessage.error(error.msg || '支付失败')
    } finally {
      payLoading.value = false
    }
  }

  // 导航
  const goBack = () => {
    router.push('/user/orders')
  }

  // 初始化
  onMounted(() => {
    loadOrderDetail()
  })
</script>

<style scoped>
  .order-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 30px 0;
  }

  /* 通用区块 */
  .order-section {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
  }

  .section-header h2 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  /* 订单信息 */
  .order-info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }

  .info-item {
    display: flex;
  }

  .label {
    width: 100px;
    color: #666;
    font-weight: bold;
  }

  /* 收货信息 */
  .address-info {
    padding: 15px;
    background: #f9f9f9;
    border-radius: 4px;
    line-height: 1.6;
  }

  /* 商品清单 */
  .product-list {
    display: flex;
    flex-direction: column;
    gap: 15px;
  }

  .product-item {
    display: flex;
    gap: 15px;
    padding: 10px 0;
  }

  .product-image {
    width: 80px;
    height: 80px;
    flex-shrink: 0;
  }

  .product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border: 1px solid #eee;
  }

  .product-info {
    flex: 1;
  }

  .product-info h3 {
    font-size: 14px;
    font-weight: normal;
    color: #333;
    margin: 0 0 8px 0;
    line-height: 1.4;
  }

  .product-price-quantity {
    display: flex;
    gap: 15px;
    color: #666;
    font-size: 14px;
  }

  .price {
    color: #e40000;
    font-weight: bold;
  }

  /* 金额信息 */
  .amount-summary {
    border-top: 1px solid #eee;
    padding-top: 15px;
  }

  .amount-row {
    display: flex;
    justify-content: space-between;
    padding: 5px 0;
    color: #666;
    font-size: 14px;
  }

  .amount-row.total {
    font-weight: bold;
    color: #333;
    font-size: 16px;
    margin-top: 10px;
  }

  .total-amount {
    color: #e40000;
    font-size: 18px;
  }

  /* 支付区域 */
  .payment-section {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    padding: 20px;
    margin-bottom: 20px;
  }

  .payment-section h2 {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px 0;
  }

  .payment-methods {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
  }

  .payment-option {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 20px;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .payment-option:hover {
    border-color: #999;
  }

  .payment-option.selected {
    border-color: #e40000;
    background: #fef0f0;
  }

  .payment-icon {
    font-size: 20px;
  }

  .pay-btn {
    width: 280px;
    height: 50px;
    font-size: 18px;
    background: #e40000;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.2s;
  }

  .pay-btn:hover:not(:disabled) {
    background: #c30000;
  }

  .pay-btn:disabled {
    background: #b0b0b0;
    cursor: not-allowed;
  }

  /* 操作区域 */
  .action-section {
    display: flex;
    gap: 15px;
    justify-content: flex-end;
    margin-top: 20px;
  }
</style>
