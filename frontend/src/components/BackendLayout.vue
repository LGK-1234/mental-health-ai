<template>
  <div class="backend-layout">
    <Sidebar />

    <main class="archive-desk">
      <section class="paper-document">
        <div class="file-tabs" aria-label="当前资料位置">
          <router-link class="section-tab" :to="sectionPath" :title="`返回${fileTitle}`">
            {{ fileTitle }}
          </router-link>
          <transition name="tab-pop">
            <span v-if="actionTitle" :key="actionTitle" class="action-tab">{{ actionTitle }}</span>
          </transition>
          <span class="record-code">BACK OFFICE / {{ routeCode }}</span>
        </div>

        <div class="document-meta">
          <span>PERSONAL ARCHIVE · ADMINISTRATIVE COPY</span>
          <span>{{ currentDate }}</span>
        </div>

        <router-view v-slot="{ Component, route: viewRoute }">
          <transition name="page-turn" mode="out-in">
            <component :is="Component" :key="viewRoute.fullPath" class="content-container" />
          </transition>
        </router-view>

        <footer class="document-footer">
          <span>CONFIDENTIAL</span>
          <span>内部资料 · 请妥善保管</span>
        </footer>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './Sidebar.vue'

const route = useRoute()

const fileTitle = computed(() => route.meta.fileTitle || route.meta.title || '管理资料')
const actionTitle = computed(() => route.meta.actionTitle || '')
const sectionPath = computed(() => route.meta.sectionPath || `/back/${route.path.split('/')[2] || 'dashboard'}`)
const routeCode = computed(() => route.path.replace('/back/', '').replaceAll('/', ' · ').toUpperCase())
const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
})
const currentDate = ref(dateFormatter.format(new Date()))
let dateTimer

const updateCurrentDate = () => {
  currentDate.value = dateFormatter.format(new Date())
}

onMounted(() => {
  dateTimer = window.setInterval(updateCurrentDate, 60 * 1000)
})

onBeforeUnmount(() => {
  window.clearInterval(dateTimer)
})
</script>

<style lang="scss" scoped>
.backend-layout {
  --ink: #352e29;
  --muted: #74675d;
  --paper: #fffaf0;
  --paper-deep: #efe3cc;
  --folder: #bda982;
  --accent: #8f3d32;
  height: 100vh;
  display: flex;
  color: var(--ink);
  overflow: hidden;
  background: #d7c8a9;
  user-select: none;
}

.archive-desk {
  box-sizing: border-box;
  min-width: 0;
  flex: 1;
  padding: 76px 42px 46px;
  overflow: auto;
  background-color: #ded2b9;
  background-image:
    linear-gradient(rgba(73, 59, 46, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(73, 59, 46, 0.025) 1px, transparent 1px),
    radial-gradient(rgba(73, 59, 46, 0.11) 0.65px, transparent 0.65px);
  background-size: 28px 28px, 28px 28px, 7px 7px;
}

.paper-document {
  position: relative;
  box-sizing: border-box;
  width: min(1480px, 100%);
  min-height: calc(100vh - 122px);
  margin: 0 auto;
  padding: 38px 34px 24px;
  border: 2px solid var(--ink);
  border-radius: 4px 9px 6px 10px;
  background-color: var(--paper);
  background-image:
    linear-gradient(rgba(143, 61, 50, 0.025) 1px, transparent 1px),
    radial-gradient(rgba(53, 46, 41, 0.065) 0.55px, transparent 0.55px);
  background-size: 100% 31px, 6px 6px;
  box-shadow: 10px 12px 0 rgba(53, 46, 41, 0.14);

  &::before {
    content: '';
    position: absolute;
    inset: 13px;
    z-index: 0;
    pointer-events: none;
    border: 1px solid rgba(53, 46, 41, 0.16);
  }
}

.file-tabs {
  position: absolute;
  top: -43px;
  left: 24px;
  right: 24px;
  z-index: 2;
  height: 43px;
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.section-tab,
.action-tab {
  box-sizing: border-box;
  height: 43px;
  display: inline-flex;
  align-items: center;
  padding: 0 25px;
  border: 2px solid var(--ink);
  border-bottom: 0;
  border-radius: 7px 10px 0 0;
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.08em;
  transform: rotate(-0.3deg);
}

.section-tab {
  color: #fffaf0;
  background: var(--accent);
  text-decoration: none;
  transition: transform 160ms ease, background-color 160ms ease, box-shadow 160ms ease;

  &:hover,
  &:focus-visible {
    color: #fffaf0;
    background: #773027;
    box-shadow: 3px -2px 0 rgba(53, 46, 41, 0.2);
    transform: translateY(-2px) rotate(-0.3deg);
  }
}

.action-tab {
  color: var(--ink);
  background: #e1cfaa;
  transform: rotate(0.45deg);
}

.record-code {
  align-self: center;
  margin: 6px 0 0 auto;
  color: rgba(53, 46, 41, 0.64);
  font-family: 'Courier New', monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.document-meta {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 25px;
  padding-bottom: 12px;
  color: var(--muted);
  border-bottom: 1px dashed rgba(53, 46, 41, 0.44);
  font-family: 'Courier New', 'Microsoft YaHei', monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.content-container {
  position: relative;
  z-index: 1;
  min-height: calc(100vh - 245px);
}

.document-footer {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 18px;
  margin-top: 30px;
  padding-top: 13px;
  color: var(--muted);
  border-top: 1px dashed rgba(53, 46, 41, 0.42);
  font-family: 'Courier New', 'Microsoft YaHei', monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

.page-turn-enter-active,
.page-turn-leave-active {
  transition: opacity 180ms ease, transform 220ms ease;
}

.page-turn-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-turn-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

.tab-pop-enter-active,
.tab-pop-leave-active {
  transition: opacity 180ms ease, transform 220ms ease;
}

.tab-pop-enter-from,
.tab-pop-leave-to {
  opacity: 0;
  transform: translateY(10px) rotate(0.45deg);
}

.paper-document {
  :deep(input),
  :deep(textarea),
  :deep([contenteditable='true']),
  :deep(.w-e-text-container),
  :deep(.w-e-text-container *) {
    user-select: text;
  }

  :deep(.el-card),
  :deep(.el-table),
  :deep(.el-table__expanded-cell) {
    color: var(--ink);
    border-color: rgba(53, 46, 41, 0.42);
    background: rgba(255, 253, 248, 0.48);
  }

  :deep(.el-card) {
    border: 1.5px solid rgba(53, 46, 41, 0.58);
    border-radius: 3px 7px 4px 8px;
    box-shadow: 3px 4px 0 rgba(53, 46, 41, 0.09);
  }

  :deep(.el-card__header) {
    color: var(--ink);
    border-bottom: 1px dashed rgba(53, 46, 41, 0.35);
    background: rgba(239, 227, 204, 0.38);
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-weight: 800;
  }

  :deep(.el-table) {
    --el-table-border-color: rgba(53, 46, 41, 0.2);
    --el-table-header-bg-color: rgba(189, 169, 130, 0.26);
    --el-table-row-hover-bg-color: rgba(143, 61, 50, 0.06);
    --el-table-current-row-bg-color: rgba(143, 61, 50, 0.08);
    --el-table-bg-color: transparent;
    --el-table-tr-bg-color: transparent;
    --el-fill-color-lighter: rgba(189, 169, 130, 0.16);
    border-top: 1.5px solid var(--ink);
    border-bottom: 1.5px solid var(--ink);
    font-size: 13px;
  }

  :deep(.el-table th.el-table__cell) {
    color: var(--ink);
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-weight: 800;
  }

  :deep(.el-table .el-table__cell) {
    padding: 13px 0;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.48);
    box-shadow: 0 0 0 1.25px rgba(53, 46, 41, 0.66) inset !important;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper:hover),
  :deep(.el-select__wrapper.is-focused) {
    box-shadow: 0 0 0 2px var(--accent) inset !important;
  }

  :deep(.el-textarea__inner) {
    color: var(--ink);
    border: 1.25px solid rgba(53, 46, 41, 0.66);
    border-radius: 2px;
    background: rgba(255, 255, 255, 0.48);
    box-shadow: none;
  }

  :deep(.el-button) {
    border-radius: 2px 5px 3px 6px;
    font-weight: 700;
  }

  :deep(.el-button--primary) {
    color: #fffaf0;
    border-color: var(--ink);
    background: var(--accent);
    box-shadow: 2px 2px 0 var(--ink);
  }

  :deep(.el-button--primary:hover) {
    background: #773027;
    transform: translate(-1px, -1px);
    box-shadow: 3px 3px 0 var(--ink);
  }

  :deep(.el-pagination) {
    --el-pagination-button-bg-color: rgba(239, 227, 204, 0.65);
    --el-pagination-hover-color: var(--accent);
    justify-content: flex-end;
  }
}

@media (prefers-reduced-motion: reduce) {
  .page-turn-enter-active,
  .page-turn-leave-active,
  .tab-pop-enter-active,
  .tab-pop-leave-active {
    transition-duration: 1ms;
  }
}

@media (max-width: 900px) {
  .archive-desk {
    padding: 68px 18px 28px;
  }

  .paper-document {
    padding: 30px 20px 20px;
  }

  .record-code {
    display: none;
  }
}

@media (max-width: 620px) {
  .archive-desk {
    padding-right: 10px;
    padding-left: 10px;
  }

  .paper-document {
    padding: 26px 14px 18px;
  }

  .section-tab,
  .action-tab {
    padding: 0 15px;
    font-size: 13px;
  }

  .document-meta span:first-child,
  .document-footer span:last-child {
    display: none;
  }
}
</style>
