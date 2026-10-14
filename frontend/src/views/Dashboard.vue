<template>
  <div class="page-container">
    <div class="welcome">
      <div>
        <h2>你好，{{ store.displayName }} 👋</h2>
        <p class="text-muted">
          当前角色：{{ roleLabel }} · {{ today }}
        </p>
      </div>
      <el-icon :size="72" color="#dbeafe"><Reading /></el-icon>
    </div>

    <div class="stat-grid" v-loading="loading">
      <el-card v-for="card in cards" :key="card.label" class="stat-card" shadow="hover">
        <div style="display: flex; justify-content: space-between; align-items: center">
          <div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
          </div>
          <el-icon class="stat-icon" :color="card.color"><component :is="card.icon" /></el-icon>
        </div>
      </el-card>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>我的借阅概况</span>
              <el-button link type="primary" @click="$router.push('/borrows')">查看全部</el-button>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="在借">{{ myStats.active }}</el-descriptions-item>
            <el-descriptions-item label="已归还">{{ myStats.returned }}</el-descriptions-item>
            <el-descriptions-item label="逾期">{{ myStats.overdue }}</el-descriptions-item>
          </el-descriptions>

          <el-table :data="myRecent" size="small" style="margin-top: 16px" empty-text="暂无借阅记录">
            <el-table-column prop="bookTitle" label="书名" show-overflow-tooltip />
            <el-table-column prop="dueDate" label="应还日期" width="120" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusType(row)" size="small">{{ statusLabel(row) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-header"><span>快捷操作</span></div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain @click="$router.push('/books')">
              <el-icon><Search /></el-icon> 浏览图书
            </el-button>
            <el-button type="success" plain @click="$router.push('/borrows')">
              <el-icon><Tickets /></el-icon> 我的借阅
            </el-button>
            <el-button v-if="store.canManageLibrary" type="warning" plain @click="$router.push('/categories')">
              <el-icon><Collection /></el-icon> 分类管理
            </el-button>
            <el-button v-if="store.isAdmin" type="danger" plain @click="$router.push('/users')">
              <el-icon><User /></el-icon> 用户管理
            </el-button>
          </div>

          <el-divider />

          <div class="text-muted">
            <p style="margin: 0 0 6px">借阅规则</p>
            <ul class="rules">
              <li>每人最多同时借阅 5 本</li>
              <li>借阅期限 30 天，可续借 1 次（+15 天）</li>
              <li>逾期每天罚金 0.5 元</li>
            </ul>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getStats, getMyBorrows } from '@/api/borrow'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const loading = ref(false)
const stats = reactive({
  totalBooks: 0,
  totalCategories: 0,
  totalUsers: 0,
  activeBorrows: 0,
  overdueBorrows: 0,
  returnedBorrows: 0
})
const myRecent = ref([])
const myStats = reactive({ active: 0, returned: 0, overdue: 0 })

const roleLabel = computed(
  () => ({ ADMIN: '管理员', LIBRARIAN: '图书管理员', MEMBER: '读者' }[store.role] || store.role)
)
const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const cards = computed(() => [
  { label: '图书总数', value: stats.totalBooks, icon: 'Reading', color: '#409eff' },
  { label: '分类数量', value: stats.totalCategories, icon: 'Collection', color: '#67c23a' },
  { label: '用户数量', value: stats.totalUsers, icon: 'User', color: '#e6a23c' },
  { label: '在借记录', value: stats.activeBorrows, icon: 'Tickets', color: '#909399' },
  { label: '逾期记录', value: stats.overdueBorrows, icon: 'Warning', color: '#f56c6c' },
  { label: '累计归还', value: stats.returnedBorrows, icon: 'CircleCheck', color: '#67c23a' }
])

function statusType(row) {
  if (row.status === 'RETURNED') return 'info'
  if (row.status === 'OVERDUE' || row.overdueDays > 0) return 'danger'
  return 'success'
}

function statusLabel(row) {
  if (row.status === 'RETURNED') return '已归还'
  if (row.status === 'OVERDUE' || row.overdueDays > 0) return '逾期'
  return '借阅中'
}

onMounted(async () => {
  loading.value = true
  try {
    const [s, mine] = await Promise.all([
      getStats(),
      getMyBorrows({ page: 0, size: 5, sort: 'id,desc' })
    ])
    Object.assign(stats, s.data)
    myRecent.value = mine.data.content
    myStats.active = mine.data.content.filter((r) => r.status !== 'RETURNED').length
    myStats.overdue = mine.data.content.filter(
      (r) => r.status === 'OVERDUE' || r.overdueDays > 0
    ).length
    myStats.returned = mine.data.content.filter((r) => r.status === 'RETURNED').length
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: 10px;
  padding: 20px 24px;
  margin-bottom: 20px;
}

.welcome h2 {
  margin: 0 0 6px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-actions .el-button {
  margin-left: 0;
}

.rules {
  margin: 0;
  padding-left: 18px;
}
</style>
