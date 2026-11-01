<!--
  MarkdownRenderer.vue —— 轻量级 Markdown 渲染器

  【设计意图】
  项目没有引入完整的 Markdown 解析库（如 marked / markdown-it），
  而是自己用正则实现了 13 步顺序转换，覆盖聊天场景最常用的 Markdown 语法。
  优点是零依赖、体积小、可控性强；缺点是不支持嵌套和复杂语法。

  【渲染流程（13 步 pipeline）】
  1. HTML 转义（防 XSS）→ 2. 代码块 → 3. 行内代码 → 4. 粗体 → 5. 斜体
  → 6. 标题(h3→h2→h1，顺序很重要) → 7. 链接 → 8. 无序列表 → 9. 有序列表
  → 10. 引用 → 11. 分割线 → 12. 换行 → 13. 清理多余 <br>

  【Props】
  - content    : Markdown 原始文本（必填）
  - isAiMessage: 是否为 AI 回复，true 时切换为蓝色主题

  【两套主题】
  - 默认主题（灰色系）：普通用户消息
  - AI 主题（蓝色系）：通过 isAiMessage=true 激活，blockquote 左边框变蓝、代码块背景变浅蓝等
-->

<template>
  <!-- 根容器 —— 根据 isAiMessage 动态添加 ai-markdown 类切换蓝色主题 -->
  <div class="markdown-content" :class="{ 'ai-markdown': isAiMessage }">
    <!-- v-html 注入渲染后的 HTML —— 已在 computed 中完成 XSS 转义 -->
    <div v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// 组件 props 定义
const props = defineProps({
  // Markdown 原始文本 —— 必填
  content: {
    type: String,
    required: true
  },
  // 是否为 AI 消息 —— 切换蓝色主题
  isAiMessage: {
    type: Boolean,
    default: false
  }
})

/**
 * Markdown → HTML 渲染管道（computed 缓存结果，content 变化时重算）
 *
 * 13 步正则替换，严格按顺序执行：
 * - 先处理「块级」语法（代码块、列表），再处理「行内」语法（粗体、斜体、行内代码）
 * - 标题必须从 h3→h2→h1 依次替换，避免 # 被 ### 的正则先匹配
 * - 有序列表和无序列表用同一 <li> 正则，最后包裹 <ul>
 */
const renderedContent = computed(() => {
  let html = props.content

  // ---------- Step 1: HTML 转义（防 XSS）----------
  // 先把所有 < > 转义为 &lt; &gt;，确保用户输入的标签不会被浏览器解析
  html = html.replace(/</g, '&lt;').replace(/>/g, '&gt;')

  // ---------- Step 2: 代码块 ----------
  // 匹配 ```lang\ncode\n``` 格式 —— 使用 ([\s\S]*?) 非贪婪匹配
  // lang 可选，默认 'text'；code.trim() 去掉首尾空行
  html = html.replace(/```(\w+)?\n([\s\S]*?)\n```/g, (match, lang, code) => {
    return `<pre class="code-block"><code class="language-${lang || 'text'}">${code.trim()}</code></pre>`
  })

  // ---------- Step 3: 行内代码 ----------
  // 匹配 `code` —— 注意：必须在粗体/斜体之前处理，否则 `**` 会被误解析
  html = html.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')

  // ---------- Step 4: 粗体 ----------
  // 匹配 **文本** —— 使用非贪婪 (.*?) 避免跨多个粗体块匹配
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')

  // ---------- Step 5: 斜体 ----------
  // 匹配 *文本* —— 同样非贪婪
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')

  // ---------- Step 6: 标题 ----------
  // 必须从 h3 → h2 → h1 顺序替换，否则 # 会先吃掉 ### 开头的行
  // ^ 和 $ 使用 gm 修饰符（多行模式），让 ^/$ 匹配每一行的首尾
  html = html.replace(/^### (.*$)/gm, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gm, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gm, '<h1>$1</h1>')

  // ---------- Step 7: 链接 ----------
  // 匹配 [文本](URL) —— 添加 target="_blank" 新标签页打开 + rel="noopener noreferrer" 安全属性
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>')

  // ---------- Step 8: 无序列表 ----------
  // 先把每行 "- xxx" 转为 <li>xxx</li>，再把连续的 li 包裹成 <ul>
  // /s 修饰符让 . 匹配换行 —— 这里使用 (li>.*</li>) 贪婪匹配所有 li
  html = html.replace(/^- (.*)$/gm, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')

  // ---------- Step 9: 有序列表 ----------
  // 只处理转换为 <li>（HTML 中 <ol> 自动编号），此处未包裹 <ol>
  // 注：这是一个简化实现，连续有序列表项可能不会自动包裹成 <ol>
  html = html.replace(/^\d+\. (.*)$/gm, '<li>$1</li>')

  // ---------- Step 10: 引用 ----------
  // 匹配 > 开头的行，转为 <blockquote>
  html = html.replace(/^> (.*)$/gm, '<blockquote>$1</blockquote>')

  // ---------- Step 11: 分割线 ----------
  // 匹配单独一行的 ---，转为 <hr>
  html = html.replace(/^---$/gm, '<hr>')

  // ---------- Step 12: 换行 ----------
  // 所有 \n 转为 <br> —— 这是最简单的换行处理，不区分"软换行"和"硬换行"
  html = html.replace(/\n/g, '<br>')

  // ---------- Step 13: 清理连续 <br> ----------
  // 上一步会在空行处产生 <br><br>，合并为单个 <br>
  html = html.replace(/<br><br>/g, '<br>')

  return html
})
</script>

<!-- Scoped 样式 —— 包含默认灰色主题 + AI 蓝色主题 -->
<style scoped>
/* ---------- 基础容器 ---------- */
.markdown-content {
  line-height: 1.6;    /* 舒适的阅读行高 */
  color: inherit;      /* 继承父级文字颜色，方便嵌入不同背景 */
}

/* ---------- 标题样式 ---------- */
/* 三级标题统一加粗 + 下边距 */
.markdown-content h1,
.markdown-content h2,
.markdown-content h3 {
  margin: 1em 0 0.5em 0;  /* 上间距大，下间距小 —— 标题紧贴正文 */
  font-weight: 600;
  line-height: 1.3;
}

/* h1 —— 加底部边框线区分层级 */
.markdown-content h1 {
  font-size: 1.5em;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.3em;
}

/* h2 —— 深灰蓝文字 */
.markdown-content h2 {
  font-size: 1.3em;
  color: #374151;
}

/* h3 —— 中灰蓝文字 */
.markdown-content h3 {
  font-size: 1.1em;
  color: #4b5563;
}

/* ---------- 段落 ---------- */
.markdown-content p {
  margin: 0.5em 0;
}

/* ---------- 列表 ---------- */
.markdown-content ul,
.markdown-content ol {
  margin: 0.5em 0;
  padding-left: 1.5em;   /* 左侧缩进给项目符号/数字留空间 */
}

.markdown-content li {
  margin: 0.3em 0;
}

/* ---------- 引用块 ---------- */
/* 默认灰色主题：左侧灰色竖线 + 浅灰背景 + 斜体 */
.markdown-content blockquote {
  border-left: 4px solid #d1d5db;
  padding-left: 1em;
  margin: 1em 0;
  color: #6b7280;
  font-style: italic;
  background: #f9fafb;
  border-radius: 0 0.5em 0.5em 0;  /* 仅右侧圆角 */
  padding: 0.5em 1em;
}

/* AI 蓝色主题 —— 覆盖引用块样式 */
.ai-markdown blockquote {
  border-left-color: #3b82f6;   /* 左侧竖线变蓝 */
  background: #eff6ff;          /* 浅蓝底 */
}

/* ---------- 分割线 ---------- */
.markdown-content hr {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 1.5em 0;
}

/* ---------- 行内代码 ---------- */
/* 等宽字体 + 浅灰底 + 玫红文字 —— 灰色主题 */
.markdown-content code.inline-code {
  background: #f3f4f6;
  padding: 0.2em 0.4em;
  border-radius: 0.25em;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.85em;
  color: #e11d48;              /* 玫红色，在浅灰底上醒目 */
}

/* AI 主题 —— 行内代码变蓝 */
.ai-markdown code.inline-code {
  background: #dbeafe;
  color: #1e40af;
}

/* ---------- 代码块 ---------- */
/* 深色代码块 —— 暗色背景 + 亮色文字，等宽字体 */
.markdown-content pre.code-block {
  background: #1f2937;          /* 深灰黑底 —— 模拟终端 */
  color: #f9fafb;              /* 近白文字 */
  padding: 1em;
  border-radius: 0.5em;
  overflow-x: auto;            /* 长行横向滚动 */
  margin: 1em 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.85em;
  line-height: 1.4;
}

/* 代码块内的 code —— 清除 inherited 样式 */
.markdown-content pre.code-block code {
  background: none;
  padding: 0;
  color: inherit;
}

/* ---------- 链接 ---------- */
/* 默认无下划线，hover 时出现下划线 —— 现代极简风格 */
.markdown-content a {
  color: #3b82f6;
  text-decoration: none;
  border-bottom: 1px solid transparent;    /* 预留 hover 下划线空间，避免文字跳动 */
  transition: border-color 0.2s ease;
}

.markdown-content a:hover {
  border-bottom-color: #3b82f6;           /* hover 时显示下划线 */
}

/* AI 主题 —— 链接变深蓝 */
.ai-markdown a {
  color: #1e40af;
}

.ai-markdown a:hover {
  border-bottom-color: #1e40af;
}

/* ---------- 粗体 ---------- */
.markdown-content strong {
  font-weight: 600;
  color: #374151;              /* 深灰 —— 加粗但不刺眼 */
}

/* AI 主题 —— 粗体变深蓝 */
.ai-markdown strong {
  color: #1e40af;
}

/* ---------- 斜体 ---------- */
.markdown-content em {
  font-style: italic;
  color: #6b7280;              /* 中灰 —— 斜体稍弱化 */
}
</style>
