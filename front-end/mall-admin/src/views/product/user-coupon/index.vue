<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="openIssueCouponDialog">
          <el-icon name="Promotion" />
          发放优惠券
        </el-button>
      </div>
    </template>

    <el-form :model="queryForm" inline @submit.prevent="loadCouponList">
      <el-form-item label="用户ID/昵称">
        <el-input
          v-model="queryForm.keyword"
          placeholder="请输入用户ID或昵称"
          clearable
          @keyup.enter="loadCouponList"
        />
      </el-form-item>
      <el-form-item label="优惠券状态">
        <el-select
          v-model="queryForm.status"
          placeholder="请选择状态"
          clearable
          style="width: 100px"
        >
          <el-option
            v-for="item in statusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="领取时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadCouponList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
        <el-button type="danger" @click="handleCleanExpired">清理过期</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="couponList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="couponId" label="优惠券ID" width="100" />
      <el-table-column prop="couponCode" label="优惠券码" min-width="140" />
      <el-table-column label="用户信息" min-width="150">
        <template #default="{ row }">
          <div>
            <span class="font-medium text-blue-600 cursor-pointer" @click="handleViewMember(row)">
              {{ row.memberName }}
            </span>
            <div class="text-gray-500 text-sm mt-1">{{ row.memberPhone }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="优惠券模板" min-width="180">
        <template #default="{ row }">
          <div class="font-medium text-purple-600">{{ row.templateName }}</div>
          <div class="text-gray-500 text-sm mt-1">
            <el-tag :type="getCouponTypeTagType(row.couponType)" size="small">
              {{ getCouponTypeName(row.couponType) }}
            </el-tag>
            <span v-if="row.couponType === 1" class="ml-2">
              满 {{ formatMoney(row.minOrderAmount) }} 减 {{ formatMoney(row.discountAmount) }}
            </span>
            <span v-else-if="row.couponType === 2" class="ml-2">
              {{ (row.discountAmount * 10).toFixed(1) }} 折 (满
              {{ formatMoney(row.minOrderAmount) }})
            </span>
            <span v-else-if="row.couponType === 3" class="ml-2">
              无门槛 {{ formatMoney(row.discountAmount) }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="getStatusTagType(row.status)" size="small">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="领取时间" width="160">
        <template #default="{ row }">
          {{ formatDate(row.receiveTime) }}
        </template>
      </el-table-column>
      <el-table-column label="使用/过期时间" width="160">
        <template #default="{ row }">
          <span v-if="row.status === 1" class="text-green-600">
            {{ formatDate(row.useTime) }}
          </span>
          <span v-else-if="row.status === 2" class="text-gray-500">
            {{ formatDate(row.expireTime) }}
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="订单号" width="140">
        <template #default="{ row }">
          <span
            v-if="row.orderSn"
            class="text-blue-600 cursor-pointer"
            @click="handleViewOrder(row)"
          >
            {{ row.orderSn }}
          </span>
          <span v-else class="text-gray-500">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="success"
            link
            v-if="row.status === 0"
            @click="handleUpdateStatus(row, 1)"
          >
            标记使用
          </el-button>
          <el-button
            size="small"
            type="warning"
            link
            v-if="row.status === 0"
            @click="handleUpdateStatus(row, 2)"
          >
            标记过期
          </el-button>
          <el-button size="small" type="info" link @click="handleViewDetail(row)">
            查看详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      :page-sizes="[10, 20, 50, 100]"
      class="mt-4 justify-end"
      @size-change="handlePageSizeChange"
      @current-change="handlePageChange"
    />

    <!-- 发放优惠券对话框 -->
    <el-dialog
      v-model="issueDialog.visible"
      title="发放优惠券"
      width="600px"
      @closed="resetIssueForm"
    >
      <el-form
        ref="issueFormRef"
        :model="issueForm"
        :rules="issueRules"
        label-width="100px"
        v-loading="issueDialog.loading"
      >
        <el-form-item label="选择模板" prop="templateId">
          <el-select
            v-model="issueForm.templateId"
            placeholder="请选择优惠券模板"
            style="width: 100%"
            @change="handleTemplateChange"
          >
            <el-option
              v-for="template in templateOptions"
              :key="template.templateId"
              :label="template.name"
              :value="template.templateId"
            >
              <div class="flex justify-between">
                <span>{{ template.name }}</span>
                <span class="text-gray-500 text-sm">
                  {{ template.issuedCount }} /
                  <span v-if="template.totalCount === -1">不限</span>
                  <span v-else>{{ template.totalCount }}</span>
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="发放对象" prop="memberType">
          <el-radio-group v-model="issueForm.memberType">
            <el-radio :value="1">指定用户</el-radio>
            <el-radio :value="2">全部用户</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          label="用户ID"
          prop="memberIds"
          v-if="issueForm.memberType === 1"
          class="mb-0"
        >
          <el-input
            v-model="issueForm.memberIds"
            type="textarea"
            placeholder="请输入用户ID，多个用户ID用逗号或换行分隔"
            :rows="4"
          />
          <div class="text-gray-500 text-sm mt-1">
            每行一个用户ID，或用逗号分隔，最多支持1000个用户
          </div>
        </el-form-item>

        <el-form-item label="发放数量" prop="count" v-if="issueForm.memberType === 2">
          <el-input-number
            v-model="issueForm.count"
            :min="1"
            :max="10000"
            controls-position="right"
            style="width: 200px"
          />
          <span class="text-gray-500 ml-2">个用户</span>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="issueDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitIssueForm" :loading="issueDialog.loading">
            确认发放
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更新状态对话框 -->
    <el-dialog
      v-model="statusDialog.visible"
      :title="statusDialog.status === 1 ? '标记为已使用' : '标记为已过期'"
      width="500px"
      @closed="resetStatusForm"
    >
      <el-form
        ref="statusFormRef"
        :model="statusForm"
        :rules="statusRules"
        label-width="100px"
        v-loading="statusDialog.loading"
      >
        <el-form-item label="优惠券码">
          <span class="font-medium">{{ currentCoupon?.couponCode }}</span>
        </el-form-item>

        <el-form-item label="用户">
          <span class="font-medium">
            {{ currentCoupon?.memberName }} ({{ currentCoupon?.memberPhone }})
          </span>
        </el-form-item>

        <el-form-item label="优惠内容">
          <div v-if="currentCoupon?.couponType === 1">
            满 {{ formatMoney(currentCoupon?.minOrderAmount) }} 减
            {{ formatMoney(currentCoupon?.discountAmount) }}
          </div>
          <div v-else-if="currentCoupon?.couponType === 2">
            {{ (currentCoupon?.discountAmount * 10).toFixed(1) }} 折 (满
            {{ formatMoney(currentCoupon?.minOrderAmount) }})
          </div>
          <div v-else-if="currentCoupon?.couponType === 3">
            无门槛优惠 {{ formatMoney(currentCoupon?.discountAmount) }}
          </div>
        </el-form-item>

        <el-form-item label="订单号" prop="orderSn" v-if="statusDialog.status === 1">
          <el-input v-model="statusForm.orderSn" placeholder="请输入使用订单号" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="statusDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitStatusForm" :loading="statusDialog.loading">
            确认{{ statusDialog.status === 1 ? '使用' : '过期' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 优惠券详情对话框 -->
    <el-drawer
      v-model="detailDialog.visible"
      title="优惠券详情"
      size="500px"
      :before-close="handleCloseDetail"
    >
      <div v-if="currentCoupon" class="px-4">
        <div class="grid grid-cols-2 gap-4 mb-6">
          <div>
            <span class="text-gray-500">优惠券ID</span>
            <div class="font-medium">{{ currentCoupon.couponId }}</div>
          </div>
          <div>
            <span class="text-gray-500">优惠券码</span>
            <div class="font-medium font-mono">{{ currentCoupon.couponCode }}</div>
          </div>
          <div>
            <span class="text-gray-500">用户ID</span>
            <div class="font-medium">{{ currentCoupon.memberId }}</div>
          </div>
          <div>
            <span class="text-gray-500">用户姓名</span>
            <div class="font-medium">{{ currentCoupon.memberName }}</div>
          </div>
          <div>
            <span class="text-gray-500">手机号</span>
            <div class="font-medium">{{ currentCoupon.memberPhone }}</div>
          </div>
          <div>
            <span class="text-gray-500">状态</span>
            <el-tag :type="getStatusTagType(currentCoupon.status)">
              {{ getStatusName(currentCoupon.status) }}
            </el-tag>
          </div>
        </div>

        <div class="mb-6">
          <h3 class="font-medium mb-3">优惠券模板</h3>
          <div class="bg-gray-50 p-4 rounded-lg">
            <div class="font-medium text-purple-600 mb-2">{{ currentCoupon.templateName }}</div>
            <div class="text-gray-600">
              <el-tag :type="getCouponTypeTagType(currentCoupon.couponType)" size="small">
                {{ getCouponTypeName(currentCoupon.couponType) }}
              </el-tag>
              <span v-if="currentCoupon.couponType === 1" class="ml-2">
                满 {{ formatMoney(currentCoupon.minOrderAmount) }} 减
                {{ formatMoney(currentCoupon.discountAmount) }}
              </span>
              <span v-else-if="currentCoupon.couponType === 2" class="ml-2">
                {{ (currentCoupon.discountAmount * 10).toFixed(1) }} 折 (满
                {{ formatMoney(currentCoupon.minOrderAmount) }})
              </span>
              <span v-else-if="currentCoupon.couponType === 3" class="ml-2">
                无门槛优惠 {{ formatMoney(currentCoupon.discountAmount) }}
              </span>
            </div>
          </div>
        </div>

        <div class="mb-6">
          <h3 class="font-medium mb-3">时间信息</h3>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <span class="text-gray-500">领取时间</span>
              <div class="font-medium">{{ formatDate(currentCoupon.receiveTime) }}</div>
            </div>
            <div v-if="currentCoupon.status === 1">
              <span class="text-gray-500">使用时间</span>
              <div class="font-medium">{{ formatDate(currentCoupon.useTime) }}</div>
            </div>
            <div v-else-if="currentCoupon.status === 2">
              <span class="text-gray-500">过期时间</span>
              <div class="font-medium">{{ formatDate(currentCoupon.expireTime) }}</div>
            </div>
            <div v-else>
              <span class="text-gray-500">过期时间</span>
              <div class="font-medium">{{ formatDate(currentCoupon.expireTime) }}</div>
            </div>
            <div v-if="currentCoupon.orderSn">
              <span class="text-gray-500">订单号</span>
              <div
                class="font-medium text-blue-600 cursor-pointer"
                @click="handleViewOrderFromDetail"
              >
                {{ currentCoupon.orderSn }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer flex justify-end">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
  import { Promotion, View, User, ShoppingBag } from '@element-plus/icons-vue'
  import type {
    UserCouponQueryParams,
    UserCouponDTO,
    CouponTemplateDTO,
    CouponIssueRequest,
    CouponUpdateStatusRequest,
    CouponTemplateSearchRequest,
  } from '@/api/coupon/type'
  import {
    getUserCouponsApi,
    updateUserCouponStatusApi,
    issueCouponsApi,
    getCouponTemplatesApi,
    cleanExpiredCouponsApi,
    getUserCouponApi,
  } from '@/api/coupon'

  const route = useRoute()
  const router = useRouter()

  // 页面状态
  const loading = ref(false)
  const couponList = ref<UserCouponDTO[]>([])
  const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0,
  })

  // 查询表单
  const queryForm = reactive<UserCouponQueryParams>({
    keyword: '',
    status: undefined,
    startTime: '',
    endTime: '',
  })
  const dateRange = ref<[string, string] | null>(null)

  // 对话框状态
  const issueDialog = reactive({
    visible: false,
    loading: false,
  })

  const statusDialog = reactive({
    visible: false,
    loading: false,
    status: 1, // 1-已使用, 2-已过期
    couponId: null as number | null,
  })

  const detailDialog = reactive({
    visible: false,
  })

  // 表单引用
  const issueFormRef = ref<FormInstance>()
  const statusFormRef = ref<FormInstance>()

  // 当前选中的优惠券
  const currentCoupon = ref<UserCouponDTO | null>(null)

  // 模板选项
  const templateOptions = ref<CouponTemplateDTO[]>([])

  // 表单数据
  const issueForm = reactive({
    templateId: null as number | null,
    memberType: 1, // 1-指定用户, 2-全部用户
    memberIds: '',
    count: 100,
  })

  const statusForm = reactive({
    orderSn: '',
  })

  // 选项数据
  const statusOptions = [
    { value: 0, label: '未使用' },
    { value: 1, label: '已使用' },
    { value: 2, label: '已过期' },
  ]

  // 表单验证规则
  const issueRules = computed<FormRules>(() => ({
    templateId: [{ required: true, message: '请选择优惠券模板', trigger: 'change' }],
    memberType: [{ required: true, message: '请选择发放对象', trigger: 'change' }],
    memberIds: [
      {
        validator: (rule, value, callback) => {
          if (issueForm.memberType === 1) {
            if (!value || value.trim() === '') {
              callback(new Error('请输入用户ID'))
            } else {
              const ids = value.split(/[\s,]+/).filter((id) => id.trim() !== '')
              if (ids.length === 0) {
                callback(new Error('请输入有效的用户ID'))
              } else if (ids.length > 1000) {
                callback(new Error('最多支持1000个用户'))
              } else {
                callback()
              }
            }
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    count: [
      {
        validator: (rule, value, callback) => {
          if (issueForm.memberType === 2) {
            if (value === undefined || value <= 0) {
              callback(new Error('请输入发放数量'))
            } else if (value > 10000) {
              callback(new Error('最多支持10000个用户'))
            } else {
              callback()
            }
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
  }))

  const statusRules = computed<FormRules>(() => ({
    orderSn: [
      {
        validator: (rule, value, callback) => {
          if (statusDialog.status === 1 && (!value || value.trim() === '')) {
            callback(new Error('请输入使用订单号'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
  }))

  // 计算属性
  const getCouponTypeTagType = (type: number | undefined) => {
    if (type === 1) return 'danger'
    if (type === 2) return 'warning'
    return 'success'
  }

  const getCouponTypeName = (type: number | undefined) => {
    if (type === 1) return '满减券'
    if (type === 2) return '折扣券'
    if (type === 3) return '无门槛券'
    return '未知类型'
  }

  const getStatusTagType = (status: number | undefined) => {
    if (status === 0) return 'info'
    if (status === 1) return 'success'
    if (status === 2) return 'warning'
    return 'info'
  }

  const getStatusName = (status: number | undefined) => {
    if (status === 0) return '未使用'
    if (status === 1) return '已使用'
    if (status === 2) return '已过期'
    return '未知'
  }

  // 方法
  const loadCouponList = async () => {
    loading.value = true

    try {
      if (dateRange.value) {
        queryForm.startTime = dateRange.value[0]
        queryForm.endTime = dateRange.value[1]
      } else {
        queryForm.startTime = ''
        queryForm.endTime = ''
      }

      const request = {
        params: queryForm,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
      }
      console.log(request)

      const res = await getUserCouponsApi(request)

      couponList.value = res.records || []
      pagination.total = res.total || 0
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.msg || '获取用户优惠券列表失败')
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryForm, {
      keyword: '',
      status: undefined,
      startTime: '',
      endTime: '',
    })
    dateRange.value = null
    pagination.pageNum = 1
    loadCouponList()
  }

  const handlePageSizeChange = (size: number) => {
    pagination.pageSize = size
    loadCouponList()
  }

  const handlePageChange = (page: number) => {
    pagination.pageNum = page
    loadCouponList()
  }

  const formatDate = (date: string | Date): string => {
    if (!date) return '-'
    const d = new Date(date)
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  const formatMoney = (value: number | undefined | null): string => {
    if (value == null || isNaN(Number(value))) return '0.00'
    return Number(value).toFixed(2)
  }

  const openIssueCouponDialog = async () => {
    // 先加载可用的优惠券模板
    try {
      const templateRequest = {
        params: { status: 1 }, // 只获取启用的模板
        pageNum: 1,
        pageSize: 100,
      }
      const templateRes = await getCouponTemplatesApi(templateRequest)
      templateOptions.value = templateRes.records || []

      if (templateOptions.value.length === 0) {
        ElMessage.warning('暂无可发放的优惠券模板')
        return
      }

      resetIssueForm()
      issueDialog.visible = true
    } catch (error: any) {
      ElMessage.error('加载优惠券模板失败')
    }
  }

  const handleTemplateChange = (templateId: number) => {
    // 可以在这里添加模板相关的逻辑
    console.log('选择模板:', templateId)
  }

  const handleUpdateStatus = (coupon: UserCouponDTO, status: number) => {
    currentCoupon.value = coupon
    statusDialog.status = status
    statusDialog.couponId = coupon.couponId
    resetStatusForm()
    statusDialog.visible = true
  }

  const handleViewDetail = async (coupon: UserCouponDTO) => {
    try {
      const res = await getUserCouponApi(coupon.couponId)
      if (res) {
        currentCoupon.value = res
        detailDialog.visible = true
      }
    } catch (error: any) {
      ElMessage.error('获取优惠券详情失败')
    }
  }

  const handleViewMember = (coupon: UserCouponDTO) => {
    // 跳转到会员详情页面
    router.push(`/admin/members/${coupon.memberId}`)
  }

  const handleViewOrder = (coupon: UserCouponDTO) => {
    if (coupon.orderSn) {
      // 跳转到订单详情页面
      router.push(`/admin/orders/${coupon.orderSn}`)
    }
  }

  const handleViewOrderFromDetail = () => {
    if (currentCoupon.value?.orderSn) {
      router.push(`/admin/orders/${currentCoupon.value.orderSn}`)
      detailDialog.visible = false
    }
  }

  const handleCleanExpired = () => {
    ElMessageBox.confirm('确定要清理所有过期优惠券吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        const res = await cleanExpiredCouponsApi()
        ElMessage.success(`成功清理 ${res} 张过期优惠券`)
        loadCouponList()
      } catch (error: any) {
        ElMessage.error('清理过期优惠券失败')
      }
    })
  }

  const resetIssueForm = () => {
    Object.assign(issueForm, {
      templateId: templateOptions.value.length > 0 ? templateOptions.value[0].templateId : null,
      memberType: 1,
      memberIds: '',
      count: 100,
    })

    if (issueFormRef.value) {
      issueFormRef.value.clearValidate()
    }
  }

  const submitIssueForm = async () => {
    if (!issueFormRef.value || !issueForm.templateId) return

    try {
      await issueFormRef.value.validate()

      issueDialog.loading = true

      const memberIds: number[] = []

      if (issueForm.memberType === 1) {
        // 指定用户
        const ids = issueForm.memberIds.split(/[\s,]+/).filter((id) => id.trim() !== '')
        if (ids.length === 0) {
          ElMessage.error('请输入有效的用户ID')
          return
        }

        ids.forEach((id) => {
          const num = Number(id.trim())
          if (!isNaN(num) && num > 0) {
            memberIds.push(num)
          }
        })

        if (memberIds.length === 0) {
          ElMessage.error('请输入有效的用户ID')
          return
        }

        // 限制数量
        if (memberIds.length > 1000) {
          memberIds.splice(1000)
        }
      } else {
        // 全部用户 - 这里需要后端支持，暂时返回错误
        ElMessage.error('暂不支持向全部用户发放，需联系开发人员')
        return
      }

      // 发放优惠券
      const request: CouponIssueRequest = {
        templateId: issueForm.templateId,
        memberIds: memberIds,
      }

      await issueCouponsApi(request)
      ElMessage.success(`成功发放 ${memberIds.length} 张优惠券`)
      issueDialog.visible = false
      loadCouponList()
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.msg || '发放优惠券失败')
    } finally {
      issueDialog.loading = false
    }
  }

  const resetStatusForm = () => {
    Object.assign(statusForm, {
      orderSn: '',
    })

    if (statusFormRef.value) {
      statusFormRef.value.clearValidate()
    }
  }

  const submitStatusForm = async () => {
    if (!statusFormRef.value || !statusDialog.couponId) return

    try {
      await statusFormRef.value.validate()

      statusDialog.loading = true

      const request: CouponUpdateStatusRequest = {
        couponId: statusDialog.couponId,
        status: statusDialog.status,
        orderSn: statusDialog.status === 1 ? statusForm.orderSn.trim() : undefined,
      }

      await updateUserCouponStatusApi(request)
      ElMessage.success(`标记为${statusDialog.status === 1 ? '已使用' : '已过期'}成功`)
      statusDialog.visible = false
      loadCouponList()
    } catch (error: any) {
      ElMessage.error(
        error?.response?.data?.msg || `标记${statusDialog.status === 1 ? '使用' : '过期'}失败`
      )
    } finally {
      statusDialog.loading = false
    }
  }

  const handleCloseDetail = (done: () => void) => {
    done()
  }

  // 初始化加载
  onMounted(() => {
    loadCouponList()
  })
</script>

<style scoped>
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
  }

  .text-blue-600 {
    color: #3b82f6;
  }

  .text-purple-600 {
    color: #9333ea;
  }

  .text-red-600 {
    color: #ef4444;
  }

  .text-green-600 {
    color: #10b981;
  }

  .text-gray-500 {
    color: #6b7280;
  }

  .font-medium {
    font-weight: 500;
  }

  .cursor-pointer {
    cursor: pointer;
  }

  .font-mono {
    font-family: monospace;
  }
</style>
