<template>
  <NavBar />

  <div class="profile-container">
    <h1 class="page-title">会员中心</h1>

    <el-card>
      <template #header>
        <span>基本信息</span>
      </template>

      <div class="avatar-section">
        <div class="avatar-preview">
          <img
            :src="avatarPreview || profileForm.avatarUrl || '/default-avatar.png'"
            alt="头像"
            class="avatar-img"
            @error="handleAvatarError"
          />
          <el-upload
            ref="avatarUploadRef"
            :show-file-list="false"
            :http-request="customUpload"
            :before-upload="beforeAvatarUpload"
            accept="image/jpeg,image/jpg,image/png"
          >
            <el-button size="small" type="primary" class="change-avatar-btn">更换头像</el-button>
          </el-upload>
        </div>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="100px"
          style="max-width: 600px; margin-top: 20px"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" placeholder="请输入用户名" />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item label="性别" prop="gender">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :value="0">未知</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="updateProfile" :loading="loading">保存信息</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>修改密码</span>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
        style="max-width: 600px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原安全码"
            show-password
          />
        </el-form-item>

        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="8位以上，包含大小写字母和数字"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="updatePassword" :loading="passwordLoading">
            修改密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive, onMounted } from 'vue'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
  import {
    getMemberInfoApi,
    updateMemberInfoApi,
    updatePasswordApi,
    uploadAvatarApi,
  } from '@/api/member'
  import type { MemberInfo, UpdateMemberInfo } from '@/api/member/type'
  import { useUserStore } from '@/stores/user'
  import router from '@/router'
  import NavBar from '@/components/navbar.vue'

  const profileFormRef = ref<FormInstance>()
  const profileForm = ref<UpdateMemberInfo>({
    username: '',
    nickname: '',
    phone: '',
    email: '',
    gender: 0,
    avatarUrl: '',
  })
  const loading = ref(false)

  const avatarUploadRef = ref()
  const avatarPreview = ref<string>('')

  // 表单规则
  const profileRules = reactive<FormRules>({
    username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
    phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
    email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  })

  // 加载会员信息
  const loadMemberInfo = async () => {
    try {
      const info = await getMemberInfoApi()
      profileForm.value = {
        username: info.username,
        nickname: info.nickname || '',
        phone: info.phone || '',
        email: info.email || '',
        gender: info.gender,
        avatarUrl: info.avatarUrl || '',
      }
      // 设置头像预览
      avatarPreview.value = info.avatarUrl || ''
    } catch (error) {
      console.error('加载会员信息失败:', error)
    }
  }

  // 头像上传前验证
  const beforeAvatarUpload = (file: File) => {
    const isJPG = file.type === 'image/jpeg' || file.type === 'image/jpg'
    const isPNG = file.type === 'image/png'
    const isValidType = isJPG || isPNG

    if (!isValidType) {
      ElMessage.error('头像只能是 JPG/JPEG/PNG 格式!')
      return false
    }

    const isLt2M = file.size / 1024 / 1024 < 2
    if (!isLt2M) {
      ElMessage.error('头像大小不能超过 2MB!')
      return false
    }

    // 设置预览
    const reader = new FileReader()
    reader.onload = (e) => {
      avatarPreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file)

    return true
  }

  // 自定义上传方法
  const customUpload = async (options: UploadRequestOptions) => {
    const { file, onSuccess, onError } = options

    try {
      const response = await uploadAvatarApi(file as File)
      ElMessage.success('头像上传成功')

      // 更新表单中的头像URL
      profileForm.value.avatarUrl = response

      // 更新用户store中的头像
      const userStore = useUserStore()
      if (userStore.userInfo) {
        userStore.userInfo.avatarUrl = response
      }

      onSuccess?.(response, file)
    } catch (error: any) {
      ElMessage.error(error.msg || '头像上传失败')
      onError?.(error)
    }
  }

  // 处理头像加载错误
  const handleAvatarError = (e: Event) => {
    ;(e.target as HTMLImageElement).src = '/default-avatar.png'
  }

  // 更新会员信息
  const updateProfile = () => {
    profileFormRef.value?.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          await updateMemberInfoApi(profileForm.value)
          ElMessage.success('会员信息更新成功')
          const userStore = useUserStore()
          await userStore.fetchUserInfo()
        } catch (error: any) {
          ElMessage.error(error.msg || '更新失败')
        } finally {
          loading.value = false
        }
      }
    })
  }

  const passwordFormRef = ref<FormInstance>()
  const passwordForm = ref({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  const passwordLoading = ref(false)

  const passwordRules = reactive<FormRules>({
    oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== passwordForm.value.newPassword) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
  })

  const updatePassword = () => {
    passwordFormRef.value?.validate(async (valid) => {
      if (valid) {
        passwordLoading.value = true
        try {
          await updatePasswordApi(passwordForm.value)
          ElMessage.success('密码修改成功，请重新登录')
          const userStore = useUserStore()
          await userStore.logout()
          router.push('/login')
        } catch (error: any) {
          ElMessage.error(error.msg || '修改失败')
        } finally {
          passwordLoading.value = false
        }
      }
    })
  }

  onMounted(() => {
    loadMemberInfo()
  })
</script>

<style scoped>
  .profile-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }

  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0 0 20px 0;
  }

  .avatar-section {
    display: flex;
    align-items: flex-start;
    gap: 40px;
  }

  .avatar-preview {
    text-align: center;
  }

  .avatar-img {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #ebeef5;
  }

  .change-avatar-btn {
    margin-top: 15px;
  }

  :deep(.el-upload) {
    display: inline-block;
  }

  :deep(.el-upload__input) {
    display: none;
  }
</style>
