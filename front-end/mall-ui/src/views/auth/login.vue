<template>
  <NavBar />

  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>用户登录</h1>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        @submit.prevent="handleLogin"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            @keyup.enter="handleLogin"
            autocomplete="off"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            @click="handleLogin"
            :loading="loading"
            class="login-btn"
            style="width: 100%"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <router-link to="/register" class="register-link">立即注册</router-link>
        <span class="copyright">© 2026 电商系统. All Rights Reserved.</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()
  const route = useRoute()
  const userStore = useUserStore()

  const loginFormRef = ref<FormInstance>()
  const loading = ref(false)

  const loginForm = reactive({
    username: '',
    password: '',
    clientId: 'member-web',
  })

  const loginRules = reactive<FormRules>({
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 64, message: '用户名长度在3-64个字符', trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在6-20个字符', trigger: 'blur' },
    ],
  })

  const handleLogin = () => {
    loginFormRef.value?.validate(async (valid) => {
      if (valid) {
        loading.value = true
        try {
          const success = await userStore.login(loginForm)
          if (success) {
            const redirect = route.query.redirect as string
            router.push(redirect || '/')
            ElMessage.success('登录成功')
          }
        } catch (error: any) {
          const errorMsg = error?.response?.data?.msg || '登录失败，请检查用户名和密码'
          ElMessage.error(errorMsg)
        } finally {
          loading.value = false
        }
      }
    })
  }
</script>

<style scoped>
  .login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: #f5f5f5;
    padding: 20px;
  }

  .login-box {
    width: 400px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .login-header {
    padding: 30px 40px 20px;
    text-align: center;
  }

  .login-header h1 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  .login-form {
    padding: 0 40px 30px;
  }

  .login-btn {
    height: 44px;
  }

  .login-footer {
    padding: 0 40px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #999;
    font-size: 12px;
  }

  .register-link {
    color: #409eff;
    text-decoration: none;
  }

  .register-link:hover {
    color: #66b1ff;
  }
</style>
