<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">发布商品</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="商品名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入商品名称"
          clearable
          @keyup.enter="() => fetchSpuList()"
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

      <el-form-item label="状态">
        <el-select v-model="queryParams.publishStatus" placeholder="全部" clearable>
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchSpuList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="spuList" border style="width: 100%">
      <el-table-column type="selection" width="50" />

      <el-table-column label="商品信息" min-width="200">
        <template #default="scope">
          <div class="flex items-center">
            <img
              v-if="scope.row.defaultImg"
              :src="scope.row.defaultImg"
              alt="商品图片"
              class="spu-image"
              @error="handleImageError(scope.row)"
            />
            <span v-else class="no-image">-</span>
            <div class="ml-3">
              <div class="font-medium">{{ scope.row.name }}</div>
              <div class="text-gray-500 text-sm line-clamp-2">{{ scope.row.description }}</div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="categoryName" label="分类" min-width="120" />
      <el-table-column prop="brandName" label="品牌" min-width="120" />
      <el-table-column prop="saleCount" label="销量" width="80" />
      <el-table-column prop="publishStatus" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.publishStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.publishStatus === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="info" link @click="handleManageSku(scope.row)">
            管理SKU
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

    <el-dialog v-model="editDialogVisible" title="编辑商品" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="editForm.description"
            placeholder="请输入商品描述"
            type="textarea"
            :rows="3"
          />
        </el-form-item>

        <el-form-item label="分类" prop="categoryId">
          <el-cascader
            v-model="editForm.categoryId"
            :options="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            placeholder="请选择分类"
            style="width: 100%"
            disabled
          />
        </el-form-item>

        <el-form-item label="品牌" prop="brandId">
          <el-select
            v-model="editForm.brandId"
            placeholder="请选择品牌"
            style="width: 100%"
            disabled
          >
            <el-option
              v-for="brand in brandOptions"
              :key="brand.id"
              :label="brand.name"
              :value="brand.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="商品状态" prop="publishStatus">
          <el-radio-group v-model="editForm.publishStatus">
            <el-radio :value="0">下架</el-radio>
            <el-radio :value="1">上架</el-radio>
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
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import type { SpuQueryParams, SpuItem } from '@/api/spu/type'
  import {
    spuListApi,
    updateSpuStatusApi,
    deleteSpuApi,
    getSpuDetailApi,
    updateSpuApi,
  } from '@/api/spu'
  import { getCategoryTreeApi } from '@/api/category'
  import { brandListApi } from '@/api/brand'
  import type { CategoryTree } from '@/api/category/type'
  import type { BrandItem } from '@/api/brand/type'

  const route = useRoute()
  const router = useRouter()

  const queryParams = reactive<SpuQueryParams>({
    name: '',
    categoryId: null,
    brandId: null,
    publishStatus: null,
    pageNum: 1,
    pageSize: 10,
  })

  const categoryOptions = ref<CategoryTree[]>([])
  const brandOptions = ref<BrandItem[]>([])

  const spuList = ref<SpuItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const editDialogVisible = ref(false)
  const editLoading = ref(false)
  const editFormRef = ref<FormInstance>()

  const editForm = reactive({
    spuId: 0,
    name: '',
    description: '',
    categoryId: null as number | null,
    brandId: null as number | null,
    publishStatus: 1,
  })

  const editRules = reactive<FormRules>({
    name: [
      { required: true, message: '请输入商品名称', trigger: 'blur' },
      { min: 2, max: 100, message: '商品名称长度在2-100个字符', trigger: 'blur' },
    ],
    description: [
      { required: true, message: '请输入商品描述', trigger: 'blur' },
      { max: 500, message: '商品描述不能超过500个字符', trigger: 'blur' },
    ],
    publishStatus: [{ required: true, message: '请选择商品状态', trigger: 'change' }],
  })

  const fetchSpuList = async (page: number = 1, size: number = 10) => {
    try {
      const params: SpuQueryParams = {
        ...queryParams,
        categoryId: Array.isArray(queryParams.categoryId)
          ? queryParams.categoryId[queryParams.categoryId.length - 1]
          : queryParams.categoryId,
        pageNum: page,
        pageSize: size,
      }

      const res = await spuListApi(params)
      spuList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取商品列表失败'
      ElMessage.error(errorMsg)
      console.error('获取商品列表失败:', error)
    }
  }

  const fetchCategories = async () => {
    try {
      categoryOptions.value = await getCategoryTreeApi()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取分类失败'
      ElMessage.error(errorMsg)
    }
  }

  const fetchBrands = async () => {
    try {
      const brandParams = { name: '', pageNum: 1, pageSize: 1000 }
      const res = await brandListApi(brandParams)
      brandOptions.value = res.records || []
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取品牌失败'
      ElMessage.error(errorMsg)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      name: '',
      categoryId: null,
      brandId: null,
      publishStatus: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchSpuList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchSpuList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchSpuList(page, pageSize.value)
  }

  const handleStatusChange = async (row: SpuItem) => {
    try {
      await updateSpuStatusApi(row.spuId, row.publishStatus)
      ElMessage.success('状态更新成功')
    } catch (error: any) {
      row.publishStatus = row.publishStatus === 1 ? 0 : 1
      const errorMsg = error?.response?.data?.msg ?? '更新状态失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleManageSku = (row: SpuItem) => {
    router.push(`/product/sku?spuId=${row.spuId}`)
  }

  const handleEdit = async (row: SpuItem) => {
    try {
      const detail = await getSpuDetailApi(row.spuId)
      editForm.spuId = row.spuId
      editForm.name = row.name
      editForm.description = row.description
      editForm.categoryId = row.categoryId
      editForm.brandId = row.brandId
      editForm.publishStatus = row.publishStatus
      editDialogVisible.value = true
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取商品详情失败'
      ElMessage.error(errorMsg)
    }
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
          await updateSpuApi(editForm)
          ElMessage.success('商品更新成功')
          editDialogVisible.value = false
          fetchSpuList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新商品失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: SpuItem) => {
    ElMessageBox.confirm(`确定要删除商品 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteSpuApi(row.spuId)
        ElMessage.success('商品删除成功')
        fetchSpuList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除商品失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const handleCreate = () => {
    router.push('/product/publish')
  }

  const handleImageError = (row: SpuItem) => {
    row.defaultImg = ''
  }

  onMounted(() => {
    fetchCategories()
    fetchBrands()
    fetchSpuList()
  })
</script>

<style scoped>
  .spu-image {
    width: 48px;
    height: 48px;
    object-fit: contain;
    border-radius: 4px;
  }

  .no-image {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 24px;
  }

  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .ml-3 {
    margin-left: 12px;
  }
</style>
