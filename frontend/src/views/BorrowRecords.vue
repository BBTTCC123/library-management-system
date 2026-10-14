<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">借阅记录</h3>
      <el-button @click="load"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.userId"
          placeholder="按用户 ID 筛选"
          clearable
          style="width: 170px"
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-input
          v-model="query.bookId"
          placeholder="按图书 ID 筛选"
          clearable
          style="width: 170px"
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px" @change="reload">
          <el-option label="借阅中" value="BORROWED" />
          <el-option label="逾期" value="OVERDUE" />
          <el-option label="已归还" value="RETURNED" />
        </el-select>
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bookTitle" label="书名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="借阅人" width="120" />
        <el-table-column prop="borrowDate" label="借出" width="115" />
        <el-table-column prop="dueDate" label="应还" width="115" />
        <el-table-column prop="returnDate" label="归还" width="115">
          <template #default="{ row }">{{ row.returnDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small">{{ statusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借" width="70" align="center" />
        <el-table-column label="罚金" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.fineAmount > 0 ? '#f56c6c' : '#909399' }">
              ￥{{ Number(row.fineAmount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'RETURNED'"
              size="small"
              type="success"
              @click="handleReturn(row)"
            >
              代还
            </el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="load"
          @size-change="reload"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBorrows, returnBook } from '@/api/borrow'

const records = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const query = reactive({ userId: '', bookId: '', status: '' })

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

async function load() {
  loading.value = true
  try {
    const res = await getBorrows({
      page: page.value - 1,
      size: size.value,
      sort: 'id,desc',
      userId: query.userId || undefined,
      bookId: query.bookId || undefined,
      status: query.status || undefined
    })
    records.value = res.data.content
    total.value = res.data.totalElements
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function resetQuery() {
  query.userId = ''
  query.bookId = ''
  query.status = ''
  reload()
}

async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(`确定代「${row.username}」归还《${row.bookTitle}》吗？`, '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await returnBook(row.id)
    ElMessage.success('归还成功')
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(load)
</script>
