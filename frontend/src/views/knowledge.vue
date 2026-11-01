<template>
  <div class="knowledge-index">
    <PageHead
      title="文章资料索引"
      kicker="ARTICLE ARCHIVE"
      subtitle="在目录中检索文章；新增、修改与状态操作统一进入文章资料页。"
    >
      <template #buttons>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新增文章资料
        </el-button>
      </template>
    </PageHead>

    <TableSearch title="文章档案检索" :formItem="formItem" @search="handleSearch" />

    <div class="table-caption">
      <div>
        <span>ARTICLE INDEX</span>
        <strong>共 {{ pagination.total }} 份资料</strong>
      </div>
      <p>状态调整与删除请进入“修改”页处理</p>
    </div>

    <el-table :data="tableData" empty-text="暂无文章资料" style="width: 100%">
      <el-table-column label="文章标题" min-width="330" fixed="left">
        <template #default="scope">
          <div class="article-title-cell">
            <span class="file-dot" aria-hidden="true"></span>
            <div>
              <strong>{{ scope.row.title }}</strong>
              <small>ID · {{ scope.row.id }}</small>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="分类" min-width="150">
        <template #default="scope">
          {{ categoryMap[scope.row.categoryId] || '未分类' }}
        </template>
      </el-table-column>

      <el-table-column prop="authorName" label="作者" min-width="110" />

      <el-table-column label="状态" width="105">
        <template #default="scope">
          <span class="status-stamp" :class="`status-${scope.row.status}`">
            {{ getStatusText(scope.row.status) }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="readCount" label="阅读量" width="100" />
      <el-table-column label="更新时间" min-width="165">
        <template #default="scope">
          {{ scope.row.updatedAt || scope.row.createdAt || '-' }}
        </template>
      </el-table-column>

      <el-table-column label="资料操作" width="120" fixed="right">
        <template #default="scope">
          <el-button text type="primary" @click="handleEdit(scope.row)">
            修改资料
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-row">
      <span>PAGE {{ String(pagination.currentPage).padStart(2, '0') }}</span>
      <el-pagination
        :current-page="pagination.currentPage"
        :page-size="pagination.size"
        layout="prev, pager, next"
        :total="pagination.total"
        @current-change="handleChange"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import PageHead from '@/components/PageHead.vue'
import TableSearch from '@/components/TableSearch.vue'
import { categoryTree, articlePage } from '@/api/admin'

const router = useRouter()

const formItem = reactive([
  { comp: 'input', prop: 'title', label: '文章标题', placeholder: '输入标题关键词' },
  { comp: 'select', prop: 'categoryId', label: '所属分类', placeholder: '选择文章分类', options: [] },
  {
    comp: 'select',
    prop: 'status',
    label: '资料状态',
    placeholder: '选择发布状态',
    options: [
      { label: '草稿', value: '0' },
      { label: '已发布', value: '1' },
      { label: '已下线', value: '2' }
    ]
  }
])

const pagination = reactive({
  currentPage: 1,
  size: 10,
  total: 0
})

const categoryMap = reactive({})
const tableData = ref([])
const lastSearch = ref({})

const getStatusText = (status) => ({
  0: '草稿',
  1: '已发布',
  2: '已下线'
})[status] || '未知'

const handleSearch = async (formData = lastSearch.value) => {
  lastSearch.value = { ...formData }
  const { records = [], total = 0 } = await articlePage({
    currentPage: pagination.currentPage,
    size: pagination.size,
    ...lastSearch.value
  })

  tableData.value = records
  pagination.total = total
}

const handleChange = (page) => {
  pagination.currentPage = page
  handleSearch(lastSearch.value)
}

const handleCreate = () => router.push('/back/knowledge/new')
const handleEdit = (row) => router.push(`/back/knowledge/${row.id}/edit`)

onMounted(async () => {
  const data = await categoryTree()
  formItem[1].options = data.map(item => {
    categoryMap[item.id] = item.categoryName
    return {
      label: item.categoryName,
      value: item.id
    }
  })

  handleSearch({})
})
</script>

<style lang="scss" scoped>
.table-caption {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  margin: 0 3px 12px;

  > div {
    display: flex;
    flex-direction: column;
    gap: 3px;
  }

  span,
  p {
    color: #74675d;
    font-family: 'Courier New', 'Microsoft YaHei', monospace;
    font-size: 9px;
    letter-spacing: 0.08em;
  }

  strong {
    color: #352e29;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-size: 14px;
  }

  p {
    margin: 0;
  }
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 11px;

  .file-dot {
    width: 12px;
    height: 12px;
    flex: 0 0 auto;
    border: 1.5px solid #8f3d32;
    border-radius: 50%;
    background: rgba(143, 61, 50, 0.12);
  }

  > div {
    min-width: 0;
    display: flex;
    flex-direction: column;
  }

  strong {
    overflow: hidden;
    color: #352e29;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  small {
    margin-top: 3px;
    color: #8a7c70;
    font-family: 'Courier New', monospace;
    font-size: 8px;
  }
}

.status-stamp {
  display: inline-flex;
  padding: 4px 8px 3px;
  border: 1px solid currentColor;
  border-radius: 2px;
  font-size: 11px;
  font-weight: 800;
  transform: rotate(-1deg);

  &.status-0 {
    color: #74675d;
  }

  &.status-1 {
    color: #41654b;
  }

  &.status-2 {
    color: #8f3d32;
  }
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 22px;

  > span {
    color: #74675d;
    font-family: 'Courier New', monospace;
    font-size: 9px;
    font-weight: 700;
    letter-spacing: 0.12em;
  }
}

@media (max-width: 720px) {
  .table-caption p,
  .pagination-row > span {
    display: none;
  }

  .pagination-row {
    justify-content: flex-end;
  }
}
</style>
