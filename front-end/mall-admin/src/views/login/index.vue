<template>
  <div class="login-container">
    <div class="gradient-bg"></div>

    <el-card class="login-card" shadow="never">
      <div class="logo-container">
        <div class="logo-icon">
          <el-icon :size="32"><Platform /></el-icon>
        </div>
        <h1 class="app-name">后台管理系统</h1>
      </div>

      <h2 class="login-title">欢迎登录</h2>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        @submit.prevent="handleLogin"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="loginForm.account"
            placeholder="用户名 / 手机号 / 邮箱"
            @keyup.enter="handleLogin"
            autofocus
          >
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
            <template #suffix>
              <el-icon class="password-toggle" @click="togglePassword">
                <View v-if="passwordVisible" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
            native-type="submit"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="footer-links">
        <span>© 2023 管理平台</span>
        <span class="help-link">帮助中心</span>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { User, Lock, View, Hide, Platform } from '@element-plus/icons-vue'
  import { useUserStore } from '@/stores/user'
  import type { FormRules } from 'element-plus'

  const router = useRouter()

  const userStore = useUserStore()

  const loginFormRef = ref()

  const loading = ref(false)

  const passwordVisible = ref(false)

  const loginForm = reactive({
    account: '',
    password: '',
  })

  const phoneReg = /^1[3-9]\d{9}$/
  const emailReg = /^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$/
  const usernameReg = /^[a-zA-Z0-9_]{3,16}$/

  const validateAccount = (_: any, value: string, callback: (error?: Error) => void) => {
    if (!value) {
      return callback(new Error('请输入账号'))
    }

    if (phoneReg.test(value) || emailReg.test(value) || usernameReg.test(value)) {
      callback()
    } else {
      callback(new Error('请输入有效的手机号、邮箱或用户名'))
    }
  }

  const rules = reactive<FormRules>({
    account: [
      { required: true, message: '请输入用户名 / 手机号 / 邮箱', trigger: 'blur' },
      { validator: validateAccount, trigger: 'blur' },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    ],
  })

  const togglePassword = () => {
    passwordVisible.value = !passwordVisible.value
  }

  const handleLogin = async () => {
    if (loading.value) return

    try {
      const valid = await loginFormRef.value?.validate()
      if (!valid) return

      loading.value = true

      const LoginParams = {
        username: loginForm.account.trim(),
        password: loginForm.password,
        clientId: 'admin-web',
      }

      const success = await userStore.login(LoginParams)

      if (!success) {
        ElMessage.error('账号或密码错误')
        return
      }

      ElMessage.success(`登录成功，欢迎 ${loginForm.account}！`)
      router.push('/dashboard')
    } catch (error) {
      console.error('Login failed:', error)
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  }
</script>

<style scoped lang="scss">
  .login-container {
    position: relative;
    width: 100%;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden;

    .gradient-bg {
      position: absolute;
      top: -50%;
      left: -50%;
      width: 200%;
      height: 200%;
      z-index: 0;
    }
  }

  .login-card {
    position: relative;
    z-index: 1;
    width: 420px;
    padding: 40px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(10px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
    border: 1px solid rgba(255, 255, 255, 0.6);
  }

  .logo-container {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;

    .logo-icon {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      background: linear-gradient(135deg, #409eff 0%, #53b4ff 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }

    .app-name {
      font-size: 28px;
      font-weight: 700;
      background: linear-gradient(to right, #333, #555);
      background-clip: text;
      margin: 0;
    }
  }

  .login-title {
    text-align: center;
    font-size: 24px;
    font-weight: 600;
    color: #1f2d3d;
    margin-bottom: 32px;
  }

  .login-form {
    .input-icon {
      color: #999;
      font-size: 16px;
    }

    .password-toggle {
      cursor: pointer;
      color: #999;
      font-size: 18px;

      &:hover {
        color: #409eff;
      }
    }

    .el-input__inner {
      padding-left: 40px;
    }
  }

  .login-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 500;
    letter-spacing: 1px;
    background: linear-gradient(135deg, #409eff 0%, #53b4ff 100%);
    border: none;
    box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  }

  .footer-links {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #eee;
    color: #999;
    font-size: 14px;

    .help-link {
      color: #409eff;
      cursor: pointer;
    }
  }

  .mobile-copyright {
    display: none;
    position: absolute;
    bottom: 20px;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
  }
</style>
