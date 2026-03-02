<template>
  <el-card>
    <template #header>
      <div class="flex justify-between items-center">
        <span class="text-lg">{{ route.meta.title }}</span>
      </div>
    </template>

    <el-form :model="queryParams" inline>
      <el-form-item label="用户名">
        <el-input
          v-model="queryParams.username"
          placeholder="请输入用户名"
          clearable
          @keyup.enter="() => fetchMemberList()"
        />
      </el-form-item>

      <el-form-item label="昵称">
        <el-input
          v-model="queryParams.nickname"
          placeholder="请输入昵称"
          clearable
          @keyup.enter="() => fetchMemberList()"
        />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input
          v-model="queryParams.phone"
          placeholder="请输入手机号"
          clearable
          @keyup.enter="() => fetchMemberList()"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 120px"
        >
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="禁用" />
        </el-select>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="() => fetchMemberList()">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="memberList" border style="width: 100%" v-loading="loading" row-key="memberId">
      <el-table-column type="index" width="60" />

      <el-table-column prop="username" label="用户名" min-width="120" />

      <el-table-column prop="nickname" label="昵称" min-width="100" />

      <el-table-column prop="phone" label="手机号" min-width="120" />

      <el-table-column prop="email" label="邮箱" min-width="150" />

      <el-table-column label="头像" width="80">
        <template #default="scope">
          <img
            v-if="scope.row.avatarUrl"
            :src="scope.row.avatarUrl"
            alt="头像"
            class="member-avatar"
            @error="handleAvatarError(scope.row)"
          />
          <div v-else class="default-avatar">
            <el-icon><User /></el-icon>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="性别" width="80">
        <template #default="scope">
          {{ scope.row.genderDesc }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="注册时间" width="180" />

      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
            编辑
          </el-button>
          <el-button size="small" type="warning" link @click="handleResetPassword(scope.row)">
            重置密码
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

    <el-dialog v-model="editDialogVisible" title="编辑会员" width="500px" @close="handleCloseEdit">
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editForm.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
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

    <el-dialog
      v-model="passwordDialogVisible"
      title="重置密码"
      width="500px"
      @close="handleClosePassword"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        style="padding-right: 150px"
        @submit.prevent
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClosePassword">取消</el-button>
          <el-button type="primary" @click="handlePasswordSubmit" :loading="passwordLoading">
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
  import { User } from '@element-plus/icons-vue'
  import type {
    MemberQueryParams,
    MemberItem,
    UpdateMemberDto,
    ResetPasswordDto,
  } from '@/api/member/type'
  import {
    memberListApi,
    updateMemberApi,
    updateMemberStatusApi,
    resetPasswordApi,
    deleteMemberApi,
  } from '@/api/member'

  const route = useRoute()

  const queryParams = reactive<MemberQueryParams>({
    username: '',
    nickname: '',
    phone: '',
    status: null,
    pageNum: 1,
    pageSize: 10,
  })

  const memberList = ref<MemberItem[]>([])
  const total = ref<number>(0)
  const currentPage = ref<number>(1)
  const pageSize = ref<number>(10)
  const loading = ref(false)

  const editDialogVisible = ref(false)
  const editLoading = ref(false)
  const editFormRef = ref<FormInstance>()
  const editForm = reactive({
    memberId: 0,
    username: '',
    nickname: '',
    phone: '',
    email: '',
    gender: 0,
  })

  const passwordDialogVisible = ref(false)
  const passwordLoading = ref(false)
  const passwordFormRef = ref<FormInstance>()
  const passwordForm = reactive({
    memberId: 0,
    newPassword: '',
  })

  const editRules = reactive<FormRules>({
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 64, message: '用户名长度在3-64个字符', trigger: 'blur' },
    ],
    phone: [
      {
        pattern: /^1[3-9]\d{9}$/,
        message: '手机号格式不正确',
        trigger: 'blur',
      },
    ],
    email: [
      {
        type: 'email',
        message: '邮箱格式不正确',
        trigger: 'blur',
      },
    ],
  })

  const passwordRules = reactive<FormRules>({
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' },
    ],
  })

  const fetchMemberList = async (page: number = 1, size: number = 10) => {
    loading.value = true
    try {
      const params: MemberQueryParams = {
        ...queryParams,
        pageNum: page,
        pageSize: size,
      }

      const res = await memberListApi(params)
      memberList.value = res.records || []
      total.value = res.total || 0
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '获取会员列表失败'
      ElMessage.error(errorMsg)
      console.error('获取会员列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const resetQuery = () => {
    Object.assign(queryParams, {
      username: '',
      nickname: '',
      phone: '',
      status: null,
      pageNum: 1,
      pageSize: 10,
    })
    fetchMemberList()
  }

  const handleSizeChange = (size: number) => {
    pageSize.value = size
    fetchMemberList(currentPage.value, size)
  }

  const handleCurrentChange = (page: number) => {
    currentPage.value = page
    fetchMemberList(page, pageSize.value)
  }

  const handleAvatarError = (row: MemberItem) => {
    row.avatarUrl = ''
  }

  // 状态切换（使用点击切换而非switch）
  const handleStatusChange = async (member: MemberItem) => {
    const newStatus = member.status === 1 ? 0 : 1
    try {
      await updateMemberStatusApi(member.memberId, newStatus)
      ElMessage.success('状态更新成功')
      member.status = newStatus
    } catch (error: any) {
      const errorMsg = error?.response?.data?.msg ?? '更新状态失败'
      ElMessage.error(errorMsg)
    }
  }

  const handleEdit = (member: MemberItem) => {
    editForm.memberId = member.memberId
    editForm.username = member.username
    editForm.nickname = member.nickname || ''
    editForm.phone = member.phone || ''
    editForm.email = member.email || ''
    editForm.gender = member.gender
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
          const updateDto: UpdateMemberDto = {
            username: editForm.username,
            nickname: editForm.nickname,
            phone: editForm.phone,
            email: editForm.email,
            gender: editForm.gender,
          }
          await updateMemberApi(editForm.memberId, updateDto)
          ElMessage.success('会员信息更新成功')
          editDialogVisible.value = false
          fetchMemberList()
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '更新会员失败'
          ElMessage.error(errorMsg)
        } finally {
          editLoading.value = false
        }
      }
    })
  }

  const handleResetPassword = (member: MemberItem) => {
    passwordForm.memberId = member.memberId
    passwordForm.newPassword = ''
    passwordDialogVisible.value = true
  }

  const handleClosePassword = () => {
    passwordDialogVisible.value = false
    passwordFormRef.value?.resetFields()
  }

  const handlePasswordSubmit = () => {
    passwordFormRef.value?.validate(async (valid) => {
      if (valid) {
        passwordLoading.value = true
        try {
          const resetDto: ResetPasswordDto = {
            newPassword: passwordForm.newPassword,
          }
          await resetPasswordApi(passwordForm.memberId, resetDto)
          ElMessage.success('密码重置成功')
          passwordDialogVisible.value = false
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg ?? '重置密码失败'
          ElMessage.error(errorMsg)
        } finally {
          passwordLoading.value = false
        }
      }
    })
  }

  const handleDelete = (member: MemberItem) => {
    ElMessageBox.confirm(`确定要删除会员 "${member.username}" 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(async () => {
      try {
        await deleteMemberApi(member.memberId)
        ElMessage.success('会员删除成功')
        fetchMemberList()
      } catch (error: any) {
        const errorMsg = error?.response?.data?.msg ?? '删除会员失败'
        ElMessage.error(errorMsg)
      }
    })
  }

  onMounted(() => {
    fetchMemberList()
  })
</script>

<style scoped>
  .member-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
  }

  .default-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
  }

  .default-avatar .el-icon {
    font-size: 16px;
  }
</style>
