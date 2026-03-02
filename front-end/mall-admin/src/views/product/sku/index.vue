<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <div>
          <span class="text-lg">{{ route.meta.title }}</span>
          <el-tag v-if="spuName" class="ml-2">{{ spuName }}</el-tag>
        </div>
        <div>
          <el-button @click="handleBack">返回商品列表</el-button>
        </div>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <template v-if="hasSpuId">
        <el-form-item label="SKU名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入SKU名称"
            clearable
            @keyup.enter="() => fetchSkuList()"
          />
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="SPU名称">
          <el-input
            v-model="queryParams.spuName"
            placeholder="请输入SPU名称"
            clearable
            @keyup.enter="() => fetchSkuList()"
          />
        </el-form-item>
        <el-form-item label="SKU名称">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入SKU名称"
            clearable
            @keyup.enter="() => fetchSkuList()"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="queryParams.categoryId"
            :options="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            placeholder="请选择分类"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="品牌">
          <el-select
            v-model="queryParams.brandId"
            placeholder="请选择品牌"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="brand in brandOptions"
              :key="brand.id"
              :label="brand.name"
              :value="brand.id"
            />
          </el-select>
        </el-form-item>
      </template>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchSkuList()"
        >
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchSkuList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="mb-4 flex items-center" v-if="hasSpuId">
      <el-button type="primary" @click="handleBatchPrice" :disabled="selectedSkuIds.length === 0">
        批量调价
      </el-button>
      <el-button type="success" @click="handleBatchStock" :disabled="selectedSkuIds.length === 0">
        批量库存
      </el-button>
      <el-button
        type="warning"
        @click="handleBatchStatus(1)"
        :disabled="selectedSkuIds.length === 0"
      >
        批量上架
      </el-button>
      <el-button
        type="danger"
        @click="handleBatchStatus(0)"
        :disabled="selectedSkuIds.length === 0"
      >
        批量下架
      </el-button>
    </div>

    <el-table :data="skuList" border style="width: 100%" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" v-if="hasSpuId" />

      <el-table-column label="SPU名称" prop="spuName" min-width="120" />

      <el-table-column label="规格组合" min-width="180">
        <template #default="scope">
          <div class="flex flex-wrap gap-1">
            <el-tag
              v-for="attr in scope.row.saleAttrs"
              :key="attr.attrId"
              size="small"
              class="mb-1"
            >
              {{ attr.attrValue }}
            </el-tag>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="SKU名称" prop="name" min-width="150" />

      <el-table-column label="标题" prop="title" min-width="150" v-if="!hasSpuId" />
      <el-table-column label="副标题" prop="subtitle" min-width="150" v-if="!hasSpuId" />

      <el-table-column label="价格（元）" width="120">
        <template #default="scope">
          <span>{{ formatPrice(scope.row.price) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="库存" width="100">
        <template #default="scope">
          <span :class="{ 'text-red-500 font-bold': scope.row.stock < 10 }">
            {{ scope.row.stock }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
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

    <el-dialog
      v-model="batchPriceDialogVisible"
      title="批量调价"
      width="500px"
      @close="handleCloseBatchPrice"
    >
      <el-form
        ref="batchPriceFormRef"
        :model="batchPriceForm"
        :rules="batchPriceRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="调整方式" prop="type">
          <el-radio-group v-model="batchPriceForm.type">
            <el-radio label="set">直接设置</el-radio>
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="价格" prop="value">
          <el-input-number
            v-model="batchPriceForm.value"
            :min="0.01"
            :precision="2"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseBatchPrice">取消</el-button>
          <el-button type="primary" @click="handleBatchPriceSubmit" :loading="batchPriceLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="batchStockDialogVisible"
      title="批量库存"
      width="500px"
      @close="handleCloseBatchStock"
    >
      <el-form
        ref="batchStockFormRef"
        :model="batchStockForm"
        :rules="batchStockRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="调整方式" prop="type">
          <el-radio-group v-model="batchStockForm.type">
            <el-radio label="set">直接设置</el-radio>
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="库存" prop="value">
          <el-input-number
            v-model="batchStockForm.value"
            :min="0"
            :step="10"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseBatchStock">取消</el-button>
          <el-button type="primary" @click="handleBatchStockSubmit" :loading="batchStockLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑SKU" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="SKU名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入SKU名称" />
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="副标题" prop="subtitle">
          <el-input v-model="editForm.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="价格（元）" prop="price">
          <el-input-number
            v-model="editForm.price"
            :min="0.01"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number
            v-model="editForm.stock"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEdit">取消</el-button>
          <el-button type="primary" @click="handleEditSubmit" :loading="editLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { SkuQueryParams, SkuItem, UpdateSkuDto, BatchUpdateSkuDto } from '@/api/sku/type'
  import { skuListApi, updateSkuApi, batchUpdateSkuApi, deleteSkuApi } from '@/api/sku'
  import { getCategoryTreeApi } from '@/api/category'
  import { brandListApi } from '@/api/brand'
  import type { CategoryTree } from '@/api/category/type'
  import type { BrandItem } from '@/api/brand/type'

  const route = useRoute()
  const router = useRouter()

  const hasSpuId = computed(() => {
    const spuId = route.query.spuId
    return spuId && !isNaN(Number(spuId))
  })

  const spuId = hasSpuId.value ? Number(route.query.spuId) : null

  const spuName = ref<string>('')

  const categoryOptions = ref<CategoryTree[]>([])
  const brandOptions = ref<BrandItem[]>([])

  const queryParams = reactive<SkuQueryParams>({
    spuId: spuId,
    keyword: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const skuList = ref<SkuItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const selectedSkuIds = ref<number[]>([])

  const batchPriceDialogVisible = ref(false)
  const batchPriceLoading = ref(false)
  const batchPriceFormRef = ref<FormInstance>()
  const batchPriceForm = reactive({
    type: 'set' as 'set' | 'add' | 'reduce',
    value: 0.01,
  })

  const validatePrice = (rule: any, value: number, callback: any) => {
    if (value === undefined || value === null) {
      callback(new Error('请输入价格'))
    } else if (value < 0.01) {
      callback(new Error('价格不能小于0.01'))
    } else {
      callback()
    }
  }

  const batchPriceRules = reactive<FormRules>({
    type: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
    value: [
      { required: true, message: '请输入价格', trigger: 'blur' },
      { validator: validatePrice, trigger: 'blur' },
    ],
  })

  const batchStockDialogVisible = ref(false)
  const batchStockLoading = ref(false)
  const batchStockFormRef = ref<FormInstance>()
  const batchStockForm = reactive({
    type: 'set' as 'set' | 'add' | 'reduce',
    value: 0,
  })
  const batchStockRules = reactive<FormRules>({
    type: [{ required: true, message: '请选择调整方式', trigger: 'change' }],
    value: [
      { required: true, message: '请输入库存', trigger: 'blur' },
      { min: 0, message: '库存不能小于0', trigger: 'blur' },
    ],
  })

  const editDialogVisible = ref(false)
  const editLoading = ref(false)
  const editFormRef = ref<FormInstance>()
  const editForm = reactive({
    skuId: 0,
    name: '',
    title: '',
    subtitle: '',
    price: 0.01,
    stock: 0,
    status: 1,
  })

  const editRules = reactive<FormRules>({
    name: [
      { required: true, message: '请输入SKU名称', trigger: 'blur' },
      { min: 2, max: 100, message: 'SKU名称长度在2-100个字符', trigger: 'blur' },
    ],
    price: [
      { required: true, message: '请输入价格', trigger: 'blur' },
      { validator: validatePrice, trigger: 'blur' },
    ],
    stock: [
      { required: true, message: '请输入库存', trigger: 'blur' },
      { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' },
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  })

  const fetchCategories = async () => {
    if (!hasSpuId.value) {
      try {
        categoryOptions.value = await getCategoryTreeApi()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '获取分类失败'
        ElMessage.error(errorMsg)
      }
    }
  }

  const fetchBrands = async () => {
    if (!hasSpuId.value) {
      try {
        const brandParams = { name: '', pageNum: 1, pageSize: 1000 }
        const res = await brandListApi(brandParams)
        brandOptions.value = res.records || []
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '获取品牌失败'
        ElMessage.error(errorMsg)
      }
    }
  }

  const fetchSkuList = async (page: number = 1, size: number = 10) => {
    try {
      const params: SkuQueryParams = {
        ...queryParams,
        spuId: spuId,
        pageNum: page,
        pageSize: size,
      }

      const res = await skuListApi(params)
      skuList.value = res.records || []
      total.value = res.total || 0

      if (skuList.value.length > 0 && hasSpuId.value) {
        spuName.value = skuList.value[0].spuName
      }

      console.log(skuList.value)
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取SKU列表失败'
      ElMessage.error(errorMsg)
      console.error('获取SKU列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      spuId: spuId,
      keyword: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })

    if (!hasSpuId.value) {
      ;(queryParams as any).spuName = ''
      ;(queryParams as any).categoryId = null
      ;(queryParams as any).brandId = null
    }

    fetchSkuList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchSkuList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchSkuList(page, pageSize.value)
  }

  const handleSelectionChange = (selection: SkuItem[]) => {
    selectedSkuIds.value = selection.map((sku) => sku.skuId)
  }

  const formatPrice = (price: number): string => {
    return price.toFixed(2)
  }

  const handleBatchPrice = () => {
    batchPriceDialogVisible.value = true
  }

  const handleCloseBatchPrice = () => {
    batchPriceDialogVisible.value = false
    batchPriceFormRef.value?.resetFields()
  }

  const handleBatchPriceSubmit = () => {
    batchPriceFormRef.value?.validate(async (valid) => {
      if (valid && selectedSkuIds.value.length > 0) {
        batchPriceLoading.value = true
        try {
          const batchDto: BatchUpdateSkuDto = {
            skuIds: selectedSkuIds.value,
            price: batchPriceForm.value,
            priceOperation: batchPriceForm.type,
          }
          await batchUpdateSkuApi(batchDto)
          ElMessage.success('批量调价成功')
          batchPriceDialogVisible.value = false
          fetchSkuList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '批量调价失败'
          ElMessage.error(errorMsg)
        } finally {
          batchPriceLoading.value = false
        }
      }
    })
  }

  const handleBatchStock = () => {
    batchStockDialogVisible.value = true
  }

  const handleCloseBatchStock = () => {
    batchStockDialogVisible.value = false
    batchStockFormRef.value?.resetFields()
  }

  const handleBatchStockSubmit = () => {
    batchStockFormRef.value?.validate(async (valid) => {
      if (valid && selectedSkuIds.value.length > 0) {
        batchStockLoading.value = true
        try {
          const batchDto: BatchUpdateSkuDto = {
            skuIds: selectedSkuIds.value,
            stock: batchStockForm.value,
            stockOperation: batchStockForm.type,
          }
          await batchUpdateSkuApi(batchDto)
          ElMessage.success('批量库存更新成功')
          batchStockDialogVisible.value = false
          fetchSkuList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '批量库存失败'
          ElMessage.error(errorMsg)
        } finally {
          batchStockLoading.value = false
        }
      }
    })
  }

  const handleBatchStatus = async (status: number) => {
    if (selectedSkuIds.value.length === 0) return

    try {
      const batchDto: BatchUpdateSkuDto = {
        skuIds: selectedSkuIds.value,
        status: status,
      }
      await batchUpdateSkuApi(batchDto)
      ElMessage.success(`批量${status === 1 ? '上架' : '下架'}成功`)
      fetchSkuList()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '批量状态更新失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleEdit = (sku: SkuItem) => {
    editForm.skuId = sku.skuId
    editForm.name = sku.name
    editForm.title = sku.title
    editForm.subtitle = sku.subtitle
    editForm.price = sku.price
    editForm.stock = sku.stock
    editForm.status = sku.status
    editDialogVisible.value = true
  }

  const handleCloseEdit = () => {
    editDialogVisible.value = false
    editFormRef.value?.resetFields()
  }

  const handleEditSubmit = () => {
    editFormRef.value?.validate(async (valid) => {
      if (valid) {
        editLoading.value = true
        try {
          const updateDto: UpdateSkuDto = {
            name: editForm.name,
            title: editForm.title,
            subtitle: editForm.subtitle,
            price: editForm.price,
            stock: editForm.stock,
            status: editForm.status,
          }
          await updateSkuApi(editForm.skuId, updateDto)
          ElMessage.success('SKU更新成功')
          editDialogVisible.value = false
          fetchSkuList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新SKU失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (sku: SkuItem) => {
    ElMessageBox.confirm(`确定要删除 SKU "${sku.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteSkuApi(sku.skuId)
        ElMessage.success('SKU删除成功')
        fetchSkuList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除SKU失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const handleBack = () => {
    router.push('/product/spu')
  }

  onMounted(() => {
    fetchCategories()
    fetchBrands()
    fetchSkuList()
  })
</script>

<style scoped>
  .mb-1 {
    margin-bottom: 4px;
  }
</style>
