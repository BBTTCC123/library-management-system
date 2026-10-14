<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="26"><Reading /></el-icon>
        <span>图书借阅系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1f2d3d"
        text-color="#c0c4cc"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.name"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon :size="18"><Fold /></el-icon>
          <span class="header-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-tag :type="roleTagType" size="small" effect="dark">{{ roleLabel }}</el-tag>
          <el-dropdown @command="onCommand">
            <span class="user-trigger">
              <el-avatar :size="30">{{ avatarText }}</el-avatar>
              <span class="user-name">{{ store.displayName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人信息
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog v-model="profileVisible" title="个人信息" width="420px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ store.user?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ store.user?.fullName }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ store.user?.email }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">
          {{ formatTime(store.user?.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const route = useRoute()
const router = useRouter()
const profileVisible = ref(false)

const allMenus = [
  { name: 'dashboard', path: '/dashboard', title: '首页概览', icon: 'Odometer' },
  { name: 'books', path: '/books', title: '图书管理', icon: 'Reading' },
  { name: 'categories', path: '/categories', title: '分类管理', icon: 'Collection', roles: ['ADMIN', 'LIBRARIAN'] },
  { name: 'my-borrows', path: '/borrows', title: '我的借阅', icon: 'Tickets' },
  { name: 'borrow-records', path: '/borrow-records', title: '借阅记录', icon: 'List', roles: ['ADMIN', 'LIBRARIAN'] },
  { name: 'users', path: '/users', title: '用户管理', icon: 'User', roles: ['ADMIN'] }
]

const menus = computed(() =>
  allMenus.filter((m) => !m.roles || m.roles.includes(store.role))
)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')
const avatarText = computed(() => (store.displayName || '?').charAt(0).toUpperCase())

const roleLabel = computed(
  () => ({ ADMIN: '管理员', LIBRARIAN: '图书管理员', MEMBER: '读者' }[store.role] || store.role)
)
const roleTagType = computed(
  () => ({ ADMIN: 'danger', LIBRARIAN: 'warning', MEMBER: 'success' }[store.role] || 'info')
)

function formatTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

async function onCommand(command) {
  if (command === 'profile') {
    try {
      await store.fetchMe()
    } catch (e) {
      /* 忽略 */
    }
    profileVisible.value = true
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    } catch {
      return
    }
    store.logout()
    ElMessage.success('已退出登录')
    router.push({ name: 'login' })
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #1f2d3d;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 18px;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.aside :deep(.el-menu) {
  border-right: none;
  flex: 1;
}

.header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

.user-name {
  font-size: 14px;
  color: #303133;
}

.main {
  padding: 0;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
