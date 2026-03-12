<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo">
        <div class="logo-icon">CL</div>
        <h1>CampusLink 管理后台</h1>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="用户名/邮箱/手机号"
            prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({ account: '', password: '' })

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!await formRef.value?.validate().catch(() => false)) return
  loading.value = true
  try {
    const result = await login(form)
    if (result.user.role !== 'admin') {
      ElMessage.error('无管理员权限，请使用管理员账号登录')
      return
    }
    authStore.setAuth({
      uId: result.user.uId,
      username: result.user.username,
      nickname: result.user.nickname,
      role: result.user.role,
      avatarUrl: result.user.avatarUrl,
      token: result.token,
      refreshToken: result.refreshToken
    })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.logo {
  text-align: center;
  margin-bottom: 36px;
}

.logo-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #409eff, #0070f3);
  border-radius: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}

.logo h1 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
}
</style>
