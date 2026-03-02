<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增轮播图</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="fetchBannerList"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="fetchBannerList">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="bannerList" border style="width: 100%">
      <el-table-column label="轮播图" width="180">
        <template #default="scope">
          <img
            v-if="scope.row.imageUrl"
            :src="scope.row.imageUrl"
            alt="轮播图"
            class="banner-image"
            @error="handleImageError(scope.row)"
          />
          <span v-else class="no-image">-</span>
        </template>
      </el-table-column>

      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="targetUrl" label="跳转链接" min-width="200" show-overflow-tooltip />

      <el-table-column label="排序" width="80">
        <template #default="scope">
          <span>{{ scope.row.sort }}</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
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

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新增轮播图"
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
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入标题（可选）" />
        </el-form-item>

        <el-form-item label="轮播图" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :on-success="handleImageSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadBannerImage"
          >
            <img v-if="createForm.imageUrl" :src="createForm.imageUrl" class="image-preview" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
          <div class="text-gray-500 text-sm mt-1">建议尺寸：1920x400，仅支持 JPG/PNG</div>
        </el-form-item>

        <el-form-item label="跳转链接" prop="targetUrl">
          <el-input v-model="createForm.targetUrl" placeholder="请输入跳转链接（如：/product/123）" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="createForm.sort"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="createForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑轮播图"
      width="500px"
      @close="handleCloseEdit"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入标题（可选）" />
        </el-form-item>

        <el-form-item label="轮播图" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :on-success="handleEditImageSuccess"
            :before-upload="beforeUpload"
            :http-request="uploadBannerImageEdit"
          >
            <img v-if="editForm.imageUrl" :src="editForm.imageUrl" class="image-preview" />
            <div v-else class="upload-placeholder">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">点击上传图片</div>
            </div>
          </el-upload>
          <div class="text-gray-500 text-sm mt-1">建议尺寸：1920x400，仅支持 JPG/PNG</div>
        </el-form-item>

        <el-form-item label="跳转链接" prop="targetUrl">
          <el-input v-model="editForm.targetUrl" placeholder="请输入跳转链接（如：/product/123）" />
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="editForm.sort"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, UploadRequestOptions, UploadRawFile } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import {
  getBannerListApi,
  createBannerApi,
  updateBannerApi,
  deleteBannerApi,
  uploadBannerImageApi,
} from '@/api/banner'
import type {
  BannerQueryParams,
  BannerItem,
  BannerCreateDto,
  BannerUpdateDto,
} from '@/api/banner/type'

const route = useRoute()

const queryParams = reactive<BannerQueryParams>({
  status: null,
  pageNum: 1,
  pageSize: 10,
})

const bannerList = ref<BannerItem[]>([])
const total = ref<number>(0)
const currentPage = ref<number>(1)
const pageSize = ref<number>(10)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const createLoading = ref(false)
const editLoading = ref(false)

const createFormRef = ref<FormInstance>()
const editFormRef = ref<FormInstance>()

const createForm = reactive<BannerCreateDto>({
  title: '',
  imageUrl: '',
  targetUrl: '',
  sort: 0,
  status: 1,
})

const editForm = reactive<BannerUpdateDto>({
  bannerId: 0,
  title: '',
  imageUrl: '',
  targetUrl: '',
  sort: 0,
  status: 1,
})

const createRules = reactive({
  imageUrl: [{ required: true, message: '请上传轮播图', trigger: 'change' }],
  targetUrl: [{ required: true, message: '请输入跳转链接', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
})

const editRules = reactive({
  imageUrl: [{ required: true, message: '请上传轮播图', trigger: 'change' }],
  targetUrl: [{ required: true, message: '请输入跳转链接', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
})

const fetchBannerList = async (page: number = 1, size: number = 10) => {
  try {
    const params: BannerQueryParams = {
      ...queryParams,
      pageNum: page,
      pageSize: size,
    }
    const res = await getBannerListApi(params)
    bannerList.value = res.records || []
    total.value = res.total || 0
  } catch (error: any) {
    const errorMsg = error?.response?.data?.msg ?? '获取轮播图列表失败'
    ElMessage.error(errorMsg)
  }
}

const resetQuery = () => {
  queryParams.status = null
  fetchBannerList()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  fetchBannerList(currentPage.value, size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchBannerList(page, pageSize.value)
}

const handleCreate = () => {
  createDialogVisible.value = true
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
        await createBannerApi(createForm)
        ElMessage.success('轮播图创建成功')
        createDialogVisible.value = false
        fetchBannerList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '创建轮播图失败'
        ElMessage.error(errorMsg)
      } finally {
        createLoading.value = false
      }
    }
  })
}

const handleEdit = (row: BannerItem) => {
  editForm.bannerId = row.bannerId
  editForm.title = row.title
  editForm.imageUrl = row.imageUrl
  editForm.targetUrl = row.targetUrl
  editForm.sort = row.sort
  editForm.status = row.status
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
        await updateBannerApi(editForm)
        ElMessage.success('轮播图更新成功')
        editDialogVisible.value = false
        fetchBannerList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '更新轮播图失败'
        ElMessage.error(errorMsg)
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleDelete = (row: BannerItem) => {
  ElMessageBox.confirm(`确定要删除轮播图 "${row.title || '无标题'}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await deleteBannerApi(row.bannerId)
      ElMessage.success('轮播图删除成功')
      fetchBannerList()
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '删除轮播图失败'
      ElMessage.error(errorMsg)
    }
  })
}

const handleImageError = (row: BannerItem) => {
  row.imageUrl = ''
}

// 上传逻辑
const uploadBannerImage = async (options: UploadRequestOptions) => {
  const file = options.file as UploadRawFile
  const formData = new FormData()
  formData.append('file', file)

  try {
    const url = await uploadBannerImageApi(formData)
    createForm.imageUrl = url
    options.onSuccess?.({})
  } catch (error: any) {
    const errorMsg = error?.response?.data?.msg ?? '图片上传失败'
    ElMessage.error(errorMsg)
    options.onSuccess?.({})
  }
}

const uploadBannerImageEdit = async (options: UploadRequestOptions) => {
  const file = options.file as UploadRawFile
  const formData = new FormData()
  formData.append('file', file)

  try {
    const url = await uploadBannerImageApi(formData)
    editForm.imageUrl = url
    options.onSuccess?.({})
  } catch (error: any) {
    const errorMsg = error?.response?.data?.msg ?? '图片上传失败'
    ElMessage.error(errorMsg)
    options.onSuccess?.({})
  }
}

const handleImageSuccess = () => {}
const handleEditImageSuccess = () => {}

const beforeUpload = (file: UploadRawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png']
  const maxSize = 5 * 1024 * 1024 // 5MB

  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 JPG/PNG 格式的图片')
    return false
  }

  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 5MB')
    return false
  }

  return true
}

onMounted(() => {
  fetchBannerList()
})
</script>

<style scoped>
.banner-image {
  width: 160px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.no-image {
  color: #999;
  font-size: 14px;
}

.image-uploader {
  display: inline-block;
  cursor: pointer;
}

.image-preview {
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
