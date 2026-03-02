<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <div class="flex items-center gap-3">
          <el-button
            type="success"
            @click="handleBatchEnable"
            :disabled="selectedRows.length === 0"
          >
            批量启用
          </el-button>
          <el-button
            type="warning"
            @click="handleBatchDisable"
            :disabled="selectedRows.length === 0"
          >
            批量禁用
          </el-button>
          <el-button type="primary" @click="handleCreate">新增分类</el-button>
        </div>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="分类名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入分类名称"
          clearable
          @keyup.enter="() => fetchCategoryList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchCategoryList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchCategoryList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      :data="categoryList"
      border
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />

      <el-table-column prop="name" label="分类名称" min-width="200">
        <template #default="scope">
          <span
            :style="{
              paddingLeft: scope.row.level * 20 + 'px',
              fontWeight: scope.row.level === 0 ? 'bold' : 'normal',
            }"
          >
            {{ scope.row.name }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="parentName" label="父分类" min-width="150" />

      <el-table-column prop="level" label="层级" width="100">
        <template #default="scope">
          <el-tag
            :type="scope.row.level === 0 ? 'success' : scope.row.level === 1 ? 'warning' : 'info'"
          >
            {{ scope.row.level === 0 ? '一级' : scope.row.level === 1 ? '二级' : '三级' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="图标" width="100">
        <template #default="scope">
          <img
            v-if="scope.row.icon"
            :src="scope.row.icon"
            alt="图标"
            class="category-icon"
            @error="handleImageError(scope.row)"
          />
          <span v-else class="no-icon">-</span>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="创建时间" width="180" />

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
      title="新增分类"
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
        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="createForm.parentId"
            :data="categoryTreeOptions"
            :props="{ label: 'name', value: 'categoryId', children: 'children' }"
            placeholder="请选择父分类"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="createForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :on-success="handleIconSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadCategoryIcon"
          >
            <img v-if="createForm.icon" :src="createForm.icon" class="avatar" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传图标</div>
            </div>
          </el-upload>
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

    <el-dialog v-model="editDialogVisible" title="编辑分类" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="父分类" prop="parentId">
          <el-tree-select
            v-model="editForm.parentId"
            :data="categoryTreeOptions"
            :props="{ label: 'name', value: 'categoryId', children: 'children' }"
            placeholder="请选择父分类"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="分类名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入分类名称" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="图标" prop="icon">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :on-success="handleEditIconSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadCategoryIconEdit"
          >
            <img v-if="editForm.icon" :src="editForm.icon" class="avatar" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传图标</div>
            </div>
          </el-upload>
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
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, UploadRequestOptions, UploadRawFile } from 'element-plus'
  import { Upload } from '@element-plus/icons-vue'
  import {
    categoryListApi,
    createCategoryApi,
    updateCategoryApi,
    deleteCategoryApi,
    batchUpdateCategoryStatusApi,
    uploadCategoryIconApi,
    getCategoryTreeApi,
  } from '@/api/category'
  import type {
    CategoryQueryParams,
    CategoryItem,
    CategoryCreateDto,
    CategoryUpdateDto,
    CategoryTree,
  } from '@/api/category/type'

  const route = useRoute()

  const queryParams = reactive<CategoryQueryParams>({
    name: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const categoryList = ref<CategoryItem[]>([])
  const categoryTreeOptions = ref<CategoryTree[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const selectedRows = ref<CategoryItem[]>([])

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<CategoryCreateDto>({
    parentId: 0,
    name: '',
    icon: '',
    status: 1,
  })

  const editForm = reactive<CategoryUpdateDto>({
    categoryId: 0,
    parentId: 0,
    name: '',
    icon: '',
    status: 1,
  })

  const createRules = reactive({
    name: [
      { required: true, message: '请输入分类名称', trigger: 'blur' },
      { min: 2, max: 64, message: '分类名称长度在2到64个字符', trigger: 'blur' },
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  })

  const editRules = reactive({
    name: [
      { required: true, message: '请输入分类名称', trigger: 'blur' },
      { min: 2, max: 64, message: '分类名称长度在2到64个字符', trigger: 'blur' },
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  })

  const fetchCategoryList = async (page: number = 1, size: number = 10) => {
    try {
      const params: CategoryQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await categoryListApi(params)
      categoryList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取分类列表失败'
      ElMessage.error(errorMsg)
      console.error('获取分类列表失败:', error)
    }
  }

  const fetchCategoryTree = async () => {
    try {
      categoryTreeOptions.value = await getCategoryTreeApi()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取分类树失败'
      ElMessage.error(errorMsg)
      console.error('获取分类树失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      name: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchCategoryList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchCategoryList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchCategoryList(page, pageSize.value)
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
          await createCategoryApi(createForm)
          ElMessage.success('分类创建成功')
          createDialogVisible.value = false
          fetchCategoryList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建分类失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = async (row: CategoryItem) => {
    editForm.categoryId = row.categoryId
    editForm.parentId = row.parentId
    editForm.name = row.name
    editForm.icon = row.icon
    editForm.status = row.status

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
          await updateCategoryApi(editForm)
          ElMessage.success('分类更新成功')
          editDialogVisible.value = false
          fetchCategoryList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新分类失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: CategoryItem) => {
    ElMessageBox.confirm(`确定要删除分类 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteCategoryApi(row.categoryId)
        ElMessage.success('分类删除成功')
        fetchCategoryList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除分类失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  const handleSelectionChange = (rows: CategoryItem[]) => {
    selectedRows.value = rows
  }

  const handleBatchEnable = async () => {
    await batchUpdateStatus(1)
  }

  const handleBatchDisable = async () => {
    await batchUpdateStatus(0)
  }

  const batchUpdateStatus = async (status: number) => {
    const ids = selectedRows.value.map((row) => row.categoryId)
    if (ids.length === 0) return

    try {
      await batchUpdateCategoryStatusApi({ ids, status })
      ElMessage.success('批量操作成功')
      fetchCategoryList()
      selectedRows.value = []
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '批量操作失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleImageError = (row: CategoryItem) => {
    row.icon = ''
  }

  const uploadCategoryIcon = async (options: UploadRequestOptions) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadCategoryIconApi(formData)
      createForm.icon = url

      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '图标上传失败'
      ElMessage.error(errorMsg)
      options.onError?.(error)
    }
  }

  const uploadCategoryIconEdit = async (options: UploadRequestOptions) => {
    const file = options.file as UploadRawFile
    const formData = new FormData()
    formData.append('file', file)

    try {
      const url = await uploadCategoryIconApi(formData)
      editForm.icon = url
      options.onSuccess?.({})
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '图标上传失败'
      ElMessage.error(errorMsg)
      options.onSuccess?.({})
    }
  }

  const handleIconSuccess = () => {}

  const handleEditIconSuccess = () => {}

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
    fetchCategoryList()
    fetchCategoryTree()
  })
</script>

<style scoped>
  .category-icon {
    width: 30px;
    height: 30px;
    object-fit: contain;
    border-radius: 4px;
  }

  .no-icon {
    color: #999;
    font-size: 14px;
  }

  .avatar-uploader {
    display: inline-block;
    cursor: pointer;
  }

  .avatar {
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
</style>
