<template>
  <NavBar />

  <div class="confirm-container">
    <h1 class="page-title">确认订单</h1>

    <!-- 商品清单 -->
    <div class="confirm-section">
      <h2 class="section-title">商品清单</h2>
      <div class="product-list">
        <div v-for="item in orderInfo.skus" :key="item.skuId" class="product-item">
          <div class="product-image">
            <img :src="item.skuPic" alt="商品图片" />
          </div>
          <div class="product-info">
            <h3>{{ item.skuName }}</h3>
            <div class="product-price-quantity">
              <span class="price">¥{{ item.skuPrice.toFixed(2) }}</span>
              <span class="quantity">× {{ item.quantity }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收货地址 -->
    <div class="confirm-section">
      <div class="section-header">
        <h2 class="section-title">收货地址</h2>
        <button class="edit-btn" @click="showAddressModal = true">编辑</button>
      </div>

      <div v-if="selectedAddress" class="address-info">
        <p>{{ selectedAddress.receiverName }} {{ selectedAddress.receiverPhone }}</p>
        <p>
          {{ selectedAddress.receiverProvince }}
          {{ selectedAddress.receiverCity }}
          {{ selectedAddress.receiverRegion }}
          {{ selectedAddress.receiverDetailAddress }}
        </p>
      </div>

      <div v-else class="no-address">
        <el-empty description="请先添加收货地址" />
        <el-button
          type="primary"
          size="small"
          @click="showAddressModal = true"
          style="margin-top: 15px"
        >
          添加地址
        </el-button>
      </div>
    </div>

    <!-- 优惠券选择 - 新增部分 -->
    <div class="confirm-section">
      <h2 class="section-title">优惠券</h2>
      <el-select
        v-model="selectedCouponCode"
        placeholder="请选择可用优惠券"
        clearable
        style="width: 100%"
      >
        <el-option
          v-for="coupon in availableCoupons"
          :key="coupon.couponId"
          :label="getCouponDisplayText(coupon)"
          :value="coupon.couponCode"
        />
      </el-select>
    </div>

    <!-- 付款信息 -->
    <div class="confirm-section">
      <h2 class="section-title">付款信息</h2>

      <!-- 支付方式 -->
      <div class="payment-method">
        <div
          v-for="(method, index) in paymentMethods"
          :key="index"
          class="payment-option"
          :class="{ selected: payType === method.value }"
          @click="payType = method.value"
        >
          <div class="payment-icon">{{ method.icon }}</div>
          <div class="payment-name">{{ method.name }}</div>
        </div>
      </div>

      <!-- 金额汇总 -->
      <div class="amount-summary">
        <div class="amount-row">
          <span>商品总额：</span>
          <span>¥{{ orderInfo.totalAmount.toFixed(2) }}</span>
        </div>
        <div v-if="selectedCouponCode" class="amount-row">
          <span>优惠券减免：</span>
          <span class="discount-amount">-¥{{ calculateDiscount().toFixed(2) }}</span>
        </div>
        <div class="amount-row total">
          <span>应付金额：</span>
          <span class="total-amount">¥{{ calculatePayAmount().toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <!-- 提交订单 -->
    <div class="submit-section">
      <div class="total-amount-large">
        应付金额：
        <span>¥{{ calculatePayAmount().toFixed(2) }}</span>
      </div>
      <button @click="createOrder" :disabled="!payType || !selectedAddress" class="submit-btn">
        提交订单
      </button>
    </div>

    <!-- 地址管理对话框 -->
    <el-dialog
      title="收货地址管理"
      v-model="showAddressModal"
      width="600px"
      @close="handleAddressClose"
    >
      <div class="address-dialog-content">
        <!-- 地址列表 -->
        <div v-if="addresses.length > 0" class="address-list">
          <div
            v-for="address in addresses"
            :key="address.addressId"
            class="address-item"
            :class="{ selected: selectedAddress?.addressId === address.addressId }"
            @click="selectAddress(address)"
          >
            <div class="address-info">
              <div class="address-header">
                <span class="name">{{ address.receiverName }}</span>
                <span class="phone">{{ address.receiverPhone }}</span>
                <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
              </div>
              <div class="address-detail">
                {{ address.receiverProvince }}
                {{ address.receiverCity }}
                {{ address.receiverRegion }}
                {{ address.receiverDetailAddress }}
              </div>
            </div>
            <div class="address-check">
              <el-radio :model-value="selectedAddress?.addressId" />
            </div>
          </div>
        </div>

        <!-- 无地址状态 -->
        <div v-else class="empty-address">
          <el-empty description="暂无收货地址" />
        </div>

        <!-- 操作按钮 -->
        <div class="dialog-footer">
          <el-button @click="openAddressForm">编辑地址</el-button>
          <el-button type="primary" @click="confirmAddressSelection" :disabled="!selectedAddress">
            确定
          </el-button>
        </div>
      </div>

      <teleport to="body">
        <el-dialog
          v-if="showAddressForm"
          title="新建收货地址"
          v-model="showAddressForm"
          width="500px"
          append-to-body
          @close="closeAddressForm"
        >
          <AddressForm @close="closeAddressForm" @success="handleAddressCreated" />
        </el-dialog>
      </teleport>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted, watch, onUnmounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import { useRoute, useRouter } from 'vue-router'
  import { confirmOrderApi, createOrderApi, submitPaymentApi } from '@/api/order'
  import { getAddressListApi, getDefaultAddressApi } from '@/api/address'
  import { getAvailableCouponsApi } from '@/api/coupon' // 新增导入
  import type {
    OrderConfirmRequest,
    OrderConfirmResponse,
    PaySubmitRequest,
  } from '@/api/order/type'
  import type { AddressDTO } from '@/api/address/type'
  import type { UserCoupon } from '@/api/coupon/type' // 新增导入
  import NavBar from '@/components/navbar.vue'
  import AddressForm from '@/components/addressform/index.vue'

  const route = useRoute()
  const router = useRouter()

  // 订单信息
  const orderInfo = ref<OrderConfirmResponse>({
    skus: [],
    totalAmount: 0,
    payAmount: 0,
    receiverName: '',
    receiverPhone: '',
    receiverProvince: '',
    receiverCity: '',
    receiverRegion: '',
    receiverDetailAddress: '',
  })

  // 地址相关
  const addresses = ref<AddressDTO[]>([])
  const selectedAddress = ref<AddressDTO | null>(null)
  const showAddressModal = ref(false)
  const showAddressForm = ref(false)

  // 优惠券相关 - 新增
  const availableCoupons = ref<UserCoupon[]>([])
  const selectedCouponCode = ref<string | null>(null)

  // 支付方式
  const payType = ref<number>(1) // 默认支付宝
  const paymentMethods = [
    { value: 1, name: '支付宝', icon: '📱' },
    { value: 2, name: '微信支付', icon: '💬' },
    { value: 3, name: '银行卡', icon: '💳' },
  ]

  // 全局事件监听器
  let addressUpdateListener: ((event: CustomEvent) => void) | null = null

  // 加载预订单信息
  onMounted(async () => {
    const params = route.query
    const request: OrderConfirmRequest = {}

    if (params.skuId) {
      request.skuId = Number(params.skuId)
      request.quantity = Number(params.quantity) || 1
    } else if (params.cartIds) {
      request.cartIds = (params.cartIds as string).split(',').map((id) => Number(id))
    }

    try {
      const data = await confirmOrderApi(request)
      orderInfo.value = data

      // 加载地址信息
      await loadAddresses()

      // 加载可用优惠券 - 新增
      await loadAvailableCoupons()

      // 监听地址更新事件
      addressUpdateListener = (event: CustomEvent) => {
        const updatedAddresses = event.detail.addresses as AddressDTO[]
        addresses.value = updatedAddresses
        reselectAddress(updatedAddresses)
      }

      window.addEventListener('addressListUpdated', addressUpdateListener as EventListener)
    } catch (error) {
      console.error('加载订单信息失败:', error)
    }
  })

  // 组件卸载时清理事件监听器
  onUnmounted(() => {
    if (addressUpdateListener) {
      window.removeEventListener('addressListUpdated', addressUpdateListener as EventListener)
    }
  })

  // 加载地址列表（保持不变）
  const loadAddresses = async () => {
    try {
      const defaultAddress = await getDefaultAddressApi()
      if (defaultAddress) {
        selectedAddress.value = defaultAddress
      }

      const addressList = await getAddressListApi()
      addresses.value = addressList

      if (!selectedAddress.value && addressList.length > 0) {
        selectedAddress.value = addressList[0]
      }
    } catch (error) {
      console.error('加载地址列表失败:', error)
    }
  }

  // 新增：加载可用优惠券
  const loadAvailableCoupons = async () => {
    try {
      const amount = orderInfo.value.totalAmount
      const coupons = await getAvailableCouponsApi(amount)
      availableCoupons.value = coupons || []

      console.log(coupons)
    } catch (error) {
      console.error('加载优惠券失败:', error)
      availableCoupons.value = []
    }
  }

  // 新增：计算当前选中优惠券的折扣
  const calculateDiscount = () => {
    if (!selectedCouponCode.value) return 0

    const coupon = availableCoupons.value.find((c) => c.couponCode === selectedCouponCode.value)
    if (!coupon) return 0

    const template = coupon.template
    const totalAmount = orderInfo.value.totalAmount

    if (template.couponType === 1) {
      // 满减券
      if (totalAmount >= (template.minOrderAmount || 0)) {
        return template.discountAmount || 0
      }
    } else if (template.couponType === 2) {
      // 折扣券
      if (totalAmount >= (template.minOrderAmount || 0)) {
        return totalAmount - totalAmount * (template.discountAmount || 1)
      }
    } else if (template.couponType === 3) {
      // 无门槛券
      return template.discountAmount || 0
    }

    return 0
  }

  // 新增：计算应付金额
  const calculatePayAmount = () => {
    const totalAmount = orderInfo.value.totalAmount
    const discount = calculateDiscount()
    return Math.max(0, totalAmount - discount)
  }

  // 新增：获取优惠券显示文本
  const getCouponDisplayText = (coupon: UserCoupon): string => {
    const template = coupon.template
    if (template.couponType === 1) {
      return `满${formatMoney(template.minOrderAmount)}减${formatMoney(template.discountAmount)}`
    } else if (template.couponType === 2) {
      return `${(template.discountAmount * 10).toFixed(1)}折`
    } else {
      return `无门槛${formatMoney(template.discountAmount)}`
    }
  }

  // 格式化金额
  const formatMoney = (value: number | undefined | null): string => {
    if (value == null || isNaN(Number(value))) return '0.00'
    return Number(value).toFixed(2)
  }

  // 重新选择地址的逻辑（保持不变）
  const reselectAddress = (addressList: AddressDTO[]) => {
    if (addressList.length === 0) {
      selectedAddress.value = null
      return
    }

    if (
      selectedAddress.value &&
      addressList.some((addr) => addr.addressId === selectedAddress.value?.addressId)
    ) {
      return
    }

    const defaultAddr = addressList.find((addr) => addr.isDefault)
    if (defaultAddr) {
      selectedAddress.value = defaultAddr
    } else {
      selectedAddress.value = addressList[0]
    }
  }

  // 选择地址（保持不变）
  const selectAddress = (address: AddressDTO) => {
    selectedAddress.value = address
  }

  // 确认地址选择（保持不变）
  const confirmAddressSelection = () => {
    if (selectedAddress.value) {
      showAddressModal.value = false
    }
  }

  // 关闭地址管理对话框（保持不变）
  const handleAddressClose = () => {
    showAddressModal.value = false
    showAddressForm.value = false
  }

  // 地址创建成功处理（保持不变）
  const handleAddressCreated = async (address: AddressDTO) => {
    showAddressForm.value = false
    await loadAddresses()
    selectedAddress.value = address
  }

  // 打开地址表单（保持不变）
  const openAddressForm = () => {
    showAddressForm.value = true
  }

  // 关闭地址表单（保持不变）
  const closeAddressForm = () => {
    showAddressForm.value = false
  }

  // 提交订单 - 修改以包含优惠券
  const createOrder = async () => {
    if (!selectedAddress.value) {
      ElMessage.warning('请选择收货地址')
      return
    }

    const params = route.query
    const request = {
      receiverName: selectedAddress.value.receiverName,
      receiverPhone: selectedAddress.value.receiverPhone,
      receiverProvince: selectedAddress.value.receiverProvince,
      receiverCity: selectedAddress.value.receiverCity,
      receiverRegion: selectedAddress.value.receiverRegion,
      receiverDetailAddress: selectedAddress.value.receiverDetailAddress,
    } as any

    if (params.skuId) {
      request.skuId = Number(params.skuId)
      request.quantity = Number(params.quantity) || 1
    } else if (params.cartIds) {
      request.cartIds = (params.cartIds as string).split(',').map((id) => Number(id))
    }

    // 新增：添加优惠券信息
    if (selectedCouponCode.value) {
      request.couponCode = selectedCouponCode.value
    }

    try {
      const response = await createOrderApi(request)

      const paymentRequest: PaySubmitRequest = {
        orderSn: response.orderSn,
        payType: payType.value,
      }

      await submitPaymentApi(paymentRequest)
      ElMessage.success('订单创建成功并已支付！')
      router.push(`/user/order/${response.orderSn}`)
    } catch (error: any) {
      console.error('创建订单失败:', error)
      ElMessage.error(error.msg || '创建订单失败')
    }
  }

  // 监听地址变化（保持不变）
  watch(selectedAddress, (newAddress) => {
    if (newAddress) {
      orderInfo.value.receiverName = newAddress.receiverName
      orderInfo.value.receiverPhone = newAddress.receiverPhone
      orderInfo.value.receiverProvince = newAddress.receiverProvince
      orderInfo.value.receiverCity = newAddress.receiverCity
      orderInfo.value.receiverRegion = newAddress.receiverRegion
      orderInfo.value.receiverDetailAddress = newAddress.receiverDetailAddress
    }
  })
</script>

<style scoped>
  /* 只添加必要的新样式 */
  .amount-row .discount-amount {
    color: #e40000;
    font-weight: bold;
  }

  .confirm-container {
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
  .confirm-section {
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

  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  .edit-btn {
    background: none;
    border: 1px solid #e5e5e5;
    padding: 5px 12px;
    border-radius: 4px;
    cursor: pointer;
    font-size: 12px;
    color: #333;
  }

  .edit-btn:hover {
    background: #f5f5f5;
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

  /* 收货地址 */
  .address-info {
    padding: 15px;
    background: #f9f9f9;
    border-radius: 4px;
    line-height: 1.6;
  }

  .no-address {
    text-align: center;
    padding: 20px;
  }

  /* 付款信息 */
  .payment-method {
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

  .payment-name {
    font-size: 14px;
    color: #333;
  }

  /* 金额汇总 */
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

  /* 提交区域 */
  .submit-section {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 30px;
    margin-top: 30px;
    padding: 20px;
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 4px;
  }

  .total-amount-large {
    font-size: 18px;
    color: #333;
  }

  .total-amount-large span {
    color: #e40000;
    font-size: 24px;
    font-weight: bold;
  }

  .submit-btn {
    padding: 12px 36px;
    background: #e40000;
    color: #fff;
    border: none;
    border-radius: 4px;
    font-size: 18px;
    font-weight: 600;
    cursor: pointer;
    transition: background 0.2s;
  }

  .submit-btn:hover:not(:disabled) {
    background: #c30000;
  }

  .submit-btn:disabled {
    background: #b0b0b0;
    cursor: not-allowed;
  }

  /* 地址对话框样式 */
  .address-dialog-content {
    min-height: 400px;
  }

  .address-list {
    max-height: 350px;
    overflow-y: auto;
    margin-bottom: 20px;
  }

  .address-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    margin-bottom: 10px;
    cursor: pointer;
    transition: all 0.2s;
  }

  .address-item:hover {
    border-color: #409eff;
  }

  .address-item.selected {
    border-color: #409eff;
    background-color: #f0f9ff;
  }

  .address-info {
    flex: 1;
  }

  .address-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 5px;
  }

  .name {
    font-weight: bold;
    color: #333;
  }

  .phone {
    color: #666;
    font-size: 14px;
  }

  .address-detail {
    color: #666;
    font-size: 14px;
  }

  .address-check {
    margin-left: 20px;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  .empty-address {
    text-align: center;
    padding: 40px 0;
  }
</style>
