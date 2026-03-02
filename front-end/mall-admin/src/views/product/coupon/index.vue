<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="openCreateTemplateDialog">
          <el-icon name="Plus" />
          新建模板
        </el-button>
      </div>
    </template>

    <el-form :model="queryForm" inline @submit.prevent="loadTemplateList">
      <el-form-item label="模板名称">
        <el-input
          v-model="queryForm.name"
          placeholder="请输入模板名称"
          clearable
          @keyup.enter="loadTemplateList"
        />
      </el-form-item>
      <el-form-item label="优惠券类型">
        <el-select
          v-model="queryForm.couponType"
          placeholder="请选择类型"
          clearable
          style="width: 120px"
        >
          <el-option
            v-for="item in couponTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
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
      <el-form-item label="创建时间">
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
        <el-button type="primary" @click="loadTemplateList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="templateList" border style="width: 100%" v-loading="loading">
      <el-table-column prop="templateId" label="模板ID" width="80" />
      <el-table-column prop="name" label="模板名称" min-width="150">
        <template #default="{ row }">
          <el-tooltip class="item" effect="dark" :content="row.description" placement="top">
            <span class="text-blue-600 cursor-pointer">{{ row.name }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="优惠类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getCouponTypeTagType(row.couponType)">
            {{ getCouponTypeName(row.couponType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="优惠内容" min-width="180">
        <template #default="{ row }">
          <div v-if="row.couponType === 1">
            <span class="text-red-600 font-medium">
              满 {{ formatMoney(row.minOrderAmount) }} 减 {{ formatMoney(row.discountAmount) }}
            </span>
          </div>
          <div v-else-if="row.couponType === 2">
            <span class="text-red-600 font-medium">
              {{ (row.discountAmount * 10).toFixed(1) }} 折
            </span>
            <span class="text-gray-500 ml-2">满 {{ formatMoney(row.minOrderAmount) }} 可用</span>
          </div>
          <div v-else-if="row.couponType === 3">
            <span class="text-red-600 font-medium">
              无门槛优惠 {{ formatMoney(row.discountAmount) }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="发放/总量" width="110">
        <template #default="{ row }">
          <span>
            {{ row.issuedCount }} /
            <span v-if="row.totalCount === -1">不限</span>
            <span v-else>{{ row.totalCount }}</span>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="每人限领" width="90">
        <template #default="{ row }">{{ row.perUserLimit }} 张</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="180">
        <template #default="{ row }">
          <div v-if="row.validDays">
            <span class="text-gray-500">领取后 {{ row.validDays }} 天有效</span>
          </div>
          <div v-else>
            <div>{{ formatDate(row.startTime) }}</div>
            <div class="text-gray-500">至 {{ formatDate(row.endTime) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="handleEditTemplate(row)">
            编辑
          </el-button>
          <el-button
            size="small"
            type="success"
            link
            @click="handleUpdateStatus(row, row.status === 1 ? 0 : 1)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" link @click="handleDeleteTemplate(row)">
            删除
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

    <!-- 创建/编辑模板对话框 -->
    <el-dialog
      v-model="templateDialog.visible"
      :title="templateDialog.isEdit ? '编辑优惠券模板' : '新建优惠券模板'"
      width="700px"
      @closed="resetTemplateForm"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="120px"
        v-loading="templateDialog.loading"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="name">
              <el-input
                v-model="templateForm.name"
                placeholder="请输入模板名称"
                maxlength="100"
                clearable
              />
            </el-form-item>

            <el-form-item label="优惠券类型" prop="couponType">
              <el-select
                v-model="templateForm.couponType"
                placeholder="请选择优惠券类型"
                style="width: 100%"
                @change="handleCouponTypeChange"
              >
                <el-option
                  v-for="item in couponTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>

            <el-form-item
              :label="templateForm.couponType === 2 ? '折扣比例' : '优惠金额'"
              prop="discountAmount"
            >
              <el-input-number
                v-model="templateForm.discountAmount"
                :min="templateForm.couponType === 2 ? 0.1 : 0.01"
                :max="templateForm.couponType === 2 ? 1 : 10000"
                :precision="2"
                :step="templateForm.couponType === 2 ? 0.05 : 1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="text-gray-500 ml-2">
                {{ templateForm.couponType === 2 ? '折' : '元' }}
              </span>
            </el-form-item>

            <el-form-item
              label="最低消费"
              prop="minOrderAmount"
              v-if="templateForm.couponType !== 3"
            >
              <el-input-number
                v-model="templateForm.minOrderAmount"
                :min="0.01"
                :precision="2"
                :step="10"
                controls-position="right"
                style="width: 100%"
                :disabled="templateForm.couponType === 3"
              />
              <span class="text-gray-500 ml-2">元</span>
            </el-form-item>

            <el-form-item label="每人限领" prop="perUserLimit">
              <el-input-number
                v-model="templateForm.perUserLimit"
                :min="1"
                :max="100"
                controls-position="right"
                style="width: 100%"
              />
              <span class="text-gray-500 ml-2">张/人</span>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="总发行量" prop="totalCount">
              <el-input-number
                v-model="templateForm.totalCount"
                :min="-1"
                :max="1000000"
                controls-position="right"
                style="width: 100%"
              />
              <span class="text-gray-500 ml-2" v-if="templateForm.totalCount === -1">不限</span>
              <span class="text-gray-500 ml-2" v-else>张</span>
            </el-form-item>

            <el-form-item label="有效期类型" prop="validType">
              <el-radio-group v-model="templateForm.validType" @change="handleValidTypeChange">
                <el-radio :value="1">固定有效期</el-radio>
                <el-radio :value="2">领取后N天有效</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="有效天数" prop="validDays" v-if="templateForm.validType === 2">
              <el-input-number
                v-model="templateForm.validDays"
                :min="1"
                :max="365"
                controls-position="right"
                style="width: 100%"
              />
              <span class="text-gray-500 ml-2">天</span>
            </el-form-item>

            <el-form-item label="开始时间" prop="startTime" v-if="templateForm.validType === 1">
              <el-date-picker
                v-model="templateForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime" v-if="templateForm.validType === 1">
              <el-date-picker
                v-model="templateForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="模板状态" prop="status">
              <el-radio-group v-model="templateForm.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="描述" prop="description">
              <el-input
                type="textarea"
                v-model="templateForm.description"
                placeholder="请输入模板描述"
                :rows="3"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="templateDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitTemplateForm" :loading="templateDialog.loading">
            {{ templateDialog.isEdit ? '更新模板' : '创建模板' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
  import { Plus } from '@element-plus/icons-vue'
  import type {
    CouponTemplateQueryParams,
    CouponTemplateDTO,
    CouponTemplateCreateRequest,
    CouponTemplateUpdateRequest,
  } from '@/api/coupon/type'
  import {
    getCouponTemplatesApi,
    createCouponTemplateApi,
    updateCouponTemplateApi,
    deleteCouponTemplateApi,
    updateTemplateStatusApi,
  } from '@/api/coupon'

  const route = useRoute()

  // 页面状态
  const loading = ref(false)
  const templateList = ref<CouponTemplateDTO[]>([])
  const pagination = reactive({
    pageNum: 1,
    pageSize: 10,
    total: 0,
  })

  // 查询表单
  const queryForm = reactive<CouponTemplateQueryParams>({
    name: '',
    couponType: undefined,
    status: undefined,
    startTime: '',
    endTime: '',
  })
  const dateRange = ref<[string, string] | null>(null)

  // 对话框状态
  const templateDialog = reactive({
    visible: false,
    isEdit: false,
    loading: false,
    currentId: null as number | null,
  })

  // 表单引用
  const templateFormRef = ref<FormInstance>()

  // 表单数据 - 关键修复：设置完整默认值
  const templateForm = reactive({
    name: '',
    description: '',
    couponType: 1, // 1-满减, 2-折扣, 3-无门槛
    discountAmount: 10, // 默认10元
    minOrderAmount: 100, // 默认满100元
    totalCount: -1, // -1表示不限
    perUserLimit: 1, // 默认每人1张
    validType: 1, // 1-固定有效期, 2-领取后N天有效
    validDays: 7, // 默认7天
    startTime: '',
    endTime: '',
    status: 1, // 1-启用, 0-禁用
  })

  // 选项数据
  const couponTypeOptions = [
    { value: 1, label: '满减券' },
    { value: 2, label: '折扣券' },
    { value: 3, label: '无门槛券' },
  ]

  const statusOptions = [
    { value: 1, label: '启用' },
    { value: 0, label: '禁用' },
  ]

  // 表单验证规则 - 关键修复：完整的验证规则
  const templateRules = computed<FormRules>(() => ({
    name: [
      { required: true, message: '请输入模板名称', trigger: 'blur' },
      { max: 100, message: '模板名称不能超过100个字符', trigger: 'blur' },
    ],
    couponType: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
    discountAmount: [
      { required: true, message: '请输入优惠金额', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (templateForm.couponType === 2) {
            if (value <= 0 || value > 1) {
              callback(new Error('折扣比例必须在0.1-1之间'))
            } else {
              callback()
            }
          } else {
            if (value <= 0) {
              callback(new Error('优惠金额必须大于0'))
            } else {
              callback()
            }
          }
        },
        trigger: 'blur',
      },
    ],
    minOrderAmount: [
      {
        validator: (rule, value, callback) => {
          if (templateForm.couponType !== 3 && (value === undefined || value <= 0)) {
            callback(new Error('最低消费金额必须大于0'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    totalCount: [
      { required: true, message: '请输入总发行量', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value < -1) {
            callback(new Error('总发行量不能小于-1'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    perUserLimit: [
      { required: true, message: '请输入每人限领数量', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value <= 0) {
            callback(new Error('每人限领数量必须大于0'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    validType: [{ required: true, message: '请选择有效期类型', trigger: 'change' }],
    validDays: [
      {
        validator: (rule, value, callback) => {
          if (templateForm.validType === 2 && (value === undefined || value <= 0)) {
            callback(new Error('有效天数必须大于0'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    startTime: [
      {
        validator: (rule, value, callback) => {
          if (templateForm.validType === 1 && !value) {
            callback(new Error('请选择开始时间'))
          } else {
            callback()
          }
        },
        trigger: 'change',
      },
    ],
    endTime: [
      {
        validator: (rule, value, callback) => {
          if (templateForm.validType === 1 && !value) {
            callback(new Error('请选择结束时间'))
          } else if (
            templateForm.validType === 1 &&
            value &&
            templateForm.startTime &&
            new Date(value) <= new Date(templateForm.startTime)
          ) {
            callback(new Error('结束时间必须晚于开始时间'))
          } else {
            callback()
          }
        },
        trigger: 'change',
      },
    ],
    status: [{ required: true, message: '请选择模板状态', trigger: 'change' }],
    description: [{ max: 200, message: '描述不能超过200个字符', trigger: 'blur' }],
  }))

  // 计算属性
  const getCouponTypeTagType = (type: number) => {
    return type === 1 ? 'danger' : type === 2 ? 'warning' : 'success'
  }

  const getCouponTypeName = (type: number) => {
    return couponTypeOptions.find((item) => item.value === type)?.label || '未知类型'
  }

  // 方法
  const loadTemplateList = async () => {
    loading.value = true

    try {
      if (dateRange.value) {
        queryForm.startTime = dateRange.value[0]
        queryForm.endTime = dateRange.value[1]
      } else {
        queryForm.startTime = ''
        queryForm.endTime = ''
      }

      const params = {
        params: queryForm,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize,
      }

      console.log(params)

      const res = await getCouponTemplatesApi(params)
      templateList.value = res.records || []
      pagination.total = res.total || 0
    } catch (error) {
      ElMessage.error('获取优惠券模板列表失败')
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryForm, {
      name: '',
      couponType: undefined,
      status: undefined,
      startTime: '',
      endTime: '',
    })
    dateRange.value = null
    pagination.pageNum = 1
    loadTemplateList()
  }

  const handlePageSizeChange = (size: number) => {
    pagination.pageSize = size
    loadTemplateList()
  }

  const handlePageChange = (page: number) => {
    pagination.pageNum = page
    loadTemplateList()
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

  const openCreateTemplateDialog = () => {
    templateDialog.isEdit = false
    templateDialog.currentId = null
    resetTemplateForm()
    templateDialog.visible = true
  }

  const handleEditTemplate = (template: CouponTemplateDTO) => {
    templateDialog.isEdit = true
    templateDialog.currentId = template.templateId

    // 转换表单数据
    templateForm.name = template.name
    templateForm.description = template.description || ''
    templateForm.couponType = template.couponType
    templateForm.discountAmount = Number(template.discountAmount)
    templateForm.minOrderAmount = template.minOrderAmount
      ? Number(template.minOrderAmount)
      : undefined
    templateForm.totalCount = template.totalCount
    templateForm.perUserLimit = template.perUserLimit
    templateForm.status = template.status

    // 确定有效期类型
    if (template.validDays) {
      templateForm.validType = 2
      templateForm.validDays = template.validDays
      templateForm.startTime = ''
      templateForm.endTime = ''
    } else {
      templateForm.validType = 1
      templateForm.validDays = 7
      templateForm.startTime = template.startTime ? formatDateForInput(template.startTime) : ''
      templateForm.endTime = template.endTime ? formatDateForInput(template.endTime) : ''
    }

    templateDialog.visible = true
  }

  const handleUpdateStatus = (template: CouponTemplateDTO, status: number) => {
    ElMessageBox.confirm(
      `确定要${status === 1 ? '启用' : '禁用'}优惠券模板 "${template.name}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    ).then(async () => {
      try {
        await updateTemplateStatusApi(template.templateId, status)
        ElMessage.success(`${status === 1 ? '启用' : '禁用'}成功`)
        loadTemplateList()
      } catch (error: any) {
        ElMessage.error(error?.msg || `${status === 1 ? '启用' : '禁用'}失败`)
      }
    })
  }

  const handleDeleteTemplate = (template: CouponTemplateDTO) => {
    ElMessageBox.confirm(`确定要删除优惠券模板 "${template.name}" 吗？此操作不可恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteCouponTemplateApi(template.templateId)
        ElMessage.success('删除模板成功')
        loadTemplateList()
      } catch (error: any) {
        ElMessage.error(error?.msg || '删除模板失败')
      }
    })
  }

  const resetTemplateForm = () => {
    // 关键修复：设置完整的默认值
    Object.assign(templateForm, {
      name: '',
      description: '',
      couponType: 1, // 满减券
      discountAmount: 10, // 10元
      minOrderAmount: 100, // 满100元
      totalCount: -1, // 不限
      perUserLimit: 1, // 每人1张
      validType: 1, // 固定有效期
      validDays: 7, // 7天
      startTime: '',
      endTime: '',
      status: 1, // 启用
    })

    // 设置默认日期范围
    const now = new Date()
    const startTime = new Date(now)
    startTime.setHours(0, 0, 0, 0)

    const endTime = new Date(now)
    endTime.setDate(endTime.getDate() + 7) // 7天后
    endTime.setHours(23, 59, 59, 999)

    templateForm.startTime = formatDateForInput(startTime)
    templateForm.endTime = formatDateForInput(endTime)

    if (templateFormRef.value) {
      templateFormRef.value.clearValidate()
    }
  }

  const formatDateForInput = (date: Date | string): string => {
    if (typeof date === 'string') {
      date = new Date(date)
    }
    return date.toISOString().replace('T', ' ').substring(0, 19)
  }

  const handleCouponTypeChange = (value: number) => {
    if (value === 2) {
      // 折扣券
      templateForm.discountAmount = 0.9 // 9折
      if (!templateForm.minOrderAmount || templateForm.minOrderAmount <= 0) {
        templateForm.minOrderAmount = 50 // 满50元
      }
    } else if (value === 3) {
      // 无门槛券
      templateForm.discountAmount = 5 // 5元
      templateForm.minOrderAmount = 0
    } else {
      // 满减券
      templateForm.discountAmount = 10 // 10元
      if (!templateForm.minOrderAmount || templateForm.minOrderAmount <= 0) {
        templateForm.minOrderAmount = 100 // 满100元
      }
    }
  }

  const handleValidTypeChange = (value: number) => {
    if (value === 1) {
      // 固定有效期
      const now = new Date()
      const startTime = new Date(now)
      startTime.setHours(0, 0, 0, 0)

      const endTime = new Date(now)
      endTime.setDate(endTime.getDate() + 7)
      endTime.setHours(23, 59, 59, 999)

      templateForm.startTime = formatDateForInput(startTime)
      templateForm.endTime = formatDateForInput(endTime)
      templateForm.validDays = 7
    } else {
      // 领取后N天有效
      templateForm.startTime = ''
      templateForm.endTime = ''
      if (!templateForm.validDays || templateForm.validDays <= 0) {
        templateForm.validDays = 7
      }
    }
  }

  const submitTemplateForm = async () => {
    if (!templateFormRef.value) return

    try {
      await templateFormRef.value.validate()

      templateDialog.loading = true

      // 关键修复：确保所有字段正确转换
      const requestData = {
        name: templateForm.name.trim(),
        description: templateForm.description?.trim() || '',
        couponType: templateForm.couponType,
        discountAmount: Number(templateForm.discountAmount),
        minOrderAmount:
          templateForm.couponType !== 3 ? Number(templateForm.minOrderAmount) : undefined,
        totalCount: Number(templateForm.totalCount),
        perUserLimit: Number(templateForm.perUserLimit),
        validDays: templateForm.validType === 2 ? Number(templateForm.validDays) : undefined,
        startTime: templateForm.validType === 1 ? templateForm.startTime : undefined,
        endTime: templateForm.validType === 1 ? templateForm.endTime : undefined,
        status: Number(templateForm.status),
      }

      if (templateDialog.isEdit && templateDialog.currentId) {
        // 更新模板
        const updateRequest: CouponTemplateUpdateRequest = {
          templateId: templateDialog.currentId,
          ...requestData,
        }

        await updateCouponTemplateApi(updateRequest)
        ElMessage.success('更新模板成功')
      } else {
        // 创建模板
        const createRequest: CouponTemplateCreateRequest = {
          ...requestData,
        }

        await createCouponTemplateApi(createRequest)
        ElMessage.success('创建模板成功')
      }

      templateDialog.visible = false
      loadTemplateList()
    } catch (error: any) {
      // 关键修复：详细错误处理
      let errorMsg =
        error?.response?.data?.msg ||
        error?.msg ||
        (templateDialog.isEdit ? '更新模板失败' : '创建模板失败')

      // 处理验证错误
      if (error?.response?.data?.code === 400) {
        const errors = error.response.data.data || error.response.data
        if (typeof errors === 'object') {
          Object.entries(errors).forEach(([field, message]) => {
            ElMessage.error(`${field}: ${message}`)
          })
        } else if (typeof errors === 'string') {
          ElMessage.error(errors)
        }
        return
      }

      // 处理Spring Validation错误
      if (error?.response?.data?.errors) {
        error.response.data.errors.forEach((err: any) => {
          ElMessage.error(`${err.field}: ${err.defaultMessage}`)
        })
        return
      }

      ElMessage.error(errorMsg)
      console.error('表单提交错误:', error)
    } finally {
      templateDialog.loading = false
    }
  }

  // 初始化加载
  onMounted(() => {
    loadTemplateList()
    resetTemplateForm() // 初始化表单
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

  .cursor-pointer {
    cursor: pointer;
  }

  .text-red-600 {
    color: #ef4444;
  }

  .text-gray-500 {
    color: #6b7280;
  }
</style>
