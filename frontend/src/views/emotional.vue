<template>
  <div class="emotion-index">
    <PageHead
      title="情绪日志索引"
      kicker="EMOTION ARCHIVE"
      subtitle="检索个人情绪记录与对应的 AI 分析资料。"
    />

    <TableSearch title="情绪资料检索" :form-item="formItem" @search="handleSearch" />

    <el-table v-loading="loading" :data="tableData" style="width: 100%">
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column label="用户昵称" min-width="120">
        <template #default="scope">
          <span class="plain-name">{{ scope.row.nickname || scope.row.username || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="diaryDate" label="记录日期" width="120" />
      <el-table-column label="情绪评分" min-width="210">
        <template #default="scope">
          <el-rate :model-value="Number(scope.row.moodScore || 0)" :max="10" disabled />
        </template>
      </el-table-column>
      <el-table-column label="生活指标" width="130">
        <template #default="scope">
          <div class="life-index">
            <span>睡眠：{{ scope.row.sleepQuality ?? '-' }} / 5</span>
            <span>压力：{{ scope.row.stressLevel ?? '-' }} / 5</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="emotionTriggers" label="情绪触发因素" min-width="150" show-overflow-tooltip />
      <el-table-column prop="diaryContent" label="日记内容" min-width="230" show-overflow-tooltip />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="scope">
          <el-button type="primary" text @click="viewEmotionDetail(scope.row)">查看资料</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="archive-pagination"
      :current-page="pagination.current"
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
import TableSearch from '@/components/TableSearch.vue'
import { getEmotionalPage } from '@/api/admin'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const activeFilters = ref({})

const formItem = [
  { comp: 'input', prop: 'userId', label: '用户ID', placeholder: '请输入用户ID' },
  {
    comp: 'select',
    prop: 'moodScreRange',
    label: '情绪评分',
    placeholder: '请选择评分范围',
    options: [
      { label: '低分（1-3）', value: '1-3' },
      { label: '中分（4-6）', value: '4-6' },
      { label: '高分（7-10）', value: '7-10' }
    ]
  }
]

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const fetchRecords = async () => {
  loading.value = true
  try {
    const { records = [], total = 0 } = await getEmotionalPage({
      ...pagination,
      ...activeFilters.value
    })
    tableData.value = records
    pagination.total = total
  } finally {
    loading.value = false
  }
}

const handleSearch = (formData = {}) => {
  activeFilters.value = { ...formData }
  pagination.current = 1
  fetchRecords()
}

const handleChange = (page) => {
  pagination.current = page
  fetchRecords()
}

const viewEmotionDetail = (row) => {
  router.push({
    name: 'emotional-detail',
    params: { id: row.id },
    state: { record: JSON.parse(JSON.stringify(row)) }
  })
}

onMounted(fetchRecords)
</script>

<style lang="scss" scoped>
.plain-name {
  color: #352e29;
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-weight: 800;
}

.life-index {
  display: flex;
  flex-direction: column;
  gap: 5px;
  color: #51483f;
  font-size: 12px;
}

.archive-pagination {
  margin-top: 24px;
}
</style>
