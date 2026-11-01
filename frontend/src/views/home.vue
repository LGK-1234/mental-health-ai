<template>
  <main class="paper-home">
    <article class="dossier-card paper card shadow">
      <div class="dossier-strip">
        <span>PERSONAL FILE</span>
        <span>NO. 2026-01</span>
      </div>

      <div class="card-body">
        <p class="eyebrow">个人心理档案</p>
        <h1>
          把零散的感受，
          <span>整理成看得见的自己。</span>
        </h1>
        <p class="summary">
          记录当下的情绪与想法，在持续的观察中了解自己的变化。
        </p>

        <div class="actions">
          <button class="paper-btn btn-primary" type="button" @click="handlePrimaryAction">
            {{ isLoggedIn ? '开始记录' : '登录后开始' }}
          </button>
          <button
            v-if="isLoggedIn"
            class="paper-btn btn-secondary-outline"
            type="button"
            @click="router.push('/consultation')"
          >
            打开 AI 对话
          </button>
          <button
            v-else
            class="paper-btn btn-secondary-outline"
            type="button"
            @click="router.push('/auth/register')"
          >
            创建账号
          </button>
        </div>
      </div>

      <div class="dossier-footer">
        <span>PRIVATE</span>
        <span>仅用于个人记录</span>
      </div>
    </article>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = ref(Boolean(localStorage.getItem('token')))

const handlePrimaryAction = () => {
  router.push(isLoggedIn.value ? '/emotion-diary' : '/auth/login')
}
</script>

<style scoped src="papercss/dist/paper.min.css"></style>

<style scoped lang="scss">
.paper-home {
  --ink: #382f29;
  --muted-ink: #74675d;
  --paper: #f6edda;
  --accent: #8f3d32;

  min-height: calc(100vh - 78px);
  padding: clamp(36px, 7vw, 88px) 20px;
  display: grid;
  place-items: center;
  color: var(--ink);
  background-color: #d9cfbb;
  background-image:
    radial-gradient(rgba(56, 47, 41, 0.08) 0.7px, transparent 0.7px),
    linear-gradient(115deg, rgba(255, 255, 255, 0.25), transparent 45%);
  background-size: 8px 8px, 100% 100%;
}

.dossier-card {
  position: relative;
  width: min(760px, 100%);
  min-height: 430px;
  margin: 0;
  padding: 0;
  overflow: visible;
  border-color: var(--ink);
  color: var(--ink);
  background: var(--paper);
  transform: rotate(-0.35deg);

  &:hover {
    transform: rotate(-0.35deg);
  }

  &::before {
    content: '';
    position: absolute;
    top: -17px;
    left: 42px;
    width: 126px;
    height: 34px;
    border: 2px solid var(--ink);
    border-bottom: 0;
    border-radius: 8px 8px 0 0;
    background: #c8b17e;
    z-index: -1;
  }
}

.dossier-strip,
.dossier-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 22px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.dossier-strip {
  border-bottom: 2px solid var(--ink);
  background: #dfc996;
}

.dossier-card .card-body {
  padding: clamp(42px, 7vw, 72px) clamp(28px, 8vw, 80px);
}

.eyebrow {
  display: inline-block;
  margin: 0 0 18px;
  padding: 4px 10px;
  border: 1.5px solid var(--accent);
  color: var(--accent);
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.18em;
  transform: rotate(-1.2deg);
}

h1 {
  max-width: 620px;
  margin: 0;
  color: var(--ink);
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: clamp(34px, 5.6vw, 58px);
  font-weight: 800;
  line-height: 1.25;
  letter-spacing: -0.04em;

  span {
    color: var(--accent);
  }
}

.summary {
  max-width: 560px;
  margin: 24px 0 0;
  color: var(--muted-ink);
  font-size: 16px;
  line-height: 1.9;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 34px;

  .paper-btn {
    min-width: 132px;
    margin: 0;
    border-color: var(--ink);
    box-shadow: 3px 4px 0 rgba(56, 47, 41, 0.22);
    font-family: inherit;
    font-weight: 700;
  }

  .btn-primary {
    border-color: #6f2f28;
    background: var(--accent);
  }

  .btn-secondary-outline {
    color: var(--ink);
    background: transparent;
  }
}

.dossier-footer {
  border-top: 1.5px dashed rgba(56, 47, 41, 0.65);
  color: var(--muted-ink);
  font-size: 11px;
}

@media (max-width: 640px) {
  .paper-home {
    align-items: start;
    min-height: calc(100vh - 68px);
    padding-top: 54px;
  }

  .dossier-card {
    min-height: 0;
    transform: none;

    &::before {
      left: 24px;
    }
  }

  .dossier-strip,
  .dossier-footer {
    padding-inline: 16px;
    letter-spacing: 0.06em;
  }

  .dossier-card .card-body {
    padding: 40px 24px 44px;
  }

  .actions {
    flex-direction: column;

    .paper-btn {
      width: 100%;
    }
  }
}
</style>
