<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="openCreateDialog">新建秒杀活动</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="活动名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入活动名称"
          clearable
          @keyup.enter="() => fetchActivityList()"
        />
      </el-form-item>

      <el-form-item label="活动状态">
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

      <el-form-item label="活动时间">
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
        <el-button type="1em" @click="() => fetchActivityList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="activityList" border style="width: 100%" v-loading="loading" row-key="id">
      <el-table-column type="expand">
        <template #default="scope">
          <el-table :data="scope.row.skus || []" border>
            <el-table-column prop="spuName" label="商品SPU" min-width="150" />
            <el-table-column prop="skuName" label="商品SKU" min-width="150" />
            <el-table-column label="商品图片" width="100">
              <template #default="item">
                <img
                  v-if="item.row.defaultImg"
                  :src="item.row.defaultImg"
                  alt="商品图片"
                  class="seckill-item-image"
                  @error="handleItemImageError(item.row)"
                />
                <span v-else class="no-image">-</span>
              </template>
            </el-table-column>
            <el-table-column label="原价/秒杀价" width="150">
              <template #default="item">
                <div>原价: ¥{{ formatPrice(item.row.price) }}</div>
                <div class="text-red-600 font-medium">
                  秒杀: ¥{{ formatPrice(item.row.seckillPrice) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="库存" width="100">
              <template #default="item">
                <div>总计: {{ formatNumber(item.row.totalStock) }}</div>
                <div>可用: {{ formatNumber(item.row.availableStock) }}</div>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="活动名称" min-width="180" />

      <el-table-column label="活动时间" min-width="200">
        <template #default="scope">
          <div>{{ formatDate(scope.row.startTime) }}</div>
          <div class="text-gray-500 text-sm">至</div>
          <div>{{ formatDate(scope.row.endTime) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="商品数量" width="100">
        <template #default="scope">{{ formatNumber(scope.row.skus?.length) || 0 }} 个</template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="180" />

      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button size="small" type="info" link @click="handleView(scope.row)">
            查看详情
          </el-button>
          <el-button
            v-if="canEdit(scope.row.status)"
            size="small"
            type="primary"
            link
            @click="openEditDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="canPreload(scope.row.status)"
            size="small"
            type="success"
            link
            @click="handlePreload(scope.row)"
          >
            预热库存
          </el-button>
          <el-button
            v-if="canEnd(scope.row.status)"
            size="small"
            type="warning"
            link
            @click="handleEnd(scope.row)"
          >
            结束活动
          </el-button>
          <el-button
            v-if="canDelete(scope.row.status)"
            size="small"
            type="danger"
            link
            @click="handleDelete(scope.row)"
          >
            删除
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

  <!-- 创建/编辑活动弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    :title="editingActivity ? '编辑秒杀活动' : '新建秒杀活动'"
    width="800px"
    @closed="resetForm"
  >
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="活动名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入活动名称" maxlength="50" />
          </el-form-item>

          <el-form-item label="活动时间" prop="timeRange" required>
            <el-date-picker
              v-model="formData.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="状态" v-if="editingActivity">
            <el-tag :type="getStatusType(formData.status)">
              {{ getStatusLabel(formData.status) }}
            </el-tag>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="选择商品SPU" prop="spuId" required>
            <el-select
              v-model="formData.spuId"
              placeholder="请选择商品SPU"
              filterable
              remote
              :remote-method="handleSearchSpu"
              :loading="spuLoading"
              style="width: 100%"
              @visible-change="handleVisibleChange"
              @clear="handleSpuClear"
              @change="handleSpuChange"
            >
              <el-option
                v-for="spu in spuOptions"
                :key="spu.spuId"
                :label="spu.name"
                :value="spu.spuId"
              >
                <div class="spu-option">
                  <img
                    v-if="spu.defaultImg"
                    :src="spu.defaultImg"
                    class="spu-image"
                    alt="商品图片"
                  />
                  <span class="spu-name">{{ spu.name }}</span>
                </div>
              </el-option>
              <template #empty>
                <div class="no-data">
                  <el-icon name="Search" class="text-gray-400" />
                  <span class="text-gray-500">
                    {{ spuSearchQuery ? '没有找到匹配的商品' : '请输入商品名称搜索' }}
                  </span>
                </div>
              </template>
            </el-select>
          </el-form-item>

          <el-form-item label="商品SKU" v-if="formData.spuId" prop="skuIds" required>
            <el-table
              :data="skuList"
              height="200"
              style="width: 100%"
              @selection-change="handleSkuSelectionChange"
            >
              <el-table-column type="selection" width="50" />
              <el-table-column prop="name" label="SKU名称" min-width="150" />
              <el-table-column label="图片" width="80">
                <template #default="scope">
                  <img
                    v-if="scope.row.defaultImg"
                    :src="scope.row.defaultImg"
                    class="sku-image"
                    alt="商品图片"
                  />
                </template>
              </el-table-column>
              <el-table-column label="原价" width="100">
                <template #default="scope">¥{{ formatPrice(scope.row.price) }}</template>
              </el-table-column>
              <el-table-column label="规格" min-width="150">
                <template #default="scope">
                  <div
                    v-for="attr in scope.row.saleAttrs"
                    :key="attr.attrId"
                    class="text-xs text-gray-500"
                  >
                    {{ attr.attrName }}: {{ attr.attrValue }}
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- SKU配置列表 -->
      <div v-if="formData.selectedSkus.length > 0" class="mt-4">
        <h4 class="font-medium mb-2">秒杀SKU配置</h4>
        <el-table :data="formData.selectedSkus" border>
          <el-table-column prop="skuName" label="SKU名称" min-width="200" />
          <el-table-column label="规格" min-width="150">
            <template #default="scope">
              <div v-for="attr in scope.row.attrs" :key="attr.attrId" class="text-xs text-gray-500">
                {{ attr.attrName }}: {{ attr.attrValue }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="秒杀价" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.seckillPrice"
                :min="0.01"
                :precision="2"
                :step="0.01"
                controls-position="right"
                style="width: 100%"
                @change="validateSeckillPrice(scope.row)"
              />
              <span class="text-gray-500 text-sm ml-1">
                原价: ¥{{ formatPrice(scope.row.price) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.stock"
                :min="1"
                :max="scope.row.totalStock"
                :step="1"
                controls-position="right"
                style="width: 100%"
              />
              <span class="text-gray-500 text-sm ml-1">
                总库存: {{ formatNumber(scope.row.totalStock) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="scope">
              <el-button type="danger" size="small" link @click="removeSku(scope.$index)">
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else-if="formData.spuId" class="text-center py-8 text-gray-500">
        <el-icon name="Collection" class="text-3xl mb-2 block text-gray-300" />
        <p>请选择要参与秒杀的商品SKU</p>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          {{ editingActivity ? '更新活动' : '创建活动' }}
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- 活动详情对话框 -->
  <el-dialog
    v-model="detailDialogVisible"
    title="活动详情"
    width="800px"
    @closed="detailDialogVisible = false"
  >
    <div v-if="viewingActivity" class="activity-detail">
      <div class="detail-section">
        <h3>活动基本信息</h3>
        <div class="detail-grid">
          <div class="detail-item">
            <span class="label">活动名称:</span>
            <span class="value">{{ viewingActivity.name }}</span>
          </div>
          <div class="detail-item">
            <span class="label">开始时间:</span>
            <span class="value">{{ formatDate(viewingActivity.startTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">结束时间:</span>
            <span class="value">{{ formatDate(viewingActivity.endTime) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">状态:</span>
            <span class="value">
              <el-tag :type="getStatusType(viewingActivity.status)">
                {{ getStatusLabel(viewingActivity.status) }}
              </el-tag>
            </span>
          </div>
        </div>
      </div>

      <div class="detail-section" v-if="viewingActivity.skus && viewingActivity.skus.length > 0">
        <h3>活动商品SKU</h3>
        <el-table :data="viewingActivity.skus" border>
          <el-table-column prop="spuName" label="商品SPU" min-width="150" />
          <el-table-column prop="skuName" label="商品SKU" min-width="150" />
          <el-table-column label="商品图片" width="100">
            <template #default="scope">
              <img
                v-if="scope.row.defaultImg"
                :src="scope.row.defaultImg"
                alt="商品图片"
                class="detail-image"
              />
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="原价/秒杀价" width="150">
            <template #default="scope">
              <div>原价: ¥{{ formatPrice(scope.row.price) }}</div>
              <div class="text-red-600 font-medium">
                秒杀: ¥{{ formatPrice(scope.row.seckillPrice) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="库存" width="100">
            <template #default="scope">
              <div>总计: {{ formatNumber(scope.row.totalStock) }}</div>
              <div>可用: {{ formatNumber(scope.row.availableStock) }}</div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else class="text-center py-8 text-gray-500">
        <el-icon name="Box" class="text-3xl mb-2 block text-gray-300" />
        <p>该活动暂无商品SKU</p>
      </div>
    </div>
    <div v-else>
      <el-empty description="活动详情加载中..." />
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
  import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox, FormInstance, FormRules, ElIcon } from 'element-plus'
  import { Search, Collection, Box } from '@element-plus/icons-vue'
  import type {
    SeckillActivityQueryParams,
    SeckillActivityItem,
    SeckillSkuDTO,
    SeckillActivityCreateRequest,
    SeckillActivityUpdateRequest,
    SpuDTO,
    SkuDTO,
  } from '@/api/seckill/type'
  import {
    seckillActivityListApi,
    getSeckillActivityApi,
    createSeckillActivityApi,
    updateSeckillActivityApi,
    deleteSeckillActivityApi,
    preloadSeckillStockApi,
    endSeckillActivityApi,
    getSpuSkusForSeckillApi,
    getSeckillActivityForEditApi,
  } from '@/api/seckill'
  import { spuListApi } from '@/api/spu'
  import type { PageResult } from '@/api/type'

  const route = useRoute()
  const router = useRouter()

  const spuSearchQuery = ref('')
  const spuSearchTimer = ref<NodeJS.Timeout | null>(null)
  const statusOptions = ref<Array<{ value: number; label: string }>>([
    { value: 0, label: '未开始' },
    { value: 1, label: '进行中' },
    { value: 2, label: '已结束' },
  ])
  const shortcuts = ref<Array<{ text: string; value: () => [Date, Date] }>>([
    {
      text: '最近7天',
      value: () => [new Date(Date.now() - 604800000), new Date()],
    },
    {
      text: '最近30天',
      value: () => [new Date(Date.now() - 2592000000), new Date()],
    },
  ])
  const queryParams = reactive<SeckillActivityQueryParams>({
    name: '',
    status: null,
    startTime: '',
    endTime: '',
    pageNum: 1,
    pageSize: 10,
  })
  const dateRange = ref<[string, string] | null>(null)
  const activityList = ref<SeckillActivityItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const loading = ref(false)
  const dialogVisible = ref(false)
  const editingActivity = ref<SeckillActivityItem | null>(null)
  const formData = reactive({
    name: '',
    timeRange: [] as string[],
    spuId: null as number | null,
    skuIds: [] as number[],
    selectedSkus: [] as Array<{
      skuId: number
      spuId: number
      skuName: string
      attrs: Array<{ attrId: number; attrName: string; attrValue: string }>
      price: number
      stock: number
      seckillPrice: number
      totalStock: number
    }>,
  })
  const spuOptions = ref<SpuDTO[]>([])
  const spuLoading = ref(false)
  const skuList = ref<SkuDTO[]>([])
  const formRef = ref<FormInstance>()
  const submitLoading = ref(false)
  const detailDialogVisible = ref(false)
  const viewingActivity = ref<SeckillActivityItem | null>(null)

  // 表单验证规则
  const rules = computed<FormRules>(() => ({
    name: [
      { required: true, message: '请输入活动名称', trigger: 'blur' },
      { min: 2, max: 50, message: '活动名称长度为2-50个字符', trigger: 'blur' },
    ],
    timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }],
    spuId: [{ required: true, message: '请选择商品SPU', trigger: 'change' }],
    skuIds: [{ required: true, message: '请至少选择一个商品SKU', trigger: 'change' }],
  }))

  // 搜索SPU
  const handleSearchSpu = (query: string) => {
    console.log('搜索SPU:', query)
    if (spuSearchTimer.value) {
      clearTimeout(spuSearchTimer.value)
    }

    spuSearchQuery.value = query

    if (!query || query.trim().length < 2) {
      spuOptions.value = []
      return
    }

    spuSearchTimer.value = setTimeout(() => {
      searchSpu(query.trim())
    }, 300)
  }

  // 处理下拉菜单显示/隐藏
  const handleVisibleChange = (visible: boolean) => {
    console.log('下拉菜单状态:', visible)
    if (visible && !spuOptions.value.length && formData.spuId) {
      // 如果已选择SPU但下拉选项为空，重新搜索
      const selectedSpu = spuOptions.value.find((spu) => spu.spuId === formData.spuId)
      if (selectedSpu) {
        searchSpu(selectedSpu.name)
      }
    }
  }

  // 处理清除选择
  const handleSpuClear = () => {
    console.log('清除SPU选择')
    formData.spuId = null
    spuOptions.value = []
    spuSearchQuery.value = ''
    skuList.value = []
    formData.skuIds = []
    formData.selectedSkus = []
  }

  // 修复的搜索SPU方法
  const searchSpu = async (query: string) => {
    spuLoading.value = true
    try {
      console.log('正在搜索SPU:', query)
      const params = {
        name: query,
        pageNum: 1,
        pageSize: 10,
      }

      const res = await spuListApi(params)
      spuOptions.value = res.records || []
      console.log('SPU搜索结果:', spuOptions.value)
    } catch (error: any) {
      console.error('搜索SPU失败:', error)
      ElMessage.error(error?.response?.data?.msg || '搜索商品失败')
      spuOptions.value = []
    } finally {
      spuLoading.value = false
    }
  }

  // 修复SPU选择处理
  const handleSpuChange = async (spuId: number | null) => {
    console.log('SPU选择变化:', spuId)

    if (!spuId) {
      skuList.value = []
      formData.skuIds = []
      formData.selectedSkus = []
      return
    }

    try {
      spuLoading.value = true

      // 获取SPU详情
      const selectedSpu = spuOptions.value.find((spu) => spu.spuId === spuId)
      console.log('选中的SPU:', selectedSpu)

      if (selectedSpu) {
        // 调用获取SKU的API
        const skus = await getSpuSkusForSeckillApi(spuId)
        console.log('获取到的SKU列表:', skus)

        // 转换SKU数据格式
        skuList.value = skus.map((sku) => ({
          ...sku,
          spuId: spuId,
          spuName: selectedSpu.name,
        }))
      } else {
        // 如果SPU不在选项中，重新搜索
        const spuRes = await spuListApi({
          spuId: spuId,
          pageNum: 1,
          pageSize: 1,
        })

        if (spuRes.records && spuRes.records.length > 0) {
          const spu = spuRes.records[0]
          const skus = await getSpuSkusForSeckillApi(spuId)
          skuList.value = skus.map((sku) => ({
            ...sku,
            spuId: spuId,
            spuName: spu.name,
          }))
        } else {
          skuList.value = []
          ElMessage.warning('未找到对应的SPU信息')
        }
      }
    } catch (error: any) {
      console.error('获取SKU列表失败:', error)
      ElMessage.error(error?.response?.data?.msg || '获取商品SKU失败')
      skuList.value = []
    } finally {
      spuLoading.value = false
    }
  }

  // 获取活动列表
  const fetchActivityList = async (page: number = 1, size: number = 10) => {
    loading.value = true
    try {
      const params: SeckillActivityQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await seckillActivityListApi(params)
      activityList.value = res.records || []
      total.value = res.total || 0
      console.log('活动列表:', activityList.value)
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取秒杀活动列表失败'
      ElMessage.error(errorMsg)
      console.error('获取秒杀活动列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 重置查询条件
  const resetQuery = () => {
    Object.assign(queryParams, {
      name: '',
      status: null,
      startTime: '',
      endTime: '',
      pageNum: 1,
      pageSize: 10,
    })
    dateRange.value = null
    fetchActivityList()
  }

  // 处理分页大小变化
  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchActivityList(currentPage.value, size)
  }

  // 处理当前页变化
  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchActivityList(page, pageSize.value)
  }

  // 处理日期范围变化
  const handleDateChange = (val: [string, string] | null) => {
    if (val) {
      queryParams.startTime = val[0]
      queryParams.endTime = val[1]
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
  }

  // 格式化价格
  const formatPrice = (price: number | undefined | null): string => {
    if (price == null || isNaN(Number(price))) {
      return '0.00'
    }
    return Number(price).toFixed(2)
  }

  // 格式化数字
  const formatNumber = (value: number | undefined | null): string => {
    if (value == null || isNaN(Number(value))) {
      return '0'
    }
    return Number(value).toLocaleString()
  }

  // 格式化日期
  const formatDate = (date: string): string => {
    if (!date) return '-'
    try {
      return new Date(date)
        .toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
        })
        .replace(/\//g, '-')
    } catch (e) {
      return date
    }
  }

  // 获取状态标签
  const getStatusLabel = (status: number) => {
    return statusOptions.value.find((s) => s.value === status)?.label || '未知状态'
  }

  // 获取状态类型
  const getStatusType = (status: number) => {
    if (status === 0) return 'info'
    if (status === 1) return 'success'
    return 'danger'
  }

  // 检查是否可以编辑
  const canEdit = (status: number) => status === 0

  // 棣査是否可以预热
  const canPreload = (status: number) => status === 0

  // 検査是否可以结束
  const canEnd = (status: number) => status === 1

  // 検査是否可以删除
  const canDelete = (status: number) => status === 0

  // 处理图片加载错误
  const handleItemImageError = (item: SeckillSkuDTO) => {
    item.defaultImg = ''
  }

  const handleView = async (activity: SeckillActivityItem) => {
    try {
      const res = await getSeckillActivityApi(activity.id)
      if (res && res) {
        viewingActivity.value = res
        detailDialogVisible.value = true
      } else {
        ElMessage.warning('活动详情为空')
      }
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取活动详情失败'
      ElMessage.error(errorMsg)
    }
  }

  // 打开创建对话框
  const openCreateDialog = () => {
    editingActivity.value = null
    resetForm()
    dialogVisible.value = true
  }

  // 打开编辑对话框
  const openEditDialog = async (activity: SeckillActivityItem) => {
    try {
      // 调用专用的编辑接口
      const res = await getSeckillActivityForEditApi(activity.id)
      console.log('编辑活动数据:', res)

      if (res) {
        editingActivity.value = res

        formData.name = res.name

        formData.timeRange = [
          formatDateForPicker(res.startTime),
          formatDateForPicker(res.endTime),
        ]

        formData.status = res.status

        if (res.skus && res.skus.length > 0) {
          formData.spuId = res.skus[0].spuId

          await searchSpu(res.skus[0].spuName || '')

          formData.skuIds = res.skus.map((sku) => sku.skuId)
          formData.selectedSkus = res.skus.map((sku) => ({
            skuId: sku.skuId,
            spuId: sku.spuId,
            skuName: sku.skuName,
            attrs: sku.saleAttrs || [],
            price: sku.originalPrice || 0, // 原价
            stock: sku.availableStock, // 当前库存
            seckillPrice: sku.seckillPrice, // 秒杀价
            totalStock: sku.totalStock, // 总库存
            maxStock: sku.maxStock || sku.totalStock, // 最大可设置库存
            originalStock: sku.originalStock || sku.availableStock, // 原始库存
          }))
        } else {
          formData.spuId = null
          formData.skuIds = []
          formData.selectedSkus = []
        }

        dialogVisible.value = true
        ElMessage.success('活动数据加载成功')
      } else {
        ElMessage.warning('活动详情为空')
      }
    } catch (error: any) {
      console.error('编辑活动失败:', error)
      const errorMsg = error?.response?.data?.msg || '获取活动编辑详情失败'
      ElMessage.error(errorMsg)
    }
  }

  // 格式化日期用于日期选择器
  const formatDateForPicker = (dateString: string): string => {
    if (!dateString) return ''

    try {
      const date = new Date(dateString)
      return date.toISOString().replace('T', ' ').substring(0, 19)
    } catch (e) {
      console.error('日期格式化失败:', e)
      return dateString.replace('T', ' ').substring(0, 19)
    }
  }

  // 预热库存
  const handlePreload = (activity: SeckillActivityItem) => {
    ElMessageBox.confirm(`确定要预热活动 "${activity.name}" 的库存吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
      .then(async () => {
        try {
          await preloadSeckillStockApi(activity.id)
          ElMessage.success('库存预热成功')
          fetchActivityList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '库存预热失败'
          ElMessage.error(errorMsg)
        }
      })
      .catch(() => {
        // 用户取消操作
      })
  }

  // 结束活动
  const handleEnd = (activity: SeckillActivityItem) => {
    ElMessageBox.confirm(`确定要结束活动 "${activity.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
      .then(async () => {
        try {
          await endSeckillActivityApi(activity.id)
          ElMessage.success('活动已结束')
          fetchActivityList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '结束活动失败'
          ElMessage.error(errorMsg)
        }
      })
      .catch(() => {
        // 用户取消操作
      })
  }

  // 删除活动
  const handleDelete = (activity: SeckillActivityItem) => {
    ElMessageBox.confirm(`确定要删除活动 "${activity.name}" 吗？此操作不可恢复！`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
      .then(async () => {
        try {
          await deleteSeckillActivityApi(activity.id)
          ElMessage.success('活动删除成功')
          fetchActivityList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '删除活动失败'
          ElMessage.error(errorMsg)
        }
      })
      .catch(() => {
        // 用户取消操作
      })
  }

  // SKU选择变化
  const handleSkuSelectionChange = (selection: SkuDTO[]) => {
    console.log('SKU选择变化:', selection)

    formData.skuIds = selection.map((sku) => sku.skuId)

    // 更新已配置的SKU
    const newSelectedSkus = selection.map((sku) => ({
      skuId: sku.skuId,
      spuId: sku.spuId,
      skuName: sku.name,
      attrs: sku.saleAttrs || [],
      price: sku.price,
      stock: sku.stock,
      seckillPrice: Math.max(0.01, sku.price * 0.8), // 默认秒杀价为原价8折
      totalStock: sku.stock,
    }))

    // 保留已配置SKU的设置
    newSelectedSkus.forEach((newSku) => {
      const existing = formData.selectedSkus.find((s) => s.skuId === newSku.skuId)
      if (existing) {
        newSku.seckillPrice = existing.seckillPrice
        newSku.stock = existing.stock
      }
    })

    // 移除已取消选择的SKU
    formData.selectedSkus = formData.selectedSkus.filter((sku) =>
      formData.skuIds.includes(sku.skuId)
    )

    // 添加新选择的SKU
    newSelectedSkus.forEach((newSku) => {
      if (!formData.selectedSkus.some((s) => s.skuId === newSku.skuId)) {
        formData.selectedSkus.push(newSku)
      }
    })

    console.log('已选择的SKU:', formData.selectedSkus)
  }

  // 验证秒杀价格
  const validateSeckillPrice = (sku: any) => {
    if (sku.seckillPrice >= sku.price) {
      ElMessage.warning('秒杀价格必须低于原价')
      sku.seckillPrice = Math.max(0.01, sku.price * 0.8)
    }

    if (sku.seckillPrice < sku.price * 0.5) {
      ElMessage.warning('秒杀价格不能低于原价的50%')
      sku.seckillPrice = sku.price * 0.5
    }
  }

  // 移除SKU
  const removeSku = (index: number) => {
    const skuToRemove = formData.selectedSkus[index]
    formData.skuIds = formData.skuIds.filter((id) => id !== skuToRemove.skuId)
    formData.selectedSkus.splice(index, 1)
  }

  // 重置表单
  const resetForm = () => {
    formData.name = ''
    formData.timeRange = []
    formData.spuId = null
    formData.skuIds = []
    formData.selectedSkus = []
    spuOptions.value = []
    skuList.value = []
    spuLoading.value = false

    if (formRef.value) {
      formRef.value.resetFields()
    }
  }

  // 提交表单
  const submitForm = async () => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      if (!formData.timeRange || formData.timeRange.length !== 2) {
        ElMessage.error('请选择完整的活动时间')
        return
      }

      if (formData.selectedSkus.length === 0) {
        ElMessage.error('请至少选择一个商品SKU')
        return
      }

      submitLoading.value = true

      if (editingActivity.value) {
        // 更新活动
        const updateRequest: SeckillActivityUpdateRequest = {
          id: editingActivity.value.id,
          name: formData.name,
          startTime: formData.timeRange[0],
          endTime: formData.timeRange[1],
          skus: formData.selectedSkus.map((sku) => ({
            spuId: sku.spuId,
            skuId: sku.skuId,
            seckillPrice: sku.seckillPrice,
            stock: sku.stock,
          })),
        }

        console.log('更新请求:', updateRequest)
        const result = await updateSeckillActivityApi(editingActivity.value.id, updateRequest)

        if (result) {
          ElMessage.success('活动更新成功')
          dialogVisible.value = false
          fetchActivityList()
        } else {
          ElMessage.error(result.msg || '更新活动失败')
        }
      } else {
        // 创建活动
        const createRequest: SeckillActivityCreateRequest = {
          name: formData.name,
          startTime: formData.timeRange[0],
          endTime: formData.timeRange[1],
          skus: formData.selectedSkus.map((sku) => ({
            spuId: sku.spuId,
            skuId: sku.skuId,
            seckillPrice: sku.seckillPrice,
            stock: sku.stock,
          })),
        }

        const result = await createSeckillActivityApi(createRequest)

        if (result) {
          ElMessage.success('活动创建成功')
          dialogVisible.value = false
          fetchActivityList()
        } else {
          ElMessage.error(result.msg || '创建活动失败')
        }
      }
    } catch (error: any) {
      console.error('提交失败:', error)
      const errorMsg =
        error?.response?.data?.msg || (editingActivity.value ? '更新活动失败' : '创建活动失败')
      ElMessage.error(errorMsg)
    } finally {
      submitLoading.value = false
    }
  }

  onMounted(() => {
    fetchActivityList()
  })

  onUnmounted(() => {
    if (spuSearchTimer.value) {
      clearTimeout(spuSearchTimer.value)
    }
  })
</script>

<style scoped>
  .seckill-item-image {
    width: 40px;
    height: 40px;
    object-fit: contain;
    border-radius: 4px;
    border: 1px solid #eee;
  }

  .no-image {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 18px;
    border: 1px solid #eee;
    border-radius: 4px;
  }

  .spu-option {
    display: flex;
    align-items: center;
    padding: 8px;
  }

  .spu-image {
    width: 30px;
    height: 30px;
    object-fit: contain;
    border-radius: 4px;
    margin-right: 8px;
    border: 1px solid #eee;
  }

  .spu-name {
    flex: 1;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .sku-image {
    width: 40px;
    height: 40px;
    object-fit: contain;
    border-radius: 4px;
    border: 1px solid #eee;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    margin-top: 20px;
  }

  .no-data {
    text-align: center;
    padding: 20px;
    color: #999;
  }

  .activity-detail {
    padding: 20px;
  }

  .detail-section {
    margin-bottom: 24px;
  }

  .detail-section h3 {
    margin-bottom: 16px;
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }

  .detail-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
  }

  .detail-item {
    display: flex;
    flex-direction: column;
  }

  .label {
    font-weight: 500;
    color: #666;
    margin-bottom: 4px;
  }

  .value {
    color: #333;
  }

  .detail-image {
    width: 40px;
    height: 40px;
    object-fit: contain;
    border-radius: 4px;
    border: 1px solid #eee;
  }
</style>
