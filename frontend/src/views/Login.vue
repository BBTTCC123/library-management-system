<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-brand">
        <el-icon :size="42" color="#409eff"><Reading /></el-icon>
        <h1>图书借阅管理系统</h1>
        <p>Spring Boot 3 + Vue 3 + Element Plus</p>
      </div>

      <el-tabs v-model="activeTab" stretch>
        <!-- 登录 -->
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            @keyup.enter="handleLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" :prefix-icon="User" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
                :prefix-icon="Lock"
              />
            </el-form-item>
            <el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">
              登 录
            </el-button>
          </el-form>

          <div class="demo-accounts">
            <div>演示账号（点击快速填入）：</div>
            <div>
              <el-tag size="small" @click="fill('admin', 'admin123')">admin / admin123</el-tag>
              <el-tag size="small" type="warning" @click="fill('librarian', 'librarian123')">
                librarian / librarian123
              </el-tag>
            </div>
            <div>
              <el-tag size="small" type="success" @click="fill('alice', 'alice123')">
                alice / alice123
              </el-tag>
              <el-tag size="small" type="success" @click="fill('bob', 'bob123')">
                bob / bob123
              </el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 注册 -->
        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="3-50 位字符" />
            </el-form-item>
            <el-form-item label="姓名" prop="fullName">
              <el-input v-model="registerForm.fullName" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="example@mail.com" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">
              注 册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const router = useRouter()
const route = useRoute()

const activeTab = ref('login')
const loading = ref(false)

const loginFormRef = ref()
const registerFormRef = ref()

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', fullName: '', email: '', password: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在 3-50 之间', trigger: 'blur' }
  ],
  fullName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度需在 6-100 之间', trigger: 'blur' }
  ]
}

function fill(username, password) {
  loginForm.username = username
  loginForm.password = password
  activeTab.value = 'login'
}

async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    const data = await store.login({ ...loginForm })
    ElMessage.success(`欢迎回来，${data.user.fullName || data.user.username}`)
    router.push(route.query.redirect || '/dashboard')
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await store.register({ ...registerForm })
    ElMessage.success('注册成功，已自动登录')
    router.push('/dashboard')
  } catch (e) {
    /* 错误已由拦截器提示 */
  } finally {
    loading.value = false
  }
}
</script>
