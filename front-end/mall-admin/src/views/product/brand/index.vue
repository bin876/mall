<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增品牌</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="品牌名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入品牌名称"
          clearable
          @keyup.enter="() => fetchBrandList()"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchBrandList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="brandList" border style="width: 100%">
      <el-table-column label="品牌Logo" width="100">
        <template #default="scope">
          <img
            v-if="scope.row.logo"
            :src="scope.row.logo"
            alt="品牌Logo"
            class="brand-logo"
            @error="handleImageError(scope.row)"
          />
          <span v-else class="no-logo">-</span>
        </template>
      </el-table-column>

      <el-table-column prop="name" label="品牌名称" min-width="150" />
      <el-table-column prop="firstLetter" label="首字母" width="80" />

      <el-table-column label="关联分类" min-width="200">
        <template #default="scope">
          <div v-if="scope.row.categoryIds && scope.row.categoryIds.length > 0">
            <el-tag
              v-for="categoryId in scope.row.categoryIds"
              :key="categoryId"
              size="small"
              class="mr-1 mb-1"
            >
              {{ getCategoryName(categoryId) }}
            </el-tag>
          </div>
          <span v-else class="text-gray-400">未关联分类</span>
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
      v-model="createDialogVisible"
      title="新增品牌"
      width="500px"
      @close="handleCloseCreate"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入品牌名称" />
        </el-form-item>

        <el-form-item label="品牌Logo" prop="logo">
          <el-upload
            class="logo-uploader"
            :show-file-list="false"
            :on-success="handleLogoSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadBrandLogo"
          >
            <img v-if="createForm.logo" :src="createForm.logo" class="logo-preview" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传Logo</div>
            </div>
          </el-upload>
          <div class="text-gray-500 text-sm mt-1">建议尺寸：200x200，仅支持 JPG/PNG/WebP</div>
        </el-form-item>

        <el-form-item label="关联分类" prop="categoryIds">
          <el-select
            v-model="createForm.categoryIds"
            multiple
            filterable
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categoryOptions"
              :key="category.categoryId"
              :label="getCategoryPath(category.categoryId)"
              :value="category.categoryId"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseCreate">取消</el-button>
          <el-button type="primary" @click="handleCreateSubmit" :loading="createLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑品牌" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="品牌名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入品牌名称" />
        </el-form-item>

        <el-form-item label="品牌Logo" prop="logo">
          <el-upload
            class="logo-uploader"
            :show-file-list="false"
            :on-success="handleEditLogoSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadBrandLogoEdit"
          >
            <img v-if="editForm.logo" :src="editForm.logo" class="logo-preview" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传Logo</div>
            </div>
          </el-upload>
          <div class="text-gray-500 text-sm mt-1">建议尺寸：200x200，仅支持 JPG/PNG/WebP</div>
        </el-form-item>

        <el-form-item label="关联分类" prop="categoryIds">
          <el-select
            v-model="editForm.categoryIds"
            multiple
            filterable
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categoryOptions"
              :key="category.categoryId"
              :label="getCategoryPath(category.categoryId)"
              :value="category.categoryId"
            />
          </el-select>
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
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, UploadRequestOptions, UploadRawFile } from 'element-plus'
  import { Upload } from '@element-plus/icons-vue'
  import {
    brandListApi,
    createBrandApi,
    updateBrandApi,
    deleteBrandApi,
    uploadBrandLogoApi,
  } from '@/api/brand'
  import { getCategoryTreeApi } from '@/api/category'
  import type {
    BrandQueryParams,
    BrandItem,
    BrandCreateDto,
    BrandUpdateDto,
  } from '@/api/brand/type'
  import type { CategoryTree } from '@/api/category/type'

  const route = useRoute()

  const queryParams = reactive<BrandQueryParams>({
    name: '',
    pageNum: 1,
    pageSize: 10,
  })

  const brandList = ref<BrandItem[]>([])
  const categoryOptions = ref<CategoryTree[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<BrandCreateDto>({
    name: '',
    logo: '',
    categoryIds: [],
  })

  const editForm = reactive<BrandUpdateDto>({
    id: 0,
    name: '',
    logo: '',
    categoryIds: [],
  })

  const createRules = reactive({
    name: [
      { required: true, message: '请输入品牌名称', trigger: 'blur' },
      { min: 2, max: 64, message: '品牌名称长度在2到64个字符', trigger: 'blur' },
    ],
    logo: [{ required: true, message: '请上传品牌Logo', trigger: 'change' }],
    categoryIds: [{ required: true, message: '请选择至少一个分类', trigger: 'change' }],
  })

  const editRules = reactive({
    name: [
      { required: true, message: '请输入品牌名称', trigger: 'blur' },
      { min: 2, max: 64, message: '品牌名称长度在2到64个字符', trigger: 'blur' },
    ],
    categoryIds: [{ required: true, message: '请选择至少一个分类', trigger: 'change' }],
  })

  const fetchBrandList = async (page: number = 1, size: number = 10) => {
    try {
      const params: BrandQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await brandListApi(params)
      brandList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取品牌列表失败'
      ElMessage.error(errorMsg)
      console.error('获取品牌列表失败:', error)
    }
  }

  const fetchCategoryTree = async () => {
    try {
      categoryOptions.value = await getCategoryTreeApi()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取分类树失败'
      ElMessage.error(errorMsg)
      console.error('获取分类树失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      name: '',
      pageNum: 1,
      pageSize: 10,
    })
    fetchBrandList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchBrandList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchBrandList(page, pageSize.value)
  }

  const flatCategoryOptions = computed(() => {
    const flat: CategoryTree[] = []

    const flatten = (categories: CategoryTree[]) => {
      categories.forEach((category) => {
        flat.push(category)
        if (category.children && category.children.length > 0) {
          flatten(category.children)
        }
      })
    }

    flatten(categoryOptions.value)
    return flat
  })

  const getCategoryPath = (categoryId: number): string => {
    const findPath = (
      categories: CategoryTree[],
      targetId: number,
      path: string[] = []
    ): string[] => {
      for (const category of categories) {
        const currentPath = [...path, category.name]
        if (category.categoryId === targetId) {
          return currentPath
        }
        if (category.children && category.children.length > 0) {
          const result = findPath(category.children, targetId, currentPath)
          if (result.length > 0) {
            return result
          }
        }
      }
      return []
    }

    const path = findPath(categoryOptions.value, categoryId)
    return path.join(' / ') || `分类${categoryId}`
  }

  const getCategoryName = (categoryId: number): string => {
    const flat = flatCategoryOptions.value
    const category = flat.find((c) => c.categoryId === categoryId)
    return category ? category.name : `分类${categoryId}`
  }

  const handleCreate = async () => {
    createDialogVisible.value = true
    await fetchCategoryTree()
  }

  const handleCloseCreate = () => {
    createDialogVisible.value = false
    createFormRef.value?.resetFields()
  }

  const handleCreateSubmit = () => {
    createFormRef.value?.validate(async (valid) => {
      if (valid) {
        createLoading.value = true
        try {
          await createBrandApi(createForm)
          ElMessage.success('品牌创建成功')
          createDialogVisible.value = false
          fetchBrandList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建品牌失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = async (row: BrandItem) => {
    editForm.id = row.id
    editForm.name = row.name
    editForm.logo = row.logo
    editForm.categoryIds = row.categoryIds || []

    await fetchCategoryTree()
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
          await updateBrandApi(editForm)
          ElMessage.success('品牌更新成功')
          editDialogVisible.value = false
          fetchBrandList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新品牌失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: BrandItem) => {
    ElMessageBox.confirm(`确定要删除品牌 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteBrandApi(row.id)
        ElMessage.success('品牌删除成功')
        fetchBrandList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除品牌失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const handleImageError = (row: BrandItem) => {
    row.logo = ''
  }

  const uploadBrandLogo = async (options: UploadRequestOptions) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadBrandLogoApi(formData)
      createForm.logo = url
      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? 'Logo上传失败'
      ElMessage.error(errorMsg)
      options.onSuccess?.({})
    }
  }

  const uploadBrandLogoEdit = async (options: UploadRequestOptions) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadBrandLogoApi(formData)
      editForm.logo = url
      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? 'Logo上传失败'
      ElMessage.error(errorMsg)
      options.onSuccess?.({})
    }
  }

  const handleLogoSuccess = () => {}

  const handleEditLogoSuccess = () => {}

  const beforeUpload = (file: UploadRawFile) => {
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    const maxSize = 5 * 1024 * 1024 // 5MB

    if (!allowedTypes.includes(file.type)) {
      ElMessage.error('只支持 JPG/PNG/GIF/WebP 格式的图片')
      return false
    }

    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过 5MB')
      return false
    }

    return true
  }

  onMounted(() => {
    fetchBrandList()
    fetchCategoryTree()
  })
</script>

<style scoped>
  .brand-logo {
    width: 40px;
    height: 40px;
    object-fit: contain;
    border-radius: 4px;
  }

  .no-logo {
    color: #999;
    font-size: 14px;
  }

  .logo-uploader {
    display: inline-block;
    cursor: pointer;
  }

  .logo-preview {
    width: 80px;
    height: 80px;
    display: block;
    border-radius: 4px;
    object-fit: contain;
    border: 1px solid #dcdfe6;
    padding: 5px;
    background: #fff;
  }

  .upload-placeholder {
    width: 80px;
    height: 80px;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #fafafa;
    cursor: pointer;
    transition: border-color 0.3s;
  }

  .upload-placeholder:hover {
    border-color: #409eff;
  }

  .upload-icon {
    font-size: 28px;
    color: #999;
    margin-bottom: 8px;
  }

  .upload-text {
    color: #666;
    font-size: 12px;
  }

  .mr-1 {
    margin-right: 0.25rem;
  }

  .mb-1 {
    margin-bottom: 0.25rem;
  }
</style>
