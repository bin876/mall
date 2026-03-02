<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增资源</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="资源名称">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入资源名称"
          clearable
          @keyup.enter="() => fetchResourceList()"
        />
      </el-form-item>
      <el-form-item label="API路径">
        <el-input
          v-model="queryParams.path"
          placeholder="请输入API路径"
          clearable
          @keyup.enter="() => fetchResourceList()"
        />
      </el-form-item>
      <el-form-item label="权限标识">
        <el-input
          v-model="queryParams.permissionCode"
          placeholder="请输入权限标识"
          clearable
          @keyup.enter="() => fetchResourceList()"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchResourceList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="() => fetchResourceList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="resourceList" border style="width: 100%">
      <el-table-column prop="name" label="资源名称" min-width="150" />
      <el-table-column prop="path" label="API路径" min-width="200" />
      <el-table-column prop="method" label="请求方法" width="100">
        <template #default="scope">
          <el-tag :type="getMethodType(scope.row.method)">
            {{ scope.row.method }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="permissionCode" label="权限标识" min-width="150" />
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
      title="新增资源"
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
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入资源名称" />
        </el-form-item>

        <el-form-item label="API路径" prop="path">
          <el-input v-model="createForm.path" placeholder="请输入API路径（支持/**）" />
        </el-form-item>

        <el-form-item label="请求方法" prop="method">
          <el-select v-model="createForm.method" placeholder="请选择请求方法" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>

        <el-form-item label="权限标识" prop="permissionCode">
          <el-select
            v-model="createForm.permissionCode"
            filterable
            placeholder="请选择权限标识"
            style="width: 100%"
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionCode"
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

    <el-dialog v-model="editDialogVisible" title="编辑资源" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="资源名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入资源名称" />
        </el-form-item>

        <el-form-item label="API路径" prop="path">
          <el-input v-model="editForm.path" placeholder="请输入API路径（支持/**）" />
        </el-form-item>

        <el-form-item label="请求方法" prop="method">
          <el-select v-model="editForm.method" placeholder="请选择请求方法" style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>

        <el-form-item label="权限标识" prop="permissionCode">
          <el-select
            v-model="editForm.permissionCode"
            filterable
            placeholder="请选择权限标识"
            style="width: 100%"
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionCode"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
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
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    resourceListApi,
    createResourceApi,
    updateResourceApi,
    deleteResourceApi,
  } from '@/api/resource'
  import { permissionSimpleListApi } from '@/api/permission'
  import type {
    ResourceQueryParams,
    ResourceItem,
    ResourceCreateDto,
    ResourceUpdateDto,
  } from '@/api/resource/type'
  import type { SimplePermissionItem } from '@/api/permission/type'

  const route = useRoute()

  const queryParams = reactive<ResourceQueryParams>({
    name: '',
    path: '',
    permissionCode: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const resourceList = ref<ResourceItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<ResourceCreateDto>({
    name: '',
    path: '',
    method: 'GET',
    permissionCode: '',
  })

  const editForm = reactive<ResourceUpdateDto>({
    resourceId: 0,
    name: '',
    path: '',
    method: 'GET',
    permissionCode: '',
    status: 1,
  })

  const permissionOptions = ref<SimplePermissionItem[]>([])

  const createRules = reactive<FormRules>({
    name: [
      { required: true, message: '请输入资源名称', trigger: 'blur' },
      { max: 64, message: '资源名称长度不能超过64个字符', trigger: 'blur' },
    ],
    path: [
      { required: true, message: '请输入API路径', trigger: 'blur' },
      { max: 200, message: 'API路径长度不能超过200个字符', trigger: 'blur' },
    ],
    method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
    permissionCode: [{ required: true, message: '请选择权限标识', trigger: 'change' }],
  })

  const editRules = reactive<FormRules>({
    name: [
      { required: true, message: '请输入资源名称', trigger: 'blur' },
      { max: 64, message: '资源名称长度不能超过64个字符', trigger: 'blur' },
    ],
    path: [
      { required: true, message: '请输入API路径', trigger: 'blur' },
      { max: 200, message: 'API路径长度不能超过200个字符', trigger: 'blur' },
    ],
    method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
    permissionCode: [{ required: true, message: '请选择权限标识', trigger: 'change' }],
  })

  const fetchPermissionOptions = async () => {
    try {
      permissionOptions.value = await permissionSimpleListApi()
    } catch (error) {
      console.error('获取权限选项失败:', error)
      ElMessage.error('获取权限选项失败')
    }
  }

  const fetchResourceList = async (page: number = 1, size: number = 10) => {
    try {
      const params: ResourceQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await resourceListApi(params)
      resourceList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取资源列表失败'
      ElMessage.error(errorMsg)
      console.error('获取资源列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      name: '',
      path: '',
      permissionCode: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchResourceList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchResourceList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchResourceList(page, pageSize.value)
  }

  const getMethodType = (method: string) => {
    const methodMap: Record<string, 'success' | 'warning' | 'primary' | 'danger'> = {
      GET: 'success',
      POST: 'warning',
      PUT: 'primary',
      DELETE: 'danger',
    }
    return methodMap[method] || 'info'
  }

  const handleCreate = async () => {
    createDialogVisible.value = true
    await fetchPermissionOptions()
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
          await createResourceApi(createForm)
          ElMessage.success('资源创建成功')
          createDialogVisible.value = false
          fetchResourceList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建资源失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = async (row: ResourceItem) => {
    editForm.resourceId = row.resourceId
    editForm.name = row.name
    editForm.path = row.path
    editForm.method = row.method
    editForm.permissionCode = row.permissionCode
    editForm.status = row.status

    editDialogVisible.value = true
    await fetchPermissionOptions()
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
          await updateResourceApi(editForm)
          ElMessage.success('资源更新成功')
          editDialogVisible.value = false
          fetchResourceList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新资源失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: ResourceItem) => {
    ElMessageBox.confirm(`确定要删除资源 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteResourceApi(row.resourceId)
        ElMessage.success('资源删除成功')
        fetchResourceList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除资源失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchResourceList()
  })
</script>

<style scoped>
  .el-pagination {
    display: flex;
    justify-content: flex-end;
  }
</style>
