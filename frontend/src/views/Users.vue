<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">用户管理</h3>
      <el-button @click="load"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <el-card shadow="never">
      <el-table v-loading="loading" :data="users" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="fullName" label="姓名" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="角色" width="150">
          <template #default="{ row }">
            <el-select
              :model-value="row.role"
              size="small"
              :disabled="row.id === store.user?.id"
              @change="(val) => handleRoleChange(row, val)"
            >
              <el-option label="管理员" value="ADMIN" />
              <el-option label="图书管理员" value="LIBRARIAN" />
              <el-option label="读者" value="MEMBER" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '正常' : '已禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :type="row.enabled ? 'danger' : 'success'"
              :disabled="row.id === store.user?.id"
              @click="handleToggle(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="reload"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, updateUserRole, updateUserEnabled } from '@/api/user'
import { useUserStore } from '@/stores/user'

const store = useUserStore()
const users = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await getUsers({ page: page.value - 1, size: size.value, sort: 'id,asc' })
    users.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

async function handleRoleChange(row, role) {
  try {
    await ElMessageBox.confirm(`确定将「${row.username}」的角色改为「${roleLabel(role)}」吗？`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await updateUserRole(row.id, role)
    ElMessage.success('角色更新成功')
    load()
  } catch (e) {
    load()
  }
}

async function handleToggle(row) {
  try {
    await updateUserEnabled(row.id, !row.enabled)
    ElMessage.success(row.enabled ? '已禁用该用户' : '已启用该用户')
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

function roleLabel(role) {
  return { ADMIN: '管理员', LIBRARIAN: '图书管理员', MEMBER: '读者' }[role] || role
}

onMounted(load)
</script>
