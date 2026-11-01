<template>
  <aside class="archive-sidebar" :class="{ collapsed: isCollapse }">
    <div class="folder-spine" aria-hidden="true"></div>

    <header class="brand">
      <div class="brand-mark" aria-hidden="true">档</div>
      <div v-if="!isCollapse" class="brand-copy">
        <p>PERSONAL ARCHIVE</p>
        <h1>心理档案管理处</h1>
        <span>内部资料目录</span>
      </div>
    </header>

    <button class="collapse-ticket" type="button" @click="toggleCollapse">
      <el-icon><component :is="isCollapse ? 'Expand' : 'Fold'" /></el-icon>
      <span v-if="!isCollapse">收起目录</span>
    </button>

    <div v-if="!isCollapse" class="index-label">
      <span>FILE INDEX</span>
      <span>04 ITEMS</span>
    </div>

    <nav class="folder-menu" aria-label="管理后台目录">
      <router-link
        v-for="(item, index) in menuItems"
        :key="item.path"
        :to="item.path"
        class="folder-item"
        :class="{ active: isMenuActive(item.path) }"
        :title="isCollapse ? item.title : undefined"
      >
        <span class="item-number">0{{ index + 1 }}</span>
        <el-icon class="item-icon"><component :is="item.icon" /></el-icon>
        <span v-if="!isCollapse" class="item-title">{{ item.title }}</span>
        <span v-if="!isCollapse" class="item-corner" aria-hidden="true"></span>
      </router-link>
    </nav>

    <div class="account-ticket">
      <div class="account-seal" aria-hidden="true">管</div>
      <div v-if="!isCollapse" class="account-copy">
        <span>当前管理员</span>
        <strong>{{ userName }}</strong>
      </div>
      <button class="logout-button" type="button" title="退出登录" @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span v-if="!isCollapse">退出</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'
import { logout } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const menuItems = [
  { path: '/back/dashboard', title: '数据分析', icon: 'DataAnalysis' },
  { path: '/back/knowledge', title: '文章数据', icon: 'Files' },
  { path: '/back/consultations', title: '咨询记录', icon: 'ChatLineSquare' },
  { path: '/back/emotional', title: '情绪日志', icon: 'Notebook' }
]

const isCollapse = computed(() => adminStore.isCollapse)
const userName = computed(() => {
  try {
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    return userInfo.nickname || userInfo.username || 'admin'
  } catch {
    return 'admin'
  }
})

const isMenuActive = (path) => {
  return route.path === path || route.path.startsWith(`${path}/`)
}

const toggleCollapse = () => adminStore.toggleCollapse()

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认收起资料并退出管理后台吗？', '退出确认', {
      confirmButtonText: '确认退出',
      cancelButtonText: '继续工作',
      type: 'warning',
      customClass: 'archive-message-box'
    })
  } catch {
    return
  }

  try {
    await logout()
  } finally {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/auth/login')
  }
}
</script>

<style lang="scss" scoped>
.archive-sidebar {
  --ink: #352e29;
  --muted: #74675d;
  --paper: #f7edd9;
  --folder: #bda982;
  --accent: #8f3d32;
  position: relative;
  z-index: 5;
  box-sizing: border-box;
  width: 280px;
  height: 100vh;
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  padding: 30px 18px 22px 23px;
  color: var(--ink);
  border-right: 2px solid var(--ink);
  background-color: #c7b58f;
  background-image:
    linear-gradient(90deg, rgba(53, 46, 41, 0.08) 1px, transparent 1px),
    radial-gradient(rgba(53, 46, 41, 0.1) 0.7px, transparent 0.7px);
  background-size: 28px 100%, 7px 7px;
  box-shadow: 8px 0 0 rgba(53, 46, 41, 0.08);
  transition: width 220ms ease, padding 220ms ease;

  &.collapsed {
    width: 82px;
    padding: 24px 10px 18px;

    .brand {
      justify-content: center;
      padding: 8px 0 18px;
    }

    .brand-mark {
      margin: 0;
    }

    .collapse-ticket {
      justify-content: center;
      width: 44px;
      margin: 8px auto 20px;
      padding: 8px;
    }

    .folder-item {
      justify-content: center;
      height: 54px;
      padding: 0;
    }

    .item-number {
      position: absolute;
      top: 4px;
      left: 5px;
      font-size: 7px;
    }

    .item-icon {
      margin: 0;
      font-size: 20px;
    }

    .account-ticket {
      justify-content: center;
      padding: 10px 6px;
    }

    .account-seal {
      display: none;
    }

    .logout-button {
      width: 38px;
      height: 38px;
      justify-content: center;
      padding: 0;
    }
  }
}

.folder-spine {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 8px;
  width: 4px;
  border-left: 1px solid rgba(53, 46, 41, 0.38);
  border-right: 1px solid rgba(255, 255, 255, 0.16);
}

.brand {
  position: relative;
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 5px 4px 25px;
  border-bottom: 1px dashed rgba(53, 46, 41, 0.52);
}

.brand-mark {
  width: 52px;
  height: 58px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: var(--accent);
  border: 2px solid var(--accent);
  background: var(--paper);
  font-family: 'Songti SC', SimSun, serif;
  font-size: 26px;
  font-weight: 800;
  box-shadow: 3px 3px 0 rgba(53, 46, 41, 0.18);
  transform: rotate(-3deg);
}

.brand-copy {
  min-width: 0;

  p {
    margin: 0 0 4px;
    color: var(--accent);
    font-family: 'Courier New', monospace;
    font-size: 8px;
    font-weight: 700;
    letter-spacing: 0.12em;
  }

  h1 {
    margin: 0 0 4px;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-size: 18px;
    line-height: 1.2;
    white-space: nowrap;
  }

  span {
    color: var(--muted);
    font-size: 11px;
  }
}

.collapse-ticket {
  display: flex;
  align-items: center;
  gap: 8px;
  width: max-content;
  margin: 12px 0 23px 4px;
  padding: 7px 11px;
  color: var(--ink);
  border: 1px solid rgba(53, 46, 41, 0.54);
  border-radius: 2px;
  background: rgba(247, 237, 217, 0.72);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 2px 2px 0 rgba(53, 46, 41, 0.11);
}

.index-label {
  display: flex;
  justify-content: space-between;
  margin: 0 8px 10px;
  color: rgba(53, 46, 41, 0.7);
  font-family: 'Courier New', monospace;
  font-size: 8px;
  font-weight: 700;
  letter-spacing: 0.11em;
}

.folder-menu {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.folder-item {
  position: relative;
  box-sizing: border-box;
  height: 58px;
  display: flex;
  align-items: center;
  padding: 0 17px 0 14px;
  color: var(--ink);
  border: 1.5px solid rgba(53, 46, 41, 0.62);
  border-radius: 3px 8px 5px 4px;
  background: rgba(239, 227, 204, 0.72);
  box-shadow: 3px 3px 0 rgba(53, 46, 41, 0.09);
  overflow: hidden;
  transition: transform 170ms ease, background-color 170ms ease, box-shadow 170ms ease;

  &:hover {
    color: var(--ink);
    background: #f1e4ca;
    transform: translateX(3px);
  }

  &.active {
    color: #fffaf0;
    border-color: var(--ink);
    background: var(--accent);
    box-shadow: 4px 4px 0 var(--ink);
    transform: translateX(5px) rotate(-0.2deg);

    .item-number {
      color: rgba(255, 250, 240, 0.72);
    }
  }
}

.item-number {
  width: 25px;
  color: var(--muted);
  font-family: 'Courier New', monospace;
  font-size: 9px;
  font-weight: 700;
}

.item-icon {
  margin-right: 11px;
  font-size: 18px;
}

.item-title {
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.06em;
}

.item-corner {
  position: absolute;
  right: -14px;
  bottom: -14px;
  width: 28px;
  height: 28px;
  border: 1px solid currentColor;
  transform: rotate(45deg);
  opacity: 0.22;
}

.account-ticket {
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: auto;
  padding: 13px 11px;
  border: 1.5px dashed rgba(53, 46, 41, 0.68);
  border-radius: 3px;
  background: rgba(255, 250, 240, 0.7);
  box-shadow: 3px 4px 0 rgba(53, 46, 41, 0.09);
}

.account-seal {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: var(--accent);
  border: 1.5px solid var(--accent);
  border-radius: 50%;
  font-family: 'Songti SC', SimSun, serif;
  font-weight: 800;
  transform: rotate(-8deg);
}

.account-copy {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;

  span {
    color: var(--muted);
    font-size: 9px;
  }

  strong {
    max-width: 92px;
    margin-top: 2px;
    overflow: hidden;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.logout-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 7px;
  color: var(--accent);
  border: 0;
  background: transparent;
  font-size: 11px;
  font-weight: 800;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
    text-underline-offset: 4px;
  }
}

@media (max-width: 760px) {
  .archive-sidebar {
    width: 82px;
    padding: 24px 10px 18px;

    .brand-copy,
    .index-label,
    .item-title,
    .item-corner,
    .account-copy,
    .logout-button span,
    .collapse-ticket span {
      display: none;
    }

    .brand {
      justify-content: center;
      padding: 8px 0 18px;
    }

    .brand-mark {
      margin: 0;
    }

    .collapse-ticket {
      justify-content: center;
      width: 44px;
      margin: 8px auto 20px;
      padding: 8px;
    }

    .folder-item {
      justify-content: center;
      height: 54px;
      padding: 0;
    }

    .item-number {
      position: absolute;
      top: 4px;
      left: 5px;
      width: auto;
      font-size: 7px;
    }

    .item-icon {
      margin: 0;
      font-size: 20px;
    }

    .account-ticket {
      justify-content: center;
      padding: 10px 6px;
    }

    .account-seal {
      display: none;
    }

    .logout-button {
      width: 38px;
      height: 38px;
      justify-content: center;
      padding: 0;
    }
  }
}
</style>
