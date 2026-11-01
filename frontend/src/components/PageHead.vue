<!--
  PageHead.vue —— 通用页面头部组件
  
  【设计意图】
  项目采用"纸质档案袋 + 文件页"的拟物设计风格，PageHead 作为每个子页面的头部，
  呈现为"文件页抬头"的视觉效果：上方小字 kicker（类似档案编号/分类），
  中间大标题，下方可选副标题，右侧通过插槽放置操作按钮。
  
  【使用场景】
  - 管理后台各数据页（知识库、咨询记录、情绪日志等）的顶部标题区
  - 通过 kicker 可动态显示档案编号（如 "ARCHIVE RECORD"）
  
  【Props】
  - title    : 主标题（文件页大标题）
  - kicker   : 小字标签（档案编号/分类标签，Courier 字体等宽显示）
  - subtitle : 副标题（可选，补充说明）
  
  【Slot】
  - buttons  : 右侧操作按钮区（通常放"新建""导出"等按钮）
-->

<template>
  <!-- 页面头部容器 —— flex 左右分布，左侧标题区，右侧按钮区 -->
  <header class="page-head">
    <!-- 左侧标题块：kicker + title + subtitle 纵向排列 -->
    <div class="title-block">
      <!-- 小字 kicker：档案编号/分类标签（等宽字体，9px，深红 #8f3d32）-->
      <p class="page-kicker">{{ props.kicker }}</p>
      <!-- 主标题：衬线字体（Noto Serif SC / 宋体），加粗，24~32px 响应式 -->
      <h2 class="page-title">{{ props.title }}</h2>
      <!-- 副标题 —— v-if 条件渲染，未传则不占位 -->
      <p v-if="props.subtitle" class="page-subtitle">{{ props.subtitle }}</p>
    </div>

    <!-- 右侧操作按钮区 —— 通过具名插槽注入按钮组 -->
    <div class="action-btn">
      <slot name="buttons"></slot>
    </div>
  </header>
</template>

<script setup>
// 定义组件 props
const props = defineProps({
  // 主标题 —— 文件页大标题
  title: {
    type: String,
    default: '页面标题'
  },
  // 小字标签 —— 档案编号/分类标识，Courier 等宽字体呈现"档案编号"质感
  kicker: {
    type: String,
    default: 'ARCHIVE RECORD'
  },
  // 副标题 —— 可选的补充说明文字
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<!-- SCSS Scoped 样式 —— 配合拟物"文件页"设计 -->
<style lang="scss" scoped>
/* ---------- 头部容器 ---------- */
.page-head {
  display: flex;                 /* flex 左右两栏布局 */
  align-items: flex-end;         /* 交叉轴底部对齐 —— 标题和按钮底部对齐更自然 */
  justify-content: space-between;
  gap: 24px;                     /* 两栏间距 */
  margin-bottom: 24px;           /* 与下方内容区的距离 */
  padding-bottom: 16px;          /* 内底边距 —— 为底部分割线留空间 */
  /* 底部分割线：深棕红 rgba —— 呼应档案袋主色调 */
  border-bottom: 1px solid rgba(53, 46, 41, 0.72);
}

/* 标题块 —— min-width:0 防止 flex 布局下文字溢出 */
.title-block {
  min-width: 0;
}

/* ---------- Kicker 小字标签 ---------- */
/* Courier New 等宽字体营造"档案编号/打字机标签"的复古质感 */
.page-kicker {
  margin: 0 0 6px;               /* 仅保留底部间距 */
  color: #8f3d32;                /* 深红（酒红）—— 档案袋主色调 */
  font-family: 'Courier New', monospace;
  font-size: 9px;                /* 极小字号，与下方大标题形成强烈对比 */
  font-weight: 700;
  letter-spacing: 0.14em;        /* 字间距拉大 —— 编号标签的排版习惯 */
}

/* ---------- 主标题 ---------- */
/* Noto Serif SC（思源宋体）/ Songti SC（苹果宋体）/ SimSun（系统宋体）— 衬线体符合纸质档案风格 */
.page-title {
  margin: 0;
  color: #352e29;                /* 深棕黑 —— 墨色 */
  font-family: 'Noto Serif SC', 'Songti SC', SimSun, serif;
  font-size: clamp(24px, 3vw, 32px);  /* 响应式字号：最小24px，视口3%，最大32px */
  font-weight: 800;              /* Extra Bold */
  line-height: 1.2;              /* 紧凑行高，大标题更有力 */
}

/* ---------- 副标题 ---------- */
.page-subtitle {
  margin: 7px 0 0;
  color: #74675d;                /* 中灰棕 —— 辅助文字色 */
  font-size: 12px;
}

/* ---------- 右侧按钮区 ---------- */
/* flex: 0 0 auto —— 不伸缩不缩小，保持内容原始宽度 */
.action-btn {
  flex: 0 0 auto;
}

/* ---------- 响应式：移动端（≤620px）---------- */
/* 窄屏下改为纵向堆叠：标题在上，按钮在下 */
@media (max-width: 620px) {
  .page-head {
    align-items: flex-start;     /* 交叉轴改为顶部对齐（纵向布局下更自然）*/
    flex-direction: column;      /* 主轴改为纵向 */
    gap: 14px;                   /* 纵向间距 */
  }
}
</style>
