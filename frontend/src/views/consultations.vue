<template>
  <div class="consultation-index">
    <PageHead
      title="咨询资料索引"
      kicker="CONSULTATION ARCHIVE"
      subtitle="按时间查阅用户与 AI 的咨询会话记录。"
    />

    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column label="会话ID" min-width="140">
        <template #default="scope">
          <span class="plain-id">{{ scope.row.id || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="userNickname" label="用户昵称" min-width="110" />
      <el-table-column label="会话资料" min-width="310">
        <template #default="scope">
          <div class="session-title">{{ scope.row.sessionTitle || '未命名会话' }}</div>
          <div class="session-preview">{{ scope.row.lastMessageContent || '暂无会话摘要' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="messageCount" label="消息数" width="90" />
      <el-table-column prop="lastMessageTime" label="最后记录时间" min-width="150" />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="scope">
          <el-button type="primary" text @click="viewSessionDetail(scope.row)">查看资料</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="archive-pagination"
      :current-page="pagination.currentPage"
      :page-size="pagination.size"
      layout="prev, pager, next"
      :total="pagination.total"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHead from '@/components/PageHead.vue'
import { getConsultationPage } from '@/api/admin'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)

const pagination = reactive({
  currentPage: 1,
  size: 10,
  total: 0
})

const viewSessionDetail = (row) => {
  router.push({
    name: 'consultation-detail',
    params: { id: row.id },
    state: { session: JSON.parse(JSON.stringify(row)) }
  })
}

const handleChange = (page) => {
  pagination.currentPage = page
  handleSearch()
}

const handleSearch = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await getConsultationPage(pagination)
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

onMounted(handleSearch)
</script>

<style lang="scss" scoped>
.plain-id {
  color: #352e29;
  font-family: 'Courier New', 'Microsoft YaHei', monospace;
  font-size: 12px;
  font-weight: 700;
  word-break: break-all;
}

.session-title {
  margin-bottom: 4px;
  color: #352e29;
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-weight: 800;
}

.session-preview {
  display: -webkit-box;
  overflow: hidden;
  color: #74675d;
  font-size: 12px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
}

.archive-pagination {
  margin-top: 24px;
}
</style>
