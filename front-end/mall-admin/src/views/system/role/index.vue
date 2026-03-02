<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增角色</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="角色编码">
        <el-input
          v-model="queryParams.roleCode"
          placeholder="请输入角色编码"
          clearable
          @keyup.enter="() => fetchRoleList()"
        />
      </el-form-item>

      <el-form-item label="角色名称">
        <el-input
          v-model="queryParams.roleName"
          placeholder="请输入角色名称"
          clearable
          @keyup.enter="() => fetchRoleList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchRoleList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchRoleList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="roleList" border style="width: 100%">
      <el-table-column prop="roleCode" label="角色编码" width="180" />
      <el-table-column prop="roleName" label="角色名称" min-width="150" />
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
      title="新增角色"
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
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="createForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="createForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="权限分配" prop="permissionIds">
          <el-select
            v-model="createForm.permissionIds"
            multiple
            filterable
            placeholder="请选择权限"
            style="width: 100%"
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionId"
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

    <el-dialog v-model="editDialogVisible" title="编辑角色" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="editForm.roleCode" placeholder="请输入角色编码" />
        </el-form-item>

        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="editForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="权限分配" prop="permissionIds">
          <el-select
            v-model="editForm.permissionIds"
            multiple
            filterable
            placeholder="请选择权限"
            style="width: 100%"
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.permissionId"
              :label="`${permission.permissionCode} - ${permission.permissionName}`"
              :value="permission.permissionId"
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
  import { ref, reactive, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    roleListApi,
    createRoleApi,
    updateRoleApi,
    deleteRoleApi,
    getRolePermissionsApi,
  } from '@/api/role'
  import { permissionSimpleListApi } from '@/api/permission'
  import type { RoleQueryParams, RoleItem, RoleCreateDto, RoleUpdateDto } from '@/api/role/type'
  import type { SimplePermissionItem } from '@/api/permission/type'

  const route = useRoute()

  const queryParams = reactive<RoleQueryParams>({
    roleCode: '',
    roleName: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const roleList = ref<RoleItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<RoleCreateDto>({
    roleCode: '',
    roleName: '',
    permissionIds: [],
  })

  const editForm = reactive<RoleUpdateDto>({
    roleId: 0,
    roleCode: '',
    roleName: '',
    status: 1,
    permissionIds: [],
  })

  const permissionOptions = ref<SimplePermissionItem[]>([])

  const createRules = reactive<FormRules>({
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { max: 64, message: '角色编码长度不能超过64个字符', trigger: 'blur' },
    ],
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { max: 64, message: '角色名称长度不能超过64个字符', trigger: 'blur' },
    ],
    permissionIds: [{ required: true, message: '请至少选择一个权限', trigger: 'change' }],
  })

  const editRules = reactive<FormRules>({
    roleCode: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { max: 64, message: '角色编码长度不能超过64个字符', trigger: 'blur' },
    ],
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { max: 64, message: '角色名称长度不能超过64个字符', trigger: 'blur' },
    ],
    permissionIds: [{ required: true, message: '请至少选择一个权限', trigger: 'change' }],
  })

  const fetchPermissionOptions = async () => {
    try {
      permissionOptions.value = await permissionSimpleListApi()
    } catch (error) {
      console.error('获取权限选项失败:', error)
      ElMessage.error('获取权限选项失败')
    }
  }

  const fetchRoleList = async (page: number = 1, size: number = 10) => {
    try {
      const params: RoleQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await roleListApi(params)
      roleList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取角色列表失败'
      ElMessage.error(errorMsg)
      console.error('获取角色列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      roleCode: '',
      roleName: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchRoleList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchRoleList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchRoleList(page, pageSize.value)
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
          await createRoleApi(createForm)
          ElMessage.success('角色创建成功')
          createDialogVisible.value = false
          fetchRoleList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建角色失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = async (row: RoleItem) => {
    editForm.roleId = row.roleId
    editForm.roleCode = row.roleCode
    editForm.roleName = row.roleName
    editForm.status = row.status

    await fetchPermissionOptions()

    try {
      const permissionIds = await getRolePermissionsApi(row.roleId)
      editForm.permissionIds = permissionIds || []
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取角色权限失败'
      ElMessage.error(errorMsg)
      editForm.permissionIds = []
    }

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
          await updateRoleApi(editForm)
          ElMessage.success('角色更新成功')
          editDialogVisible.value = false
          fetchRoleList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新角色失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: RoleItem) => {
    ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteRoleApi(row.roleId)
        ElMessage.success('角色删除成功')
        fetchRoleList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除角色失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchRoleList()
  })
</script>

<style scoped>
  .el-pagination {
    display: flex;
    justify-content: flex-end;
  }
</style>
