<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增权限</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="权限标识">
        <el-input
          v-model="queryParams.permissionCode"
          placeholder="请输入权限标识"
          clearable
          @keyup.enter="() => fetchPermissionList()"
        />
      </el-form-item>

      <el-form-item label="权限名称">
        <el-input
          v-model="queryParams.permissionName"
          placeholder="请输入权限名称"
          clearable
          @keyup.enter="() => fetchPermissionList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchPermissionList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchPermissionList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="permissionList" border style="width: 100%">
      <el-table-column prop="permissionCode" label="权限标识" min-width="200" />
      <el-table-column prop="permissionName" label="权限名称" min-width="150" />
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
      title="新增权限"
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
        <el-form-item label="权限标识" prop="permissionCode">
          <el-input
            v-model="createForm.permissionCode"
            placeholder="请输入权限标识，如：user:view"
          />
        </el-form-item>

        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="createForm.permissionName" placeholder="请输入权限名称" />
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

    <el-dialog v-model="editDialogVisible" title="编辑权限" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="权限标识" prop="permissionCode">
          <el-input
            v-model="editForm.permissionCode"
            placeholder="请输入权限标识，如：user:view"
            disabled
          />
        </el-form-item>

        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="editForm.permissionName" placeholder="请输入权限名称" />
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
    permissionListApi,
    createPermissionApi,
    updatePermissionApi,
    deletePermissionApi,
  } from '@/api/permission'
  import type {
    PermissionQueryParams,
    PermissionItem,
    PermissionCreateDto,
    PermissionUpdateDto,
  } from '@/api/permission/type'

  const route = useRoute()

  const queryParams = reactive<PermissionQueryParams>({
    permissionCode: '',
    permissionName: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const permissionList = ref<PermissionItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<PermissionCreateDto>({
    permissionCode: '',
    permissionName: '',
  })

  const editForm = reactive<PermissionUpdateDto>({
    permissionId: 0,
    permissionCode: '',
    permissionName: '',
    status: 1,
  })

  const createRules = reactive<FormRules>({
    permissionCode: [
      { required: true, message: '请输入权限标识', trigger: 'blur' },
      { max: 64, message: '权限标识长度不能超过64个字符', trigger: 'blur' },
    ],
    permissionName: [
      { required: true, message: '请输入权限名称', trigger: 'blur' },
      { max: 64, message: '权限名称长度不能超过64个字符', trigger: 'blur' },
    ],
  })

  const editRules = reactive<FormRules>({
    permissionCode: [
      { required: true, message: '请输入权限标识', trigger: 'blur' },
      { max: 64, message: '权限标识长度不能超过64个字符', trigger: 'blur' },
    ],
    permissionName: [
      { required: true, message: '请输入权限名称', trigger: 'blur' },
      { max: 64, message: '权限名称长度不能超过64个字符', trigger: 'blur' },
    ],
  })

  const fetchPermissionList = async (page: number = 1, size: number = 10) => {
    try {
      const params: PermissionQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await permissionListApi(params)
      permissionList.value = res.records || []
      total.value = res.total || 0
    } catch (error) {
      console.error('获取权限列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      permissionCode: '',
      permissionName: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchPermissionList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchPermissionList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchPermissionList(page, pageSize.value)
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
          await createPermissionApi(createForm)
          ElMessage.success('创建成功')
          createDialogVisible.value = false
          fetchPermissionList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建权限失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = (row: PermissionItem) => {
    editForm.permissionId = row.permissionId
    editForm.permissionCode = row.permissionCode
    editForm.permissionName = row.permissionName
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
          await updatePermissionApi(editForm)
          ElMessage.success('更新成功')
          editDialogVisible.value = false
          fetchPermissionList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新权限失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: PermissionItem) => {
    if (!row.canDelete) {
      ElMessageBox.alert(
        `该权限已被${row.usedByRoles}个角色和${row.usedByMenus}个菜单使用，无法删除`,
        '提示',
        { type: 'warning' }
      )
      return
    }

    ElMessageBox.confirm(`确定要删除权限 "${row.permissionName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deletePermissionApi(row.permissionId)
        ElMessage.success('删除成功')
        fetchPermissionList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除权限失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchPermissionList()
  })
</script>

<style scoped>
  .el-pagination {
    display: flex;
    justify-content: flex-end;
  }
</style>
