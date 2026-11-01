<template>
  <div class="rich-text-editor">
    <!-- 富文本编辑器主体（wangEditor）-->
    <div class="editor-container">
      <!-- 工具栏：顶部按钮行 -->
      <WangToolbar 
        :editor="editorRef" 
        :defaultConfig="toolbarConfig" 
        mode="default" 
        class="editor-toolbar"
      />
      <!-- 编辑区：v-model 双向绑定 HTML 内容 -->
      <WangEditor
        v-model="content"
        :defaultConfig="editorConfig"
        mode="default"
        class="wang-editor"
        @onCreated="handleEditorCreated"
        @onChange="handleEditorChange"
        @onDestroyed="handleEditorDestroyed"
      />
    </div>

    <!-- 字数统计 + 进度条 -->
    <div v-if="showWordCount" class="editor-footer">
      <div class="word-count">
        <span class="count-text">{{ currentCharCount }} / {{ maxCharCount }}</span>
        <!-- 进度条：根据字数 / 上限比例显示，Math.min 防止超出 -->
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: Math.min((currentCharCount / maxCharCount) * 100, 100) + '%' }"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeUnmount, shallowRef, watch } from 'vue'
import { ElMessage } from 'element-plus'
import '@wangeditor/editor/dist/css/style.css'
import { Editor as WangEditor, Toolbar as WangToolbar } from '@wangeditor/editor-for-vue'

// ===== Props =====
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容...'
  },
  maxCharCount: {
    type: Number,
    default: 2000
  },
  showWordCount: {
    type: Boolean,
    default: true
  },
  showSecurityTip: {
    type: Boolean,
    default: true
  },
  toolbarKeys: {
    type: Array,
    default: () => [
      'bold', 'italic', 'underline', 'color', 'bgColor', '|',
      'fontSize', 'fontFamily', '|',
      'header1', 'header2', 'header3', '|',
      'bulletedList', 'numberedList', 'blockquote', '|',
      'insertLink', '|',
      'undo', 'redo'
    ]
  },
  minHeight: {
    type: String,
    default: '300px'
  }
})

// ===== Emits =====
const emit = defineEmits(['update:modelValue', 'change', 'created'])

// ===== 响应式数据 =====
// editorRef 用 shallowRef 包装避免深层响应式开销（wangEditor 实例不需要 reactive）
const editorRef = shallowRef(null)
const currentCharCount = ref(0)

// ===== 双向绑定 content（v-model 适配）=====
const content = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// ===== 编辑器配置 =====
const editorConfig = reactive({
  placeholder: props.placeholder,
  MENU_CONF: {
    // 字号列表
    fontSize: {
      fontSizeList: [
        '12px', '13px', '14px', '15px', '16px', '17px', '18px', 
        '19px', '20px', '22px', '24px', '26px', '28px', '30px', '32px', '36px'
      ]
    },
    // 字体列表：中英混排
    fontFamily: {
      fontFamilyList: [
        'Arial',
        'Tahoma',
        'Verdana',
        '"Times New Roman"',
        '"Courier New"',
        '"Microsoft YaHei"',
        '"微软雅黑"',
        '"SimSun"',
        '"宋体"',
        '"SimHei"',
        '"黑体"',
        '"KaiTi"',
        '"楷体"'
      ]
    },
    // 文字颜色：丰富的主题色板
    color: {
      colors: [
        // 基础颜色
        '#000000', '#333333', '#666666', '#999999', '#CCCCCC',
        // 治愈系主题色
        '#4A90E2', '#7ED321', '#F5A623', '#9013FE',
        // 红色系
        '#FF6B6B', '#FF4757', '#FF3838', '#FF2D2D', '#DC3545',
        // 橙色系
        '#FFA502', '#FF6348', '#FF7675', '#FDCB6E', '#F39C12',
        // 黄色系
        '#FFC312', '#F1C40F', '#F39801', '#FFD93D', '#FFDD59',
        // 绿色系
        '#2ED573', '#1DD1A1', '#10AC84', '#00B894', '#00A085',
        // 蓝色系
        '#3742FA', '#2F3542', '#40739E', '#487EB0', '#0984E3',
        // 紫色系
        '#8E44AD', '#9B59B6', '#A55EEA', '#3D5AFE', '#667AFA',
        // 粉色系
        '#FD79A8', '#E84393', '#FF7675', '#FF6B9D', '#FF5722'
      ]
    },
    // 背景色：浅色治愈系
    bgColor: {
      colors: [
        // 基础背景色
        '#FFFFFF', '#F8F9FA', '#E9ECEF', '#DEE2E6', '#CED4DA',
        // 浅色治愈系
        '#E3F2FD', '#E8F5E8', '#FFF3E0', '#F3E5F5',
        // 浅红色系
        '#FFEBEE', '#FCE4EC', '#F8BBD9', '#F48FB1',
        // 浅橙色系
        '#FFF3E0', '#FFE0B2', '#FFCC80', '#FFB74D',
        // 浅黄色系
        '#FFFDE7', '#FFF9C4', '#FFF176', '#FFEB3B',
        // 浅绿色系
        '#E8F5E8', '#C8E6C9', '#A5D6A7', '#81C784',
        // 浅蓝色系
        '#E3F2FD', '#BBDEFB', '#90CAF9', '#64B5F6',
        // 浅紫色系
        '#F3E5F5', '#E1BEE7', '#CE93D8', '#BA68C8',
        // 浅灰色系
        '#FAFAFA', '#F5F5F5', '#EEEEEE', '#E0E0E0'
      ]
    },
    // 行高选项
    lineHeight: {
      lineHeightList: ['1', '1.15', '1.2', '1.5', '1.75', '2', '2.5', '3']
    }
  }
})

// ===== 工具栏配置 =====
const toolbarConfig = reactive({
  toolbarKeys: props.toolbarKeys
})

// ===== 事件处理 =====

// 编辑器创建完成回调：保存实例、初始化字数、触发 created 事件
const handleEditorCreated = (editor) => {
  editorRef.value = editor
  
  updateCharCount()
  
  // 调试信息（开发时排查字体/菜单问题用）
  console.log('编辑器实例:', editor)
  console.log('工具栏配置:', editor.getConfig())
  
  const menus = editor.getAllMenuKeys()
  console.log('所有可用菜单:', menus)
  
  if (menus.includes('fontFamily')) {
    console.log('字体菜单已启用')
  } else {
    console.warn('字体菜单未启用')
  }
  
  emit('created', editor)
  console.log('富文本编辑器已创建')
}

// 编辑器内容变更回调：更新字数 + 触发 change 事件
const handleEditorChange = (editor) => {
  updateCharCount()
  
  emit('change', {
    html: editor.getHtml(),
    text: editor.getText()
  })
}

// 编辑器销毁回调
const handleEditorDestroyed = () => {
  editorRef.value = null
  console.log('富文本编辑器已销毁')
}

// 计算当前字数（去除多余空白后），超过上限时弹警告
const updateCharCount = () => {
  if (!editorRef.value) return
  
  const text = editorRef.value.getText()
  const cleanText = text.replace(/\s+/g, ' ').trim()
  currentCharCount.value = cleanText === '' ? 0 : cleanText.length
  
  if (currentCharCount.value > props.maxCharCount) {
    ElMessage.warning(`内容长度不能超过 ${props.maxCharCount} 字符`)
  }
}

// ===== 暴露给父组件的公共方法 =====
const getHtml = () => {
  return editorRef.value ? editorRef.value.getHtml() : ''
}

const getText = () => {
  return editorRef.value ? editorRef.value.getText() : ''
}

const setHtml = (html) => {
  if (editorRef.value) {
    editorRef.value.setHtml(html)
  }
}

const clear = () => {
  if (editorRef.value) {
    editorRef.value.clear()
  }
}

const insertText = (text) => {
  if (editorRef.value) {
    editorRef.value.insertText(text)
  }
}

const focus = () => {
  if (editorRef.value) {
    editorRef.value.focus()
  }
}

// 通过 defineExpose 让父组件用 ref 调用以上方法
defineExpose({
  getHtml,
  getText,
  setHtml,
  clear,
  insertText,
  focus,
  editor: editorRef
})

// 监听 placeholder 变化，动态更新编辑器占位符
watch(() => props.placeholder, (newPlaceholder) => {
  editorConfig.placeholder = newPlaceholder
})

// 组件销毁时手动销毁编辑器实例（wangEditor 要求）
onBeforeUnmount(() => {
  if (editorRef.value) {
    editorRef.value.destroy()
  }
})
</script>

<!-- ======================== Scoped Styles ======================== -->
<style scoped>
/* ---------- 编辑器外层容器 ---------- */
.rich-text-editor {
  border: 1px solid #e5e7eb;     /* 浅灰色边框，与 Element Plus 风格统一 */
  border-radius: 0.5rem;          /* 圆角 8px，柔和过渡 */
  overflow: hidden;               /* 裁剪内部溢出内容 */
  background: white;              /* 白底，突出编辑区 */
}

/* ---------- 编辑器内部容器：纵向排列工具栏 + 编辑区 ---------- */
.editor-container {
  display: flex;
  flex-direction: column;
}

/* 工具栏区域 —— 顶部边框分割线 */
.editor-toolbar {
  border-bottom: 1px solid #e5e7eb;
}

/* 编辑区最小高度 —— 通过 v-bind 动态绑定 props.minHeight */
.wang-editor {
  min-height: v-bind(minHeight);
}

/* ---------- 工具栏深度定制 ---------- */
/* 使用 :deep() 穿透 scoped，修改 wangEditor 原生样式 */
:deep(.w-e-toolbar) {
  border: none;           /* 去除默认边框，由外层 rich-text-editor 提供 */
  background: #f9fafb;    /* 浅灰底，与白色编辑区形成层级对比 */
  padding: 0.5rem;        /* 工具栏内边距 */
  flex-wrap: wrap;        /* 按钮过多时自动换行 */
}

/* 单个工具栏按钮项 */
:deep(.w-e-toolbar .w-e-bar-item) {
  margin: 0 0.125rem;     /* 按钮间距 */
  border-radius: 0.25rem; /* 按钮圆角 */
  height: 28px;           /* 固定高度 */
  min-width: 28px;        /* 最小宽度，保证视觉统一 */
}

/* 按钮 hover 态 */
:deep(.w-e-toolbar .w-e-bar-item:hover) {
  background: #e5e7eb;
}

/* 按钮激活态（当前光标处于对应格式时）—— 蓝色高亮 */
:deep(.w-e-toolbar .w-e-bar-item.w-e-bar-item-active) {
  background: #eaf2ff !important;   /* 浅蓝底 */
  color: #2563eb !important;       /* 深蓝文字 */
  outline: 1px solid #bfdbfe;       /* 外描边增强识别 */
}

/* 工具栏分割线 —— 放大间距让分组更清晰 */
:deep(.w-e-toolbar .w-e-bar-divider) {
  margin: 0 0.25rem;
}

/* ---------- 编辑区内容容器深度定制 ---------- */
:deep(.w-e-text-container) {
  background: white;
  padding: 1rem;          /* 内边距，让文字不贴边 */
  position: relative;     /* 为绝对定位的 placeholder 提供定位基准 */
}

/* 编辑区核心元素（Slate 编辑器根节点）*/
:deep(.w-e-text-container [data-slate-editor]) {
  min-height: v-bind(minHeight);  /* 动态最小高度 */
  padding: 0;
  text-align: left !important;    /* 强制左对齐，覆盖默认居中 */
  line-height: 1.6;               /* 舒适的行高 */
}

/* 段落 —— 覆盖 wangEditor 默认 margin */
:deep(.w-e-text-container [data-slate-editor] p) {
  text-align: left !important;
  margin: 0 0 0.5rem 0;   /* 只保留底部间距，段落间自然分隔 */
  padding: 0;
  line-height: inherit;   /* 继承父级 1.6 行高 */
}

/* 通配选择器 —— 不强制字体，让编辑器处理，避免覆盖用户选择 */
:deep(.w-e-text-container [data-slate-editor] *) {
}

/* ---------- 颜色面板样式 ---------- */
/* 最大宽度 + 内边距，避免面板过宽 */
:deep(.w-e-color-panel) {
  max-width: 300px;
  padding: 8px;
}

/* 颜色网格 —— 8 列等宽 */
:deep(.w-e-color-panel .w-e-color-list) {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

/* 单个颜色色块 */
:deep(.w-e-color-panel .w-e-color-item) {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  cursor: pointer;
  transition: all 0.2s ease;
}

/* 色块 hover —— 放大 + 蓝色阴影反馈 */
:deep(.w-e-color-panel .w-e-color-item:hover) {
  transform: scale(1.1);
  border-color: #4A90E2;
  box-shadow: 0 2px 4px rgba(74, 144, 226, 0.3);
}

/* ---------- 通用下拉选择列表 ---------- */
:deep(.w-e-select-list) {
  max-height: 200px;      /* 限制高度，超出滚动 */
  overflow-y: auto;
}

/* 列表项 */
:deep(.w-e-select-list .w-e-select-list-item) {
  padding: 8px 12px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

:deep(.w-e-select-list .w-e-select-list-item:hover) {
  background-color: #f8f9fa;
}

/* 当前选中项 —— 浅蓝高亮 */
:deep(.w-e-select-list .w-e-select-list-item.selected) {
  background-color: #e3f2fd;
  color: #4A90E2;
}

/* ---------- 编辑器 placeholder ---------- */
/* 绝对定位覆盖在编辑区上方，pointer-events: none 不阻挡输入 */
:deep(.w-e-text-placeholder) {
  color: #9ca3af;
  font-style: normal;
  text-align: left !important;
  padding: 0;
  margin: 0;
  line-height: 1.6;
  position: absolute;
  top: 1rem;
  left: 1rem;
  right: 1rem;
  pointer-events: none;
  white-space: pre-wrap;
  font-family: "Source Han Sans CN", "Microsoft YaHei", sans-serif;
}

/* ---------- wangEditor 通用下拉面板 ---------- */
/* z-index 设为 3000 确保浮层不被页面其他元素遮挡 */
:deep(.w-e-panel) {
  z-index: 3000 !important;
  pointer-events: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-radius: 0.375rem;
  border: 1px solid #e5e7eb;
  background: white;
  max-width: 320px;
}

/* ---------- 字体选择面板 ---------- */
/* 限制最大高度，超出滚动 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-family) {
  max-height: 200px;
  overflow-y: auto;
}

/* 每个字体项 —— 等宽分隔，点击可选中 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-family .w-e-panel-content-font-family-item) {
  padding: 8px 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  line-height: 1.4;
}

:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-family .w-e-panel-content-font-family-item:hover) {
  background-color: #f8f9fa;
}

/* 选中态 —— 浅蓝底 + 加粗 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-family .w-e-panel-content-font-family-item.selected) {
  background-color: #e3f2fd;
  color: #4A90E2;
  font-weight: 500;
}

/* ---------- 字号选择面板 ---------- */
/* 4 列网格排布字号选项 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-size) {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  padding: 8px;
}

/* 单个字号按钮 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-size .w-e-panel-content-font-size-item) {
  padding: 6px 8px;
  text-align: center;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  transition: all 0.2s ease;
}

:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-size .w-e-panel-content-font-size-item:hover) {
  background-color: #f8f9fa;
  border-color: #4A90E2;
}

/* 选中态 —— 实心蓝底 + 白字 */
:deep(.w-e-panel .w-e-panel-content .w-e-panel-content-font-size .w-e-panel-content-font-size-item.selected) {
  background-color: #4A90E2;
  color: white;
  border-color: #4A90E2;
}

/* 通用选中项 —— 在右侧追加 ✔ 对勾图标 */
:deep(.w-e-select-list .selected::after) {
  content: '✔';
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  color: #4A90E2;
  font-size: 12px;
}

/* ---------- 编辑器底部状态栏 ---------- */
/* 左右分布：左侧字数统计 + 进度条，右侧安全提示 */
.editor-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 1rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

/* 字数统计区域 */
.word-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: #6b7280;
}

.count-text {
  font-weight: 500;
}

/* 字数进度条 —— 窄长条，直观显示占用比例 */
.progress-bar {
  width: 60px;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

/* 进度条填充 —— 蓝色，随字数增长 */
.progress-fill {
  height: 100%;
  background: #4A90E2;
  transition: all 0.3s ease;
  border-radius: 2px;
}

/* 底部安全提示 —— 与 footer 同底色，左侧对齐 */
.security-tip {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.75rem;
  color: #6B7280;
  padding: 0.5rem 1rem;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
}

/* 安全图标 —— 绿色（#10B981 是治愈系主题色板中的成功绿）*/
.security-tip i {
  color: #10B981;
}

/* ---------- 响应式：移动端适配 ---------- */
@media (max-width: 768px) {
  /* 工具栏内边距缩小，给编辑区更多空间 */
  :deep(.w-e-toolbar) {
    padding: 0.375rem;
  }
  
  /* 按钮紧凑化 */
  :deep(.w-e-toolbar .w-e-bar-item) {
    margin: 0 0.0625rem;
    height: 24px;
    min-width: 24px;
    font-size: 0.875rem;
  }
  
  /* 编辑区内边距也略缩小 */
  :deep(.w-e-text-container) {
    padding: 0.75rem;
  }
  
  /* footer 内边距缩小 */
  .editor-footer {
    padding: 0.375rem 0.75rem;
  }
}
</style>