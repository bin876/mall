<template>
  <NavBar />

  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>会员注册</h1>
      </div>

      <!-- 步骤条 -->
      <el-steps :active="currentStep" finish-status="success" simple>
        <el-step title="填写账户信息"></el-step>
        <el-step title="验证手机号"></el-step>
        <el-step title="完成注册"></el-step>
      </el-steps>

      <div class="step-content">
        <div v-if="currentStep === 0" class="form-step">
          <el-form
            ref="accountFormRef"
            :model="registerForm"
            :rules="accountRules"
            label-position="top"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="4-20位字母数字下划线"
                autocomplete="off"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="8位以上，包含大小写字母和数字"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                autocomplete="new-password"
              />
            </el-form-item>

            <el-form-item label="昵称" prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称（可选）"
                autocomplete="off"
              />
            </el-form-item>
          </el-form>

          <div class="step-actions">
            <el-button @click="goToLogin">返回登录</el-button>
            <el-button type="primary" @click="nextStep(0)">下一步</el-button>
          </div>
        </div>

        <div v-if="currentStep === 1" class="form-step">
          <el-form
            ref="phoneFormRef"
            :model="registerForm"
            :rules="phoneRules"
            label-position="top"
          >
            <el-form-item label="手机号" prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                autocomplete="off"
              />
            </el-form-item>

            <el-form-item label="验证码" prop="smsCode">
              <div class="sms-code-container">
                <el-input
                  v-model="registerForm.smsCode"
                  placeholder="请输入验证码"
                  style="width: 220px"
                  autocomplete="off"
                />
                <el-button :disabled="smsDisabled" @click="sendSmsCode" style="margin-left: 10px">
                  {{ smsDisabled ? `(${smsCount}s)` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="邮箱（可选）" prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" autocomplete="off" />
            </el-form-item>
          </el-form>

          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep(1)">下一步</el-button>
          </div>
        </div>

        <div v-if="currentStep === 2" class="form-step">
          <div class="success-content">
            <el-icon class="success-icon" size="60" color="#e40000">
              <CircleCheck />
            </el-icon>
            <h3>注册成功！</h3>
            <p>您的账号 {{ registerForm.username }} 已创建成功</p>
            <p class="tip">3秒后自动跳转到登录页</p>
          </div>

          <div class="step-actions">
            <el-button @click="goToLogin">立即登录</el-button>
          </div>
        </div>
      </div>

      <div class="register-footer">
        <span>© 2026 电商系统. All Rights Reserved.</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue'
  import { useRouter } from 'vue-router'
  import { CircleCheck } from '@element-plus/icons-vue'
  import type { FormInstance, FormRules } from 'element-plus'
  import { registerApi } from '@/api/member'
  import type { RegisterMember } from '@/api/member/type'
  import NavBar from '@/components/navbar.vue'

  const router = useRouter()

  // 步骤控制
  const currentStep = ref(0)

  // 注册表单
  const registerForm = reactive<RegisterMember>({
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phone: '',
    smsCode: '',
    email: '',
  })

  // 表单引用
  const accountFormRef = ref<FormInstance>()
  const phoneFormRef = ref<FormInstance>()

  // 短信验证码
  const smsDisabled = ref(false)
  const smsCount = ref(60)

  // 表单规则
  const accountRules = reactive<FormRules>({
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      {
        pattern: /^[a-zA-Z0-9_]{4,20}$/,
        message: '4-20位字母数字下划线',
        trigger: 'blur',
      },
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      {
        pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,}$/,
        message: '8位以上，包含大小写字母和数字',
        trigger: 'blur',
      },
    ],
    confirmPassword: [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== registerForm.password) {
            callback(new Error('两次输入密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur',
      },
    ],
    nickname: [{ max: 20, message: '昵称不能超过20个字符', trigger: 'blur' }],
  })

  const phoneRules = reactive<FormRules>({
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      {
        pattern: /^1[3-9]\d{9}$/,
        message: '手机号格式不正确',
        trigger: 'blur',
      },
    ],
    smsCode: [
      { required: true, message: '请输入验证码', trigger: 'blur' },
      { len: 6, message: '验证码为6位数字', trigger: 'blur' },
    ],
    email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  })

  // 步骤控制
  const prevStep = () => {
    if (currentStep.value > 0) {
      currentStep.value--
    }
  }

  const nextStep = async (step: number) => {
    if (step === 0) {
      // 验证账户信息
      await accountFormRef.value?.validate(async (valid) => {
        if (valid) {
          currentStep.value = 1
        }
      })
    } else if (step === 1) {
      // 验证手机信息
      await phoneFormRef.value?.validate(async (valid) => {
        if (valid) {
          // 模拟注册（实际应调用短信验证）
          await handleRegister()
        }
      })
    }
  }

  // 发送短信验证码
  const sendSmsCode = () => {
    if (!registerForm.phone) {
      ElMessage.warning('请先输入手机号')
      return
    }

    // 验证手机号格式
    if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
      ElMessage.warning('手机号格式不正确')
      return
    }

    smsDisabled.value = true
    let count = 60
    const timer = setInterval(() => {
      count--
      smsCount.value = count
      if (count <= 0) {
        clearInterval(timer)
        smsDisabled.value = false
      }
    }, 1000)

    // 模拟发送成功
    ElMessage.success('验证码已发送（模拟）')
  }

  // 处理注册
  const handleRegister = async () => {
    try {
      // 调用注册接口
      await registerApi({
        username: registerForm.username,
        password: registerForm.password,
        phone: registerForm.phone,
        email: registerForm.email || undefined,
        nickname: registerForm.nickname || undefined,
      })

      // 跳转到成功步骤
      currentStep.value = 2

      // 3秒后跳转登录
      setTimeout(() => {
        goToLogin()
      }, 3000)
    } catch (error: any) {
      ElMessage.error(error.msg || '注册失败')
    }
  }

  // 导航
  const goToLogin = () => {
    router.push('/login')
  }
</script>

<style scoped>
  .register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: #f5f5f5;
    padding: 20px;
  }

  .register-box {
    width: 500px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    overflow: hidden;
  }

  .register-header {
    padding: 30px 40px 20px;
    text-align: center;
  }

  .register-header h1 {
    font-size: 24px;
    font-weight: 600;
    color: #333;
    margin: 0;
  }

  /* 步骤条 */
  :deep(.el-steps) {
    padding: 0 40px 20px;
  }

  :deep(.el-step__title) {
    font-size: 14px;
  }

  /* 步骤内容 */
  .step-content {
    padding: 0 40px 30px;
  }

  .form-step {
    margin-top: 20px;
  }

  :deep(.el-form-item__label) {
    font-weight: normal;
    color: #666;
  }

  :deep(.el-input__inner) {
    height: 44px;
  }

  /* 短信验证码 */
  .sms-code-container {
    display: flex;
    align-items: center;
  }

  /* 成功内容 */
  .success-content {
    text-align: center;
    padding: 40px 0;
  }

  .success-icon {
    margin-bottom: 20px;
  }

  .success-content h3 {
    font-size: 20px;
    color: #333;
    margin: 15px 0;
  }

  .success-content p {
    color: #666;
    margin: 5px 0;
  }

  .tip {
    color: #e40000;
    font-weight: bold;
  }

  /* 操作按钮 */
  .step-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 30px;
  }

  /* 底部 */
  .register-footer {
    padding: 0 40px 20px;
    text-align: center;
    color: #999;
    font-size: 12px;
  }
</style>
