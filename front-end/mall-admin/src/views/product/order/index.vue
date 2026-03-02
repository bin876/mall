<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="订单编号">
        <el-input
          v-model="queryParams.orderSn"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter="() => fetchOrderList()"
        />
      </el-form-item>

      <el-form-item label="用户ID">
        <el-input
          v-model.number="queryParams.memberId"
          placeholder="请输入用户ID"
          clearable
          @keyup.enter="() => fetchOrderList()"
        />
      </el-form-item>

      <el-form-item label="用户名">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          @keyup.enter="() => fetchOrderList()"
        />
      </el-form-item>

      <el-form-item label="订单状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="下单时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          :shortcuts="shortcuts"
          @change="handleDateChange"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchOrderList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="orderList" border style="width: 100%" v-loading="loading" row-key="orderId">
      <el-table-column type="expand">
        <template #default="scope">
          <el-table :data="scope.row.orderItems" border>
            <el-table-column prop="skuName" label="商品名称" min-width="200" />
            <el-table-column label="商品图片" width="100">
              <template #default="item">
                <img
                  v-if="item.row.skuPic"
                  :src="item.row.skuPic"
                  alt="商品图片"
                  class="order-item-image"
                  @error="handleItemImageError(item.row)"
                />
                <span v-else class="no-image">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="skuPrice" label="单价" width="100">
              <template #default="item">¥{{ formatPrice(item.row.skuPrice) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="item">
                ¥{{ formatPrice(item.row.skuPrice * item.row.quantity) }}
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-table-column>

      <el-table-column prop="orderSn" label="订单编号" min-width="180" />

      <el-table-column label="用户信息" min-width="120">
        <template #default="scope">
          <div>ID: {{ scope.row.memberId }}</div>
          <div>{{ scope.row.username }}</div>
        </template>
      </el-table-column>

      <el-table-column label="收货信息" min-width="150">
        <template #default="scope">
          <div>{{ scope.row.receiverName }} {{ scope.row.receiverPhone }}</div>
          <div class="text-gray-500 text-sm">
            {{ scope.row.receiverProvince }}{{ scope.row.receiverCity
            }}{{ scope.row.receiverRegion }}
          </div>
          <div class="text-gray-500 text-sm" v-if="scope.row.receiverDetailAddress">
            {{ scope.row.receiverDetailAddress }}
          </div>
        </template>
      </el-table-column>

      <el-table-column label="金额" width="120">
        <template #default="scope">
          <div>总金额: ¥{{ formatPrice(scope.row.totalAmount) }}</div>
          <div class="text-red-600 font-medium">实付: ¥{{ formatPrice(scope.row.payAmount) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="下单时间" width="180" />

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" type="info" link @click="handleView(scope.row)">
            查看详情
          </el-button>
          <el-button
            v-if="canDeliver(scope.row.status)"
            size="small"
            type="primary"
            link
            @click="handleDeliver(scope.row)"
          >
            发货
          </el-button>
          <el-button
            v-if="canClose(scope.row.status)"
            size="small"
            type="danger"
            link
            @click="handleClose(scope.row)"
          >
            关闭订单
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100]"
      style="margin-top: 20px; justify-content: flex-end"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { OrderQueryParams, OrderListItem } from '@/api/order/type'
  import { orderListApi, updateOrderStatusApi } from '@/api/order'

  const route = useRoute()

  const statusOptions = [
    { value: 0, label: '待付款' },
    { value: 1, label: '已付款' },
    { value: 2, label: '已发货' },
    { value: 3, label: '已完成' },
    { value: 4, label: '已关闭' },
    { value: 5, label: '已取消' },
  ]

  const shortcuts = [
    { text: '最近7天', value: () => [new Date(Date.now() - 604800000), new Date()] },
    { text: '最近30天', value: () => [new Date(Date.now() - 2592000000), new Date()] },
  ]

  const queryParams = reactive<OrderQueryParams>({
    orderSn: '',
    memberId: null,
    username: '',
    status: null,
    startTime: '',
    endTime: '',
    pageNum: 1,
    pageSize: 10,
  })

  const dateRange = ref<[string, string] | null>(null)

  const orderList = ref<OrderListItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const loading = ref(false)

  const fetchOrderList = async (page: number = 1, size: number = 10) => {
    loading.value = true
    try {
      const params: OrderQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await orderListApi(params)
      orderList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取订单列表失败'
      ElMessage.error(errorMsg)
      console.error('获取订单列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      orderSn: '',
      memberId: null,
      username: '',
      status: null,
      startTime: '',
      endTime: '',
      pageNum: 1,
      pageSize: 10,
    })
    dateRange.value = null
    fetchOrderList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchOrderList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchOrderList(page, pageSize.value)
  }

  const handleDateChange = (val: [string, string] | null) => {
    if (val) {
      queryParams.startTime = val[0]
      queryParams.endTime = val[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
  }

  const formatPrice = (price: number): string => {
    return price.toFixed(2)
  }

  const getStatusLabel = (status: number) => {
    return statusOptions.find((s) => s.value === status)?.label || '未知状态'
  }

  const getStatusType = (status: number) => {
    if (status === 0) return 'warning'
    if (status === 1) return 'primary'
    if (status === 2) return 'success'
    if (status === 3) return 'info'
    return 'danger'
  }

  const canDeliver = (status: number) => status === 1 // 已付款
  const canClose = (status: number) => status === 0 || status === 1 // 待付款/已付款

  const handleItemImageError = (item: any) => {
    item.skuPic = ''
  }

  const handleView = (order: OrderListItem) => {
    console.log('查看详情:', order.orderId)
  }

  const handleDeliver = (order: OrderListItem) => {
    ElMessageBox.confirm(`确定要发货订单 ${order.orderSn} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await updateOrderStatusApi(order.orderId, 2) // 2=已发货
        ElMessage.success('发货成功')
        fetchOrderList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '发货失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const handleClose = (order: OrderListItem) => {
    ElMessageBox.confirm(`确定要关闭订单 ${order.orderSn} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await updateOrderStatusApi(order.orderId, 4) // 4=已关闭
        ElMessage.success('订单已关闭')
        fetchOrderList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '关闭订单失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchOrderList()
  })
</script>

<style scoped>
  .order-item-image {
    width: 40px;
    height: 40px;
    object-fit: contain;
    border-radius: 4px;
  }

  .no-image {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 18px;
  }
</style>
