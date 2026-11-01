<template>
  <div class="frontend-layout">
    <!-- 顶部导航栏 -->
    <header class="site-header">
      <div class="navbar-container">
        <!-- 品牌 Logo + 名称 -->
        <router-link to="/" class="brand-section" aria-label="返回首页">
          <span class="brand-mark" aria-hidden="true">档</span>
          <span class="brand-name">个人心理档案</span>
        </router-link>

        <!-- 导航区：根据登录状态动态切换内容 -->
        <nav class="nav-section" aria-label="主要导航">
          <router-link to="/" class="nav-link">首页</router-link>
          <!-- 登录后才显示的功能入口 -->
          <router-link v-if="isLoggedIn" to="/emotion-diary" class="nav-link">情绪记录</router-link>
          <router-link v-if="isLoggedIn" to="/consultation" class="nav-link">AI 对话</router-link>

          <!-- 登录状态：显示"退出"按钮 -->
          <button v-if="isLoggedIn" class="text-button" type="button" @click="handleLogout">
            退出
          </button>
          <!-- 未登录状态：显示"登录" + "注册" -->
          <template v-else>
            <router-link to="/auth/login" class="nav-link">登录</router-link>
            <router-link to="/auth/register" class="register-link">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 主内容区：flex:1 自动填满剩余高度 -->
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { logout } from '@/api/admin'

const router = useRouter()

// 登录状态：从 localStorage 的 token 判断（页面刷新后仍保持状态）
const isLoggedIn = ref(Boolean(localStorage.getItem('token')))

// 清除登录态：删除 token / userInfo，跳转登录页
const clearLoginState = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  isLoggedIn.value = false
  router.push('/auth/login')
}

// 退出登录：先调用后端 API，无论成功与否都清理本地状态
// 使用 try...finally 确保即使后端接口报错，用户也能退出
const handleLogout = async () => {
  try {
    await logout()
  } finally {
    clearLoginState()
  }
}
</script>

<style scoped lang="scss">
/* ===== 根容器：纵向 flex，导航栏固定在顶部 ===== */
.frontend-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #d9cfbb;
}

/* ===== 站点导航栏 ===== */
.site-header {
  position: relative;
  z-index: 20;
  border-bottom: 2px solid #382f29;
  background: #f6edda;
  box-shadow: 0 3px 0 rgba(56, 47, 41, 0.12);   /* 底部硬阴影，拟物 */
}

/* 导航栏内容居中容器 */
.navbar-container {
  width: min(1180px, calc(100% - 40px));
  min-height: 76px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
}

/* ===== 品牌区 ===== */
.brand-section {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #382f29;
}

/* 方形徽标：暗酒红边框 + 旋转 -3° */
.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border: 2px solid #8f3d32;
  color: #8f3d32;
  background: #f9f0df;
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: 22px;
  font-weight: 800;
  transform: rotate(-3deg);
}

.brand-name {
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

/* ===== 导航区 ===== */
.nav-section {
  display: flex;
  align-items: center;
  gap: 26px;
}

/* nav-link / text-button / register-link 共用的字体基础样式 */
.nav-link,
.text-button,
.register-link {
  color: #5f5249;
  font-family: inherit;
  font-size: 15px;
  font-weight: 700;
}

/* nav-link 和 text-button：hover 时底部显示暗酒红下划线 */
/* 通过 ::after 的 scaleX(0→1) 实现平滑展开动画 */
.nav-link,
.text-button {
  position: relative;
  padding: 8px 0;

  &::after {
    content: '';
    position: absolute;
    right: 0;
    bottom: 2px;
    left: 0;
    height: 2px;
    background: #8f3d32;
    transform: scaleX(0);
    transform-origin: center;
    transition: transform 0.18s ease;
  }

  &:hover,
  &.router-link-exact-active {
    color: #8f3d32;

    &::after {
      transform: scaleX(1);
    }
  }
}

/* 退出按钮：纯文字，无背景 */
.text-button {
  border: 0;
  background: transparent;
  cursor: pointer;
}

/* 注册按钮：米黄底 + 硬阴影 + 不对称圆角，强调 CTA */
.register-link {
  padding: 8px 16px;
  border: 2px solid #382f29;
  border-radius: 4px 7px 5px 6px;
  background: #dfc996;
  box-shadow: 2px 3px 0 rgba(56, 47, 41, 0.2);

  &:hover {
    color: #8f3d32;
    transform: translateY(-1px);   /* hover 时微抬 */
  }
}

/* 主内容区：自动填满剩余高度 */
.main-content {
  flex: 1;
}

/* ===== 响应式 ===== */
@media (max-width: 760px) {
  .navbar-container {
    width: min(100% - 28px, 1180px);
    min-height: 68px;
  }

  /* 移动端隐藏品牌名，只保留徽标 */
  .brand-name {
    display: none;
  }

  .brand-mark {
    width: 38px;
    height: 38px;
    font-size: 20px;
  }

  .nav-section {
    gap: 14px;
  }

  .nav-link,
  .text-button,
  .register-link {
    font-size: 14px;
  }
}
</style>
