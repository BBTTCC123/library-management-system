<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">我的借阅</h3>
      <el-button @click="load"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-radio-group v-model="status" @change="reload">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="BORROWED">借阅中</el-radio-button>
          <el-radio-button label="OVERDUE">逾期</el-radio-button>
          <el-radio-button label="RETURNED">已归还</el-radio-button>
        </el-radio-group>
      </div>

      <el-table v-loading="loading" :data="records" stripe>
        <el-table-column prop="bookTitle" label="书名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="book-title">{{ row.bookTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column prop="borrowDate" label="借出日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />
        <el-table-column prop="returnDate" label="归还日期" width="120">
          <template #default="{ row }">{{ row.returnDate || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small">{{ statusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="罚金" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.fineAmount > 0 ? '#f56c6c' : '#909399' }">
              ￥{{ Number(row.fineAmount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status !== 'RETURNED'">
              <el-button size="small" type="primary" :disabled="row.renewCount >= 1 || row.overdueDays > 0" @click="handleRenew(row)">
                续借
              </el-button>
              <el-button size="small" type="success" @click="handleReturn(row)">归还</el-button>
            </template>
            <span v-else class="text-muted">已完成</span>
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
import { getMyBorrows, returnBook, renewBorrow } from '@/api/borrow'

const records = ref([])
const loading = ref(false)
const status = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

function statusType(row) {
  if (row.status === 'RETURNED') return 'info'
  if (row.status === 'OVERDUE' || row.overdueDays > 0) return 'danger'
  return 'success'
}

function statusLabel(row) {
  if (row.status === 'RETURNED') return '已归还'
  if (row.status === 'OVERDUE' || row.overdueDays > 0) return `逾期${row.overdueDays}天`
  return '借阅中'
}

async function load() {
  loading.value = true
  try {
    const res = await getMyBorrows({
      page: page.value - 1,
      size: size.value,
      sort: 'id,desc',
      status: status.value || undefined
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

async function handleReturn(row) {
  try {
    await ElMessageBox.confirm(`确定归还《${row.bookTitle}》吗？`, '归还确认', { type: 'info' })
  } catch {
    return
  }
  try {
    const res = await returnBook(row.id)
    if (res.data.fineAmount > 0) {
      ElMessage.warning(`归还成功，产生逾期罚金 ￥${res.data.fineAmount.toFixed(2)}`)
    } else {
      ElMessage.success('归还成功')
    }
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

async function handleRenew(row) {
  try {
    await renewBorrow(row.id)
    ElMessage.success('续借成功，应还日期已延长 15 天')
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(load)
</script>
