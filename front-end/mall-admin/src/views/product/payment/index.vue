<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
      </div>
    </template>

    <!-- 查询条件（标准化） -->
    <el-form :model="queryParams" inline>
      <el-form-item label="订单编号">
        <el-input
          v-model="queryParams.orderSn"
          placeholder="请输入订单编号"
          clearable
          @keyup.enter="() => fetchPaymentList()"
        />
      </el-form-item>

      <el-form-item label="支付流水号">
        <el-input
          v-model="queryParams.paySn"
          placeholder="请输入支付流水号"
          clearable
          @keyup.enter="() => fetchPaymentList()"
        />
      </el-form-item>

      <el-form-item label="支付方式">
        <el-select
          v-model="queryParams.payType"
          placeholder="请选择支付方式"
          clearable
          style="width: 120px"
        >
          <el-option :value="1" label="支付宝" />
          <el-option :value="2" label="微信" />
          <el-option :value="3" label="银行卡" />
        </el-select>
      </el-form-item>

      <el-form-item label="支付状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择支付状态"
          clearable
          style="width: 120px"
        >
          <el-option :value="0" label="待支付" />
          <el-option :value="1" label="成功" />
          <el-option :value="2" label="失败" />
          <el-option :value="3" label="已退款" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchPaymentList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="paymentList"
      border
      style="width: 100%"
      v-loading="loading"
      row-key="paymentId"
    >
      <el-table-column prop="paySn" label="支付流水号" min-width="180" />

      <el-table-column prop="orderSn" label="订单编号" min-width="180" />

      <el-table-column label="用户ID" prop="memberId" width="100" />

      <el-table-column label="支付方式" width="100">
        <template #default="scope">
          {{ scope.row.payTypeDesc }}
        </template>
      </el-table-column>

      <el-table-column label="金额" width="100">
        <template #default="scope">
          <span class="text-red-600">¥{{ formatPrice(scope.row.amount) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.statusDesc }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="payTime" label="支付时间" width="180" />

      <el-table-column prop="createTime" label="创建时间" width="180" />

      <el-table-column label="操作" width="80" fixed="right">
        <template #default="scope">
          <el-button size="small" type="info" link @click="handleView(scope.row)">详情</el-button>
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

    <!-- 支付详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="支付详情"
      width="600px"
      @close="handleCloseDetail"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="支付ID">{{ detail.paymentId }}</el-descriptions-item>
        <el-descriptions-item label="订单编号">{{ detail.orderSn }}</el-descriptions-item>
        <el-descriptions-item label="支付流水号">{{ detail.paySn }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.memberId }}</el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail.payTypeDesc }}</el-descriptions-item>
        <el-descriptions-item label="支付金额">
          <span class="text-red-600">¥{{ formatPrice(detail.amount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">{{ detail.statusDesc }}</el-descriptions-item>
        <el-descriptions-item label="支付时间">
          {{ detail.payTime || '未支付' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="mt-4" v-if="detail.callbackContent">
        <h4 class="font-medium mb-2">回调原始内容</h4>
        <pre class="bg-gray-100 p-2 rounded text-sm overflow-x-auto max-h-40 overflow-y-auto">
          {{ detail.callbackContent }}
        </pre>
      </div>
      <div class="mt-4 text-gray-500" v-else>无回调内容</div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDetail">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import type { PaymentQueryParams, PaymentItem } from '@/api/payment/type'
  import { paymentListApi, paymentDetailApi } from '@/api/payment'

  const route = useRoute()

  const queryParams = reactive<PaymentQueryParams>({
    orderSn: '',
    paySn: '',
    payType: null,
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const paymentList = ref<PaymentItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const loading = ref(false)

  const detailDialogVisible = ref(false)
  const detail = ref<PaymentItem>({
    paymentId: 0,
    memberId: 0,
    orderId: 0,
    orderSn: '',
    paySn: '',
    payType: 0,
    payTypeDesc: '',
    amount: 0,
    status: 0,
    statusDesc: '',
    payTime: '',
    createTime: '',
    updateTime: '',
    callbackContent: '',
  })

  const fetchPaymentList = async (page: number = 1, size: number = 10) => {
    loading.value = true
    try {
      const params: PaymentQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await paymentListApi(params)
      paymentList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取支付记录失败'
      ElMessage.error(errorMsg)
      console.error('获取支付记录失败:', error)
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      orderSn: '',
      paySn: '',
      payType: null,
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchPaymentList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchPaymentList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchPaymentList(page, pageSize.value)
  }

  const formatPrice = (price: number): string => {
    return price.toFixed(2)
  }

  const getStatusType = (status: number) => {
    if (status === 1) return 'success'
    if (status === 0) return 'warning'
    if (status === 2) return 'danger'
    return 'info'
  }

  const handleView = async (payment: PaymentItem) => {
    try {
      const res = await paymentDetailApi(payment.paymentId)
      detail.value = res
      detailDialogVisible.value = true
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取支付详情失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleCloseDetail = () => {
    detailDialogVisible.value = false
    detail.value = {
      paymentId: 0,
      memberId: 0,
      orderId: 0,
      orderSn: '',
      paySn: '',
      payType: 0,
      payTypeDesc: '',
      amount: 0,
      status: 0,
      statusDesc: '',
      payTime: '',
      createTime: '',
      updateTime: '',
      callbackContent: '',
    }
  }

  onMounted(() => {
    fetchPaymentList()
  })
</script>

<style scoped>
  .max-h-40 {
    max-height: 160px;
  }
</style>
