<template>
  <div class="consultation-detail-page" v-loading="loading">
    <PageHead
      title="咨询会话资料"
      kicker="CONSULTATION RECORD"
      subtitle="会话概要与完整交流记录已整理为独立资料页。"
    >
      <template #buttons>
        <el-button @click="goBack"><el-icon><Back /></el-icon>返回咨询目录</el-button>
      </template>
    </PageHead>

    <div class="record-banner">
      <div>
        <span>SESSION NO.</span>
        <strong>{{ sessionInfo.id || route.params.id }}</strong>
      </div>
      <div class="record-status">
        <span>RECORD TYPE</span>
        <strong>AI 咨询会话</strong>
      </div>
    </div>

    <section class="detail-sheet">
      <header class="section-heading">
        <span class="section-number">01</span>
        <div><p>SESSION INFORMATION</p><h3>会话资料</h3></div>
      </header>

      <div class="info-grid">
        <dl><dt>用户昵称</dt><dd>{{ sessionInfo.userNickname || '-' }}</dd></dl>
        <dl><dt>会话标题</dt><dd>{{ sessionInfo.sessionTitle || '未命名会话' }}</dd></dl>
        <dl><dt>开始时间</dt><dd>{{ sessionInfo.startedAt || '-' }}</dd></dl>
        <dl><dt>最后记录时间</dt><dd>{{ sessionInfo.lastMessageTime || '-' }}</dd></dl>
        <dl><dt>消息数量</dt><dd>{{ sessionInfo.messageCount ?? sessionMessages.length }}</dd></dl>
        <dl><dt>会话编号</dt><dd class="mono-value">{{ sessionInfo.id || route.params.id }}</dd></dl>
      </div>
    </section>

    <section class="detail-sheet conversation-sheet">
      <header class="section-heading">
        <span class="section-number">02</span>
        <div><p>CONVERSATION TRANSCRIPT</p><h3>对话记录</h3></div>
        <span class="message-total">共 {{ sessionMessages.length }} 条</span>
      </header>

      <el-empty v-if="!loading && !sessionMessages.length" description="这份资料暂时没有对话记录" />
      <div v-else class="message-list">
        <article
          v-for="(message, index) in sessionMessages"
          :key="message.id || index"
          class="message-slip"
          :class="message.senderType === 1 ? 'user-message' : 'ai-message'"
        >
          <header>
            <span class="message-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <strong>{{ message.senderType === 1 ? '用户记录' : 'AI 回复' }}</strong>
            <time>{{ message.createdAt || '-' }}</time>
          </header>
          <p>{{ message.content || '（无文字内容）' }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHead from '@/components/PageHead.vue'
import { getSessionDetail } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const sessionInfo = reactive({ ...(window.history.state?.session || {}), id: window.history.state?.session?.id || route.params.id })
const sessionMessages = ref([])
const loading = ref(false)

const loadMessages = async () => {
  loading.value = true
  try {
    const data = await getSessionDetail(route.params.id)
    sessionMessages.value = Array.isArray(data) ? data : (data?.messages || data?.records || [])
  } finally {
    loading.value = false
  }
}

const goBack = () => router.push('/back/consultations')

onMounted(loadMessages)
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
    min-width: 0;
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
    overflow-wrap: anywhere;
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

.message-total {
  margin-left: auto;
  color: #74675d;
  font: 700 10px 'Courier New', 'Microsoft YaHei', monospace;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
    min-height: 20px;
    padding: 11px 10px;
    color: #352e29;
    font-size: 13px;
    overflow-wrap: anywhere;
  }
}

.mono-value {
  font-family: 'Courier New', 'Microsoft YaHei', monospace;
}

.message-list {
  display: grid;
  gap: 13px;
}

.message-slip {
  position: relative;
  padding: 15px 17px;
  border: 1px solid rgba(53, 46, 41, 0.36);
  border-left: 5px solid #8f7b56;
  border-radius: 2px 6px 3px 7px;
  background: rgba(239, 227, 204, 0.32);

  &.ai-message {
    border-left-color: #8f3d32;
    background: rgba(143, 61, 50, 0.045);
  }

  header {
    display: flex;
    align-items: center;
    gap: 9px;
    margin-bottom: 9px;
    color: #352e29;
  }

  time {
    margin-left: auto;
    color: #74675d;
    font: 11px 'Courier New', 'Microsoft YaHei', monospace;
  }

  p {
    margin: 0;
    color: #51483f;
    font-size: 13px;
    line-height: 1.8;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }
}

.message-index {
  color: #8f3d32;
  font: 700 10px 'Courier New', monospace;
}

@media (max-width: 880px) {
  .info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .record-banner {
    align-items: flex-start;
    flex-direction: column;
  }

  .record-status {
    align-items: flex-start;
  }

  .detail-sheet {
    padding: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .message-slip header {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .message-slip time {
    width: 100%;
    margin-left: 0;
  }
}
</style>
