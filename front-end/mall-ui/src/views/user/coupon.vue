<template>
  <NavBar />

  <div class="coupons-container">
    <h1 class="page-title">我的优惠券</h1>

    <div class="status-tabs">
      <el-button :class="{ active: activeStatus === null }" @click="changeStatus(null)">
        全部
      </el-button>
      <el-button :class="{ active: activeStatus === 0 }" @click="changeStatus(0)">未使用</el-button>
      <el-button :class="{ active: activeStatus === 1 }" @click="changeStatus(1)">已使用</el-button>
      <el-button :class="{ active: activeStatus === 2 }" @click="changeStatus(2)">已过期</el-button>
    </div>

    <div v-if="coupons.length > 0" class="coupons-list">
      <div
        v-for="coupon in coupons"
        :key="coupon.couponId"
        :class="['coupon-item', getCouponStatusClass(coupon.status)]"
      >
        <div class="coupon-amount">
          <template v-if="coupon.template.couponType === 1">
            ¥{{ formatPrice(coupon.template.discountAmount) }}
          </template>
          <template v-else-if="coupon.template.couponType === 2">
            {{ coupon.template.discountAmount }}折
          </template>
          <template v-else-if="coupon.template.couponType === 3">
            ¥{{ formatPrice(coupon.template.discountAmount) }}
          </template>
        </div>

        <div class="coupon-details">
          <h3 class="coupon-name">{{ coupon.template.name }}</h3>

          <div class="coupon-conditions">
            <template v-if="coupon.template.minOrderAmount">
              满¥{{ formatPrice(coupon.template.minOrderAmount) }}可用
            </template>
            <template v-else>无门槛使用</template>
          </div>

          <div class="coupon-time">
            {{ formatDate(coupon.receiveTime) }} 至 {{ formatDate(coupon.expireTime) }}
          </div>
        </div>

        <div class="coupon-actions">
          <template v-if="coupon.status === 0 && isCouponValid(coupon)">
            <el-button size="small" type="primary" @click="useCoupon(coupon)">立即使用</el-button>
          </template>
          <template v-else-if="coupon.status === 1">
            <span class="used-text">已使用</span>
          </template>
          <template v-else-if="coupon.status === 2 || !isCouponValid(coupon)">
            <span class="expired-text">已过期</span>
          </template>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-empty :description="getEmptyDescription()" />
      <el-button
        v-if="activeStatus === 0 || activeStatus === null"
        type="primary"
        style="margin-top: 20px"
        @click="goToHome"
      >
        去逛逛
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import { getMyCouponsApi } from '@/api/coupon'
  import type { UserCoupon } from '@/api/coupon/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()

  const activeStatus = ref<number | null>(null)
  const coupons = ref<UserCoupon[]>([])

  const formatPrice = (price: number): string => {
    return price.toFixed(2)
  }

  const formatDate = (dateString: string) => {
    return dateString.replace('T', ' ').replace(/\.\d+/, '').substring(5, 16)
  }

  const isCouponValid = (coupon: UserCoupon) => {
    const now = new Date()
    const expireTime = new Date(coupon.expireTime)
    return expireTime > now
  }

  const fetchCoupons = async () => {
    try {
      const status = activeStatus.value === null ? undefined : activeStatus.value
      const res = await getMyCouponsApi(status)
      coupons.value = res || []
    } catch (error) {
      console.error('获取优惠券失败:', error)
      ElMessage.error('加载失败')
    }
  }

  const changeStatus = (status: number | null) => {
    activeStatus.value = status
    fetchCoupons()
  }

  const useCoupon = (coupon: UserCoupon) => {
    router.push('/')
    ElMessage.success(`已选择优惠券：${coupon.template.name}`)
  }

  const getEmptyDescription = () => {
    if (activeStatus.value === 0) return '暂无可使用的优惠券'
    if (activeStatus.value === 1) return '暂无已使用的优惠券'
    if (activeStatus.value === 2) return '暂无已过期的优惠券'
    return '暂无优惠券'
  }

  const goToHome = () => {
    router.push('/')
  }

  const getCouponStatusClass = (status: number) => {
    if (status === 0) return 'coupon-available'
    if (status === 1) return 'coupon-used'
    if (status === 2) return 'coupon-expired'
    return ''
  }

  onMounted(() => {
    fetchCoupons()
  })
</script>

<style scoped>
  .coupons-container {
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

  .coupons-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .coupon-item {
    display: flex;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    overflow: hidden;
    background: #fff;
    transition: transform 0.2s;
  }

  .coupon-item:hover {
    transform: translateY(-2px);
  }

  .coupon-amount {
    width: 80px;
    background: #e40000;
    color: white;
    font-size: 20px;
    font-weight: bold;
    text-align: center;
    padding: 20px 0;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .coupon-details {
    flex: 1;
    padding: 15px;
  }

  .coupon-name {
    font-size: 16px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
  }

  .coupon-conditions {
    color: #666;
    font-size: 12px;
    margin-bottom: 8px;
  }

  .coupon-time {
    color: #999;
    font-size: 12px;
  }

  .coupon-actions {
    padding: 15px;
    display: flex;
    align-items: center;
  }

  .coupon-available {
    border-left: 4px solid #e40000;
  }

  .coupon-used {
    border-left: 4px solid #999;
    opacity: 0.6;
  }

  .coupon-expired {
    border-left: 4px solid #ccc;
    opacity: 0.4;
  }

  .used-text,
  .expired-text {
    color: #999;
    font-size: 12px;
  }

  .empty-state {
    text-align: center;
    padding: 60px 0;
  }
</style>
