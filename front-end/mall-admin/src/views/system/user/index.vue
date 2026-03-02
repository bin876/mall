<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
        <el-button type="primary" @click="handleCreate">新增用户</el-button>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="用户名">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          @keyup.enter="() => fetchUserList()"
        />
      </el-form-item>

      <el-form-item label="真实姓名">
        <el-input
          v-model="queryParams.realName"
          placeholder="请输入真实姓名"
          clearable
          @keyup.enter="() => fetchUserList()"
        />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="() => fetchUserList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          @change="() => fetchUserList()"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchUserList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="userList" border style="width: 100%">
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="100" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
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
          <el-button
            size="small"
            type="danger"
            link
            @click="handleDelete(scope.row)"
            v-has="'system:user:*'"
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

    <el-dialog
      v-model="createDialogVisible"
      title="新增用户"
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
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="createForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="createForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="createForm.gender" placeholder="请选择性别">
            <el-option :value="0" label="未知" />
            <el-option :value="1" label="男" />
            <el-option :value="2" label="女" />
          </el-select>
        </el-form-item>

        <el-form-item label="角色分配" prop="roleIds">
          <el-select
            v-model="createForm.roleIds"
            multiple
            filterable
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.roleId"
              :label="`${role.roleCode} - ${role.roleName}`"
              :value="role.roleId"
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

    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select v-model="editForm.gender" placeholder="请选择性别">
            <el-option :value="0" label="未知" />
            <el-option :value="1" label="男" />
            <el-option :value="2" label="女" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="角色分配" prop="roleIds">
          <el-select
            v-model="editForm.roleIds"
            multiple
            filterable
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.roleId"
              :label="`${role.roleCode} - ${role.roleName}`"
              :value="role.roleId"
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
    userListApi,
    createUserApi,
    updateUserApi,
    deleteUserApi,
    getUserRolesApi,
  } from '@/api/user'
  import { roleSimpleListApi } from '@/api/role'
  import type { UserQueryParams, UserItem, UserCreateDto, UserUpdateDto } from '@/api/user/type'
  import type { SimpleRoleItem } from '@/api/role/type'

  const route = useRoute()

  const queryParams = reactive<UserQueryParams>({
    username: '',
    realName: '',
    phone: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const userList = ref<UserItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)

  const createDialogVisible = ref(false)
  const editDialogVisible = ref(false)
  const createLoading = ref(false)
  const editLoading = ref(false)

  const createFormRef = ref<FormInstance>()
  const editFormRef = ref<FormInstance>()

  const createForm = reactive<UserCreateDto>({
    username: '',
    password: '',
    realName: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    roleIds: [],
  })

  const editForm = reactive<UserUpdateDto>({
    userId: 0,
    realName: '',
    nickname: '',
    email: '',
    phone: '',
    gender: 0,
    status: 1,
    roleIds: [],
  })

  const roleOptions = ref<SimpleRoleItem[]>([])

  const createRules = reactive<FormRules>({
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' },
    ],
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { max: 64, message: '真实姓名长度不能超过64个字符', trigger: 'blur' },
    ],
    roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }],
  })

  const editRules = reactive<FormRules>({
    realName: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { max: 64, message: '真实姓名长度不能超过64个字符', trigger: 'blur' },
    ],
    roleIds: [{ required: true, message: '请至少选择一个角色', trigger: 'change' }],
  })

  const fetchRoleOptions = async () => {
    try {
      roleOptions.value = await roleSimpleListApi()
    } catch (error) {
      console.error('获取角色选项失败:', error)
      ElMessage.error('获取角色选项失败')
    }
  }

  const fetchUserList = async (page: number = 1, size: number = 10) => {
    try {
      const params: UserQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await userListApi(params)
      userList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取用户列表失败'
      ElMessage.error(errorMsg)
      console.error('获取用户列表失败:', error)
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      username: '',
      realName: '',
      phone: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchUserList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchUserList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchUserList(page, pageSize.value)
  }

  const handleCreate = async () => {
    createDialogVisible.value = true
    await fetchRoleOptions()
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
          await createUserApi(createForm)
          ElMessage.success('用户创建成功')
          createDialogVisible.value = false
          fetchUserList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '创建用户失败'
          ElMessage.error(errorMsg)
        } finally {
          createLoading.value = false
        }
      }
    })
  }

  const handleEdit = async (row: UserItem) => {
    editForm.userId = row.userId
    editForm.realName = row.realName
    editForm.nickname = row.nickname
    editForm.email = row.email
    editForm.phone = row.phone
    editForm.gender = row.gender
    editForm.status = row.status

    await fetchRoleOptions()

    try {
      const roleIds = await getUserRolesApi(row.userId)
      editForm.roleIds = roleIds || []
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取用户角色失败'
      ElMessage.error(errorMsg)
      editForm.roleIds = []
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
          await updateUserApi(editForm)
          ElMessage.success('用户更新成功')
          editDialogVisible.value = false
          fetchUserList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新用户失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleDelete = (row: UserItem) => {
    ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteUserApi(row.userId)
        ElMessage.success('用户删除成功')
        fetchUserList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除用户失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchUserList()
  })
</script>

<style scoped>
  .el-pagination {
    display: flex;
    justify-content: flex-end;
  }
</style>
