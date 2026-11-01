<template>
  <div class="emotion-detail-page" v-loading="loading">
    <PageHead
      title="情绪日志资料"
      kicker="EMOTION RECORD"
      subtitle="个人记录、生活指标与 AI 分析已整理为独立资料页。"
    >
      <template #buttons>
        <el-button @click="goBack"><el-icon><Back /></el-icon>返回情绪目录</el-button>
      </template>
    </PageHead>

    <template v-if="currentDetail">
      <div class="record-banner">
        <div><span>RECORD NO.</span><strong>{{ currentDetail.id }}</strong></div>
        <div class="record-status"><span>FILE DATE</span><strong>{{ currentDetail.diaryDate || '-' }}</strong></div>
      </div>

      <section class="detail-sheet">
        <header class="section-heading">
          <span class="section-number">01</span>
          <div><p>PERSONAL INFORMATION</p><h3>用户与情绪资料</h3></div>
        </header>

        <div class="info-grid">
          <dl><dt>用户名</dt><dd>{{ currentDetail.username || '-' }}</dd></dl>
          <dl><dt>昵称</dt><dd>{{ currentDetail.nickname || '-' }}</dd></dl>
          <dl><dt>用户ID</dt><dd>{{ currentDetail.userId || '-' }}</dd></dl>
          <dl><dt>记录日期</dt><dd>{{ currentDetail.diaryDate || '-' }}</dd></dl>
          <dl class="score-cell">
            <dt>情绪评分</dt>
            <dd><el-rate :model-value="Number(currentDetail.moodScore || 0)" :max="10" disabled /></dd>
          </dl>
          <dl><dt>主要情绪</dt><dd><el-tag :type="emotionTagType(currentDetail.dominantEmotion)">{{ currentDetail.dominantEmotion || '-' }}</el-tag></dd></dl>
          <dl><dt>睡眠质量</dt><dd>{{ currentDetail.sleepQuality ?? '-' }} / 5</dd></dl>
          <dl><dt>压力水平</dt><dd>{{ currentDetail.stressLevel ?? '-' }} / 5</dd></dl>
        </div>
      </section>

      <section class="detail-sheet">
        <header class="section-heading">
          <span class="section-number">02</span>
          <div><p>DIARY CONTENT</p><h3>日记资料</h3></div>
        </header>

        <article class="text-record">
          <span>情绪触发因素</span>
          <p>{{ currentDetail.emotionTriggers || '无' }}</p>
        </article>
        <article class="text-record">
          <span>日记内容</span>
          <p>{{ currentDetail.diaryContent || '无' }}</p>
        </article>
      </section>

      <section class="detail-sheet">
        <header class="section-heading">
          <span class="section-number">03</span>
          <div><p>AI ANALYSIS</p><h3>AI 情绪分析</h3></div>
        </header>

        <div class="analysis-grid">
          <div class="analysis-field">
            <span>主要情绪</span>
            <el-tag :type="emotionTagType(aiData.primaryEmotion)">{{ aiData.primaryEmotion || '未分析' }}</el-tag>
          </div>
          <div class="analysis-field">
            <span>风险等级</span>
            <el-tag :type="riskTagType(aiData.riskLevel)">{{ riskText(aiData.riskLevel) }}</el-tag>
          </div>
          <div class="analysis-field">
            <span>情绪性质</span>
            <el-tag :type="aiData.isNegative ? 'danger' : 'success'">{{ aiData.isNegative ? '负面情绪' : '正面情绪' }}</el-tag>
          </div>
          <div class="analysis-field score-progress">
            <span>情绪强度</span>
            <el-progress :percentage="emotionPercentage" :stroke-width="8" :color="emotionScoreColor" />
          </div>
        </div>

        <article class="analysis-note">
          <h4>专业建议</h4>
          <p>{{ aiData.suggestion || '无' }}</p>
        </article>
        <article class="analysis-note">
          <h4>风险描述</h4>
          <p>{{ aiData.riskDescription || '无' }}</p>
        </article>
        <article class="analysis-note">
          <h4>改善建议</h4>
          <ul v-if="improvementSuggestions.length">
            <li v-for="(item, index) in improvementSuggestions" :key="`${index}-${item}`">{{ item }}</li>
          </ul>
          <p v-else>无</p>
        </article>
      </section>

      <section class="detail-sheet disposal-sheet">
        <header class="section-heading">
          <span class="section-number">04</span>
          <div><p>RECORD MANAGEMENT</p><h3>资料处置</h3></div>
        </header>
        <div class="disposal-row">
          <div>
            <strong>删除情绪日志</strong>
            <p>删除后将无法从管理目录恢复这份资料。</p>
          </div>
          <el-button class="delete-button" :loading="deleting" @click="handleDelete">删除资料</el-button>
        </div>
      </section>
    </template>

    <el-empty v-else-if="!loading" description="未找到这份情绪日志资料">
      <el-button @click="goBack">返回情绪目录</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHead from '@/components/PageHead.vue'
import { deleteEmotional, getEmotionalPage } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const currentDetail = ref(window.history.state?.record ? { ...window.history.state.record } : null)
const loading = ref(false)
const deleting = ref(false)

const aiData = computed(() => {
  const raw = currentDetail.value?.aiEmotionAnalysis
  if (!raw) return {}
  if (typeof raw === 'object') return raw
  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
})

const improvementSuggestions = computed(() => {
  const suggestions = aiData.value.improvementSuggestions
  if (Array.isArray(suggestions)) return suggestions.filter(Boolean)
  return suggestions ? [suggestions] : []
})

const emotionPercentage = computed(() => {
  const rawScore = Number(aiData.value.emotionScore || 0)
  const normalized = rawScore > 0 && rawScore <= 10 ? rawScore * 10 : rawScore
  return Math.min(100, Math.max(0, normalized))
})

const emotionScoreColor = computed(() => {
  if (emotionPercentage.value >= 80) return '#8f3d32'
  if (emotionPercentage.value >= 60) return '#a77433'
  if (emotionPercentage.value >= 40) return '#8f7b56'
  return '#587158'
})

const emotionTagType = (emotion) => ({
  快乐: 'success', 平静: 'success', 兴奋: 'warning', 满足: 'success',
  愤怒: 'danger', 悲伤: 'info', 焦虑: 'warning', 恐惧: 'danger',
  沮丧: 'info', 压力: 'warning'
})[emotion] || 'info'

const riskTagType = (level) => {
  const key = String(level ?? '').toLowerCase()
  if (['3', '高', '高风险', 'high', '危机'].includes(key)) return 'danger'
  if (['2', '中', '中风险', 'medium', '预警'].includes(key)) return 'warning'
  if (['1', '低', '低风险', 'low', '关注'].includes(key)) return 'info'
  return 'success'
}

const riskText = (level) => {
  const map = { 0: '正常', 1: '关注', 2: '预警', 3: '危机' }
  return map[level] || level || '未标注'
}

const loadRecordFallback = async () => {
  if (currentDetail.value) return
  loading.value = true
  try {
    const data = await getEmotionalPage({ current: 1, size: 100, id: route.params.id })
    const records = data?.records || []
    currentDetail.value = records.find(item => String(item.id) === String(route.params.id)) || null
  } finally {
    loading.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('删除后将无法在情绪目录中找到这份资料，确认继续吗？', '删除情绪资料', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'archive-message-box',
      confirmButtonClass: 'archive-delete-confirm'
    })
  } catch {
    return
  }

  deleting.value = true
  try {
    await deleteEmotional(route.params.id)
    ElMessage.success('情绪资料已删除')
    router.push('/back/emotional')
  } finally {
    deleting.value = false
  }
}

const goBack = () => router.push('/back/emotional')

onMounted(loadRecordFallback)
</script>

<style lang="scss" scoped>
.record-banner {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 22px;
  padding: 14px 17px;
  border: 1px dashed rgba(53, 46, 41, 0.58);
  background: rgba(189, 169, 130, 0.16);

  > div {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  span {
    color: #74675d;
    font: 700 8px 'Courier New', monospace;
    letter-spacing: 0.12em;
  }

  strong {
    font-size: 13px;
  }
}

.record-status {
  align-items: flex-end;
}

.detail-sheet {
  box-sizing: border-box;
  margin-bottom: 22px;
  padding: 22px;
  border: 1.5px solid rgba(53, 46, 41, 0.64);
  border-radius: 3px 7px 4px 8px;
  background: rgba(255, 253, 248, 0.42);
  box-shadow: 3px 4px 0 rgba(53, 46, 41, 0.07);
}

.section-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(53, 46, 41, 0.35);

  p {
    margin: 0 0 2px;
    color: #8f3d32;
    font: 700 8px 'Courier New', monospace;
    letter-spacing: 0.12em;
  }

  h3 {
    margin: 0;
    color: #352e29;
    font: 800 19px 'Noto Serif SC', 'Songti SC', SimSun, serif;
  }
}

.section-number {
  width: 36px;
  height: 36px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: #8f3d32;
  border: 1px solid #8f3d32;
  border-radius: 50%;
  font: 700 11px 'Courier New', monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border-top: 1px solid rgba(53, 46, 41, 0.27);
  border-left: 1px solid rgba(53, 46, 41, 0.27);

  dl {
    min-width: 0;
    border-right: 1px solid rgba(53, 46, 41, 0.27);
    border-bottom: 1px solid rgba(53, 46, 41, 0.27);
  }

  dt {
    padding: 8px 10px;
    color: #74675d;
    background: rgba(189, 169, 130, 0.2);
    font-size: 10px;
    font-weight: 800;
  }

  dd {
    min-height: 24px;
    padding: 11px 10px;
    color: #352e29;
    font-size: 13px;
    overflow-wrap: anywhere;
  }
}

.score-cell {
  grid-column: span 2;
}

.text-record {
  padding: 14px 16px;
  border-left: 4px solid #8f7b56;
  background: rgba(239, 227, 204, 0.3);

  + .text-record {
    margin-top: 14px;
  }

  span {
    display: block;
    margin-bottom: 7px;
    color: #74675d;
    font-size: 10px;
    font-weight: 800;
  }

  p {
    margin: 0;
    color: #352e29;
    font-size: 13px;
    line-height: 1.8;
    white-space: pre-wrap;
  }
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 15px;
}

.analysis-field {
  min-height: 70px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  gap: 9px;
  padding: 11px 13px;
  border: 1px solid rgba(53, 46, 41, 0.25);
  background: rgba(189, 169, 130, 0.13);

  > span {
    color: #74675d;
    font-size: 10px;
    font-weight: 800;
  }
}

.score-progress {
  :deep(.el-progress) {
    width: 100%;
  }
}

.analysis-note {
  margin-top: 12px;
  padding: 14px 16px;
  border: 1px dashed rgba(53, 46, 41, 0.45);
  background: rgba(255, 255, 255, 0.25);

  h4 {
    margin: 0 0 8px;
    color: #8f3d32;
    font: 800 14px 'Noto Serif SC', 'Songti SC', SimSun, serif;
  }

  p,
  li {
    color: #51483f;
    font-size: 13px;
    line-height: 1.8;
  }

  ul {
    margin: 0;
    padding-left: 19px;
  }

  li {
    list-style: square;
  }
}

.disposal-sheet {
  border-color: rgba(81, 70, 52, 0.72);
}

.disposal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;

  strong {
    color: #352e29;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  }

  p {
    margin: 5px 0 0;
    color: #74675d;
    font-size: 12px;
  }
}

.delete-button {
  color: #fffaf0;
  border-color: #514634;
  background: #8f7b56;

  &:hover,
  &:focus {
    color: #fffaf0;
    border-color: #352e29;
    background: #766442;
  }
}

@media (max-width: 1000px) {
  .info-grid,
  .analysis-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .record-banner,
  .disposal-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .record-status {
    align-items: flex-start;
  }

  .detail-sheet {
    padding: 16px;
  }

  .info-grid,
  .analysis-grid {
    grid-template-columns: 1fr;
  }

  .score-cell {
    grid-column: auto;
  }
}
</style>
