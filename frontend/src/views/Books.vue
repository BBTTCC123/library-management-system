<template>
  <div class="page-container">
    <div class="page-header">
      <h3 class="page-title">图书管理</h3>
      <el-button v-if="store.canManageLibrary" type="primary" @click="openCreate">
        <el-icon><Plus /></el-icon> 新增图书
      </el-button>
    </div>

    <el-card shadow="never">
      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索书名 / 作者 / ISBN"
          clearable
          style="width: 260px"
          :prefix-icon="Search"
          @keyup.enter="reload"
          @clear="reload"
        />
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px" @change="reload">
          <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-checkbox v-model="query.availableOnly" @change="reload">仅看有库存</el-checkbox>
        <el-button type="primary" @click="reload">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="books" stripe>
        <el-table-column prop="isbn" label="ISBN" width="150" />
        <el-table-column label="书名" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="book-title">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="作者" width="150" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />
        <el-table-column label="分类" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.categoryName" size="small" effect="plain">{{ row.categoryName }}</el-tag>
            <span v-else class="text-muted">未分类</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.availableCopies > 0 ? 'success' : 'danger'" size="small">
              {{ row.availableCopies }} / {{ row.totalCopies }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :disabled="row.availableCopies <= 0"
              @click="handleBorrow(row)"
            >
              借阅
            </el-button>
            <template v-if="store.canManageLibrary">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑图书' : '新增图书'" width="620px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="如 9787111213826" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社">
              <el-input v-model="form.publisher" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版年份">
              <el-input-number v-model="form.publishYear" :min="1000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类">
              <el-select v-model="form.categoryId" placeholder="请选择" clearable style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="馆藏总量" prop="totalCopies">
              <el-input-number v-model="form.totalCopies" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="简介">
              <el-input v-model="form.description" type="textarea" :rows="3" maxlength="1000" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getBooks, createBook, updateBook, deleteBook } from '@/api/book'
import { getCategories } from '@/api/category'
import { borrowBook } from '@/api/borrow'
import { useUserStore } from '@/stores/user'

const store = useUserStore()

const books = ref([])
const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const page = ref(1)
const size = ref(10)
const total = ref(0)

const query = reactive({ keyword: '', categoryId: null, availableOnly: false })

const emptyForm = () => ({
  id: null,
  isbn: '',
  title: '',
  author: '',
  publisher: '',
  publishYear: new Date().getFullYear(),
  categoryId: null,
  totalCopies: 1,
  description: ''
})
const form = reactive(emptyForm())

const rules = {
  isbn: [{ required: true, message: '请输入 ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  totalCopies: [{ required: true, message: '请输入馆藏总量', trigger: 'change' }]
}

async function load() {
  loading.value = true
  try {
    const res = await getBooks({
      page: page.value - 1,
      size: size.value,
      sort: 'id,desc',
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      availableOnly: query.availableOnly || undefined
    })
    books.value = res.data.content
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
  query.keyword = ''
  query.categoryId = null
  query.availableOnly = false
  reload()
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, {
    id: row.id,
    isbn: row.isbn,
    title: row.title,
    author: row.author,
    publisher: row.publisher,
    publishYear: row.publishYear,
    categoryId: row.categoryId,
    totalCopies: row.totalCopies,
    description: row.description
  })
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    delete payload.id
    if (form.id) {
      await updateBook(form.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createBook(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } catch (e) {
    /* 拦截器已提示 */
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除《${row.title}》吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteBook(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

async function handleBorrow(row) {
  try {
    await ElMessageBox.confirm(`确定借阅《${row.title}》吗？`, '借阅确认', { type: 'info' })
  } catch {
    return
  }
  try {
    await borrowBook(row.id)
    ElMessage.success('借阅成功')
    load()
  } catch (e) {
    /* 拦截器已提示 */
  }
}

onMounted(async () => {
  load()
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (e) {
    /* 忽略 */
  }
})
</script>
