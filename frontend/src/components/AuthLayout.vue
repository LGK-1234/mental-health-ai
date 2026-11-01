<template>
  <!-- 根容器：整个认证页的背景，用多层渐变模拟档案袋的麻布纹理 -->
  <div class="auth-layout">
    <!-- 固定在左上角的返回首页链接，悬浮时向左微动效 -->
    <router-link class="home-link" to="/" aria-label="返回首页">
      <span aria-hidden="true">←</span>
      返回首页
    </router-link>

    <!-- 核心舞台：模拟立体档案袋的容器，内部三层结构叠放 -->
    <main class="archive-stage" aria-label="个人心理档案账户入口">
      <!-- === 档案袋背板（最底层，z-index: 0）=== -->
      <div class="folder-back" aria-hidden="true">
        <!-- 档案袋顶部的标签牌 -->
        <div class="folder-tab">PERSONAL ARCHIVE</div>
        <!-- 背板上的装饰虚线 -->
        <div class="folder-back-line"></div>
      </div>

      <!-- === 文件插槽层（中间层，z-index: 2）=== -->
      <!-- 用 perspective 实现3D透视，让文件切换动画有立体感 -->
      <div class="document-slot">
        <!-- router-view 用 v-slot 拿到当前路由的 Component 和 route -->
        <router-view v-slot="{ Component, route }">
          <!-- 过渡动画：mode="out-in" 确保旧文件先退出（缩回档案袋），新文件再进入（从档案袋拉出） -->
          <transition name="file-swap" mode="out-in">
            <article
              :key="route.path"
              class="document-sheet"
              :class="{ 'register-sheet': route.path.includes('register') }"
            >
              <!-- 文件头部：左侧圆形徽章 + 右侧标题文案 -->
              <header class="document-heading">
                <!-- 圆形文件徽章，旋转-6度增加拟物感 -->
                <div class="file-emblem" aria-hidden="true">
                  <span>PERSONAL</span>
                  <strong>档</strong>
                  <span>ARCHIVE</span>
                </div>

                <!-- 标题文案区：英文小字眉 + 中文大标题 + 描述 -->
                <div class="heading-copy">
                  <p class="eyebrow">PERSONAL FILE · PRIVATE</p>
                  <h1>个人心理档案</h1>
                  <p>记录感受，整理变化，留下只属于你的个人观察。</p>
                </div>
              </header>

              <!-- 分割线 + 动态编号：根据当前路由显示不同编号 -->
              <!-- 登录页显示 ACCESS RECORD / 01，注册页显示 NEW RECORD / 02 -->
              <div class="document-rule" aria-hidden="true">
                <span>{{ route.path.includes('register') ? 'NEW RECORD / 02' : 'ACCESS RECORD / 01' }}</span>
              </div>

              <!-- 动态渲染：登录页渲染 LoginForm，注册页渲染 RegisterForm -->
              <component :is="Component" />

              <!-- 文件底部：保密标识 -->
              <footer class="document-footer">
                <span>CONFIDENTIAL</span>
                <span>仅用于个人记录</span>
              </footer>
            </article>
          </transition>
        </router-view>
      </div>

      <!-- === 档案袋前盖（最顶层，z-index: 3）=== -->
      <!-- 覆盖在文件底部，形成"文件插在档案袋里"的视觉效果 -->
      <div class="folder-front" aria-hidden="true">
        <span class="folder-label">NO. 2026-01</span>
        <span class="folder-note">PERSONAL · CONFIDENTIAL</span>
      </div>
    </main>
  </div>
</template>

<style scoped lang="scss">
/* ===== CSS 变量：定义整套"纸质档案"的色彩系统 ===== */
.auth-layout {
  --ink: #352e29;          /* 墨色：文字、边框主色 */
  --muted-ink: #786b60;    /* 淡墨：次要文字、辅助线 */
  --paper: #fffaf0;        /* 纸白：文件页背景（暖白偏黄） */
  --folder: #bda982;       /* 档案袋米色：背板+标签 */
  --folder-dark: #9f8961;  /* 档案袋深米色：前盖，制造层次 */
  --accent: #8f3d32;       /* 强调色：暗酒红，用于徽章、点缀 */

  min-height: 100vh;
  padding: 54px 24px 76px;
  color: var(--ink);
  background-color: #ded2b9;
  /* 多层渐变叠加，模拟档案袋麻布的编织纹理 */
  background-image:
    linear-gradient(rgba(73, 59, 46, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(73, 59, 46, 0.025) 1px, transparent 1px),
    radial-gradient(rgba(73, 59, 46, 0.12) 0.65px, transparent 0.65px);
  background-size: 28px 28px, 28px 28px, 7px 7px;
  overflow-x: hidden;
}

/* ===== 返回首页链接 ===== */
.home-link {
  position: fixed;       /* 固定在视口左上角，不随滚动移动 */
  top: 28px;
  left: 34px;
  z-index: 10;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  color: var(--ink);
  border: 1px solid transparent;
  border-radius: 3px;
  font-size: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: background-color 180ms ease, border-color 180ms ease, transform 180ms ease;

  span {
    font-family: Georgia, serif;
    font-size: 20px;
    line-height: 1;
  }

  /* hover 效果：浅纸底 + 细边框 + 向左微移2px，模拟"拿起"感觉 */
  &:hover {
    color: var(--ink);
    border-color: rgba(53, 46, 41, 0.36);
    background: rgba(255, 250, 240, 0.52);
    transform: translateX(-2px);
  }
}

/* ===== 核心舞台：档案袋容器 ===== */
.archive-stage {
  position: relative;
  box-sizing: border-box;
  width: min(760px, 100%);   /* 最大760px，移动端自适应 */
  min-height: 720px;
  margin: 0 auto;
  padding: 70px 24px 92px;
  isolation: isolate;        /* 创建独立层叠上下文，防止子元素z-index溢出 */
}

/* ===== 档案袋背板（最底层 z-index: 0）===== */
/* 轻微旋转0.25°模拟真实档案袋放置的歪斜感 */
.folder-back {
  position: absolute;
  box-sizing: border-box;
  inset: 106px 0 36px;
  z-index: 0;
  border: 2px solid var(--ink);
  border-radius: 7px 12px 10px 8px;  /* 不对称圆角，更拟物 */
  background:
    linear-gradient(103deg, transparent 0 48%, rgba(62, 48, 34, 0.065) 49%, transparent 51%),
    var(--folder);
  box-shadow: 11px 15px 0 rgba(53, 46, 41, 0.13);  /* 硬阴影，模拟纸质档案的投影 */
  transform: rotate(0.25deg);
}

/* 背板顶部的标签牌 */
.folder-tab {
  position: absolute;
  box-sizing: border-box;
  top: -39px;              /* 向上伸出背板外 */
  left: 26px;
  min-width: 225px;
  height: 40px;
  padding: 13px 20px 0;
  border: 2px solid var(--ink);
  border-bottom: 0;        /* 底部不画边框，和背板无缝衔接 */
  border-radius: 8px 12px 0 0;
  background: var(--folder);
  font-family: 'Courier New', monospace;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

/* 背板上的装饰虚线 */
.folder-back-line {
  position: absolute;
  inset: 35px 30px auto;
  border-top: 1px dashed rgba(53, 46, 41, 0.38);
}

/* ===== 文件插槽层（中间层 z-index: 2）===== */
.document-slot {
  position: relative;
  z-index: 2;
  width: min(620px, calc(100% - 34px));
  margin: 0 auto;
  perspective: 1200px;     /* 3D透视距离，让文件切换动画有景深 */
}

/* ===== 文件页（核心视觉主体）===== */
/* 轻微旋转-0.22°，和背板旋转方向相反，错落有致 */
.document-sheet {
  position: relative;
  box-sizing: border-box;
  min-height: 610px;
  padding: 42px 52px 30px;
  border: 2px solid var(--ink);
  border-radius: 4px 8px 5px 9px;
  background-color: var(--paper);
  /* 背景：横线（模拟稿纸）+ 颗粒点（模拟纸张纹理）*/
  background-image:
    linear-gradient(rgba(143, 61, 50, 0.035) 1px, transparent 1px),
    radial-gradient(rgba(53, 46, 41, 0.08) 0.55px, transparent 0.55px);
  background-size: 100% 30px, 6px 6px;
  box-shadow: 7px 9px 0 rgba(53, 46, 41, 0.16);
  transform: rotate(-0.22deg);
  transform-origin: 50% 100%;  /* 旋转原点在底部中心，配合拉出动画更自然 */

  /* 伪元素：文件内圈的细边框（模拟真实纸张的折痕边）*/
  &::before {
    content: '';
    position: absolute;
    inset: 13px;
    pointer-events: none;
    border: 1px solid rgba(53, 46, 41, 0.18);
  }

  /* 伪元素：右上角的 COPY 01 水印，旋转2°增加拟物感 */
  &::after {
    content: 'COPY 01';
    position: absolute;
    top: 22px;
    right: 24px;
    color: rgba(143, 61, 50, 0.66);
    font-family: 'Courier New', monospace;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.12em;
    transform: rotate(2deg);
  }
}

/* 注册页文件更高，因为注册表单字段更多 */
.register-sheet {
  min-height: 735px;
}

/* ===== 文件头部 ===== */
.document-heading {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 88px 1fr;  /* 88px徽章 + 剩余空间标题 */
  align-items: center;
  gap: 24px;
}

/* 左侧圆形文件徽章：双线边框 + 旋转-6° + 暗酒红色 */
.file-emblem {
  width: 84px;
  height: 84px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--accent);
  border: 3px double var(--accent);  /* 双线边框，公文印章风格 */
  border-radius: 50%;
  font-family: 'Courier New', monospace;
  font-size: 7px;
  font-weight: 700;
  letter-spacing: 0.13em;
  transform: rotate(-6deg);

  strong {
    margin: 1px 0 2px;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;  /* 衬线字体，公文感 */
    font-size: 31px;
    line-height: 1;
  }
}

/* 右侧标题文案区 */
.heading-copy {
  min-width: 0;

  .eyebrow {
    margin: 0 0 7px;
    color: var(--accent);
    font-family: 'Courier New', monospace;
    font-size: 10px;
    font-weight: 700;
    letter-spacing: 0.15em;
  }

  h1 {
    margin: 0 0 8px;
    font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
    font-size: clamp(27px, 4vw, 36px);  /* 响应式字号 */
    font-weight: 800;
    line-height: 1.18;
  }

  > p:last-child {
    margin: 0;
    color: var(--muted-ink);
    font-size: 13px;
    line-height: 1.7;
  }
}

/* ===== 分割线 + 编号 ===== */
.document-rule {
  position: relative;
  z-index: 1;
  margin: 28px 0 24px;
  border-top: 1px solid var(--ink);

  /* 编号放在分割线上方，用 paper 背景遮住线的一部分，形成"穿线"效果 */
  span {
    position: absolute;
    top: -8px;
    right: 0;
    padding-left: 10px;
    color: var(--muted-ink);
    background: var(--paper);
    font-family: 'Courier New', monospace;
    font-size: 9px;
    font-weight: 700;
    letter-spacing: 0.1em;
  }
}

/* ===== 文件底部：保密标识 ===== */
.document-footer {
  position: relative;
  z-index: 1;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 27px;
  padding-top: 13px;
  color: var(--muted-ink);
  border-top: 1px dashed rgba(53, 46, 41, 0.42);  /* 虚线分割，比顶部实线轻 */
  font-family: 'Courier New', 'Microsoft YaHei', monospace;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.1em;
}

/* ===== 档案袋前盖（最顶层 z-index: 3）===== */
/* 盖在文件底部，露出前盖的圆形弧形上沿 + 标签 */
.folder-front {
  position: absolute;
  box-sizing: border-box;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3;
  height: 105px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 0 32px 24px;
  overflow: hidden;
  color: rgba(53, 46, 41, 0.76);
  border: 2px solid var(--ink);
  border-radius: 5px 8px 12px 9px;
  background:
    linear-gradient(175deg, rgba(255, 255, 255, 0.08), transparent 45%),
    var(--folder-dark);
  box-shadow: 8px 10px 0 rgba(53, 46, 41, 0.12);
  transform: rotate(-0.18deg);

  /* 伪元素：前盖的弧形上沿（露出底部的半圆），营造"档案袋盖着文件"的效果 */
  &::before {
    content: '';
    position: absolute;
    left: -8%;
    right: -8%;
    top: -66px;
    height: 100px;
    border-bottom: 2px solid rgba(53, 46, 41, 0.42);
    border-radius: 50%;
    background: var(--folder);
  }
}

.folder-label,
.folder-note {
  position: relative;
  z-index: 1;
  font-family: 'Courier New', monospace;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

/* ===== 文件切换动画 ===== */
/* 进入动画：从档案袋底部拉出（先opacity淡入，再从下往上位移，最后微回弹）*/
.file-swap-enter-active {
  animation: pull-file 720ms cubic-bezier(0.2, 0.78, 0.22, 1) both;
}

/* 退出动画：缩回档案袋底部（同时缩小+下沉+旋转）*/
.file-swap-leave-active {
  animation: return-file 480ms cubic-bezier(0.55, 0.02, 0.82, 0.38) both;
}

/* 退出动画：文件缩回到档案袋里 */
@keyframes return-file {
  0% {
    opacity: 1;
    transform: translateY(0) rotate(-0.22deg) scale(1);
  }
  74% {
    opacity: 0.95;
  }
  100% {
    opacity: 0;
    transform: translateY(185px) rotate(0.8deg) scale(0.965);  /* 下沉+转正+缩小 */
  }
}

/* 进入动画：文件从档案袋里被拉出来 */
@keyframes pull-file {
  0% {
    opacity: 0;
    transform: translateY(185px) rotate(0.8deg) scale(0.965);  /* 和退出结束状态一致 */
  }
  34% {
    opacity: 0.92;
  }
  76% {
    transform: translateY(-8px) rotate(-0.42deg) scale(1.002);  /* 微回弹，模拟惯性 */
  }
  100% {
    opacity: 1;
    transform: translateY(0) rotate(-0.22deg) scale(1);
  }
}

/* ===== 无障碍：尊重用户的"减少动画"偏好 ===== */
@media (prefers-reduced-motion: reduce) {
  .file-swap-enter-active,
  .file-swap-leave-active {
    animation-duration: 1ms;  /* 几乎瞬间完成，跳过动画 */
  }
}

/* ===== 响应式：680px 以下移动端适配 ===== */
@media (max-width: 680px) {
  .auth-layout {
    padding: 62px 10px 42px;
  }

  .home-link {
    top: 15px;
    left: 15px;
  }

  .archive-stage {
    padding: 58px 8px 70px;
  }

  .folder-back {
    inset: 92px 0 25px;
  }

  .folder-tab {
    left: 14px;
    min-width: 185px;
    padding-left: 14px;
    font-size: 9px;
  }

  .document-slot {
    width: calc(100% - 10px);
  }

  .document-sheet,
  .register-sheet {
    min-height: 0;
    padding: 34px 25px 26px;
  }

  .document-sheet::before {
    inset: 9px;
  }

  .document-sheet::after {
    top: 16px;
    right: 17px;
  }

  .document-heading {
    grid-template-columns: 62px 1fr;
    gap: 14px;
  }

  .file-emblem {
    width: 60px;
    height: 60px;
    font-size: 5px;

    strong {
      font-size: 23px;
    }
  }

  .heading-copy {
    h1 {
      font-size: 25px;
    }

    > p:last-child {
      font-size: 11px;
    }
  }

  .document-rule {
    margin: 23px 0 20px;
  }

  .folder-front {
    height: 78px;
    padding: 0 18px 15px;
  }

  .folder-note {
    display: none;  /* 移动端隐藏右侧次要文案 */
  }
}
</style>
